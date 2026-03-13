#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/../_common.sh"

require_base_tools
CUSTOMER_ID="${CUSTOMER_ID:-1002}"
PAYMENT_STRATEGY="${PAYMENT_STRATEGY:-LOCAL_ONLY}"

row="$(pick_product_ge 2)"
IFS='|' read -r pid av <<< "$row"

ids="$(new_ids partitioned-success)"
IFS='|' read -r order_id event_id <<< "$ids"

items="[{\"productId\":${pid},\"qty\":1}]"
publish_order_event "$event_id" "$order_id" "$CUSTOMER_ID" "PARTITIONED_BEST_EFFORT" "$PAYMENT_STRATEGY" "$items"
echo "productId=${pid} available=${av}"
