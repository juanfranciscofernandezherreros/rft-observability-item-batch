package com.sixgroup.refit.observability.config;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobRunnerConfig {

    @Bean
    public CommandLineRunner runJob(
            JobLauncher jobLauncher,
            @Qualifier("t32aJob") Job t32aJob,
            ApplicationContext applicationContext,
            @Value("${app.jobs.t32a.end-period}") String endPeriod,
            @Value("${app.jobs.t32a.reporting-date}") String reportingDate) {

        return args -> {

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("endPeriod", endPeriod)
                    .addString("reportingDate", reportingDate)
                    .addLong("runId", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(t32aJob, jobParameters);

            int exitCode = SpringApplication.exit(applicationContext,
                    () -> execution.getStatus() == BatchStatus.COMPLETED ? 0 : 1);
            System.exit(exitCode);
        };
    }
}
