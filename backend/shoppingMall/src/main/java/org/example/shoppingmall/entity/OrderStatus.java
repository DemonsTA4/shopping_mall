package org.example.shoppingmall.entity; // 请替换为您的实际包路径

public enum OrderStatus {
    PENDING_PAYMENT,    // 待付款
    PROCESSING,         // 处理中 (例如：已付款，待发货)
    AWAITING_SHIPMENT,  // 等待发货 (PROCESSING 的一个更具体的阶段)
    SHIPPED,            // 已发货
    DELIVERED,          // 已送达 (物流显示送达)
    COMPLETED,          // 已完成 (例如：用户确认收货或自动完成)
    CANCELLED,          // 已取消 (用户取消或系统取消)
    REFUND_PENDING,     // 退款中
    REFUNDED,           // 已退款
    FAILED              // 订单失败 (例如支付失败)
}