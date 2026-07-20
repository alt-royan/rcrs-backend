docker network create rcrs
docker-compose -f ./local/infra/docker-compose.yml up -d
docker-compose -f ./local/docker-compose.yml up -d
