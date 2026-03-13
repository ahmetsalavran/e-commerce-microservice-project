#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/../_common.sh"

require_base_tools
CUSTOMER_ID="${CUSTOMER_ID:-1002}"
PAYMENT_STRATEGY="${PAYMENT_STRATEGY:-LOCAL_ONLY}"

row_ok="$(pick_product_ge 2)"
row_low="$(pick_product_lowest)"
IFS='|' read -r p_ok a_ok <<< "$row_ok"
IFS='|' read -r p_low a_low <<< "$row_low"
qty_low=$((a_low + 1))

ids="$(new_ids partial-success)"
IFS='|' read -r order_id event_id <<< "$ids"

items="[{\"productId\":${p_ok},\"qty\":1},{\"productId\":${p_low},\"qty\":${qty_low}}]"
publish_order_event "$event_id" "$order_id" "$CUSTOMER_ID" "PARTIAL_OK_BUT_NOT_ZERO" "$PAYMENT_STRATEGY" "$items"
echo "okProduct=${p_ok} lowProduct=${p_low} lowAvailable=${a_low} lowRequested=${qty_low}"
