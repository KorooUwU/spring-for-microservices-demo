package com.demo.shop.gateway;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring Shop Microservices - API Gateway")
                        .version("1.0.0")
                        .description("Tập trung giao diện Swagger UI cho toàn bộ hệ thống Microservices. Sử dụng menu 'Select a definition' ở góc trên bên phải để chuyển đổi giữa Product Service và Order Service.")
                        .contact(new Contact().name("Spring Microservices Demo")));
    }
}
