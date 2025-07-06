package com.itech.itech_backend.repository;

import com.itech.itech_backend.model.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

    @Query("SELECT o FROM OtpVerification o WHERE o.emailOrPhone = :emailOrPhone")
    Optional<OtpVerification> findByEmailOrPhone(@Param("emailOrPhone") String emailOrPhone);

    @Modifying
    @Transactional
    @Query("DELETE FROM OtpVerification o WHERE o.emailOrPhone = :emailOrPhone")
    void deleteByEmailOrPhone(@Param("emailOrPhone") String emailOrPhone);
}
