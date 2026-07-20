FROM eclipse-temurin:21-jdk AS build

COPY services/metadata-read-service/target/metadata-read-service-*.jar /app/metadata-read-service.jar
WORKDIR /app

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "metadata-read-service.jar"]
