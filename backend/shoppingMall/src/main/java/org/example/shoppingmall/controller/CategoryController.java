package org.example.shoppingmall.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.shoppingmall.common.Result;
import org.example.shoppingmall.dto.CategoryDTO;
import org.example.shoppingmall.service.CategoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Result<List<CategoryDTO>> getAllCategories() {
        return Result.success(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public Result<CategoryDTO> getCategoryById(@PathVariable Long id) {
        return Result.success(categoryService.getCategoryById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public Result<CategoryDTO> createCategory(@RequestBody @Valid CategoryDTO categoryDTO) {
        return Result.success(categoryService.createCategory(categoryDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public Result<CategoryDTO> updateCategory(@PathVariable Long id, @RequestBody @Valid CategoryDTO categoryDTO) {
        return Result.success(categoryService.updateCategory(id, categoryDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('SELLER')")
    public Result<CategoryDTO> updateCategoryStatus(@PathVariable Long id, @RequestBody Integer status) {
        return Result.success(categoryService.updateCategoryStatus(id, status));
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('SELLER')")
    public Result<String> uploadCategoryIcon(@RequestParam("file") MultipartFile file) {
        return Result.success(categoryService.uploadCategoryIcon(file));
    }
} 