package com.standalone.stock.db.schema;

import com.standalone.core.builder.DataType;
import com.standalone.core.builder.annotation.MetaData;
import com.standalone.core.dao.Column;
import com.standalone.core.dao.Dao;
import com.standalone.core.dao.Model;

import lombok.experimental.FieldNameConstants;

@FieldNameConstants
public class TradeRecord extends Model {
    @Column
    @MetaData(type = DataType.STRING)
    public String ticker;
    @Column
    @MetaData(type = DataType.NUMBER)
    public long shares;
    @Column
    @MetaData(type = DataType.NUMBER)
    public float price;
    @Column
    @MetaData(type = DataType.TIME)
    public long matchedTime;
    @Column
    @MetaData(type = DataType.NUMBER)
    public double purchasePrice;
    @Column(name = "sell_order")
    @MetaData(type = DataType.BOOLEAN)
    public boolean isSellOrder;

    public final static Dao<TradeRecord> DAO = Dao.of(TradeRecord.class);
}
