FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/api-gateway-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]