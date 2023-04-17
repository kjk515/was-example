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
import java.util.logging.Level;

import com.jin.was.config.ServerConfig;
import com.jin.was.servlet.HttpServletRequest;
import com.jin.was.servlet.HttpServletResponse;
import com.jin.was.servlet.SimpleServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        } catch (IOException ex) {
            logger.warn("Error talking to: {} ", connection.getRemoteSocketAddress(), ex);
        } catch (Exception ex) {
            logger.warn("Exception ", ex);
        } finally {
            try {
                connection.close();
            } catch (IOException ex) {
            }
        }
    }

    private boolean sendServlet(OutputStream outputStream, RequestHeader requestHeader) throws IOException {
        Writer writer = new OutputStreamWriter(outputStream);
        try {
            String className = requestHeader.url();
            Class<?> clazz = Class.forName(className);
            if (!SimpleServlet.class.isAssignableFrom(clazz)) {
                return false;
            }

            Object servlet = clazz.getConstructor().newInstance();
            Method clazzMethod = clazz.getMethod(SimpleServlet.GET_METHOD, HttpServletRequest.class, HttpServletResponse.class);

            HttpServletRequest req = new HttpServletRequest(requestHeader.parameter());
            Writer responseWriter = new StringWriter();
            HttpServletResponse res = new HttpServletResponse(responseWriter);

            clazzMethod.invoke(servlet, req, res);

            if (requestHeader.isHttpRequest()) {
                new ResponseHeader(requestHeader.version(), ResponseCode.OK, SERVLET_RESPONSE_CONTENT_TYPE, responseWriter.toString().getBytes().length)
                    .writeHeader(outputStream);
            }

            writer.write(responseWriter.toString());
            writer.flush();

            return true;

        } catch (ClassNotFoundException e) {
            logger.info("Not found in Servlets");
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Servlet instantiation error");
        }

        return false;
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
