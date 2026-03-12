#!/usr/bin/env bash
set -euo pipefail

ORDER_URL="${ORDER_URL:-http://localhost:8082}"
USER_URL="${USER_URL:-http://localhost:8086}"
TIMEOUT_SEC="${TIMEOUT_SEC:-20}"
POLL_SEC="${POLL_SEC:-1}"

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

require_cmd curl
require_cmd docker

add_to_cart() {
  local customer_id="$1"
  local product_id="$2"
  local qty="$3"
  curl -sS -f -X POST "${ORDER_URL}/addToCart" \
    -H 'Content-Type: application/json' \
    -d "{\"customerId\":${customer_id},\"productId\":${product_id},\"qty\":${qty}}"
}

confirm_cart() {
  local order_id="$1"
  local code
  code="$(curl -sS -o /dev/null -w "%{http_code}" -X POST "${ORDER_URL}/confirmCart" \
    -H 'Content-Type: application/json' \
    -d "{\"orderId\":${order_id}}")"
  echo "${code}"
}

latest_inventory_event_type_for_order() {
  local order_id="$1"
  docker compose exec -T inventory-db psql -U postgres -d inventorydb -At -c \
    "select event_type from outbox_event where key='${order_id}' order by id desc limit 1;" \
    2>/dev/null || true
}

wait_for_inventory_event() {
  local order_id="$1"
  local expected="$2"
  local waited=0
  local got=""

  while [ "${waited}" -lt "${TIMEOUT_SEC}" ]; do
    got="$(latest_inventory_event_type_for_order "${order_id}" | tr -d '\r')"
    if [ "${got}" = "${expected}" ]; then
      echo "PASS orderId=${order_id} expected=${expected} got=${got}"
      return 0
    fi
    sleep "${POLL_SEC}"
    waited=$((waited + POLL_SEC))
  done

  echo "FAIL orderId=${order_id} expected=${expected} got=${got:-<none>}" >&2
  return 1
}

print_strategy() {
  local customer_id="$1"
  local body
  body="$(curl -sS -f "${USER_URL}/users/by-customer/${customer_id}/strategies")"
  echo "customer=${customer_id} strategy=${body}"
}

run_scenario() {
  local name="$1"
  local customer_id="$2"
  local expected_event="$3"
  shift 3

  echo
  echo "=== ${name} ==="
  echo "customerId=${customer_id} expectedInventoryEvent=${expected_event}"

  local first=1
  local oid=""
  while [ "$#" -gt 0 ]; do
    local product_id="$1"
    local qty="$2"
    shift 2

    local current_oid
    current_oid="$(add_to_cart "${customer_id}" "${product_id}" "${qty}")"
    if [ "${first}" -eq 1 ]; then
      oid="${current_oid}"
      first=0
    fi
    echo "addToCart productId=${product_id} qty=${qty} -> orderId=${current_oid}"
  done

  local http_code
  http_code="$(confirm_cart "${oid}")"
  echo "confirmCart orderId=${oid} -> HTTP ${http_code}"
  if [ "${http_code}" != "200" ]; then
    echo "FAIL orderId=${oid} confirmCart HTTP ${http_code}" >&2
    return 1
  fi

  wait_for_inventory_event "${oid}" "${expected_event}"
}

echo "Order URL: ${ORDER_URL}"
echo "User URL:  ${USER_URL}"
echo "Timeout:   ${TIMEOUT_SEC}s"

print_strategy 1001
print_strategy 1002

# Scenario 1: ALL_OR_NOTHING success (product 3 has stock)
run_scenario "AON_SUCCESS" 1001 "INVENTORY_CONFIRMED" \
  3 1

# Scenario 2: ALL_OR_NOTHING fail (product 1 stock is 0)
run_scenario "AON_FAIL" 1001 "INVENTORY_REJECTED" \
  1 1

# Scenario 3: PARTITIONED_BEST_EFFORT success (segments 0,1,2 with stocked products)
run_scenario "PARTITIONED_SUCCESS" 1002 "INVENTORY_CONFIRMED" \
  3 1  51 1  123 1

# Scenario 4: PARTITIONED_BEST_EFFORT fail (segment 1 includes zero-stock product 55)
run_scenario "PARTITIONED_FAIL" 1002 "INVENTORY_REJECTED" \
  3 1  55 1  123 1

echo
echo "All scenarios finished."
