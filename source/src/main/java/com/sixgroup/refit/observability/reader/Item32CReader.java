package com.sixgroup.refit.observability.reader;

import com.sixgroup.refit.observability.model.Item32CRow;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.Resource;

public class Item32CReader extends FlatFileItemReader<Item32CRow> {

    public Item32CReader(Resource csvResource) {
        setName("item32cCsvReader");
        setResource(csvResource);
        setLinesToSkip(1);

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer(",");
        tokenizer.setQuoteCharacter('"');
        tokenizer.setStrict(true);
        tokenizer.setNames(
                "regulatorid", "reportcode", "queryid", "channel", "reporttype",
                "deliverydatefrom", "deliverydateto", "inserttmstmp", "reportschedule",
                "reportfrequencydaily", "reportfrequencymonth", "lastdayofmonth",
                "reportformat", "reportstatus", "partition");

        DefaultLineMapper<Item32CRow> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSet -> new Item32CRow(
                fieldSet.readString("regulatorid"),
                fieldSet.readString("reportcode"),
                fieldSet.readString("queryid"),
                fieldSet.readString("channel"),
                fieldSet.readString("reporttype"),
                fieldSet.readString("deliverydatefrom"),
                fieldSet.readString("deliverydateto"),
                fieldSet.readString("inserttmstmp"),
                fieldSet.readString("reportschedule"),
                fieldSet.readString("reportfrequencydaily"),
                fieldSet.readString("reportfrequencymonth"),
                fieldSet.readString("lastdayofmonth"),
                fieldSet.readString("reportformat"),
                fieldSet.readString("reportstatus"),
                fieldSet.readString("partition")));
        setLineMapper(lineMapper);
    }
}
