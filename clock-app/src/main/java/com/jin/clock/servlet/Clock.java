package com.jin.clock.servlet;

import com.jin.was.servlet.HttpServletRequest;
import com.jin.was.servlet.HttpServletResponse;
import com.jin.was.servlet.SimpleServlet;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;

public class Clock implements SimpleServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {

        Writer writer = res.getWriter();
        writer.write("현재시간 : " + LocalDateTime.now());
    }
}
