package com.ecommerce.common.dto.order;

import com.ecommerce.common.dto.user.AddressDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDTO {
    
    private String id;
    
    private String orderNumber;
    
    @NotNull(message = "User ID is required")
    private String userId;
    
    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemDTO> items;
    
    @NotNull(message = "Shipping address is required")
    @Valid
    private AddressDTO shippingAddress;
    
    @Valid
    private AddressDTO billingAddress;
    
    private OrderStatus status;
    
    private PaymentStatus paymentStatus;
    
    private String paymentMethod;
    
    private BigDecimal subtotal;
    
    private BigDecimal taxAmount;
    
    private BigDecimal shippingAmount;
    
    private BigDecimal discountAmount;
    
    private BigDecimal totalAmount;
    
    private String couponCode;
    
    private String notes;
    
    private String trackingNumber;
    
    private String shippingMethod;
    
    private LocalDateTime estimatedDeliveryDate;
    
    private LocalDateTime orderDate;
    
    private LocalDateTime shippedDate;
    
    private LocalDateTime deliveredDate;
    
    private LocalDateTime cancelledDate;
    
    private String cancellationReason;
    
    public enum OrderStatus {
        PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED
    }
    
    public enum PaymentStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED
    }
}