#!/usr/bin/env bash
set -euo pipefail

ORDER_URL="${ORDER_URL:-http://localhost:8082}"
USER_URL="${USER_URL:-http://localhost:8086}"
WAIT_SEC="${WAIT_SEC:-3}"
VERIFY_TIMEOUT_SEC="${VERIFY_TIMEOUT_SEC:-20}"

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

require_cmd curl
require_cmd docker

title() {
  printf '\n\033[1;34m%s\033[0m\n' "$1"
}

step() {
  printf '\033[1;36m%s\033[0m\n' "$1"
}

pause() {
  read -r -p "Devam için Enter... " _
}

user_strategy() {
  local cid="$1"
  step "User strategy check customerId=${cid}"
  curl -sS -f "${USER_URL}/users/by-customer/${cid}/strategies" | sed 's/^/  /'
  echo
}

add_to_cart() {
  local cid="$1"
  local pid="$2"
  local qty="$3"
  curl -sS -f -X POST "${ORDER_URL}/addToCart" \
    -H 'Content-Type: application/json' \
    -d "{\"customerId\":${cid},\"productId\":${pid},\"qty\":${qty}}"
}

confirm_cart() {
  local oid="$1"
  curl -sS -o /dev/null -w "%{http_code}" -X POST "${ORDER_URL}/confirmCart" \
    -H 'Content-Type: application/json' \
    -d "{\"orderId\":${oid}}"
}

db_check() {
  local oid="$1"

  step "DB check orderdb (order=${oid})"
  docker compose exec -T order-db psql -U postgres -d orderdb -P pager=off -c \
    "select order_id, customer_id, status, order_type, payment_strategy, total_price from orders where order_id=${oid};"
  docker compose exec -T order-db psql -U postgres -d orderdb -P pager=off -c \
    "select order_id, product_id, qty from order_items where order_id=${oid} order by product_id;"
  docker compose exec -T order-db psql -U postgres -d orderdb -P pager=off -c \
    "select id, event_type, key, status, created_at from outbox_event where key='${oid}' order by id desc limit 5;"

  step "DB check inventorydb (order=${oid})"
  docker compose exec -T inventory-db psql -U postgres -d inventorydb -P pager=off -c \
    "select id, event_type, key, status, created_at from outbox_event where key='${oid}' order by id desc limit 5;"
}

capture_stock_snapshot() {
  local products_csv="$1"
  docker compose exec -T inventory-db psql -U postgres -d inventorydb -At -F '|' -c \
    "select product_id, available, reserved, version from product_stock where product_id in (${products_csv}) order by product_id;"
}

print_stock_snapshot() {
  local label="$1"
  local file="$2"
  step "Inventory stock ${label}"
  printf "  %-10s %-10s %-10s %-10s\n" "product_id" "available" "reserved" "version"
  awk -F'|' '{printf "  %-10s %-10s %-10s %-10s\n", $1, $2, $3, $4}' "${file}"
}

print_stock_delta() {
  local before_file="$1"
  local after_file="$2"
  step "Inventory stock DELTA (after - before)"
  printf "  %-10s %-10s %-10s\n" "product_id" "before" "after"
  awk -F'|' '
    NR==FNR { before[$1]=$2; next }
    {
      b=before[$1];
      a=$2;
      d=a-b;
      sign=(d>0?"+":"");
      printf "  %-10s %-10s %-10s (delta=%s%d)\n", $1, b, a, sign, d
    }
  ' "${before_file}" "${after_file}"
}

latest_inventory_event_type() {
  local oid="$1"
  docker compose exec -T inventory-db psql -U postgres -d inventorydb -At -c \
    "select event_type from outbox_event where key='${oid}' order by id desc limit 1;" \
    2>/dev/null | tr -d '\r'
}

verify_expected_event() {
  local oid="$1"
  local expected="$2"
  local waited=0
  local got=""

  while [ "${waited}" -lt "${VERIFY_TIMEOUT_SEC}" ]; do
    got="$(latest_inventory_event_type "${oid}")"
    if [ -n "${got}" ]; then
      break
    fi
    sleep 1
    waited=$((waited + 1))
  done

  if [ "${got}" = "${expected}" ]; then
    printf '\033[1;32mRESULT: PASS\033[0m expected=%s got=%s orderId=%s\n' "${expected}" "${got}" "${oid}"
    return 0
  fi

  printf '\033[1;31mRESULT: FAIL\033[0m expected=%s got=%s orderId=%s\n' "${expected}" "${got:-<none>}" "${oid}"
  return 1
}

run_case() {
  local name="$1"
  local cid="$2"
  local expected="$3"
  shift 3

  title "Scenario: ${name}"
  echo "customerId=${cid} expected=${expected}"
  user_strategy "${cid}"
  pause

  local oid=""
  local first=1
  local product_list=""
  local args=("$@")
  local i=0
  local before_file
  local after_file
  before_file="$(mktemp)"
  after_file="$(mktemp)"
  trap 'rm -f "${before_file}" "${after_file}"' RETURN

  while [ "${i}" -lt "${#args[@]}" ]; do
    local pid="${args[$i]}"
    if [ -z "${product_list}" ]; then
      product_list="${pid}"
    else
      product_list="${product_list},${pid}"
    fi
    i=$((i + 2))
  done

  capture_stock_snapshot "${product_list}" > "${before_file}"
  print_stock_snapshot "BEFORE" "${before_file}"
  while [ "$#" -gt 0 ]; do
    local pid="$1"
    local qty="$2"
    shift 2

    local curr_oid
    curr_oid="$(add_to_cart "${cid}" "${pid}" "${qty}")"
    if [ "${first}" -eq 1 ]; then
      oid="${curr_oid}"
      first=0
    fi
    if [ -z "${product_list}" ]; then
      product_list="${pid}"
    else
      product_list="${product_list},${pid}"
    fi
    step "addToCart productId=${pid} qty=${qty} -> orderId=${curr_oid}"
  done

  pause

  local code
  code="$(confirm_cart "${oid}")"
  step "confirmCart orderId=${oid} -> HTTP ${code}"
  sleep "${WAIT_SEC}"
  capture_stock_snapshot "${product_list}" > "${after_file}"
  print_stock_snapshot "AFTER" "${after_file}"
  print_stock_delta "${before_file}" "${after_file}"

  db_check "${oid}"
  verify_expected_event "${oid}" "${expected}" || true
  pause
}

title "Sequential Manual Test Runner"
echo "ORDER_URL=${ORDER_URL}"
echo "USER_URL=${USER_URL}"
echo "WAIT_SEC=${WAIT_SEC}"
echo "VERIFY_TIMEOUT_SEC=${VERIFY_TIMEOUT_SEC}"
echo "Her senaryoda adim adim ilerlemek icin Enter'a bas."

run_case "AON_SUCCESS" 1001 "INVENTORY_CONFIRMED" \
  3 1

run_case "AON_FAIL" 1001 "INVENTORY_REJECTED" \
  1 1

run_case "PARTITIONED_SUCCESS" 1002 "INVENTORY_CONFIRMED" \
  3 1  51 1  123 1

run_case "PARTITIONED_FAIL" 1002 "INVENTORY_REJECTED" \
  3 1  55 1  123 1

title "Done"
