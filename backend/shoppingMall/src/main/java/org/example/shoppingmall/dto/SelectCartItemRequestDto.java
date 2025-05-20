// SelectCartItemRequestDto.java (如果还没有，则创建)
package org.example.shoppingmall.dto;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class SelectCartItemRequestDto {
    @NotNull(message = "选中状态不能为空")
    private Boolean selected;
}