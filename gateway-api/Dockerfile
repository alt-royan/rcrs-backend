FROM eclipse-temurin:21-jdk AS build

COPY target/gateway-api-*.jar /app/gateway-api.jar
WORKDIR /app

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "gateway-api.jar"]
