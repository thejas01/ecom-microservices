package com.ecommerce.common.utils.kafka;

public class KafkaTopics {
    
    // User Service Topics
    public static final String USER_EVENTS_TOPIC = "user-events-topic";
    public static final String USER_CREATED = "user-created-topic";
    public static final String USER_UPDATED = "user-updated-topic";
    public static final String USER_DELETED = "user-deleted-topic";
    public static final String USER_LOGIN = "user-login-topic";
    public static final String USER_REGISTRATION = "user-registration-topic";
    public static final String PASSWORD_RESET = "password-reset-topic";
    
    // Product Service Topics
    public static final String PRODUCT_CREATED = "product-created-topic";
    public static final String PRODUCT_UPDATED = "product-updated-topic";
    public static final String PRODUCT_DELETED = "product-deleted-topic";
    public static final String PRICE_UPDATED = "price-updated-topic";
    public static final String CATEGORY_UPDATED = "category-updated-topic";
    
    // Inventory Service Topics
    public static final String INVENTORY_UPDATED = "inventory-updated-topic";
    public static final String STOCK_RESERVED = "stock-reserved-topic";
    public static final String STOCK_RELEASED = "stock-released-topic";
    public static final String LOW_STOCK_ALERT = "low-stock-alert-topic";
    public static final String OUT_OF_STOCK = "out-of-stock-topic";
    
    // Order Service Topics
    public static final String ORDER_CREATED = "order-created-topic";
    public static final String ORDER_CONFIRMED = "order-confirmed-topic";
    public static final String ORDER_CANCELLED = "order-cancelled-topic";
    public static final String ORDER_SHIPPED = "order-shipped-topic";
    public static final String ORDER_DELIVERED = "order-delivered-topic";
    
    // Payment Service Topics
    public static final String PAYMENT_INITIATED = "payment-initiated-topic";
    public static final String PAYMENT_PROCESSED = "payment-processed-topic";
    public static final String PAYMENT_FAILED = "payment-failed-topic";
    public static final String PAYMENT_REFUNDED = "payment-refunded-topic";
    
    private KafkaTopics() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}