package com.sixgroup.refit.observability.csv;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CSVBatchConfig {

    @Value("classpath:/market-data.csv")
    private Resource csv_file;

    @Bean
    public ItemReader<MarketDataPojo> csvReader(){
        FlatFileItemReader<MarketDataPojo> reader = new FlatFileItemReader<MarketDataPojo>();
        reader.setLinesToSkip(1);
        reader.setResource(csv_file);
        DefaultLineMapper<MarketDataPojo> line_mapper = new DefaultLineMapper<MarketDataPojo>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer(",");
        tokenizer.setNames("TID", "TickerName", "TickerDescription");
        line_mapper.setLineTokenizer(tokenizer);
        line_mapper.setFieldSetMapper(new MarketDataFieldSetMapper());
        reader.setLineMapper(line_mapper);
        return reader;
    }

    @Bean
    public Step step_first(JobRepository jobRepository, PlatformTransactionManager transactionManager){
        return new StepBuilder("step_first",jobRepository).<MarketDataPojo, MarketDataPojo>chunk(4, transactionManager)
                .reader(csvReader())
                .writer(chunk -> {
                    System.out.println("Chunk Start");
                    //Do whatever we want to do with chunk
                    System.out.println(chunk);
                    System.out.println("Chunk End");
                }).build();
    }

    @Bean
    public Job job2(JobRepository jobRepository, PlatformTransactionManager transactionManager) throws Exception {
        return new JobBuilder("job2", jobRepository)
                .start(step_first(jobRepository, transactionManager))
                .build();
    }


}
