package com.standalone.stock.activities;

import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.standalone.stock.databinding.ActivitySettingsBinding;
import com.standalone.stock.settings.Config;

import java.util.Locale;
import java.util.function.Consumer;

public class SettingsActivity extends AppCompatActivity {
    private Config config;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySettingsBinding binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        config = Config.of(this);
        String s=
        binding.tvTax.setText(concat());

        initSeekbar(binding.sbTax, binding.tvTax, config.getTax(), 100);
        initSeekbar(binding.sbCost, binding.tvCost, config.getCost(), 100);
        initSeekbar(binding.sbStopLoss, binding.tvStopLoss, config.getStopLoss(), 1);
    }

    private void watch(SeekBar sb, Consumer<SeekBar> changed) {
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                changed.accept(seekBar);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private String concat(String s, double d) {
        return String.format(Locale.getDefault(), "%s\t%.2f", s, d);
    }
}
