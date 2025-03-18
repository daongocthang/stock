package com.standalone.core.util;

import android.view.ViewGroup;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Inspection {

    @SuppressWarnings("unchecked")
    public static <T> List<T> findView(Class<T> viewType, ViewGroup view) {
        List<T> results = new ArrayList<>();
        int len = view.getChildCount();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                T child = (T) view.getChildAt(i);
                if (viewType.isInstance(child)) {
                    results.add(child);
                } else if (child instanceof ViewGroup) {
                    int count = ((ViewGroup) child).getChildCount();
                    if (count > 0) {
                        results.addAll(findView(viewType, (ViewGroup) child));
                    }
                }
            }
        }

        return results;
    }

    public static List<Field> getInheritedFields(Class<?> type) {
        List<Field> results = new ArrayList<>();
        Class<?> i = type;
        while (i != null && i != Object.class) {
            Collections.addAll(results, i.getDeclaredFields());
            i = i.getSuperclass();
        }
        return results;
    }
}
