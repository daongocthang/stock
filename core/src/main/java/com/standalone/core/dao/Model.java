package com.standalone.core.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.standalone.core.builder.DataType;
import com.standalone.core.builder.annotation.MetaData;
import com.standalone.core.util.Json;


public class Model {
    @Column(ready_only = true)
    public long id;
    @Column
    public long lastSynced;
    @Column
    public boolean isDeleted;
    @Column
    @MetaData(tag = "id", type = DataType.NUMBER)
    public long remoteId;
    @Column
    @MetaData(type = DataType.STRING)
    public String createdAt;
    @Column
    @MetaData(type = DataType.STRING)
    public String updatedAt;

    public Model() {
        createdAt = Dao.getTimestamp();
        updatedAt = Dao.getTimestamp();
    }

    public String asJson() {
        try {
            return Json.stringify(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
