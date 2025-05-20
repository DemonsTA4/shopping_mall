package org.example.shoppingmall.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class FavoriteRequestDTO {
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "收藏状态不能为空")
    private Boolean isFavorite; // true 表示收藏, false 表示取消收藏
}