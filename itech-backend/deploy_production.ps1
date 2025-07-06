# Production Deployment Script for Quicko API Integration
# This script helps deploy the application with proper Quicko API configuration

param(
    [string]$QuickoApiKey = "",
    [bool]$EnableQuickoApi = $false,
    [string]$Environment = "development"
)

Write-Host "=== 🚀 Quicko API Integration - Production Deployment ===" -ForegroundColor Green
Write-Host ""

# Validate parameters
if ($Environment -eq "production" -and $EnableQuickoApi -and [string]::IsNullOrEmpty($QuickoApiKey)) {
    Write-Host "❌ Error: Quicko API key is required for production deployment with API enabled" -ForegroundColor Red
    Write-Host "Usage: .\deploy_production.ps1 -QuickoApiKey 'your_api_key' -EnableQuickoApi $true -Environment 'production'" -ForegroundColor Yellow
    exit 1
}

Write-Host "📋 Deployment Configuration:" -ForegroundColor Blue
Write-Host "  Environment: $Environment" -ForegroundColor Cyan
Write-Host "  Quicko API Enabled: $EnableQuickoApi" -ForegroundColor Cyan
Write-Host "  API Key Configured: $(!([string]::IsNullOrEmpty($QuickoApiKey)))" -ForegroundColor Cyan
Write-Host ""

# Set environment variables
Write-Host "🔧 Setting Environment Variables..." -ForegroundColor Yellow

if (![string]::IsNullOrEmpty($QuickoApiKey)) {
    $env:QUICKO_API_KEY = $QuickoApiKey
    Write-Host "  ✓ QUICKO_API_KEY set" -ForegroundColor Green
} else {
    Write-Host "  ⚠️  QUICKO_API_KEY not set (using mock data)" -ForegroundColor Yellow
}

$env:QUICKO_API_ENABLED = $EnableQuickoApi.ToString().ToLower()
Write-Host "  ✓ QUICKO_API_ENABLED set to $($env:QUICKO_API_ENABLED)" -ForegroundColor Green

# Additional production environment variables
if ($Environment -eq "production") {
    $env:SPRING_PROFILES_ACTIVE = "production"
    Write-Host "  ✓ SPRING_PROFILES_ACTIVE set to production" -ForegroundColor Green
}

Write-Host ""

# Build the application
Write-Host "🔨 Building Application..." -ForegroundColor Yellow
try {
    if (Test-Path "mvnw.cmd") {
        Write-Host "  Using Maven wrapper..." -ForegroundColor Cyan
        cmd /c "mvnw.cmd clean package -DskipTests"
    } elseif (Get-Command mvn -ErrorAction SilentlyContinue) {
        Write-Host "  Using system Maven..." -ForegroundColor Cyan
        mvn clean package -DskipTests
    } else {
        Write-Host "  ❌ Maven not found. Please install Maven or ensure mvnw.cmd works" -ForegroundColor Red
        exit 1
    }
    Write-Host "  ✓ Build completed successfully" -ForegroundColor Green
} catch {
    Write-Host "  ❌ Build failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Database setup reminder
Write-Host "💾 Database Setup:" -ForegroundColor Yellow
Write-Host "  Ensure the following tables are created:" -ForegroundColor Cyan
Write-Host "    - vendor_gst_selection" -ForegroundColor White
Write-Host "    - vendor_tds_selection" -ForegroundColor White
Write-Host "  The application will auto-create these with spring.jpa.hibernate.ddl-auto=update" -ForegroundColor Cyan
Write-Host ""

# Start the application
Write-Host "🚀 Starting Application..." -ForegroundColor Yellow

if ($Environment -eq "production") {
    Write-Host "  Production mode - Application will run on configured port" -ForegroundColor Cyan
    Write-Host "  Monitor logs for Quicko API integration status" -ForegroundColor Cyan
} else {
    Write-Host "  Development mode - Application will run on port 8080" -ForegroundColor Cyan
}

# Check if application is already running
$existingProcess = netstat -an | findstr ":8080" 2>$null
if ($existingProcess) {
    Write-Host "  ⚠️  Port 8080 is already in use. Stop existing application first." -ForegroundColor Yellow
    $choice = Read-Host "Do you want to continue anyway? (y/n)"
    if ($choice -ne "y" -and $choice -ne "Y") {
        exit 1
    }
}

try {
    if (Test-Path "target/*.jar") {
        $jarFile = Get-ChildItem "target/*.jar" | Where-Object { $_.Name -notlike "*-sources.jar" -and $_.Name -notlike "*-javadoc.jar" } | Select-Object -First 1
        Write-Host "  Starting JAR: $($jarFile.Name)" -ForegroundColor Cyan
        
        if ($Environment -eq "production") {
            # Production mode - run in background
            Start-Process java -ArgumentList "-jar", $jarFile.FullName -WindowStyle Hidden
            Write-Host "  ✓ Application started in background" -ForegroundColor Green
        } else {
            # Development mode - run in current window
            Write-Host "  Starting in development mode..." -ForegroundColor Cyan
            Write-Host "  Press Ctrl+C to stop the application" -ForegroundColor Yellow
            java -jar $jarFile.FullName
        }
    } else {
        Write-Host "  ❌ JAR file not found in target directory" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "  ❌ Failed to start application: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "=== 🎉 Deployment Summary ===" -ForegroundColor Green
Write-Host ""
Write-Host "✅ Application deployed successfully!" -ForegroundColor Green
Write-Host ""

if ($EnableQuickoApi -and ![string]::IsNullOrEmpty($QuickoApiKey)) {
    Write-Host "🔗 Quicko API Integration: ENABLED" -ForegroundColor Green
    Write-Host "  - Real-time GST/TDS data fetching" -ForegroundColor White
    Write-Host "  - Production API calls to Quicko" -ForegroundColor White
} else {
    Write-Host "🔧 Mock Data Mode: ENABLED" -ForegroundColor Yellow
    Write-Host "  - Using realistic mock GST/TDS data" -ForegroundColor White
    Write-Host "  - Perfect for development and testing" -ForegroundColor White
    Write-Host "  - To enable real API: Set QUICKO_API_KEY and QUICKO_API_ENABLED=true" -ForegroundColor White
}

Write-Host ""
Write-Host "📋 Available Endpoints:" -ForegroundColor Blue
Write-Host "  Authentication:" -ForegroundColor Cyan
Write-Host "    POST /auth/register" -ForegroundColor White
Write-Host "    POST /auth/login" -ForegroundColor White
Write-Host "    POST /auth/verify-otp" -ForegroundColor White
Write-Host ""
Write-Host "  Quicko Integration:" -ForegroundColor Cyan
Write-Host "    GET  /vendor/{id}/gst/{gst}/details" -ForegroundColor White
Write-Host "    GET  /vendor/{id}/tds/{pan}/details" -ForegroundColor White
Write-Host "    POST /vendor/{id}/tax-selections" -ForegroundColor White
Write-Host "    GET  /vendor/{id}/gst/{gst}/selected-rates" -ForegroundColor White
Write-Host "    GET  /vendor/{id}/tax-dashboard" -ForegroundColor White
Write-Host ""
Write-Host "🔗 Application URL: http://localhost:8080" -ForegroundColor Blue
Write-Host ""

if ($Environment -eq "production") {
    Write-Host "⚠️  Production Notes:" -ForegroundColor Yellow
    Write-Host "  - Monitor application logs for any issues" -ForegroundColor White
    Write-Host "  - Set up proper SSL/HTTPS for production" -ForegroundColor White
    Write-Host "  - Configure proper database connection" -ForegroundColor White
    Write-Host "  - Set up monitoring and alerting" -ForegroundColor White
}
