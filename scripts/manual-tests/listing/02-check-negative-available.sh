#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/../_common.sh"

psql_listing "select product_id, available, updated_at from products where available < 0 order by product_id;"
