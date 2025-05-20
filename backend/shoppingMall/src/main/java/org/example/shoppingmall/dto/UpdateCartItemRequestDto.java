// UpdateCartItemRequestDto.java
package org.example.shoppingmall.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCartItemRequestDto {

    @NotNull(message = "购物车项ID不能为空")
    private Long id; // 购物车项的 ID

    @NotNull(message = "数量不能为空")
    @Min(value = 0, message = "商品数量不能为负数 (0表示删除)") // 假设数量为0时会删除该项
    private Integer quantity;
}