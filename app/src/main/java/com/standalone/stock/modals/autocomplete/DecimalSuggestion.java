package com.standalone.stock.modals.autocomplete;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.standalone.core.adapter.AutoCompleteAdapter;
import com.standalone.core.util.NumericFormat;
import com.standalone.stock.databinding.SimpleItemLineBinding;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class DecimalSuggestion extends AutoCompleteAdapter<String> {
    private final int size;

    public DecimalSuggestion(int size) {
        this.size = size;
    }

    @Override
    public List<String> getFilterResults(CharSequence constraint) {
        List<String> results = new ArrayList<>();
        String s = constraint.toString();
        try {
            long value = NumericFormat.parse(s).longValue();
            if (value > 0) {
                int i = 0;
                while (i + s.length() < size) {
                    i++;
                    long nextValue = (long) (value * Math.pow(10, i));
                    results.add(NumericFormat.format(nextValue));
                }
            }
        } catch (ParseException ignore) {
        }

        return results;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        SimpleItemLineBinding itemBinding;
        if (view == null) {
            itemBinding = SimpleItemLineBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        } else {
            itemBinding = SimpleItemLineBinding.bind(view);
        }

        itemBinding.tvItem.setText(itemList.get(i));
        return itemBinding.getRoot();
    }
}
