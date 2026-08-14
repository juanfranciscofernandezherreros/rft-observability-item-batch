package com.sixgroup.refit.observability.batch;

import com.sixgroup.refit.observability.config.Item32Properties;
import com.sixgroup.refit.observability.dto.Item32ACountsDto;
import com.sixgroup.refit.observability.model.Item32AData;
import com.sixgroup.refit.observability.sql.Item32ADataFinderService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.atomic.AtomicBoolean;

@Configuration
@RequiredArgsConstructor
public class Item32ABatchConfig {

    private final Item32ADataFinderService item32ADataFinderService;
    private final Item32Properties item32Properties;

    /**
     * The reader IS the Item T32A query: it delegates to
     * {@link Item32ADataFinderService#fetchCounts}, which runs the two
     * Impala COUNT queries, and hands the single raw-counts DTO to the
     * step once before signalling end-of-data.
     */
    @Bean
    @StepScope
    public ItemReader<Item32ACountsDto> t32aReader(
            @Value("#{jobParameters['endPeriod']}") String endPeriod) {
        AtomicBoolean alreadyRead = new AtomicBoolean(false);
        return () -> {
            if (alreadyRead.compareAndSet(false, true)) {
                return item32ADataFinderService.fetchCounts(endPeriod);
            }
            return null;
        };
    }

    /**
     * Converts the raw Kudu/Impala counts DTO into the Item32AData entity.
     */
    @Bean
    @StepScope
    public ItemProcessor<Item32ACountsDto, Item32AData> t32aProcessor(
            @Value("#{jobParameters['reportingDate']}") String reportingDate) {
        return new Item32AProcessor(item32Properties, reportingDate);
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<Item32AData> t32aCsvWriter(
            @Value("#{jobParameters['outputPath'] ?: '${app.output.t32a-csv:output/item32a-output.csv}'}") String outputPath) {
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

    @Bean
    public Step t32aStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<Item32ACountsDto> t32aReader,
            ItemProcessor<Item32ACountsDto, Item32AData> t32aProcessor,
            FlatFileItemWriter<Item32AData> t32aCsvWriter) {
        return new StepBuilder("t32aStep", jobRepository)
                .<Item32ACountsDto, Item32AData>chunk(1, transactionManager)
                .reader(t32aReader)
                .processor(t32aProcessor)
                .writer(t32aCsvWriter)
                .build();
    }

    @Bean
    public Job t32aJob(JobRepository jobRepository, Step t32aStep) {
        return new JobBuilder("t32aJob", jobRepository)
                .start(t32aStep)
                .build();
    }
}
