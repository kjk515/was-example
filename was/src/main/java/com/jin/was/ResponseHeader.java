package com.jin.was;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;

public record ResponseHeader(
    String version,
    ResponseCode responseCode,
    String contentType,
    int contentLength,
    Date date,
    String server
) {
    private static final String SERVER = "JHTTP 2.0";

    public ResponseHeader(String version, ResponseCode responseCode, String contentType, int contentLength) {
        this(version, responseCode, contentType, contentLength, new Date(), ResponseHeader.SERVER);
    }

    public void writeHeader(OutputStream outputStream) throws IOException {
        Writer writer = new OutputStreamWriter(outputStream);
        writer.write(version + " " + responseCode.contents + "\r\n");
        writer.write("Date: " + date + "\r\n");
        writer.write("Server: " + server + "\r\n");
        writer.write("Content-length: " + contentLength + "\r\n");
        writer.write("Content-type: " + contentType + "\r\n");
        writer.write("\r\n");
        writer.flush();
    }
}
