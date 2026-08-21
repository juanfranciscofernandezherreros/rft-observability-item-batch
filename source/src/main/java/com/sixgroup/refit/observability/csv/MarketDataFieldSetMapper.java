package com.sixgroup.refit.observability.csv;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class MarketDataFieldSetMapper implements FieldSetMapper<MarketDataPojo> {

    @Override
    public MarketDataPojo mapFieldSet(FieldSet fieldSet) throws BindException {
        MarketDataPojo market_data = new MarketDataPojo();
        market_data.setId(fieldSet.readInt("TID"));
        market_data.setTicker(fieldSet.readString("TickerName"));
        market_data.setDescription(fieldSet.readString("TickerDescription"));
        return market_data;
    }
}
