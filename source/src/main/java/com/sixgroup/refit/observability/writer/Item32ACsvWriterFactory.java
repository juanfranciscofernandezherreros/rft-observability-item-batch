package com.sixgroup.refit.observability.writer;

import com.sixgroup.refit.observability.model.Item32AData;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.core.io.FileSystemResource;

/**
 * Builds the writer that turns each Item32AData entity into a CSV row.
 */
public final class Item32ACsvWriterFactory {

    private Item32ACsvWriterFactory() {
    }

    public static FlatFileItemWriter<Item32AData> create(String outputPath) {
        FlatFileItemWriter<Item32AData> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource(outputPath));
        writer.setHeaderCallback(w -> w.write("ReportingDate,TotalNrTrades,TotalNrReports"));

        BeanWrapperFieldExtractor<Item32AData> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[] {"reportingDate", "totalNrTrades", "totalNrReports"});

        DelimitedLineAggregator<Item32AData> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);

        writer.setLineAggregator(lineAggregator);
        return writer;
    }
}
