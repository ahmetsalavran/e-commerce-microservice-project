#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/../_common.sh"

CUSTOMER_ID="${1:-1002}"

psql_payment "select ${CUSTOMER_ID}::bigint as customer_id, coalesce(sum(amount),0) as balance from payment_account_event where customer_id=${CUSTOMER_ID};"
psql_payment "select id, customer_id, order_id, correlation_id, event_type, amount, created_at from payment_account_event where customer_id=${CUSTOMER_ID} order by id desc limit 20;"
