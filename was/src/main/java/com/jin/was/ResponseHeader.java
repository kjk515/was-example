package com.jin.was;

public record ResponseHeader(
    String responseCode,
    String contentType,
    int contentLength,
    Date date,
    String server
) {
    private static final String SERVER = "JHTTP 2.0"; // TODO

    public ResponseHeader(String responseCode, String contentType, int contentLength) {
        this(responseCode, contentType, contentLength, new Date(), ResponseHeader.SERVER);
    }

    public void writeHeader(OutputStream outputStream) throws IOException {
        Writer writer = new OutputStreamWriter(outputStream);
        writer.write(responseCode + "\r\n");
        writer.write("Date: " + date + "\r\n");
        writer.write("Server: " + server + "\r\n");
        writer.write("Content-length: " + contentLength + "\r\n");
        writer.write("Content-type: " + contentType + "\r\n");
        writer.write("\r\n");
        writer.flush();
    }
}
