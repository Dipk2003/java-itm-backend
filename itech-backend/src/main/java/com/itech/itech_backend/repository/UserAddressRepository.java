package com.itech.itech_backend.repository;

import com.itech.itech_backend.model.User;
import com.itech.itech_backend.model.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    
    List<UserAddress> findByUser(User user);
    
    List<UserAddress> findByUserId(Long userId);
    
    List<UserAddress> findByUserIdOrderByIsDefaultDesc(Long userId);
    
    Optional<UserAddress> findByUserIdAndIsDefaultTrue(Long userId);
    
    Optional<UserAddress> findByUserIdAndId(Long userId, Long addressId);
    
    @Query("SELECT ua FROM UserAddress ua WHERE ua.user.id = :userId AND ua.addressType = :addressType")
    List<UserAddress> findByUserIdAndAddressType(@Param("userId") Long userId, @Param("addressType") String addressType);
    
    void deleteByUserIdAndId(Long userId, Long addressId);
    
    int countByUserId(Long userId);
}
