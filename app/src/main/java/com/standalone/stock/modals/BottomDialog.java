package com.standalone.stock.modals;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.standalone.core.builder.DataBuilder;
import com.standalone.core.dao.Dao;
import com.standalone.core.dao.Model;
import com.standalone.core.util.DatePicker;
import com.standalone.core.util.InputValidator;
import com.standalone.core.util.Inspection;
import com.standalone.core.util.NumericFormat;
import com.standalone.core.util.TimeMillis;
import com.standalone.stock.ContentBuilder;
import com.standalone.stock.R;
import com.standalone.stock.databinding.BottomDialogBinding;
import com.standalone.stock.db.Stock;
import com.standalone.stock.db.TradeRecord;
import com.standalone.stock.db.ticker.Ticker;
import com.standalone.stock.modals.autocomplete.DecimalSuggestion;
import com.standalone.stock.modals.autocomplete.TickerSuggestion;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class BottomDialog extends BottomSheetDialogFragment {
    public static final String ARG_NAME = "id";
    private static final String TAG = "BOTTOM_SHEET_DIALOG_FRAGMENT";
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private BottomDialogBinding binding;
    private final AppCompatActivity activity;
    private boolean canSell;
    private long itemId;

    public BottomDialog(AppCompatActivity activity) {
        this.activity = activity;
    }

    public static BottomDialog from(AppCompatActivity activity) {
        return new BottomDialog(activity);
    }

    public void show() {
        show(activity.getSupportFragmentManager(), TAG);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, com.standalone.core.R.style.AppTheme_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomDialogBinding.inflate(inflater, container, false);
        Dialog dialog = getDialog();
        assert dialog != null;
        Window window = dialog.getWindow();
        assert window != null;
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //init
        final Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(ARG_NAME)) {
            itemId = bundle.getLong(ARG_NAME, -1);
            canSell = itemId >= 0;
        }

        setupUiComponents();
    }

    private void setupIfSelling() {
        if (!canSell) return;

        Stock stock = Stock.DAO.get(itemId);
        binding.edTicker.setText(stock.ticker);
        binding.edTicker.setEnabled(false);

        binding.edShares.setText(NumericFormat.format(stock.shares));
        binding.edPrice.setText(String.valueOf(stock.price));
        binding.edDate.setText(TimeMillis.format(stock.matchedTime, DATE_FORMAT));

        binding.btSubmit.setText(R.string.sell);
        binding.btSubmit.setBackgroundResource(com.standalone.core.R.color.danger);
    }

    private void setupIfBuying() {
        if (canSell) return;

        binding.edTicker.setAdapter(new TickerSuggestion(activity));
        binding.edTicker.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Ticker ticker = (Ticker) adapterView.getItemAtPosition(position);
                binding.edTicker.setText(ticker.name);
            }
        });
    }

    private void setupUiComponents() {
        // TextField::Shares
        binding.edShares.setAdapter(new DecimalSuggestion(6));
        NumericFormat.watch(binding.edShares);

        // TextField::Date
        binding.edDate.setText(TimeMillis.format(DatePicker.UTC_TODAY, DATE_FORMAT));
        binding.btCalendar.setEndIconOnClickListener(new View.OnClickListener() {
            final DatePicker datePicker = DatePicker.Builder.from(activity).build();

            @Override
            public void onClick(View view) {
                try {
                    if (binding.edDate.getText() == null) throw new RuntimeException();
                    String s = binding.edDate.getText().toString();
                    datePicker.setSelection(TimeMillis.parse(s, DATE_FORMAT));
                } catch (ParseException e) {
                    datePicker.setSelection(DatePicker.UTC_TODAY);
                }

                datePicker.show((selection) -> {
                    binding.edDate.setText(TimeMillis.format(selection, DATE_FORMAT));
                });
            }
        });

        //Button::Submit
        binding.btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performTrading();
            }
        });


        setupIfSelling();
        setupIfBuying();
    }

    private void performTrading() {
        try {
            Map<String, Object> map = new HashMap<>();
            Inspection.findView(EditText.class, binding.getRoot()).forEach(editText -> {
                InputValidator.getInstance().validate(editText).notEmpty();
                map.put(editText.getTag().toString(), editText.getText().toString());
            });

            final DataBuilder builder = new DataBuilder();

            Stock stock = builder.setDateFormat(DATE_FORMAT).add(map).build(Stock.class);


            if (canSell) {
                updateOrDelete(stock);
            } else {
                Stock.DAO.insert(stock);
            }

            // Writing to history
            builder.add(TradeRecord.Fields.isSellOrder, canSell)
                    .add(TradeRecord.Fields.purchasePrice, canSell ? Stock.DAO.get(itemId).price : 0);
            TradeRecord record = builder.build(TradeRecord.class);
            TradeRecord.DAO.insert(record);

        } catch (InputValidator.ValidationError ignore) {
        }
    }

    void updateOrDelete(Stock stock) {
        Stock holder = Stock.DAO.get(itemId);
        long shares = holder.shares - stock.shares;
        if (shares < 0) {
            InputValidator.getInstance().validate(binding.edShares).setError(getString(R.string.invalid_field));
            return;
        }

        if (shares > 0) {
            stock.shares = shares;
            stock.updatedAt = Dao.getTimestamp();
            ContentValues cv = ContentBuilder.of(Stock.class).fields(
                    Stock.Fields.ticker,
                    Stock.Fields.shares,
                    Stock.Fields.price,
                    "updatedAt"
            ).build();
            Stock.DAO.update(itemId, cv);
            return;
        }

        Stock.DAO.delete(itemId);
    }
}
