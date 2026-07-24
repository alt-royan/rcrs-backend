## ---- Builder stage ----
FROM maven:3.9.16-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .
COPY shared-lib/pom.xml shared-lib/
COPY discovery-server/pom.xml discovery-server/
COPY gateway-api/pom.xml gateway-api/
COPY services/media-service/pom.xml services/media-service/
COPY services/metadata-read-service/pom.xml services/metadata-read-service/
COPY services/metadata-write-service/pom.xml services/metadata-write-service/
COPY services/search-service/pom.xml services/search-service/
COPY services/user-service/pom.xml services/user-service/
COPY services/workflow-service/pom.xml services/workflow-service/
# Скачиваем все зависимости (слой закэшируется)
RUN mvn dependency:go-offline -B

# Теперь копируем исходники и собираем
COPY shared-lib shared-lib
COPY discovery-server/src discovery-server/src
RUN mvn clean package -DskipTests -pl discovery-server -am

## ---- Final stage ----
FROM eclipse-temurin:21-jre-alpine

# Создаём непривилегированного пользователя
RUN addgroup -g 1001 -S appuser && adduser -u 1001 -S appuser -G appuser

WORKDIR /app
# Копируем только собранный jar из builder
COPY --from=builder /build/discovery-server/target/discovery-server-*.jar app.jar

# Переключаемся на непривилегированного пользователя
USER appuser

EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
