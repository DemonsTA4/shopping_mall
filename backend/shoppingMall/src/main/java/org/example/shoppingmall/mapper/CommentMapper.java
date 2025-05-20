package org.example.shoppingmall.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.shoppingmall.entity.Comment;

import java.util.List;

@Mapper
public interface CommentMapper {
    
    // 基础CRUD操作
    int insert(Comment comment);
    
    int update(Comment comment);
    
    int deleteById(@Param("id") Integer id);
    
    Comment selectById(@Param("id") Integer id);
    
    List<Comment> selectAll();
    
    // 查询商品的评论列表
    List<Comment> selectByProductId(@Param("productId") Integer productId);
    
    // 查询用户的评论列表
    List<Comment> selectByUserId(@Param("userId") Long userId);
    
    // 获取商品的平均评分
    Double getAverageRatingByProductId(@Param("productId") Long productId);
    
    // 获取产品平均评分
    Double getAverageRatingByProductId(@Param("productId") Integer productId);
    
    // 计数
    int countByProductId(@Param("productId") Integer productId);
} 