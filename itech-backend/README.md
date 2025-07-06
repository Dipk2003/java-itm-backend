# iTech Backend API

## Quick Start
```bash
# Setup database
CREATE DATABASE itech_db;

# Run application
./mvnw spring-boot:run

# Server starts on: http://localhost:8080
```

## Features
- **User Authentication** (JWT)
- **Vendor Management** (Premium tiers: Diamond/Platinum/Gold/Basic)
- **Product Catalog** (Categories & Stock)
- **Lead Tracking** (Priority & Status)
- **Smart Chatbot** (AI vendor recommendations)

## API Test Cases

### 1. Authentication Tests

#### Test Case 1.1: User Registration
```bash
POST http://localhost:8080/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "9876543210"
}
```
**Expected Result**: `200 OK` with OTP sent message

#### Test Case 1.2: OTP Verification
```bash
POST http://localhost:8080/auth/verify-otp
Content-Type: application/json

{
  "phone": "9876543210",
  "otp": "123456"
}
```
**Expected Result**: `200 OK` with JWT token

#### Test Case 1.3: Login
```bash
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "phone": "9876543210"
}
```
**Expected Result**: `200 OK` with new OTP sent

### 2. Product Management Tests

#### Test Case 2.1: Get All Products
```bash
GET http://localhost:8080/products
```
**Expected Result**: `200 OK` with product list

#### Test Case 2.2: Create Product (Authenticated)
```bash
POST http://localhost:8080/products
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "name": "iPhone 15",
  "description": "Latest Apple smartphone",
  "price": 999.99,
  "categoryId": 1,
  "stock": 50
}
```
**Expected Result**: `201 Created` with product details

### 3. Category Tests

#### Test Case 3.1: Get All Categories
```bash
GET http://localhost:8080/categories
```
**Expected Result**: `200 OK` with category list

#### Test Case 3.2: Create Category (Admin)
```bash
POST http://localhost:8080/categories
Authorization: Bearer <ADMIN_JWT_TOKEN>
Content-Type: application/json

{
  "name": "Electronics"
}
```
**Expected Result**: `201 Created` with category details

### 4. Lead Management Tests

#### Test Case 4.1: Create Lead
```bash
POST http://localhost:8080/leads
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "customerName": "Jane Smith",
  "customerEmail": "jane@example.com",
  "customerPhone": "9876543211",
  "productInterest": "Laptops",
  "priority": "HIGH",
  "notes": "Looking for gaming laptop"
}
```
**Expected Result**: `201 Created` with lead ID

#### Test Case 4.2: Get Vendor Leads
```bash
GET http://localhost:8080/leads/vendor
Authorization: Bearer <VENDOR_JWT_TOKEN>
```
**Expected Result**: `200 OK` with vendor's leads

### 5. Contact Message Tests

#### Test Case 5.1: Send Contact Message
```bash
POST http://localhost:8080/contact/message
Content-Type: application/json

{
  "name": "Customer Name",
  "email": "customer@example.com",
  "phone": "9876543212",
  "subject": "Product Inquiry",
  "message": "I need more information about your services"
}
```
**Expected Result**: `200 OK` with confirmation message

### 6. Chatbot Tests

#### Test Case 6.1: Start Chat Session
```bash
POST http://localhost:8080/api/chatbot/start-session
```
**Expected Result**: 
```json
{
  "response": "Hello! 👋 Welcome to iTech! I'm here to help you find the best vendors and products on our platform...",
  "sessionId": "uuid-generated",
  "hasRecommendations": false
}
```

#### Test Case 6.2: Product Query
```bash
POST http://localhost:8080/api/chatbot/chat
Content-Type: application/json

{
  "message": "I need electronics products",
  "sessionId": "session-id-from-6.1"
}
```
**Expected Result**: 
```json
{
  "response": "Here are the top recommended vendors for your product query...",
  "sessionId": "same-session-id",
  "hasRecommendations": true,
  "recommendations": [
    {
      "vendorId": 1,
      "vendorName": "TechCorp",
      "vendorType": "DIAMOND",
      "vendorEmail": "contact@techcorp.com",
      "vendorPhone": "+1234567890",
      "performanceScore": 95.5,
      "products": ["iPhone", "MacBook"],
      "categories": ["Electronics"],
      "reason": "Premium DIAMOND vendor with 2 products"
    }
  ]
}
```

#### Test Case 6.3: Service Query
```bash
POST http://localhost:8080/api/chatbot/chat
Content-Type: application/json

{
  "message": "Who provides web development services?",
  "sessionId": "session-id"
}
```
**Expected Result**: Service providers list with premium vendors first

#### Test Case 6.4: Chat History
```bash
GET http://localhost:8080/api/chatbot/history/{sessionId}
```
**Expected Result**: `200 OK` with conversation history

### 7. Admin Tests

#### Test Case 7.1: Vendor Dashboard
```bash
GET http://localhost:8080/vendor/dashboard
Authorization: Bearer <VENDOR_JWT_TOKEN>
```
**Expected Result**: Dashboard with leads, products, performance data

#### Test Case 7.2: Admin Analytics
```bash
GET http://localhost:8080/admin/users
Authorization: Bearer <ADMIN_JWT_TOKEN>
```
**Expected Result**: User management data

#### Test Case 7.3: Chatbot Analytics (Admin)
```bash
GET http://localhost:8080/admin/chatbot/analytics
Authorization: Bearer <ADMIN_JWT_TOKEN>
```
**Expected Result**: 
```json
{
  "totalMessages": 150,
  "messagesLast24Hours": 25,
  "messagesLastWeek": 89,
  "uniqueSessionsLastWeek": 42
}
```

### 8. Error Test Cases

#### Test Case 8.1: Unauthorized Access
```bash
GET http://localhost:8080/vendor/dashboard
# No Authorization header
```
**Expected Result**: `401 Unauthorized`

#### Test Case 8.2: Invalid Token
```bash
GET http://localhost:8080/vendor/dashboard
Authorization: Bearer invalid_token
```
**Expected Result**: `401 Unauthorized`

#### Test Case 8.3: Admin Endpoint Access (Non-Admin)
```bash
GET http://localhost:8080/admin/users
Authorization: Bearer <VENDOR_JWT_TOKEN>
```
**Expected Result**: `403 Forbidden`

## Health Check
```bash
GET http://localhost:8080/api/chatbot/health
```
**Expected Result**: `"Chatbot service is running"`

## Database Verification

### Check Tables Created
```sql
USE itech_db;
SHOW TABLES;
```
**Expected Tables**: 
- `user`
- `category` 
- `product`
- `product_image`
- `lead`
- `contact_message`
- `vendor_ranking`
- `vendor_tax_profile`
- `chatbot_message`
- `otp_verification`

### Sample Data Queries
```sql
-- Check users
SELECT id, name, email, vendor_type FROM user;

-- Check products with vendors
SELECT p.name, p.price, c.name as category, u.name as vendor 
FROM product p 
JOIN category c ON p.category_id = c.id 
JOIN user u ON p.vendor_id = u.id;

-- Check chatbot conversations
SELECT session_id, user_message, created_at FROM chatbot_message 
ORDER BY created_at DESC LIMIT 10;
```

## Quick Testing Script
```bash
# Test server is running
curl http://localhost:8080/api/chatbot/health

# Test chatbot
curl -X POST http://localhost:8080/api/chatbot/start-session

# Test public endpoints
curl http://localhost:8080/products
curl http://localhost:8080/categories
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Port 8080 in use | Change `server.port` in `application.properties` |
| Database connection failed | Check MySQL is running and credentials |
| JWT token expired | Re-authenticate to get new token |
| Chatbot not responding | Check `/api/chatbot/health` endpoint |

## Tech Stack
- **Backend**: Spring Boot 3.5.3, Java 21
- **Database**: MySQL 8.0
- **Security**: JWT, Spring Security
- **Build**: Maven
