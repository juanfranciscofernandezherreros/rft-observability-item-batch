package com.sixgroup.refit.observability.batch;

import com.sixgroup.refit.observability.config.Item32Properties;
import com.sixgroup.refit.observability.dto.Item32ACountsDto;
import com.sixgroup.refit.observability.model.Item32AData;
import com.sixgroup.refit.observability.processor.Item32AProcessor;
import com.sixgroup.refit.observability.reader.Item32AReader;
import com.sixgroup.refit.observability.sql.Item32ADataFinderService;
import com.sixgroup.refit.observability.writer.Item32ACsvWriterFactory;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Wires the T32A job: reader ({@link Item32AReader}) runs the Impala/Kudu
 * query, processor ({@link Item32AProcessor}) maps the DTO into the entity,
 * writer ({@link Item32ACsvWriterFactory}) writes it out as CSV.
 */
@Configuration
@RequiredArgsConstructor
public class Item32ABatchConfig {

    private final Item32ADataFinderService item32ADataFinderService;
    private final Item32Properties item32Properties;

    @Bean
    @StepScope
    public ItemReader<Item32ACountsDto> t32aReader(
            @Value("#{jobParameters['endPeriod']}") String endPeriod) {
        return new Item32AReader(item32ADataFinderService, endPeriod);
    }

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
        return Item32ACsvWriterFactory.create(outputPath);
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
