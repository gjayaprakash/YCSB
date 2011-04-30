package com.yahoo.ycsb.db;

import java.io.Serializable;
import java.util.Map;

public class Record implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final long count;
    private final Map<String, String> value;

    public Record(long count, Map<String, String> value) {
        this.count = count;
        this.value = value;
    }

    public long count() {
        return count;
    }

    public Map<String, String> value() {
        return value;
    }
}