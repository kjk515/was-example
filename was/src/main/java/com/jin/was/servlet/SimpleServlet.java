package com.jin.was.servlet;

import java.io.IOException;

public interface SimpleServlet {

    String GET_METHOD = "doGet";

    void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
