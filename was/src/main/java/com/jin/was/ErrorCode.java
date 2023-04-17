package com.jin.was;

public enum ErrorCode {
    FORBIDDEN("403", "403 Forbidden"),
    NOT_FOUND("404", "404 File Not Found"),
    SERVER_ERROR("500", "500 Internal Server Error"),
    NOT_IMPLEMENTED("501", "501 Not Implemented"),
    ;

    final String code;
    final String response;

    ErrorCode(String code, String response) {
        this.code = code;
        this.response = response;
    }
}
