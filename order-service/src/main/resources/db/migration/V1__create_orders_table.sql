CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(36) PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id VARCHAR(36) NOT NULL,
    customer_email VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    subtotal DECIMAL(19, 2) NOT NULL,
    tax_amount DECIMAL(19, 2) DEFAULT 0.00,
    shipping_amount DECIMAL(19, 2) DEFAULT 0.00,
    discount_amount DECIMAL(19, 2) DEFAULT 0.00,
    total_amount DECIMAL(19, 2) NOT NULL,
    payment_id VARCHAR(36),
    payment_status VARCHAR(50),
    shipping_address TEXT,
    billing_address TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP
);

CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_order_number ON orders(order_number);