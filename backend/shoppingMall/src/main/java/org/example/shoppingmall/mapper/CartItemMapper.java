package org.example.shoppingmall.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.shoppingmall.entity.CartItem;

import java.util.List;

@Mapper
public interface CartItemMapper {
    
    // 基础CRUD操作
    void insert(CartItem cartItem);
    
    void update(CartItem cartItem);
    
    void deleteById(Integer id);
    
    void deleteByUserId(Long userId);
    
    CartItem selectById(Integer id);
    
    List<CartItem> selectByUserId(Long userId);
    
    CartItem selectByUserIdAndProductId(
        @Param("userId") Long userId,
        @Param("productId") Integer productId
    );
    
    int countByUserId(Long userId);
    
    void updateQuantity(
        @Param("id") Integer id,
        @Param("quantity") Integer quantity
    );
    
    void updateSelected(
        @Param("id") Integer id,
        @Param("selected") boolean selected
    );
    
    void updateSelectedByUserId(
        @Param("userId") Long userId,
        @Param("selected") boolean selected
    );
    
    // 根据购物车ID查询所有购物车项
    List<CartItem> selectByCartId(Integer cartId);
    
    // 根据购物车ID和商品ID查询购物车项
    CartItem selectByCartIdAndProductId(@Param("cartId") Integer cartId, @Param("productId") Integer productId);
    
    // 从购物车中删除指定商品
    int deleteByCartIdAndProductId(@Param("cartId") Integer cartId, @Param("productId") Integer productId);
    
    // 清空购物车中的所有项
    int deleteByCartId(Integer cartId);
    
    // 检查是否存在某商品
    int countByCartIdAndProductId(@Param("cartId") Integer cartId, @Param("productId") Integer productId);
}
 