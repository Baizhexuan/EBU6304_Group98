@echo off
:: ─────────────────────────────────────────────────────────
:: run.bat  –  Runs the TA Recruitment System application.
:: Must be run from the project root directory so that the
:: 'data\' folder is found at the correct relative path.
:: ─────────────────────────────────────────────────────────
if not exist bin (
    echo bin\ directory not found. Please run compile.bat first.
    pause
    exit /b 1
)

echo Starting TA Recruitment System...
java -cp bin Main
