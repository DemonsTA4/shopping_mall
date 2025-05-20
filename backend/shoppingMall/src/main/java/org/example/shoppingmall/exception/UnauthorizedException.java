package org.example.shoppingmall.exception;

import org.example.shoppingmall.common.ResultCode;

public class UnauthorizedException extends ApiException {
    public UnauthorizedException() {
        super(ResultCode.UNAUTHORIZED);
    }

    public UnauthorizedException(String message) {
        super(ResultCode.UNAUTHORIZED.getCode(), message);
    }
} 