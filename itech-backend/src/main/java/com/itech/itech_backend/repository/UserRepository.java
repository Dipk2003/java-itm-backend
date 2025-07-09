package com.itech.itech_backend.repository;

import com.itech.itech_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmailOrPhone(String email, String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    
    // Role-based queries
    List<User> findByRole(String role);
    long countByRole(String role);
    
    // Verification status queries
    List<User> findByVerifiedTrue();
    List<User> findByVerifiedFalse();
    
    // Active status queries
    List<User> findByIsActiveTrue();
    List<User> findByIsActiveFalse();
    
    // GST and PAN queries for vendors
    Optional<User> findByGstNumber(String gstNumber);
    Optional<User> findByPanNumber(String panNumber);
    
    // Department queries for admins
    List<User> findByDepartment(String department);
    
    // Combined queries
    List<User> findByRoleAndVerifiedTrue(String role);
    List<User> findByRoleAndIsActiveTrue(String role);
}
