# Build stage
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

RUN chmod +x mvnw

COPY shared-lib/pom.xml shared-lib/
COPY services/catalog-service/pom.xml services/catalog-service/

RUN ./mvnw dependency:go-offline -B

COPY . .

RUN ./mvnw package -pl services/catalog-service -am -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

EXPOSE 8080

COPY --from=builder /app/services/catalog-service/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
