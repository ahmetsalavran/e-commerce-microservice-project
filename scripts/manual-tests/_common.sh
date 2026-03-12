#!/usr/bin/env bash
set -euo pipefail

BROKER="${BROKER:-localhost:29092}"
ORDER_TOPIC="${ORDER_TOPIC:-order.confirmed}"

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing command: $1" >&2
    exit 1
  fi
}

require_base_tools() {
  require_cmd docker
  require_cmd kcat
  require_cmd jq
}

psql_inventory_at() {
  docker compose exec -T inventory-db psql -U postgres -d inventorydb -At -F '|' -c "$1"
}

psql_inventory() {
  docker compose exec -T inventory-db psql -U postgres -d inventorydb -P pager=off -c "$1"
}

psql_order() {
  docker compose exec -T order-db psql -U postgres -d orderdb -P pager=off -c "$1"
}

psql_payment() {
  docker compose exec -T payment-db psql -U postgres -d paymentdb -P pager=off -c "$1"
}

psql_listing() {
  docker compose exec -T product-listing-db psql -U postgres -d listingdb -P pager=off -c "$1"
}

psql_user() {
  docker compose exec -T user-db psql -U postgres -d userdb -P pager=off -c "$1"
}

pick_product_ge() {
  local min_stock="$1"
  psql_inventory_at "select product_id, available from product_stock where available >= ${min_stock} order by available desc, product_id limit 1;"
}

pick_product_lowest() {
  psql_inventory_at "select product_id, available from product_stock order by available asc, product_id limit 1;"
}

pick_product_zero() {
  psql_inventory_at "select product_id, available from product_stock where available = 0 order by product_id limit 1;"
}

new_ids() {
  local prefix="$1"
  local ts rand order_id event_id
  ts="$(date +%s)"
  rand="$RANDOM"
  order_id="$((700000 + (ts % 100000) + rand % 1000))"
  event_id="manual-${prefix}-${ts}-${rand}"
  echo "${order_id}|${event_id}"
}

publish_order_event() {
  local event_id="$1"
  local order_id="$2"
  local customer_id="$3"
  local order_type="$4"
  local payment_strategy="$5"
  local items_json="$6"
  local now_utc

  now_utc="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"

  jq -nc \
    --arg eventId "${event_id}" \
    --argjson orderId "${order_id}" \
    --argjson customerId "${customer_id}" \
    --arg occurredAt "${now_utc}" \
    --arg orderType "${order_type}" \
    --arg paymentStrategy "${payment_strategy}" \
    --argjson items "${items_json}" \
    '{
      eventId: $eventId,
      orderId: $orderId,
      customerId: $customerId,
      occurredAt: $occurredAt,
      orderType: $orderType,
      paymentStrategy: $paymentStrategy,
      items: $items,
      totalPrice: 0
    }' \
  | kcat -b "${BROKER}" -t "${ORDER_TOPIC}" -P -k "${order_id}"

  echo "Published -> topic=${ORDER_TOPIC} key=${order_id}"
  echo "orderId=${order_id}"
  echo "eventId=${event_id}"
}
