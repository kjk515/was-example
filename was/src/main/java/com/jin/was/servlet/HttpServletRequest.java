package com.jin.was.servlet;

import java.util.Map;

public record HttpServletRequest(
    Map<String, String> parameter
) {

    public String getParameter(String key) {
        return this.parameter.get(key);
    }
}
