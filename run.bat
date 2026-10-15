@echo off
title BharatBazaar - Indian E-Commerce Shopping Cart
cls
echo =========================================================
echo    BharatBazaar - Indian Shopping Cart (B.Tech Project)
echo    Currency: INR (Rs.) | 5%% GST | Fast Delivery
echo =========================================================
echo.

REM Check if Java is installed
where javac >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] 'javac' not found. Please make sure Java JDK 17 is installed and added to PATH.
    pause
    exit /b 1
)

echo [1/3] Preparing build directory...
if not exist "bin" mkdir bin

echo [2/3] Compiling Java classes with javac...
javac -d bin -encoding UTF-8 src\main\java\com\shopmart\model\*.java src\main\java\com\shopmart\service\*.java src\main\java\com\shopmart\*.java

if %ERRORLEVEL% neq 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b 1
)

echo [3/3] Starting BharatBazaar Server on port 8080...
echo.
echo Opening http://localhost:8080 in your browser...
start http://localhost:8080
echo.
echo Server running! Press Ctrl+C in this window to stop.
echo.

java -cp bin com.shopmart.ShoppingCartServer
pause
