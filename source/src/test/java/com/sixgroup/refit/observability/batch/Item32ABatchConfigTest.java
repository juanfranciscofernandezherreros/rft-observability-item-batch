package com.sixgroup.refit.observability.batch;

import com.sixgroup.refit.observability.config.DatasourceSchemaProperties;
import com.sixgroup.refit.observability.config.Item32Properties;
import com.sixgroup.refit.observability.sql.Item32ADataFinderService;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Boots only the T32A batch slice (reader = SQL query via
 * {@link Item32ADataFinderService}, writer = CSV) against H2.
 */
@SpringBatchTest
@SpringBootTest(classes = {
        Item32ABatchConfigTest.TestConfig.class,
        Item32ABatchConfig.class,
        Item32ADataFinderService.class
})
@EnableConfigurationProperties({Item32Properties.class, DatasourceSchemaProperties.class})
@TestPropertySource(properties = {
        "component-config.kududb.control-refit-schema=PUBLIC",
        "component-config.kududb.transc-schema=PUBLIC",
        "component-config.item32.item32aproperties.initial-total-trades-new=0",
        "component-config.item32.item32aproperties.initial-total-trades-all=0",
        "app.output.t32a-csv=target/test-output/item32a-output.csv"
})
class Item32ABatchConfigTest {

    @EnableAutoConfiguration
    static class TestConfig {
    }

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    /**
     * opr_data/record_status are already seeded by the app's own
     * schema.sql/data.sql (on the test classpath via src/main/resources),
     * which yield totalNrTrades=3 and totalNrReports=3 for endPeriod
     * 2026-03-31 — see those files for the raw rows.
     */
    @Test
    void readerRunsTheQueryAndWriterProducesTheCsv() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("endPeriod", "2026-03-31")
                .addString("reportingDate", "2026-04-30")
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        List<String> lines = Files.readAllLines(Path.of("target/test-output/item32a-output.csv"));
        assertThat(lines).containsExactly(
                "ReportingDate,TotalNrTrades,TotalNrReports",
                "2026-04-30,3,3");
    }
}
