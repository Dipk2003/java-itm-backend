# Quicko API Integration Test Script
# This script tests the Quicko API integration endpoints

$baseUrl = "http://localhost:8080"
$vendorId = 1
$gstNumber = "27AABCS0996C1ZS"
$panNumber = "AABCS0996C"

Write-Host "=== Testing Quicko API Integration ===" -ForegroundColor Green
Write-Host ""

# Test 1: Fetch GST Details
Write-Host "1. Testing GST Details Fetch..." -ForegroundColor Yellow
try {
    $gstResponse = Invoke-RestMethod -Uri "$baseUrl/vendor/$vendorId/gst/$gstNumber/details" -Method GET
    Write-Host "✓ GST Details fetched successfully" -ForegroundColor Green
    Write-Host "  Legal Name: $($gstResponse.legalName)"
    Write-Host "  GST Rates Count: $($gstResponse.gstRates.Count)"
} catch {
    Write-Host "✗ Failed to fetch GST details: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 2: Fetch TDS Details
Write-Host "2. Testing TDS Details Fetch..." -ForegroundColor Yellow
try {
    $tdsResponse = Invoke-RestMethod -Uri "$baseUrl/vendor/$vendorId/tds/$panNumber/details" -Method GET
    Write-Host "✓ TDS Details fetched successfully" -ForegroundColor Green
    Write-Host "  Assessee Name: $($tdsResponse.assesseeName)"
    Write-Host "  TDS Rates Count: $($tdsResponse.tdsRates.Count)"
} catch {
    Write-Host "✗ Failed to fetch TDS details: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 3: Save Tax Selections
Write-Host "3. Testing Tax Selections Save..." -ForegroundColor Yellow
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
        }
    )
}

try {
    $saveResponse = Invoke-RestMethod -Uri "$baseUrl/vendor/$vendorId/tax-selections" -Method POST -Body ($selectionData | ConvertTo-Json -Depth 5) -ContentType "application/json"
    Write-Host "✓ Tax selections saved successfully" -ForegroundColor Green
    Write-Host "  Status: $($saveResponse.status)"
} catch {
    Write-Host "✗ Failed to save tax selections: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 4: Get Selected GST Rates
Write-Host "4. Testing Selected GST Rates Retrieval..." -ForegroundColor Yellow
try {
    $selectedGstResponse = Invoke-RestMethod -Uri "$baseUrl/vendor/$vendorId/gst/$gstNumber/selected-rates" -Method GET
    Write-Host "✓ Selected GST rates retrieved successfully" -ForegroundColor Green
    Write-Host "  Selected Rates Count: $($selectedGstResponse.Count)"
    
    foreach ($rate in $selectedGstResponse) {
        if ($rate.isSelected) {
            Write-Host "    - $($rate.description): $($rate.rate)%" -ForegroundColor Cyan
        }
    }
} catch {
    Write-Host "✗ Failed to get selected GST rates: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 5: Get Tax Dashboard
Write-Host "5. Testing Tax Dashboard..." -ForegroundColor Yellow
try {
    $dashboardResponse = Invoke-RestMethod -Uri "$baseUrl/vendor/$vendorId/tax-dashboard?gstNumber=$gstNumber&panNumber=$panNumber" -Method GET
    Write-Host "✓ Tax dashboard retrieved successfully" -ForegroundColor Green
    Write-Host "  Vendor ID: $($dashboardResponse.vendor.id)"
    
    if ($dashboardResponse.selectedGstRates) {
        Write-Host "  Selected GST Rates: $($dashboardResponse.selectedGstRates.Count)"
    }
    
    if ($dashboardResponse.selectedTdsRates) {
        Write-Host "  Selected TDS Rates: $($dashboardResponse.selectedTdsRates.Count)"
    }
} catch {
    Write-Host "✗ Failed to get tax dashboard: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== Test Complete ===" -ForegroundColor Green
Write-Host ""
Write-Host "Note: Make sure your Spring Boot application is running on localhost:8080" -ForegroundColor Blue
Write-Host "and that you have a vendor with ID 1 in your database." -ForegroundColor Blue
