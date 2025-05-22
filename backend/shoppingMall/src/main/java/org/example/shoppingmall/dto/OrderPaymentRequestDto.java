// OrderPaymentRequestDto.java
package org.example.shoppingmall.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class OrderPaymentRequestDto {
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod; // 例如 "alipay", "wechat"
    // 如果还有其他支付时需要传递的参数，也可以加在这里
}