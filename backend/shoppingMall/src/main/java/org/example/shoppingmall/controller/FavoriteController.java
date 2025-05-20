package org.example.shoppingmall.controller;

import lombok.RequiredArgsConstructor;
import org.example.shoppingmall.common.Result;
import org.example.shoppingmall.dto.ProductResponseDto; // 您 Service 返回的 DTO
import org.example.shoppingmall.security.CustomUserDetails;
import org.example.shoppingmall.service.FavoriteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites") // ★ 统一的收藏接口前缀 ★
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    // 添加收藏
    @PostMapping("/{productId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> addFavorite(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long productId) {
        favoriteService.addFavorite(currentUser.getId(), productId); // 假设 currentUser.getId() 返回用户ID
        return Result.success("收藏成功",null);
    }

    // 取消收藏
    @DeleteMapping("/{productId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> removeFavorite(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long productId) {
        favoriteService.removeFavorite(currentUser.getId(), productId);
        return Result.success("取消收藏成功",null);
    }

    // 检查商品是否被当前用户收藏
    @GetMapping("/{productId}/status")
    @PreAuthorize("isAuthenticated()")
    public Result<Boolean> isFavorite(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long productId) {
        boolean isFavorite = favoriteService.isFavorite(currentUser.getId(), productId);
        return Result.success(isFavorite);
    }

    // 获取当前用户收藏的商品列表 (分页)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Page<ProductResponseDto>> getUserFavorites(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = "1") int pageNum,      // 前端通常传 page 或 pageNum
            @RequestParam(defaultValue = "10") int pageSize) { // 前端通常传 pageSize 或 size

        // Spring Data Pageable 的页码是从0开始的，所以如果前端传1作为第一页，需要减1
        Pageable pageable = PageRequest.of(pageNum > 0 ? pageNum - 1 : 0, pageSize, Sort.by("createdAt").descending()); // 假设 Favorite 实体有 createdAt 字段用于排序
        Page<ProductResponseDto> favoritesPage = favoriteService.getUserFavorites(currentUser.getId(), pageable);
        return Result.success(favoritesPage);
    }

    // 获取商品收藏数量可以放在 ProductController 中，因为它更像是商品的属性
    // 如果要放在这里，可以是：
    /*
    @GetMapping("/product/{productId}/count")
    public Result<Long> getProductFavoriteCount(@PathVariable Long productId) {
        return Result.success(favoriteService.getProductFavoriteCount(productId));
    }
    */
}