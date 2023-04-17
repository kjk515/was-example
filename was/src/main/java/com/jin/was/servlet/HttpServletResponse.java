package com.jin.was.servlet;

import java.io.Writer;

public record HttpServletResponse(
    Writer writer
) {
    public Writer getWriter() {
        return writer;
    }
}
