-- Insert test user with the ID that was being requested
INSERT INTO users (
    id, 
    username, 
    email, 
    first_name, 
    last_name, 
    phone_number, 
    date_of_birth, 
    gender, 
    active, 
    email_verified, 
    phone_verified, 
    created_at, 
    updated_at
) VALUES (
    '8e14b828-700a-4b2a-a65e-61bad9bb4a17',
    'testuser',
    'testuser@example.com',
    'Test',
    'User',
    '+1234567890',
    '1990-01-01',
    'OTHER',
    true,
    true,
    true,
    NOW(),
    NOW()
) ON CONFLICT (id) DO NOTHING;

-- Add some additional test users
INSERT INTO users (
    id, 
    username, 
    email, 
    first_name, 
    last_name, 
    phone_number, 
    date_of_birth, 
    gender, 
    active, 
    email_verified, 
    phone_verified, 
    created_at, 
    updated_at
) VALUES 
(
    'a1234567-89ab-cdef-0123-456789abcdef',
    'john.doe',
    'john.doe@example.com',
    'John',
    'Doe',
    '+1234567891',
    '1985-05-15',
    'MALE',
    true,
    true,
    false,
    NOW(),
    NOW()
),
(
    'b2345678-9abc-def0-1234-56789abcdef0',
    'jane.smith',
    'jane.smith@example.com',
    'Jane',
    'Smith',
    '+1234567892',
    '1992-08-20',
    'FEMALE',
    true,
    true,
    true,
    NOW(),
    NOW()
) ON CONFLICT (id) DO NOTHING;