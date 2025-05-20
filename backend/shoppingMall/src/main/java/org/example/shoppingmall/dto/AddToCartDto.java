// 文件名: AddToCartRequestDto.java
package org.example.shoppingmall.dto; // 请确保包名正确

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddToCartDto {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "商品数量至少为1")
    private Integer quantity;
}