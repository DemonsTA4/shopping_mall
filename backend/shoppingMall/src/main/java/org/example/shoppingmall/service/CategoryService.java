package org.example.shoppingmall.service;

import org.example.shoppingmall.dto.CategoryDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CategoryService {

    /**
     * 获取所有分类
     * @return 分类列表
     */
    List<CategoryDTO> getAllCategories();
    
    /**
     * 根据ID获取分类
     * @param id 分类ID
     * @return 分类信息
     */
    CategoryDTO getCategoryById(Long id);
    
    /**
     * 创建分类
     * @param categoryDTO 分类信息
     * @return 创建后的分类
     */
    CategoryDTO createCategory(CategoryDTO categoryDTO);
    
    /**
     * 更新分类
     * @param id 分类ID
     * @param categoryDTO 分类信息
     * @return 更新后的分类
     */
    CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO);
    
    /**
     * 删除分类
     * @param id 分类ID
     */
    void deleteCategory(Long id);
    
    /**
     * 更新分类状态
     * @param id 分类ID
     * @param status 状态（0:禁用,1:启用）
     * @return 更新后的分类
     */
    CategoryDTO updateCategoryStatus(Long id, Integer status);
    
    /**
     * 上传分类图标
     * @param file 图标文件
     * @return 图标URL
     */
    String uploadCategoryIcon(MultipartFile file);
} 