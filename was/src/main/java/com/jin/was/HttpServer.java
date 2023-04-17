package com.jin.was;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by cybaek on 15. 5. 22..
 */
public class HttpServer {

    private static final Logger logger = Logger.getLogger(HttpServer.class.getCanonicalName());
    private static final int NUM_THREADS = 50;
    private static final String INDEX_FILE = "index.html";
    private static final int DEFAULT_PORT = 80;

    private final String rootPath;
    private final int port;

    public HttpServer() {
        this.rootPath = null;
        this.port = getPort(-1);
    }

    public HttpServer(String rootPath, int port) throws IOException {
        this.rootPath = rootPath;
        this.port = getPort(port);
    }

    public static int getPort(int port) {
        int configPort = ServerConfig.getInstance().port();

        if (port >= 0 && port <= 65535) {
            return port;
        }
        else if (configPort >= 0 && configPort <= 65535) {
            return configPort;
        }
        else {
            return DEFAULT_PORT;
        }
    }

    public void start() throws IOException {
        ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);

        try (ServerSocket server = new ServerSocket(port)) {
            logger.info("Accepting connections on port " + server.getLocalPort());
            logger.info("Document Root: " + rootPath);

            while (true) {
                logger.info("while");

                try {
                    Socket request = server.accept();
                    logger.info("request: " + request);
                    Runnable r = new RequestProcessor(rootPath, INDEX_FILE, request);
                    pool.submit(r);
                } catch (IOException ex) {
                    logger.log(Level.WARNING, "Error accepting connection", ex);
                }
            }
        }
    }

    public static void main(String[] args) {
        String rootPath = null;
        int port = 0;

        if (args.length > 0) {
            rootPath = args[0];
        }

        if (args.length >= 2) {
            port = Integer.parseInt(args[1]);
        }

        try {
            HttpServer webserver = new HttpServer(rootPath, port);
            webserver.start();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Server could not start", e);
        }
    }
}
