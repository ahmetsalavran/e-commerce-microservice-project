#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/../_common.sh"

if [ "$#" -eq 0 ]; then
  psql_listing "select product_id, available, updated_at from products order by product_id limit 30;"
  exit 0
fi

csv="$(IFS=, ; echo "$*")"
psql_listing "select product_id, available, updated_at from products where product_id in (${csv}) order by product_id;"
