package com.standalone.stock.adapters;

import static java.lang.String.valueOf;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.standalone.core.util.NumericFormat;
import com.standalone.stock.databinding.ItemStockBinding;
import com.standalone.stock.db.schema.Stock;
import com.standalone.stock.fragments.BottomDialog;
import com.standalone.stock.settings.Config;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.ViewHolder> {

    private List<Stock> itemList;

    private final AppCompatActivity activity;


    public StockAdapter(AppCompatActivity activity) {
        this.activity = activity;
        prefetch();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final ItemStockBinding binding = ItemStockBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(itemList.get(position), activity);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void prefetch() {
        itemList = Stock.DAO.list();
        Collections.sort(itemList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemStockBinding itemBinding;

        public ViewHolder(@NonNull ItemStockBinding itemBinding) {
            super(itemBinding.getRoot());
            this.itemBinding = itemBinding;
        }

        public void bind(Stock stock, AppCompatActivity activity) {
            Config config = Config.of(activity);
            double net = stock.price * (1 + config.getCost()/100);
            double stopLoss = net * (1 - config.getStopLoss() / 100);

            itemBinding.tvTicker.setText(stock.ticker);
            itemBinding.tvPrice.setText(String.format(Locale.US, "%,.2f", net));
            itemBinding.tvShares.setText(NumericFormat.format(stock.shares));
            itemBinding.tvStopLoss.setText(String.format(Locale.US, "%,.2f", stopLoss));

            itemBinding.getRoot().setOnClickListener(view -> {
                BottomDialog.from(activity).setArgument(stock.id).show();
            });
        }
    }
}
