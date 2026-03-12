#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/../_common.sh"

ORDER_ID="${1:-}"
if [ -z "${ORDER_ID}" ]; then
  echo "Usage: $0 <orderId>" >&2
  exit 1
fi

psql_inventory "select event_id, status, updated_at from processed_event where order_id=${ORDER_ID} order by updated_at desc limit 5;"
psql_inventory "select id, event_type, key, status, created_at from outbox_event where key='${ORDER_ID}' order by id desc limit 10;"
psql_inventory "select order_id, product_id, qty, status, created_at from inventory_reservation where order_id=${ORDER_ID} order by id;"
