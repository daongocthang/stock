package com.standalone.core.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class NumericFormat {

    public static void watch(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // do nothing
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable == null || editable.toString().length() == 0)
                    return;

                try {
                    editText.removeTextChangedListener(this);

                    String s = editable.toString();
                    if (s.length() > 23) {
                        s = s.substring(0, s.length() - 1);
                    } else {
                        Number number = parse(s);
                        s = format(number);
                    }
                    editText.setText(s);
                    editText.setSelection(s.length());
                } catch (Exception e) {
                    //ignore
                } finally {
                    editText.addTextChangedListener(this);
                }
            }
        });
    }

    public static String format(Number number) {
        return NumberFormat.getInstance(Locale.US).format(number);
    }

    public static Number parse(String s) throws ParseException {
        return NumberFormat.getInstance(Locale.US).parse(sanifyText(s));
    }


    public static String sanifyText(String s) {
        return s.trim().replace(",", "");
    }
}
