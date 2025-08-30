-- Create notifications table
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(50) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(500),
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    template_name VARCHAR(100),
    reference_id VARCHAR(255),
    reference_type VARCHAR(50),
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    error_message TEXT,
    sent_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_type CHECK (type IN ('ORDER_CONFIRMATION', 'ORDER_SHIPPED', 'ORDER_DELIVERED', 
        'ORDER_CANCELLED', 'PAYMENT_SUCCESS', 'PAYMENT_FAILED', 'PAYMENT_REFUNDED', 
        'USER_REGISTRATION', 'PASSWORD_RESET', 'PROMOTIONAL')),
    CONSTRAINT chk_channel CHECK (channel IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP')),
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'DELIVERED', 'BOUNCED', 'UNSUBSCRIBED'))
);

-- Create indexes for better query performance
CREATE INDEX idx_notifications_recipient ON notifications(recipient);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notifications_reference ON notifications(reference_id, reference_type);
CREATE INDEX idx_notifications_status_retry ON notifications(status, retry_count) WHERE status = 'FAILED';
CREATE INDEX idx_notifications_pending_created ON notifications(status, created_at) WHERE status = 'PENDING';

-- Create composite indexes for common queries
CREATE INDEX idx_notifications_recipient_status ON notifications(recipient, status);
CREATE INDEX idx_notifications_type_date ON notifications(type, sent_at);

-- Add comments
COMMENT ON TABLE notifications IS 'Stores all system notifications sent through various channels';
COMMENT ON COLUMN notifications.type IS 'Type of notification being sent';
COMMENT ON COLUMN notifications.channel IS 'Communication channel used for notification';
COMMENT ON COLUMN notifications.recipient IS 'Email address, phone number, or device ID based on channel';
COMMENT ON COLUMN notifications.status IS 'Current status of the notification';
COMMENT ON COLUMN notifications.template_name IS 'Name of the template used for rendering the notification';
COMMENT ON COLUMN notifications.reference_id IS 'ID of the related entity (order, payment, user, etc.)';
COMMENT ON COLUMN notifications.reference_type IS 'Type of the related entity';
COMMENT ON COLUMN notifications.retry_count IS 'Number of times this notification has been retried';
COMMENT ON COLUMN notifications.max_retries IS 'Maximum number of retry attempts allowed';
COMMENT ON COLUMN notifications.error_message IS 'Error details if notification failed';
COMMENT ON COLUMN notifications.sent_at IS 'Timestamp when notification was successfully sent';