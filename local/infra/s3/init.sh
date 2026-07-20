#!/bin/sh
set -e

ENDPOINT="http://s3:9000"
BUCKETS="images rcrs-audio rcrs-upload"

echo "Waiting for S3-Ninja to be ready..."
until aws --endpoint-url="$ENDPOINT" s3 ls > /dev/null 2>&1; do
  sleep 2
done
echo "S3-Ninja is ready."

for BUCKET in $BUCKETS; do
  if aws --endpoint-url="$ENDPOINT" s3 ls "s3://$BUCKET" > /dev/null 2>&1; then
    echo "Bucket '$BUCKET' already exists, skipping."
  else
    echo "Creating bucket '$BUCKET'..."
    aws --endpoint-url="$ENDPOINT" s3 mb "s3://$BUCKET"
    echo "Bucket '$BUCKET' created."
  fi
done

echo "S3 initialization complete."
