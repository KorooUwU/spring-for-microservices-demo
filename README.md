# Spring for Microservices – Shop Demo

Demo dành cho bài thuyết trình **Spring for Microservices**, bám theo mô hình Shop trong slide: Spring Boot + Spring Cloud Config, Eureka, Gateway, OpenFeign, Resilience4j.

## Stack
- Java 21, Spring Boot **3.5.7**, Spring Cloud **2025.0.1** (Northfields)
- Maven (Docker build tự dùng Maven), Docker Desktop + Docker Compose
- H2 in-memory tách riêng trong Product/Order Service; dữ liệu được seed tự động. Đây là demo học tập, không lưu lâu dài sau restart.

## Sơ đồ
```text
                   :8080
Client --> API Gateway ----------------> Product Service :8081 --> H2(Product)
                |           (lb/Eureka)
                +----------------------> Order Service   :8082 --> H2(Order)
                                            | OpenFeign + Circuit Breaker
                                            +------------------> Product Service
Config Server :8888 --> đọc ./config-repo
Eureka Server :8761 --> đăng ký/tìm dịch vụ
```

## Chạy bằng Docker (Windows/macOS/Linux)
Tại thư mục chứa `compose.yaml`:
```bash
docker compose up --build -d --wait --wait-timeout 300
docker compose ps
docker compose logs -f api-gateway
```
Build lần đầu cần mạng để tải image và dependencies. Mỗi container có healthcheck; Compose chờ các dependency đạt `service_healthy` rồi mới khởi động service tiếp theo. Config Server được kiểm tra qua endpoint cấp cấu hình `/product-service/default`; các service còn lại dùng `/actuator/health`. Image runtime cài curl để chạy healthcheck. Lệnh `--wait` chờ toàn bộ container healthy. Eureka vẫn cần thời gian đồng bộ registry, vì vậy nếu request đầu tiên trả 503, đợi vài giây rồi thử lại. Xem `docker compose ps` và `docker compose logs` nếu có service không healthy.

### Đường dẫn kiểm tra
- Eureka Dashboard: http://localhost:8761
- Config Server: http://localhost:8888/product-service/default
- Gateway: http://localhost:8080/api/products
- Config value: http://localhost:8080/api/products/meta/banner
- Orders: http://localhost:8080/api/orders
- Health: http://localhost:8080/actuator/health

### Demo API bằng PowerShell
```powershell
Invoke-RestMethod http://localhost:8080/api/products
Invoke-RestMethod http://localhost:8080/api/products/1
Invoke-RestMethod http://localhost:8080/api/products/meta/banner
Invoke-RestMethod -Method POST http://localhost:8080/api/orders -ContentType 'application/json' -Body '{"productId":1,"quantity":2}'
Invoke-RestMethod http://localhost:8080/api/orders
```
Hoặc chạy `./scripts/demo.ps1` trong PowerShell.

### Demo Circuit Breaker (quan trọng khi báo cáo)
1. Gọi `POST http://localhost:8080/api/orders` để tạo đơn thành công.
2. Dừng Product: `docker compose stop product-service`.
3. Gọi lại POST nhiều lần cùng JSON. Order Service trả **HTTP 503**, thông báo Product Service không sẵn sàng, **không tạo đơn giả với giá 0**.
4. Chờ tối thiểu 10 giây sau khi circuit mở rồi chạy `docker compose start product-service`.
5. Đợi Eureka cập nhật, gọi lại để thấy Order Service phục hồi. Thời điểm phục hồi phụ thuộc phát hiện trạng thái service/registry.

PowerShell: `try { Invoke-RestMethod -Method POST http://localhost:8080/api/orders -ContentType 'application/json' -Body '{"productId":1,"quantity":2}' } catch { $_.Exception.Response.StatusCode }`.

### Phân biệt lỗi sản phẩm
Khi Product Service hoạt động, tạo đơn với `productId` không tồn tại trả **404**, không lưu đơn và không tính vào số lỗi của circuit breaker. Lỗi kết nối, timeout hoặc lỗi máy chủ Product trả **503**, không lưu đơn và có thể làm mở circuit breaker. Khi circuit đang OPEN, request bị từ chối ngay bằng 503, chưa thể kiểm tra sản phẩm có tồn tại hay không.

### Cấu hình tập trung
`config-server` chạy Spring Cloud Config **native backend** đọc file trong `config-repo/` (không yêu cầu Git, thuận tiện chạy offline sau build). Trên slide có thể giải thích rằng môi trường thực thường dùng Git backend. `product-service.yml` có `shop.banner`, xem bằng `/api/products/meta/banner`. Sau khi đổi cấu hình, cần khởi động lại product-service để đọc cấu hình mới; demo này **không** bật `/actuator/refresh`.

Cả Product, Order và Gateway đều dùng `spring.config.import: configserver:...` bắt buộc. Nếu Config Server không truy cập được lúc khởi động, ứng dụng sẽ không khởi động thành công thay vì âm thầm chạy cấu hình mặc định. Khi chạy trực tiếp bằng Java/Maven, cần khởi động Config Server trước; trong Docker, Compose chờ healthcheck và chính sách restart cho phép thử khởi động lại nếu gặp lỗi tạm thời.

### Độc lập dữ liệu
`product-service` lưu bảng `products` trong H2(Product), `order-service` lưu `shop_orders` trong H2(Order). Order chỉ giao tiếp Product qua HTTP/OpenFeign; không truy vấn database của Product.

### Kiểm thử Order Service
Chạy `mvn test` trong thư mục `order-service`. Test dùng H2 và mock ProductClient, kiểm tra 404 không mở mạch, 503 làm mở mạch, không lưu đơn khi lỗi và tạo đơn thành công sau các lần 404. Config Server và Eureka được tắt riêng trong test; cấu hình chạy thật vẫn bắt buộc Config Server.

### Demo script
- PowerShell: `scripts/demo.ps1`
- Bash: `bash scripts/check.sh`

### Tắt, dọn
```bash
docker compose down
```

## Ghi chú
- Đây là **mã nguồn tạo mới** để minh họa kiến trúc từ PPTX, không phải bản sao project trong sách.
- Không có xác thực người dùng, event-driven messaging hoặc transaction phân tán. Không dùng nguyên trạng cho production.
- Spring Cloud và Spring Boot phải chọn release tương thích; xem https://spring.io/projects/spring-cloud/
- Gateway dùng `spring.cloud.gateway.server.webflux.routes` phù hợp Spring Cloud Gateway mới.
- Đã kiểm tra build Docker cho 5 service, healthcheck, nạp banner từ Config Server, tạo đơn, 404 với ID không tồn tại, 503 khi Product dừng và không lưu đơn khi lỗi. Hai test Order Service đã chạy thành công trong Docker. Product cũng đã được kiểm tra từ chối khởi động khi không kết nối được Config Server.
