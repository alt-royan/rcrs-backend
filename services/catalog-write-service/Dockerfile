FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace/app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY shared-lib/pom.xml shared-lib/
COPY services/catalog-write-service/pom.xml services/catalog-write-service/
RUN ./mvnw -pl services/catalog-write-service -am dependency:go-offline
COPY shared-lib/src shared-lib/src
COPY services/catalog-write-service/src services/catalog-write-service/src
RUN ./mvnw -pl services/catalog-write-service -am package -DskipTests

FROM eclipse-temurin:21-jre
COPY --from=build /workspace/app/services/catalog-write-service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
