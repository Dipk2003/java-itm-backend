# 🧪 Complete Testing Guide - Quicko API Integration

## 🚀 How to Test Everything

This guide will walk you through testing all the Quicko API integration features step by step.

## 📋 Prerequisites

1. ✅ Spring Boot application running on port 8080
2. ✅ MySQL database connected
3. ✅ Test scripts available in project directory

## 🧪 Testing Methods

### Method 1: **Automated Test Script** (Recommended)
- Uses PowerShell scripts for comprehensive testing
- Tests all endpoints with real authentication
- Provides detailed output and validation

### Method 2: **Manual API Testing**
- Using curl commands or Postman
- Step-by-step manual validation
- Good for understanding the flow

### Method 3: **Browser Testing**
- Testing endpoints that return JSON
- Quick verification of basic functionality

## 🎯 **Method 1: Automated Testing (Best)**

### Step 1: Start the Application
```bash
# Option A: Using deployment script
.\deploy_production.ps1

# Option B: Manual start (if already built)
java -jar target/itech-backend-0.0.1-SNAPSHOT.jar
```

### Step 2: Run Comprehensive Test
```bash
# This tests everything with authentication
.\test_all_quicko_features.ps1
```

### Step 3: Interactive Test (Get OTP from console)
```bash
# This lets you enter the actual OTP from application logs
.\test_quicko_with_auth.ps1
```

## 🔧 **Method 2: Manual Testing**

### Step 1: Test Application Health
```bash
curl http://localhost:8080/actuator/health
```
Expected: `{"status":"UP"}`

### Step 2: Register Test User
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testvendor@example.com",
    "phone": "+1234567890",
    "password": "password123"
  }'
```

### Step 3: Request OTP
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"emailOrPhone": "testvendor@example.com"}'
```

### Step 4: Check Application Console for OTP
Look for output like:
```
🔑 OTP: 123456
📧 EMAIL SENT TO: testvendor@example.com
```

### Step 5: Verify OTP and Get Token
```bash
curl -X POST http://localhost:8080/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "emailOrPhone": "testvendor@example.com",
    "otp": "123456"
  }'
```

### Step 6: Test GST Details Fetch
```bash
curl -X GET "http://localhost:8080/vendor/1/gst/27AABCS0996C1ZS/details" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Step 7: Test Tax Selections Save
```bash
curl -X POST "http://localhost:8080/vendor/1/tax-selections" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "gstNumber": "27AABCS0996C1ZS",
    "selectedGstRates": [
      {
        "category": "Goods",
        "description": "Standard Rate",
        "rate": 18.0,
        "hsn": "8544",
        "taxType": "SGST+CGST",
        "isSelected": true
      }
    ]
  }'
```

## 🌐 **Method 3: Browser Testing**

### Test Basic Endpoints (No Auth Required)
1. Open browser: `http://localhost:8080/actuator/health`
2. Should see: `{"status":"UP"}`

### Test Registration (No Auth Required)
1. Use browser dev tools or Postman
2. POST to: `http://localhost:8080/auth/register`
3. Body: `{"email":"test@test.com","phone":"+1234567890"}`

## 🎯 **What Each Test Validates**

### ✅ Authentication Tests
- User registration works
- OTP generation and sending
- OTP verification and JWT token generation
- Token-based API access

### ✅ Quicko API Integration Tests
- GST details fetching from Quicko API (mock)
- TDS details fetching from Quicko API (mock)
- API error handling and fallbacks
- Retry logic and timeout handling

### ✅ Database Operation Tests
- Tax selections saving to database
- Data retrieval and filtering
- Relationship integrity
- CRUD operations

### ✅ Business Logic Tests
- Vendor can select preferred tax rates
- Selected rates are properly stored
- Dashboard data aggregation
- Tax rate filtering and selection

## 📊 **Expected Test Results**

### ✅ **Success Indicators**
```
✅ Authentication: Working
✅ GST API Integration: Working (Mock Data)
✅ TDS API Integration: Working (Mock Data)
✅ Tax Selection Save: Working
✅ Selected Rates Retrieval: Working
✅ Tax Dashboard: Working
✅ Database Operations: Working
```

### ❌ **Common Issues & Solutions**

#### Issue: "Port 8080 already in use"
**Solution:**
```bash
# Check what's using port 8080
netstat -an | findstr :8080

# Kill existing process or use different port
# Then restart application
```

#### Issue: "Invalid or Expired OTP"
**Solution:**
- Check application console for actual OTP
- OTP expires in 5 minutes
- Request new OTP if expired

#### Issue: "403 Forbidden"
**Solution:**
- Ensure you're using Bearer token in Authorization header
- Verify token is not expired
- Check that user has proper permissions

#### Issue: "Database connection error"
**Solution:**
- Ensure MySQL is running
- Check database credentials in application.properties
- Verify database exists

## 🎮 **Interactive Testing Session**

### Option 1: Full Automated Test
```bash
# This runs everything automatically
.\test_all_quicko_features.ps1 -otp "123456"
```

### Option 2: Step-by-Step Interactive
```bash
# This prompts you for OTP
.\test_quicko_with_auth.ps1
```

### Option 3: Manual Step-by-Step
```bash
# 1. Register user
Invoke-RestMethod -Uri "http://localhost:8080/auth/register" -Method POST -Body '{"email":"test@test.com","phone":"+1234567890"}' -ContentType "application/json"

# 2. Request OTP
Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method POST -Body '{"emailOrPhone":"test@test.com"}' -ContentType "application/json"

# 3. Check console for OTP, then verify
$otp = Read-Host "Enter OTP from console"
$auth = Invoke-RestMethod -Uri "http://localhost:8080/auth/verify-otp" -Method POST -Body "{\"emailOrPhone\":\"test@test.com\",\"otp\":\"$otp\"}" -ContentType "application/json"

# 4. Test with token
$headers = @{"Authorization" = "Bearer $($auth.token)"}
Invoke-RestMethod -Uri "http://localhost:8080/vendor/1/gst/27AABCS0996C1ZS/details" -Headers $headers
```

## 📱 **Production vs Development Testing**

### Development Mode (Mock Data)
```bash
# Uses mock GST/TDS data - always works
$env:QUICKO_API_ENABLED = "false"
.\deploy_production.ps1
```

### Production Mode (Real API)
```bash
# Uses real Quicko API - requires API key
$env:QUICKO_API_KEY = "your_api_key"
$env:QUICKO_API_ENABLED = "true"
.\deploy_production.ps1
```

## 🎯 **Testing Checklist**

### Before Testing
- [ ] Application is running (port 8080)
- [ ] Database is connected
- [ ] Test scripts are available

### Core Features
- [ ] User registration works
- [ ] OTP generation and verification
- [ ] JWT token authentication
- [ ] GST details fetching
- [ ] TDS details fetching  
- [ ] Tax selections saving
- [ ] Selected rates retrieval
- [ ] Tax dashboard data

### Error Handling
- [ ] Invalid OTP handling
- [ ] Expired token handling
- [ ] API timeout handling
- [ ] Database error handling

### Performance
- [ ] API response times < 5 seconds
- [ ] Database queries execute quickly
- [ ] Memory usage is reasonable

## 🚀 **Quick Start Testing**

**Just run this command and follow the prompts:**
```bash
.\test_quicko_with_auth.ps1
```

This will:
1. Register a test user
2. Send OTP to console
3. Prompt you for the OTP
4. Test all Quicko API features
5. Show comprehensive results

**That's it! The easiest way to test everything! 🎉**
