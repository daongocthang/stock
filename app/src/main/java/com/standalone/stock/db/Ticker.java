package com.standalone.stock.db;

import com.standalone.core.builder.DataType;
import com.standalone.core.builder.annotation.MetaData;
import com.standalone.core.dao.Column;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Ticker {
    @Column(ready_only = true)
    public long id;
    @Column
    @MetaData(tag = "ticker", type = DataType.STRING)
    public String name;
    @Column
    @MetaData(tag = "organ_name", type = DataType.STRING)
    public String organ;

    public static TickerDao DAO = new TickerDao();

    public boolean migrate() {
        return DAO.insertIgnoreDuplicate(this) >= 0;
    }
}
