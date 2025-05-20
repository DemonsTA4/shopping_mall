package org.example.shoppingmall.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.shoppingmall.dto.ProductSearchDto;
import org.example.shoppingmall.entity.Product;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ProductMapper {
    
    // 基础CRUD操作
    void insert(Product product);
    
    void update(Product product);
    
    void deleteById(Integer id);
    
    Product selectById(Integer id);
    
    List<Product> selectAll();
    
    // 分页查询
    List<Product> selectByPage(@Param("offset") int offset, @Param("limit") int limit);
    
    // 计数
    int count();
    
    // 根据卖家ID查询
    List<Product> selectBySellerId(@Param("sellerId") Integer sellerId, 
                                  @Param("offset") int offset, 
                                  @Param("limit") int limit);
    
    // 根据卖家ID和商品ID查询
    Product selectByIdAndSellerId(@Param("id") Integer id, @Param("sellerId") Integer sellerId);
    
    // 根据状态查询活跃商品
    List<Product> selectByIsActiveTrue(@Param("offset") int offset, @Param("limit") int limit);
    
    // 按名称搜索商品
    List<Product> selectByNameContaining(@Param("name") String name, 
                                        @Param("offset") int offset, 
                                        @Param("limit") int limit);
    
    // 按分类ID查询商品
    List<Product> selectByCategoryId(
        @Param("categoryId") Integer categoryId,
        @Param("offset") int offset,
        @Param("limit") int limit
    );
    
    // 高级搜索
    List<Product> advancedSearch(@Param("keyword") String keyword,
                                @Param("categoryId") Integer categoryId,
                                @Param("minPrice") BigDecimal minPrice,
                                @Param("maxPrice") BigDecimal maxPrice,
                                @Param("offset") int offset, 
                                @Param("limit") int limit);
    
    // 按创建时间降序查询活跃商品
    List<Product> selectByIsActiveTrueOrderByCreatedAtDesc(@Param("limit") int limit);
    
    // 查询评分最高的商品 - 添加注解实现
    @Select("SELECT p.*, u.username as seller_username, AVG(c.rating) as avg_rating " +
           "FROM products p " +
           "LEFT JOIN users u ON p.seller_id = u.id " +
           "LEFT JOIN comments c ON p.id = c.product_id " +
           "WHERE p.is_active = 1 " +
           "GROUP BY p.id " +
           "ORDER BY avg_rating DESC " +
           "LIMIT #{limit}")
    List<Product> selectTopRatedActiveProducts(@Param("limit") int limit);

    List<Product> search(
        @Param("search") ProductSearchDto search,
        @Param("offset") int offset,
        @Param("limit") int limit,
        @Param("orderBy") String orderBy,
        @Param("orderDirection") String orderDirection
    );
    
    int countSearch(@Param("search") ProductSearchDto search);
    
    int updateStock(
        @Param("id") Integer id,
        @Param("stock") Integer stock
    );
    
    int updateSales(
        @Param("id") Integer id,
        @Param("increment") Integer increment
    );
} 