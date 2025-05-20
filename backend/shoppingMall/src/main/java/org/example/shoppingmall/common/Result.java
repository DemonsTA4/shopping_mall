package org.example.shoppingmall.common; // 确保包名与您的项目一致

import lombok.Data;
import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor; // 移除，改用私有构造函数和静态工厂

@Data
@NoArgsConstructor // Lombok: 生成无参构造函数，可用于某些序列化库或框架
public class Result<T> {

    private Integer code;   // 状态码
    private String message; // 提示信息
    private T data;         // 数据负载

    // 私有构造函数，强制使用静态工厂方法创建实例
    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // --- 成功相关的静态工厂方法 ---

    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    // --- 失败/错误相关的静态工厂方法 ---

    /**
     * 创建一个失败/错误响应，使用指定的 code 和 message。
     */
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 创建一个失败/错误响应，使用自定义消息，code 默认为 400 (Bad Request/Validation Error)。
     */
    public static <T> Result<T> fail(String message) {
        return new Result<>(ResultCode.VALIDATION_ERROR.getCode(), message, null); // 默认为参数验证错误
    }

    /**
     * 创建一个失败/错误响应，使用 ResultCode 枚举。
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 创建一个失败/错误响应，使用 ResultCode 枚举及自定义消息。
     * 将使用 ResultCode 的 code，但使用提供的自定义消息。
     */
    public static <T> Result<T> error(ResultCode resultCode, String customMessage) {
        return new Result<>(resultCode.getCode(), customMessage, null);
    }

    // --- 特定状态的静态工厂方法 ---

    /**
     * 创建一个 "资源未找到" (404 Not Found) 的响应。
     */
    public static <T> Result<T> notFound(String message) {
        return new Result<>(ResultCode.NOT_FOUND.getCode(), message, null);
    }

    /**
     * 创建一个 "禁止访问" (403 Forbidden) 的响应。
     */
    public static <T> Result<T> forbidden(String message) {
        return new Result<>(ResultCode.FORBIDDEN.getCode(), message, null);
    }

    /**
     * 创建一个 "未授权" (401 Unauthorized) 的响应。
     */
    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(ResultCode.UNAUTHORIZED.getCode(), message, null);
    }

    /**
     * 创建一个 "参数验证错误" (400 Bad Request) 的响应。
     */
    public static <T> Result<T> validationError(String message) {
        return new Result<>(ResultCode.VALIDATION_ERROR.getCode(), message, null);
    }

    // 您可以根据需要添加更多特定状态的静态工厂方法，例如：
    // public static <T> Result<T> conflict(String message) {
    //     return new Result<>(ResultCode.CONFLICT.getCode(), message, null);
    // }
}
