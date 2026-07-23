docker network create rcrs
mvnw.cmd clean install -DskipTests
docker-compose -f ./local/docker-compose.yml build
docker-compose -f ./local/docker-compose.yml up -d
