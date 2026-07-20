#!/bin/bash
set -e

ES_URL="http://localhost:9200"
SETTINGS_DIR="/index-settings"

echo "Waiting for Elasticsearch..."
until curl -sf "$ES_URL/_cluster/health" > /dev/null 2>&1; do
  sleep 2
done
echo "Elasticsearch is ready."

for file in "$SETTINGS_DIR"/*-settings.json; do
  [ -f "$file" ] || continue

  filename=$(basename "$file")
  index_name="${filename%-settings.json}"

  if curl -sf "$ES_URL/$index_name" > /dev/null 2>&1; then
    echo "Index '$index_name' already exists, skipping."
    continue
  fi

  response=$(curl -s -w "\n%{http_code}" -X PUT "$ES_URL/$index_name" \
    -H "Content-Type: application/json" -d @"$file")

  http_code=$(echo "$response" | tail -1)
  body_resp=$(echo "$response" | sed '$d')

  if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
    echo "Index '$index_name' created successfully."
  else
    echo "Failed to create index '$index_name' (HTTP $http_code): $body_resp"
  fi
done

echo "Index initialization complete."
