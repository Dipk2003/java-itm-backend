-- Create Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20) UNIQUE,
    password VARCHAR(255) NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    role VARCHAR(50) DEFAULT 'ROLE_USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create Vendors table
CREATE TABLE IF NOT EXISTS vendors (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20) UNIQUE,
    password VARCHAR(255) NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    role VARCHAR(50) DEFAULT 'ROLE_VENDOR',
    vendor_type VARCHAR(50) DEFAULT 'BASIC',
    business_name VARCHAR(255),
    business_address TEXT,
    gst_number VARCHAR(15),
    pan_number VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create Admins table
CREATE TABLE IF NOT EXISTS admins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20) UNIQUE,
    password VARCHAR(255) NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    role VARCHAR(50) DEFAULT 'ROLE_ADMIN',
    department VARCHAR(255),
    designation VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_role ON users(role);

CREATE INDEX idx_vendors_email ON vendors(email);
CREATE INDEX idx_vendors_phone ON vendors(phone);
CREATE INDEX idx_vendors_role ON vendors(role);
CREATE INDEX idx_vendors_gst ON vendors(gst_number);
CREATE INDEX idx_vendors_pan ON vendors(pan_number);
CREATE INDEX idx_vendors_type ON vendors(vendor_type);

CREATE INDEX idx_admins_email ON admins(email);
CREATE INDEX idx_admins_phone ON admins(phone);
CREATE INDEX idx_admins_role ON admins(role);
CREATE INDEX idx_admins_department ON admins(department);
CREATE INDEX idx_admins_active ON admins(is_active);

-- Insert some sample data for testing
INSERT INTO users (name, email, phone, password, is_verified) VALUES 
('John Doe', 'john@example.com', '9876543210', 'password123', TRUE),
('Jane Smith', 'jane@example.com', '9876543211', 'password123', TRUE);

INSERT INTO vendors (name, email, phone, password, is_verified, business_name, gst_number, pan_number) VALUES 
('ABC Electronics', 'vendor@abc.com', '9876543212', 'password123', TRUE, 'ABC Electronics Pvt Ltd', '27ABCDE1234F1Z5', 'ABCDE1234F'),
('XYZ Traders', 'vendor@xyz.com', '9876543213', 'password123', TRUE, 'XYZ Traders', '27XYZDE1234F1Z5', 'XYZDE1234F');

INSERT INTO admins (name, email, phone, password, is_verified, department, designation) VALUES 
('Admin User', 'admin@example.com', '9876543214', 'password123', TRUE, 'IT', 'System Administrator'),
('Super Admin', 'superadmin@example.com', '9876543215', 'password123', TRUE, 'Management', 'Super Administrator');

-- Show tables and their structure
SHOW TABLES;
DESCRIBE users;
DESCRIBE vendors;
DESCRIBE admins;
