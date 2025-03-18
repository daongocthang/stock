package com.standalone.stock.modals.autocomplete;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.standalone.core.adapter.AutoCompleteAdapter;
import com.standalone.stock.databinding.ItemTickerBinding;
import com.standalone.stock.db.ticker.Ticker;

import java.util.ArrayList;
import java.util.List;

public class TickerSuggestion extends AutoCompleteAdapter<Ticker> {
    private final Context context;

    public TickerSuggestion(Context context) {
        this.context = context;
        this.itemList = new ArrayList<>();
    }

    @Override
    public long getItemId(int position) {
        return itemList.get(position).id;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        final ItemTickerBinding binding;
        if (view == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            binding = ItemTickerBinding.inflate(inflater, parent, false);
            view = binding.getRoot();
        } else {
            binding = ItemTickerBinding.bind(view);
        }

        Ticker ticker = getItem(position);
        binding.tvTicker.setText(ticker.name);
        binding.tvOrgan.setText(ticker.organ);

        return view;
    }

    @Override
    protected List<Ticker> getFilterResults(CharSequence constraint) {
        return Ticker.DAO.filterByName(constraint.toString());
    }
}
