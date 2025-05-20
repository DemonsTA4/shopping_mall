package org.example.shoppingmall.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.shoppingmall.entity.Favorite;

import java.util.List;

@Mapper
public interface FavoriteMapper {
    
    // 基础CRUD操作
    int insert(Favorite favorite);
    
    int deleteById(Integer id);
    
    int deleteByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Integer productId);
    
    Favorite selectById(Integer id);
    
    // 查询用户收藏的商品
    List<Favorite> selectByUserId(@Param("userId") Long userId, 
                                @Param("offset") int offset, 
                                @Param("limit") int limit);
    
    // 查询商品被收藏的次数
    Long countByProductId(@Param("productId") Integer productId);
    
    // 查询某个用户是否收藏了某个商品
    Favorite selectByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Integer productId);
    
    // 计数
    int countByUserId(@Param("userId") Long userId);
} 