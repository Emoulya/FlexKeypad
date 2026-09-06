@echo off
setlocal
cd /d "%~dp0"

echo ===================================================
echo      FlexKeypad Companion 1-Click Setup
echo ===================================================
echo.

call build-companion.bat
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Setup cannot continue because build failed.
    pause
    exit /b 1
)

echo.
echo Creating Desktop shortcut...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ws = New-Object -ComObject WScript.Shell; $sc = $ws.CreateShortcut([IO.Path]::Combine([Environment]::GetFolderPath('Desktop'), 'FlexKeypad Companion.lnk')); $sc.TargetPath = [IO.Path]::Combine('%~dp0', 'FlexKeypadCompanion.exe'); $sc.WorkingDirectory = '%~dp0'; $sc.Description = 'FlexKeypad USB Companion'; $sc.Save()"

echo Creating Startup shortcut (Auto-start on Windows boot)...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ws = New-Object -ComObject WScript.Shell; $sc = $ws.CreateShortcut([IO.Path]::Combine([Environment]::GetFolderPath('Startup'), 'FlexKeypad Companion.lnk')); $sc.TargetPath = [IO.Path]::Combine('%~dp0', 'FlexKeypadCompanion.exe'); $sc.WorkingDirectory = '%~dp0'; $sc.Description = 'FlexKeypad USB Companion'; $sc.Save()"

echo.
echo Starting FlexKeypad Companion in background...
start "" "%~dp0FlexKeypadCompanion.exe"

echo.
echo ===================================================
echo [SUCCESS] Setup complete!
echo FlexKeypad Companion is now running in your System Tray
echo (look for the icon in the bottom-right corner near clock).
echo ===================================================
echo.
pause
