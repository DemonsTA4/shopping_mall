package org.example.shoppingmall.entity; // 或者一个通用的 enums 包

public enum CouponType {
    PERCENTAGE,         // 百分比折扣，例如：九折 (discountValue=10 表示10% off)
    FIXED_AMOUNT,       // 固定金额减免，例如：满100减10 (discountValue=10)
    FREE_SHIPPING       // 免运费 (discountValue 可以为0，或者表示特定运费减免额度)
    // 可以根据需要添加更多类型，例如：满赠券等
}