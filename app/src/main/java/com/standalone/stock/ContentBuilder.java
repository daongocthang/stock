package com.standalone.stock;

import android.content.ContentValues;

import com.standalone.core.util.StrUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContentBuilder<T> {
    private final List<String> fieldNameList = new ArrayList<>();
    private final T target;

    public static <T> ContentBuilder<T> from(T target) {
        return new ContentBuilder<>(target);
    }

    public ContentBuilder<T> fields(String... names) {
        fieldNameList.addAll(Arrays.asList(names));
        return this;
    }

    ContentBuilder(T target) {
        this.target = target;
    }

    public ContentValues build() {
        ContentValues cv = new ContentValues();
        try {
            for (String fieldName : fieldNameList) {
                String name = StrUtil.camelToSnake(fieldName);
                Field field = target.getClass().getDeclaredField(fieldName);
                Object value = field.get(target);
                if (value == null) {
                    cv.putNull(name);
                    return cv;
                } else if (canAssign(field.getType(), long.class)) {
                    cv.put(name, (long) value);
                } else if (canAssign(field.getType(), int.class)) {
                    cv.put(name, (int) value);
                } else if (canAssign(field.getType(), double.class)) {
                    cv.put(name, (double) value);
                } else if (canAssign(field.getType(), float.class)) {
                    cv.put(name, (float) value);
                } else if (canAssign(field.getType(), boolean.class)) {
                    cv.put(name, (boolean) value);
                } else {
                    cv.put(name, String.valueOf(value));
                }
            }
            return cv;
        } catch (Exception e) {
            throw new RuntimeException("Appears an error with " + e.getMessage());
        }
    }

    private boolean canAssign(Class<?> a, Class<?> b) {
        return b.isAssignableFrom(a);
    }
}
