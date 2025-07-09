# Separated User Authentication System

This document describes the implementation of a separated user authentication system with three distinct user types: Users, Vendors, and Admins.

## Overview

The system has been updated to use three separate tables instead of a single user table:
- `users` - For regular users with role `ROLE_USER`
- `vendors` - For vendors with role `ROLE_VENDOR`
- `admins` - For administrators with role `ROLE_ADMIN`

## Database Schema

### Users Table
- `id` (BIGINT, Primary Key, Auto Increment)
- `name` (VARCHAR(255), NOT NULL)
- `email` (VARCHAR(255), UNIQUE, NOT NULL)
- `phone` (VARCHAR(20), UNIQUE)
- `password` (VARCHAR(255), NOT NULL)
- `is_verified` (BOOLEAN, DEFAULT FALSE)
- `role` (VARCHAR(50), DEFAULT 'ROLE_USER')
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### Vendors Table
- `id` (BIGINT, Primary Key, Auto Increment)
- `name` (VARCHAR(255), NOT NULL)
- `email` (VARCHAR(255), UNIQUE, NOT NULL)
- `phone` (VARCHAR(20), UNIQUE)
- `password` (VARCHAR(255), NOT NULL)
- `is_verified` (BOOLEAN, DEFAULT FALSE)
- `role` (VARCHAR(50), DEFAULT 'ROLE_VENDOR')
- `vendor_type` (VARCHAR(50), DEFAULT 'BASIC')
- `business_name` (VARCHAR(255))
- `business_address` (TEXT)
- `gst_number` (VARCHAR(15))
- `pan_number` (VARCHAR(10))
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### Admins Table
- `id` (BIGINT, Primary Key, Auto Increment)
- `name` (VARCHAR(255), NOT NULL)
- `email` (VARCHAR(255), UNIQUE, NOT NULL)
- `phone` (VARCHAR(20), UNIQUE)
- `password` (VARCHAR(255), NOT NULL)
- `is_verified` (BOOLEAN, DEFAULT FALSE)
- `role` (VARCHAR(50), DEFAULT 'ROLE_ADMIN')
- `department` (VARCHAR(255))
- `designation` (VARCHAR(255))
- `is_active` (BOOLEAN, DEFAULT TRUE)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

## Backend Implementation

### New Repositories
- `UsersRepository` - For users table operations
- `VendorsRepository` - For vendors table operations
- `AdminsRepository` - For admins table operations

### New Services
- `UsersService` - Business logic for users
- `VendorsService` - Business logic for vendors
- `AdminsService` - Business logic for admins
- `UnifiedAuthService` - Unified authentication service

### New Controllers
- `UsersController` - REST endpoints for users management
- `VendorsController` - REST endpoints for vendors management
- `AdminsController` - REST endpoints for admins management (to be created)

### Authentication Endpoints

#### Registration
- `POST /auth/register` - User registration
- `POST /auth/vendor/register` - Vendor registration
- `POST /auth/admin/register` - Admin registration

#### Authentication
- `POST /auth/login` - Unified login for all user types
- `POST /auth/verify` - OTP verification
- `POST /auth/verify-otp` - Alternative OTP verification

#### User Management
- `GET /api/users` - Get all users (Admin only)
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user (Admin only)

#### Vendor Management
- `GET /api/vendors` - Get all vendors (Admin only)
- `GET /api/vendors/{id}` - Get vendor by ID
- `GET /api/vendors/gst/{gst}` - Get vendor by GST number
- `GET /api/vendors/pan/{pan}` - Get vendor by PAN number
- `PUT /api/vendors/{id}` - Update vendor
- `DELETE /api/vendors/{id}` - Delete vendor (Admin only)

#### Admin Management
- `GET /api/admins` - Get all admins (Admin only)
- `GET /api/admins/{id}` - Get admin by ID
- `PUT /api/admins/{id}` - Update admin
- `DELETE /api/admins/{id}` - Delete admin (Admin only)
- `PATCH /api/admins/{id}/activate` - Activate admin
- `PATCH /api/admins/{id}/deactivate` - Deactivate admin

## Frontend Implementation

### Updated Components
- `UnifiedAuth.jsx` - New unified authentication component
- `UnifiedAuth.css` - Styles for the unified auth component

### Updated Services
- `api.js` - Updated with new endpoints for separated user management
- `AuthContext.js` - Updated to support all three user types

### Features
- Tab-based user type selection (User/Vendor/Admin)
- Dynamic form fields based on user type
- Vendor-specific fields (Business Name, GST, PAN)
- Admin-specific fields (Department, Designation)
- Admin access code requirement
- Unified OTP verification

## Configuration

### Database Setup
1. Run the SQL script: `create_separated_tables.sql`
2. This will create the three tables with proper indexes and sample data

### Backend Configuration
1. Ensure all new classes are properly imported
2. Update `application.properties` if needed
3. Start the Spring Boot application

### Frontend Configuration
1. Update API endpoints in `api.js`
2. Use the new `UnifiedAuth` component
3. Update routing if needed

## Testing

### Sample Data
The SQL script includes sample data for testing:
- Users: john@example.com, jane@example.com
- Vendors: vendor@abc.com, vendor@xyz.com
- Admins: admin@example.com, superadmin@example.com

All test accounts use password: `password123`

### Admin Access
Admin login requires the access code: `ADMIN2025`

## Security Features

### Role-Based Access Control
- Users can only access user-specific features
- Vendors can access vendor-specific features
- Admins have full access to all features

### Data Separation
- Each user type has its own table
- No data mixing between user types
- Type-specific fields for each user category

### Authentication Flow
1. User selects user type (User/Vendor/Admin)
2. Fills appropriate form fields
3. System determines correct table to use
4. OTP verification for security
5. JWT token generation with correct role

## Migration from Single User Table

If you have existing data in a single user table, you can migrate using:

```sql
-- Migrate users
INSERT INTO users (name, email, phone, password, is_verified, role, created_at)
SELECT name, email, phone, password, is_verified, role, created_at
FROM user WHERE role = 'ROLE_USER';

-- Migrate vendors
INSERT INTO vendors (name, email, phone, password, is_verified, role, vendor_type, created_at)
SELECT name, email, phone, password, is_verified, role, vendor_type, created_at
FROM user WHERE role = 'ROLE_VENDOR';

-- Migrate admins
INSERT INTO admins (name, email, phone, password, is_verified, role, created_at)
SELECT name, email, phone, password, is_verified, role, created_at
FROM user WHERE role = 'ROLE_ADMIN';
```

## Benefits

1. **Data Organization**: Better data organization with type-specific fields
2. **Performance**: Improved query performance with separate tables
3. **Security**: Better security with role-based access control
4. **Scalability**: Easy to add new user types or modify existing ones
5. **Maintenance**: Easier to maintain and update user-specific features

## Future Enhancements

1. Add more user types as needed
2. Implement user type-specific dashboards
3. Add user type-specific permissions
4. Implement user type-specific workflows
5. Add audit logging for each user type

## Support

For any issues or questions about the separated authentication system, please contact the development team.
