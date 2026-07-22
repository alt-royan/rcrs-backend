#!/bin/sh
set -e

ENDPOINT="http://s3:4566"
BUCKETS="images rcrs-audio rcrs-upload"
QUEUE="rcrs-upload-event-queue"

echo "Waiting for LocalStack to be ready..."
until aws --endpoint-url="$ENDPOINT" s3 ls > /dev/null 2>&1; do
  sleep 2
done
echo "LocalStack is ready."

for BUCKET in $BUCKETS; do
  if aws --endpoint-url="$ENDPOINT" s3 ls "s3://$BUCKET" > /dev/null 2>&1; then
    echo "Bucket '$BUCKET' already exists, skipping."
  else
    echo "Creating bucket '$BUCKET'..."
    aws --endpoint-url="$ENDPOINT" s3 mb "s3://$BUCKET"
    echo "Bucket '$BUCKET' created."
  fi
done

echo "Creating SQS queue '$QUEUE'..."
QUEUE_URL=$(aws --endpoint-url="$ENDPOINT" sqs create-queue --queue-name "$QUEUE" | jq .QueueUrl)
echo "SQS queue '$QUEUE' created."
echo "${QUEUE_URL//\"}"

echo "Setting SQS queue policy..."
QUEUE_POLICY=./queue-policy.json
aws --endpoint-url="$ENDPOINT" sqs set-queue-attributes \
  --queue-url "${QUEUE_URL//\"}" \
  --attributes Policy="$QUEUE_POLICY"
echo "SQS queue policy applied."

echo "Configuring S3 bucket notification..."
aws --endpoint-url="$ENDPOINT" s3api put-bucket-notification-configuration \
  --bucket rcrs-upload \
  --notification-configuration file:///scripts/notification.json
echo "S3 bucket notification configured."

echo "S3 initialization complete."
