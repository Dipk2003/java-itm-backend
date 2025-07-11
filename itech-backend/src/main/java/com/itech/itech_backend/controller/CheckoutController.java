package com.itech.itech_backend.controller;

import com.itech.itech_backend.dto.CheckoutDto;
import com.itech.itech_backend.dto.CartDto;
import com.itech.itech_backend.model.Order;
import com.itech.itech_backend.service.CartService;
import com.itech.itech_backend.service.OrderService;
import com.itech.itech_backend.service.PaymentService;
import com.itech.itech_backend.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/checkout")
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * Get checkout summary - cart items and totals
     */
    @GetMapping("/summary")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getCheckoutSummary(HttpServletRequest request) {
        try {
            Long userId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.badRequest().body("Invalid user session");
            }

            CartDto cart = cartService.getUserCart(userId);
            
            if (cart.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body("Cart is empty");
            }

            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            log.error("Error getting checkout summary", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Process checkout - create order and initiate payment
     */
    @PostMapping("/process")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> processCheckout(@RequestBody CheckoutDto checkoutDto, HttpServletRequest request) {
        try {
            Long userId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.badRequest().body("Invalid user session");
            }

            // Validate cart is not empty
            CartDto cart = cartService.getUserCart(userId);
            if (cart.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body("Cart is empty");
            }

            // Create order
            Map<String, Object> orderResponse = orderService.createOrder(userId, checkoutDto);
            
            log.info("Checkout processed successfully for user: {}", userId);
            return ResponseEntity.ok(orderResponse);
            
        } catch (IllegalArgumentException e) {
            log.error("Checkout validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error processing checkout", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Verify payment and complete order
     */
    @PostMapping("/verify-payment")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> paymentData, HttpServletRequest request) {
        try {
            Long userId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.badRequest().body("Invalid user session");
            }

            String razorpayOrderId = paymentData.get("razorpay_order_id");
            String razorpayPaymentId = paymentData.get("razorpay_payment_id");
            String signature = paymentData.get("razorpay_signature");

            if (razorpayOrderId == null || razorpayPaymentId == null || signature == null) {
                return ResponseEntity.badRequest().body("Missing payment verification data");
            }

            boolean verified = orderService.verifyAndCompletePayment(razorpayOrderId, razorpayPaymentId, signature);

            if (verified) {
                // Clear user cart after successful payment
                cartService.clearCart(userId);
                
                return ResponseEntity.ok(Map.of(
                    "status", "success", 
                    "message", "Payment verified and order completed successfully"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "failed", 
                    "message", "Payment verification failed"
                ));
            }
        } catch (Exception e) {
            log.error("Error verifying payment", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Get order confirmation details
     */
    @GetMapping("/confirmation/{orderNumber}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getOrderConfirmation(@PathVariable String orderNumber, HttpServletRequest request) {
        try {
            Long userId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.badRequest().body("Invalid user session");
            }

            Order order = orderService.getOrderByNumber(orderNumber);
            
            // Verify order belongs to current user
            if (!order.getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }

            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Error getting order confirmation", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Cancel order (if payment pending)
     */
    @PostMapping("/cancel/{orderNumber}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> cancelOrder(@PathVariable String orderNumber, HttpServletRequest request) {
        try {
            Long userId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.badRequest().body("Invalid user session");
            }

            Order order = orderService.getOrderByNumber(orderNumber);
            
            // Verify order belongs to current user
            if (!order.getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Only allow cancellation for pending orders
            if (order.getStatus() != Order.OrderStatus.PENDING || 
                order.getPaymentStatus() != Order.PaymentStatus.PENDING) {
                return ResponseEntity.badRequest().body("Order cannot be cancelled");
            }

            Order cancelledOrder = orderService.updateOrderStatus(order.getId(), Order.OrderStatus.CANCELLED);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Order cancelled successfully",
                "order", cancelledOrder
            ));
            
        } catch (Exception e) {
            log.error("Error cancelling order", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
