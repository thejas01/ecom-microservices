#!/bin/bash

# User Service API Testing Script
# This script tests the User Service APIs

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="http://localhost:8082"
AUTH_SERVICE_URL="http://localhost:8081"

# Variables to store test data
JWT_TOKEN=""
USER_ID=""
ADDRESS_ID=""

echo -e "${YELLOW}User Service API Testing Script${NC}"
echo "================================"

# Function to print test results
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ $2${NC}"
    else
        echo -e "${RED}✗ $2${NC}"
    fi
}

# Function to extract value from JSON
extract_json_value() {
    echo $1 | grep -o "\"$2\":[^,}]*" | cut -d':' -f2 | tr -d ' "' | head -1
}

# Check if services are running
echo -e "\n${YELLOW}Checking services...${NC}"

check_service() {
    response=$(curl -s -o /dev/null -w "%{http_code}" "$2/actuator/health" 2>/dev/null)
    if [ "$response" = "200" ]; then
        echo -e "${GREEN}✓ $1 is running${NC}"
        return 0
    else
        echo -e "${RED}✗ $1 is not running on $2${NC}"
        return 1
    fi
}

# Check required services
if ! check_service "Auth Service" "$AUTH_SERVICE_URL"; then
    echo -e "${RED}Please start the Auth Service first${NC}"
    exit 1
fi

if ! check_service "User Service" "$BASE_URL"; then
    echo -e "${RED}Please start the User Service first${NC}"
    exit 1
fi

# Step 1: Get JWT Token (Admin)
echo -e "\n${YELLOW}Step 1: Getting Admin JWT Token${NC}"

# First, register an admin user (if not exists)
admin_register_response=$(curl -s -X POST "$AUTH_SERVICE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "adminuser",
    "email": "admin@example.com",
    "password": "AdminPass123!",
    "role": "ADMIN"
  }' 2>/dev/null)

# Login as admin
login_response=$(curl -s -X POST "$AUTH_SERVICE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "adminuser",
    "password": "AdminPass123!"
  }' 2>/dev/null)

JWT_TOKEN=$(extract_json_value "$login_response" "accessToken")

if [ -n "$JWT_TOKEN" ]; then
    echo -e "${GREEN}✓ Successfully obtained JWT token${NC}"
else
    echo -e "${RED}✗ Failed to obtain JWT token${NC}"
    echo "Response: $login_response"
    exit 1
fi

# Step 2: Test Public Endpoints
echo -e "\n${YELLOW}Step 2: Testing Public Endpoints${NC}"

# Check username availability
response=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/users/check-username/testuser123")
print_result $((response == 200)) "Check username availability (HTTP $response)"

# Check email availability
response=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/users/check-email/test123@example.com")
print_result $((response == 200)) "Check email availability (HTTP $response)"

# Step 3: Create a new user
echo -e "\n${YELLOW}Step 3: Creating a new user${NC}"

create_user_response=$(curl -s -X POST "$BASE_URL/users" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "username": "testuser'$(date +%s)'",
    "email": "test'$(date +%s)'@example.com",
    "firstName": "Test",
    "lastName": "User",
    "phoneNumber": "+1234567890",
    "dateOfBirth": "1990-01-15",
    "gender": "MALE"
  }' 2>/dev/null)

USER_ID=$(extract_json_value "$create_user_response" "id")
http_status=$(echo "$create_user_response" | grep -o '"success":[^,}]*' | cut -d':' -f2)

if [ "$http_status" = "true" ] && [ -n "$USER_ID" ]; then
    echo -e "${GREEN}✓ User created successfully (ID: $USER_ID)${NC}"
else
    echo -e "${RED}✗ Failed to create user${NC}"
    echo "Response: $create_user_response"
fi

# Step 4: Get user by ID
echo -e "\n${YELLOW}Step 4: Getting user by ID${NC}"

get_user_response=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/users/$USER_ID" \
  -H "Authorization: Bearer $JWT_TOKEN")
print_result $((get_user_response == 200)) "Get user by ID (HTTP $get_user_response)"

# Step 5: Update user
echo -e "\n${YELLOW}Step 5: Updating user${NC}"

update_response=$(curl -s -o /dev/null -w "%{http_code}" -X PUT "$BASE_URL/users/$USER_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "firstName": "Updated",
    "lastName": "Name",
    "phoneNumber": "+0987654321"
  }')
print_result $((update_response == 200)) "Update user (HTTP $update_response)"

# Step 6: Search users
echo -e "\n${YELLOW}Step 6: Searching users${NC}"

search_response=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/users/search?query=test&page=0&size=10" \
  -H "Authorization: Bearer $JWT_TOKEN")
print_result $((search_response == 200)) "Search users (HTTP $search_response)"

# Step 7: Create address
echo -e "\n${YELLOW}Step 7: Creating address for user${NC}"

create_address_response=$(curl -s -X POST "$BASE_URL/users/$USER_ID/addresses" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
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
  }' 2>/dev/null)

ADDRESS_ID=$(extract_json_value "$create_address_response" "id")
address_success=$(echo "$create_address_response" | grep -o '"success":[^,}]*' | cut -d':' -f2)

if [ "$address_success" = "true" ] && [ -n "$ADDRESS_ID" ]; then
    echo -e "${GREEN}✓ Address created successfully (ID: $ADDRESS_ID)${NC}"
else
    echo -e "${RED}✗ Failed to create address${NC}"
    echo "Response: $create_address_response"
fi

# Step 8: Get user addresses
echo -e "\n${YELLOW}Step 8: Getting user addresses${NC}"

get_addresses_response=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/users/$USER_ID/addresses" \
  -H "Authorization: Bearer $JWT_TOKEN")
print_result $((get_addresses_response == 200)) "Get user addresses (HTTP $get_addresses_response)"

# Step 9: Verify email
echo -e "\n${YELLOW}Step 9: Verifying user email${NC}"

verify_email_response=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/users/$USER_ID/verify-email" \
  -H "Authorization: Bearer $JWT_TOKEN")
print_result $((verify_email_response == 200)) "Verify email (HTTP $verify_email_response)"

# Step 10: Get user stats
echo -e "\n${YELLOW}Step 10: Getting user statistics${NC}"

stats_response=$(curl -s "$BASE_URL/users/stats" \
  -H "Authorization: Bearer $JWT_TOKEN")
active_users=$(extract_json_value "$stats_response" "activeUserCount")

if [ -n "$active_users" ]; then
    echo -e "${GREEN}✓ User stats retrieved (Active users: $active_users)${NC}"
else
    echo -e "${RED}✗ Failed to get user stats${NC}"
fi

# Summary
echo -e "\n${YELLOW}Test Summary${NC}"
echo "============="
echo -e "${GREEN}Testing completed!${NC}"
echo -e "User ID: $USER_ID"
echo -e "Address ID: $ADDRESS_ID"

# Cleanup option
echo -e "\n${YELLOW}Cleanup (Optional)${NC}"
echo "To deactivate the test user, run:"
echo "curl -X POST \"$BASE_URL/users/$USER_ID/deactivate\" -H \"Authorization: Bearer $JWT_TOKEN\""