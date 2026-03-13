#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/../_common.sh"

if [ "$#" -eq 0 ]; then
  psql_inventory "select product_id, available, reserved, version from product_stock order by product_id limit 30;"
  exit 0
fi

csv="$(IFS=, ; echo "$*")"
psql_inventory "select product_id, available, reserved, version from product_stock where product_id in (${csv}) order by product_id;"
