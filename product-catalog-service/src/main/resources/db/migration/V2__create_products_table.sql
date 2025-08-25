-- Create products table
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    short_description VARCHAR(500),
    slug VARCHAR(200) NOT NULL UNIQUE,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    compare_at_price DECIMAL(10,2) CHECK (compare_at_price >= 0),
    cost_price DECIMAL(10,2) CHECK (cost_price >= 0),
    category_id UUID NOT NULL,
    brand VARCHAR(100),
    weight DOUBLE PRECISION,
    length DOUBLE PRECISION,
    width DOUBLE PRECISION,
    height DOUBLE PRECISION,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    is_featured BOOLEAN DEFAULT false,
    meta_title VARCHAR(200),
    meta_description VARCHAR(500),
    meta_keywords VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT chk_product_status CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE', 'OUT_OF_STOCK', 'DISCONTINUED'))
);

-- Create indexes for better performance
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_slug ON products(slug);
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_brand ON products(brand);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_is_featured ON products(is_featured);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_created_at ON products(created_at DESC);

-- Create product tags table
CREATE TABLE product_tags (
    product_id UUID NOT NULL,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (product_id, tag),
    CONSTRAINT fk_product_tags_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX idx_product_tags_tag ON product_tags(tag);

-- Create trigger to update updated_at timestamp
CREATE TRIGGER update_products_updated_at BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert some sample products
INSERT INTO products (sku, name, description, short_description, slug, price, category_id, brand, status, is_featured)
SELECT 
    'LAPTOP-001',
    'Professional Laptop Pro 15',
    'High-performance laptop with Intel Core i7 processor, 16GB RAM, and 512GB SSD. Perfect for professionals and power users.',
    'Powerful laptop for professionals',
    'professional-laptop-pro-15',
    1299.99,
    c.id,
    'TechBrand',
    'ACTIVE',
    true
FROM categories c WHERE c.slug = 'computers-laptops';

INSERT INTO products (sku, name, description, short_description, slug, price, compare_at_price, category_id, brand, status)
SELECT 
    'PHONE-001',
    'SmartPhone X12',
    'Latest flagship smartphone with 5G connectivity, triple camera system, and all-day battery life.',
    'Flagship 5G smartphone',
    'smartphone-x12',
    899.99,
    999.99,
    c.id,
    'PhoneMaker',
    'ACTIVE'
FROM categories c WHERE c.slug = 'mobile-phones';

INSERT INTO products (sku, name, description, short_description, slug, price, category_id, brand, status, weight, length, width, height)
SELECT 
    'SHIRT-001',
    'Classic Cotton T-Shirt',
    'Comfortable 100% cotton t-shirt available in multiple colors. Perfect for everyday wear.',
    'Comfortable cotton t-shirt',
    'classic-cotton-tshirt',
    29.99,
    c.id,
    'FashionBrand',
    'ACTIVE',
    0.2,
    30.0,
    25.0,
    1.0
FROM categories c WHERE c.slug = 'mens-clothing';

-- Add some tags to products
INSERT INTO product_tags (product_id, tag)
SELECT id, 'laptop' FROM products WHERE sku = 'LAPTOP-001'
UNION ALL
SELECT id, 'computer' FROM products WHERE sku = 'LAPTOP-001'
UNION ALL
SELECT id, 'electronics' FROM products WHERE sku = 'LAPTOP-001'
UNION ALL
SELECT id, 'smartphone' FROM products WHERE sku = 'PHONE-001'
UNION ALL
SELECT id, '5g' FROM products WHERE sku = 'PHONE-001'
UNION ALL
SELECT id, 'mobile' FROM products WHERE sku = 'PHONE-001';