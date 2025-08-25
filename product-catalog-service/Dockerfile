FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the jar file
COPY target/product-catalog-service-*.jar app.jar

# Expose the port
EXPOSE 8083

# Set environment variables
ENV JAVA_OPTS=""

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]