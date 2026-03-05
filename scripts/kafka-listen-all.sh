#!/usr/bin/env bash
set -euo pipefail

BROKER="${BROKER:-kafka:9092}"
FROM_BEGINNING="${FROM_BEGINNING:-true}"

if [[ "${FROM_BEGINNING}" == "true" ]]; then
  RESET_FLAG="--from-beginning"
else
  RESET_FLAG=""
fi

topics=(
  "order.confirmed"
  "order.confirmed.partitioned"
  "inventory.confirmed"
  "inventory.rejected"
)

pids=()

cleanup() {
  for pid in "${pids[@]:-}"; do
    kill "${pid}" >/dev/null 2>&1 || true
  done
}
trap cleanup EXIT INT TERM

echo "Listening all order->inventory flow topics..."
echo "Broker: ${BROKER}"
echo "From beginning: ${FROM_BEGINNING}"
echo

for topic in "${topics[@]}"; do
  group="debug-${topic//./-}-$(date +%s)"
  (
    docker compose exec -T kafka kafka-console-consumer \
      --bootstrap-server "${BROKER}" \
      --group "${group}" \
      --topic "${topic}" \
      ${RESET_FLAG} \
      --property print.timestamp=true \
      --property print.partition=true \
      --property print.offset=true \
      --property print.key=true \
      --property key.separator=" | " \
      | sed "s/^/[${topic}] /"
  ) &
  pids+=("$!")
done

wait

