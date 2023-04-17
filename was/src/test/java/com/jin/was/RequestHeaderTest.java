package com.jin.was;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.Test;

public class RequestHeaderTest {

    @Test
    public void testOf() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("requestHeader");
        RequestHeader requestHeader = RequestHeader.of(new InputStreamReader((inputStream), StandardCharsets.UTF_8));

        assertTrue(requestHeader.isGetMethod());
        assertEquals("com.jin.clock.servlet.Hello", requestHeader.url());
        assertEquals(Map.of("name", "jkkang"), requestHeader.parameter());
        assertEquals("www.dev1.com", requestHeader.host());
    }
}
