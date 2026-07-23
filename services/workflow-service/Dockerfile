## ---- Builder stage ----
FROM maven:3.8-openjdk-21 AS builder

WORKDIR /build
# Копируем только pom.xml для кэширования зависимостей
COPY services/workflow-service/pom.xml .
# Скачиваем все зависимости (слой закэшируется)
RUN mvn dependency:go-offline -B

# Теперь копируем исходники и собираем
COPY services/workflow-service/src ./src
RUN mvn clean package -DskipTests

## ---- Final stage ----
FROM eclipse-temurin:21-jre-alpine

# Создаём непривилегированного пользователя
RUN addgroup -g 1001 -S appuser && adduser -u 1001 -S appuser -G appuser

WORKDIR /app
# Копируем только собранный jar из builder
COPY --from=builder /build/target/workflow-service-*.jar app.jar

# Переключаемся на непривилегированного пользователя
USER appuser

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
