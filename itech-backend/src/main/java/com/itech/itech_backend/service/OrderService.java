package com.itech.itech_backend.service;

import com.itech.itech_backend.dto.CheckoutDto;
import com.itech.itech_backend.model.*;
import com.itech.itech_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final ProductRepository productRepository;
    private final PaymentService paymentService;

    public Map<String, Object> createOrder(Long userId, CheckoutDto checkoutDto) {
        log.info("Creating order for user: {}", userId);
        
        // Get user and cart
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        
        // Validate stock and calculate amounts
        validateCartItems(cart);
        double totalAmount = cart.getTotalAmount();
        double shippingAmount = calculateShipping(cart);
        double taxAmount = calculateTax(totalAmount);
        double grandTotal = totalAmount + shippingAmount + taxAmount;
        
        // Get shipping address
        UserAddress shippingAddress = getShippingAddress(userId, checkoutDto);
        
        // Generate order number
        String orderNumber = generateOrderNumber();
        
        // Create order
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .status(Order.OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .shippingAmount(shippingAmount)
                .taxAmount(taxAmount)
                .grandTotal(grandTotal)
                .shippingName(shippingAddress.getFullName())
                .shippingAddress(shippingAddress.getAddressLine1() + 
                    (shippingAddress.getAddressLine2() != null ? ", " + shippingAddress.getAddressLine2() : ""))
                .shippingCity(shippingAddress.getCity())
                .shippingState(shippingAddress.getState())
                .shippingPincode(shippingAddress.getPincode())
                .shippingPhone(shippingAddress.getPhone())
                .paymentStatus(Order.PaymentStatus.PENDING)
                .paymentMethod(checkoutDto.getPaymentMethod())
                .build();
        
        order = orderRepository.save(order);
        
        // Create order items and update product stock
        createOrderItems(order, cart);
        
        // Create payment order if not COD
        Map<String, Object> response;
        if (!"COD".equals(checkoutDto.getPaymentMethod())) {
            response = paymentService.createOrder(
                orderNumber,
                BigDecimal.valueOf(grandTotal),
                user.getEmail(),
                user.getPhone()
            );
            
            // Update order with Razorpay order ID
            order.setRazorpayOrderId((String) response.get("razorpayOrderId"));
            orderRepository.save(order);
        } else {
            // For COD, mark as confirmed
            order.setStatus(Order.OrderStatus.CONFIRMED);
            order.setPaymentStatus(Order.PaymentStatus.PENDING);
            orderRepository.save(order);
            
            response = Map.of(
                "orderId", order.getId(),
                "orderNumber", orderNumber,
                "amount", grandTotal,
                "paymentMethod", "COD"
            );
        }
        
        // Clear cart after successful order creation
        cartItemRepository.deleteByCartId(cart.getId());
        
        response.put("orderId", order.getId());
        response.put("orderNumber", orderNumber);
        
        log.info("Order created successfully: {}", orderNumber);
        return response;
    }

    public boolean verifyAndCompletePayment(String razorpayOrderId, String razorpayPaymentId, String signature) {
        // Verify payment with Razorpay
        boolean isValid = paymentService.verifyPayment(razorpayOrderId, razorpayPaymentId, signature);
        
        if (isValid) {
            // Find order by Razorpay order ID
            Order order = orderRepository.findAll().stream()
                    .filter(o -> razorpayOrderId.equals(o.getRazorpayOrderId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            
            // Update order status
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            order.setStatus(Order.OrderStatus.CONFIRMED);
            order.setRazorpayPaymentId(razorpayPaymentId);
            order.setPaymentTransactionId(razorpayPaymentId);
            
            orderRepository.save(order);
            
            log.info("Payment verified and order confirmed: {}", order.getOrderNumber());
            return true;
        }
        
        return false;
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Page<Order> getUserOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public Order getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = getOrderById(orderId);
        order.setStatus(status);
        
        if (status == Order.OrderStatus.SHIPPED) {
            order.setShippedAt(LocalDateTime.now());
        } else if (status == Order.OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        }
        
        return orderRepository.save(order);
    }

    // Vendor specific methods
    public List<Order> getVendorOrders(Long vendorId) {
        return orderRepository.findOrdersByVendorId(vendorId);
    }

    public Page<Order> getVendorOrders(Long vendorId, Pageable pageable) {
        return orderRepository.findOrdersByVendorId(vendorId, pageable);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    private void validateCartItems(Cart cart) {
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (!product.canOrder(item.getQuantity())) {
                throw new RuntimeException("Product " + product.getName() + " is not available in required quantity");
            }
        }
    }

    private double calculateShipping(Cart cart) {
        // Simple shipping calculation - can be made more complex
        double shippingAmount = 0.0;
        
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (!product.isFreeShipping()) {
                double productShipping = product.getShippingCharge() != null ? product.getShippingCharge() : 50.0;
                shippingAmount += productShipping * item.getQuantity();
            }
        }
        
        // Free shipping for orders above ₹500
        if (cart.getTotalAmount() > 500) {
            shippingAmount = 0.0;
        }
        
        return shippingAmount;
    }

    private double calculateTax(double amount) {
        // Simple GST calculation - 18%
        return amount * 0.18;
    }

    private UserAddress getShippingAddress(Long userId, CheckoutDto checkoutDto) {
        if (checkoutDto.getAddressId() != null) {
            return userAddressRepository.findByUserIdAndId(userId, checkoutDto.getAddressId())
                    .orElseThrow(() -> new RuntimeException("Address not found"));
        } else if (checkoutDto.getShippingAddress() != null) {
            // Create temporary address object
            CheckoutDto.AddressDto addr = checkoutDto.getShippingAddress();
            User user = userRepository.findById(userId).orElseThrow();
            
            return UserAddress.builder()
                    .user(user)
                    .fullName(addr.getFullName())
                    .addressLine1(addr.getAddressLine1())
                    .addressLine2(addr.getAddressLine2())
                    .city(addr.getCity())
                    .state(addr.getState())
                    .pincode(addr.getPincode())
                    .phone(addr.getPhone())
                    .addressType("TEMP")
                    .build();
        } else {
            throw new RuntimeException("Shipping address is required");
        }
    }

    private String generateOrderNumber() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private void createOrderItems(Order order, Cart cart) {
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            
            // Create order item
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .vendor(product.getVendor())
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getPrice())
                    .totalPrice(cartItem.getSubtotal())
                    .productName(product.getName())
                    .productDescription(product.getDescription())
                    .build();
            
            order.getItems().add(orderItem);
            
            // Update product stock and order count
            product.setStock(product.getStock() - cartItem.getQuantity());
            product.setOrderCount(product.getOrderCount() + cartItem.getQuantity());
            productRepository.save(product);
        }
    }
}
