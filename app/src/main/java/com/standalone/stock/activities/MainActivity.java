package com.standalone.stock.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;

import com.standalone.stock.adapters.StockAdapter;
import com.standalone.stock.databinding.ActivityMainBinding;
import com.standalone.stock.fragments.BottomDialog;

public class MainActivity extends AppCompatActivity implements BottomDialog.OnCloseListener {
    private StockAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        adapter = new StockAdapter(this);
        binding.recyclerView.setAdapter(adapter);
        binding.fab.setOnClickListener(view -> {
            BottomDialog.from(this).show();
        });
    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onClose(DialogInterface dialog) {
        adapter.prefetch();
    }
}