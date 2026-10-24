package com.demo.shop.order;

import feign.FeignException;
import feign.Request;
import feign.Response;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
    "spring.cloud.config.enabled=false", "eureka.client.enabled=false"
})
@ActiveProfiles("test")
class OrderFailureTest {
    @Autowired OrderController controller;
    @Autowired OrderRepository orders;
    @Autowired CircuitBreakerRegistry registry;
    @MockitoBean ProductClient products;

    @BeforeEach void resetState() {
        orders.deleteAll();
        registry.circuitBreaker("productLookup").reset();
    }

    private FeignException productError(int status) {
        Request request = Request.create(Request.HttpMethod.GET, "/products/999", Map.of(),
            (byte[]) null, java.nio.charset.StandardCharsets.UTF_8, null);
        return FeignException.errorStatus("ProductClient#findById", Response.builder()
            .request(request).status(status).reason("test").headers(Map.of()).build());
    }

    private void expectStatus(long id, int status) {
        assertThatThrownBy(() -> controller.create(new OrderController.CreateOrderRequest(id, 2)))
            .isInstanceOfSatisfying(ResponseStatusException.class,
                exception -> assertThat(exception.getStatusCode().value()).isEqualTo(status));
    }

    @Test void missingProductsDoNotOpenCircuitOrCreateOrders() {
        when(products.findById(999L)).thenThrow(productError(404));
        for (int i = 0; i < 5; i++) expectStatus(999L, 404);
        CircuitBreaker circuit = registry.circuitBreaker("productLookup");
        assertThat(circuit.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        assertThat(circuit.getMetrics().getNumberOfFailedCalls()).isZero();
        assertThat(orders.count()).isZero();
        when(products.findById(1L)).thenReturn(new ProductDto(1L, "Laptop", new BigDecimal("1200")));
        assertThat(controller.create(new OrderController.CreateOrderRequest(1L, 2)).getTotalPrice())
            .isEqualByComparingTo("2400");
        assertThat(orders.count()).isEqualTo(1);
    }

    @Test void serviceFailuresOpenCircuitAndDoNotCreateOrders() {
        when(products.findById(1L)).thenThrow(productError(503));
        expectStatus(1L, 503);
        expectStatus(1L, 503);
        assertThat(registry.circuitBreaker("productLookup").getState()).isEqualTo(CircuitBreaker.State.OPEN);
        expectStatus(1L, 503);
        verify(products, times(2)).findById(1L);
        assertThat(orders.count()).isZero();
    }
}
