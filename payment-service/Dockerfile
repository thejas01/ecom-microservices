FROM openjdk:17-jdk-slim

# Create app directory
WORKDIR /app

# Copy the jar file
COPY target/payment-service-*.jar payment-service.jar

# Expose port
EXPOSE 8087

# Run the application
ENTRYPOINT ["java", "-jar", "payment-service.jar"]