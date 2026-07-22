#!/bin/sh
set -e

ENDPOINT="http://s3:4566"
BUCKETS="images rcrs-audio rcrs-upload"
QUEUE="rcrs-upload-event-queue"
REGION="eu-west-1"

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
QUEUE_URL=$(aws --endpoint-url="$ENDPOINT" sqs create-queue --queue-name "$QUEUE" --region "$REGION" --output text --query QueueUrl)
echo "SQS queue '$QUEUE' created: $QUEUE_URL"

echo "Setting SQS queue policy..."
aws --endpoint-url="$ENDPOINT" sqs set-queue-attributes \
  --queue-url "$QUEUE_URL" \
  --attributes Policy=file:///scripts/queue-policy.json \
  --region "$REGION"
echo "SQS queue policy applied."

echo "Configuring S3 bucket notification..."
aws --endpoint-url="$ENDPOINT" s3api put-bucket-notification-configuration \
  --bucket rcrs-upload \
  --notification-configuration file:///scripts/notification.json \
  --region "$REGION"
echo "S3 bucket notification configured."

echo "S3 initialization complete."
