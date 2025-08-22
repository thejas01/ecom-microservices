# User Service Testing Guide

This guide provides comprehensive instructions for testing the User Service, including unit tests, integration tests, and manual API testing.

## Prerequisites

Before testing, ensure you have:
1. PostgreSQL running on port 5432
2. Redis running on port 6379
3. Kafka running on port 9092
4. Config Server running on port 8888
5. Eureka Server running on port 8761

## 1. Setting Up Test Environment

### Option A: Using Docker Compose (Recommended)

Create a `docker-compose-test.yml` file in the project root:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: e_com
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: 123456
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data

  redis:
    image: redis:7
    ports:
      - "6379:6379"

  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

volumes:
  postgres-data:
```

Start the infrastructure:
```bash
docker-compose -f docker-compose-test.yml up -d
```

### Option B: Manual Setup

1. **PostgreSQL Setup**:
```sql
-- Connect to PostgreSQL as superuser
CREATE DATABASE e_com;
CREATE USER postgres WITH PASSWORD '123456';
GRANT ALL PRIVILEGES ON DATABASE e_com TO postgres;
```

2. **Redis Setup**:
```bash
# Install Redis if not already installed
brew install redis  # macOS
sudo apt-get install redis  # Ubuntu

# Start Redis
redis-server
```

3. **Kafka Setup**:
```bash
# Download and start Kafka
wget https://downloads.apache.org/kafka/3.5.0/kafka_2.13-3.5.0.tgz
tar -xzf kafka_2.13-3.5.0.tgz
cd kafka_2.13-3.5.0

# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka (in another terminal)
bin/kafka-server-start.sh config/server.properties
```

## 2. Running Unit Tests

```bash
cd user-service
mvn clean test
```

### Running Specific Test Classes
```bash
# Test a specific class
mvn test -Dtest=UserServiceTest

# Test with pattern
mvn test -Dtest=*ServiceTest

# Run with coverage
mvn test jacoco:report
```

## 3. Running Integration Tests

### Create Integration Test Classes

Create test files for integration testing:

`src/test/java/com/ecommerce/user/integration/UserControllerIntegrationTest.java`

## 4. Manual API Testing

### Start Required Services

1. **Start Config Server**:
```bash
cd config-server
mvn spring-boot:run
```

2. **Start Eureka Server**:
```bash
cd eureka-server
mvn spring-boot:run
```

3. **Start User Service**:
```bash
cd user-service
mvn spring-boot:run
```

### API Testing with cURL

#### 1. Create a User (Admin only)
```bash
curl -X POST http://localhost:8082/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-jwt-token>" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "dateOfBirth": "1990-01-15",
    "gender": "MALE"
  }'
```

#### 2. Get User by ID
```bash
curl -X GET http://localhost:8082/users/{userId} \
  -H "Authorization: Bearer <jwt-token>"
```

#### 3. Update User
```bash
curl -X PUT http://localhost:8082/users/{userId} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <jwt-token>" \
  -d '{
    "firstName": "John Updated",
    "phoneNumber": "+0987654321"
  }'
```

#### 4. Search Users (Admin only)
```bash
curl -X GET "http://localhost:8082/users/search?query=john&page=0&size=10" \
  -H "Authorization: Bearer <admin-jwt-token>"
```

#### 5. Check Username Availability
```bash
curl -X GET http://localhost:8082/users/check-username/johndoe
```

#### 6. Create Address
```bash
curl -X POST http://localhost:8082/users/{userId}/addresses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <jwt-token>" \
  -d '{
    "type": "HOME",
    "label": "Home Address",
    "addressLine1": "123 Main Street",
    "addressLine2": "Apt 4B",
    "city": "New York",
    "state": "NY",
    "postalCode": "10001",
    "country": "USA",
    "isDefault": true
  }'
```

#### 7. Get User Addresses
```bash
curl -X GET http://localhost:8082/users/{userId}/addresses \
  -H "Authorization: Bearer <jwt-token>"
```

## 5. Testing with Postman

### Import Collection

Create a Postman collection with the following structure:

```
User Service Tests
├── Authentication
│   └── Get JWT Token (from Auth Service)
├── User Management
│   ├── Create User
│   ├── Get User by ID
│   ├── Update User
│   ├── Search Users
│   ├── Deactivate User
│   └── Check Username/Email
└── Address Management
    ├── Create Address
    ├── Get Addresses
    ├── Update Address
    ├── Set Default Address
    └── Delete Address
```

### Environment Variables
```json
{
  "baseUrl": "http://localhost:8082",
  "adminToken": "{{admin_jwt_token}}",
  "userToken": "{{user_jwt_token}}",
  "userId": "{{created_user_id}}",
  "addressId": "{{created_address_id}}"
}
```

## 6. Load Testing with Apache JMeter

### Basic Load Test Plan

1. **Thread Group**: 100 users, 10 seconds ramp-up
2. **HTTP Requests**:
   - GET /users/{userId} - 40%
   - GET /users - 20%
   - POST /users - 10%
   - PUT /users/{userId} - 20%
   - GET /users/{userId}/addresses - 10%

3. **Assertions**:
   - Response code: 200/201
   - Response time: < 500ms

## 7. Testing Kafka Events

### Monitor Kafka Topics
```bash
# List topics
kafka-topics.sh --bootstrap-server localhost:9092 --list

# Consume messages from user-events-topic
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic user-events-topic \
  --from-beginning
```

### Expected Events
- USER_CREATED
- USER_UPDATED
- USER_ACTIVATED/DEACTIVATED
- EMAIL_VERIFIED
- ADDRESS_CREATED/UPDATED/DELETED

## 8. Testing Redis Caching

### Monitor Redis
```bash
# Connect to Redis CLI
redis-cli

# Monitor all commands
MONITOR

# Check cached users
KEYS users:*

# Get specific cached user
GET users::{userId}
```

### Cache Testing Scenarios
1. First request should hit database
2. Subsequent requests should hit cache
3. Update operations should evict cache
4. Cache should expire after 30 minutes

## 9. Database Testing

### Verify Migrations
```sql
-- Check if tables are created
SELECT * FROM information_schema.tables 
WHERE table_schema = 'public';

-- Check users table
SELECT * FROM users;

-- Check addresses table
SELECT * FROM addresses;

-- Verify constraints
SELECT * FROM information_schema.table_constraints 
WHERE table_schema = 'public';
```

## 10. Error Scenarios Testing

### Test Validation Errors
```bash
# Missing required fields
curl -X POST http://localhost:8082/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-jwt-token>" \
  -d '{
    "username": "jo"  # Too short
  }'
```

### Test Business Logic Errors
```bash
# Duplicate username
curl -X POST http://localhost:8082/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-jwt-token>" \
  -d '{
    "username": "existinguser",
    "email": "new@example.com",
    "firstName": "Test",
    "lastName": "User"
  }'
```

### Test Authorization Errors
```bash
# Access without token
curl -X GET http://localhost:8082/users

# Access other user's data without admin role
curl -X GET http://localhost:8082/users/{otherUserId} \
  -H "Authorization: Bearer <regular-user-token>"
```

## 11. Performance Monitoring

### Application Metrics
```bash
# Health check
curl http://localhost:8082/actuator/health

# Metrics
curl http://localhost:8082/actuator/metrics

# Specific metric
curl http://localhost:8082/actuator/metrics/http.server.requests
```

### Database Performance
```sql
-- Check slow queries
SELECT * FROM pg_stat_statements 
ORDER BY total_time DESC 
LIMIT 10;

-- Check index usage
SELECT * FROM pg_stat_user_indexes;
```

## 12. Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Check PostgreSQL is running
   - Verify credentials in application.yml
   - Check database exists

2. **Redis Connection Failed**
   - Check Redis is running
   - Verify port 6379 is not in use

3. **Kafka Connection Failed**
   - Check Kafka and Zookeeper are running
   - Verify bootstrap servers configuration

4. **JWT Token Invalid**
   - Ensure Auth Service is running
   - Token might be expired
   - Check JWT secret matches

### Debug Logging
```yaml
# Add to application.yml for debug logging
logging:
  level:
    com.ecommerce.user: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type: TRACE
```

## 13. Automated Test Script

Create `test-user-service.sh`:

```bash
#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "Starting User Service Tests..."

# Check if services are running
check_service() {
    if curl -f -s "http://localhost:$2/actuator/health" > /dev/null; then
        echo -e "${GREEN}✓ $1 is running${NC}"
    else
        echo -e "${RED}✗ $1 is not running on port $2${NC}"
        exit 1
    fi
}

# Check required services
check_service "Config Server" 8888
check_service "Eureka Server" 8761
check_service "User Service" 8082

# Run tests
echo -e "\n${GREEN}Running API Tests...${NC}"

# Test 1: Check username availability
echo -n "Test 1: Check username availability... "
response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/users/check-username/testuser)
if [ $response -eq 200 ]; then
    echo -e "${GREEN}PASSED${NC}"
else
    echo -e "${RED}FAILED (HTTP $response)${NC}"
fi

# Add more tests...

echo -e "\n${GREEN}All tests completed!${NC}"
```

Make it executable:
```bash
chmod +x test-user-service.sh
./test-user-service.sh
```

## Summary

This testing guide covers:
- Unit testing with JUnit and Mockito
- Integration testing with Spring Boot Test
- Manual API testing with cURL and Postman
- Load testing with JMeter
- Event streaming testing with Kafka
- Cache testing with Redis
- Database verification
- Error scenario testing
- Performance monitoring

Regular testing ensures the User Service maintains high quality and reliability.