package com.standalone.core.builder;

import android.text.TextUtils;

import com.standalone.core.builder.annotation.MetaData;
import com.standalone.core.util.Inspection;
import com.standalone.core.util.NumericFormat;
import com.standalone.core.util.TimeMillis;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataBuilder {
    Map<String, Object> dict = new HashMap<>();
    String dateFormat = "yyyy-MM-dd";

    static final Map<String, Method> CONVERTERS = new HashMap<>();

    static {
        Method[] methods = DataBuilder.class.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("_")) {
                String name = method.getParameterTypes()[0].getSimpleName() + method.getName();
                CONVERTERS.put(name, method);
            }
        }
    }

    public DataBuilder dispose() {
        dict.clear();
        return this;
    }


    public DataBuilder setDateFormat(String s) {
        this.dateFormat = s;
        return this;
    }

    public DataBuilder add(String key, Object value) {
        this.dict.put(key, value);
        return this;
    }

    public DataBuilder add(Map<String, Object> map) {
        this.dict.putAll(map);
        return this;
    }


    private boolean hasKey(String key) {
        return dict.containsKey(key);
    }

    public <T> T build(Class<T> cls) {
        return build(cls, false);
    }

    public <T> T build(Class<T> cls, boolean inherited) {
        try {
            T t = cls.newInstance();
            List<Field> fieldList = inherited ? Inspection.getInheritedFields(cls) : Arrays.asList(t.getClass().getDeclaredFields());
            for (Field field : fieldList) {
                MetaData metaData = field.getAnnotation(MetaData.class);
                if (metaData == null) continue;

                String fieldName = (TextUtils.isEmpty(metaData.tag())) ? field.getName() : metaData.tag();

                // Ignore generated properties by itself
                if (fieldName.startsWith("$")) continue;

                if (!hasKey(fieldName))
                    throw new RuntimeException(String.format("%s does not exist.", fieldName));

                Object value = convert(dict.get(fieldName), metaData.type());
                field.setAccessible(true);
                field.set(t, value);
            }
            return t;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Object convert(Object any, DataType type) {
        if (any == null) return null;
        String className = any.getClass().getSimpleName();
        if (type.equal(className)) return any;

        String error = "Cannot convert " + any.getClass().getSimpleName() + " to " + type;
        String converterId = "String_" + type;
        Method method = CONVERTERS.get(converterId);
        if (method == null)
            throw new UnsupportedOperationException(error + ". Requested converter does not exist.");

        try {
            String s = String.valueOf(any);
            return method.invoke(this, s);
        } catch (Exception e) {
            throw new RuntimeException(error + ". Conversion failed with " + e.getMessage());
        }
    }

    Long _Time(String s) throws ParseException {
        return TimeMillis.parse(s, dateFormat);
    }

    Boolean _Boolean(String s) {
        return Boolean.valueOf(s);
    }

    Number _Number(String s) throws ParseException {
        return NumericFormat.parse(s);
    }

    String _String(String s) {
        return s;
    }
}
