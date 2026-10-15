# BharatBazaar PowerShell Runner
Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host "   BharatBazaar - Indian Shopping Cart (B.Tech Project)  " -ForegroundColor Yellow
Write-Host "   Currency: INR (₹) | 5% GST | Free Delivery above ₹499 " -ForegroundColor Green
Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host ""

if (!(Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

Write-Host "[1/2] Compiling Java classes with javac..." -ForegroundColor Green
$files = (Get-ChildItem -Recurse "src\main\java\com\shopmart\*.java").FullName
javac -d bin -encoding UTF-8 $files

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Compilation failed!" -ForegroundColor Red
    exit 1
}

Write-Host "[2/2] Launching BharatBazaar at http://localhost:8080..." -ForegroundColor Green
Start-Process "http://localhost:8080"
Write-Host "Server is running! Press Ctrl+C in this window to stop." -ForegroundColor Yellow
Write-Host ""

java -cp bin com.shopmart.ShoppingCartServer
