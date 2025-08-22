# API Gateway

The API Gateway serves as the single entry point for all client requests to the e-commerce microservices platform.

## Features

- **Dynamic Routing**: Routes requests to appropriate microservices using Spring Cloud Gateway
- **Service Discovery**: Integrates with Eureka for dynamic service discovery
- **Authentication**: JWT token validation for protected endpoints
- **Load Balancing**: Built-in load balancing across service instances
- **Circuit Breaker**: Resilience4j integration for fault tolerance
- **Request/Response Logging**: Comprehensive logging for monitoring
- **CORS Support**: Cross-Origin Resource Sharing configuration
- **Rate Limiting**: Can be configured for API rate limiting
- **Fallback Responses**: Graceful handling when services are unavailable

## Running the API Gateway

```bash
cd api-gateway
mvn spring-boot:run
```

The API Gateway will start on port 8080.

## API Routes

### Public Routes (No Authentication Required)
- `/api/auth/**` → Auth Service
- `/api/products/**` → Product Catalog Service
- `/api/categories/**` → Product Catalog Service

### Protected Routes (JWT Token Required)
- `/api/users/**` → User Service
- `/api/orders/**` → Order Service
- `/api/payments/**` → Payment Service
- `/api/inventory/**` → Inventory Service
- `/api/notifications/**` → Notification Service

## Authentication

Protected routes require a valid JWT token in the Authorization header:
```
Authorization: Bearer <jwt-token>
```

The gateway validates the token and adds user information to downstream request headers:
- `X-User-Id`: User ID from JWT
- `X-Username`: Username from JWT

## Configuration

The gateway uses Spring Cloud Config Server for centralized configuration. Key configurations include:

- **JWT Secret**: For token validation
- **Route Definitions**: Service routing rules
- **Circuit Breaker**: Resilience patterns
- **CORS Settings**: Allowed origins and methods

## Filters

### Global Filters
1. **LoggingFilter**: Logs all requests and responses with timing information

### Route-Specific Filters
1. **AuthenticationFilter**: Validates JWT tokens for protected routes
2. **RewritePath**: Removes API prefix before forwarding to services

## Fallback Endpoints

When a service is unavailable, the gateway returns a standardized error response:
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 503,
  "error": "Service Unavailable",
  "message": "Service is temporarily unavailable. Please try again later.",
  "service": "SERVICE_NAME"
}
```

## Monitoring

Access gateway routes and metrics:
- Routes: `GET /actuator/gateway/routes`
- Metrics: `GET /actuator/metrics`
- Health: `GET /actuator/health`

## Testing

Run tests with:
```bash
mvn test
```

## Docker Support

Build and run with Docker:
```bash
docker build -t api-gateway .
docker run -p 8080:8080 api-gateway
```