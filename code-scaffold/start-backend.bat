@echo off
setlocal

REM ============================================================
REM  启动后端 (Spring Boot, 端口 9999)
REM ------------------------------------------------------------
REM  本机 PATH 里的默认 java 是 1.8，但本项目要求 Java 17
REM  (Spring Boot 3.5.7 / jakarta.*)，所以这里固定用 JDK 17，
REM  不修改系统 JAVA_HOME，不影响其他项目。
REM ============================================================

set "JAVA17=C:\Program Files\Java\jdk-17"

if exist "%JAVA17%\bin\java.exe" goto :jdk_ok
echo [错误] 未找到 JDK 17: %JAVA17%
echo         请编辑本脚本，把 JAVA17 改成你机器上的 JDK 17 路径。
exit /b 1

:jdk_ok
set "JAVA_HOME=%JAVA17%"
set "PATH=%JAVA_HOME%\bin;%PATH%"

cd /d "%~dp0springboot"

REM ---- 端口占用检查（Windows 下后端运行时会锁住 target 里的 jar，导致 clean 失败）----
netstat -ano | findstr ":9999" | findstr "LISTENING" >nul
if not errorlevel 1 (
    echo [错误] 9999 端口已被占用，后端可能已在运行。
    echo         请先关闭正在运行的后端进程再试。
    exit /b 1
)

echo [1/2] 编译中...
call mvn -q -DskipTests package
if errorlevel 1 (
    echo [错误] 编译失败，请查看上面的 Maven 输出。
    exit /b 1
)

echo [2/2] 启动后端: http://localhost:9999
echo       （按 Ctrl+C 停止）
echo.
java -jar target\springboot-0.0.1-SNAPSHOT.jar

endlocal
