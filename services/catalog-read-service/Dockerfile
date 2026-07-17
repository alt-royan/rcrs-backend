FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace/app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY shared-lib/pom.xml shared-lib/
COPY services/catalog-read-service/pom.xml services/catalog-read-service/
RUN ./mvnw -pl services/catalog-read-service -am dependency:go-offline
COPY shared-lib/src shared-lib/src
COPY services/catalog-read-service/src services/catalog-read-service/src
RUN ./mvnw -pl services/catalog-read-service -am package -DskipTests

FROM eclipse-temurin:21-jre
COPY --from=build /workspace/app/services/catalog-read-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
