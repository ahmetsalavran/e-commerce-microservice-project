#!/usr/bin/env bash
set -e

echo "Building all modules..."
mvn -DskipTests clean install
echo "DONE."