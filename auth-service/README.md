# Auth Service

The Authentication and Authorization service handles user registration, login, JWT token management, and password reset functionality.

## Features

- **User Registration**: Create new user accounts with email verification
- **User Login**: Authenticate users with username/email and password
- **JWT Token Management**: Generate and validate access and refresh tokens
- **Password Reset**: Secure password reset flow with email tokens
- **Role-based Access**: Support for different user roles (CUSTOMER, ADMIN, VENDOR)
- **Event Publishing**: Publishes authentication events to Kafka
- **Database Integration**: PostgreSQL with Flyway migrations

## API Endpoints

### Public Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Register a new user |
| POST | `/auth/login` | Login with credentials |
| POST | `/auth/password-reset` | Initiate password reset |
| POST | `/auth/password-reset/confirm` | Confirm password reset |
| POST | `/auth/validate` | Validate JWT token |
| GET | `/auth/health` | Health check |

### Request/Response Examples

#### Register User
```bash
POST /auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "securepassword123",
  "role": "CUSTOMER"
}
```

#### Login
```bash
POST /auth/login
Content-Type: application/json

{
  "usernameOrEmail": "johndoe",
  "password": "securepassword123"
}
```

#### Response
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "userId": "user-uuid",
    "username": "johndoe",
    "email": "john@example.com",
    "role": "CUSTOMER"
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

## Configuration

The service uses Spring Cloud Config for centralized configuration. Key configurations include:

- **Database**: PostgreSQL connection settings
- **JWT**: Secret key and expiration times
- **Kafka**: Bootstrap servers and topics
- **Security**: Password encoding strength

## Database Schema

### user_credentials table
- `id` (UUID) - Primary key
- `username` (VARCHAR) - Unique username
- `email` (VARCHAR) - Unique email address
- `password` (VARCHAR) - Encrypted password
- `role` (VARCHAR) - User role (CUSTOMER, ADMIN, VENDOR)
- `enabled` (BOOLEAN) - Account status
- `account_non_expired` (BOOLEAN) - Account expiry status
- `account_non_locked` (BOOLEAN) - Account lock status
- `credentials_non_expired` (BOOLEAN) - Password expiry status
- `last_login_at` (TIMESTAMP) - Last login time
- `reset_token` (VARCHAR) - Password reset token
- `reset_token_expires_at` (TIMESTAMP) - Reset token expiry
- `created_at` (TIMESTAMP) - Creation time
- `updated_at` (TIMESTAMP) - Last update time

## JWT Token Structure

### Access Token Claims
- `sub` - Username
- `userId` - User ID
- `role` - User role
- `email` - User email
- `iat` - Issued at
- `exp` - Expiration time

### Refresh Token Claims
- `sub` - Username
- `userId` - User ID
- `tokenType` - "REFRESH"
- `iat` - Issued at
- `exp` - Expiration time

## Kafka Events

The service publishes the following events:

- **user-registration-topic**: When a new user registers
- **user-login-topic**: When a user logs in
- **password-reset-topic**: When password reset is initiated

## Security

- **BCrypt**: Password hashing with strength 12
- **JWT**: Signed with HS256 algorithm
- **Stateless**: No server-side session storage
- **CORS**: Configured for cross-origin requests

## Running the Service

### Prerequisites
- Java 17+
- PostgreSQL database
- Kafka cluster
- Config Server running

### Local Development
```bash
cd auth-service
mvn spring-boot:run
```

### Docker
```bash
docker build -t auth-service .
docker run -p 8081:8081 auth-service
```

## Testing
```bash
mvn test
```

## Monitoring

- Health check: `GET /actuator/health`
- Metrics: `GET /actuator/metrics`
- Info: `GET /actuator/info`