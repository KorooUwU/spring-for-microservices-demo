#!/usr/bin/env bash
set -euo pipefail
curl -fsS http://127.0.0.1:8080/api/products
printf '\n'
curl -fsS http://127.0.0.1:8888/product-service/default
printf '\n'
curl -fsS -X POST http://127.0.0.1:8080/api/orders -H 'Content-Type: application/json' -d '{"productId":1,"quantity":2}'
printf '\n'
curl -fsS http://127.0.0.1:8080/api/orders
printf '\n'
