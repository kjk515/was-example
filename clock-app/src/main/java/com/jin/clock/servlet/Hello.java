package com.jin.clock.servlet;

import com.jin.was.servlet.HttpServletRequest;
import com.jin.was.servlet.HttpServletResponse;
import com.jin.was.servlet.SimpleServlet;

import java.io.IOException;
import java.io.Writer;
import java.util.logging.Logger;

public class Hello implements SimpleServlet {

    private static final Logger logger = Logger.getLogger(SimpleServlet.class.getCanonicalName());

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {

        System.out.println("run doGet");
        logger.info("run Servlet doGet");

        Writer writer = res.getWriter();
        writer.write("<html><body>Hello WWWWWWWW</body></html>");
//        writer.write(req.getParameter("name"));
    }
}
