package com.ecommerce.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {
    
    @Value("${notification.sms.mock-mode:true}")
    private boolean mockMode;
    
    @Value("${notification.sms.provider:twilio}")
    private String smsProvider;
    
    @Value("${notification.sms.from-number:+1234567890}")
    private String fromNumber;
    
    @Value("${notification.sms.api-key:}")
    private String apiKey;
    
    @Value("${notification.sms.api-secret:}")
    private String apiSecret;
    
    private final Random random = new Random();
    
    public SmsResponse sendSms(String toPhoneNumber, String message) {
        log.info("Sending SMS to: {}, Provider: {}", toPhoneNumber, smsProvider);
        
        if (!isValidPhoneNumber(toPhoneNumber)) {
            log.error("Invalid phone number: {}", toPhoneNumber);
            return SmsResponse.failed("Invalid phone number format");
        }
        
        if (message == null || message.trim().isEmpty()) {
            log.error("Empty message content");
            return SmsResponse.failed("Message content cannot be empty");
        }
        
        if (mockMode) {
            return sendMockSms(toPhoneNumber, message);
        }
        
        return sendRealSms(toPhoneNumber, message);
    }
    
    private SmsResponse sendMockSms(String toPhoneNumber, String message) {
        log.info("MOCK SMS SENT - To: {}, Message: {}", toPhoneNumber, message);
        
        // Simulate random success/failure for testing
        if (random.nextDouble() < 0.9) { // 90% success rate
            String messageId = "MOCK_MSG_" + System.currentTimeMillis();
            return SmsResponse.success(messageId);
        } else {
            return SmsResponse.failed("Mock SMS delivery failed");
        }
    }
    
    private SmsResponse sendRealSms(String toPhoneNumber, String message) {
        try {
            switch (smsProvider.toLowerCase()) {
                case "twilio":
                    return sendViaTwilio(toPhoneNumber, message);
                case "nexmo":
                    return sendViaNexmo(toPhoneNumber, message);
                case "aws-sns":
                    return sendViaAwsSns(toPhoneNumber, message);
                default:
                    log.error("Unsupported SMS provider: {}", smsProvider);
                    return SmsResponse.failed("Unsupported SMS provider");
            }
        } catch (Exception e) {
            log.error("Error sending SMS to: {}", toPhoneNumber, e);
            return SmsResponse.failed("SMS delivery failed: " + e.getMessage());
        }
    }
    
    private SmsResponse sendViaTwilio(String toPhoneNumber, String message) {
        // Twilio integration placeholder
        log.info("Sending SMS via Twilio to: {}", toPhoneNumber);
        // Actual Twilio API integration would go here
        return SmsResponse.failed("Twilio integration not implemented");
    }
    
    private SmsResponse sendViaNexmo(String toPhoneNumber, String message) {
        // Nexmo/Vonage integration placeholder
        log.info("Sending SMS via Nexmo to: {}", toPhoneNumber);
        // Actual Nexmo API integration would go here
        return SmsResponse.failed("Nexmo integration not implemented");
    }
    
    private SmsResponse sendViaAwsSns(String toPhoneNumber, String message) {
        // AWS SNS integration placeholder
        log.info("Sending SMS via AWS SNS to: {}", toPhoneNumber);
        // Actual AWS SNS integration would go here
        return SmsResponse.failed("AWS SNS integration not implemented");
    }
    
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        // Basic validation - adjust regex based on requirements
        String phoneRegex = "^\\+?[1-9]\\d{1,14}$"; // E.164 format
        return phoneNumber.matches(phoneRegex);
    }
    
    public String formatPhoneNumber(String phoneNumber) {
        // Remove all non-digit characters except +
        String cleaned = phoneNumber.replaceAll("[^+\\d]", "");
        
        // Ensure it starts with +
        if (!cleaned.startsWith("+")) {
            // Default to US country code if not specified
            cleaned = "+1" + cleaned;
        }
        
        return cleaned;
    }
    
    public int calculateSmsSegments(String message) {
        if (message == null || message.isEmpty()) {
            return 0;
        }
        
        int messageLength = message.length();
        
        // Standard SMS length limits
        if (messageLength <= 160) {
            return 1;
        } else {
            // Multi-part messages use 153 characters per segment
            return (int) Math.ceil(messageLength / 153.0);
        }
    }
    
    public static class SmsResponse {
        private final boolean success;
        private final String messageId;
        private final String errorMessage;
        private final Map<String, Object> metadata;
        
        private SmsResponse(boolean success, String messageId, String errorMessage) {
            this.success = success;
            this.messageId = messageId;
            this.errorMessage = errorMessage;
            this.metadata = new HashMap<>();
        }
        
        public static SmsResponse success(String messageId) {
            return new SmsResponse(true, messageId, null);
        }
        
        public static SmsResponse failed(String errorMessage) {
            return new SmsResponse(false, null, errorMessage);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessageId() {
            return messageId;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public Map<String, Object> getMetadata() {
            return metadata;
        }
        
        public void addMetadata(String key, Object value) {
            metadata.put(key, value);
        }
    }
}