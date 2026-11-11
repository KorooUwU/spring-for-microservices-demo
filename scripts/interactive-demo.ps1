# Interactive Demo Script cho PowerShell
# Giúp biểu diễn các endpoint Microservices trực tiếp qua Terminal nếu muốn

function Show-Menu {
    Clear-Host
    Write-Host "==========================================================" -ForegroundColor Cyan
    Write-Host "      DEMO SPRING MICROSERVICES CLI CONTROLLER            " -ForegroundColor Yellow
    Write-Host "==========================================================" -ForegroundColor Cyan
    Write-Host "1. Xem danh sach san pham (Gateway -> Product Service)"
    Write-Host "2. Xem banner cau hinh tu Config Server"
    Write-Host "3. Tao don hang moi (Order Service Feign -> Product Service)"
    Write-Host "4. Xem danh sach tat ca don hang da luu (H2 DB cua Order)"
    Write-Host "5. Test tao don voi san pham khong ton tai (Mong doi 404)"
    Write-Host "6. Kiem tra Healthcheck toan bo he thong (Actuator)"
    Write-Host "7. Mo Eureka Dashboard tren trinh duyet"
    Write-Host "8. Mo Swagger UI tap trung tren trinh duyet"
    Write-Host "9. Mo Web Dashboard Next.js (http://localhost:3000)"
    Write-Host "0. Thoat"
    Write-Host "==========================================================" -ForegroundColor Cyan
}

do {
    Show-Menu
    $choice = Read-Host "Nhap lua chon cua ban (0-9)"
    switch ($choice) {
        "1" {
            Write-Host "`n[GET] http://127.0.0.1:8080/api/products" -ForegroundColor Green
            try {
                $res = Invoke-RestMethod -Uri "http://127.0.0.1:8080/api/products"
                $res | ConvertTo-Json -Depth 5
            } catch {
                Write-Host "Loi: $($_.Exception.Message)" -ForegroundColor Red
            }
            Pause
        }
        "2" {
            Write-Host "`n[GET] http://127.0.0.1:8080/api/products/meta/banner" -ForegroundColor Green
            try {
                $res = Invoke-RestMethod -Uri "http://127.0.0.1:8080/api/products/meta/banner"
                $res | ConvertTo-Json
            } catch {
                Write-Host "Loi: $($_.Exception.Message)" -ForegroundColor Red
            }
            Pause
        }
        "3" {
            Write-Host "`n[POST] http://127.0.0.1:8080/api/orders (Body: productId=1, quantity=2)" -ForegroundColor Green
            try {
                $body = @{ productId = 1; quantity = 2 } | ConvertTo-Json
                $res = Invoke-RestMethod -Method POST -Uri "http://127.0.0.1:8080/api/orders" -ContentType "application/json" -Body $body
                Write-Host "Tao don thanh cong:" -ForegroundColor Green
                $res | ConvertTo-Json
            } catch {
                Write-Host "Loi $($_.Exception.Response.StatusCode): $($_.Exception.Message)" -ForegroundColor Red
            }
            Pause
        }
        "4" {
            Write-Host "`n[GET] http://127.0.0.1:8080/api/orders" -ForegroundColor Green
            try {
                $res = Invoke-RestMethod -Uri "http://127.0.0.1:8080/api/orders"
                $res | ConvertTo-Json -Depth 5
            } catch {
                Write-Host "Loi: $($_.Exception.Message)" -ForegroundColor Red
            }
            Pause
        }
        "5" {
            Write-Host "`n[POST] http://127.0.0.1:8080/api/orders (Body: productId=999, quantity=1)" -ForegroundColor Yellow
            try {
                $body = @{ productId = 999; quantity = 1 } | ConvertTo-Json
                $res = Invoke-RestMethod -Method POST -Uri "http://127.0.0.1:8080/api/orders" -ContentType "application/json" -Body $body
                $res | ConvertTo-Json
            } catch {
                Write-Host "Ket qua mong doi: HTTP 404 (Feign bat loi, khong ngat mach)" -ForegroundColor Magenta
                Write-Host "Chi tiet loi: $($_.Exception.Message)" -ForegroundColor Yellow
            }
            Pause
        }
        "6" {
            Write-Host "`n[GET] http://127.0.0.1:8080/actuator/health" -ForegroundColor Green
            try {
                $res = Invoke-RestMethod -Uri "http://127.0.0.1:8080/actuator/health"
                $res | ConvertTo-Json
            } catch {
                Write-Host "Loi: $($_.Exception.Message)" -ForegroundColor Red
            }
            Pause
        }
        "7" {
            Start-Process "http://127.0.0.1:8761"
        }
        "8" {
            Start-Process "http://127.0.0.1:8080/swagger-ui.html"
        }
        "9" {
            Start-Process "http://localhost:3000"
        }
    }
} while ($choice -ne "0")

