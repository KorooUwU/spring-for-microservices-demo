package com.demo.shop.order;
import java.util.List;
import feign.FeignException;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController
@RequestMapping("/orders")
public class OrderController {
 public record CreateOrderRequest(Long productId,Integer quantity){}
 private final OrderRepository orders;
 private final ProductClient products;
 private final CircuitBreakerFactory<?,?> breakers;
 public OrderController(OrderRepository orders,ProductClient products,CircuitBreakerFactory<?,?> breakers){this.orders=orders;this.products=products;this.breakers=breakers;}
 @GetMapping public List<ShopOrder> all(){return orders.findAll();}
 @GetMapping("/{id}") public ShopOrder one(@PathVariable Long id){return orders.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found"));}
 @PostMapping @ResponseStatus(HttpStatus.CREATED)
 public ShopOrder create(@RequestBody CreateOrderRequest input){
  if(input.productId()==null||input.quantity()==null||input.quantity()<1||input.quantity()>100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"productId and quantity (1..100) required");
  ProductDto product= (ProductDto)breakers.create("productLookup").run(()->products.findById(input.productId()), ex->{
   // The time limiter can wrap the original Feign exception.
   for(Throwable cause=ex;cause!=null;cause=cause.getCause()) {
    if(cause instanceof FeignException.NotFound) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found; no order was created",ex);
   }
   throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Product service unavailable; no order was created",ex);
  });
  if(product==null||product.price()==null||product.price().signum()<0)throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Invalid product data");
  return orders.save(new ShopOrder(product,input.quantity()));
 }
}
