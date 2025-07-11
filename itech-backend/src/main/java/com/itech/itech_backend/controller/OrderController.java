package com.itech.itech_backend.controller;

import com.itech.itech_backend.dto.CheckoutDto;
import com.itech.itech_backend.model.Order;
import com.itech.itech_backend.service.OrderService;
import com.itech.itech_backend.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final JwtTokenUtil jwtTokenUtil;

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> checkout(@RequestBody CheckoutDto checkoutDto, HttpServletRequest request) {
        try {
            Long userId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.badRequest().body("Invalid user session");
            }
            Map<String, Object> response = orderService.createOrder(userId, checkoutDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during checkout", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/verify-payment")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> paymentData) {
        try {
            String razorpayOrderId = paymentData.get("razorpay_order_id");
            String razorpayPaymentId = paymentData.get("razorpay_payment_id");
            String signature = paymentData.get("razorpay_signature");
            
            boolean verified = orderService.verifyAndCompletePayment(razorpayOrderId, razorpayPaymentId, signature);
            
            if (verified) {
                return ResponseEntity.ok(Map.of("status", "success", "message", "Payment verified successfully"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("status", "failed", "message", "Payment verification failed"));
            }
        } catch (Exception e) {
            log.error("Error verifying payment", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Order>> getUserOrders(HttpServletRequest request) {
        try {
            Long userId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.badRequest().build();
            }
            List<Order> orders = orderService.getUserOrders(userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error getting user orders", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my-orders/paginated")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<Order>> getUserOrdersPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        try {
            Long userId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.badRequest().build();
            }
            Pageable pageable = PageRequest.of(page, size);
            Page<Order> orders = orderService.getUserOrders(userId, pageable);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error getting user orders", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId, HttpServletRequest request) {
        try {
            Long userId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.badRequest().build();
            }
            
            Order order = orderService.getOrderById(orderId);
            
            // Check if order belongs to current user
            if (!order.getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Error getting order", e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/number/{orderNumber}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Order> getOrderByNumber(@PathVariable String orderNumber, HttpServletRequest request) {
        try {
            Long userId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.badRequest().build();
            }
            
            Order order = orderService.getOrderByNumber(orderNumber);
            
            // Check if order belongs to current user
            if (!order.getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Error getting order", e);
            return ResponseEntity.notFound().build();
        }
    }

    // Admin endpoints
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam Order.OrderStatus status) {
        try {
            Order order = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Error updating order status", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Vendor endpoints
    @GetMapping("/vendor/my-orders")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<Order>> getVendorOrders(HttpServletRequest request) {
        try {
            Long vendorId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (vendorId == null) {
                return ResponseEntity.badRequest().build();
            }
            List<Order> orders = orderService.getVendorOrders(vendorId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error getting vendor orders", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/vendor/my-orders/paginated")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<Page<Order>> getVendorOrdersPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        try {
            Long vendorId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (vendorId == null) {
                return ResponseEntity.badRequest().build();
            }
            Pageable pageable = PageRequest.of(page, size);
            Page<Order> orders = orderService.getVendorOrders(vendorId, pageable);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error getting vendor orders", e);
            return ResponseEntity.badRequest().build();
        }
    }

}
