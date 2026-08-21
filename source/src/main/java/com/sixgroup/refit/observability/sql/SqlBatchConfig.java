package com.sixgroup.refit.observability.sql;

import com.sixgroup.refit.observability.model.Item32CRow;
import com.sixgroup.refit.observability.processor.Item32CProcessor;
import com.sixgroup.refit.observability.reader.Item32CReader;
import com.sixgroup.refit.observability.writer.Item32CWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@ConditionalOnBean(name = "kudurc-ds")
public class SqlBatchConfig {

    @Bean
    public ItemReader<Item32CRow> item32cReader(
            @Value("${item32c.csv-resource:classpath:item32c.csv}") Resource csvResource) {
        return new Item32CReader(csvResource);
    }

    @Bean
    public ItemProcessor<Item32CRow, Item32CRow> item32cProcessor() {
        return new Item32CProcessor();
    }

    @Bean
    public ItemWriter<Item32CRow> item32cWriter(
            @Qualifier("kudurc-ds") DataSource dataSource,
            @Value("${item32c.target-table:emir_refit_mbt_account_mng.regu_report}") String targetTable) {
        return new Item32CWriter(dataSource, targetTable);
    }

    @Bean
    public Step item32cStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<Item32CRow> item32cReader,
            ItemProcessor<Item32CRow, Item32CRow> item32cProcessor,
            ItemWriter<Item32CRow> item32cWriter,
            @Value("${item32c.chunk-size:100}") int chunkSize) {

        return new StepBuilder("item32cStep", jobRepository)
                .<Item32CRow, Item32CRow>chunk(chunkSize, transactionManager)
                .reader(item32cReader)
                .processor(item32cProcessor)
                .writer(item32cWriter)
                .build();
    }

    @Bean
    public Job item32c(JobRepository jobRepository, Step item32cStep) {
        return new JobBuilder("item32c", jobRepository)
                .start(item32cStep)
                .build();
    }
}
