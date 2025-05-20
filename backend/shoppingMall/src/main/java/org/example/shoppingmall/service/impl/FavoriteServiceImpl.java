package org.example.shoppingmall.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.shoppingmall.dto.ProductResponseDto;
import org.example.shoppingmall.entity.Favorite;
import org.example.shoppingmall.entity.Product;
import org.example.shoppingmall.entity.User;
import org.example.shoppingmall.repository.FavoriteRepository;
import org.example.shoppingmall.repository.ProductRepository;
import org.example.shoppingmall.repository.UserRepository;
import org.example.shoppingmall.service.FavoriteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void addFavorite(Long userId, Long productId) {
        // 检查用户和产品是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在，ID: " + userId));
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("商品不存在，ID: " + productId));
        
        // 检查是否已经收藏过
        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            return; // 已经收藏过，不需要重复添加
        }
        
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setProduct(product);
        
        favoriteRepository.save(favorite);
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long productId) {
        // 检查用户和产品是否存在
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("用户不存在，ID: " + userId);
        }
        
        if (!productRepository.existsById(productId)) {
            throw new EntityNotFoundException("商品不存在，ID: " + productId);
        }
        
        // 查找并删除收藏记录
        favoriteRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Override
    public boolean isFavorite(Long userId, Long productId) {
        // 检查用户和产品是否存在
        if (!userRepository.existsById(userId) || !productRepository.existsById(productId)) {
            return false;
        }
        
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    public Page<ProductResponseDto> getUserFavorites(Long userId, Pageable pageable) {
        // 检查用户是否存在
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("用户不存在，ID: " + userId);
        }
        
        // 获取用户收藏的商品
        return favoriteRepository.findByUserId(userId, pageable)
                .map(favorite -> {
                    ProductResponseDto dto = new ProductResponseDto();
                    Product product = favorite.getProduct();
                    
                    dto.setId(product.getId());
                    dto.setName(product.getName());
                    dto.setPrice(product.getPrice());
                    dto.setImageUrl(product.getImageUrl());
                    // 处理 Category
                    if (product.getCategory() != null) {
                        ProductResponseDto.CategoryInfo categoryInfo = new ProductResponseDto.CategoryInfo();
                        categoryInfo.setId(product.getCategory().getId());     // 假设 Category 实体有 getId()
                        categoryInfo.setName(product.getCategory().getName()); // 假设 Category 实体有 getName()
                        dto.setCategory(categoryInfo); // <--- 正确设置 CategoryInfo
                    }
                    if (product.getBrand() != null) { // product.getBrand() 返回 Brand 实体对象
                        ProductResponseDto.BrandInfo brandInfo = new ProductResponseDto.BrandInfo();
                        brandInfo.setId(product.getBrand().getId());
                        brandInfo.setName(product.getBrand().getName());
                        brandInfo.setLogoUrl(product.getBrand().getLogoUrl()); // 假设 Brand 实体有 getLogoUrl()
                        dto.setBrand(brandInfo);
                    }
                    dto.setStockQuantity(product.getStock());
                    dto.setDescription(product.getDescription());
                    dto.setOriginalPrice(product.getOriginalPrice());
                    dto.setSales(product.getSales());
                    // 其他需要的属性...
                    
                    return dto;
                });
    }

    @Override
    public Long getProductFavoriteCount(Long productId) {
        // 检查商品是否存在
        if (!productRepository.existsById(productId)) {
            throw new EntityNotFoundException("商品不存在，ID: " + productId);
        }
        
        return favoriteRepository.countByProductId(productId);
    }
} 