@echo off
setlocal
cd /d "%~dp0"

echo ===================================================
echo     Compiling FlexKeypad Windows Native Companion
echo ===================================================
echo.

set "CSC=C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe"
if not exist "%CSC%" set "CSC=C:\Windows\Microsoft.NET\Framework\v4.0.30319\csc.exe"

if not exist "%CSC%" (
    echo [ERROR] C# Compiler csc.exe not found on this system.
    pause
    exit /b 1
)

echo Using compiler: %CSC%
"%CSC%" /target:winexe /optimize+ /out:FlexKeypadCompanion.exe /r:System.dll,System.Windows.Forms.dll,System.Drawing.dll FlexKeypadCompanion.cs

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] FlexKeypadCompanion.exe compiled successfully!
) else (
    echo.
    echo [ERROR] Compilation failed.
    pause
    exit /b 1
)
