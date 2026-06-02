package com.sixgroup.refit.observability.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class ImpalaRow {

    private final Map<String, Object> columns = new LinkedHashMap<>();

    public void put(String columnName, Object value) {
        columns.put(columnName, value);
    }

    public Object get(String columnName) {
        return columns.get(columnName);
    }

    public Map<String, Object> getColumns() {
        return columns;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        columns.forEach((key, value) ->
            sb.append(String.format("%-30s | %s%n", key, value))
        );
        return sb.toString();
    }
}
