# Quicko API Integration - Vendor Dashboard

This integration allows vendors to fetch GST and TDS details from the Quicko API and select which rates to use for their sales transactions.

## Features

1. **Fetch GST Details**: Get comprehensive GST information from Quicko API
2. **Fetch TDS Details**: Get TDS rates and details from Quicko API  
3. **Save Tax Selections**: Allow vendors to choose and save their preferred GST/TDS rates
4. **Retrieve Selections**: Get vendor's saved tax configurations
5. **Dashboard Integration**: Complete tax dashboard with all vendor data

## API Endpoints

### 1. Fetch GST Details from Quicko

**GET** `/vendor/{vendorId}/gst/{gstNumber}/details`

Fetches GST details from Quicko API for the given GST number.

**Example:**
```bash
curl -X GET "http://localhost:8080/vendor/1/gst/27AABCS0996C1ZS/details"
```

**Response:**
```json
{
  "gstin": "27AABCS0996C1ZS",
  "legalName": "Demo Company Pvt Ltd",
  "tradeName": "Demo Company",
  "businessType": "Private Limited Company",
  "status": "Active",
  "gstRates": [
    {
      "category": "Goods",
      "description": "Standard Rate",
      "rate": 18.0,
      "hsn": "8544",
      "taxType": "SGST+CGST",
      "isApplicable": true
    }
  ]
}
```

### 2. Fetch TDS Details from Quicko

**GET** `/vendor/{vendorId}/tds/{panNumber}/details`

Fetches TDS details from Quicko API for the given PAN number.

**Example:**
```bash
curl -X GET "http://localhost:8080/vendor/1/tds/AABCS0996C/details"
```

### 3. Save Vendor Tax Selections

**POST** `/vendor/{vendorId}/tax-selections`

Saves vendor's selected GST and TDS rates to the database.

**Request Body:**
```json
{
  "gstNumber": "27AABCS0996C1ZS",
  "selectedGstRates": [
    {
      "category": "Goods",
      "description": "Standard Rate",
      "rate": 18.0,
      "hsn": "8544",
      "taxType": "SGST+CGST",
      "isSelected": true
    },
    {
      "category": "Services",
      "description": "Standard Rate", 
      "rate": 18.0,
      "hsn": "998311",
      "taxType": "SGST+CGST",
      "isSelected": true
    }
  ],
  "selectedTdsRates": [
    {
      "section": "194C",
      "description": "Payment to contractors",
      "rate": 1.0,
      "paymentType": "Contractor",
      "categoryCode": "CON",
      "natureOfPayment": "Contractor",
      "isSelected": true
    }
  ]
}
```

### 4. Get Vendor GST Selections

**GET** `/vendor/{vendorId}/gst/{gstNumber}/selections`

Returns all GST rate selections (both selected and unselected) for a vendor.

### 5. Get Selected GST Rates Only

**GET** `/vendor/{vendorId}/gst/{gstNumber}/selected-rates`

Returns only the GST rates that the vendor has selected for use in sales.

**Example:**
```bash
curl -X GET "http://localhost:8080/vendor/1/gst/27AABCS0996C1ZS/selected-rates"
```

### 6. Get Vendor TDS Selections

**GET** `/vendor/{vendorId}/tds/{panNumber}/selections`

Returns all TDS rate selections for a vendor.

### 7. Get Selected TDS Rates Only

**GET** `/vendor/{vendorId}/tds/{panNumber}/selected-rates`

Returns only the TDS rates that the vendor has selected.

### 8. Complete Tax Dashboard

**GET** `/vendor/{vendorId}/tax-dashboard?gstNumber={gst}&panNumber={pan}`

Returns comprehensive dashboard data including vendor info, ranking, and tax selections.

**Example:**
```bash
curl -X GET "http://localhost:8080/vendor/1/tax-dashboard?gstNumber=27AABCS0996C1ZS&panNumber=AABCS0996C"
```

## Configuration

### Application Properties

```properties
# Quicko API Configuration
quicko.api.base-url=https://api.quicko.com
quicko.api.key=YOUR_QUICKO_API_KEY
quicko.api.timeout=30
```

### Setting up Quicko API

1. Sign up at [Quicko Developer Portal](https://quicko.com/developers)
2. Get your API key
3. Replace the empty `quicko.api.key` value in `application.properties`
4. Verify the base URL with Quicko's documentation

## Database Tables

The integration creates these new tables:

### vendor_gst_selection
- Stores vendor's GST rate selections
- Links to vendor via vendor_id
- Tracks which rates are selected for use

### vendor_tds_selection  
- Stores vendor's TDS rate selections
- Links to vendor via vendor_id
- Tracks which TDS sections are selected

## Usage Flow

1. **Vendor Dashboard Access**: Vendor logs into their dashboard
2. **Fetch Tax Details**: System calls Quicko API to get latest GST/TDS rates
3. **Rate Selection**: Vendor reviews and selects which rates to use for sales
4. **Save Selections**: Vendor's choices are saved to database
5. **Sales Integration**: Selected rates are used when creating sales transactions

## Error Handling

- API timeouts are handled gracefully (30 second default)
- Mock data is returned if Quicko API is unavailable
- All errors are logged for debugging
- Fallback responses ensure system continues to work

## Testing

When `quicko.api.key` is empty, the system returns mock data for testing:

- GST details with common tax rates (5%, 12%, 18%, 28%)
- TDS details with standard sections (194C, 194I, 194J, etc.)
- Realistic business information for demo purposes

## Security

- All endpoints require vendor authentication
- Vendor can only access their own tax data
- GST/TDS details are fetched only for authenticated vendor
- Sensitive API keys are stored in configuration, not code
