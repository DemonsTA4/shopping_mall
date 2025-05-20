package org.example.shoppingmall.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.shoppingmall.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {
    
    // 基础CRUD操作
    int insert(Order order);
    
    int update(Order order);
    
    int deleteById(Integer id);
    
    Order selectById(Integer id);
    
    List<Order> selectAll();
    
    // 分页查询
    List<Order> selectByPage(@Param("offset") int offset, @Param("limit") int limit);
    
    // 按买家ID查询订单
    List<Order> selectByBuyerId(@Param("buyerId") Integer buyerId, 
                              @Param("offset") int offset, 
                              @Param("limit") int limit);
    
    // 按卖家ID查询订单
    List<Order> selectBySellerId(@Param("sellerId") Integer sellerId, 
                               @Param("offset") int offset, 
                               @Param("limit") int limit);
    
    // 按状态查询订单
    List<Order> selectByStatus(@Param("status") String status, 
                             @Param("offset") int offset, 
                             @Param("limit") int limit);
    
    // 按时间范围查询订单
    List<Order> selectByDateRange(@Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate, 
                                @Param("offset") int offset, 
                                @Param("limit") int limit);
    
    // 统计订单数量
    int countOrdersBetweenDates(@Param("startDate") LocalDateTime startDate, 
                              @Param("endDate") LocalDateTime endDate);
    
    // 统计订单总金额
    BigDecimal sumOrderAmountBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);
    
    // 按日期分组统计订单数量
    List<Object[]> countOrdersByDate(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);
    
    // 按日期分组统计订单金额
    List<Object[]> sumOrderAmountByDate(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
    
    // 按用户分组统计订单金额
    List<Object[]> sumOrderAmountByUser(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate, 
                                      @Param("limit") int limit);
    
    // 查询指定买家的所有订单
    List<Order> findByBuyerId(@Param("buyerId") Integer buyerId);
    
    // 计数
    int count();
} 