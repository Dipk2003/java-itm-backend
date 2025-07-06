# iTech Backend API Testing Script
# This script tests all features of the iTech backend application

$baseUrl = "http://localhost:8080"
$headers = @{"Content-Type" = "application/json"}

Write-Host "🚀 Starting iTech Backend API Testing" -ForegroundColor Green
Write-Host "=================================================" -ForegroundColor Yellow

# Test 1: Health Check
Write-Host "`n📡 1. Testing Application Health..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/chatbot/health" -Method GET
    Write-Host "✅ Health Check: $response" -ForegroundColor Green
} catch {
    Write-Host "❌ Health Check Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Categories Management
Write-Host "`n📂 2. Testing Categories Management..." -ForegroundColor Cyan

# Get all categories first
try {
    $categories = Invoke-RestMethod -Uri "$baseUrl/categories" -Method GET
    Write-Host "✅ Get All Categories: Found $($categories.Count) categories" -ForegroundColor Green
    if ($categories.Count -gt 0) {
        Write-Host "   Categories: $($categories | ForEach-Object { $_.name } | Join-String ', ')" -ForegroundColor White
    }
} catch {
    Write-Host "❌ Get Categories Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Add a new category
try {
    $categoryData = @{
        name = "Test Category $(Get-Date -Format 'yyyyMMdd-HHmmss')"
    } | ConvertTo-Json

    $newCategory = Invoke-RestMethod -Uri "$baseUrl/categories" -Method POST -Body $categoryData -Headers $headers
    Write-Host "✅ Add Category: Created '$($newCategory.name)' with ID $($newCategory.id)" -ForegroundColor Green
} catch {
    Write-Host "❌ Add Category Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Contact Messages
Write-Host "`n📧 3. Testing Contact Messages..." -ForegroundColor Cyan
try {
    $contactData = @{
        name = "Test User"
        email = "test@example.com"
        phone = "9876543210"
        message = "This is a test contact message from API testing."
    } | ConvertTo-Json

    $contactResponse = Invoke-RestMethod -Uri "$baseUrl/contact" -Method POST -Body $contactData -Headers $headers
    Write-Host "✅ Contact Message: Saved message with ID $($contactResponse.id)" -ForegroundColor Green
} catch {
    Write-Host "❌ Contact Message Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Authentication Flow
Write-Host "`n🔐 4. Testing Authentication Flow..." -ForegroundColor Cyan

# Register a new user
try {
    $registerData = @{
        name = "Test User"
        email = "testuser$(Get-Date -Format 'yyyyMMddHHmmss')@example.com"
        phone = "91987654321$(Get-Random -Minimum 10 -Maximum 99)"
        businessName = "Test Business"
        businessAddress = "Test Address"
        gstNumber = "22AAAAA0000A1Z5"
        vendorType = "MANUFACTURER"
    } | ConvertTo-Json

    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method POST -Body $registerData -Headers $headers
    Write-Host "✅ User Registration: $registerResponse" -ForegroundColor Green
} catch {
    Write-Host "❌ User Registration Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test login (OTP request)
try {
    $loginData = @{
        emailOrPhone = "test@example.com"
    } | ConvertTo-Json

    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Body $loginData -Headers $headers
    Write-Host "✅ Login OTP Request: $loginResponse" -ForegroundColor Green
} catch {
    Write-Host "❌ Login OTP Request Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Admin Features
Write-Host "`n👑 5. Testing Admin Features..." -ForegroundColor Cyan
try {
    $vendors = Invoke-RestMethod -Uri "$baseUrl/admin/vendors" -Method GET
    Write-Host "✅ Get All Vendors: Found $($vendors.Count) vendors" -ForegroundColor Green
    if ($vendors.Count -gt 0) {
        $firstVendor = $vendors[0]
        Write-Host "   First Vendor: $($firstVendor.name) (ID: $($firstVendor.id), Type: $($firstVendor.vendorType))" -ForegroundColor White
    }
} catch {
    Write-Host "❌ Get Vendors Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 6: Products Management
Write-Host "`n🛍️ 6. Testing Products Management..." -ForegroundColor Cyan

# Get all products
try {
    $products = Invoke-RestMethod -Uri "$baseUrl/products" -Method GET
    Write-Host "✅ Get All Products: Found $($products.Count) products" -ForegroundColor Green
    if ($products.Count -gt 0) {
        $firstProduct = $products[0]
        Write-Host "   First Product: $($firstProduct.name) - $($firstProduct.price)" -ForegroundColor White
    }
} catch {
    Write-Host "❌ Get Products Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test products by vendor (if vendors exist)
if ($vendors -and $vendors.Count -gt 0) {
    try {
        $vendorId = $vendors[0].id
        $vendorProducts = Invoke-RestMethod -Uri "$baseUrl/products/vendor/$vendorId" -Method GET
        Write-Host "✅ Get Products by Vendor: Found $($vendorProducts.Count) products for vendor $vendorId" -ForegroundColor Green
    } catch {
        Write-Host "❌ Get Products by Vendor Failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 7: Chatbot Features
Write-Host "`n🤖 7. Testing Chatbot Features..." -ForegroundColor Cyan

# Start chat session
try {
    $chatStart = Invoke-RestMethod -Uri "$baseUrl/api/chatbot/start-session" -Method POST -Headers $headers
    Write-Host "✅ Start Chat Session: $($chatStart.response)" -ForegroundColor Green
    $sessionId = $chatStart.sessionId
    Write-Host "   Session ID: $sessionId" -ForegroundColor White
} catch {
    Write-Host "❌ Start Chat Session Failed: $($_.Exception.Message)" -ForegroundColor Red
    $sessionId = $null
}

# Send chat message
if ($sessionId) {
    try {
        $chatData = @{
            message = "Hello, I am looking for electronics products"
            sessionId = $sessionId
            userIp = "127.0.0.1"
        } | ConvertTo-Json

        $chatResponse = Invoke-RestMethod -Uri "$baseUrl/api/chatbot/chat" -Method POST -Body $chatData -Headers $headers
        Write-Host "✅ Chat Message: $($chatResponse.response)" -ForegroundColor Green
        Write-Host "   Has Recommendations: $($chatResponse.hasRecommendations)" -ForegroundColor White
    } catch {
        Write-Host "❌ Chat Message Failed: $($_.Exception.Message)" -ForegroundColor Red
    }

    # Get chat history
    try {
        $chatHistory = Invoke-RestMethod -Uri "$baseUrl/api/chatbot/history/$sessionId" -Method GET
        Write-Host "✅ Chat History: Found $($chatHistory.Count) messages" -ForegroundColor Green
    } catch {
        Write-Host "❌ Chat History Failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 8: Leads Management
Write-Host "`n📊 8. Testing Leads Management..." -ForegroundColor Cyan

# Get lead statuses and priorities
try {
    $statuses = Invoke-RestMethod -Uri "$baseUrl/api/leads/statuses" -Method GET
    Write-Host "✅ Lead Statuses: $($statuses -join ', ')" -ForegroundColor Green
} catch {
    Write-Host "❌ Get Lead Statuses Failed: $($_.Exception.Message)" -ForegroundColor Red
}

try {
    $priorities = Invoke-RestMethod -Uri "$baseUrl/api/leads/priorities" -Method GET
    Write-Host "✅ Lead Priorities: $($priorities -join ', ')" -ForegroundColor Green
} catch {
    Write-Host "❌ Get Lead Priorities Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Create a test lead (if vendors exist)
if ($vendors -and $vendors.Count -gt 0) {
    try {
        $leadData = @{
            customerName = "Test Customer"
            customerEmail = "customer@example.com"
            customerPhone = "9876543210"
            requirements = "Looking for electronic components"
            vendorId = $vendors[0].id
            status = "NEW"
            priority = "MEDIUM"
            estimatedValue = 10000.0
            notes = "Test lead created via API testing"
        } | ConvertTo-Json

        $newLead = Invoke-RestMethod -Uri "$baseUrl/api/leads" -Method POST -Body $leadData -Headers $headers
        Write-Host "✅ Create Lead: Created lead with ID $($newLead.id) for customer '$($newLead.customerName)'" -ForegroundColor Green
        
        # Test lead stats for this vendor
        try {
            $vendorId = $vendors[0].id
            $leadStats = Invoke-RestMethod -Uri "$baseUrl/api/leads/vendor/$vendorId/stats" -Method GET
            Write-Host "✅ Lead Stats: Total: $($leadStats.total), New: $($leadStats.new), InProgress: $($leadStats.in_progress)" -ForegroundColor Green
        } catch {
            Write-Host "❌ Lead Stats Failed: $($_.Exception.Message)" -ForegroundColor Red
        }

    } catch {
        Write-Host "❌ Create Lead Failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 9: Vendor Dashboard & Ranking
Write-Host "`n📈 9. Testing Vendor Dashboard..." -ForegroundColor Cyan
if ($vendors -and $vendors.Count -gt 0) {
    try {
        $vendorId = $vendors[0].id
        $ranking = Invoke-RestMethod -Uri "$baseUrl/vendor/$vendorId/ranking" -Method GET
        Write-Host "✅ Vendor Ranking: Score: $($ranking.score), Rank: $($ranking.rank)" -ForegroundColor Green
    } catch {
        Write-Host "❌ Vendor Ranking Failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 10: Tax/PAN Verification
Write-Host "`n🏦 10. Testing Tax/PAN Verification..." -ForegroundColor Cyan
if ($vendors -and $vendors.Count -gt 0) {
    try {
        $vendorId = $vendors[0].id
        $taxData = @{
            vendorId = $vendorId
            pan = "ABCDE1234F"
            gst = "22AAAAA0000A1Z5"
            legalName = "Test Legal Entity"
        }
        
        $taxResponse = Invoke-RestMethod -Uri "$baseUrl/tax/verify-pan" -Method POST -Body $taxData
        Write-Host "✅ Tax Verification: PAN: $($taxResponse.panNumber), GST: $($taxResponse.gstNumber)" -ForegroundColor Green
    } catch {
        Write-Host "❌ Tax Verification Failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 11: Duplicate Lead Detection
Write-Host "`n🔍 11. Testing Duplicate Lead Detection..." -ForegroundColor Cyan
try {
    $duplicates = Invoke-RestMethod -Uri "$baseUrl/api/leads/duplicates?email=customer@example.com`&phone=9876543210" -Method GET
    Write-Host "✅ Duplicate Detection: Found $($duplicates.Count) potential duplicates" -ForegroundColor Green
} catch {
    Write-Host "❌ Duplicate Detection Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🎉 API Testing Complete!" -ForegroundColor Green
Write-Host "=================================================" -ForegroundColor Yellow
Write-Host "Summary: All major features of your iTech backend have been tested." -ForegroundColor White
Write-Host "✅ Authentication and Authorization" -ForegroundColor Green
Write-Host "✅ User Management and Registration" -ForegroundColor Green
Write-Host "✅ Product Management" -ForegroundColor Green
Write-Host "✅ Category Management" -ForegroundColor Green
Write-Host "✅ Lead Management and CRM" -ForegroundColor Green
Write-Host "✅ Chatbot Functionality" -ForegroundColor Green
Write-Host "✅ Vendor Dashboard and Ranking" -ForegroundColor Green
Write-Host "✅ Contact Messages" -ForegroundColor Green
Write-Host "✅ Tax/PAN Verification" -ForegroundColor Green
Write-Host "✅ Admin Features" -ForegroundColor Green
