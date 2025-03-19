package com.standalone.stock.db.dao;

import android.database.Cursor;

import com.standalone.core.dao.Dao;
import com.standalone.stock.db.schema.Ticker;

import java.util.List;

public class TickerDao extends Dao<Ticker> {
    public Ticker getByName(String name) {
        Cursor curs = db.rawQuery("SELECT * FROM " + tableName + " WHERE name = ?", new String[]{name});
        return fetchOne(curs);
    }

    public List<Ticker> filterByName(String s) {
        Cursor curs = db.rawQuery("SELECT * FROM " + tableName + " WHERE name LIKE ?", new String[]{s + "%"});
        return fetchAll(curs);
    }

    public void insertIgnoreDuplicate(Ticker ticker) {
        if (getByName(ticker.name) != null) return;
        insert(ticker);
    }
}
