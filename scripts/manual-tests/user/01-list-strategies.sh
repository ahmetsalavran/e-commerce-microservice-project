#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/../_common.sh"

psql_user "select customer_id, inventory_strategy, payment_strategy from user_profile order by customer_id;"
