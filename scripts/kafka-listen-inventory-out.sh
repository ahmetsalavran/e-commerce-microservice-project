#!/usr/bin/env bash
set -euo pipefail

BROKER="${BROKER:-kafka:9092}"
CONSUMER_GROUP="${CONSUMER_GROUP:-debug-inventory-out-$(date +%s)}"

echo "Listening inventory output topics with group: ${CONSUMER_GROUP}"
echo "Topics: inventory.confirmed, inventory.rejected"
echo

docker compose exec -T kafka kafka-console-consumer \
  --bootstrap-server "${BROKER}" \
  --group "${CONSUMER_GROUP}" \
  --include "inventory\\.confirmed|inventory\\.rejected" \
  --from-beginning \
  --property print.timestamp=true \
  --property print.partition=true \
  --property print.offset=true \
  --property print.key=true \
  --property key.separator=" | " \
| while IFS= read -r line; do
    if [[ "${line}" == *" | "* ]]; then
      meta="${line%% | *}"
      payload="${line#* | }"
      printf '\033[1;36m%s\033[0m\n' "${meta}"
      if command -v jq >/dev/null 2>&1; then
        printf '%s\n' "${payload}" | jq -C . 2>/dev/null || printf '\033[0;37m%s\033[0m\n' "${payload}"
      else
        printf '\033[0;37m%s\033[0m\n' "${payload}"
      fi
      printf '\033[1;30m%s\033[0m\n' "------------------------------------------------------------"
    else
      printf '\033[0;33m%s\033[0m\n' "${line}"
    fi
  done
