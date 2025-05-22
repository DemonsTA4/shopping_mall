package org.example.shoppingmall.repository;

import org.example.shoppingmall.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderOrderNo(String orderNo);
    
    List<OrderItem> findByProductId(Long productId);
    
    boolean existsByProductIdAndOrderBuyerId(Long productId, Long buyerId);
} 