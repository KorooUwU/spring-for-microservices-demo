# Kịch bản thuyết trình thực hành (khoảng 5 phút)

**0:00–0:45** — Mở sơ đồ: API Gateway là cổng vào :8080; Eureka :8761 quản lý discovery; Config Server :8888 cấp cấu hình; Order và Product có DB riêng.

**0:45–1:30** — Mở http://localhost:8761, chỉ vào tên `PRODUCT-SERVICE`, `ORDER-SERVICE`, `API-GATEWAY` đã đăng ký (nếu đã sẵn sàng).

**1:30–2:15** — Gọi GET http://localhost:8080/api/products qua Gateway, chứng minh client không cần biết port 8081.

**2:15–3:15** — Gọi POST http://localhost:8080/api/orders với JSON `{ "productId":1, "quantity":2 }`; Order Service dùng OpenFeign gọi Product Service và tính thành tiền theo giá thực tế. Gọi GET /api/orders để xem đơn.

**3:15–4:30** — `docker compose stop product-service`, thử POST lần nữa. Hệ thống trả 503 và không tạo đơn giả. Nhắc `Resilience4j` làm circuit breaker, giúp hạn chế gọi liên tục tới dependency lỗi.

**4:30–5:00** — Nêu Config Server và hạn chế: demo dùng H2 in-memory, không có bảo mật hoặc giao dịch phân tán. Khởi động lại Product Service để hệ thống phục hồi, nếu đủ thời gian.

**Câu hỏi giảng viên dễ hỏi:**
1. Eureka khác Gateway thế nào? Eureka cung cấp registry, Gateway làm định tuyến đầu vào.
2. Tại sao mỗi service có DB riêng? Giảm phụ thuộc dữ liệu trực tiếp, nhưng tăng khó khăn nhất quán phân tán.
3. Tại sao không dùng 0 làm giá fallback? Tạo đơn sai nghiệp vụ; thay bằng 503 có kiểm soát.
4. Config Server khác application.yml? Tập trung cấu hình giữa nhiều service; project demo dùng native backend, không phải Git.
5. `depends_on` đảm bảo service sẵn sàng chưa? Dạng ngắn chỉ đảm bảo thứ tự. Demo dùng healthcheck và `condition: service_healthy` để chờ dependency healthy; registry Eureka vẫn cần thời gian đồng bộ.
