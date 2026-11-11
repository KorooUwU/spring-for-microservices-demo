package com.demo.shop.order;

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
    public OpenAPI orderServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .version("1.0.0")
                        .description("Microservice tạo đơn hàng. Sử dụng OpenFeign gọi liên dịch vụ sang Product Service và bảo vệ bằng Circuit Breaker Resilience4j.")
                        .contact(new Contact().name("Spring Microservice Shop Demo")))
                .servers(List.of(
                        new Server().url("/api").description("Qua API Gateway (:8080)"),
                        new Server().url("/").description("Trực tiếp Order Service (:8082)")
                ));
    }
}
