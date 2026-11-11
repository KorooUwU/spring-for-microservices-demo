package com.demo.shop.order;

import feign.FeignException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/orders")
@Tag(name = "Order Controller", description = "Các API quản lý và tạo đơn hàng liên dịch vụ")
public class OrderController {

    public record CreateOrderRequest(
        @Schema(description = "ID của sản phẩm trong kho (ví dụ: 1 cho Laptop, 2 cho Keyboard)", example = "1")
        Long productId,
        @Schema(description = "Số lượng đặt mua (từ 1 đến 100)", example = "2")
        Integer quantity
    ) {}

    private final OrderRepository orders;
    private final ProductClient products;
    private final CircuitBreakerFactory<?, ?> breakers;

    public OrderController(OrderRepository orders, ProductClient products, CircuitBreakerFactory<?, ?> breakers) {
        this.orders = orders;
        this.products = products;
        this.breakers = breakers;
    }

    @GetMapping
    @Operation(summary = "Xem danh sách các đơn hàng đã tạo", description = "Lấy toàn bộ đơn hàng lưu trong H2 Database độc lập của Order Service.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    public List<ShopOrder> all() {
        return orders.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết 1 đơn hàng theo ID", description = "Tìm kiếm thông tin đơn hàng theo ID.")
    @ApiResponse(responseCode = "200", description = "Tìm thấy đơn hàng")
    @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng")
    public ShopOrder one(@Parameter(description = "ID của đơn hàng", example = "1") @PathVariable Long id) {
        return orders.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Tạo đơn hàng mới (Demo OpenFeign + Circuit Breaker)",
        description = "Order Service gọi OpenFeign sang Product Service lấy đơn giá. Nếu Product Service bị ngắt (stop), Circuit Breaker Resilience4j sẽ kích hoạt và trả về 503 Service Unavailable, ngăn chặn tạo đơn hàng sai lệch."
    )
    @ApiResponse(responseCode = "201", description = "Đơn hàng được tạo thành công")
    @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ (thiếu productId hoặc quantity không hợp lệ)")
    @ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm trong kho (không tạo đơn)")
    @ApiResponse(responseCode = "503", description = "Product Service không sẵn sàng (Circuit Breaker kích hoạt)")
    public ShopOrder create(@RequestBody CreateOrderRequest input) {
        if (input.productId() == null || input.quantity() == null || input.quantity() < 1 || input.quantity() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId and quantity (1..100) required");
        }
        ProductDto product = (ProductDto) breakers.create("productLookup").run(() -> products.findById(input.productId()), ex -> {
            for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
                if (cause instanceof FeignException.NotFound) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found; no order was created", ex);
                }
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Product service unavailable; no order was created", ex);
        });
        if (product == null || product.price() == null || product.price().signum() < 0) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Invalid product data");
        }
        return orders.save(new ShopOrder(product, input.quantity()));
    }
}
