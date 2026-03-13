#!/usr/bin/env bash
set -euo pipefail

BROKER="${BROKER:-localhost:29092}"
GROUP_ID="${GROUP_ID:-debug-all-$(date +%s)}"

kcat -b "${BROKER}" -G "${GROUP_ID}" \
  order.confirmed order.confirmed.partitioned inventory.rejected payment.charge.requested inventory.compensate.requested \
  -o end -J \
| jq -C '{topic, partition, offset, key, payload: (.payload | (try fromjson catch .))}'
