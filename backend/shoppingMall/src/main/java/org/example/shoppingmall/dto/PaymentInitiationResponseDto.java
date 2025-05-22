// PaymentInitiationResponseDto.java
package org.example.shoppingmall.dto;

import lombok.Data;

@Data
public class PaymentInitiationResponseDto {
    private String qrUrl; // 二维码URL
    private String orderNo; // 订单号（可选，方便前端使用）
    // 其他支付相关信息...
}