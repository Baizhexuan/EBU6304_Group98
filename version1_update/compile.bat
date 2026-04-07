@echo off
:: ─────────────────────────────────────────────────────────
:: compile.bat  –  Compiles all Java source files to bin\
:: Run from the project root directory.
:: ─────────────────────────────────────────────────────────
if not exist bin mkdir bin

echo Compiling source files...
javac -d bin -sourcepath src src\Main.java src\User.java src\TAProfile.java src\Job.java src\Application.java src\FileStorage.java src\LoginFrame.java src\TADashboard.java src\MODashboard.java src\AdminDashboard.java

if %ERRORLEVEL% == 0 (
    echo.
    echo Compilation successful! Run "run.bat" to start the application.
) else (
    echo.
    echo Compilation FAILED. Please check the error messages above.
)
pause
