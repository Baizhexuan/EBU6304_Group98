@echo off
chcp 65001 > nul
echo ==============================================
echo   BUPT TA Recruitment System (L2 UI Launch)
echo ==============================================

cd /d "%~dp0"

if not exist bin\com\bupt\ta\recruitment\ui\AppLauncher.class (
    echo [ERROR] AppLauncher.class not found. Please run compile.bat first!
    pause
    exit /b
)

java -cp bin com.bupt.ta.recruitment.ui.AppLauncher
pause
