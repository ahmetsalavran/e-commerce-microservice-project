#!/usr/bin/env bash
set -euo pipefail

docker compose exec -T kafka kafka-topics --bootstrap-server kafka:9092 --list | sort
