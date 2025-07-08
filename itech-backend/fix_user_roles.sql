-- Fix existing users' roles to ROLE_VENDOR
-- This script will update all users who don't have ROLE_VENDOR to have ROLE_VENDOR

UPDATE user 
SET role = 'ROLE_VENDOR' 
WHERE role IS NULL OR role = '' OR role = 'ROLE_USER';

-- Check the results
SELECT id, name, email, role, vendor_type, is_verified 
FROM user 
ORDER BY id;
