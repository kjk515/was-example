package com.jin.was;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jin.was.util.PathUtils;

public record RequestHeader(
    String method,
    String url,
    String version,
    Map<String, String> parameter,
    String host
) {

    public static RequestHeader of(InputStreamReader inputStreamReader) throws IOException {
        List<String> requestContents = requestContents(inputStreamReader);

        String[] contents = requestContents.get(0).split("\\s+");
        String[] path = contents[1].split("\\?");
        String host = requestContents.stream().filter(header -> header.startsWith("Host:")).findFirst().get().split(":")[1].trim();

        return new RequestHeader(
            contents[0],
            path[0].substring(1),
            (contents.length > 2) ? contents[2] : null,
            path.length > 1 ? PathUtils.paramToMap(path[1]) : new HashMap<>(),
            host
        );
    }

    public boolean isGetMethod() {
        return this.method.equals("GET");
    }

    public boolean isHttpRequest() {
        return this.version.startsWith("HTTP/");
    }
    
    private static List<String> requestContents(InputStreamReader inputStreamReader) throws IOException {
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String line;
        List<String> result = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            result.add(line);
            if (line.isEmpty()) { // end of headers
                break;
            }
        }
        
        return result;
    }
}
