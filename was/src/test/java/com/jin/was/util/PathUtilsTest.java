package com.jin.was.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.Test;

public class PathUtilsTest {

    @Test
    public void testIncludeAffixRootPath() {
        String actual = PathUtils.affixRootPath("/webapps");
        assertEquals("/webapps", actual);
    }

    @Test
    public void testExcludeAffixRootPath() {
        String actual = PathUtils.affixRootPath("webapps");
        assertEquals("/webapps", actual);
    }

    @Test
    public void testParamToMap() {
        Map<String, String> paramMap = PathUtils.paramToMap("param1=hello&param2=jkkang");
        assertEquals(2, paramMap.size());
        assertEquals("hello", paramMap.get("param1"));
        assertEquals("jkkang", paramMap.get("param2"));
    }

    @Test
    public void testCanonicalPath() throws IOException {
        String path = "/Users/we/Documents/Java-WAS/dev1/../dev1/404.html";
        File file = new File(path);
        assertNotEquals(file.getPath(), file.getCanonicalPath());
    }
}
