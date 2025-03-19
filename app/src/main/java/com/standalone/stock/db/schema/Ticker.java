package com.standalone.stock.db.schema;

import com.standalone.core.builder.DataType;
import com.standalone.core.builder.annotation.MetaData;
import com.standalone.core.dao.Column;
import com.standalone.stock.db.dao.TickerDao;

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
    public final static TickerDao DAO = new TickerDao();

    public void migrate() {
        DAO.insertIgnoreDuplicate(this);
    }
}
