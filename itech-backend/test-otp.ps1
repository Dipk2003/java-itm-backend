# OTP Test Script for Indian Trade Mart
# This script tests the OTP functionality and checks console output

Write-Host "🧪 OTP Test Script for Indian Trade Mart" -ForegroundColor Green
Write-Host "=======================================" -ForegroundColor Green

$apiBase = "http://localhost:8080"
$testPhone = "9876543210"

# Test 1: Check server connection
Write-Host "`n1. Testing server connection..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$apiBase/test/hello" -Method Get
    Write-Host "✅ Server connected successfully!" -ForegroundColor Green
    Write-Host "Response: $response" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Server connection failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Make sure your Spring Boot server is running on port 8080" -ForegroundColor Yellow
    exit 1
}

# Test 2: Generate OTP
Write-Host "`n2. Testing OTP generation..." -ForegroundColor Yellow
Write-Host "Phone Number: $testPhone" -ForegroundColor Cyan
Write-Host "📱 Check your terminal/console for the OTP display!" -ForegroundColor Magenta

try {
    $body = @{
        phone = $testPhone
    }
    
    $response = Invoke-RestMethod -Uri "$apiBase/test/send-otp" -Method Post -Body $body
    Write-Host "✅ OTP generation successful!" -ForegroundColor Green
    Write-Host "Response: $response" -ForegroundColor Cyan
    
    Write-Host "`n🔍 What to look for in your terminal:" -ForegroundColor Yellow
    Write-Host "- Lines with 📱📱📱 SIMULATED SMS SENT TO" -ForegroundColor Cyan
    Write-Host "- Lines with 🔥🔥🔥 YOUR OTP IS:" -ForegroundColor Cyan
    Write-Host "- Lines with 🚨 OTP ALERT:" -ForegroundColor Cyan
    
} catch {
    Write-Host "❌ OTP generation failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Test with different phone number
Write-Host "`n3. Testing with different phone number..." -ForegroundColor Yellow
$testPhone2 = "9123456789"
Write-Host "Phone Number: $testPhone2" -ForegroundColor Cyan

try {
    $body = @{
        phone = $testPhone2
    }
    
    $response = Invoke-RestMethod -Uri "$apiBase/test/send-otp" -Method Post -Body $body
    Write-Host "✅ Second OTP generation successful!" -ForegroundColor Green
    Write-Host "Response: $response" -ForegroundColor Cyan
    
} catch {
    Write-Host "❌ Second OTP generation failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n📋 Summary:" -ForegroundColor Green
Write-Host "- If you see OTP in your terminal, the functionality is working" -ForegroundColor Cyan
Write-Host "- If you don't see OTP, check the Spring Boot application logs" -ForegroundColor Cyan
Write-Host "- Make sure sms.simulation.enabled=true in application.properties" -ForegroundColor Cyan
Write-Host "- Look for colorful console output with emojis" -ForegroundColor Cyan

Write-Host "`n🔧 Troubleshooting:" -ForegroundColor Yellow
Write-Host "If OTP is not visible in console:" -ForegroundColor White
Write-Host "1. Check if your IDE/terminal supports emoji display" -ForegroundColor Cyan
Write-Host "2. Look for the text 'OTP ALERT' in your logs" -ForegroundColor Cyan
Write-Host "3. Check application logs for 'Simulated SMS sent'" -ForegroundColor Cyan
Write-Host "4. Restart your Spring Boot application" -ForegroundColor Cyan

Write-Host "`n✅ Test completed!" -ForegroundColor Green
