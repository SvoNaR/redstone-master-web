@echo off
setlocal
cd /d "%~dp0"

set PORT_PID=
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do set PORT_PID=%%a

if defined PORT_PID (
    echo.
    echo Port 8080 is already in use ^(PID %PORT_PID%^).
    echo This usually means the server is already running from a previous start.
    echo.
    choice /C YN /M "Stop it and start a new instance"
    if errorlevel 2 (
        echo.
        echo Open http://localhost:8080/ in your browser, or run stop.bat to shut down.
        echo.
        pause
        exit /b 0
    )
    echo Stopping PID %PORT_PID%...
    taskkill /PID %PORT_PID% /F >nul 2>&1
    timeout /t 2 /nobreak >nul
)

echo.
echo Starting Redstone Master Web on http://localhost:8080/
echo Press Ctrl+C to stop, or run stop.bat from another window.
echo.
set MAVEN_OPTS=-Dfile.encoding=UTF-8
mvn spring-boot:run
pause
