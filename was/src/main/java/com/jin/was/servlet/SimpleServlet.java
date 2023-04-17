package com.jin.was.servlet;

import java.io.IOException;

public interface SimpleServlet {

    void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
