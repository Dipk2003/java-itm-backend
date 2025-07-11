# iTech Backend - Chatbot API Testing Script
# This script demonstrates how to properly test all chatbot endpoints including authenticated ones

$baseUrl = "http://localhost:8080"
$testResults = @()

# Colors for output
$Green = "Green"
$Red = "Red"
$Yellow = "Yellow"
$Cyan = "Cyan"
$Magenta = "Magenta"

Write-Host "🤖 iTech Backend - Chatbot API Testing" -ForegroundColor $Green
Write-Host "=======================================" -ForegroundColor $Green

# Function to test endpoints
function Test-ApiEndpoint {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Url,
        [hashtable]$Headers = @{"Content-Type"="application/json"},
        [string]$Body = $null,
        [bool]$ExpectAuth = $false
    )
    
    Write-Host "`n🔍 Testing: $Name" -ForegroundColor $Yellow
    Write-Host "   URL: $Url" -ForegroundColor Gray
    
    try {
        if ($Method -eq "GET") {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Headers $Headers
        } elseif ($Method -eq "DELETE") {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Headers $Headers
        } else {
            if ($Body) {
                $response = Invoke-RestMethod -Uri $Url -Method $Method -Headers $Headers -Body $Body
            } else {
                $response = Invoke-RestMethod -Uri $Url -Method $Method -Headers $Headers
            }
        }
        
        Write-Host "   ✅ SUCCESS" -ForegroundColor $Green
        $script:testResults += @{Name=$Name; Status="SUCCESS"; Response=$response}
        return $response
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($ExpectAuth -and $statusCode -eq 403) {
            Write-Host "   ⚠️  EXPECTED: 403 Forbidden (Requires Authentication)" -ForegroundColor $Yellow
            $script:testResults += @{Name=$Name; Status="EXPECTED_AUTH"; Error="Requires Authentication"}
        } else {
            Write-Host "   ❌ FAILED: $($_.Exception.Message)" -ForegroundColor $Red
            $script:testResults += @{Name=$Name; Status="FAILED"; Error=$_.Exception.Message}
        }
        return $null
    }
}

# 1. Test Public Chatbot Endpoints
Write-Host "`n📱 1. TESTING PUBLIC CHATBOT ENDPOINTS" -ForegroundColor $Magenta

# Health check
Test-ApiEndpoint -Name "Chatbot Health Check" -Method "GET" -Url "$baseUrl/api/chatbot/health"

# Start a chat session
Write-Host "`n🚀 Starting new chat session..." -ForegroundColor $Cyan
$sessionResponse = Test-ApiEndpoint -Name "Start Chat Session" -Method "POST" -Url "$baseUrl/api/chatbot/start-session"

# Get session ID for further testing
$sessionId = "test-session-$(Get-Date -Format 'yyyyMMddHHmmss')"
if ($sessionResponse -and $sessionResponse.sessionId) {
    $sessionId = $sessionResponse.sessionId
    Write-Host "   📝 Using Session ID: $sessionId" -ForegroundColor $Cyan
} else {
    Write-Host "   📝 Using Generated Session ID: $sessionId" -ForegroundColor $Cyan
}

# Send a chat message
$chatBody = @{
    message = "Hello, I need help with electronics products"
    sessionId = $sessionId
    userIp = "127.0.0.1"
} | ConvertTo-Json

Test-ApiEndpoint -Name "Send Chat Message" -Method "POST" -Url "$baseUrl/api/chatbot/message" -Body $chatBody

# Send another message for testing
$chatBody2 = @{
    message = "What are your best smartphones?"
    sessionId = $sessionId
    userIp = "127.0.0.1"
} | ConvertTo-Json

Test-ApiEndpoint -Name "Send Second Chat Message" -Method "POST" -Url "$baseUrl/api/chatbot/message" -Body $chatBody2

# Get chat history
Test-ApiEndpoint -Name "Get Chat History" -Method "GET" -Url "$baseUrl/api/chatbot/history/$sessionId"

# 2. Test Admin Endpoints (These will require authentication)
Write-Host "`n👑 2. TESTING ADMIN CHATBOT ENDPOINTS (Requires Authentication)" -ForegroundColor $Magenta

Test-ApiEndpoint -Name "Get Chatbot Analytics" -Method "GET" -Url "$baseUrl/admin/chatbot/analytics" -ExpectAuth $true

Test-ApiEndpoint -Name "Get All Conversations" -Method "GET" -Url "$baseUrl/admin/chatbot/conversations?page=0&size=20&sortBy=createdAt&sortDir=desc" -ExpectAuth $true

Test-ApiEndpoint -Name "Get Specific Conversation" -Method "GET" -Url "$baseUrl/admin/chatbot/conversation/$sessionId" -ExpectAuth $true

Test-ApiEndpoint -Name "Get Recent Queries" -Method "GET" -Url "$baseUrl/admin/chatbot/recent-queries?limit=10" -ExpectAuth $true

# 3. Demonstrate how to get admin authentication
Write-Host "`n🔐 3. HOW TO ACCESS ADMIN ENDPOINTS" -ForegroundColor $Magenta
Write-Host "To access admin endpoints, you need to:" -ForegroundColor $Yellow
Write-Host "   1. Login as an admin user" -ForegroundColor Gray
Write-Host "   2. Get a JWT token with ADMIN role" -ForegroundColor Gray
Write-Host "   3. Include the token in Authorization header" -ForegroundColor Gray

Write-Host "`n📝 Example Admin Login Process:" -ForegroundColor $Cyan

# Show how to login as admin (this is just demonstration)
Write-Host "   Step 1: Login with admin credentials" -ForegroundColor Gray
$adminLoginBody = @{
    emailOrPhone = "admin@test.com"
    password = "your_admin_password"
    adminCode = "ADMIN2025"
} | ConvertTo-Json

Write-Host "   POST $baseUrl/auth/login" -ForegroundColor Gray
Write-Host "   Body: $adminLoginBody" -ForegroundColor Gray

Write-Host "`n   Step 2: Verify OTP to get JWT token" -ForegroundColor Gray
$otpBody = @{
    emailOrPhone = "admin@test.com"
    otp = "123456"
} | ConvertTo-Json

Write-Host "   POST $baseUrl/auth/verify" -ForegroundColor Gray
Write-Host "   Body: $otpBody" -ForegroundColor Gray

Write-Host "`n   Step 3: Use token in requests" -ForegroundColor Gray
Write-Host "   Headers: @{" -ForegroundColor Gray
Write-Host "       'Content-Type' = 'application/json'" -ForegroundColor Gray
Write-Host "       'Authorization' = 'Bearer YOUR_JWT_TOKEN'" -ForegroundColor Gray
Write-Host "   }" -ForegroundColor Gray

# 4. Test with mock admin token (for demonstration)
Write-Host "`n🧪 4. TESTING WITH MOCK AUTHENTICATION" -ForegroundColor $Magenta

# Try to test admin login flow
Write-Host "`nTrying admin login flow..." -ForegroundColor $Cyan

try {
    # First, try to get an admin token by logging in
    $adminLogin = @{
        emailOrPhone = "admin@test.com"
        password = "password123"
        adminCode = "ADMIN2025"
    } | ConvertTo-Json

    $loginResult = Test-ApiEndpoint -Name "Admin Login (OTP Send)" -Method "POST" -Url "$baseUrl/auth/login" -Body $adminLogin
    
    if ($loginResult) {
        Write-Host "   ✅ Admin login initiated. Check console for OTP." -ForegroundColor $Green
        Write-Host "   📝 To complete authentication:" -ForegroundColor $Yellow
        Write-Host "      1. Check your backend console for the OTP" -ForegroundColor Gray
        Write-Host "      2. Use the OTP to verify and get JWT token" -ForegroundColor Gray
        Write-Host "      3. Use the JWT token for admin endpoints" -ForegroundColor Gray
    }
} catch {
    Write-Host "   ⚠️  Admin login failed. Make sure admin user exists." -ForegroundColor $Yellow
}

# 5. Summary and Report
Write-Host "`n📊 5. TEST SUMMARY" -ForegroundColor $Magenta
Write-Host "==================" -ForegroundColor $Magenta

$successCount = ($testResults | Where-Object {$_.Status -eq "SUCCESS"}).Count
$failCount = ($testResults | Where-Object {$_.Status -eq "FAILED"}).Count
$authCount = ($testResults | Where-Object {$_.Status -eq "EXPECTED_AUTH"}).Count
$totalCount = $testResults.Count

Write-Host "`nTotal Tests: $totalCount" -ForegroundColor White
Write-Host "Successful: $successCount" -ForegroundColor $Green
Write-Host "Failed: $failCount" -ForegroundColor $Red
Write-Host "Requires Auth: $authCount" -ForegroundColor $Yellow

if ($successCount -gt 0) {
    Write-Host "`n✅ Successful Tests:" -ForegroundColor $Green
    $testResults | Where-Object {$_.Status -eq "SUCCESS"} | ForEach-Object {
        Write-Host "   - $($_.Name)" -ForegroundColor $Green
    }
}

if ($authCount -gt 0) {
    Write-Host "`n🔐 Authentication Required:" -ForegroundColor $Yellow
    $testResults | Where-Object {$_.Status -eq "EXPECTED_AUTH"} | ForEach-Object {
        Write-Host "   - $($_.Name)" -ForegroundColor $Yellow
    }
}

if ($failCount -gt 0) {
    Write-Host "`n❌ Failed Tests:" -ForegroundColor $Red
    $testResults | Where-Object {$_.Status -eq "FAILED"} | ForEach-Object {
        Write-Host "   - $($_.Name): $($_.Error)" -ForegroundColor $Red
    }
}

Write-Host "`n🎯 RESOLUTION SUMMARY:" -ForegroundColor $Magenta
Write-Host "=====================" -ForegroundColor $Magenta
Write-Host "✅ 403 errors are expected for admin endpoints without authentication" -ForegroundColor $Green
Write-Host "✅ Public chatbot endpoints should work without authentication" -ForegroundColor $Green
Write-Host "✅ Use PowerShell/curl instead of browser for API testing" -ForegroundColor $Green
Write-Host "✅ Replace :sessionId with actual session IDs in URLs" -ForegroundColor $Green

Write-Host "`n📱 Next Steps:" -ForegroundColor $Cyan
Write-Host "   1. Use this script for proper API testing" -ForegroundColor Gray
Write-Host "   2. Get admin JWT token for admin endpoint testing" -ForegroundColor Gray
Write-Host "   3. Use Postman collection for interactive testing" -ForegroundColor Gray
Write-Host "   4. Replace placeholder values with real data" -ForegroundColor Gray

Write-Host "`n🎉 Testing Complete!" -ForegroundColor $Green
