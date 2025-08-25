# Product Catalog Service

The Product Catalog Service manages products, categories, and product images for the e-commerce platform.

## Features

- Product management (CRUD operations)
- Category management with hierarchical structure
- Product image management
- Product search and filtering
- Brand management
- Tag-based product organization
- Integration with inventory service via Kafka events

## Technology Stack

- Spring Boot 3.1.5
- Spring Data JPA with PostgreSQL
- Spring Cloud Netflix Eureka (Service Discovery)
- Spring Cloud Config (Configuration Management)
- Apache Kafka (Event Streaming)
- Flyway (Database Migration)
- Spring Security with JWT

## API Endpoints

### Category Endpoints

- `POST /api/categories` - Create a new category (Admin/Manager)
- `PUT /api/categories/{id}` - Update category (Admin/Manager)
- `GET /api/categories/{id}` - Get category by ID
- `GET /api/categories/slug/{slug}` - Get category by slug
- `GET /api/categories` - Get all categories (paginated)
- `GET /api/categories/root` - Get root categories
- `GET /api/categories/{parentId}/subcategories` - Get subcategories
- `GET /api/categories/search?query={query}` - Search categories
- `DELETE /api/categories/{id}` - Delete category (Admin)

### Product Endpoints

- `POST /api/products` - Create a new product (Admin/Manager)
- `PUT /api/products/{id}` - Update product (Admin/Manager)
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/sku/{sku}` - Get product by SKU
- `GET /api/products/slug/{slug}` - Get product by slug
- `GET /api/products` - Get all products (paginated)
- `GET /api/products/category/{categoryId}` - Get products by category
- `GET /api/products/brand/{brand}` - Get products by brand
- `GET /api/products/price-range?minPrice={min}&maxPrice={max}` - Get products by price range
- `GET /api/products/featured` - Get featured products
- `GET /api/products/search?query={query}` - Search products
- `GET /api/products/tags?tags={tag1,tag2}` - Get products by tags
- `GET /api/products/brands` - Get all available brands
- `PATCH /api/products/{id}/status?status={status}` - Update product status (Admin/Manager)
- `DELETE /api/products/{id}` - Delete product (Admin)

## Configuration

The service uses Spring Cloud Config for centralized configuration. Key configurations include:

- Database connection (PostgreSQL)
- Kafka configuration
- JWT secret and expiration
- Image upload settings
- Search and pagination defaults

## Kafka Events

The service publishes the following events:

- `PRODUCT_CREATED` - When a new product is created
- `PRODUCT_UPDATED` - When a product is updated
- `PRODUCT_DELETED` - When a product is deleted
- `PRODUCT_OUT_OF_STOCK` - When a product goes out of stock
- `PRODUCT_PRICE_CHANGED` - When product price changes

## Database Schema

### Categories Table
- Hierarchical structure with parent-child relationships
- Unique slug for SEO-friendly URLs
- Display order for custom sorting

### Products Table
- Comprehensive product information
- SKU for inventory tracking
- Multiple pricing fields (price, compare_at_price, cost_price)
- Dimensions and weight for shipping calculations
- SEO metadata fields

### Product Images Table
- Multiple images per product
- Primary image designation
- Image metadata (dimensions, mime type)

## Running the Service

1. Ensure PostgreSQL is running and the `product_db` database exists
2. Ensure Kafka is running
3. Start Config Server and Eureka Server
4. Run the service:
   ```bash
   mvn spring-boot:run
   ```

The service will start on port 8083 by default.

## Development

### Building
```bash
mvn clean install
```

### Running Tests
```bash
mvn test
```

### Docker Build
```bash
docker build -t product-catalog-service .
```

## Monitoring

- Health endpoint: `http://localhost:8083/actuator/health`
- Metrics endpoint: `http://localhost:8083/actuator/metrics`
- Prometheus endpoint: `http://localhost:8083/actuator/prometheus`