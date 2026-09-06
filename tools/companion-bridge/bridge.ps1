# FlexKeypad Desktop Companion Bridge (PowerShell with Win32 SendInput)
# ---------------------------------------------------------------------
# Injects real hardware-level keystrokes into Windows using user32.dll SendInput.
# Works in all applications, text editors, browsers, and games.

$cSharpSource = @"
using System;
using System.Runtime.InteropServices;
using System.Collections.Concurrent;
using System.Threading;

public class RealDesktopInput {
    [DllImport("user32.dll", SetLastError = true)]
    public static extern IntPtr OpenDesktop(string lpszDesktop, uint dwFlags, bool fInherit, uint dwDesiredAccess);

    [DllImport("user32.dll", SetLastError = true)]
    public static extern bool SetThreadDesktop(IntPtr hDesktop);

    [DllImport("user32.dll")]
    public static extern void keybd_event(byte bVk, byte bScan, uint dwFlags, UIntPtr dwExtraInfo);

    [DllImport("user32.dll")]
    public static extern byte MapVirtualKey(uint uCode, uint uMapType);

    const uint DESKTOP_ALL_ACCESS = 0x01FF;
    const uint KEYEVENTF_KEYUP = 0x0002;
    const uint KEYEVENTF_EXTENDEDKEY = 0x0001;

    private struct KeyEventItem {
        public byte vk;
        public bool isDown;
    }

    private static BlockingCollection<KeyEventItem> queue = new BlockingCollection<KeyEventItem>();
    private static Thread workerThread;
    private static bool initialized = false;

    public static void Start() {
        if (initialized) return;
        initialized = true;

        workerThread = new Thread(() => {
            IntPtr hDesk = OpenDesktop("Default", 0, false, DESKTOP_ALL_ACCESS);
            if (hDesk != IntPtr.Zero) {
                SetThreadDesktop(hDesk);
            }

            foreach (var item in queue.GetConsumingEnumerable()) {
                byte scan = MapVirtualKey(item.vk, 0);
                uint flags = item.isDown ? 0 : KEYEVENTF_KEYUP;
                if ((item.vk >= 0x21 && item.vk <= 0x2E) || (item.vk >= 0x5B && item.vk <= 0x5D)) {
                    flags |= KEYEVENTF_EXTENDEDKEY;
                }
                keybd_event(item.vk, scan, flags, UIntPtr.Zero);
            }
        });
        workerThread.IsBackground = true;
        workerThread.Start();
    }

    public static void SendKey(ushort vkCode, bool isDown) {
        if (!initialized) Start();
        queue.Add(new KeyEventItem { vk = (byte)vkCode, isDown = isDown });
    }
}
"@
if (-not ([System.Management.Automation.PSTypeName]'RealDesktopInput').Type) {
    Add-Type -TypeDefinition $cSharpSource -Language CSharp
}
[RealDesktopInput]::Start()

$port = 8899
$hostIp = "127.0.0.1"

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "   FlexKeypad Desktop Companion (Win32 SendInput) " -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "Connecting to FlexKeypad at ${hostIp}:${port}..."

# Map USB HID KeyCodes (0x07) to Windows Virtual Key (VK) codes
$hidToVk = @{
    # Letters A-Z (0x04 - 0x1D -> 0x41 - 0x5A)
    0x04 = 0x41; 0x05 = 0x42; 0x06 = 0x43; 0x07 = 0x44; 0x08 = 0x45; 0x09 = 0x46
    0x0A = 0x47; 0x0B = 0x48; 0x0C = 0x49; 0x0D = 0x4A; 0x0E = 0x4B; 0x0F = 0x4C
    0x10 = 0x4D; 0x11 = 0x4E; 0x12 = 0x4F; 0x13 = 0x50; 0x14 = 0x51; 0x15 = 0x52
    0x16 = 0x53; 0x17 = 0x54; 0x18 = 0x55; 0x19 = 0x56; 0x1A = 0x57; 0x1B = 0x58
    0x1C = 0x59; 0x1D = 0x5A

    # Numbers 1-9, 0 (0x1E - 0x27 -> 0x31 - 0x39, 0x30)
    0x1E = 0x31; 0x1F = 0x32; 0x20 = 0x33; 0x21 = 0x34; 0x22 = 0x35
    0x23 = 0x36; 0x24 = 0x37; 0x25 = 0x38; 0x26 = 0x39; 0x27 = 0x30

    # Controls
    0x28 = 0x0D # Enter
    0x29 = 0x1B # Escape
    0x2A = 0x08 # Backspace
    0x2B = 0x09 # Tab
    0x2C = 0x20 # Space
    0x4C = 0x2E # Delete

    # Navigation
    0x4F = 0x27 # Right
    0x50 = 0x25 # Left
    0x51 = 0x28 # Down
    0x52 = 0x26 # Up
    0x4A = 0x24 # Home
    0x4D = 0x23 # End
    0x4B = 0x21 # Page Up
    0x4E = 0x22 # Page Down

    # Function Keys F1-F12 (0x3A - 0x45 -> 0x70 - 0x7B)
    0x3A = 0x70; 0x3B = 0x71; 0x3C = 0x72; 0x3D = 0x73; 0x3E = 0x74; 0x3F = 0x75
    0x40 = 0x76; 0x41 = 0x77; 0x42 = 0x78; 0x43 = 0x79; 0x44 = 0x7A; 0x45 = 0x7B
}

$vkCtrl = 0x11
$vkAlt = 0x12
$vkShift = 0x10
$vkWin = 0x5B

while ($true) {
    try {
        $client = New-Object System.Net.Sockets.TcpClient
        $client.Connect([System.Net.IPAddress]::Loopback, $port)
        $stream = $client.GetStream()
        $reader = New-Object System.IO.StreamReader($stream)

        Write-Host "[CONNECTED] Connected to FlexKeypad on port $port!" -ForegroundColor Green
        Write-Host "Hardware keystrokes active! Switch to Notepad or any app to test.`n" -ForegroundColor Yellow

        while ($client.Connected -and !$reader.EndOfStream) {
            $line = $reader.ReadLine()
            if ($line) {
                try {
                    $event = $line | ConvertFrom-Json
                    $action = $event.action
                    $code = [int]$event.keyCode
                    $mods = $event.modifiers

                    $vk = $hidToVk[$code]

                    if ($action -eq "DOWN") {
                        # Press modifiers
                        if ($mods -contains "MODIFIER_LEFT_CTRL" -or $mods -contains "MODIFIER_RIGHT_CTRL") {
                            [RealDesktopInput]::SendKey($vkCtrl, $true)
                        }
                        if ($mods -contains "MODIFIER_LEFT_ALT" -or $mods -contains "MODIFIER_RIGHT_ALT") {
                            [RealDesktopInput]::SendKey($vkAlt, $true)
                        }
                        if ($mods -contains "MODIFIER_LEFT_SHIFT" -or $mods -contains "MODIFIER_RIGHT_SHIFT") {
                            [RealDesktopInput]::SendKey($vkShift, $true)
                        }
                        if ($mods -contains "MODIFIER_LEFT_GUI" -or $mods -contains "MODIFIER_RIGHT_GUI") {
                            [RealDesktopInput]::SendKey($vkWin, $true)
                        }

                        # Press main key
                        if ($vk) {
                            Write-Host "[DOWN] Key 0x$($vk.ToString('X2'))" -ForegroundColor Cyan
                            [RealDesktopInput]::SendKey($vk, $true)
                        }
                    } elseif ($action -eq "UP") {
                        # Release main key
                        if ($vk) {
                            Write-Host "[UP]   Key 0x$($vk.ToString('X2'))" -ForegroundColor Gray
                            [RealDesktopInput]::SendKey($vk, $false)
                        }

                        # Release modifiers
                        [RealDesktopInput]::SendKey($vkCtrl, $false)
                        [RealDesktopInput]::SendKey($vkAlt, $false)
                        [RealDesktopInput]::SendKey($vkShift, $false)
                        [RealDesktopInput]::SendKey($vkWin, $false)
                    } elseif ($action -eq "RELEASE_ALL") {
                        [RealDesktopInput]::SendKey($vkCtrl, $false)
                        [RealDesktopInput]::SendKey($vkAlt, $false)
                        [RealDesktopInput]::SendKey($vkShift, $false)
                        [RealDesktopInput]::SendKey($vkWin, $false)
                    }
                } catch {
                    # Ignore malformed packets
                }
            }
        }
        $client.Close()
    } catch {
        Write-Host "[WAIT] Waiting for FlexKeypad connection..." -NoNewline
        Start-Sleep -Seconds 2
        Write-Host "`r" -NoNewline
    }
}
