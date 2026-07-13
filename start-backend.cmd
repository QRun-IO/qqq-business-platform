@echo off
setlocal
cd /d "%~dp0"

set "SERVER_PORT=18082"
set "JAR=platform-app\target\platform-app-0.1.0-SNAPSHOT.jar"

curl.exe --fail --silent --max-time 2 "http://localhost:%SERVER_PORT%/api/public/v1/health" >nul 2>&1
if not errorlevel 1 (
  echo QQQ backend is already running at http://localhost:%SERVER_PORT%
  exit /b 0
)

if /I not "%~1"=="--no-build" (
  echo Building backend and required modules...
  call mvn.cmd -pl platform-app -am -DskipTests package
  if errorlevel 1 exit /b 1
)

if not exist "%JAR%" (
  echo Backend jar not found: %JAR%
  echo Run this script without --no-build first.
  exit /b 1
)

echo Starting QQQ backend at http://localhost:%SERVER_PORT%
echo Health check: http://localhost:%SERVER_PORT%/api/public/v1/health
java -jar "%JAR%"
