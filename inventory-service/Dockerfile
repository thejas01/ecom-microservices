FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/inventory-service-*.jar inventory-service.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "inventory-service.jar"]