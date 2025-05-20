package org.example.shoppingmall.service;

import org.example.shoppingmall.dto.CartItemDTO;
import java.util.List;

public interface CartService {

    /**
     * 获取购物车中商品数量
     * @return 商品数量
     */
    Integer getCartCount();
    
    /**
     * 获取购物车列表
     * @return 购物车商品列表
     */
    List<CartItemDTO> getCartList();
    
    /**
     * 添加商品到购物车
     * @param productId 商品ID
     * @param quantity 数量
     * @return 添加后的购物车项
     */
    CartItemDTO addToCart(Long productId, Integer quantity);
    
    /**
     * 更新购物车商品数量
     * @param id 购物车项ID
     * @param quantity 数量
     * @return 更新后的购物车项
     */
    CartItemDTO updateCartItemQuantity(Long id, Integer quantity);
    
    /**
     * 从购物车中删除商品
     * @param id 购物车项ID
     */
    void removeFromCart(Long id);
    
    /**
     * 清空购物车
     */
    void clearCart();
    
    /**
     * 选择/取消选择购物车商品
     * @param id 购物车项ID
     * @param selected 是否选中
     * @return 更新后的购物车项
     */
    CartItemDTO updateCartItemSelected(Long id, Boolean selected);
    
    /**
     * 全选/取消全选购物车商品
     * @param selected 是否全选
     * @return 更新后的购物车列表
     */
    List<CartItemDTO> updateAllCartItemsSelected(Boolean selected);
}