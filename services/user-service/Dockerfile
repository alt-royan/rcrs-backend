FROM eclipse-temurin:21-jdk AS build

COPY services/user-service/target/user-service-*.jar /app/user-service.jar
WORKDIR /app

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "user-service.jar"]
