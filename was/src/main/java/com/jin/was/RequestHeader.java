package com.jin.was;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public record RequestHeader(
    String method,
    String url,
    String version,
    String host
) {

    public static RequestHeader of(InputStreamReader inputStreamReader) throws IOException {
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String line;
        List<String> requestContents = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            requestContents.add(line);
            if (line.isEmpty()) { // end of headers
                break;
            }
        }

        String[] tokens = requestContents.get(0).split("\\s+");
        String host = requestContents.stream().filter(header -> header.startsWith("Host:")).findFirst().get().split(":")[1].trim();

        return new RequestHeader(
            tokens[0],
            tokens[1].substring(1),
            (tokens.length > 2) ? tokens[2] : null,
            host
        );
    }

    public boolean isGetMethod() {
        return this.method.equals("GET");
    }

    public boolean isHttpRequest() {
        return this.version.startsWith("HTTP/");
    }
}
