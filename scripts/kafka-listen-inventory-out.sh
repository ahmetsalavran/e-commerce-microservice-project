#!/usr/bin/env bash
set -euo pipefail

BROKER="${BROKER:-kafka:9092}"
CONSUMER_GROUP="${CONSUMER_GROUP:-debug-inventory-out-$(date +%s)}"

echo "Listening inventory output topics with group: ${CONSUMER_GROUP}"
echo "Topics: inventory.confirmed, inventory.rejected"
echo

docker compose exec -T kafka kafka-console-consumer \
  --bootstrap-server "${BROKER}" \
  --group "${CONSUMER_GROUP}" \
  --include "inventory\\.confirmed|inventory\\.rejected" \
  --from-beginning \
  --property print.timestamp=true \
  --property print.partition=true \
  --property print.offset=true \
  --property print.key=true \
  --property key.separator=" | "
