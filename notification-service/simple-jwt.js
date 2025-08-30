const crypto = require('crypto');

// Same secret as in application.yml
const secret = 'your-super-secure-512-bit-secret-key-for-jwt-token-generation-and-validation-in-microservices';

// Create base64url encoding function
function base64urlEncode(str) {
    return Buffer.from(str)
        .toString('base64')
        .replace(/\+/g, '-')
        .replace(/\//g, '_')
        .replace(/=/g, '');
}

// Create JWT header
const header = {
    "alg": "HS256",
    "typ": "JWT"
};

// Create JWT payload
const payload = {
    "sub": "admin",
    "role": "ADMIN",
    "iat": Math.floor(Date.now() / 1000),
    "exp": Math.floor(Date.now() / 1000) + (24 * 60 * 60) // 24 hours
};

// Encode header and payload
const encodedHeader = base64urlEncode(JSON.stringify(header));
const encodedPayload = base64urlEncode(JSON.stringify(payload));

// Create signature
const data = encodedHeader + '.' + encodedPayload;
const signature = crypto
    .createHmac('sha256', secret)
    .update(data)
    .digest('base64')
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=/g, '');

// Create final JWT
const jwt = encodedHeader + '.' + encodedPayload + '.' + signature;

console.log('JWT Token:', jwt);
console.log('Payload:', payload);