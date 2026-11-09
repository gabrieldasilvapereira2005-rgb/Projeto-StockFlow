@echo off
setlocal
cd /d "%~dp0"
where java >nul 2>&1
if errorlevel 1 (
  echo JDK 17 ou superior nao encontrado.
  echo https://adoptium.net/temurin/releases/?version=17
  pause
  exit /b 1
)
where mvn >nul 2>&1
if errorlevel 1 (
  echo Maven nao encontrado.
  echo https://maven.apache.org/download.cgi
  pause
  exit /b 1
)
mvn -q compile exec:java
if errorlevel 1 pause
