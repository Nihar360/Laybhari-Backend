package com.laybhari.service;

import com.laybhari.dto.CheckoutRequest;
import com.laybhari.dto.CreatePaymentResponse;
import com.laybhari.dto.OrderDto;
import com.laybhari.dto.OrderItemDto;
import com.laybhari.dto.VerifyPaymentRequest;
import com.laybhari.entity.*;
import com.laybhari.repository.*;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final AddressService addressService;
    private final EmailService emailService;
    private final WhatsAppService whatsAppService;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("499.00");
    private static final BigDecimal FLAT_SHIPPING_FEE = new BigDecimal("50.00");

    public OrderService(OrderRepository orderRepository,
                        AddressRepository addressRepository,
                        CartRepository cartRepository,
                        CartItemRepository cartItemRepository,
                        UserRepository userRepository,
                        AddressService addressService,
                        EmailService emailService,
                        WhatsAppService whatsAppService) {
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.addressService = addressService;
        this.emailService = emailService;
        this.whatsAppService = whatsAppService;
    }

    private User getUserByEmail(String identifier) {
        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> new IllegalArgumentException("User not found for: " + identifier));
    }

    @Transactional
    public OrderDto checkout(String userEmail, CheckoutRequest request) {
        if (request == null || request.getAddressId() == null) {
            throw new IllegalArgumentException("addressId is required for checkout");
        }

        User user = getUserByEmail(userEmail);

        Address address = addressRepository.findByIdAndUser(request.getAddressId(), user)
                .orElseThrow(() -> new IllegalArgumentException("Address not found or does not belong to user. ID: " + request.getAddressId()));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty. Cannot place an empty order.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setStatus("PLACED");
        order.setPaymentStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            ProductVariant variant = cartItem.getProductVariant();
            if (variant == null) {
                continue;
            }
            Product product = variant.getProduct();

            int requestedQty = cartItem.getQuantity();
            int currentStock = variant.getStock() != null ? variant.getStock() : 0;

            if (currentStock < requestedQty) {
                String productName = product != null ? product.getName() : "Product";
                throw new IllegalArgumentException("Insufficient stock for " + productName +
                        " (" + variant.getWeightLabel() + "). Available: " + currentStock + ", Requested: " + requestedQty);
            }

            // Deduct requested quantity from product variant stock
            variant.setStock(currentStock - requestedQty);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductVariant(variant);
            item.setProductName(product != null ? product.getName() : "Unknown Product");
            item.setWeightLabel(variant.getWeightLabel());
            item.setPrice(variant.getPrice());
            item.setQuantity(requestedQty);

            BigDecimal lineTotal = variant.getPrice().multiply(BigDecimal.valueOf(requestedQty));
            item.setLineTotal(lineTotal);

            subtotal = subtotal.add(lineTotal);
            orderItems.add(item);
        }

        if (orderItems.isEmpty()) {
            throw new IllegalArgumentException("Cart contains no valid items to checkout.");
        }

        order.setItems(orderItems);
        order.setSubtotal(subtotal);

        BigDecimal shippingFee = (subtotal.compareTo(FREE_SHIPPING_THRESHOLD) < 0) ? FLAT_SHIPPING_FEE : BigDecimal.ZERO;
        order.setShippingFee(shippingFee);

        BigDecimal totalAmount = subtotal.add(shippingFee);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        // Clear user cart_items after successful order placement
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);

        return toOrderDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getUserOrders(String userEmail) {
        User user = getUserByEmail(userEmail);
        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toOrderDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(String userEmail, Long orderId) {
        User user = getUserByEmail(userEmail);
        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new IllegalArgumentException("Order not found or unauthorized with ID: " + orderId));
        return toOrderDto(order);
    }

    public OrderDto toOrderDto(Order order) {
        if (order == null) {
            return null;
        }
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        if (order.getUser() != null) {
            dto.setUserId(order.getUser().getId());
            dto.setCustomerName(order.getUser().getName());
            dto.setCustomerEmail(order.getUser().getEmail());
        }
        if (order.getAddress() != null) {
            dto.setAddress(addressService.toAddressDto(order.getAddress()));
        }
        dto.setStatus(order.getStatus());
        dto.setSubtotal(order.getSubtotal());
        dto.setShippingFee(order.getShippingFee());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setRazorpayOrderId(order.getRazorpayOrderId());
        dto.setRazorpayPaymentId(order.getRazorpayPaymentId());
        dto.setRazorpaySignature(order.getRazorpaySignature());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        List<OrderItemDto> itemDtos = new ArrayList<>();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                OrderItemDto itemDto = new OrderItemDto();
                itemDto.setId(item.getId());
                itemDto.setProductVariantId(item.getProductVariant() != null ? item.getProductVariant().getId() : null);
                itemDto.setProductName(item.getProductName());
                itemDto.setWeightLabel(item.getWeightLabel());
                itemDto.setPrice(item.getPrice());
                itemDto.setQuantity(item.getQuantity());
                itemDto.setLineTotal(item.getLineTotal());
                itemDtos.add(itemDto);
            }
        }
        dto.setItems(itemDtos);
        return dto;
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<OrderDto> getAllOrdersAdmin(String status, org.springframework.data.domain.Pageable pageable) {
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            return orderRepository.findByStatusOrderByCreatedAtDesc(status.trim().toUpperCase(), pageable)
                    .map(this::toOrderDto);
        }
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toOrderDto);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderByIdAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));
        return toOrderDto(order);
    }

    private static final java.util.Set<String> ALLOWED_STATUSES = java.util.Set.of("PLACED", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED");

    @Transactional
    public OrderDto updateOrderStatusAdmin(Long orderId, String newStatus) {
        if (newStatus == null || !ALLOWED_STATUSES.contains(newStatus.trim().toUpperCase())) {
            throw new IllegalArgumentException("Invalid status: '" + newStatus + "'. Allowed statuses: PLACED, CONFIRMED, SHIPPED, DELIVERED, CANCELLED.");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        String oldStatus = order.getStatus();
        String targetStatus = newStatus.trim().toUpperCase();

        // If order status changes to CANCELLED, restore deducted stock back to inventory
        if (!"CANCELLED".equalsIgnoreCase(oldStatus) && "CANCELLED".equalsIgnoreCase(targetStatus)) {
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    ProductVariant variant = item.getProductVariant();
                    if (variant != null) {
                        int currentStock = variant.getStock() != null ? variant.getStock() : 0;
                        variant.setStock(currentStock + item.getQuantity());
                    }
                }
            }
        }

        order.setStatus(targetStatus);
        order.setUpdatedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);

        if (!"CONFIRMED".equalsIgnoreCase(oldStatus) && "CONFIRMED".equalsIgnoreCase(targetStatus)) {
            sendOrderNotifications(saved);
        }

        return toOrderDto(saved);
    }

    @Transactional
    public CreatePaymentResponse createPayment(String userEmail, Long orderId) {
        User user = getUserByEmail(userEmail);
        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new IllegalArgumentException("Order not found or unauthorized with ID: " + orderId));

        if ("PAID".equalsIgnoreCase(order.getPaymentStatus())) {
            throw new IllegalStateException("Order ID " + orderId + " is already PAID. Double payment prevented.");
        }

        // Amount in paise (multiply totalAmount by 100)
        long amountInPaise = order.getTotalAmount()
                .multiply(new BigDecimal("100"))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        try {
            RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "order_rcptid_" + order.getId());

            com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            String razorpayOrderId = razorpayOrder.get("id");

            order.setRazorpayOrderId(razorpayOrderId);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            return new CreatePaymentResponse(razorpayOrderId, amountInPaise, "INR", razorpayKeyId);
        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order for order ID {}", orderId, e);
            throw new RuntimeException("Failed to create Razorpay payment order: " + e.getMessage(), e);
        }
    }

    @Transactional
    public OrderDto verifyPayment(String userEmail, Long orderId, VerifyPaymentRequest request) {
        if (request == null || request.getRazorpayOrderId() == null || request.getRazorpayPaymentId() == null || request.getRazorpaySignature() == null) {
            throw new IllegalArgumentException("Missing required Razorpay payment verification parameters.");
        }

        User user = getUserByEmail(userEmail);
        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new IllegalArgumentException("Order not found or unauthorized with ID: " + orderId));

        if ("PAID".equalsIgnoreCase(order.getPaymentStatus())) {
            log.info("Order ID {} is already marked as PAID.", orderId);
            return toOrderDto(order);
        }

        // Requirement 4: Confirm razorpayOrderId round-trips correctly and matches stored order row
        if (order.getRazorpayOrderId() == null || !order.getRazorpayOrderId().equals(request.getRazorpayOrderId())) {
            log.warn("POTENTIAL TAMPERING ATTEMPT: Razorpay order ID mismatch for order ID {}. Stored: {}, Received: {}",
                    orderId, order.getRazorpayOrderId(), request.getRazorpayOrderId());
            throw new IllegalArgumentException("Razorpay order ID mismatch. Payment verification failed.");
        }

        // Requirement 3: Recompute HMAC-SHA256 signature
        String data = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
        String expectedSignature = calculateHmacSha256(data, razorpayKeySecret);

        boolean isValid = MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                request.getRazorpaySignature().getBytes(StandardCharsets.UTF_8)
        );

        if (!isValid) {
            log.warn("POTENTIAL TAMPERING ATTEMPT! Razorpay signature verification failed for order ID {}. Expected: {}, Received: {}",
                    orderId, expectedSignature, request.getRazorpaySignature());
            throw new IllegalArgumentException("Invalid Razorpay payment signature. Payment verification failed.");
        }

        // Verification successful: update order status
        order.setPaymentStatus("PAID");
        order.setStatus("CONFIRMED");
        order.setRazorpayPaymentId(request.getRazorpayPaymentId());
        order.setRazorpaySignature(request.getRazorpaySignature());
        order.setUpdatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        log.info("Payment verified successfully for Order ID {}. Payment ID: {}", orderId, request.getRazorpayPaymentId());

        // Dispatch Email & WhatsApp order confirmation notifications
        sendOrderNotifications(savedOrder);

        return toOrderDto(savedOrder);
    }

    private void sendOrderNotifications(Order order) {
        try {
            emailService.sendOrderConfirmationEmail(order);
        } catch (Exception e) {
            log.error("Failed to send order email notification for order {}: {}", order.getId(), e.getMessage());
        }

        try {
            whatsAppService.sendOrderConfirmationWhatsApp(order);
        } catch (Exception e) {
            log.error("Failed to send order WhatsApp notification for order {}: {}", order.getId(), e.getMessage());
        }
    }

    private String calculateHmacSha256(String data, String secret) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            byte[] hmacData = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hmacData) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC-SHA256 signature", e);
        }
    }
}
