package com.ecommerce.notification.template;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateEngine {
    
    private final TemplateEngine thymeleafEngine;
    
    @Value("${notification.template.company-name:E-Commerce Platform}")
    private String companyName;
    
    @Value("${notification.template.support-email:support@ecommerce.com}")
    private String supportEmail;
    
    @Value("${notification.template.website-url:https://www.ecommerce.com}")
    private String websiteUrl;
    
    @Value("${notification.template.logo-url:https://www.ecommerce.com/logo.png}")
    private String logoUrl;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    
    public String processTemplate(String templateName, Map<String, Object> variables) {
        log.info("Processing email template: {}", templateName);
        
        try {
            // Add default variables
            Map<String, Object> enrichedVariables = enrichVariables(variables);
            
            // Create Thymeleaf context
            Context context = new Context();
            context.setVariables(enrichedVariables);
            
            // Process template
            return thymeleafEngine.process(templateName, context);
            
        } catch (Exception e) {
            log.error("Error processing template: {}", templateName, e);
            // Fallback to simple template processing
            return processSimpleTemplate(templateName, variables);
        }
    }
    
    private Map<String, Object> enrichVariables(Map<String, Object> variables) {
        Map<String, Object> enriched = new HashMap<>(variables);
        
        // Add company defaults
        enriched.putIfAbsent("companyName", companyName);
        enriched.putIfAbsent("supportEmail", supportEmail);
        enriched.putIfAbsent("websiteUrl", websiteUrl);
        enriched.putIfAbsent("logoUrl", logoUrl);
        
        // Add current year for copyright
        enriched.put("currentYear", LocalDateTime.now().getYear());
        
        // Format dates if present
        if (enriched.containsKey("date") && enriched.get("date") instanceof LocalDateTime) {
            LocalDateTime date = (LocalDateTime) enriched.get("date");
            enriched.put("formattedDate", date.format(DATE_FORMATTER));
            enriched.put("formattedDateTime", date.format(DATETIME_FORMATTER));
        }
        
        return enriched;
    }
    
    private String processSimpleTemplate(String templateName, Map<String, Object> variables) {
        log.warn("Falling back to simple template processing for: {}", templateName);
        
        try {
            // Load template from resources
            String templateContent = loadTemplateContent(templateName);
            
            // Simple variable replacement
            return replaceVariables(templateContent, variables);
            
        } catch (Exception e) {
            log.error("Error in simple template processing", e);
            return generateFallbackContent(templateName, variables);
        }
    }
    
    private String loadTemplateContent(String templateName) throws IOException {
        try {
            String templatePath = "templates/" + templateName + ".html";
            Path path = Paths.get(getClass().getClassLoader().getResource(templatePath).toURI());
            return Files.readString(path);
        } catch (java.net.URISyntaxException e) {
            throw new IOException("Failed to load template: " + templateName, e);
        }
    }
    
    private String replaceVariables(String template, Map<String, Object> variables) {
        String result = template;
        Map<String, Object> enrichedVariables = enrichVariables(variables);
        
        // Pattern to find ${variableName} placeholders
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(template);
        
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object value = enrichedVariables.get(variableName);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }
    
    private String generateFallbackContent(String templateName, Map<String, Object> variables) {
        StringBuilder content = new StringBuilder();
        
        content.append("<html><body style='font-family: Arial, sans-serif;'>");
        content.append("<h2>").append(companyName).append("</h2>");
        
        switch (templateName.toLowerCase()) {
            case "order-confirmation":
                content.append("<h3>Order Confirmation</h3>");
                content.append("<p>Your order has been confirmed.</p>");
                if (variables.containsKey("orderNumber")) {
                    content.append("<p>Order Number: ").append(variables.get("orderNumber")).append("</p>");
                }
                break;
                
            case "payment-success":
                content.append("<h3>Payment Successful</h3>");
                content.append("<p>Your payment has been processed successfully.</p>");
                if (variables.containsKey("amount")) {
                    content.append("<p>Amount: $").append(variables.get("amount")).append("</p>");
                }
                break;
                
            case "welcome-email":
                content.append("<h3>Welcome!</h3>");
                content.append("<p>Welcome to ").append(companyName).append("!</p>");
                if (variables.containsKey("customerName")) {
                    content.append("<p>Hi ").append(variables.get("customerName")).append(",</p>");
                }
                content.append("<p>Thank you for joining us.</p>");
                break;
                
            default:
                content.append("<h3>Notification</h3>");
                content.append("<p>You have a new notification from ").append(companyName).append(".</p>");
        }
        
        content.append("<hr>");
        content.append("<p style='font-size: 12px; color: #666;'>");
        content.append("If you have any questions, please contact us at ");
        content.append("<a href='mailto:").append(supportEmail).append("'>").append(supportEmail).append("</a>");
        content.append("</p>");
        content.append("</body></html>");
        
        return content.toString();
    }
    
    public Map<String, String> getAvailableTemplates() {
        Map<String, String> templates = new HashMap<>();
        
        templates.put("order-confirmation", "Order Confirmation Email");
        templates.put("order-shipped", "Order Shipped Notification");
        templates.put("order-delivered", "Order Delivered Confirmation");
        templates.put("order-cancelled", "Order Cancellation Notice");
        templates.put("payment-success", "Payment Success Confirmation");
        templates.put("payment-failed", "Payment Failed Notification");
        templates.put("payment-refunded", "Payment Refund Confirmation");
        templates.put("welcome-email", "Welcome Email for New Users");
        templates.put("password-reset", "Password Reset Instructions");
        templates.put("promotional", "Promotional Email");
        
        return templates;
    }
    
    public boolean validateTemplate(String templateName) {
        return getAvailableTemplates().containsKey(templateName.toLowerCase());
    }
}