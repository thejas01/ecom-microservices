# Config Server

The Config Server provides centralized configuration management for all microservices in the e-commerce platform.

## Features

- Centralized configuration for all microservices
- Environment-specific configurations (dev, test, prod)
- Native file-based configuration (can be migrated to Git)
- Configuration encryption/decryption support
- Auto-refresh capability (with Spring Cloud Bus)
- Basic authentication protection

## Running the Config Server

```bash
cd config-server
mvn spring-boot:run
```

The Config Server will start on port 8888.

## Accessing Configuration

To access configuration for a specific service:

```
http://localhost:8888/{application-name}/{profile}
```

Examples:
- http://localhost:8888/auth-service/default
- http://localhost:8888/user-service/dev
- http://localhost:8888/order-service/prod

## Authentication

The Config Server is protected with basic authentication:
- Username: `config-user`
- Password: `config-pass`

## Configuration Files

All configuration files are located in `src/main/resources/config/`:

- `application.yml` - Common configuration for all services
- `application-dev.yml` - Development environment overrides
- `application-prod.yml` - Production environment overrides
- Individual service configurations (e.g., `auth-service.yml`)

## Adding New Service Configuration

1. Create a new YAML file in `src/main/resources/config/` named `{service-name}.yml`
2. Add service-specific configuration
3. The configuration will be automatically available at `http://localhost:8888/{service-name}/default`

## Encryption/Decryption

To encrypt sensitive values:

```bash
curl -X POST http://config-user:config-pass@localhost:8888/encrypt -d "mySecretValue"
```

To decrypt:

```bash
curl -X POST http://config-user:config-pass@localhost:8888/decrypt -d "{encrypted-value}"
```

## Client Configuration

Microservices should include the following configuration in their `bootstrap.yml`:

```yaml
spring:
  application:
    name: service-name
  cloud:
    config:
      uri: http://localhost:8888
      username: config-user
      password: config-pass
      fail-fast: true
      retry:
        initial-interval: 1000
        max-attempts: 6
```