package com.solace.dev;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ReflectionHelper {
    static final private Pattern pattern = Pattern.compile("get(.+)");

    static Map<String, Method> getters(Class clazz) {
        Map<String,Method> result = new HashMap<>();
        for(Method method : clazz.getMethods()) {
            if (isGetter(method)) {
                Matcher matcher = pattern.matcher(method.getName());
                if (matcher.find()) {
                    String field = matcher.group(1);
                    result.put(field.toLowerCase(), method);
                }
            }
        }
        return result;
    }

    static private boolean isGetter(Method method){
        if(!method.getName().startsWith("get"))       return false;
        if(method.getParameterTypes().length != 0)    return false;
        return !void.class.equals(method.getReturnType());
    }
}
