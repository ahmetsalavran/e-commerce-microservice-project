#!/usr/bin/env bash
set -euo pipefail

BROKER="${BROKER:-kafka:9092}"
TOPIC="${TOPIC:-inventory.confirmed}"

ORDER_ID="${ORDER_ID:-90001}"
CUSTOMER_ID="${CUSTOMER_ID:-1002}"
STRATEGY_KEY="${STRATEGY_KEY:-ALL_OR_NOTHING}"
PAYMENT_STRATEGY="${PAYMENT_STRATEGY:-LOCAL_ONLY}" # LOCAL_ONLY | THIRD_PARTY_ONLY
TOTAL_PRICE="${TOTAL_PRICE:-150.00}"
PRODUCT_ID="${PRODUCT_ID:-1}"
REQUESTED_QTY="${REQUESTED_QTY:-1}"
DEDUCTED_QTY="${DEDUCTED_QTY:-1}"

EVENT_ID="${EVENT_ID:-manual-$(date +%s)-$RANDOM}"
DECIDED_AT="${DECIDED_AT:-$(date -u +"%Y-%m-%dT%H:%M:%SZ")}"
KEY="${KEY:-${ORDER_ID}}"

PAYLOAD=$(
  cat <<JSON
{"eventId":"${EVENT_ID}","orderId":${ORDER_ID},"customerId":${CUSTOMER_ID},"strategyKey":"${STRATEGY_KEY}","paymentStrategy":"${PAYMENT_STRATEGY}","success":true,"message":"MANUAL_INVENTORY_CONFIRMED","items":[{"productId":${PRODUCT_ID},"requestedQty":${REQUESTED_QTY},"deductedQty":${DEDUCTED_QTY}}],"decidedAt":"${DECIDED_AT}","totalPrice":${TOTAL_PRICE}}
JSON
)

echo "Producing to topic=${TOPIC} broker=${BROKER}"
echo "key=${KEY}"
echo "payload=${PAYLOAD}"
echo

printf '%s|%s\n' "${KEY}" "${PAYLOAD}" | docker compose exec -T kafka kafka-console-producer \
  --bootstrap-server "${BROKER}" \
  --topic "${TOPIC}" \
  --property parse.key=true \
  --property key.separator='|'

echo "Done."
