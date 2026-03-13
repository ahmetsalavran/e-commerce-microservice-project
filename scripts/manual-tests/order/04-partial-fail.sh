#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/../_common.sh"

require_base_tools
CUSTOMER_ID="${CUSTOMER_ID:-1002}"
PAYMENT_STRATEGY="${PAYMENT_STRATEGY:-LOCAL_ONLY}"

row="$(pick_product_zero || true)"
if [ -z "${row}" ]; then
  echo "No zero-stock product found, cannot force PARTIAL fail." >&2
  exit 1
fi
IFS='|' read -r pid av <<< "$row"

ids="$(new_ids partial-fail)"
IFS='|' read -r order_id event_id <<< "$ids"

items="[{\"productId\":${pid},\"qty\":1}]"
publish_order_event "$event_id" "$order_id" "$CUSTOMER_ID" "PARTIAL_OK_BUT_NOT_ZERO" "$PAYMENT_STRATEGY" "$items"
echo "productId=${pid} available=${av}"
