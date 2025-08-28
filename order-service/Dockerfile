FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/order-service-*.jar order-service.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "order-service.jar"]