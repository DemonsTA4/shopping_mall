package org.example.shoppingmall.dto;
import lombok.Data;

@Data
public class PaymentStatusDto {
    private String status; // 例如: "paid", "pending", "failed", "unpaid" 等
    // 可以添加其他需要的字段，如支付时间、错误信息等
}