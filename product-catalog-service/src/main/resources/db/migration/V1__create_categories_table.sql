-- Create categories table
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    slug VARCHAR(100) NOT NULL UNIQUE,
    image_url VARCHAR(500),
    parent_id UUID,
    display_order INTEGER,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_parent_category FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- Create indexes for better performance
CREATE INDEX idx_categories_parent_id ON categories(parent_id);
CREATE INDEX idx_categories_slug ON categories(slug);
CREATE INDEX idx_categories_is_active ON categories(is_active);
CREATE INDEX idx_categories_display_order ON categories(display_order);

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_categories_updated_at BEFORE UPDATE ON categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert some initial categories
INSERT INTO categories (name, description, slug, display_order) VALUES
('Electronics', 'Electronic devices and accessories', 'electronics', 1),
('Clothing', 'Apparel and fashion items', 'clothing', 2),
('Home & Garden', 'Home improvement and garden supplies', 'home-garden', 3),
('Sports & Outdoors', 'Sports equipment and outdoor gear', 'sports-outdoors', 4),
('Books', 'Physical and digital books', 'books', 5);

-- Insert subcategories
INSERT INTO categories (name, description, slug, parent_id, display_order) 
SELECT 'Computers & Laptops', 'Desktop computers and laptops', 'computers-laptops', id, 1 
FROM categories WHERE slug = 'electronics';

INSERT INTO categories (name, description, slug, parent_id, display_order) 
SELECT 'Mobile Phones', 'Smartphones and accessories', 'mobile-phones', id, 2 
FROM categories WHERE slug = 'electronics';

INSERT INTO categories (name, description, slug, parent_id, display_order) 
SELECT 'Men''s Clothing', 'Clothing for men', 'mens-clothing', id, 1 
FROM categories WHERE slug = 'clothing';

INSERT INTO categories (name, description, slug, parent_id, display_order) 
SELECT 'Women''s Clothing', 'Clothing for women', 'womens-clothing', id, 2 
FROM categories WHERE slug = 'clothing';