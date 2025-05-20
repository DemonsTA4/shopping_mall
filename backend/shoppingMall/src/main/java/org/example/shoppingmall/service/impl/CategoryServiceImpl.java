package org.example.shoppingmall.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shoppingmall.common.ResultCode;
import org.example.shoppingmall.dto.CategoryDTO;
import org.example.shoppingmall.entity.Category;
import org.example.shoppingmall.exception.ApiException;
import org.example.shoppingmall.repository.CategoryRepository;
import org.example.shoppingmall.service.CategoryService;
import org.example.shoppingmall.service.FileService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final FileService fileService;

    @Override
    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(ResultCode.CATEGORY_NOT_EXISTS));
        return convertToDTO(category);
    }

    @Override
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        // 检查分类名称是否已存在
        if (categoryRepository.existsByName(categoryDTO.getName())) {
            throw new ApiException(ResultCode.CATEGORY_EXISTS);
        }
        
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        
        category = categoryRepository.save(category);
        return convertToDTO(category);
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(ResultCode.CATEGORY_NOT_EXISTS));
        
        // 检查分类名称是否已存在（排除自身）
        if (!category.getName().equals(categoryDTO.getName()) && 
                categoryRepository.existsByName(categoryDTO.getName())) {
            throw new ApiException(ResultCode.CATEGORY_EXISTS);
        }
        
        if (categoryDTO.getName() != null) {
            category.setName(categoryDTO.getName());
        }
        if (categoryDTO.getDescription() != null) {
            category.setDescription(categoryDTO.getDescription());
        }
        if (categoryDTO.getIcon() != null) {
            category.setIcon(categoryDTO.getIcon());
        }
        if (categoryDTO.getSort() != null) {
            category.setSort(categoryDTO.getSort());
        }
        if (categoryDTO.getStatus() != null) {
            category.setStatus(categoryDTO.getStatus());
        }
        
        category = categoryRepository.save(category);
        return convertToDTO(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ApiException(ResultCode.CATEGORY_NOT_EXISTS);
        }
        
        // TODO: 检查分类下是否有商品，如果有则不允许删除
        
        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional
    public CategoryDTO updateCategoryStatus(Long id, Integer status) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(ResultCode.CATEGORY_NOT_EXISTS));
        
        category.setStatus(status);
        category = categoryRepository.save(category);
        return convertToDTO(category);
    }

    @Override
    public String uploadCategoryIcon(MultipartFile file) {
        return fileService.uploadFile(file, "category");
    }
    
    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        BeanUtils.copyProperties(category, dto);
        return dto;
    }
} 