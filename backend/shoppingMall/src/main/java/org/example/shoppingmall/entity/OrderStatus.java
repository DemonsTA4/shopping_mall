package org.example.shoppingmall.entity; // 请确保包路径正确

import lombok.Getter; // 1. 导入 Lombok 的 @Getter 注解

@Getter // 2. 在枚举定义的上方添加 @Getter 注解
public enum OrderStatus {
    PENDING_PAYMENT(1, "待付款"),
    PROCESSING(2, "处理中"),
    AWAITING_SHIPMENT(2, "等待发货"), // 您需要决定 PROCESSING 和 AWAITING_SHIPMENT 的映射关系
    SHIPPED(3, "已发货"),
    DELIVERED(3, "已送达"),
    AWAITING_REVIEW(4, "待评价"),
    COMPLETED(5, "已完成"),
    CANCELLED(6, "已取消"),
    REFUND_PENDING(7, "退款中"),
    REFUNDED(8, "已退款"),
    FAILED(9, "订单失败");

    // 3. 定义 final 类型的字段
    private final int code;
    private final String description;

    // 4. 构造函数保持不变
    OrderStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    // 5. 不需要手动编写 getCode() 和 getDescription() 方法了，Lombok会自动生成它们。
    // public int getCode() {
    //     return code;
    // }
    //
    // public String getDescription() {
    //     return description;
    // }

    // （可选）如果您需要通过code反查枚举的静态方法，这个仍然需要手动编写
    public static OrderStatus fromCode(int code) {
        for (OrderStatus status : OrderStatus.values()) {
            // 即使 getCode() 是 Lombok 生成的，这里也能正确调用
            if (status.getCode() == code) {
                return status;
            }
        }
        // 可以考虑在找不到对应code时返回一个默认值或抛出异常
        // 例如： throw new IllegalArgumentException("No OrderStatus found for code: " + code);
        return null;
    }
}