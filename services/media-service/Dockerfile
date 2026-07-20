FROM eclipse-temurin:21-jre

RUN apt-get update && apt-get install -y --no-install-recommends ffmpeg && rm -rf /var/lib/apt/lists/*

COPY services/media-service/target/media-service-*.jar /app/media-service.jar
WORKDIR /app

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "media-service.jar"]
