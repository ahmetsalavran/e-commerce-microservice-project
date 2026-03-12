#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/../_common.sh"

require_base_tools
CUSTOMER_ID="${CUSTOMER_ID:-1001}"
PAYMENT_STRATEGY="${PAYMENT_STRATEGY:-LOCAL_ONLY}"

row="$(pick_product_ge 2)"
if [ -z "$row" ]; then
  echo "No product with available>=2" >&2
  exit 1
fi
IFS='|' read -r pid av <<< "$row"

ids="$(new_ids aon-success)"
IFS='|' read -r order_id event_id <<< "$ids"

items="[{\"productId\":${pid},\"qty\":1}]"
publish_order_event "$event_id" "$order_id" "$CUSTOMER_ID" "ALL_OR_NOTHING" "$PAYMENT_STRATEGY" "$items"
echo "productId=${pid} available=${av}"
