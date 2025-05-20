package org.example.shoppingmall.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.shoppingmall.entity.Banner;

import java.util.List;

@Mapper
public interface BannerMapper {
    
    // 基础CRUD操作
    int insert(Banner banner);
    
    int update(Banner banner);
    
    int deleteById(Integer id);
    
    Banner selectById(Integer id);
    
    List<Banner> selectAll();
    
    // 查询激活状态的Banner
    List<Banner> selectByIsActiveTrue();
    
    // 根据位置查询激活的Banner
    List<Banner> selectByPositionAndIsActiveTrue(@Param("position") String position);
    
    // 根据排序查询Banner
    List<Banner> selectByIsActiveTrueOrderBySortOrder();
    
    // 更新Banner状态
    int updateStatus(@Param("id") Integer id, @Param("isActive") boolean isActive);
} 