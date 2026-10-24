package com.demo.shop.product;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController
@RequestMapping("/products")
public class ProductController {
 private final ProductRepository repo;
 @Value("${shop.banner:Welcome to Spring Shop}") private String banner;
 public ProductController(ProductRepository repo){this.repo=repo;}
 @GetMapping public List<Product> all(){return repo.findAll();}
 @GetMapping("/{id}") public Product one(@PathVariable Long id){return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found"));}
 @GetMapping("/meta/banner") public Map<String,String> banner(){return Map.of("banner",banner);}
}
