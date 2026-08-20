package com.sixgroup.refit.observability.sql;

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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class SqlBatchConfig {

    @Bean
    public ItemReader<Resource> item32cReader() {
        return new Item32CReader();
    }

    @Bean
    public ItemProcessor<Resource, Resource> item32cProcessor() {
        return new Item32CProcessor();
    }

    @Bean
    public ItemWriter<Resource> item32cWriter(DataSource dataSource) {
        return new Item32CWriter(dataSource);
    }

    @Bean
    public Step item32cStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<Resource> item32cReader,
            ItemProcessor<Resource, Resource> item32cProcessor,
            ItemWriter<Resource> item32cWriter) {

        return new StepBuilder("item32cStep", jobRepository)
                .<Resource, Resource>chunk(1, transactionManager)
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
