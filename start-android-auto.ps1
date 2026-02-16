# Start Android Auto Testing Script

Write-Host "=== Android Auto Testing Setup ===" -ForegroundColor Cyan

# Check if adb is accessible
$adbPath = "adb"
if (-not (Get-Command $adbPath -ErrorAction SilentlyContinue)) {
    $adbPath = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
    if (-not (Test-Path $adbPath)) {
        Write-Host "ERROR: ADB not found!" -ForegroundColor Red
        Write-Host "Please install Android SDK Platform Tools" -ForegroundColor Yellow
        Write-Host "Expected location: $adbPath" -ForegroundColor Yellow
        exit
    }
}

Write-Host "Using ADB at: $adbPath" -ForegroundColor Green

# Check connected devices
Write-Host "`nChecking connected devices..." -ForegroundColor Yellow
& $adbPath devices

$deviceCount = (& $adbPath devices | Select-String -Pattern "device$").Matches.Count
if ($deviceCount -eq 0) {
    Write-Host "`nWARNING: No devices connected!" -ForegroundColor Red
    Write-Host "Please:" -ForegroundColor Yellow
    Write-Host "  1. Connect your phone via USB" -ForegroundColor Yellow
    Write-Host "  2. Enable USB debugging on phone" -ForegroundColor Yellow
    Write-Host "  3. Accept 'Allow USB debugging' prompt" -ForegroundColor Yellow
    Read-Host "`nPress Enter after connecting device to continue"
    & $adbPath devices
}

# Set up port forwarding
Write-Host "`nSetting up port forwarding..." -ForegroundColor Yellow
$forwardResult = & $adbPath forward tcp:5277 tcp:5277 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] Port forwarding successful! (tcp:5277)" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Port forwarding failed!" -ForegroundColor Red
    Write-Host "Try running: adb kill-server, then adb start-server" -ForegroundColor Yellow
    exit
}

# Start DHU
$dhuPath = "$env:LOCALAPPDATA\Android\Sdk\extras\google\auto\desktop-head-unit.exe"
if (Test-Path $dhuPath) {
    Write-Host "`nStarting Desktop Head Unit..." -ForegroundColor Yellow
    Start-Process $dhuPath
    Write-Host "[OK] DHU started!" -ForegroundColor Green
} else {
    Write-Host "`nWARNING: DHU not found at:" -ForegroundColor Yellow
    Write-Host "  $dhuPath" -ForegroundColor Yellow
    Write-Host "`nPlease install via Android Studio SDK Manager:" -ForegroundColor Yellow
    Write-Host "  1. Open Android Studio" -ForegroundColor White
    Write-Host "  2. Tools -> SDK Manager -> SDK Tools tab" -ForegroundColor White
    Write-Host "  3. Check 'Android Auto Desktop Head Unit emulator'" -ForegroundColor White
    Write-Host "  4. Click Apply/OK" -ForegroundColor White
    Write-Host "`nOr download from: https://developer.android.com/training/cars/testing#dhu" -ForegroundColor Cyan
}

Write-Host "`n=== Setup Complete ===" -ForegroundColor Cyan
Write-Host "`nNext steps:" -ForegroundColor White
Write-Host "  1. Open Domoticz app on your connected phone" -ForegroundColor White
Write-Host "  2. DHU window should show Android Auto interface" -ForegroundColor White
Write-Host "  3. Navigate to different categories to test favorites" -ForegroundColor White
Write-Host "`nTroubleshooting:" -ForegroundColor Yellow
Write-Host "  - If DHU shows 'Waiting for phone', restart Domoticz app" -ForegroundColor White
Write-Host "  - If connection fails, run this script again" -ForegroundColor White
Write-Host "  - Check phone screen for connection prompts" -ForegroundColor White
