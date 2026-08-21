package com.sixgroup.refit.observability.processor;

import com.sixgroup.refit.observability.model.Item32CRow;
import org.springframework.batch.item.ItemProcessor;

public class Item32CProcessor implements ItemProcessor<Item32CRow, Item32CRow> {

    @Override
    public Item32CRow process(final Item32CRow row) {
        if (row == null) {
            throw new IllegalStateException("item32c CSV row must not be null");
        }
        if (isBlank(row.regulatorId()) || isBlank(row.reportCode()) || isBlank(row.queryId())) {
            throw new IllegalStateException("item32c CSV row is missing regulatorid/reportcode/queryid");
        }
        return row;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
