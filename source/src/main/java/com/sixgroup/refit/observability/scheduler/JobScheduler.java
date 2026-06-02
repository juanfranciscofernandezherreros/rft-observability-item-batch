package com.sixgroup.refit.observability.scheduler;

import com.sixgroup.refit.observability.api.JobLaunchService;
import com.sixgroup.refit.observability.api.dto.JobRunRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobScheduler {

    private static final Logger log = LoggerFactory.getLogger(JobScheduler.class);

    private final JobLaunchService jobLaunchService;

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

    public JobScheduler(JobLaunchService jobLaunchService) {
        this.jobLaunchService = jobLaunchService;
    }

    @Scheduled(cron = "${batch.schedule.cron:0 0 2 * * *}")
    public void ejecutarJobs() {
        log.info("Scheduler disparado — lanzando los 4 jobs con fechas por defecto");

        JobRunRequest t32a = new JobRunRequest();
        t32a.setFecha(fecha);
        t32a.setEndPeriod(endPeriod);
        jobLaunchService.launch("t32aJob", t32a);

        JobRunRequest t32b = new JobRunRequest();
        t32b.setFecha(fecha);
        t32b.setEndPeriod(endPeriod);
        jobLaunchService.launch("t32bJob", t32b);

        JobRunRequest t32c = new JobRunRequest();
        t32c.setFecha(fecha);
        t32c.setStartDate(t32cStartDate);
        t32c.setEndDate(t32cEndDate);
        jobLaunchService.launch("t32cJob", t32c);

        JobRunRequest t33a = new JobRunRequest();
        t33a.setReportingDate(t33aReportingDate);
        t33a.setEndDate(t33aEndDate);
        jobLaunchService.launch("t33aJob", t33a);
    }
}
