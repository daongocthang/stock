package com.standalone.core.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.standalone.core.util.AnyObject;
import com.standalone.core.util.StrUtil;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import lombok.SneakyThrows;

public class Dao<T> {
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    protected final String tableName;
    protected final SQLiteDatabase db;

    final Class<T> cls;

    public static String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        return sdf.format(Calendar.getInstance().getTime());
    }

    public static <T> Dao<T> of(Class<T> cls) {
        return new Dao<>(cls);
    }

    private Dao(Class<T> cls) {
        this.cls = cls;
        this.db = DatabaseManager.getInstance().getDb();
        tableName = StrUtil.pluralize(cls.getSimpleName());
        try {
            createTableIfNotExist();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Dao() {
        this.cls = getClassType();
        this.db = DatabaseManager.getInstance().getDb();
        tableName = StrUtil.pluralize(cls.getSimpleName());
        try {
            createTableIfNotExist();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void clear() {
        db.execSQL("DELETE FROM " + tableName);
    }

    public void insert(T t) {
        try {
            db.insert(tableName, null, convertToContentValues(t));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public List<T> list() {
        Cursor curs = db.rawQuery("SELECT * FROM " + tableName, null);
        return fetchAll(curs);
    }

    public T get(long id) {
        Cursor curs = db.rawQuery("SELECT * FROM " + tableName + " WHERE _id = ?", new String[]{String.valueOf(id)});
        return fetchOne(curs);
    }

    @Deprecated
    public void update(long id, T t) {
        try {
            db.update(tableName, convertToContentValues(t), "_id = ?", new String[]{String.valueOf(id)});
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(long id, ContentValues cv) {
        db.update(tableName, cv, "_id = ?", new String[]{String.valueOf(id)});
    }

    public void delete(long id) {
        db.delete(tableName, "_id = ?", new String[]{String.valueOf(id)});
    }


    public long count() {
        return DatabaseUtils.queryNumEntries(db, tableName);
    }


    protected T fetchOne(Cursor cursor) {
        T t = null;
        if (cursor == null) return null;

        try (cursor) {
            if (cursor.moveToFirst()) {
                t = convertToObject(cursor);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException();
        }

        return t;
    }

    protected List<T> fetchAll(Cursor cursor) {
        List<T> rows = new ArrayList<>();
        if (cursor == null) return rows;

        try (cursor) {
            if (cursor.moveToFirst()) {
                do {
                    rows.add(convertToObject(cursor));
                } while (cursor.moveToNext());

            }
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException();
        }

        return rows;
    }

    protected ContentValues convertToContentValues(T t) throws IllegalAccessException {
        ContentValues cv = new ContentValues();
        accessDeclaredFields(field -> {
            Column column = field.getAnnotation(Column.class);
            assert column != null;
            if (column.ready_only()) return;

            Object value = field.get(t);
            Class<?> type = field.getType();
            String fieldName = getFieldName(column.name(), field.getName());
            if (value == null) return;

            if (canAssign(type, int.class)) {
                cv.put(fieldName, (int) value);
            } else if (canAssign(type, long.class)) {
                cv.put(fieldName, (long) value);
            } else if (canAssign(type, double.class)) {
                cv.put(fieldName, (double) value);
            } else if (canAssign(type, boolean.class)) {
                cv.put(fieldName, (boolean) value);
            } else {
                cv.put(fieldName, String.valueOf(value));
            }
        });
        return cv;
    }

    protected T convertToObject(Cursor cursor) throws IllegalAccessException, InstantiationException {
        T t = cls.newInstance();
        accessDeclaredFields(field -> {
            Object value = null;
            Column column = field.getAnnotation(Column.class);
            assert column != null;
            Class<?> type = field.getType();
            String fieldName = getFieldName(column.name(), field.getName());
            int colIndex = cursor.getColumnIndex((column.ready_only() ? "_" : "") + fieldName);

            if (canAssign(type, long.class)) {
                value = cursor.getLong(colIndex);
            } else if (canAssign(type, int.class)) {
                value = cursor.getInt(colIndex);
            } else if (canAssign(type, double.class)) {
                value = cursor.getDouble(colIndex);
            } else if (canAssign(type, float.class)) {
                value = cursor.getFloat(colIndex);
            } else if (canAssign(type, boolean.class)) {
                value = cursor.getInt(colIndex) > 0;
            } else if (canAssign(type, String.class)) {
                value = cursor.getString(colIndex);
            }

            if (value != null) field.set(t, value);
        });
        return t;
    }

    private void accessDeclaredFields(Accessor<Field> action) throws IllegalAccessException {
        for (Field field : getInheritedFields(cls)) {
            Column column = field.getAnnotation(Column.class);
            if (column == null) continue;
            field.setAccessible(true);
            action.access(field);
        }
    }

    private List<Field> getInheritedFields(Class<?> type) {
        List<Field> results = new ArrayList<>();
        Class<?> i = type;
        while (i != null && i != Object.class) {
            Collections.addAll(results, i.getDeclaredFields());
            i = i.getSuperclass();
        }

        return results;
    }


    private boolean canAssign(Class<?> a, Class<?> b) {
        return b.isAssignableFrom(a);
    }

    private void createTableIfNotExist() throws IllegalAccessException {
        List<String> cols = new ArrayList<>();
        accessDeclaredFields(field -> {
            {
                Column column = field.getAnnotation(Column.class);
                assert column != null;
                StringBuilder builder = new StringBuilder();
                String fieldName = getFieldName(column.name(), field.getName());
                builder.append(column.ready_only() ? "_" : "").append(fieldName).append(" ");
                Class<?> type = field.getType();
                if (canAssign(type, int.class) || canAssign(type, long.class) || canAssign(type, boolean.class)) {
                    builder.append("INTEGER");
                } else if (canAssign(type, double.class) || canAssign(type, float.class)) {
                    builder.append("REAL");
                } else if (canAssign(type, String.class)) {
                    builder.append("TEXT");
                } else {
                    throw new RuntimeException();
                }

                if (column.ready_only()) {
                    builder.append(" PRIMARY KEY AUTOINCREMENT");
                }

                cols.add(builder.toString());
            }
        });

        Collections.sort(cols);
        String sql = String.format("CREATE TABLE IF NOT EXISTS %s(%s);", tableName, String.join(", ", cols));
        db.execSQL(sql);
    }

    private String getFieldName(String s, String defaultValue) {
        return StrUtil.camelToSnake(StrUtil.getOrEmpty(s, defaultValue));
    }

    @SuppressWarnings("unchecked")
    public Class<T> getClassType() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        assert parameterizedType != null;
        return (Class<T>) parameterizedType.getActualTypeArguments()[0];
    }

    @FunctionalInterface
    interface Accessor<T> {
        void access(T t) throws IllegalAccessException;

    }
}