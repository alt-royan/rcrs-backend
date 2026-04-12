# Build stage
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

RUN chmod +x mvnw

COPY shared-lib/pom.xml shared-lib/
COPY services/upload-service/pom.xml services/upload-service/

RUN ./mvnw dependency:go-offline -B

COPY . .

RUN ./mvnw package -pl services/upload-service -am -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

EXPOSE 8082

COPY --from=builder /app/services/upload-service/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
