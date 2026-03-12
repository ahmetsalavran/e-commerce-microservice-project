#!/usr/bin/env bash
set -euo pipefail

# Repeatable seed:
# If current balance is below target, inserts PAYMENT_TOPUP event with the delta.
# If current balance is already >= target, does nothing.

docker compose exec -T payment-db psql -U postgres -d paymentdb -v ON_ERROR_STOP=1 <<'SQL'
WITH targets(customer_id, target_balance) AS (
  VALUES
    (1001::bigint, 10000.00::numeric(19,2)),
    (1002::bigint, 10000.00::numeric(19,2)),
    (1003::bigint,  5000.00::numeric(19,2))
),
latest AS (
  SELECT
    t.customer_id,
    t.target_balance,
    COALESCE((
      SELECT sum(e.amount)
      FROM payment_account_event e
      WHERE e.customer_id = t.customer_id
    ), 0::numeric(19,2)) AS current_balance
  FROM targets t
),
topups AS (
  SELECT
    customer_id,
    target_balance,
    current_balance,
    GREATEST(target_balance - current_balance, 0::numeric(19,2)) AS topup_amount
  FROM latest
)
INSERT INTO payment_account_event(
  customer_id, order_id, correlation_id, event_type, amount, created_at
)
SELECT
  customer_id,
  NULL,
  'seed-topup-' || customer_id || '-' || extract(epoch FROM clock_timestamp())::bigint,
  'PAYMENT_TOPUP',
  topup_amount,
  now()
FROM topups
WHERE topup_amount > 0;

SELECT
  customer_id,
  (
    SELECT coalesce(sum(e.amount), 0::numeric(19,2))
    FROM payment_account_event e
    WHERE e.customer_id = t.customer_id
  ) AS latest_balance
FROM (VALUES (1001::bigint), (1002::bigint), (1003::bigint)) t(customer_id)
ORDER BY customer_id;
SQL
