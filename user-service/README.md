# User Service

The User Service manages user profiles, authentication data, and user addresses in the e-commerce platform.

## Features

- **User Management**: Create, read, update, and manage user profiles
- **Address Management**: Manage multiple addresses per user with default address support
- **User Search**: Search users by name, username, or email
- **User Verification**: Email and phone number verification
- **Caching**: Redis-based caching for improved performance
- **Event Publishing**: Publishes user lifecycle events to Kafka
- **Database Integration**: PostgreSQL with Flyway migrations
- **Security**: JWT-based authentication and role-based access control

## API Endpoints

### User Management

| Method | Endpoint | Description | Access |
|--------|----------|-------------|---------|
| POST | `/users` | Create a new user | Admin only |
| GET | `/users/{id}` | Get user by ID | Admin or own profile |
| GET | `/users/username/{username}` | Get user by username | Admin only |
| GET | `/users/email/{email}` | Get user by email | Admin only |
| GET | `/users` | Get all users (paginated) | Admin only |
| GET | `/users/search` | Search users | Admin only |
| PUT | `/users/{id}` | Update user profile | Admin or own profile |
| POST | `/users/{id}/deactivate` | Deactivate user | Admin only |
| POST | `/users/{id}/activate` | Activate user | Admin only |
| POST | `/users/{id}/verify-email` | Verify user email | Admin or own profile |
| POST | `/users/{id}/verify-phone` | Verify user phone | Admin or own profile |
| POST | `/users/{id}/last-login` | Update last login time | Internal use |
| GET | `/users/stats` | Get user statistics | Admin only |
| GET | `/users/check-username/{username}` | Check username availability | Public |
| GET | `/users/check-email/{email}` | Check email availability | Public |

### Address Management

| Method | Endpoint | Description | Access |
|--------|----------|-------------|---------|
| POST | `/users/{userId}/addresses` | Create new address | Admin or own profile |
| GET | `/users/{userId}/addresses` | Get all user addresses | Admin or own profile |
| GET | `/users/{userId}/addresses/{addressId}` | Get specific address | Admin or own profile |
| GET | `/users/{userId}/addresses/default` | Get default address | Admin or own profile |
| PUT | `/users/{userId}/addresses/{addressId}` | Update address | Admin or own profile |
| POST | `/users/{userId}/addresses/{addressId}/set-default` | Set as default address | Admin or own profile |
| DELETE | `/users/{userId}/addresses/{addressId}` | Delete address | Admin or own profile |
| GET | `/users/{userId}/addresses/stats` | Get address statistics | Admin or own profile |

## Request/Response Examples

### Create User
```bash
POST /users
Content-Type: application/json
Authorization: Bearer {admin-jwt-token}

{
  "username": "johndoe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "dateOfBirth": "1990-01-15",
  "gender": "MALE"
}
```

### Create Address
```bash
POST /users/{userId}/addresses
Content-Type: application/json
Authorization: Bearer {jwt-token}

{
  "type": "HOME",
  "label": "Home Address",
  "addressLine1": "123 Main Street",
  "addressLine2": "Apt 4B",
  "city": "New York",
  "state": "NY",
  "postalCode": "10001",
  "country": "USA",
  "isDefault": true
}
```

### Response Format
```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": "user-uuid",
    "username": "johndoe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "dateOfBirth": "1990-01-15",
    "gender": "MALE",
    "active": true,
    "emailVerified": false,
    "phoneVerified": false,
    "createdAt": "2024-01-01T12:00:00",
    "updatedAt": "2024-01-01T12:00:00",
    "addresses": []
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

## Configuration

The service uses Spring Cloud Config for centralized configuration. Key configurations include:

- **Database**: PostgreSQL connection settings
- **Redis**: Caching configuration
- **Kafka**: Event publishing settings
- **JWT**: Token validation settings
- **Security**: Access control configuration

## Database Schema

### users table
- `id` (UUID) - Primary key
- `username` (VARCHAR) - Unique username
- `email` (VARCHAR) - Unique email address
- `first_name` (VARCHAR) - User's first name
- `last_name` (VARCHAR) - User's last name
- `phone_number` (VARCHAR) - Phone number
- `date_of_birth` (DATE) - Date of birth
- `gender` (VARCHAR) - Gender (MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY)
- `profile_image_url` (VARCHAR) - Profile picture URL
- `active` (BOOLEAN) - Account status
- `email_verified` (BOOLEAN) - Email verification status
- `phone_verified` (BOOLEAN) - Phone verification status
- `last_login_at` (TIMESTAMP) - Last login time
- `created_at` (TIMESTAMP) - Creation time
- `updated_at` (TIMESTAMP) - Last update time

### addresses table
- `id` (UUID) - Primary key
- `user_id` (UUID) - Foreign key to users table
- `type` (VARCHAR) - Address type (HOME, WORK, BILLING, SHIPPING, OTHER)
- `label` (VARCHAR) - User-defined label
- `address_line_1` (VARCHAR) - Primary address line
- `address_line_2` (VARCHAR) - Secondary address line
- `city` (VARCHAR) - City
- `state` (VARCHAR) - State/Province
- `postal_code` (VARCHAR) - Postal/ZIP code
- `country` (VARCHAR) - Country
- `is_default` (BOOLEAN) - Default address flag
- `created_at` (TIMESTAMP) - Creation time
- `updated_at` (TIMESTAMP) - Last update time

## Kafka Events

The service publishes the following events to `user-events-topic`:

- **USER_CREATED**: When a new user is created
- **USER_UPDATED**: When user profile is updated
- **USER_ACTIVATED**: When user account is activated
- **USER_DEACTIVATED**: When user account is deactivated
- **EMAIL_VERIFIED**: When user email is verified
- **PHONE_VERIFIED**: When user phone is verified
- **ADDRESS_CREATED**: When a new address is added
- **ADDRESS_UPDATED**: When an address is updated
- **ADDRESS_DELETED**: When an address is removed
- **ADDRESS_SET_DEFAULT**: When an address is set as default

## Security

- **JWT Authentication**: Bearer token validation
- **Role-based Access**: ADMIN and CUSTOMER roles
- **Method-level Security**: PreAuthorize annotations
- **Data Protection**: Users can only access their own data (except admins)

## Caching

- **Redis Integration**: User profiles cached for 30 minutes
- **Cache Keys**: Based on user ID and username
- **Cache Eviction**: Automatic eviction on user updates

## Running the Service

### Prerequisites
- Java 17+
- PostgreSQL database
- Redis server
- Kafka cluster
- Config Server running

### Local Development
```bash
cd user-service
mvn spring-boot:run
```

### Docker
```bash
docker build -t user-service .
docker run -p 8082:8082 user-service
```

## Testing
```bash
mvn test
```

## Monitoring

- Health check: `GET /actuator/health`
- Metrics: `GET /actuator/metrics`
- Info: `GET /actuator/info`
- Prometheus metrics: `GET /actuator/prometheus`

## Dependencies

- Spring Boot 3.1.5
- Spring Security
- Spring Data JPA
- Spring Cloud Config
- Spring Cloud Eureka
- Spring Kafka
- Redis
- PostgreSQL
- Flyway
- JWT (jjwt)
- Testcontainers (for testing)