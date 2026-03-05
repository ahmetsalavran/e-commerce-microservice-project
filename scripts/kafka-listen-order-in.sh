#!/usr/bin/env bash
set -euo pipefail

BROKER="${BROKER:-kafka:9092}"
CONSUMER_GROUP="${CONSUMER_GROUP:-debug-order-in-$(date +%s)}"

echo "Listening order input topics with group: ${CONSUMER_GROUP}"
echo "Topics: order.confirmed, order.confirmed.partitioned"
echo

docker compose exec -T kafka kafka-console-consumer \
  --bootstrap-server "${BROKER}" \
  --group "${CONSUMER_GROUP}" \
  --include "order\\.confirmed|order\\.confirmed\\.partitioned" \
  --from-beginning \
  --property print.timestamp=true \
  --property print.partition=true \
  --property print.offset=true \
  --property print.key=true \
  --property key.separator=" | "
