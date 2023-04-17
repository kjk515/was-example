package com.jin.was;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jin.was.servlet.HttpServletRequest;
import com.jin.was.servlet.HttpServletResponse;

public class RequestProcessor implements Runnable {

    private final static Logger logger = Logger.getLogger(RequestProcessor.class.getCanonicalName());
    private String rootPath;
    private String indexFileName = "index.html";
    private Socket connection;
    private File requestFile;

    private static final List<String> FORBIDDEN_CONTENT_TYPES = List.of(
        "application/octet-stream"
    );

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

            String matchedRootPath = rootPath != null ? rootPath : ServerConfig.getInstance().appBasePath(requestHeader.host());
            requestFile = new File(matchedRootPath, requestHeader.url());
            if (requestFile.getCanonicalPath().equals(matchedRootPath)) {
                requestFile = new File(matchedRootPath, indexFileName);
            }

            if (!requestFile.getCanonicalPath().startsWith(matchedRootPath)) {
                sendError(outputStream, requestHeader, ErrorCode.FORBIDDEN);
                return;
            }

            if (!requestHeader.isGetMethod()) {
                sendError(outputStream, requestHeader, ErrorCode.NOT_IMPLEMENTED);
                return;
            }

            String contentType = URLConnection.getFileNameMap().getContentTypeFor(requestFile.getName());

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

    private boolean sendServlet(OutputStream outputStream, RequestHeader requestHeader) throws IOException {
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
                new ResponseHeader(requestHeader.version() + " 200 OK", "text/plain; charset=utf-8", responseWriter.toString().getBytes().length)
                    .writeHeader(outputStream);
            }

            writer.write(responseWriter.toString());
            writer.flush();
            return true;
        } catch (ClassNotFoundException e) {
            logger.warning("Class Not Found!!!!!!!!!!!!!!");
            sendError(outputStream, requestHeader, ErrorCode.NOT_FOUND);
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            logger.warning("!!!!!!!!!!!!!!!!");
        }

        return false;
    }

    private void sendFile(String contentType, OutputStream outputStream, RequestHeader requestHeader) throws IOException {
        if (FORBIDDEN_CONTENT_TYPES.contains(contentType)) {
            sendError(outputStream, requestHeader, ErrorCode.FORBIDDEN);
            return;
        }

        if (requestFile.canRead()) {
            byte[] theData = Files.readAllBytes(requestFile.toPath());
            if (requestHeader.version().startsWith("HTTP/")) {
                new ResponseHeader(requestHeader.version() + " 200 OK", contentType, theData.length)
                    .writeHeader(outputStream);
            }
            outputStream.write(theData);
            outputStream.flush();
        } else {
            sendError(outputStream, requestHeader, ErrorCode.NOT_FOUND);
        }
    }

    private void sendError(OutputStream outputStream, RequestHeader requestHeader, ErrorCode errorCode) throws IOException {
        byte[] bytes = getClass().getClassLoader().getResourceAsStream(ServerConfig.getInstance().errorPagePath(requestHeader.host(), errorCode)).readAllBytes();

        if (requestHeader.isHttpRequest()) {
            new ResponseHeader(requestHeader.version() + " " + errorCode.response, "text/html", bytes.length)
                .writeHeader(outputStream);
        }
        outputStream.write(bytes);
        outputStream.flush();
    }
}
