package org.example.shoppingmall.common;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "成功"),
    ERROR(500, "服务器内部错误"),
    VALIDATION_ERROR(400, "参数验证错误"),
    UNAUTHORIZED(401, "未授权或登录已过期"), // 在 CartServiceImpl 中使用
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "请求的资源不存在"),   // 通用资源未找到

    // 用户相关错误码 (1000-1099)
    LOGIN_ERROR(1001, "用户名或密码错误"),
    USER_EXISTS(1002, "用户已存在"),
    USER_NOT_EXISTS(1003, "用户不存在"), // <--- 新增的枚举常量
    OLD_PASSWORD_ERROR(1004, "原密码不正确"),


    // 文件上传相关 (1100-1199)
    UPLOAD_FAILED(1101, "文件上传失败"),

    // 分类相关 (1200-1299)
    CATEGORY_EXISTS(1201, "分类名称已存在"),
    CATEGORY_NOT_EXISTS(1202, "分类不存在"),

    // 商品相关 (1300-1399)
    PRODUCT_NOT_EXISTS(1301, "商品不存在"),
    STOCK_NOT_ENOUGH(1302, "商品库存不足"),

    // 购物车相关 (1400-1499)
    CART_ITEM_NOT_EXISTS(1401, "购物车商品不存在"),

    // 订单相关 (1500-1599)
    ORDER_NOT_EXISTS(1501, "订单不存在"),
    ORDER_STATUS_ERROR(1502, "订单状态错误，无法进行此操作"),


    CONFLICT(409, "资源冲突或操作不允许"); // 通用冲突


    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    // Lombok @Getter 会自动生成 getCode() 和 getMessage()
}
