package com.jin.clock;

import com.jin.was.HttpServer;

import java.io.IOException;

public class ClockApplication {

    public static void main(String[] args) throws IOException {
        HttpServer server = new HttpServer();
        server.start();
    }
}
