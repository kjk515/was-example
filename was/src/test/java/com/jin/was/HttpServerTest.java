package com.jin.was;

import static org.junit.Assert.assertTrue;

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
}
