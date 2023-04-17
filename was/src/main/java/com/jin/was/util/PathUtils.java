package com.jin.was.util;

import java.util.HashMap;
import java.util.Map;

public class PathUtils {

    public static String affixRootPath(String path) {
        return path.startsWith("/") ? path : "/" + path;
    }

    public static Map<String, String> paramToMap(String paramString) {
        Map<String, String> paramMap = new HashMap<>();
        String[] params = paramString.split("&");

        for (String param : params) {
            String[] pair = param.split("=");
            if (pair.length == 2) {
                String key = pair[0];
                String value = pair[1];
                paramMap.put(key, value);
            }
        }
        return paramMap;
    }
}
