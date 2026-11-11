package com.demo.shop.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/products")
@Tag(name = "Product Controller", description = "Các API truy vấn danh mục và cấu hình sản phẩm")
public class ProductController {
    private final ProductRepository repo;
    @Value("${shop.banner:Welcome to Spring Shop}")
    private String banner;

    public ProductController(ProductRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả sản phẩm", description = "Trả về danh sách sản phẩm được seed tự động trong H2 database của Product Service.")
    @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    public List<Product> all() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết 1 sản phẩm theo ID", description = "Tìm kiếm sản phẩm theo ID. Trả về 404 nếu không tìm thấy.")
    @ApiResponse(responseCode = "200", description = "Tìm thấy sản phẩm")
    @ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm")
    public Product one(@Parameter(description = "ID của sản phẩm (ví dụ: 1 cho Laptop, 2 cho Keyboard)", example = "1") @PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    @GetMapping("/meta/banner")
    @Operation(summary = "Xem giá trị Banner từ Config Server", description = "Demo Spring Cloud Config: Lấy giá trị cấu hình shop.banner nạp tập trung từ Config Server.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    public Map<String, String> banner() {
        return Map.of("banner", banner);
    }
}
