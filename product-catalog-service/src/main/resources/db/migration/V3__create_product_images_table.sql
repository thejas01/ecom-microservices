-- Create product_images table
CREATE TABLE product_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(200),
    display_order INTEGER DEFAULT 0,
    is_primary BOOLEAN DEFAULT false,
    width INTEGER,
    height INTEGER,
    size_in_bytes BIGINT,
    mime_type VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_product_images_product_id ON product_images(product_id);
CREATE INDEX idx_product_images_display_order ON product_images(display_order);
CREATE INDEX idx_product_images_is_primary ON product_images(is_primary);

-- Ensure only one primary image per product
CREATE UNIQUE INDEX idx_product_images_primary ON product_images(product_id) WHERE is_primary = true;

-- Insert sample product images
INSERT INTO product_images (product_id, image_url, alt_text, display_order, is_primary, width, height, mime_type)
SELECT 
    id,
    'https://example.com/images/laptop-pro-15-main.jpg',
    'Professional Laptop Pro 15 - Main View',
    1,
    true,
    1200,
    800,
    'image/jpeg'
FROM products WHERE sku = 'LAPTOP-001';

INSERT INTO product_images (product_id, image_url, alt_text, display_order, is_primary, width, height, mime_type)
SELECT 
    id,
    'https://example.com/images/laptop-pro-15-side.jpg',
    'Professional Laptop Pro 15 - Side View',
    2,
    false,
    1200,
    800,
    'image/jpeg'
FROM products WHERE sku = 'LAPTOP-001';

INSERT INTO product_images (product_id, image_url, alt_text, display_order, is_primary, width, height, mime_type)
SELECT 
    id,
    'https://example.com/images/smartphone-x12-front.jpg',
    'SmartPhone X12 - Front View',
    1,
    true,
    800,
    1200,
    'image/jpeg'
FROM products WHERE sku = 'PHONE-001';

INSERT INTO product_images (product_id, image_url, alt_text, display_order, is_primary, width, height, mime_type)
SELECT 
    id,
    'https://example.com/images/smartphone-x12-back.jpg',
    'SmartPhone X12 - Back View',
    2,
    false,
    800,
    1200,
    'image/jpeg'
FROM products WHERE sku = 'PHONE-001';

INSERT INTO product_images (product_id, image_url, alt_text, display_order, is_primary, width, height, mime_type)
SELECT 
    id,
    'https://example.com/images/cotton-tshirt-white.jpg',
    'Classic Cotton T-Shirt - White',
    1,
    true,
    800,
    800,
    'image/jpeg'
FROM products WHERE sku = 'SHIRT-001';

-- Create a view for products with their primary image
CREATE VIEW products_with_primary_image AS
SELECT 
    p.*,
    pi.image_url as primary_image_url,
    pi.alt_text as primary_image_alt
FROM 
    products p
    LEFT JOIN product_images pi ON p.id = pi.product_id AND pi.is_primary = true;