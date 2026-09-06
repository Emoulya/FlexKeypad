#!/usr/bin/env python3
"""
FlexKeypad Desktop Companion Bridge
------------------------------------
Receives low-latency input events sent from the FlexKeypad Android application
via USB cable (ADB reverse socket: 127.0.0.1:8899) and simulates physical keystrokes
on the host PC.

Prerequisites:
  1. Connect your Android device via USB with USB debugging enabled.
  2. Run: adb reverse tcp:8899 tcp:8899
  3. Run: python bridge.py
"""

import socket
import json
import sys
import time

PORT = 8899
HOST = "127.0.0.1"

# Try importing pynput or keyboard if available for cross-platform injection
keyboard_controller = None
try:
    from pynput.keyboard import Controller, Key, KeyCode
    keyboard_controller = Controller()
    print("[INFO] pynput keyboard controller initialized.")
except ImportError:
    print("[WARN] 'pynput' not installed. Keystrokes will be logged to console.")
    print("       To enable actual key injection, run: pip install pynput")


# Map USB HID KeyCodes to pynput keys
KEY_MAP = {
    0x04: 'a', 0x05: 'b', 0x06: 'c', 0x07: 'd', 0x08: 'e', 0x09: 'f',
    0x0A: 'g', 0x0B: 'h', 0x0C: 'i', 0x0D: 'j', 0x0E: 'k', 0x0F: 'l',
    0x10: 'm', 0x11: 'n', 0x12: 'o', 0x13: 'p', 0x14: 'q', 0x15: 'r',
    0x16: 's', 0x17: 't', 0x18: 'u', 0x19: 'v', 0x1A: 'w', 0x1B: 'x',
    0x1C: 'y', 0x1D: 'z',
    0x1E: '1', 0x1F: '2', 0x20: '3', 0x21: '4', 0x22: '5',
    0x23: '6', 0x24: '7', 0x25: '8', 0x26: '9', 0x27: '0',
    0x28: 'enter', 0x29: 'esc', 0x2A: 'backspace', 0x2B: 'tab', 0x2C: 'space',
    0x4C: 'delete', 0x4F: 'right', 0x50: 'left', 0x51: 'down', 0x52: 'up',
    0x3A: 'f1', 0x3B: 'f2', 0x3C: 'f3', 0x3D: 'f4', 0x3E: 'f5', 0x3F: 'f6',
    0x40: 'f7', 0x41: 'f8', 0x42: 'f9', 0x43: 'f10', 0x44: 'f11', 0x45: 'f12'
}

def resolve_pynput_key(code):
    if not keyboard_controller:
        return None
    name = KEY_MAP.get(code)
    if not name:
        return None
    if len(name) == 1:
        return KeyCode.from_char(name)
    special_keys = {
        'enter': Key.enter, 'esc': Key.esc, 'backspace': Key.backspace,
        'tab': Key.tab, 'space': Key.space, 'delete': Key.delete,
        'right': Key.right, 'left': Key.left, 'down': Key.down, 'up': Key.up,
        'f1': Key.f1, 'f2': Key.f2, 'f3': Key.f3, 'f4': Key.f4, 'f5': Key.f5,
        'f6': Key.f6, 'f7': Key.f7, 'f8': Key.f8, 'f9': Key.f9, 'f10': Key.f10,
        'f11': Key.f11, 'f12': Key.f12
    }
    return special_keys.get(name)

def main():
    print("==================================================")
    print("      FlexKeypad Desktop Companion Bridge         ")
    print("==================================================")
    print(f"Connecting to FlexKeypad app on {HOST}:{PORT}...")

    while True:
        try:
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            sock.connect((HOST, PORT))
            print(f"[CONNECTED] Successfully connected to FlexKeypad on port {PORT}!")

            buffer = ""
            while True:
                data = sock.recv(1024)
                if not data:
                    print("[DISCONNECTED] Connection closed by device.")
                    break

                buffer += data.decode("utf-8", errors="ignore")
                while "\n" in buffer:
                    line, buffer = buffer.split("\n", 1)
                    line = line.strip()
                    if not line:
                        continue
                    try:
                        event = json.loads(line)
                        action = event.get("action")
                        key_code = event.get("keyCode", 0)
                        modifiers = event.get("modifiers", [])

                        print(f"[{action}] KeyCode: 0x{key_code:02X} | Modifiers: {modifiers}")

                        if keyboard_controller:
                            target_key = resolve_pynput_key(key_code)
                            if action == "DOWN":
                                if "MODIFIER_LEFT_CTRL" in modifiers or "MODIFIER_RIGHT_CTRL" in modifiers:
                                    keyboard_controller.press(Key.ctrl)
                                if "MODIFIER_LEFT_ALT" in modifiers or "MODIFIER_RIGHT_ALT" in modifiers:
                                    keyboard_controller.press(Key.alt)
                                if "MODIFIER_LEFT_SHIFT" in modifiers or "MODIFIER_RIGHT_SHIFT" in modifiers:
                                    keyboard_controller.press(Key.shift)
                                if target_key:
                                    keyboard_controller.press(target_key)
                            elif action == "UP":
                                if target_key:
                                    keyboard_controller.release(target_key)
                                keyboard_controller.release(Key.ctrl)
                                keyboard_controller.release(Key.alt)
                                keyboard_controller.release(Key.shift)
                            elif action == "RELEASE_ALL":
                                keyboard_controller.release(Key.ctrl)
                                keyboard_controller.release(Key.alt)
                                keyboard_controller.release(Key.shift)
                    except json.JSONDecodeError:
                        pass
        except ConnectionRefusedError:
            print("[RETRY] App not detected or 'adb reverse' not active. Retrying in 2 seconds...", end="\r")
            time.sleep(2)
        except KeyboardInterrupt:
            print("\n[EXIT] Exiting companion bridge.")
            sys.exit(0)
        except Exception as e:
            print(f"\n[ERROR] {e}. Reconnecting in 2 seconds...")
            time.sleep(2)

if __name__ == "__main__":
    main()
