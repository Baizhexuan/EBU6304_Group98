@echo off
REM 关闭命令回显，避免脚本输出过于杂乱。
echo ==============================================
echo   BUPT TA Recruitment System (L1 PairB Build)
echo ==============================================

REM 切换到脚本所在目录，保证相对路径始终正确。
cd /d "%~dp0"

REM 第一步先清理旧的编译产物目录。
echo [1/3] Cleaning old bin directory...
REM 如果 bin 目录已存在，则强制删除整个目录树。
if exist bin rmdir /s /q bin
REM 重新创建新的 bin 目录用于存放 class 文件。
mkdir bin

REM 第二步扫描全部 Java 源文件并写入 sources.txt。
echo [2/3] Finding all Java source files...
dir /s /b src\*.java > sources.txt

REM 第三步按 UTF-8 编码编译所有源码到 bin 目录。
echo [3/3] Compiling Java classes to bin/...
javac -encoding UTF-8 -d bin @sources.txt

REM 根据 ERRORLEVEL 判断编译是否成功。
if %ERRORLEVEL% EQU 0 (
    echo.
    echo BUILD SUCCESS!
) else (
    echo.
    echo BUILD FAILED! Check compile errors.
)
echo.
REM 暂停窗口，方便用户查看编译结果。
pause
