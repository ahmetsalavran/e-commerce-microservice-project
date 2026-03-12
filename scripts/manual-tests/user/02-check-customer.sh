#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/../_common.sh"

CUSTOMER_ID="${1:-}"
if [ -z "${CUSTOMER_ID}" ]; then
  echo "Usage: $0 <customerId>" >&2
  exit 1
fi

psql_user "select customer_id, inventory_strategy, payment_strategy from user_profile where customer_id=${CUSTOMER_ID};"
psql_user "select customer_id, product_id, qty from user_order_info where customer_id=${CUSTOMER_ID} order by product_id;"
