package com.sixgroup.refit.observability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BatchProcessingDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchProcessingDemoApplication.class, args);
    }
}
