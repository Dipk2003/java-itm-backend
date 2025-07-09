package com.itech.itech_backend.repository;

import com.itech.itech_backend.model.Vendors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorsRepository extends JpaRepository<Vendors, Long> {
    Optional<Vendors> findByEmail(String email);
    Optional<Vendors> findByPhone(String phone);
    Optional<Vendors> findByEmailOrPhone(String email, String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Optional<Vendors> findByGstNumber(String gstNumber);
    Optional<Vendors> findByPanNumber(String panNumber);
}
