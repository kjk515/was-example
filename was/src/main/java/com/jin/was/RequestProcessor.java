package com.jin.was;

import com.jin.was.servlet.HttpServletRequest;
import com.jin.was.servlet.HttpServletResponse;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestProcessor implements Runnable {

    private final static Logger logger = Logger.getLogger(RequestProcessor.class.getCanonicalName());
    private String rootPath;
    private String indexFileName = "index.html";
    private Socket connection;

    public RequestProcessor(String rootPath, String indexFileName, Socket connection) {
        this.rootPath = rootPath;
        if (indexFileName != null) {
            this.indexFileName = indexFileName;
        }
        this.connection = connection;
    }

    @Override
    public void run() {
        try {
            OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
            RequestHeader requestHeader = RequestHeader.of(new InputStreamReader((connection.getInputStream()), StandardCharsets.UTF_8), indexFileName);

            if (!requestHeader.isGetMethod()) {
                send501(outputStream, requestHeader);
                return;
            }

            String contentType = URLConnection.getFileNameMap().getContentTypeFor(requestHeader.url());

            if (contentType == null) {
                if (sendServlet(outputStream, requestHeader)) {
                    return;
                }
            }

            sendFile(contentType, outputStream, requestHeader);

        } catch (IOException ex) {
            logger.log(Level.WARNING, "Error talking to " + connection.getRemoteSocketAddress(), ex);
        } catch (Exception ex) {
            logger.warning("Exception");
        } finally {
            try {
                connection.close();
            } catch (IOException ex) {
            }
        }
    }

    private boolean sendServlet(OutputStream outputStream, RequestHeader requestHeader) throws IOException, URISyntaxException {
        Writer writer = new OutputStreamWriter(outputStream);
        try {
            String className = requestHeader.url(); // TODO rootDirectory??
            Class<?> clazz = Class.forName(className);

            Constructor<?> constructor = clazz.getConstructor();
            Object servlet = constructor.newInstance();
            Method clazzMethod = clazz.getMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);

            HttpServletRequest req = new HttpServletRequest();
            Writer responseWriter = new StringWriter();
            HttpServletResponse res = new HttpServletResponse(responseWriter);

            clazzMethod.invoke(servlet, req, res);

            if (requestHeader.isHttpRequest()) {
                new ResponseHeader(requestHeader.version() + " 200 OK", "text/plain", responseWriter.toString().getBytes().length)
                    .writeHeader(outputStream);
            }

            writer.write(responseWriter.toString());
            writer.flush();
            return true;
        } catch (ClassNotFoundException e) {
            logger.warning("Class Not Found!!!!!!!!!!!!!!");
            send404(outputStream, requestHeader);
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            logger.warning("!!!!!!!!!!!!!!!!");
        }

        return false;
    }

    private void sendFile(String contentType, OutputStream outputStream, RequestHeader requestHeader) throws IOException {
        String matchedRootDirectory = rootPath != null ? rootPath : ServerConfig.getInstance().appBasePath(requestHeader.host());
        File theFile = new File(matchedRootDirectory, requestHeader.url());

        if (theFile.canRead()
            // Don't let clients outside the document root // TODO root 접근 권한제어?
            && theFile.getCanonicalPath().startsWith(matchedRootDirectory)) {
            byte[] theData = Files.readAllBytes(theFile.toPath());
            if (requestHeader.version().startsWith("HTTP/")) {
                new ResponseHeader(requestHeader.version() + " 200 OK", contentType, theData.length)
                    .writeHeader(outputStream);
            }
            outputStream.write(theData);
            outputStream.flush();
        } else {
            send404(outputStream, requestHeader);
        }
    }

    private void send501(OutputStream outputStream, RequestHeader requestHeader) throws IOException {
        byte[] bytes = getClass().getClassLoader().getResourceAsStream("html/501.html").readAllBytes();

        if (requestHeader.isHttpRequest()) {
            new ResponseHeader(requestHeader.version() + " 501 Not Implemented", "text/html", bytes.length)
                .writeHeader(outputStream);
        }
        outputStream.write(bytes);
        outputStream.flush();
    }

    private void send404(OutputStream outputStream, RequestHeader requestHeader) throws IOException {
        byte[] bytes = ServerConfig.getInstance().errorPagePath(requestHeader.host()) getClass().getClassLoader().getResourceAsStream("html/404.html").readAllBytes();

        if (requestHeader.isHttpRequest()) {
            new ResponseHeader(requestHeader.version() + " 404 File Not Found", "text/html", bytes.length)
                .writeHeader(outputStream);
        }
        outputStream.write(bytes);
        outputStream.flush();
    }
}
