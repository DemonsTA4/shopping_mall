package org.example.shoppingmall.repository;

import org.example.shoppingmall.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> { // 假设 CartItem ID 是 Long

    // 根据购物车ID和商品ID查找购物车项
    Optional<CartItem> findByCart_IdAndProduct_Id(Long cartId, Long productId);

    // 根据购物车ID查找所有购物车项
    List<CartItem> findByCart_Id(Long cartId);

    // 根据购物车ID计算购物车项数量
    Long countByCart_Id(Long cartId);

    // 根据购物车ID和商品项ID查找 (用于更新或删除特定项时验证归属)
    Optional<CartItem> findByIdAndCart_Id(Long cartItemId, Long cartId);

    // 根据购物车ID删除所有购物车项 (用于清空购物车)
    @Modifying // JPQL的DELETE或UPDATE操作需要此注解
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteByCart_Id(@Param("cartId") Long cartId);

    // 注意：之前基于 userId 的方法如 findByUserId, countByUserId, findByUserIdAndProductId,
    // findByIdAndUserId, deleteByUserId 将不再直接适用，因为 CartItem 不再直接关联 User。
    // 这些操作现在应该通过先获取用户的 Cart，然后操作 Cart 中的 items，
    // 或者通过 CartItemRepository 中基于 cart_id 的新方法来完成。
}
