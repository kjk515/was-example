package com.jin.was;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

public class HttpServerTest {

    @Test
    public void testServer() {
        Map<String, String> map = new HashMap<>();
        map.put("404", "404error");
        map.put("501", "501error");
        List<Map<String, String>> list = new ArrayList<>();
        list.add(map);

        Optional<String> any = list.stream().map(it -> it.get("501")).findFirst();

        assertTrue(any.isPresent());
    }

    @Test
    public void testCanonicalPath() throws IOException {
        String path = "/Users/we/Documents/Java-WAS/dev1/../dev1/404.html";
        File file = new File(path);
        String path1 = file.getPath();
        String canonicalPath = file.getCanonicalPath();
        assertNotEquals(file.getPath(), file.getCanonicalPath());
    }
}
