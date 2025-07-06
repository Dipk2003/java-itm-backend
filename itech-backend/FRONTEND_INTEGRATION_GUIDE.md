# 🎨 Frontend Integration Guide - Quicko API

This guide helps you integrate the Quicko API backend with your frontend application for a complete vendor dashboard experience.

## 🚀 Quick Start

### Backend Endpoints Overview
```
Base URL: http://localhost:8080
Authentication: Bearer Token (JWT)
```

## 📱 Frontend User Flow

### 1. **Vendor Authentication**
```javascript
// Step 1: Register/Login
const loginResponse = await fetch('/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ emailOrPhone: 'vendor@test.com' })
});

// Step 2: Verify OTP
const authResponse = await fetch('/auth/verify-otp', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ 
        emailOrPhone: 'vendor@test.com', 
        otp: '123456' 
    })
});

const { token } = await authResponse.json();
```

### 2. **Tax Dashboard - Main Component**
```javascript
// React Component Example
import React, { useState, useEffect } from 'react';

const VendorTaxDashboard = ({ vendorId, token }) => {
    const [dashboardData, setDashboardData] = useState(null);
    const [gstRates, setGstRates] = useState([]);
    const [tdsRates, setTdsRates] = useState([]);
    const [loading, setLoading] = useState(true);

    // Fetch comprehensive dashboard data
    useEffect(() => {
        fetchDashboardData();
    }, [vendorId]);

    const fetchDashboardData = async () => {
        try {
            const response = await fetch(
                `/vendor/${vendorId}/tax-dashboard?gstNumber=27AABCS0996C1ZS&panNumber=AABCS0996C`,
                {
                    headers: { 'Authorization': `Bearer ${token}` }
                }
            );
            const data = await response.json();
            setDashboardData(data);
            setLoading(false);
        } catch (error) {
            console.error('Error fetching dashboard:', error);
            setLoading(false);
        }
    };

    return (
        <div className="tax-dashboard">
            <h1>Tax Management Dashboard</h1>
            
            {loading ? (
                <div>Loading...</div>
            ) : (
                <>
                    <VendorInfo vendor={dashboardData.vendor} />
                    <GstManagement vendorId={vendorId} token={token} />
                    <TdsManagement vendorId={vendorId} token={token} />
                    <SelectedRatesSummary 
                        gstRates={dashboardData.selectedGstRates}
                        tdsRates={dashboardData.selectedTdsRates}
                    />
                </>
            )}
        </div>
    );
};
```

### 3. **GST Management Component**
```javascript
const GstManagement = ({ vendorId, token }) => {
    const [gstDetails, setGstDetails] = useState(null);
    const [selectedRates, setSelectedRates] = useState([]);
    const [gstNumber, setGstNumber] = useState('27AABCS0996C1ZS');

    // Fetch GST details from Quicko API
    const fetchGstDetails = async () => {
        try {
            const response = await fetch(
                `/vendor/${vendorId}/gst/${gstNumber}/details`,
                {
                    headers: { 'Authorization': `Bearer ${token}` }
                }
            );
            const data = await response.json();
            setGstDetails(data);
        } catch (error) {
            console.error('Error fetching GST details:', error);
        }
    };

    // Save selected GST rates
    const saveGstSelections = async () => {
        const selectionData = {
            gstNumber: gstNumber,
            selectedGstRates: selectedRates.map(rate => ({
                ...rate,
                isSelected: true
            }))
        };

        try {
            const response = await fetch(`/vendor/${vendorId}/tax-selections`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(selectionData)
            });
            
            if (response.ok) {
                alert('GST selections saved successfully!');
            }
        } catch (error) {
            console.error('Error saving selections:', error);
        }
    };

    return (
        <div className="gst-management">
            <h2>GST Rate Management</h2>
            
            <div className="gst-input">
                <input 
                    type="text" 
                    value={gstNumber}
                    onChange={(e) => setGstNumber(e.target.value)}
                    placeholder="Enter GST Number"
                />
                <button onClick={fetchGstDetails}>Fetch GST Details</button>
            </div>

            {gstDetails && (
                <div className="gst-details">
                    <h3>{gstDetails.legalName}</h3>
                    <p>Status: {gstDetails.status}</p>
                    
                    <div className="gst-rates">
                        <h4>Available GST Rates:</h4>
                        {gstDetails.gstRates.map((rate, index) => (
                            <div key={index} className="rate-item">
                                <label>
                                    <input 
                                        type="checkbox"
                                        checked={selectedRates.includes(rate)}
                                        onChange={(e) => {
                                            if (e.target.checked) {
                                                setSelectedRates([...selectedRates, rate]);
                                            } else {
                                                setSelectedRates(selectedRates.filter(r => r !== rate));
                                            }
                                        }}
                                    />
                                    {rate.description} - {rate.rate}% 
                                    ({rate.category}, HSN: {rate.hsn})
                                </label>
                            </div>
                        ))}
                    </div>
                    
                    <button onClick={saveGstSelections}>
                        Save Selected Rates
                    </button>
                </div>
            )}
        </div>
    );
};
```

### 4. **TDS Management Component**
```javascript
const TdsManagement = ({ vendorId, token }) => {
    const [tdsDetails, setTdsDetails] = useState(null);
    const [selectedTdsRates, setSelectedTdsRates] = useState([]);
    const [panNumber, setPanNumber] = useState('AABCS0996C');

    // Fetch TDS details from Quicko API
    const fetchTdsDetails = async () => {
        try {
            const response = await fetch(
                `/vendor/${vendorId}/tds/${panNumber}/details`,
                {
                    headers: { 'Authorization': `Bearer ${token}` }
                }
            );
            const data = await response.json();
            setTdsDetails(data);
        } catch (error) {
            console.error('Error fetching TDS details:', error);
        }
    };

    return (
        <div className="tds-management">
            <h2>TDS Rate Management</h2>
            
            <div className="tds-input">
                <input 
                    type="text" 
                    value={panNumber}
                    onChange={(e) => setPanNumber(e.target.value)}
                    placeholder="Enter PAN Number"
                />
                <button onClick={fetchTdsDetails}>Fetch TDS Details</button>
            </div>

            {tdsDetails && (
                <div className="tds-details">
                    <h3>{tdsDetails.assesseeName}</h3>
                    <p>Financial Year: {tdsDetails.financialYear}</p>
                    
                    <div className="tds-rates">
                        <h4>Available TDS Rates:</h4>
                        {tdsDetails.tdsRates.map((rate, index) => (
                            <div key={index} className="rate-item">
                                <label>
                                    <input 
                                        type="checkbox"
                                        onChange={(e) => {
                                            // Handle TDS rate selection
                                        }}
                                    />
                                    Section {rate.section} - {rate.rate}% 
                                    ({rate.description})
                                </label>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};
```

### 5. **Selected Rates Summary**
```javascript
const SelectedRatesSummary = ({ vendorId, token }) => {
    const [selectedGstRates, setSelectedGstRates] = useState([]);
    const [selectedTdsRates, setSelectedTdsRates] = useState([]);

    useEffect(() => {
        fetchSelectedRates();
    }, []);

    const fetchSelectedRates = async () => {
        try {
            // Fetch selected GST rates
            const gstResponse = await fetch(
                `/vendor/${vendorId}/gst/27AABCS0996C1ZS/selected-rates`,
                {
                    headers: { 'Authorization': `Bearer ${token}` }
                }
            );
            const gstData = await gstResponse.json();
            setSelectedGstRates(gstData.filter(rate => rate.isSelected));

            // Fetch selected TDS rates
            const tdsResponse = await fetch(
                `/vendor/${vendorId}/tds/AABCS0996C/selected-rates`,
                {
                    headers: { 'Authorization': `Bearer ${token}` }
                }
            );
            const tdsData = await tdsResponse.json();
            setSelectedTdsRates(tdsData.filter(rate => rate.isSelected));

        } catch (error) {
            console.error('Error fetching selected rates:', error);
        }
    };

    return (
        <div className="selected-rates-summary">
            <h2>Your Selected Tax Rates</h2>
            
            <div className="gst-summary">
                <h3>Selected GST Rates ({selectedGstRates.length})</h3>
                {selectedGstRates.map((rate, index) => (
                    <div key={index} className="rate-summary-item">
                        <span className="rate-name">{rate.description}</span>
                        <span className="rate-value">{rate.rate}%</span>
                        <span className="rate-type">{rate.category}</span>
                    </div>
                ))}
            </div>

            <div className="tds-summary">
                <h3>Selected TDS Rates ({selectedTdsRates.length})</h3>
                {selectedTdsRates.map((rate, index) => (
                    <div key={index} className="rate-summary-item">
                        <span className="rate-name">Section {rate.section}</span>
                        <span className="rate-value">{rate.rate}%</span>
                        <span className="rate-type">{rate.description}</span>
                    </div>
                ))}
            </div>
        </div>
    );
};
```

## 🎨 CSS Styling Examples

```css
/* Tax Dashboard Styles */
.tax-dashboard {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif;
}

.gst-management, .tds-management {
    background: #f8f9fa;
    border-radius: 8px;
    padding: 24px;
    margin: 20px 0;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.rate-item {
    display: flex;
    align-items: center;
    padding: 12px;
    border: 1px solid #e9ecef;
    border-radius: 6px;
    margin: 8px 0;
    background: white;
}

.rate-item:hover {
    background: #f1f3f4;
}

.rate-item label {
    display: flex;
    align-items: center;
    cursor: pointer;
    width: 100%;
}

.rate-item input[type="checkbox"] {
    margin-right: 12px;
    transform: scale(1.2);
}

.rate-summary-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px;
    background: #e3f2fd;
    border-radius: 6px;
    margin: 8px 0;
}

.rate-name {
    font-weight: 600;
    color: #1565c0;
}

.rate-value {
    font-size: 1.1em;
    font-weight: bold;
    color: #2e7d32;
}

.rate-type {
    font-size: 0.9em;
    color: #666;
    font-style: italic;
}

/* Button Styles */
button {
    background: #1976d2;
    color: white;
    border: none;
    padding: 12px 24px;
    border-radius: 6px;
    cursor: pointer;
    font-size: 14px;
    font-weight: 500;
    transition: background 0.2s;
}

button:hover {
    background: #1565c0;
}

button:disabled {
    background: #ccc;
    cursor: not-allowed;
}
```

## 📊 State Management (Redux Example)

```javascript
// Redux Actions
export const fetchGstDetails = (vendorId, gstNumber, token) => async (dispatch) => {
    dispatch({ type: 'FETCH_GST_DETAILS_START' });
    
    try {
        const response = await fetch(`/vendor/${vendorId}/gst/${gstNumber}/details`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await response.json();
        
        dispatch({ 
            type: 'FETCH_GST_DETAILS_SUCCESS', 
            payload: data 
        });
    } catch (error) {
        dispatch({ 
            type: 'FETCH_GST_DETAILS_ERROR', 
            payload: error.message 
        });
    }
};

// Redux Reducer
const taxReducer = (state = initialState, action) => {
    switch (action.type) {
        case 'FETCH_GST_DETAILS_START':
            return { ...state, loading: true };
        case 'FETCH_GST_DETAILS_SUCCESS':
            return { ...state, loading: false, gstDetails: action.payload };
        case 'FETCH_GST_DETAILS_ERROR':
            return { ...state, loading: false, error: action.payload };
        default:
            return state;
    }
};
```

## 🚀 Integration Checklist

### ✅ **Backend Ready**
- [x] Authentication endpoints working
- [x] Quicko API integration complete  
- [x] Tax selection endpoints functional
- [x] Database operations verified
- [x] Error handling implemented

### 🎯 **Frontend Tasks**
- [ ] Set up authentication flow
- [ ] Create vendor dashboard components
- [ ] Implement GST rate selection UI
- [ ] Add TDS rate management
- [ ] Build selected rates summary
- [ ] Add loading states and error handling
- [ ] Style components for better UX

### 🔗 **Integration Points**
1. **Authentication**: Use JWT tokens for all API calls
2. **Error Handling**: Handle 403, 404, 500 errors gracefully
3. **Loading States**: Show spinners during API calls
4. **Real-time Updates**: Refresh data after selections
5. **Responsive Design**: Ensure mobile compatibility

## 📱 Mobile Considerations

```javascript
// Responsive design for mobile
const useIsMobile = () => {
    const [isMobile, setIsMobile] = useState(window.innerWidth < 768);
    
    useEffect(() => {
        const handleResize = () => setIsMobile(window.innerWidth < 768);
        window.addEventListener('resize', handleResize);
        return () => window.removeEventListener('resize', handleResize);
    }, []);
    
    return isMobile;
};

// Use in components
const VendorTaxDashboard = () => {
    const isMobile = useIsMobile();
    
    return (
        <div className={`tax-dashboard ${isMobile ? 'mobile' : 'desktop'}`}>
            {/* Responsive layout */}
        </div>
    );
};
```

## 🎯 **Next Steps**

1. **Start with Authentication**: Implement login/OTP flow
2. **Build Dashboard Layout**: Create main container component
3. **Add GST Management**: Implement rate fetching and selection
4. **Include TDS Features**: Add TDS rate management
5. **Test Integration**: Verify all API calls work
6. **Polish UI/UX**: Add styling and animations
7. **Deploy Frontend**: Connect to your backend API

The backend is fully ready and tested - you just need to build the frontend components using these endpoints! 🚀
