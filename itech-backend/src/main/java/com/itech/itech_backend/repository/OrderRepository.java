package com.itech.itech_backend.repository;

import com.itech.itech_backend.model.Order;
import com.itech.itech_backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    List<Order> findByUser(User user);
    
    List<Order> findByUserId(Long userId);
    
    Page<Order> findByUserId(Long userId, Pageable pageable);
    
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    List<Order> findByStatus(Order.OrderStatus status);
    
    List<Order> findByPaymentStatus(Order.PaymentStatus paymentStatus);
    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = :status")
    List<Order> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Order.OrderStatus status);
    
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT SUM(o.grandTotal) FROM Order o WHERE o.user.id = :userId AND o.paymentStatus = 'PAID'")
    Double getTotalAmountByUserId(@Param("userId") Long userId);
    
    // Vendor specific queries
    @Query("SELECT DISTINCT o FROM Order o JOIN o.items oi WHERE oi.vendor.id = :vendorId")
    List<Order> findOrdersByVendorId(@Param("vendorId") Long vendorId);
    
    @Query("SELECT DISTINCT o FROM Order o JOIN o.items oi WHERE oi.vendor.id = :vendorId ORDER BY o.createdAt DESC")
    Page<Order> findOrdersByVendorId(@Param("vendorId") Long vendorId, Pageable pageable);
}
