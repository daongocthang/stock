package com.standalone.stock;

import static junit.framework.TestCase.assertEquals;

import android.content.ContentValues;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.standalone.core.builder.DataBuilder;
import com.standalone.core.dao.Dao;
import com.standalone.core.ext.Fetcher;
import com.standalone.core.util.AnyObject;
import com.standalone.core.util.DatePicker;
import com.standalone.stock.db.Stock;
import com.standalone.stock.db.ticker.Ticker;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import okhttp3.Response;

@RunWith(RobolectricTestRunner.class)
public class SandwichTest {
    @Test
    public void testAnyObject() throws IOException, ExecutionException, InterruptedException {
        Response response = Fetcher.from("https://ai.vietcap.com.vn/api/get_all_tickers").get();
        assert response.body() != null;
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<HashMap<String, ?>> typeRef = new TypeReference<HashMap<String, ?>>() {
        };
        Map<String, ?> map = mapper.readValue(response.body().string(), typeRef);
        if (!map.containsKey("record_count")) throw new IOException();
        Object any = map.get("record_count");
        if (any == null) throw new IOException();
        System.out.println(any.getClass().getTypeName());
        Object count = AnyObject.convert(any, Integer.class);
        assertEquals(count, Long.class);
    }

    @Test
    public void testContentBuilder() {
        Stock stock = new DataBuilder().add(Stock.Fields.ticker, "AAA")
                .setDateFormat(Dao.DATE_FORMAT)
                .add(Stock.Fields.price, 12.1)
                .add(Stock.Fields.shares, 10000)
                .add("date", Dao.getTimestamp())
                .build(Stock.class);

        ContentValues cv = ContentBuilder.of(stock).fields(Stock.Fields.ticker,
                Stock.Fields.price,
                Stock.Fields.shares
        ).build();

        System.out.println(cv.toString());
    }
}
