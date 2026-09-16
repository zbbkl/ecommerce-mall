@echo off
setlocal

REM ============================================================
REM  一键启动：先起后端，等 9999 就绪后再起前端
REM  后端和前端各占一个命令行窗口，关窗口即停止对应服务
REM ============================================================

cd /d "%~dp0"

echo 启动后端...
start "后端 9999" cmd /k "%~dp0start-backend.bat"

echo 等待后端就绪（最多 120 秒）...
set /a tries=0
:wait
timeout /t 3 >nul
netstat -ano | findstr ":9999" | findstr "LISTENING" >nul
if not errorlevel 1 goto :ready

set /a tries+=1
if %tries% lss 40 goto :wait

echo [警告] 等了 120 秒后端仍未就绪，请查看"后端 9999"窗口里的报错。
echo         仍将启动前端，但页面上的接口调用会失败。
goto :front

:ready
echo 后端已就绪。

:front
echo 启动前端...
start "前端 8080" cmd /k "%~dp0start-frontend.bat"

echo.
echo 后端: http://localhost:9999
echo 前端: http://localhost:8080
echo.
echo 默认账号: admin / 123 (管理员)    tom / 123 (普通用户)

endlocal
