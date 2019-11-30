package com.solace.dev;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class ReflectionHelperTest {

    @Test
    public void gettersTest() {
        Map<String, Method> getters = ReflectionHelper.getters(Foo.class);
        assertEquals(5, getters.size()); // including 'getClass'
        assertTrue( getters.containsKey("quantity") );
        assertTrue( getters.containsKey("itemname") );
        assertTrue( getters.containsKey("price") );
        assertTrue( getters.containsKey("id") );
    }
}
