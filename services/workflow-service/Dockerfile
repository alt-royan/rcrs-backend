FROM eclipse-temurin:21-jdk AS build

COPY target/workflow-service-*.jar /app/workflow-service.jar
WORKDIR /app

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "workflow-service.jar"]
