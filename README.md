# E-commerce Microservices

A comprehensive e-commerce platform built with Spring Boot microservices architecture.

## Architecture Overview

This project implements a microservices-based e-commerce platform with the following services:

- **Eureka Server** - Service discovery and registration
- **API Gateway** - Single entry point for all client requests
- **Auth Service** - Authentication and authorization
- **User Service** - User management and profiles
- **Product Catalog Service** - Product information and catalog
- **Inventory Service** - Stock and inventory management
- **Order Service** - Order processing and management
- **Payment Service** - Payment processing
- **Notification Service** - Email and notification handling
- **Common Libraries** - Shared utilities and DTOs

## Technology Stack

- **Java 17**
- **Spring Boot 3.1.5**
- **Spring Cloud 2022.0.4**
- **Apache Kafka 3.4.1** - Event streaming
- **PostgreSQL 15** - Primary database
- **Redis 7** - Caching and session management
- **JWT** - Authentication tokens
- **Docker & Docker Compose** - Containerization
- **Maven** - Build tool

## Infrastructure Components

- **Zookeeper** - Kafka coordination
- **Kafka** - Message streaming platform
- **Kafka UI** - Kafka monitoring (http://localhost:8080)
- **PostgreSQL** - Multiple databases for microservices
- **Redis** - Caching layer
- **MailHog** - Email testing (http://localhost:8025)

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+
- Docker & Docker Compose

### Running the Infrastructure

Start all infrastructure services:

```bash
docker-compose up -d
```

This will start:
- Zookeeper (port 2181)
- Kafka (port 9092)
- Kafka UI (port 8080)
- PostgreSQL (port 5432)
- Redis (port 6379)
- MailHog SMTP (port 1025) and UI (port 8025)

### Database Setup

The PostgreSQL container automatically creates separate databases for each microservice:
- `auth_db`
- `user_db`
- `product_db`
- `inventory_db`
- `order_db`
- `payment_db`
- `notification_db`

### Building the Project

```bash
mvn clean install
```

### Running Individual Services

Navigate to each service directory and run:

```bash
mvn spring-boot:run
```

## Service Ports

| Service | Port |
|---------|------|
| Eureka Server | 8761 |
| API Gateway | 8080 |
| Auth Service | 8081 |
| User Service | 8082 |
| Product Catalog | 8083 |
| Inventory Service | 8084 |
| Order Service | 8085 |
| Payment Service | 8086 |
| Notification Service | 8087 |

## Monitoring & Tools

- **Kafka UI**: http://localhost:8080 - Monitor Kafka topics and messages
- **MailHog UI**: http://localhost:8025 - View test emails
- **Eureka Dashboard**: http://localhost:8761 - Service registry

## Development

### Adding New Services

1. Create a new module in the parent POM
2. Add service-specific configuration
3. Implement Spring Boot application
4. Register with Eureka
5. Add to API Gateway routing

### Testing

Run tests with:

```bash
mvn test
```

The project uses Testcontainers for integration testing.

## Configuration

Services use Spring Cloud Config for centralized configuration management. Configuration files are located in the `config-server` module.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License.