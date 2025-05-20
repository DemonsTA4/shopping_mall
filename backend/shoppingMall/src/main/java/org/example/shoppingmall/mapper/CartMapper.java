package org.example.shoppingmall.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.shoppingmall.entity.Cart;

@Mapper
public interface CartMapper {
    
    // 基础CRUD操作
    int insert(Cart cart);
    
    int update(Cart cart);
    
    int deleteById(Integer id);
    
    Cart selectById(Integer id);
    
    // 根据用户ID查询购物车
    Cart selectByUserId(Long userId);
    
    // 创建购物车（如果不存在）
    int insertIfNotExists(Long userId);
    
    // 清空购物车（仅保留购物车，清空所有购物车项）
    int clearCartItems(Integer cartId);
    
    // 检查购物车是否存在
    int countByUserId(Long userId);
} 