@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script for Windows
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set MAVEN_VERSION=3.9.6
set MAVEN_URL=https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip
set MAVEN_DIR=%USERPROFILE%\.m2\wrapper\apache-maven-%MAVEN_VERSION%
set MAVEN_CMD=%MAVEN_DIR%\bin\mvn.cmd

if exist "%MAVEN_CMD%" goto run

echo Maven not found locally. Downloading Maven %MAVEN_VERSION%...
echo.

mkdir "%MAVEN_DIR%" 2>nul
set DOWNLOAD_DIR=%USERPROFILE%\.m2\wrapper
set ZIP_FILE=%DOWNLOAD_DIR%\maven.zip

powershell -Command "& { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%ZIP_FILE%' }"

if not exist "%ZIP_FILE%" (
    echo ERROR: Failed to download Maven.
    echo Please install Maven manually from https://maven.apache.org/download.cgi
    exit /b 1
)

echo Extracting Maven...
powershell -Command "& { Expand-Archive -Path '%ZIP_FILE%' -DestinationPath '%DOWNLOAD_DIR%' -Force }"
del "%ZIP_FILE%" 2>nul

if not exist "%MAVEN_CMD%" (
    echo ERROR: Maven extraction failed.
    exit /b 1
)

echo Maven %MAVEN_VERSION% installed successfully!
echo.

:run
"%MAVEN_CMD%" %*
