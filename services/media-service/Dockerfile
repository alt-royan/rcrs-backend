# Build stage
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

RUN chmod +x mvnw

COPY shared-lib/pom.xml shared-lib/
COPY services/media-service/pom.xml services/media-service/

RUN ./mvnw dependency:go-offline -B

COPY . .

RUN ./mvnw package -pl services/media-service -am -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

EXPOSE 8081

COPY --from=builder /app/services/media-service/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
