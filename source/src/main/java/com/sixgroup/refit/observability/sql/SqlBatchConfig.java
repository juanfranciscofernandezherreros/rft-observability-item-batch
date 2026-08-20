package com.sixgroup.refit.observability.sql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;

@Slf4j
@Configuration
public class SqlBatchConfig {

    @Value("${batch.sql.input:classpath:/local-input.sql}")
    private Resource sqlFile;

    @Bean
    public Step sqlStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            DataSource dataSource) {

        return new StepBuilder("sqlStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    if (!sqlFile.exists()) {
                        throw new IllegalStateException("SQL file not found: " + sqlFile.getDescription());
                    }

                    log.info("Executing SQL file: {}", sqlFile.getDescription());
                    try (Connection connection = dataSource.getConnection()) {
                        ScriptUtils.executeSqlScript(connection, sqlFile);

                        try (var statement = connection.createStatement();
                             var resultSet = statement.executeQuery(
                                     "SELECT COUNT(*) FROM emir_refit_mbt_account_mng.regu_report")) {
                            if (resultSet.next()) {
                                long count = resultSet.getLong(1);
                                log.info("Rows in emir_refit_mbt_account_mng.regu_report: {}", count);
                            }
                        }
                    }
                    log.info("SQL file executed successfully");

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Job sqlJob(JobRepository jobRepository, Step sqlStep) {
        return new JobBuilder("sqlJob", jobRepository)
                .start(sqlStep)
                .build();
    }
}
