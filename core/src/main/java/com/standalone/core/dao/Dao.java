package com.standalone.core.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.standalone.core.util.StrUtil;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
        createTableIfNotExist();
    }

    public Dao() {
        this.cls = getClassType();
        this.db = DatabaseManager.getInstance().getDb();
        tableName = StrUtil.pluralize(cls.getSimpleName());
        createTableIfNotExist();
    }

    public void clear() {
        db.execSQL("DELETE FROM " + tableName);
    }

    public long insert(T t) {
        return db.insert(tableName, null, parseContentValues(t));
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
        db.update(tableName, parseContentValues(t), "_id = ?", new String[]{String.valueOf(id)});
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
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                t = parseModel(cursor);
            }
            cursor.close();
        }
        return t;
    }

    protected List<T> fetchAll(Cursor cursor) {
        List<T> rows = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    rows.add(parseModel(cursor));
                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        return rows;
    }

    protected ContentValues parseContentValues(T t) {
        ContentValues cv = new ContentValues();
        accessDeclaredFields(new FieldAccessor() {
            @Override
            public void onAccess(Field field, Column column) throws IllegalAccessException {
                if (column.ready_only()) return;

                Object value = field.get(t);
                Class<?> type = field.getType();
                String fieldName = getFieldName(column.name(), field.getName());
                if (value == null) return;

                if (canAssign(type, Integer.class)) {
                    cv.put(fieldName, (int) value);
                } else if (canAssign(type, Long.class)) {
                    cv.put(fieldName, (long) value);
                } else if (canAssign(type, Double.class)) {
                    cv.put(fieldName, (double) value);
                } else if (canAssign(type, Boolean.class)) {
                    cv.put(fieldName, (boolean) value);
                } else {
                    cv.put(fieldName, String.valueOf(value));
                }

            }
        });
        return cv;
    }

    protected T parseModel(Cursor cursor) {
        try {
            T t = cls.newInstance();
            accessDeclaredFields(new FieldAccessor() {
                @Override
                public void onAccess(Field field, Column column) throws IllegalAccessException {
                    Object value = null;
                    Class<?> type = field.getType();
                    String fieldName = getFieldName(column.name(), field.getName());
                    int colIndex = cursor.getColumnIndex((column.ready_only() ? "_" : "") + fieldName);
                    if (canAssign(type, Integer.class)) {
                        value = cursor.getInt(colIndex);
                    } else if (canAssign(type, Long.class)) {
                        value = cursor.getLong(colIndex);
                    } else if (canAssign(type, Double.class)) {
                        value = cursor.getDouble(colIndex);
                    } else if (canAssign(type, Boolean.class)) {
                        value = cursor.getInt(colIndex) > 0;
                    } else if (canAssign(type, String.class)) {
                        value = cursor.getString(colIndex);
                    }

                    if (value != null) field.set(t, value);
                }
            });
            return t;
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private void accessDeclaredFields(FieldAccessor accessor) {
        try {
            for (Field field : getInheritedFields(cls)) {
                Column column = field.getAnnotation(Column.class);
                field.setAccessible(true);
                if (column == null) continue;
                accessor.onAccess(field, column);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
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
        return a.isAssignableFrom(b);
    }

    private void createTableIfNotExist() {
        List<String> cols = new ArrayList<>();
        accessDeclaredFields(new FieldAccessor() {
            @Override
            public void onAccess(Field field, Column column) throws IllegalAccessException {
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
                    throw new DataTypeException();
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

    static class DataTypeException extends RuntimeException {

    }

    interface FieldAccessor {
        void onAccess(Field field, Column column) throws IllegalAccessException;
    }


    @SuppressWarnings("unchecked")
    public Class<T> getClassType() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        assert parameterizedType != null;
        return (Class<T>) parameterizedType.getActualTypeArguments()[0];
    }
}
