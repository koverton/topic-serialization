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

    @Test
    public void applyGetterTest() {
        Foo foo = new Foo(54321, "shoes", 54.321, 1234 );
        Map<String, Method> getters = ReflectionHelper.getters(foo.getClass());

        assertEquals( "54321", ReflectionHelper.applyGetter(getters.get("id"), foo));
        assertEquals( "shoes", ReflectionHelper.applyGetter(getters.get("itemname"), foo));
        assertEquals( "54.321", ReflectionHelper.applyGetter(getters.get("price"), foo));
        assertEquals( "1234", ReflectionHelper.applyGetter(getters.get("quantity"), foo));
    }
}
