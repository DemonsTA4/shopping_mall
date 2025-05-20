package org.example.shoppingmall.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.shoppingmall.entity.Category;

import java.util.List;

@Mapper
public interface CategoryMapper {
    
    // 插入新分类
    int insert(Category category);
    
    // 更新分类
    int update(Category category);
    
    // 通过ID删除分类
    int deleteById(Integer id);
    
    // 通过ID查找分类
    Category selectById(Integer id);
    
    // 通过名称查找分类
    Category selectByName(String name);
    
    // 查找所有分类
    List<Category> selectAll();
    
    // 查找父分类的所有子分类
    List<Category> selectByParentId(Integer parentId);
    
    // 分页查询
    List<Category> selectByPage(@Param("offset") int offset, @Param("limit") int limit);
    
    // 获取分类总数
    int count();
    
    // 检查分类是否有子分类
    int countChildren(Integer parentId);
    
    // 批量删除分类
    int deleteByIds(@Param("ids") List<Integer> ids);

    List<Category> searchCategories(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("offset") Integer offset, // 使用Integer以便可以为null，匹配XML中的if test
            @Param("limit") Integer limit    // 使用Integer以便可以为null
    );

    /**
     * 统计符合模糊搜索条件的分类总数。
     *
     * @param keyword 搜索关键词 (可选)
     * @param status 状态 (可选)
     * @return 符合条件的分类总数
     */
    int countSearchCategories(
            @Param("keyword") String keyword,
            @Param("status") Integer status
    );

} 