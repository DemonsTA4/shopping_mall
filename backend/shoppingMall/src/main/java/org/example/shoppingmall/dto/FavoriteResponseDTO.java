package org.example.shoppingmall.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponseDTO {
    private boolean success;
    private String message;
    private Long productId;
    private Boolean currentFavoriteStatus; // 返回当前最新的收藏状态
}