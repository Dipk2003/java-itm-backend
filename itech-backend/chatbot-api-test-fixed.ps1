# iTech Backend - Chatbot API Testing Script (Fixed)
$baseUrl = "http://localhost:8080"

Write-Host "🤖 iTech Backend - Chatbot API Testing" -ForegroundColor Green
Write-Host "=======================================" -ForegroundColor Green

# Test 1: Health Check
Write-Host "`n📡 Testing Chatbot Health..." -ForegroundColor Cyan
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/api/chatbot/health" -Method GET
    Write-Host "✅ Health Check: Working" -ForegroundColor Green
} catch {
    Write-Host "❌ Health Check: Failed - $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Start Chat Session  
Write-Host "`n🚀 Testing Start Chat Session..." -ForegroundColor Cyan
try {
    $session = Invoke-RestMethod -Uri "$baseUrl/api/chatbot/start-session" -Method POST -ContentType "application/json"
    Write-Host "✅ Start Session: Success" -ForegroundColor Green
    $sessionId = $session.sessionId
    Write-Host "   Session ID: $sessionId" -ForegroundColor White
} catch {
    Write-Host "❌ Start Session: Failed - $($_.Exception.Message)" -ForegroundColor Red
    $sessionId = "test-session-12345"
    Write-Host "   Using fallback Session ID: $sessionId" -ForegroundColor Yellow
}

# Test 3: Send Chat Message
Write-Host "`n💬 Testing Send Chat Message..." -ForegroundColor Cyan
try {
    $chatBody = @{
        message = "Hello, I need help with electronics"
        sessionId = $sessionId
        userIp = "127.0.0.1"
    } | ConvertTo-Json
    
    $chatResponse = Invoke-RestMethod -Uri "$baseUrl/api/chatbot/message" -Method POST -Body $chatBody -ContentType "application/json"
    Write-Host "✅ Send Message: Success" -ForegroundColor Green
    Write-Host "   Response: $($chatResponse.response)" -ForegroundColor White
} catch {
    Write-Host "❌ Send Message: Failed - $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Get Chat History
Write-Host "`n📜 Testing Get Chat History..." -ForegroundColor Cyan
try {
    $history = Invoke-RestMethod -Uri "$baseUrl/api/chatbot/history/$sessionId" -Method GET
    Write-Host "✅ Get History: Success" -ForegroundColor Green
    Write-Host "   Messages found: $($history.Count)" -ForegroundColor White
} catch {
    Write-Host "❌ Get History: Failed - $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Admin Endpoints (These should return 403)
Write-Host "`n👑 Testing Admin Endpoints (Should return 403)..." -ForegroundColor Cyan

Write-Host "`n🔍 Testing Admin Analytics..." -ForegroundColor Yellow
try {
    $analytics = Invoke-RestMethod -Uri "$baseUrl/admin/chatbot/analytics" -Method GET
    Write-Host "✅ Admin Analytics: Unexpected Success" -ForegroundColor Green
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 403) {
        Write-Host "✅ Admin Analytics: 403 Forbidden (Expected - Requires Auth)" -ForegroundColor Yellow
    } else {
        Write-Host "❌ Admin Analytics: Unexpected Error - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n🔍 Testing Admin Conversations..." -ForegroundColor Yellow
try {
    $conversations = Invoke-RestMethod -Uri "$baseUrl/admin/chatbot/conversations" -Method GET
    Write-Host "✅ Admin Conversations: Unexpected Success" -ForegroundColor Green
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 403) {
        Write-Host "✅ Admin Conversations: 403 Forbidden (Expected - Requires Auth)" -ForegroundColor Yellow
    } else {
        Write-Host "❌ Admin Conversations: Unexpected Error - $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Summary
Write-Host "`n📊 SUMMARY" -ForegroundColor Magenta
Write-Host "==========" -ForegroundColor Magenta
Write-Host "✅ Public chatbot endpoints should work" -ForegroundColor Green
Write-Host "⚠️  Admin endpoints require authentication (403 is expected)" -ForegroundColor Yellow
Write-Host "🔐 To test admin endpoints, you need:" -ForegroundColor Cyan
Write-Host "   1. Login as admin user" -ForegroundColor Gray
Write-Host "   2. Get JWT token" -ForegroundColor Gray
Write-Host "   3. Include token in Authorization header" -ForegroundColor Gray

Write-Host "`n🎯 ISSUE RESOLUTION:" -ForegroundColor Magenta
Write-Host "===================" -ForegroundColor Magenta
Write-Host "✅ 403 errors are NORMAL for admin endpoints without auth" -ForegroundColor Green
Write-Host "✅ Replace :sessionId with actual session IDs" -ForegroundColor Green
Write-Host "✅ Use API tools (Postman/curl) instead of browser" -ForegroundColor Green
Write-Host "✅ Browser errors are because you need a frontend app" -ForegroundColor Green

Write-Host "`n🎉 Testing Complete!" -ForegroundColor Green
