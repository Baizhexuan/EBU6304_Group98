@echo off
echo ==============================================
echo   BUPT TA Recruitment System (L1 Setup & Test)
echo ==============================================

cd /d "%~dp0"

if not exist bin\com\bupt\ta\recruitment\util\DataSeeder.class (
    echo [ERROR] bin directory or DataSeeder.class not found. Please run compile.bat first!
    pause
    exit /b
)

echo [1/2] Running Data Seeder...
java -cp bin com.bupt.ta.recruitment.util.DataSeeder

echo.
echo [2/2] Running L1 Environment Base Test...
java -cp bin com.bupt.ta.recruitment.test.L1Test

echo.
echo SYSTEM READY. Check out 'data' folder for CSV results.
pause
