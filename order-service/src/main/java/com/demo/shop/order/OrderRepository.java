package com.demo.shop.order;
import org.springframework.data.jpa.repository.JpaRepository;
public interface OrderRepository extends JpaRepository<ShopOrder,Long>{}
