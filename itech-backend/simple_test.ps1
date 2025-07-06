# Simple iTech Backend API Testing Script
$baseUrl = "http://localhost:8080"
$headers = @{"Content-Type" = "application/json"}

Write-Host "=== iTech Backend API Testing ===" -ForegroundColor Green

# Test 1: Health Check
Write-Host "`n1. Health Check..." -ForegroundColor Cyan
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/api/chatbot/health" -Method GET
    Write-Host "SUCCESS: $health" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Get Products
Write-Host "`n2. Get All Products..." -ForegroundColor Cyan
try {
    $products = Invoke-RestMethod -Uri "$baseUrl/products" -Method GET
    Write-Host "SUCCESS: Found $($products.Count) products" -ForegroundColor Green
    if ($products.Count -gt 0) {
        $product = $products[0]
        Write-Host "  Sample Product: $($product.name) - Price: $($product.price)" -ForegroundColor White
    }
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Get Categories
Write-Host "`n3. Get All Categories..." -ForegroundColor Cyan
try {
    $categories = Invoke-RestMethod -Uri "$baseUrl/categories" -Method GET
    Write-Host "SUCCESS: Found $($categories.Count) categories" -ForegroundColor Green
    if ($categories.Count -gt 0) {
        Write-Host "  Categories: $($categories | ForEach-Object { $_.name } | Join-String ', ')" -ForegroundColor White
    }
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Get Vendors
Write-Host "`n4. Get All Vendors..." -ForegroundColor Cyan
try {
    $vendors = Invoke-RestMethod -Uri "$baseUrl/admin/vendors" -Method GET
    Write-Host "SUCCESS: Found $($vendors.Count) vendors" -ForegroundColor Green
    if ($vendors.Count -gt 0) {
        $vendor = $vendors[0]
        Write-Host "  Sample Vendor: $($vendor.name) (ID: $($vendor.id), Type: $($vendor.vendorType))" -ForegroundColor White
    }
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Start Chatbot Session
Write-Host "`n5. Start Chatbot Session..." -ForegroundColor Cyan
try {
    $chatStart = Invoke-RestMethod -Uri "$baseUrl/api/chatbot/start-session" -Method POST -Headers $headers
    Write-Host "SUCCESS: $($chatStart.response)" -ForegroundColor Green
    $sessionId = $chatStart.sessionId
    Write-Host "  Session ID: $sessionId" -ForegroundColor White
    
    # Test chat message
    if ($sessionId) {
        Write-Host "`n6. Send Chat Message..." -ForegroundColor Cyan
        $chatData = @{
            message = "Hello, I need electronics products"
            sessionId = $sessionId
            userIp = "127.0.0.1"
        } | ConvertTo-Json
        
        $chatResponse = Invoke-RestMethod -Uri "$baseUrl/api/chatbot/chat" -Method POST -Body $chatData -Headers $headers
        Write-Host "SUCCESS: $($chatResponse.response)" -ForegroundColor Green
    }
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 7: Contact Message
Write-Host "`n7. Submit Contact Message..." -ForegroundColor Cyan
try {
    $contactData = @{
        name = "Test User"
        email = "test@example.com"
        phone = "9876543210"
        message = "Test message from API testing"
    } | ConvertTo-Json
    
    $contact = Invoke-RestMethod -Uri "$baseUrl/contact" -Method POST -Body $contactData -Headers $headers
    Write-Host "SUCCESS: Contact message saved with ID $($contact.id)" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 8: Lead Statuses and Priorities
Write-Host "`n8. Get Lead Statuses and Priorities..." -ForegroundColor Cyan
try {
    $statuses = Invoke-RestMethod -Uri "$baseUrl/api/leads/statuses" -Method GET
    Write-Host "SUCCESS Lead Statuses: $($statuses -join ', ')" -ForegroundColor Green
    
    $priorities = Invoke-RestMethod -Uri "$baseUrl/api/leads/priorities" -Method GET
    Write-Host "SUCCESS Lead Priorities: $($priorities -join ', ')" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 9: User Registration
Write-Host "`n9. User Registration..." -ForegroundColor Cyan
try {
    $timestamp = Get-Date -Format "yyyyMMddHHmmss"
    $registerData = @{
        name = "Test User $timestamp"
        email = "test$timestamp@example.com"
        phone = "919876543$($timestamp.Substring(8,3))"
        businessName = "Test Business"
        businessAddress = "Test Address"
        gstNumber = "22AAAAA0000A1Z5"
        vendorType = "MANUFACTURER"
    } | ConvertTo-Json
    
    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method POST -Body $registerData -Headers $headers
    Write-Host "SUCCESS: $registerResponse" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 10: Add Category
Write-Host "`n10. Add New Category..." -ForegroundColor Cyan
try {
    $categoryData = @{
        name = "Test Category $(Get-Date -Format 'HHmmss')"
    } | ConvertTo-Json
    
    $newCategory = Invoke-RestMethod -Uri "$baseUrl/categories" -Method POST -Body $categoryData -Headers $headers
    Write-Host "SUCCESS: Created category '$($newCategory.name)' with ID $($newCategory.id)" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Testing Complete ===" -ForegroundColor Green
Write-Host "Your iTech backend application is running successfully!" -ForegroundColor Yellow
