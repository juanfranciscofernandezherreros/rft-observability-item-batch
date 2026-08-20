package com.sixgroup.refit.observability.sql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;

@Slf4j
@Configuration
public class SqlBatchConfig {

    private static final Resource ITEM32C_SQL = new ClassPathResource("item32c.sql");

    @Bean
    public Step item32cStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            DataSource dataSource) {

        return new StepBuilder("item32cStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    if (!ITEM32C_SQL.exists()) {
                        throw new IllegalStateException("SQL file not found: " + ITEM32C_SQL.getDescription());
                    }

                    log.info("Executing item32c SQL file: {}", ITEM32C_SQL.getDescription());
                    try (Connection connection = dataSource.getConnection()) {
                        ScriptUtils.executeSqlScript(connection, ITEM32C_SQL);

                        try (var statement = connection.createStatement();
                             var resultSet = statement.executeQuery(
                                     "SELECT COUNT(*) FROM emir_refit_mbt_account_mng.regu_report")) {
                            if (resultSet.next()) {
                                long count = resultSet.getLong(1);
                                log.info("Rows in emir_refit_mbt_account_mng.regu_report: {}", count);
                            }
                        }
                    }
                    log.info("item32c SQL executed successfully");

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Job item32c(JobRepository jobRepository, Step item32cStep) {
        return new JobBuilder("item32c", jobRepository)
                .start(item32cStep)
                .build();
    }
}
