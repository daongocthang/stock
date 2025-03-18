package com.standalone.core.builder;

import androidx.annotation.NonNull;

public enum DataType {
    TIME("Time"),
    NUMBER("Number"),
    BOOLEAN("Boolean"),
    STRING("String");

    final String name;

    DataType(String name) {
        this.name = name;
    }

    public boolean equal(String s) {
        return name.equals(s);
    }

    @NonNull
    public String toString() {
        return name;
    }

}
