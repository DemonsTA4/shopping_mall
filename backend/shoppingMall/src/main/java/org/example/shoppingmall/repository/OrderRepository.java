package org.example.shoppingmall.repository;

import org.example.shoppingmall.entity.Order;
import org.example.shoppingmall.entity.OrderStatus; // 导入OrderStatus
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph; // 导入EntityGraph
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> { // 主键类型为 Long

    // 使用 @EntityGraph 来急切加载关联数据，避免N+1问题
    @Override
    @EntityGraph(attributePaths = {"buyer", "items", "items.product"})
    Optional<Order> findById(Long id);

    @EntityGraph(attributePaths = {"buyer", "items", "items.product"})
    Page<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId, Pageable pageable); // 推荐的按用户分页查询方法

    // 用于 serviceImpl 中的 getUserOrdersWithDetails，但上面一个更通用
    @Query("SELECT o FROM Order o JOIN FETCH o.buyer b WHERE b.id = :buyerId") // 确保是 buyer.id
    @EntityGraph(attributePaths = {"items", "items.product"}) // user 已经在JOIN FETCH中了
    Page<Order> findByBuyerIdWithDetails(@Param("buyerId") Long buyerId, Pageable pageable);

    Optional<Order> findByOrderNo(String orderNo);

    @EntityGraph(attributePaths = {"buyer", "items", "items.product"}) // buyer 也需要关联，确保Order实体有buyer字段
    Optional<Order> findByIdAndBuyerId(Long id, Long buyerId); // 确保是 buyer.id

    // 使用 OrderStatus 枚举类型作为参数
    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime time); // 注意方法名和参数类型

    @Query("SELECT COUNT(o) FROM Order o WHERE o.buyer.id = :buyerId AND o.status = :status")
    Long countByBuyerIdAndStatus(@Param("buyerId") Long buyerId, @Param("status") OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    Integer countOrdersBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.status <> org.example.shoppingmall.entity.OrderStatus.CANCELLED") // 使用枚举常量
    BigDecimal sumOrderAmountBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT FUNCTION('DATE', o.createdAt) as orderDate, COUNT(o) as orderCount " +
            "FROM Order o " +
            "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('DATE', o.createdAt) " +
            "ORDER BY FUNCTION('DATE', o.createdAt)") // <--- 修改此处
    List<Object[]> countOrdersByDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT FUNCTION('DATE', o.createdAt) as orderDate, SUM(o.totalAmount) as totalAmount " +
            "FROM Order o " +
            "WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.status <> org.example.shoppingmall.entity.OrderStatus.CANCELLED " + // 使用枚举常量
            "GROUP BY FUNCTION('DATE', o.createdAt) " +
            "ORDER BY FUNCTION('DATE', o.createdAt)") // <--- 修改此处
    List<Object[]> sumOrderAmountByDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o.buyer.id as userId, COUNT(o) as orderCount " +
            "FROM Order o " +
            "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY o.buyer.id " +
            "ORDER BY orderCount DESC")
    List<Object[]> countOrdersByUser(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query("SELECT o.buyer.id as userId, SUM(o.totalAmount) as totalAmount " +
            "FROM Order o " +
            "WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.status <> org.example.shoppingmall.entity.OrderStatus.CANCELLED " + // 使用枚举常量
            "GROUP BY o.buyer.id " +
            "ORDER BY totalAmount DESC")
    List<Object[]> sumOrderAmountByUser(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);


    @Query("SELECT COUNT(o) FROM Order o WHERE o.buyer.id = :buyerId")
    Long countByBuyerId(@Param("buyerId") Long buyerId);

}
