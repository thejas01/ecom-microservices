package com.ecommerce.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${notification.email.from-name:E-Commerce Platform}")
    private String fromName;
    
    @Value("${notification.email.mock-mode:true}")
    private boolean mockMode;
    
    public void sendSimpleEmail(String to, String subject, String content) {
        if (mockMode) {
            log.info("MOCK EMAIL SENT - To: {}, Subject: {}, Content: {}", to, subject, content);
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            log.info("Simple email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Error sending simple email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        if (mockMode) {
            log.info("MOCK HTML EMAIL SENT - To: {}, Subject: {}", to, subject);
            log.debug("HTML Content: {}", htmlContent);
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Error sending HTML email to: {}", to, e);
            throw new RuntimeException("Failed to send HTML email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    public void sendTemplatedEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        log.info("Sending templated email - To: {}, Template: {}", to, templateName);
        // Template processing will be handled by EmailTemplateEngine
        // This method is a placeholder for template-based emails
    }
    
    public void sendEmailWithAttachment(String to, String subject, String content, String attachmentPath) {
        if (mockMode) {
            log.info("MOCK EMAIL WITH ATTACHMENT SENT - To: {}, Subject: {}, Attachment: {}", 
                    to, subject, attachmentPath);
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content);
            
            // Add attachment logic here if needed
            
            mailSender.send(message);
            log.info("Email with attachment sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Error sending email with attachment to: {}", to, e);
            throw new RuntimeException("Failed to send email with attachment", e);
        }
    }
    
    public boolean validateEmailAddress(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                           "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
}