package com.sixgroup.refit.observability.config;

import com.sixgroup.refit.observability.job.common.CsvItemWriter;
import com.sixgroup.refit.observability.job.t32a.T32AItemProcessor;
import com.sixgroup.refit.observability.job.t32a.T32AItemReader;
import com.sixgroup.refit.observability.job.t32b.T32BItemProcessor;
import com.sixgroup.refit.observability.job.t32b.T32BItemReader;
import com.sixgroup.refit.observability.job.t32c.T32CItemProcessor;
import com.sixgroup.refit.observability.job.t32c.T32CItemReader;
import com.sixgroup.refit.observability.job.t33a.T33AItemProcessor;
import com.sixgroup.refit.observability.job.t33a.T33AItemReader;
import com.sixgroup.refit.observability.listener.JobListener;
import com.sixgroup.refit.observability.model.ImpalaRow;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

@Configuration
public class BatchConfig {

    private static final int CHUNK_SIZE = 100;
    private static final String IMPALA_DRIVER = "com.cloudera.impala.jdbc.Driver";

    private final EmirQueryProperties props;

    public BatchConfig(EmirQueryProperties props) {
        this.props = props;
    }

    @Value("${app.output.delimiter:;}")
    private String delimiter;

    @Value("${app.output.t32a.path:/tmp/t32a_output.csv}")
    private String t32aOutputPath;
    @Value("${app.output.t32b.path:/tmp/t32b_output.csv}")
    private String t32bOutputPath;
    @Value("${app.output.t32c.path:/tmp/t32c_output.csv}")
    private String t32cOutputPath;
    @Value("${app.output.t33a.path:/tmp/t33a_output.csv}")
    private String t33aOutputPath;

    // =========================================================================
    // 1. DATASOURCES
    // =========================================================================

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSourceProperties targetDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @BatchDataSource
    public DataSource targetDataSource() {
        return targetDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean("targetJdbcTemplate")
    @Primary
    public JdbcTemplate targetJdbcTemplate() {
        return new JdbcTemplate(targetDataSource());
    }

    @Bean("impalaDataSource")
    @ConfigurationProperties(prefix = "app.impala")
    public DataSource impalaDataSource() {
        return new SimpleImpalaDataSource();
    }

    @Bean("impalaJdbcTemplate")
    public JdbcTemplate impalaJdbcTemplate() {
        return new JdbcTemplate(impalaDataSource());
    }

    // =========================================================================
    // 2. JOB T-32a
    // =========================================================================

    @Bean
    @StepScope
    public T32AItemReader t32aReader(
            @Value("#{jobParameters['endPeriod']}") String endPeriod) {
        return new T32AItemReader(impalaJdbcTemplate(), endPeriod,
                props.getTables().getOprData(),
                props.getTables().getRecordStatus());
    }

    @Bean
    @StepScope
    public T32AItemProcessor t32aProcessor(
            @Value("#{jobParameters['fecha']}") String fecha) {
        return new T32AItemProcessor(fecha);
    }

    @Bean
    @StepScope
    public CsvItemWriter t32aWriter(
            @Value("#{jobParameters['outputPath']}") String outputPath) {
        return new CsvItemWriter(outputPath != null ? outputPath : t32aOutputPath, delimiter);
    }

    @Bean
    public Step t32aStep(JobRepository jobRepository, PlatformTransactionManager txManager,
                         T32AItemReader t32aReader, T32AItemProcessor t32aProcessor,
                         CsvItemWriter t32aWriter) {
        return new StepBuilder("t32aStep", jobRepository)
                .<ImpalaRow, ImpalaRow>chunk(CHUNK_SIZE, txManager)
                .reader(t32aReader).processor(t32aProcessor).writer(t32aWriter)
                .build();
    }

    @Bean
    public Job t32aJob(JobRepository jobRepository, JobListener listener, Step t32aStep) {
        return new JobBuilder("t32aJob", jobRepository)
                .listener(listener).start(t32aStep).build();
    }

    @Bean
    @StepScope
    public T32BItemReader t32bReader(
            @Value("#{jobParameters['endPeriod']}") String endPeriod) {
        return new T32BItemReader(impalaJdbcTemplate(), endPeriod,
                props.getTables().getLatestTradeState());
    }

    @Bean
    @StepScope
    public T32BItemProcessor t32bProcessor(
            @Value("#{jobParameters['fecha']}") String fecha) {
        return new T32BItemProcessor(fecha);
    }

    @Bean
    @StepScope
    public CsvItemWriter t32bWriter(
            @Value("#{jobParameters['outputPath']}") String outputPath) {
        return new CsvItemWriter(outputPath != null ? outputPath : t32bOutputPath, delimiter);
    }

    @Bean
    public Step t32bStep(JobRepository jobRepository, PlatformTransactionManager txManager,
                         T32BItemReader t32bReader, T32BItemProcessor t32bProcessor,
                         CsvItemWriter t32bWriter) {
        return new StepBuilder("t32bStep", jobRepository)
                .<ImpalaRow, ImpalaRow>chunk(CHUNK_SIZE, txManager)
                .reader(t32bReader).processor(t32bProcessor).writer(t32bWriter)
                .build();
    }

    @Bean
    public Job t32bJob(JobRepository jobRepository, JobListener listener, Step t32bStep) {
        return new JobBuilder("t32bJob", jobRepository)
                .listener(listener).start(t32bStep).build();
    }

    // =========================================================================
    // 4. JOB T-32c
    // =========================================================================

    @Bean
    @StepScope
    public T32CItemReader t32cReader(
            @Value("#{jobParameters['fecha']}") String fecha,
            @Value("#{jobParameters['startDate']}") String startDate,
            @Value("#{jobParameters['endDate']}") String endDate) {
        return new T32CItemReader(impalaJdbcTemplate(), fecha, startDate, endDate,
                props.getTables().getReportsFileOutgoing());
    }

    @Bean
    @StepScope
    public T32CItemProcessor t32cProcessor() {
        return new T32CItemProcessor();
    }

    @Bean
    @StepScope
    public CsvItemWriter t32cWriter(
            @Value("#{jobParameters['outputPath']}") String outputPath) {
        return new CsvItemWriter(outputPath != null ? outputPath : t32cOutputPath, delimiter);
    }

    @Bean
    public Step t32cStep(JobRepository jobRepository, PlatformTransactionManager txManager,
                         T32CItemReader t32cReader, T32CItemProcessor t32cProcessor,
                         CsvItemWriter t32cWriter) {
        return new StepBuilder("t32cStep", jobRepository)
                .<ImpalaRow, ImpalaRow>chunk(CHUNK_SIZE, txManager)
                .reader(t32cReader).processor(t32cProcessor).writer(t32cWriter)
                .build();
    }

    @Bean
    public Job t32cJob(JobRepository jobRepository, JobListener listener, Step t32cStep) {
        return new JobBuilder("t32cJob", jobRepository)
                .listener(listener).start(t32cStep).build();
    }

    @Bean
    @StepScope
    public T33AItemReader t33aReader(
            @Value("#{jobParameters['reportingDate']}") String reportingDate,
            @Value("#{jobParameters['endDate']}") String endDate) {
        return new T33AItemReader(impalaJdbcTemplate(), reportingDate, endDate,
                props.getTables().getRecoStatusEnhHist(),
                props.eeaCountriesSqlList());
    }

    @Bean
    @StepScope
    public T33AItemProcessor t33aProcessor() {
        return new T33AItemProcessor();
    }

    @Bean
    @StepScope
    public CsvItemWriter t33aWriter(
            @Value("#{jobParameters['outputPath']}") String outputPath) {
        return new CsvItemWriter(outputPath != null ? outputPath : t33aOutputPath, delimiter);
    }

    @Bean
    public Step t33aStep(JobRepository jobRepository, PlatformTransactionManager txManager,
                         T33AItemReader t33aReader, T33AItemProcessor t33aProcessor,
                         CsvItemWriter t33aWriter) {
        return new StepBuilder("t33aStep", jobRepository)
                .<ImpalaRow, ImpalaRow>chunk(CHUNK_SIZE, txManager)
                .reader(t33aReader).processor(t33aProcessor).writer(t33aWriter)
                .build();
    }

    @Bean
    public Job t33aJob(JobRepository jobRepository, JobListener listener, Step t33aStep) {
        return new JobBuilder("t33aJob", jobRepository)
                .listener(listener).start(t33aStep).build();
    }

    private static class SimpleImpalaDataSource implements DataSource {
        private String url;
        private String username;
        private String password;

        public void setUrl(String url) { this.url = url; }
        public void setUsername(String username) { this.username = username; }
        public void setPassword(String password) { this.password = password; }

        @Override
        public Connection getConnection() throws SQLException {
            try {
                Class.forName(IMPALA_DRIVER);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Impala driver not found: " + IMPALA_DRIVER, e);
            }
            Properties p = new Properties();
            p.setProperty("UID", this.username);
            p.setProperty("PWD", this.password);
            return DriverManager.getConnection(this.url, p);
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            try {
                Class.forName(IMPALA_DRIVER);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Impala driver not found: " + IMPALA_DRIVER, e);
            }
            Properties p = new Properties();
            p.setProperty("UID", username);
            p.setProperty("PWD", password);
            return DriverManager.getConnection(this.url, p);
        }

        @Override public PrintWriter getLogWriter()                        { return null; }
        @Override public void setLogWriter(PrintWriter out)                { }
        @Override public void setLoginTimeout(int seconds)                 { }
        @Override public int getLoginTimeout()                             { return 0; }
        @Override public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException();
        }
        @Override public <T> T unwrap(Class<T> iface) throws SQLException  { throw new SQLException(); }
        @Override public boolean isWrapperFor(Class<?> iface)              { return false; }
    }
}
