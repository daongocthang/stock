package com.standalone.core.util;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.function.Consumer;

public class DatePicker {
    public static final long UTC_TODAY = MaterialDatePicker.todayInUtcMilliseconds();
    private static final String TAG = "MATERIAL_DATE_PICKER";
    private final AppCompatActivity activity;
    private String title;
    private long selection;

    private DatePicker(Builder builder) {
        this.activity = builder.activity;
        this.title = builder.title;
        this.selection = builder.selection;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSelection(long selection) {
        this.selection = selection;
    }

    public void show(Consumer<Long> callback) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder
                .datePicker()
                .setSelection(selection)
                .setTitleText(title)
                .build();
        datePicker.addOnPositiveButtonClickListener(callback::accept);
        datePicker.show(activity.getSupportFragmentManager(), TAG);
    }

    public static class Builder {
        private long selection = UTC_TODAY;
        private String title = "Select date";
        private final AppCompatActivity activity;

        public static Builder from(AppCompatActivity activity) {
            return new Builder(activity);
        }

        private Builder(AppCompatActivity activity) {
            this.activity = activity;
        }

        public Builder setTitle(String s) {
            this.title = s;
            return this;
        }

        public Builder setSelection(long millis) {
            this.selection = millis;
            return this;
        }

        public DatePicker build() {
            return new DatePicker(this);
        }
    }
}
