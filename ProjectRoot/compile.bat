@echo off
chcp 65001 > nul
echo ==============================================
echo   BUPT TA Recruitment System (L2 Build)
echo ==============================================

cd /d "%~dp0"

if exist bin rmdir /s /q bin
mkdir bin

dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d bin @sources.txt

if %ERRORLEVEL% EQU 0 (
    echo BUILD SUCCESS!
) else (
    echo BUILD FAILED! Check compile errors.
)
pause
