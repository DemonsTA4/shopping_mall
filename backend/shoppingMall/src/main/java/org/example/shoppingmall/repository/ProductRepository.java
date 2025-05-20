package org.example.shoppingmall.repository;

import org.example.shoppingmall.entity.Product;
// import org.example.shoppingmall.entity.Category; // 确保 Category 实体已定义
// import org.example.shoppingmall.entity.User; // 确保 User 实体已定义
// import org.example.shoppingmall.entity.Brand; // 确保 Brand 实体已定义
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> { // Product ID 是 Long

    // 根据卖家ID查找商品 (分页)
    // 假设 Product 实体有: private User seller;
    Page<Product> findBySeller_Id(Long sellerId, Pageable pageable);

    // 根据卖家ID和商品ID查找，确保卖家只能操作自己的商品
    // Product ID 是 Long, Seller ID (User ID) 是 Long
    Optional<Product> findByIdAndSeller_Id(Long productId, Long sellerId);

    // 示例：根据商品名称和卖家ID查找 (用于检查商品是否已存在于该卖家店铺)
    // Seller ID (User ID) 是 Long
    Optional<Product> findByNameAndSeller_Id(String name, Long sellerId);

    // --- 前台用户可能需要的查询 ---

    // 查询所有上架商品 (分页)
    // 假设 Product 实体有: private Integer status; (1 代表上架/active)
    Page<Product> findByStatus(Integer status, Pageable pageable); // 例如 status = 1

    // 根据名称模糊查询上架商品 (分页)
    Page<Product> findByNameContainingIgnoreCaseAndStatus(String name, Integer status, Pageable pageable);

    // 根据分类实体和状态查询上架商品 (分页)
    // 假设 Product 实体有: private Category category;
    // Category ID 是 Integer (根据下面的 @Query 参数)
    Page<Product> findByCategory_IdAndStatus(Integer categoryId, Integer status, Pageable pageable);


    Page<Product> findByIsFeaturedTrueAndStatus(Integer status, Pageable pageable);

    // 按分类ID查询上架商品 (自定义查询，修正了 categories -> category 和 isActive -> status)
    // Category ID 是 Integer
    @Query("SELECT p FROM Product p WHERE p.status = 1 AND (:categoryId IS NULL OR p.category.id = :categoryId)")
    Page<Product> findActiveByCategoryId(@Param("categoryId") Integer categoryId, Pageable pageable);

    // 根据关键词搜索上架商品 (修正了 isActive -> status, brand -> brand.name)
    // 假设 Product 实体有: private String description; private Brand brand; (Brand 实体有 String name;)
    @Query("SELECT p FROM Product p LEFT JOIN p.brand b WHERE p.status = 1 AND " +
            "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')))") // 搜索品牌名称
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            Pageable pageable);

    // 高级搜索上架商品 (修正了 categories -> category, isActive -> status, brand -> brand.name)
    // Category ID 是 Integer
    @Query("SELECT p FROM Product p LEFT JOIN p.category c LEFT JOIN p.brand b WHERE p.status = 1 AND " +
            "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:categoryId IS NULL OR c.id = :categoryId) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> advancedSearch(
            @Param("keyword") String keyword,
            @Param("categoryId") Integer categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    // 查询最新的上架商品
    // 假设 Product 实体有: private LocalDateTime createTime; (数据库列名可能是 created_at)
    @Query("SELECT p FROM Product p WHERE p.status = 1 ORDER BY p.createTime DESC")
    List<Product> findActiveOrderByCreateTimeDesc(Pageable pageable);

    // 查询评分最高的上架商品 (修正了 isActive -> status 和 GROUP BY 中的 updatedAt -> updateTime)
    // 假设 Product 实体有: private Set<Comment> comments; (Comment 实体有 Integer rating;)
    // 假设 Product 实体有: private User seller; (用于 GROUP BY)
    @Query("SELECT p FROM Product p LEFT JOIN p.comments c WHERE p.status = 1 " +
            "GROUP BY p.id, p.name, p.category, p.brand, p.seller, p.description, p.price, p.originalPrice, p.stock, p.status, p.imageUrl, p.images, p.detail, p.params, p.specs, p.sales, p.createTime, p.updateTime " + // <--- 修改了 p.updatedAt 为 p.updateTime，并确保所有非聚合列都在此
            "ORDER BY COALESCE(AVG(c.rating), 0) DESC, COUNT(c) DESC") // 按平均分和评论数排序
    List<Product> findTopRatedActiveProducts(Pageable pageable);


    @Query("SELECT p FROM Product p WHERE " +
            "p.isFeatured = true AND p.status = 1 AND " + // 核心条件：特色且上架
            "(:keyword IS NULL OR p.name LIKE %:keyword% OR p.description LIKE %:keyword%) AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> findFeaturedProductsWithFilters(
            @Param("keyword") String keyword,
            @Param("categoryId") Integer categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    // 根据卖家ID和状态查询商品 (分页)
    // Seller ID (User ID) 是 Long
    Page<Product> findBySeller_IdAndStatus(Long sellerId, Integer status, Pageable pageable);

}
