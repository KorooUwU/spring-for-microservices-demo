package com.demo.shop.product;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class DataSeed {
 @Bean CommandLineRunner seedProducts(ProductRepository repo){return args->{if(repo.count()==0){repo.save(new Product("Laptop",new BigDecimal("1200.00")));repo.save(new Product("Keyboard",new BigDecimal("75.00")));repo.save(new Product("Mouse",new BigDecimal("25.00")));}};}
}
