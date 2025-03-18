package com.standalone.stock;

import android.content.ContentValues;

import com.standalone.core.util.StrUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;

public class ContentBuilder<T> {
    private static final Map<String, Method> CONVERTERS = new HashMap<>();

    static {
        Method[] methods = ContentBuilder.class.getDeclaredMethods();
        for (Method method : methods) {
            if (!method.getName().startsWith("_")) continue;
            String s = method.getParameterTypes()[1].getName();
            CONVERTERS.put(s, method);
        }
    }


    private final List<String> fieldNameList = new ArrayList<>();
    private final T target;
    private final ContentValues cv;

    public static <T> ContentBuilder<T> of(T target) {
        return new ContentBuilder<>(target);
    }

    public ContentBuilder<T> fields(String... names) {
        fieldNameList.addAll(Arrays.asList(names));
        return this;
    }

    ContentBuilder(T target) {
        this.target = target;
        this.cv = new ContentValues();
    }

    @SneakyThrows
    public ContentValues build() {
        for (String fieldName : fieldNameList) {
            String name = StrUtil.camelToSnake(fieldName);
            Field field = target.getClass().getDeclaredField(fieldName);
            Object value = field.get(target);
            if (value == null) {
                cv.putNull(name);
                return cv;
            }

            Method method = CONVERTERS.get(field.getType().getName());
            if (method == null) throw new UnsupportedOperationException();
            method.invoke(this, name, value);
        }

        return cv;
    }

    private void _putLong(String s, Object any) {
        cv.put(s, (Long) any);
    }

    private void _putInteger(String s, Object any) {
        cv.put(s, (Integer) any);
    }

    private void _putDouble(String s, Object any) {
        cv.put(s, (Double) any);
    }

    private void _putFloat(String s, Object any) {
        cv.put(s, (Float) any);
    }

    private void _putBoolean(String s, Object any) {
        cv.put(s, (Boolean) any);
    }

    private void _putString(String s, Object any) {
        cv.put(s, (String) any);
    }


}
