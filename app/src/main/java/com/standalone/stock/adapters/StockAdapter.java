package com.standalone.stock.adapters;

import static java.lang.String.valueOf;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.standalone.core.util.NumericFormat;
import com.standalone.stock.databinding.ItemStockBinding;
import com.standalone.stock.db.schema.Stock;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.ViewHolder> {

    private List<Stock> itemList;
    public View.OnClickListener itemClickListener;

    public StockAdapter() {
        prefetch();
    }

    public void setOnItemClickListener(View.OnClickListener listener) {
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final ItemStockBinding binding = ItemStockBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(itemList.get(position), itemClickListener);
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

        public void bind(Stock stock, View.OnClickListener listener) {
            double stopLoss = stock.price * 0.93;

            itemBinding.tvTicker.setText(stock.ticker);
            itemBinding.tvPrice.setText(valueOf(stock.price));
            itemBinding.tvShares.setText(NumericFormat.format(stock.shares));
            itemBinding.tvStopLoss.setText(String.format(Locale.US, "%,.2f", stopLoss));
            if (listener != null)
                itemBinding.getRoot().setOnClickListener(listener);
        }
    }
}
