package org.example.shoppingmall.repository;

import org.example.shoppingmall.entity.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * 根据用户ID查找用户的购物车。
     * 使用 @EntityGraph 来急切加载购物车中的商品项及其关联的商品信息，
     * 以避免在访问 cart.getItems() 时产生N+1查询问题。
     *
     * @param userId 用户ID
     * @return 包含用户购物车的Optional对象
     */
    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    Optional<Cart> findByUserId(Long userId);

    // 如果Cart实体中没有直接的userId字段，而是通过User user关联，
    // 那么方法名应该是 findByUser_Id
    // @EntityGraph(attributePaths = {"user", "items", "items.product"})
    // Optional<Cart> findByUser_Id(Long userId);
}
