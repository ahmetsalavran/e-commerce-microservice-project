#!/usr/bin/env bash
set -euo pipefail

BROKER="${BROKER:-localhost:29092}"

kcat -b "${BROKER}" -C -o end -G debug-inventory-out-$(date +%s) inventory.rejected payment.charge.requested \
| jq -R -C .
