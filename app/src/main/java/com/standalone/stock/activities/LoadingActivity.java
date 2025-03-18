package com.standalone.stock.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.standalone.core.builder.DataBuilder;
import com.standalone.core.ext.Fetcher;
import com.standalone.core.util.AnyObject;
import com.standalone.core.util.NetworkUtil;
import com.standalone.stock.databinding.ActivityLoadingBinding;
import com.standalone.stock.db.ticker.Ticker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import lombok.SneakyThrows;
import okhttp3.Response;

public class LoadingActivity extends AppCompatActivity {
    ActivityLoadingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoadingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        try {
            if (!NetworkUtil.isNetworkAvailable(this)) throw new InterruptedException();
            performLoadData(prefetch(), this::transitMainActivity);
        } catch (IOException | ExecutionException | InterruptedException e) {
            transitMainActivity();
        }
    }

    void transitMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    void performLoadData(Map<String, Object>[] values, Runnable completion) {
        final Executor executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler();
        executor.execute(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                final DataBuilder builder = new DataBuilder();
                final int total = values.length;
                int count = 0;
                for (Map<String, Object> map : values) {
                    builder.dispose().add(map).build(Ticker.class).migrate();
                    count += 1;
                    final int progress = Math.round((float) count * 100 / total);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            String s = "Loading... " + progress + "%";
                            binding.progressStatus.setText(s);
                            binding.progressBar.setProgress(progress);
                        }
                    });
                    Thread.sleep(1);
                }

                completion.run();
            }
        });
    }

    @SuppressWarnings("unchecked")
    Map<String, Object>[] prefetch() throws IOException, ExecutionException, InterruptedException {
        Response response = Fetcher.from("https://ai.vietcap.com.vn/api/get_all_tickers").get();
        if (response.body() == null) throw new IOException();
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<HashMap<String, ?>> typeRef = new TypeReference<HashMap<String, ?>>() {
        };
        Map<String, ?> map = mapper.readValue(response.body().string(), typeRef);
        if (!map.containsKey("record_count")) throw new IOException();
        Object any = map.get("record_count");
        if (any == null) throw new IOException();
        int i = AnyObject.convert(any, Integer.class);
        if (Ticker.DAO.count() == i) throw new IOException();

        if (!map.containsKey("ticker_info")) throw new IOException();
        List<?> list = (List<?>) map.get("ticker_info");
        if (list == null) throw new IOException();

        List<Map<String, Object>> results = new ArrayList<>();
        for (Object ob : list) {
            if (LinkedHashMap.class.isAssignableFrom(ob.getClass())) {
                results.add((Map<String, Object>) ob);
            }
        }

        return results.toArray(new Map[0]);
    }
}
