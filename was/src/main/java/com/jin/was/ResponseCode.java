package com.jin.was;

public enum ResponseCode {
    FORBIDDEN("403", "403 Forbidden"),
    NOT_FOUND("404", "404 File Not Found"),
    SERVER_ERROR("500", "500 Internal Server Error"),
    NOT_IMPLEMENTED("501", "501 Not Implemented"),
    OK("200", "200 OK")
    ;

    final String code;
    final String contents;

    ResponseCode(String code, String contents) {
        this.code = code;
        this.contents = contents;
    }

    public String code() {
        return code;
    }
}
