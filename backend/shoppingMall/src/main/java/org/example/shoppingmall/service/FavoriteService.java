package org.example.shoppingmall.service;

import org.example.shoppingmall.dto.ProductResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FavoriteService {

    void addFavorite(Long userId, Long productId);
    
    void removeFavorite(Long userId, Long productId);
    
    boolean isFavorite(Long userId, Long productId);
    
    Page<ProductResponseDto> getUserFavorites(Long userId, Pageable pageable);
    
    Long getProductFavoriteCount(Long productId);
} 