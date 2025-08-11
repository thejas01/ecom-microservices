# E-commerce Microservices Platform

A production-ready, scalable e-commerce platform built with Java Spring Boot microservices architecture.

## Architecture Overview

This project implements a microservices architecture with the following components:

### Core Infrastructure
- **Service Discovery**: Eureka Server for service registration and discovery
- **Configuration Management**: Spring Cloud Config Server for centralized configuration
- **API Gateway**: Spring Cloud Gateway for request routing and load balancing
- **Security**: JWT-based authentication with shared security utilities

### Microservices
- **Product Catalog Service**: Manages products, categories, and inventory (MongoDB + Redis)
- **User Accounts Service**: Handles user registration, authentication, and profiles (PostgreSQL)
- **Shopping Cart Service**: Manages user shopping carts (Redis)
- **Order Processing Service**: Processes orders asynchronously (RabbitMQ)

### Observability & Monitoring
- **Logging**: ELK Stack (Elasticsearch, Logstash, Kibana)
- **Tracing**: Zipkin for distributed tracing
- **Metrics**: Micrometer with Spring Boot Actuator

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### Running the Infrastructure

1. Start the infrastructure services:
```bash
docker-compose up -d mongodb postgresql redis rabbitmq elasticsearch kibana logstash zipkin
```

2. Start the service discovery:
```bash
cd service-discovery
mvn spring-boot:run
```

3. Start the config server:
```bash
cd config-server
mvn spring-boot:run
```

### Building the Project

```bash
mvn clean install
```

### Running Individual Services

Each microservice can be run independently:

```bash
cd [service-name]
mvn spring-boot:run
```

## Service Ports

| Service | Port | Description |
|---------|------|-------------|
| Service Discovery | 8761 | Eureka Dashboard |
| Config Server | 8888 | Configuration Server |
| Product Catalog | 8081 | Product management API |
| User Accounts | 8082 | User management API |
| Shopping Cart | 8083 | Cart management API |
| Order Processing | 8084 | Order processing API |
| API Gateway | 8080 | Main application entry point |

## Infrastructure Ports

| Service | Port | Description |
|---------|------|-------------|
| MongoDB | 27017 | NoSQL database |
| PostgreSQL | 5432 | Relational database |
| Redis | 6379 | In-memory cache |
| RabbitMQ | 5672, 15672 | Message queue, Management UI |
| Elasticsearch | 9200 | Search engine |
| Kibana | 5601 | Log visualization |
| Logstash | 5044 | Log processing |
| Zipkin | 9411 | Distributed tracing |

## Development

### Phase 1: Core Infrastructure ✅
- [x] Parent Maven project with multi-module structure
- [x] Common models and DTOs
- [x] Shared security utilities
- [x] Eureka Server for service discovery
- [x] Spring Cloud Config Server
- [x] Docker Compose for local development

### Phase 2: Core Microservices (In Progress)
- [ ] Product Catalog Service
- [ ] User Accounts Service  
- [ ] Shopping Cart Service
- [ ] Order Processing Service

### Phase 3: Production Infrastructure (Planned)
- [ ] API Gateway
- [ ] Comprehensive testing
- [ ] CI/CD pipeline
- [ ] Kubernetes manifests

## Security

- JWT-based authentication
- BCrypt password hashing
- Role-based authorization
- HTTPS in production

## Testing

Run all tests:
```bash
mvn test
```

Run integration tests:
```bash
mvn verify
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.