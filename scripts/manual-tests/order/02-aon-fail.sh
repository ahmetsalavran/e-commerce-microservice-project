#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/../_common.sh"

require_base_tools
CUSTOMER_ID="${CUSTOMER_ID:-1001}"
PAYMENT_STRATEGY="${PAYMENT_STRATEGY:-LOCAL_ONLY}"

row="$(pick_product_lowest)"
IFS='|' read -r pid av <<< "$row"
qty=$((av + 1))

ids="$(new_ids aon-fail)"
IFS='|' read -r order_id event_id <<< "$ids"

items="[{\"productId\":${pid},\"qty\":${qty}}]"
publish_order_event "$event_id" "$order_id" "$CUSTOMER_ID" "ALL_OR_NOTHING" "$PAYMENT_STRATEGY" "$items"
echo "productId=${pid} available=${av} requestedQty=${qty}"
