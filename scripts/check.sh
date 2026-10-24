#!/usr/bin/env bash
set -euo pipefail
curl -fsS http://localhost:8080/api/products
printf '
'
curl -fsS http://localhost:8888/product-service/default
printf '
'
curl -fsS -X POST http://localhost:8080/api/orders -H 'Content-Type: application/json' -d '{"productId":1,"quantity":2}'
printf '
'
curl -fsS http://localhost:8080/api/orders
printf '
'
