package com.jin.was;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jin.was.config.ServerConfig;
import com.jin.was.servlet.HttpServletRequest;
import com.jin.was.servlet.HttpServletResponse;
import com.jin.was.servlet.SimpleServlet;

public class RequestProcessor implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RequestProcessor.class);
    private static final List<String> FORBIDDEN_CONTENT_TYPES = List.of(
            "application/octet-stream"
    );
    private static final String SERVLET_RESPONSE_CONTENT_TYPE = "text/plain; charset=utf-8";
    private static final String ERROR_RESPONSE_CONTENT_TYPE = "text/html; charset=utf-8";

    private String rootPath;
    private String indexFileName = "index.html";
    private Socket connection;
    private File responseFile;

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
            RequestHeader requestHeader = RequestHeader.of(new InputStreamReader((connection.getInputStream()), StandardCharsets.UTF_8));

            if (!requestHeader.isGetMethod()) {
                sendError(outputStream, requestHeader, ResponseCode.NOT_IMPLEMENTED);
                return;
            }

            if (rootPath == null) { // 실행 파라미터가 없는경우
                this.rootPath = ServerConfig.getInstance().appBasePath(requestHeader.host());
            }
            responseFile = new File(rootPath, requestHeader.url());
            if (!responseFile.getCanonicalPath().startsWith(rootPath)) {
                sendError(outputStream, requestHeader, ResponseCode.FORBIDDEN);
                return;
            }

            if (responseFile.getCanonicalPath().equals(rootPath)) {
                responseFile = new File(rootPath, indexFileName);
            }
            String contentType = URLConnection.getFileNameMap().getContentTypeFor(responseFile.getName());
            if (contentType == null) {
                if (sendServlet(outputStream, requestHeader)) {
                    return;
                }
            }
            sendFile(contentType, outputStream, requestHeader);

        } catch (IOException e) {
            logger.warn("Error talking to: {} ", connection.getRemoteSocketAddress(), e);
        } catch (Exception e) {
            logger.error("RequestProcessor run exception ", e);
        } finally {
            try {
                connection.close();
            } catch (IOException e) {
                logger.error("Error connection socket", e);
            }
        }
    }

    private boolean sendServlet(OutputStream outputStream, RequestHeader requestHeader) throws IOException {
        String className = requestHeader.url();
        Class<?> clazz = null;

        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            logger.info("Not found in Servlets");
        }
        if (clazz == null || !SimpleServlet.class.isAssignableFrom(clazz)) {
            return false;
        }

        Writer responseWriter = new StringWriter();
        HttpServletRequest req = new HttpServletRequest(requestHeader.parameter());
        HttpServletResponse res = new HttpServletResponse(responseWriter);

        try {
            Object servlet = clazz.getConstructor().newInstance();
            Method clazzMethod = clazz.getMethod(SimpleServlet.GET_METHOD, HttpServletRequest.class, HttpServletResponse.class);

            clazzMethod.invoke(servlet, req, res);

        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Servlet instantiation error", e);
        }

        if (requestHeader.isHttpRequest()) {
            int responseLength = responseWriter.toString().getBytes().length;
            new ResponseHeader(requestHeader.version(), ResponseCode.OK, SERVLET_RESPONSE_CONTENT_TYPE, responseLength)
                    .writeHeader(outputStream);
        }

        Writer writer = new OutputStreamWriter(outputStream);
        writer.write(responseWriter.toString());
        writer.flush();

        return true;
    }

    private void sendFile(String contentType, OutputStream outputStream, RequestHeader requestHeader) throws IOException {
        if (contentType != null && FORBIDDEN_CONTENT_TYPES.contains(contentType)) {
            sendError(outputStream, requestHeader, ResponseCode.FORBIDDEN);
            return;
        }
        if (!responseFile.canRead()) {
            sendError(outputStream, requestHeader, ResponseCode.NOT_FOUND);
            return;
        }

        byte[] theData = Files.readAllBytes(responseFile.toPath());
        if (requestHeader.isHttpRequest()) {
            new ResponseHeader(requestHeader.version(), ResponseCode.OK, contentType, theData.length)
                .writeHeader(outputStream);
        }
        outputStream.write(theData);
        outputStream.flush();
    }

    private void sendError(OutputStream outputStream, RequestHeader requestHeader, ResponseCode responseCode) throws IOException {
        byte[] bytes = getClass().getClassLoader().getResourceAsStream(ServerConfig.getInstance().errorPagePath(requestHeader.host(), responseCode)).readAllBytes();

        if (requestHeader.isHttpRequest()) {
            new ResponseHeader(requestHeader.version(), responseCode, ERROR_RESPONSE_CONTENT_TYPE, bytes.length)
                .writeHeader(outputStream);
        }
        outputStream.write(bytes);
        outputStream.flush();
    }
}
