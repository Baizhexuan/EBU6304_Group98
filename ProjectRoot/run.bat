@echo off
REM 关闭命令回显，保持输出整洁。
echo ==============================================
echo   BUPT TA Recruitment System (L1 Setup & Test)
echo ==============================================

REM 切换到脚本所在目录，确保后续相对路径有效。
cd /d "%~dp0"

REM 运行前先检查 DataSeeder 是否已经编译完成。
if not exist bin\com\bupt\ta\recruitment\util\DataSeeder.class (
    echo [ERROR] bin directory or DataSeeder.class not found. Please run compile.bat first!
    pause
    exit /b
)

REM 先生成演示数据，保证 CSV 环境完整。
echo [1/2] Running Data Seeder...
java -cp bin com.bupt.ta.recruitment.util.DataSeeder

echo.
REM 再执行 L1 基础环境测试。
echo [2/2] Running L1 Environment Base Test...
java -cp bin com.bupt.ta.recruitment.test.L1Test

echo.
REM 输出最终提示，提醒用户查看生成的数据文件。
echo SYSTEM READY. Check out 'data' folder for CSV results.
REM 暂停窗口，方便用户确认执行结果。
pause
