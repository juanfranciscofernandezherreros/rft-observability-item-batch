package com.sixgroup.refit.observability.config;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
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
            @Qualifier("job2") Job job2,
            ApplicationContext applicationContext) {

        return args -> {

            JobExecution execution =
                    jobLauncher.run(job2, new JobParameters());

            if (execution.getStatus() == BatchStatus.COMPLETED) {
                int exitCode = SpringApplication.exit(applicationContext);
                System.exit(exitCode);
            }
        };
    }
}