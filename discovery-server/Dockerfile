FROM eclipse-temurin:21-jdk AS build

COPY discovery-server/target/discovery-server-*.jar /app/discovery-server.jar
WORKDIR /app

EXPOSE 8761
ENTRYPOINT ["java", "-jar", "discovery-server.jar"]
