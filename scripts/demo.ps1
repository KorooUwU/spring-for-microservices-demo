$ErrorActionPreference = 'Stop'
Write-Host '1) Danh sach san pham'
Invoke-RestMethod http://127.0.0.1:8080/api/products | ConvertTo-Json
Write-Host '2) Config Server'
Invoke-RestMethod http://127.0.0.1:8888/product-service/default | ConvertTo-Json -Depth 6
Write-Host '3) Tao don hang'
Invoke-RestMethod -Method POST -Uri http://127.0.0.1:8080/api/orders -ContentType 'application/json' -Body '{"productId":1,"quantity":2}' | ConvertTo-Json
Write-Host '4) Danh sach don hang'
Invoke-RestMethod http://127.0.0.1:8080/api/orders | ConvertTo-Json
Write-Host '5) Swagger UI Gateway: http://127.0.0.1:8080/swagger-ui.html'
Write-Host '6) Eureka Dashboard: http://127.0.0.1:8761'
