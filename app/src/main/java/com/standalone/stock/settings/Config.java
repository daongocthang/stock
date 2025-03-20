package com.standalone.stock.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.standalone.core.App;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.Properties;

import lombok.experimental.FieldNameConstants;

@FieldNameConstants
public class Config {
    public static final String TAG = "config_prefs";
    private static final String TAX = "tax";
    private static final String COST = "transaction_cost";
    private static final String STOP_LOSS = "stop_loss";

    private final Properties env;
    private final SharedPreferences prefs;

    public static Config of(Context context) {
        return new Config(context);
    }

    private Config(Context context) {
        this.prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        this.env = App.loadEnv();
    }

    public float getTax() {
        return prefs.getFloat(TAX, defValue(TAX));
    }

    public void setTax(int i) {
        prefs.edit().putFloat(TAX, i).apply();
    }

    public float getCost() {
        return prefs.getFloat(COST, defValue(COST));
    }

    public void setCost(int i) {
        prefs.edit().putFloat(COST, i).apply();
    }

    public float getStopLoss() {
        return prefs.getFloat(STOP_LOSS, defValue(STOP_LOSS));
    }

    public void setStopLoss(int i) {
        prefs.edit().putFloat(STOP_LOSS, i).apply();
    }

    float defValue(String s) {
        return Float.parseFloat(env.getProperty(StringUtils.upperCase(s)));
    }
}
