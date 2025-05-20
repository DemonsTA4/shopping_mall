package org.example.shoppingmall.exception;

import lombok.Getter;
import lombok.Setter; // 注意：通常异常类的字段设为 final 并在构造时赋值，不需要 setter
import org.example.shoppingmall.common.ResultCode;

@Getter
// @Setter // 通常 errorCode 和 message 在异常创建后不应被修改
public class ApiException extends RuntimeException {
    private final int errorCode; // 建议设为 final
    // private final String message; // message 已经由 super(message) 处理，如果需要单独存储可以保留

    public ApiException(String message) {
        super(message);
        // 根据您的定义，这里的 errorCode 会是默认的，或者您希望有一个默认的错误码
        this.errorCode = ResultCode.VALIDATION_ERROR.getCode(); // 例如，默认为参数验证错误
    }

    /**
     * 接受 ResultCode 枚举的构造函数。
     * @param resultCode 结果码枚举实例
     */
    public ApiException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.errorCode = resultCode.getCode();
    }

    /**
     * 接受 ResultCode 枚举和自定义消息的构造函数。
     * @param resultCode 结果码枚举实例
     * @param customMessage 自定义的覆盖消息
     */
    public ApiException(ResultCode resultCode, String customMessage) {
        super(customMessage); // 使用自定义消息
        this.errorCode = resultCode.getCode(); // 但仍然使用ResultCode的code
    }


    public ApiException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    // getErrorCode() 由 Lombok @Getter 生成
    // getMessage() 继承自 RuntimeException，并由 super(message) 设置
}
