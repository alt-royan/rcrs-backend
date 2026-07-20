FROM eclipse-temurin:21-jdk AS build

COPY services/metadata-write-service/target/metadata-write-service-*.jar /app/metadata-write-service.jar
WORKDIR /app

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "metadata-write-service.jar"]
