package com.demo.shop.order;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity
@Table(name="shop_orders")
public class ShopOrder {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 private Long productId;
 private String productName;
 private int quantity;
 private BigDecimal unitPrice;
 private BigDecimal totalPrice;
 protected ShopOrder(){}
 public ShopOrder(ProductDto product,int quantity){this.productId=product.id();this.productName=product.name();this.quantity=quantity;this.unitPrice=product.price();this.totalPrice=product.price().multiply(BigDecimal.valueOf(quantity));}
 public Long getId(){return id;}public Long getProductId(){return productId;}public String getProductName(){return productName;}public int getQuantity(){return quantity;}public BigDecimal getUnitPrice(){return unitPrice;}public BigDecimal getTotalPrice(){return totalPrice;}
}
