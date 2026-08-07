package com.sixgroup.refit.observability;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;

@SpringBootApplication(exclude = BatchAutoConfiguration.class)
public class BatchProcessingDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchProcessingDemoApplication.class, args);
    }
}
