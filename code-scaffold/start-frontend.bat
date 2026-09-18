@echo off
setlocal

REM ============================================================
REM  启动前端 (Vue CLI dev server, 端口 8080)
REM ------------------------------------------------------------
REM  注意：修改 vue.config.js 后必须重启本脚本才生效。
REM        若 8080 被占用，Vue CLI 会自动改用 8081/8082，
REM        此时请关掉占用 8080 的旧 node 进程再重启。
REM ============================================================

cd /d "%~dp0vue"

if exist "node_modules" goto :deps_ok
echo [1/2] 首次运行，安装依赖（可能要几分钟）...
call npm install
if errorlevel 1 (
    echo [错误] npm install 失败。
    exit /b 1
)

:deps_ok
echo [2/2] 启动前端: http://localhost:8080
echo       （按 Ctrl+C 停止）
echo.
call npm run serve

endlocal
