# Kịch bản thuyết trình & Demo thực hành (khoảng 5 phút)

> **Lưu ý mạng Windows:** Sử dụng địa chỉ `127.0.0.1` thay cho `localhost` để tránh xung đột cổng IPv6 của WSL2.

---

### Kịch bản chi tiết từng bước (Dùng Swagger UI trực quan)

#### 1. (0:00 – 0:45) Giới thiệu kiến trúc tổng quan
* **Mở sơ đồ kiến trúc** trong `README.md`:
  * **API Gateway (`:8080`)**: Điểm vào (Single Entry Point) duy nhất cho Client.
  * **Eureka Server (`:8761`)**: Quản lý đăng ký và tìm kiếm dịch vụ (Service Discovery).
  * **Config Server (`:8888`)**: Cung cấp cấu hình tập trung cho toàn bộ service.
  * **Product Service (`:8081`)** & **Order Service (`:8082`)**: Mỗi microservice sở hữu Database H2 độc lập (Database-per-service pattern).

#### 2. (0:45 – 1:30) Kiểm tra Eureka Service Discovery
* Mở trình duyệt vào: **http://127.0.0.1:8761**
* **Điểm nhấn thuyết trình:**
  * Chỉ vào mục **"Instances currently registered with Eureka"**.
  * Giải thích: Các service `API-GATEWAY`, `PRODUCT-SERVICE`, `ORDER-SERVICE` tự động đăng ký với Eureka sau khi khởi động. Gateway và Order Service sẽ tự tìm địa chỉ của nhau thông qua Eureka mà không cần hardcode IP/port.

#### 3. (1:30 – 2:30) Demo API Gateway & Config Server qua Swagger UI
* Mở Swagger UI tập trung tại: **http://127.0.0.1:8080/swagger-ui.html**
* **Thao tác 1 (Product Service qua Gateway):**
  * Tại góc trên bên phải, mục **Select a definition**, chọn **`1. Product Service`**.
  * Bấm vào API **`GET /products`** -> bấm **Try it out** -> **Execute**.
  * Kết quả: Trả về HTTP 200 kèm danh sách 3 sản phẩm (Laptop $1200, Keyboard $75, Mouse $25).
  * *Điểm nhấn:* Client gọi qua cổng Gateway `:8080`, Gateway tự định tuyến tới port nội bộ `:8081`.
* **Thao tác 2 (Spring Cloud Config):**
  * Bấm vào API **`GET /products/meta/banner`** -> **Try it out** -> **Execute**.
  * Kết quả: Trả về `{"banner": "Spring Cloud Config works!"}`.
  * *Điểm nhấn:* Giá trị banner này được nạp động từ file `config-repo/product-service.yml` thông qua Config Server.

#### 4. (2:30 – 3:30) Demo Inter-service Communication (OpenFeign)
* Tại mục **Select a definition**, chuyển sang **`2. Order Service`**.
* Mở API **`POST /orders`** (Tạo đơn hàng):
  * Bấm **Try it out**.
  * Dữ liệu mẫu đã có sẵn:
    ```json
    {
      "productId": 1,
      "quantity": 2
    }
    ```
  * Bấm **Execute**.
  * Kết quả: HTTP 201 Created:
    ```json
    {
      "id": 1,
      "productId": 1,
      "productName": "Laptop",
      "quantity": 2,
      "unitPrice": 1200.00,
      "totalPrice": 2400.00
    }
    ```
  * *Điểm nhấn:* Order Service không tự có giá và tên sản phẩm; nó đã dùng **Spring Cloud OpenFeign** gọi sang Product Service, lấy giá 1200, nhân với số lượng 2 để tính `totalPrice = 2400.00` rồi lưu vào DB riêng của Order.
* Bấm vào **`GET /orders`** -> **Execute** để xem đơn hàng đã lưu thành công trong DB của Order.

#### 5. (3:30 – 4:30) Demo Circuit Breaker (Resilience4j) & Khả năng chịu lỗi
* **Bước 1 (Mô phỏng sự cố):**
  Mở terminal, tắt Product Service:
  ```bash
  docker compose stop product-service
  ```
* **Bước 2 (Gọi lại trên Swagger UI):**
  * Quay lại Swagger UI tại `POST /orders`, nhấn **Execute** lại một lần nữa.
  * **Kết quả:** Trả về ngay lập tức **HTTP 503 Service Unavailable**:
    `"Product service unavailable; no order was created"`
  * *Điểm nhấn:*
    * Nhờ **Resilience4j Circuit Breaker**, khi dependency bị lỗi, hệ thống ngắt mạch an toàn, trả về lỗi 503 có kiểm soát, **không tạo đơn hàng rác với giá 0**.
    * Gọi tiếp `GET /orders` để chứng minh: Số lượng đơn hàng vẫn giữ nguyên, không có đơn hàng hỏng nào bị lưu vào database.
* **Bước 3 (Khôi phục hệ thống):**
  * Chạy lệnh khôi phục:
    ```bash
    docker compose start product-service
    ```
  * Đợi vài giây để Eureka cập nhật trạng thái UP.
  * Bấm **Execute** lại `POST /orders` trên Swagger UI -> Hệ thống phục hồi, trả về HTTP 201 bình thường!

#### 6. (4:30 – 5:00) Kết luận & Trả lời câu hỏi
* Tổng kết các ưu điểm kiến trúc: Phân tách dữ liệu (Database-per-service), định tuyến thông minh (Gateway), chịu lỗi phân tán (Circuit Breaker), cấu hình tập trung (Config Server).

---

### Câu hỏi giảng viên dễ hỏi & Gợi ý trả lời:
1. **Eureka khác API Gateway như thế nào?**
   * *Trả lời:* Eureka là **Service Registry** (danh bạ điện thoại, giúp các service biết nhau đang ở IP/port nào). Gateway là **Reverse Proxy / Entry Point** (bảo vệ mạng nội bộ, định tuyến request từ bên ngoài vào đúng service).
2. **Tại sao mỗi service lại dùng Database riêng (H2 riêng)?**
   * *Trả lời:* Để đảm bảo tính độc lập và lỏng lẻo (loose coupling). Khi Product Service sửa đổi cấu trúc bảng hay đổi sang MongoDB/PostgreSQL, Order Service hoàn toàn không bị ảnh hưởng.
3. **Tại sao không dùng giá 0 hoặc giá mặc định khi Product Service lỗi?**
   * *Trả lời:* Dùng giá 0 sẽ tạo ra đơn hàng miễn phí sai nghiệp vụ nghiêm trọng. Dùng 503 có kiểm soát để báo cho người dùng thử lại là phương án an toàn nhất trong nghiệp vụ bán hàng.
4. **Config Server khác gì file `application.yml` thông thường?**
   * *Trả lời:* Config Server quản lý tập trung ở một nơi, giúp quản trị viên có thể thay đổi cấu hình hàng loạt service mà không cần đóng gói lại mã nguồn JAR của từng service.
