package com.standalone.stock.db.schema;

import com.standalone.core.builder.DataType;
import com.standalone.core.builder.annotation.MetaData;
import com.standalone.core.dao.Column;
import com.standalone.core.dao.Dao;
import com.standalone.core.dao.Model;

import java.util.Comparator;

import lombok.ToString;
import lombok.experimental.FieldNameConstants;

@ToString
@FieldNameConstants
public class Stock extends Model implements Comparable<Stock> {
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

    public final static Dao<Stock> DAO = Dao.of(Stock.class);


    @Override
    public int compareTo(Stock other) {
        return (int) (other.matchedTime - this.matchedTime);
    }
}
