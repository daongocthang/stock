package com.standalone.core.util;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class AnyObject {
    static final Map<String, Method> CONVERTERS = new HashMap<>();

    static {
        Method[] methods = AnyObject.class.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getParameterTypes().length == 1) {
                String s = method.getParameterTypes()[0].getName() + "_" + method.getReturnType().getName();
                CONVERTERS.put(s, method);
            }
        }
    }

    public static <T> T convert(Object any, Class<T> type) {
        if (any == null) return null;

        if (type.isAssignableFrom(any.getClass())) return type.cast(any);

        String error = "Cannot convert from " + any.getClass().getName() + " to " + type.getName();
        String s = any.getClass().getName() + "_" + type.getName();
        Method converter = CONVERTERS.get(s);
        if (converter == null) {
            throw new UnsupportedOperationException(error + ". Requested converter does not exist");
        }

        try {
            return type.cast(converter.invoke(type, any));
        } catch (Exception e) {
            throw new RuntimeException(error + ". Conversion failed with " + e.getMessage());
        }
    }

    static long stringToLong(String s) {
        return Long.parseLong(s);
    }

    static String longToString(Long l) {
        return String.valueOf(l);
    }

    static int stringToInteger(String s) {
        return Integer.parseInt(s);
    }

    static String integerToString(Integer i) {
        return String.valueOf(i);
    }

    static double stringToDouble(String s) {
        return Double.parseDouble(s);
    }

    static String doubleToString(Double d) {
        return String.valueOf(d);
    }

    static float stringToFloat(String s) {
        return Float.parseFloat(s);
    }

    static String floatToString(Float f) {
        return String.valueOf(f);
    }

    static boolean stringToBoolean(String s) {
        return Boolean.parseBoolean(s);
    }

    static boolean integerToBoolean(Integer i) {
        return i > 0 ? Boolean.TRUE : Boolean.FALSE;
    }

    static String booleanToString(Boolean b) {
        return String.valueOf(b);
    }
}
