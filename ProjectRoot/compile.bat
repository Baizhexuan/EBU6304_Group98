@echo off
echo ==============================================
echo   BUPT TA Recruitment System (L1 PairB Build)
echo ==============================================

cd /d "%~dp0"

echo [1/3] Cleaning old bin directory...
if exist bin rmdir /s /q bin
mkdir bin

echo [2/3] Finding all Java source files...
dir /s /b src\*.java > sources.txt

echo [3/3] Compiling Java classes to bin/...
javac -encoding UTF-8 -d bin @sources.txt

if %ERRORLEVEL% EQU 0 (
    echo.
    echo BUILD SUCCESS!
) else (
    echo.
    echo BUILD FAILED! Check compile errors.
)
echo.
pause
