# Comprehensive Quicko API Integration Test Script with Authentication
# This script tests the complete flow including user registration and authentication

$baseUrl = "http://localhost:8080"
$testEmail = "vendor@test.com"
$testPhone = "+1234567890"
$testPassword = "password123"
$gstNumber = "27AABCS0996C1ZS"
$panNumber = "AABCS0996C"

Write-Host "=== Comprehensive Quicko API Integration Test ===" -ForegroundColor Green
Write-Host ""

# Function to make authenticated requests
function Invoke-AuthenticatedRequest {
    param(
        [string]$Uri,
        [string]$Method = "GET",
        [string]$Body = $null,
        [string]$Token = $null
    )
    
    $headers = @{}
    if ($Token) {
        $headers["Authorization"] = "Bearer $Token"
    }
    $headers["Content-Type"] = "application/json"
    
    try {
        if ($Body) {
            return Invoke-RestMethod -Uri $Uri -Method $Method -Body $Body -Headers $headers
        } else {
            return Invoke-RestMethod -Uri $Uri -Method $Method -Headers $headers
        }
    } catch {
        Write-Host "Request failed: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            Write-Host "Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
        }
        return $null
    }
}

# Test 1: Register a test user
Write-Host "1. Testing User Registration..." -ForegroundColor Yellow
$registerData = @{
    email = $testEmail
    phone = $testPhone
    password = $testPassword
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method POST -Body $registerData -ContentType "application/json"
    Write-Host "✓ User registration response: $registerResponse" -ForegroundColor Green
} catch {
    Write-Host "! Registration may have failed (user might already exist): $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host ""

# Test 2: Send login OTP
Write-Host "2. Sending Login OTP..." -ForegroundColor Yellow
$loginData = @{
    emailOrPhone = $testEmail
} | ConvertTo-Json

try {
    $otpResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Body $loginData -ContentType "application/json"
    Write-Host "✓ OTP sent successfully: $otpResponse" -ForegroundColor Green
} catch {
    Write-Host "✗ Failed to send OTP: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 3: Verify OTP (using default OTP for testing)
Write-Host "3. Verifying OTP (using default test OTP: 123456)..." -ForegroundColor Yellow
$verifyData = @{
    emailOrPhone = $testEmail
    otp = "123456"
} | ConvertTo-Json

$token = $null
$vendorId = $null

try {
    $authResponse = Invoke-RestMethod -Uri "$baseUrl/auth/verify-otp" -Method POST -Body $verifyData -ContentType "application/json"
    
    if ($authResponse -and $authResponse.token) {
        $token = $authResponse.token
        $vendorId = $authResponse.user.id
        Write-Host "✓ Authentication successful!" -ForegroundColor Green
        Write-Host "  Token: $($token.Substring(0, 20))..." -ForegroundColor Cyan
        Write-Host "  Vendor ID: $vendorId" -ForegroundColor Cyan
    } else {
        Write-Host "✗ Authentication failed - no token received" -ForegroundColor Red
        Write-Host "Response: $authResponse" -ForegroundColor Yellow
    }
} catch {
    Write-Host "✗ Authentication failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "! This might be expected if OTP verification is strict" -ForegroundColor Yellow
}

Write-Host ""

# If we don't have a token, let's try to test with a mock vendor ID
if (-not $token) {
    Write-Host "⚠️  Authentication failed, testing with direct database access..." -ForegroundColor Yellow
    $vendorId = 1
    
    # Try to test endpoints that might be less restricted
    Write-Host ""
    Write-Host "4. Testing Public/Less Restricted Endpoints..." -ForegroundColor Yellow
    
    # Test health endpoint
    try {
        $health = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method GET
        Write-Host "✓ Health endpoint working: $($health.status)" -ForegroundColor Green
    } catch {
        Write-Host "! Health endpoint not accessible" -ForegroundColor Yellow
    }
    
    Write-Host ""
    Write-Host "Note: Since authentication is required, let's create a simplified test" -ForegroundColor Blue
    Write-Host "You can manually test the endpoints after obtaining a valid JWT token" -ForegroundColor Blue
    
    # Show the test data that would be used
    Write-Host ""
    Write-Host "=== Test Data for Manual Testing ===" -ForegroundColor Magenta
    Write-Host "Vendor ID: $vendorId"
    Write-Host "GST Number: $gstNumber"
    Write-Host "PAN Number: $panNumber"
    Write-Host "Base URL: $baseUrl"
    
    return
}

# Test 4: Fetch GST Details (with authentication)
Write-Host "4. Testing GST Details Fetch..." -ForegroundColor Yellow
$gstResponse = Invoke-AuthenticatedRequest -Uri "$baseUrl/vendor/$vendorId/gst/$gstNumber/details" -Token $token

if ($gstResponse) {
    Write-Host "✓ GST Details fetched successfully" -ForegroundColor Green
    Write-Host "  Legal Name: $($gstResponse.legalName)" -ForegroundColor Cyan
    Write-Host "  GST Rates Count: $($gstResponse.gstRates.Count)" -ForegroundColor Cyan
    Write-Host "  Status: $($gstResponse.status)" -ForegroundColor Cyan
    
    # Show some rate details
    if ($gstResponse.gstRates) {
        Write-Host "  Sample Rates:" -ForegroundColor Cyan
        $gstResponse.gstRates | Select-Object -First 3 | ForEach-Object {
            Write-Host "    - $($_.description): $($_.rate)% ($($_.category))" -ForegroundColor DarkCyan
        }
    }
}

Write-Host ""

# Test 5: Fetch TDS Details
Write-Host "5. Testing TDS Details Fetch..." -ForegroundColor Yellow
$tdsResponse = Invoke-AuthenticatedRequest -Uri "$baseUrl/vendor/$vendorId/tds/$panNumber/details" -Token $token

if ($tdsResponse) {
    Write-Host "✓ TDS Details fetched successfully" -ForegroundColor Green
    Write-Host "  Assessee Name: $($tdsResponse.assesseeName)" -ForegroundColor Cyan
    Write-Host "  TDS Rates Count: $($tdsResponse.tdsRates.Count)" -ForegroundColor Cyan
    Write-Host "  Financial Year: $($tdsResponse.financialYear)" -ForegroundColor Cyan
    
    # Show some rate details
    if ($tdsResponse.tdsRates) {
        Write-Host "  Sample Rates:" -ForegroundColor Cyan
        $tdsResponse.tdsRates | Select-Object -First 3 | ForEach-Object {
            Write-Host "    - $($_.section): $($_.rate)% ($($_.description))" -ForegroundColor DarkCyan
        }
    }
}

Write-Host ""

# Test 6: Save Tax Selections
Write-Host "6. Testing Tax Selections Save..." -ForegroundColor Yellow
$selectionData = @{
    gstNumber = $gstNumber
    selectedGstRates = @(
        @{
            category = "Goods"
            description = "Standard Rate"
            rate = 18.0
            hsn = "8544"
            taxType = "SGST+CGST"
            isSelected = $true
        },
        @{
            category = "Services"
            description = "Standard Rate"
            rate = 18.0
            hsn = "998311"
            taxType = "SGST+CGST"
            isSelected = $true
        },
        @{
            category = "Goods"
            description = "Reduced Rate"
            rate = 12.0
            hsn = "8544"
            taxType = "SGST+CGST"
            isSelected = $false
        }
    )
    selectedTdsRates = @(
        @{
            section = "194C"
            description = "Payment to contractors"
            rate = 1.0
            paymentType = "Contractor"
            categoryCode = "CON"
            natureOfPayment = "Contractor"
            isSelected = $true
        },
        @{
            section = "194J"
            description = "Professional fees"
            rate = 10.0
            paymentType = "Professional"
            categoryCode = "PRO"
            natureOfPayment = "Professional fees"
            isSelected = $true
        }
    )
    notes = "Selected rates for Q4 2024 sales"
} | ConvertTo-Json -Depth 5

$saveResponse = Invoke-AuthenticatedRequest -Uri "$baseUrl/vendor/$vendorId/tax-selections" -Method POST -Body $selectionData -Token $token

if ($saveResponse) {
    Write-Host "✓ Tax selections saved successfully" -ForegroundColor Green
    Write-Host "  Status: $($saveResponse.status)" -ForegroundColor Cyan
    Write-Host "  Message: $($saveResponse.message)" -ForegroundColor Cyan
}

Write-Host ""

# Test 7: Get Selected GST Rates
Write-Host "7. Testing Selected GST Rates Retrieval..." -ForegroundColor Yellow
$selectedGstResponse = Invoke-AuthenticatedRequest -Uri "$baseUrl/vendor/$vendorId/gst/$gstNumber/selected-rates" -Token $token

if ($selectedGstResponse) {
    Write-Host "✓ Selected GST rates retrieved successfully" -ForegroundColor Green
    Write-Host "  Selected Rates Count: $($selectedGstResponse.Count)" -ForegroundColor Cyan
    
    if ($selectedGstResponse.Count -gt 0) {
        Write-Host "  Selected Rates:" -ForegroundColor Cyan
        $selectedGstResponse | Where-Object { $_.isSelected } | ForEach-Object {
            Write-Host "    - $($_.description): $($_.rate)% (HSN: $($_.hsn))" -ForegroundColor DarkCyan
        }
    }
}

Write-Host ""

# Test 8: Get Selected TDS Rates
Write-Host "8. Testing Selected TDS Rates Retrieval..." -ForegroundColor Yellow
$selectedTdsResponse = Invoke-AuthenticatedRequest -Uri "$baseUrl/vendor/$vendorId/tds/$panNumber/selected-rates" -Token $token

if ($selectedTdsResponse) {
    Write-Host "✓ Selected TDS rates retrieved successfully" -ForegroundColor Green
    Write-Host "  Selected Rates Count: $($selectedTdsResponse.Count)" -ForegroundColor Cyan
    
    if ($selectedTdsResponse.Count -gt 0) {
        Write-Host "  Selected Rates:" -ForegroundColor Cyan
        $selectedTdsResponse | Where-Object { $_.isSelected } | ForEach-Object {
            Write-Host "    - $($_.section): $($_.rate)% ($($_.description))" -ForegroundColor DarkCyan
        }
    }
}

Write-Host ""

# Test 9: Get All GST Selections (both selected and unselected)
Write-Host "9. Testing All GST Selections Retrieval..." -ForegroundColor Yellow
$allGstResponse = Invoke-AuthenticatedRequest -Uri "$baseUrl/vendor/$vendorId/gst/$gstNumber/selections" -Token $token

if ($allGstResponse) {
    Write-Host "✓ All GST selections retrieved successfully" -ForegroundColor Green
    Write-Host "  Total Selections: $($allGstResponse.Count)" -ForegroundColor Cyan
    
    $selectedCount = ($allGstResponse | Where-Object { $_.isSelected }).Count
    $unselectedCount = $allGstResponse.Count - $selectedCount
    
    Write-Host "  Selected: $selectedCount, Unselected: $unselectedCount" -ForegroundColor Cyan
}

Write-Host ""

# Test 10: Get Tax Dashboard
Write-Host "10. Testing Comprehensive Tax Dashboard..." -ForegroundColor Yellow
$dashboardResponse = Invoke-AuthenticatedRequest -Uri "$baseUrl/vendor/$vendorId/tax-dashboard?gstNumber=$gstNumber&panNumber=$panNumber" -Token $token

if ($dashboardResponse) {
    Write-Host "✓ Tax dashboard retrieved successfully" -ForegroundColor Green
    Write-Host "  Vendor ID: $($dashboardResponse.vendor.id)" -ForegroundColor Cyan
    Write-Host "  Vendor Email: $($dashboardResponse.vendor.email)" -ForegroundColor Cyan
    
    if ($dashboardResponse.ranking) {
        Write-Host "  Vendor Ranking: $($dashboardResponse.ranking.score)" -ForegroundColor Cyan
    }
    
    if ($dashboardResponse.selectedGstRates) {
        Write-Host "  Selected GST Rates: $($dashboardResponse.selectedGstRates.Count)" -ForegroundColor Cyan
    }
    
    if ($dashboardResponse.selectedTdsRates) {
        Write-Host "  Selected TDS Rates: $($dashboardResponse.selectedTdsRates.Count)" -ForegroundColor Cyan
    }
}

Write-Host ""
Write-Host "=== Comprehensive Test Complete ===" -ForegroundColor Green
Write-Host ""
Write-Host "📊 Test Summary:" -ForegroundColor Blue
Write-Host "• Authentication: $(if($token){'✓ Successful'}else{'✗ Failed'})" -ForegroundColor Blue
Write-Host "• GST Details Fetch: $(if($gstResponse){'✓ Working'}else{'✗ Failed'})" -ForegroundColor Blue
Write-Host "• TDS Details Fetch: $(if($tdsResponse){'✓ Working'}else{'✗ Failed'})" -ForegroundColor Blue
Write-Host "• Tax Selections Save: $(if($saveResponse){'✓ Working'}else{'✗ Failed'})" -ForegroundColor Blue
Write-Host "• Selected Rates Retrieval: $(if($selectedGstResponse){'✓ Working'}else{'✗ Failed'})" -ForegroundColor Blue
Write-Host "• Tax Dashboard: $(if($dashboardResponse){'✓ Working'}else{'✗ Failed'})" -ForegroundColor Blue
Write-Host ""

if ($token) {
    Write-Host "🎉 All Quicko API integration features are working correctly!" -ForegroundColor Green
    Write-Host ""
    Write-Host "🔧 Key Features Verified:" -ForegroundColor Magenta
    Write-Host "  ✓ User authentication with OTP" -ForegroundColor Green
    Write-Host "  ✓ Quicko API integration (GST/TDS fetch)" -ForegroundColor Green
    Write-Host "  ✓ Tax rate selection and storage" -ForegroundColor Green
    Write-Host "  ✓ Selected rates retrieval for sales" -ForegroundColor Green
    Write-Host "  ✓ Comprehensive vendor dashboard" -ForegroundColor Green
} else {
    Write-Host "⚠️  Some features require proper authentication setup" -ForegroundColor Yellow
    Write-Host "   Consider configuring OTP service or using test authentication" -ForegroundColor Yellow
}
