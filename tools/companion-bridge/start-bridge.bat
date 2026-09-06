@echo off
title FlexKeypad USB Companion Bridge
cd /d "%~dp0"

echo ==================================================
echo       FlexKeypad USB Companion Bridge
echo ==================================================
echo Setting up ADB port forwarding (tcp:8899)...
adb forward tcp:8899 tcp:8899

echo.
echo Starting Keyboard Input Receiver...
powershell -ExecutionPolicy Bypass -File "%~dp0bridge.ps1"

pause
