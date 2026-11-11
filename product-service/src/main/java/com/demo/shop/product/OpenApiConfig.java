package com.demo.shop.product;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product Service API")
                        .version("1.0.0")
                        .description("Microservice quản lý danh mục và thông tin giá sản phẩm.")
                        .contact(new Contact().name("Spring Microservice Shop Demo")))
                .servers(List.of(
                        new Server().url("/api").description("Qua API Gateway (:8080)"),
                        new Server().url("/").description("Trực tiếp Product Service (:8081)")
                ));
    }
}
