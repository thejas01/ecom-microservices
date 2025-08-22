# Common Libraries

This module contains shared libraries used across all microservices in the e-commerce platform.

## Structure

### common-dto
Contains Data Transfer Objects (DTOs) shared between services:
- **User DTOs**: UserDTO, AddressDTO, UserResponseDTO
- **Product DTOs**: ProductDTO, CategoryDTO, DimensionsDTO
- **Order DTOs**: OrderDTO, OrderItemDTO
- **Payment DTOs**: PaymentDTO, CardDetailsDTO
- **Notification DTOs**: NotificationDTO

### common-utils
Contains utility classes and common functionality:
- **Exception Handling**: BusinessException, ResourceNotFoundException, GlobalExceptionHandler
- **Response Objects**: ApiResponse, ErrorResponse, PageInfo
- **Security Utilities**: JwtTokenUtil, SecurityConstants
- **Kafka Configuration**: KafkaTopics, EventType

## Usage

Add as dependency in service `pom.xml`:

```xml
<dependency>
    <groupId>com.ecommerce</groupId>
    <artifactId>common-dto</artifactId>
    <version>${project.version}</version>
</dependency>

<dependency>
    <groupId>com.ecommerce</groupId>
    <artifactId>common-utils</artifactId>
    <version>${project.version}</version>
</dependency>
```

## Features

### DTOs
- Bean Validation annotations
- JSON serialization/deserialization
- Lombok for boilerplate code reduction
- Builder pattern support

### Exception Handling
- Centralized exception handling with GlobalExceptionHandler
- Standardized error responses
- HTTP status code mapping
- Validation error handling

### API Responses
- Consistent response format with ApiResponse
- Pagination support with PageInfo
- Success/error response builders

### Security
- JWT token utilities
- Security constants and headers
- Role-based access control support

### Kafka Integration
- Centralized topic definitions
- Event type enumerations
- Consistent messaging patterns

## Building

```bash
cd common-libs
mvn clean install
```

This will install the libraries to the local Maven repository for use by other services.