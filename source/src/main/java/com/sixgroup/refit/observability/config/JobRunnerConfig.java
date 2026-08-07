package com.sixgroup.refit.observability.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobRunnerConfig {

    @Bean
    public CommandLineRunner runJob(
            JobLauncher jobLauncher,
            @Qualifier("job2") Job job2) {

        return args -> jobLauncher.run(
                job2,
                new JobParametersBuilder()
                        .addLong("timestamp", System.currentTimeMillis())
                        .toJobParameters()
        );
    }
}
