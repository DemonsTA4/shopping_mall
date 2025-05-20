package org.example.shoppingmall.exception;

import org.example.shoppingmall.common.ResultCode;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException() {
        super(ResultCode.NOT_FOUND);
    }

    public ResourceNotFoundException(String message) {
        super(ResultCode.NOT_FOUND.getCode(), message);
    }
} 