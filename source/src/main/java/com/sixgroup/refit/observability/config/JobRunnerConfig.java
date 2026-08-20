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
            @Qualifier("item32c") Job item32c,
            ApplicationContext applicationContext) {

        return args -> {
            JobExecution execution = jobLauncher.run(item32c, new JobParameters());

            int exitCode = execution.getStatus() == BatchStatus.COMPLETED ? 0 : 1;
            SpringApplication.exit(applicationContext, () -> exitCode);
        };
    }
}
