FROM eclipse-temurin:21-jdk AS build

COPY services/search-service/target/search-service-*.jar /app/search-service.jar
WORKDIR /app

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "search-service.jar"]
