#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/../_common.sh"

CUSTOMER_ID="${1:-1002}"
AMOUNT="${2:-1000.00}"

psql_payment "insert into payment_account_event(customer_id, order_id, correlation_id, event_type, amount, created_at) values (${CUSTOMER_ID}, null, 'manual-topup-${CUSTOMER_ID}-' || extract(epoch from now())::bigint, 'PAYMENT_TOPUP', ${AMOUNT}, now());"
psql_payment "select ${CUSTOMER_ID}::bigint as customer_id, coalesce(sum(amount),0) as balance from payment_account_event where customer_id=${CUSTOMER_ID};"
