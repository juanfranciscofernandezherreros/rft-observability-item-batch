package com.sixgroup.refit.observability;

import com.sixgroup.refit.observability.api.JobLaunchService;
import com.sixgroup.refit.observability.api.dto.JobRunRequest;
import com.sixgroup.refit.observability.api.dto.JobRunResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@SpringBootApplication(exclude = { KafkaAutoConfiguration.class })
@EnableScheduling
public class BatchApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(BatchApplication.class, args);
        int exitCode = SpringApplication.exit(ctx);
        System.exit(exitCode);
    }

    @Component
    static class BatchStartupRunner implements ApplicationRunner {

        private static final Logger log = LoggerFactory.getLogger(BatchStartupRunner.class);

        private final JobLaunchService jobLaunchService;

        @Value("${spring.batch.job.name:}")
        private String jobName;

        @Value("${batch.defaults.fecha}")
        private String fecha;
        @Value("${batch.defaults.endperiod}")
        private String endPeriod;
        @Value("${batch.defaults.t32c-startdate}")
        private String t32cStartDate;
        @Value("${batch.defaults.t32c-enddate}")
        private String t32cEndDate;
        @Value("${batch.defaults.t33a-reporting-date}")
        private String t33aReportingDate;
        @Value("${batch.defaults.t33a-end-date}")
        private String t33aEndDate;

        BatchStartupRunner(JobLaunchService jobLaunchService) {
            this.jobLaunchService = jobLaunchService;
        }

        @Override
        public void run(ApplicationArguments args) {
            if (jobName == null || jobName.isBlank()) {
                log.warn("No se ha especificado ningún job (spring.batch.job.name está vacío).");
                log.warn("Valores válidos: t32aJob, t32bJob, t32cJob, t33aJob");
                log.warn("Ejemplo: java -jar app.jar --spring.batch.job.name=t32aJob");
                return;
            }

            log.info("=== Lanzando job: {} ===", jobName);
            JobRunRequest req = buildRequest(jobName);
            if (req == null) {
                log.error("Job desconocido: '{}'. Valores válidos: t32aJob, t32bJob, t32cJob, t33aJob", jobName);
                return;
            }

            JobRunResponse resp = jobLaunchService.launch(jobName, req);
            boolean ok = !"FAILED".equalsIgnoreCase(resp.getStatus())
                      && !"FAILED_TO_LAUNCH".equalsIgnoreCase(resp.getStatus());
            log.info("=== Job {} finalizado — estado: {} ===", jobName, resp.getStatus());
            if (!ok) {
                log.error("Detalle del error: {}", resp.getMessage());
            }
        }

        private JobRunRequest buildRequest(String name) {
            JobRunRequest req = new JobRunRequest();
            switch (name) {
                case "t32aJob":
                    req.setFecha(fecha);
                    req.setEndPeriod(endPeriod);
                    return req;
                case "t32bJob":
                    req.setFecha(fecha);
                    req.setEndPeriod(endPeriod);
                    return req;
                case "t32cJob":
                    req.setFecha(fecha);
                    req.setStartDate(t32cStartDate);
                    req.setEndDate(t32cEndDate);
                    return req;
                case "t33aJob":
                    req.setReportingDate(t33aReportingDate);
                    req.setEndDate(t33aEndDate);
                    return req;
                default:
                    return null;
            }
        }
    }
}
