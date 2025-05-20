package org.example.shoppingmall.exception;// GlobalExceptionHandler.java
import org.example.shoppingmall.common.Result;
import org.example.shoppingmall.common.ResultCode;
import org.example.shoppingmall.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Result<Object>> handleApiException(ApiException ex) {
        int businessErrorCode = ex.getErrorCode(); // <--- 修改这里，使用 getErrorCode()
        String message = ex.getMessage();       // ex.getMessage() 是从 RuntimeException 继承的

        Result<Object> errorResult = Result.fail(businessErrorCode, message);
        log.warn("ApiException被捕获: code={}, message='{}'", businessErrorCode, message);

        HttpStatus httpStatus;
        // 根据业务错误码决定HTTP状态码
        if (businessErrorCode == ResultCode.LOGIN_ERROR.getCode() ||
                businessErrorCode == ResultCode.UNAUTHORIZED.getCode()) {
            httpStatus = HttpStatus.UNAUTHORIZED; // 401
        } else if (businessErrorCode == ResultCode.VALIDATION_ERROR.getCode() ||
                businessErrorCode == ResultCode.OLD_PASSWORD_ERROR.getCode()) {
            httpStatus = HttpStatus.BAD_REQUEST; // 400
        } else if (businessErrorCode == ResultCode.USER_EXISTS.getCode() ||
                businessErrorCode == ResultCode.CATEGORY_EXISTS.getCode() ||
                businessErrorCode == ResultCode.CONFLICT.getCode()) {
            httpStatus = HttpStatus.CONFLICT; // 409
        } else if (businessErrorCode == ResultCode.NOT_FOUND.getCode() ||
                businessErrorCode == ResultCode.USER_NOT_EXISTS.getCode() ||
                businessErrorCode == ResultCode.PRODUCT_NOT_EXISTS.getCode() ||
                businessErrorCode == ResultCode.CATEGORY_NOT_EXISTS.getCode() ||
                businessErrorCode == ResultCode.CART_ITEM_NOT_EXISTS.getCode() ||
                businessErrorCode == ResultCode.ORDER_NOT_EXISTS.getCode()) {
            httpStatus = HttpStatus.NOT_FOUND; // 404
        } else if (businessErrorCode == ResultCode.FORBIDDEN.getCode()) {
            httpStatus = HttpStatus.FORBIDDEN; // 403
        } else if (businessErrorCode == ResultCode.ERROR.getCode() ||
                businessErrorCode == ResultCode.UPLOAD_FAILED.getCode()) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR; // 500
        } else {
            log.warn("未明确映射HTTP状态的ApiException code: {}, 默认使用BAD_REQUEST", businessErrorCode);
            httpStatus = HttpStatus.BAD_REQUEST;
        }

        return new ResponseEntity<>(errorResult, httpStatus);
    }

    // ... 你其他的 @ExceptionHandler 方法 ...

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Object>> handleGenericException(Exception ex) {
        log.error("未处理的系统异常: ", ex);
        Result<Object> errorResult = Result.error(ResultCode.ERROR);
        return new ResponseEntity<>(errorResult, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}