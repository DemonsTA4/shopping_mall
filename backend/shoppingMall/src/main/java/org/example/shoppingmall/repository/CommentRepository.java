package org.example.shoppingmall.repository;

import org.example.shoppingmall.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> { // 主键为 Long

    // 根据商品ID查询可见评论列表 (不分页)
    @EntityGraph(attributePaths = {"user", "product"})
    List<Comment> findByProduct_IdAndVisibleTrue(Long productId); // visible = true 由方法名隐含

    // 根据商品ID查询可见评论列表 (分页)
    @EntityGraph(attributePaths = {"user", "product"})
    Page<Comment> findByProduct_IdAndVisibleTrue(Long productId, Pageable pageable); // visible = true 由方法名隐含

    // 根据用户ID查询可见评论列表 (不分页)
    @EntityGraph(attributePaths = {"user", "product"})
    List<Comment> findByUser_IdAndVisibleTrue(Long userId); // <--- 修改点：移除了 Boolean visible 参数

    // 根据用户ID查询可见评论列表 (分页)
    @EntityGraph(attributePaths = {"user", "product"})
    Page<Comment> findByUser_IdAndVisibleTrue(Long userId, Pageable pageable); // <--- 修改点：移除了 Boolean visible 参数

    // 获取某商品下可见评论的平均评分
    @Query("SELECT AVG(c.rating) FROM Comment c WHERE c.product.id = :productId AND c.visible = true")
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    // 统计某商品下特定评分的可见评论数量
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.product.id = :productId AND c.visible = true AND c.rating = :rating")
    Long countByProduct_IdAndVisibleTrueAndRating(@Param("productId") Long productId, @Param("rating") Integer rating);

    // 检查某个订单项是否已被评论
    boolean existsByOrderItem_Id(Long orderItemId);

    // 查询某商品下最新的前5条可见评论，并按创建时间降序排列
    @EntityGraph(attributePaths = {"user", "product"})
    List<Comment> findTop5ByProduct_IdAndVisibleTrueOrderByCreatedAtDesc(Long productId); // visible = true 由方法名隐含

    @Override
    @EntityGraph(attributePaths = {"user", "product", "orderItem"})
    Optional<Comment> findById(Long id);
}
