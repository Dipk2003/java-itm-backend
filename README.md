# iTech Backend Documentation

## Overview

This backend is designed for a B2B platform using Java Spring Boot. It supports user authentication (via OTP), role-based authorization, vendor tax data (PAN/GST) verification, product and category management, and vendor ranking for lead generation. All APIs are organized into RESTful endpoints with JWT-based security.

---

## 🏗️ Project Structure

```
com.itech.itech_backend
│
├── config/                  # Spring Security configuration
│   └── SecurityConfig.java
│
├── controller/              # API endpoints
│   ├── AuthController.java
│   ├── AdminController.java
│   ├── ProductController.java
│   ├── CategoryController.java
│   └── PanVerificationController.java
│
├── dto/                     # Response/request classes
│   └── AuthResponse.java
│
├── enums/                   # User roles
│   └── Role.java
│
├── filter/                  # JWT filter
│   └── JwtFilter.java
│
├── model/                   # JPA models/entities
│   ├── User.java
│   ├── OtpVerification.java
│   ├── Product.java
│   ├── ProductImage.java
│   ├── Category.java
│   ├── VendorRanking.java
│   ├── VendorTaxProfile.java
│   └── ContactMessage.java
│
├── repository/              # Spring Data JPA repositories
│   ├── UserRepository.java
│   ├── OtpVerificationRepository.java
│   ├── ProductRepository.java
│   ├── CategoryRepository.java
│   ├── VendorRankingRepository.java
│   └── VendorTaxProfileRepository.java
│
├── service/                 # Business logic
│   ├── AuthService.java
│   ├── AdminService.java
│   ├── UserDetailsServiceImpl.java
│   ├── VendorTaxService.java
│   ├── VendorRankingService.java
│   ├── CategoryService.java
│   ├── ProductService.java
│   ├── ContactMessageService.java
│   └── VendorTaxProfileService.java
│
└── util/                    # Utility classes
    └── [TBD]
```

---

## 🔐 Authentication & Authorization

### Features:

* OTP-based login via Email or Phone
* JWT token generation after OTP verification
* Role-based access control: `ADMIN`, `VENDOR`, `VENDOR_PREMIUM`

### Files:

* `AuthController.java`: handles `/auth/register/email`, `/auth/register/phone`, `/auth/verify`
* `AuthService.java`: sends and verifies OTPs, marks user as verified
* `OtpVerification.java`: stores OTPs and expiry times
* `UserDetailsServiceImpl.java`: loads Spring Security UserDetails

---

## 📦 Models Explained

### `User.java`

| Field      | Type    | Description             |
| ---------- | ------- | ----------------------- |
| id         | UUID    | Primary key             |
| name       | String  | Full name               |
| email      | String  | Unique email (nullable) |
| phone      | String  | Unique phone (nullable) |
| role       | String  | Role: ADMIN/VENDOR/...  |
| isVerified | boolean | OTP verification status |

### `OtpVerification.java`

| Field        | Type          | Description     |
| ------------ | ------------- | --------------- |
| id           | UUID          | Primary key     |
| emailOrPhone | String        | Identifier      |
| otp          | String        | 6-digit OTP     |
| expiryTime   | LocalDateTime | Expiry (10 min) |

### `VendorRanking.java`

| Field           | Type   | Description               |
| --------------- | ------ | ------------------------- |
| vendor          | User   | Vendor user               |
| totalLeads      | int    | Leads assigned            |
| leadsConverted  | int    | Successfully closed leads |
| rating          | double | Vendor’s rating           |
| avgResponseTime | double | Time to respond in hours  |
| rankScore       | double | Calculated rank score     |

### `Product.java`

* Represents a listed product by a vendor

### `ProductImage.java`

* Images associated with each product

### `Category.java`

* Product category (e.g. Electronics, Furniture)

### `VendorTaxProfile.java`

* Stores PAN, GST, TDS fetched via Quicko API

### `ContactMessage.java`

* Leads submitted by users via chatbot/contact form

---

## 📊 Vendor Ranking Logic

Defined in `VendorRankingService.java`:

```
Score = 0.5 × leadsConverted + 0.3 × rating − 0.2 × responseTime
```

Used to sort and promote `VENDOR_PREMIUM` leads.

---

## 🧾 PAN/GST Integration

### Quicko API (Planned/Optional)

* `VendorTaxService.java`: integrates with Quicko or similar to verify PAN and fetch GST, TDS, etc.
* `PanVerificationController.java`: receives vendor PAN, sends API request, stores response.
* `VendorTaxProfile.java`: stores structured PAN+GST+TDS results

---

## 🔄 Product & Category Modules

* `ProductController`, `CategoryController`
* CRUD via `ProductService`, `CategoryService`
* Images managed via `ProductImage.java`

---

## ✉️ Contact Message

* `ContactMessageService` handles incoming form/chat leads
* `ContactMessage.java` stores them for admin review

---

## ✅ Completed Features

* JWT secured backend
* OTP-based login
* Admin + vendor role logic
* Vendor ranking scoring
* PAN API integration logic

---

## 🛠️ To Be Done

* Aadhaar Verification API
* Vendor dashboard API
* Frontend integration & JWT token exchange
* Chatbot for auto lead capture

---
*developed by Dipanshu kumar pandey
