#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/../_common.sh"

EVENT_ID="${1:-}"
if [ -z "${EVENT_ID}" ]; then
  echo "Usage: $0 <eventId>" >&2
  exit 1
fi

psql_payment "select event_id, event_type, order_id, status, created_at, updated_at from processed_event where event_id='${EVENT_ID}';"
psql_payment "select id, customer_id, order_id, correlation_id, event_type, amount, created_at from payment_account_event where correlation_id='${EVENT_ID}' order by id desc;"
