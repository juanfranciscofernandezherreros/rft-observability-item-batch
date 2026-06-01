package com.regis.emirrefit.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Component
public class JobListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(JobListener.class);

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info(">>> INICIANDO JOB [{}] — instancia #{}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getJobInstance().getInstanceId());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        Duration duracion = (jobExecution.getEndTime() != null)
                ? Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime())
                : Duration.ZERO;

        log.info("<<< JOB FINALIZADO [{}] — estado: {} — duración: {} ms",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus(),
                duracion.toMillis());

        // Los fallos quedan registrados en los step executions
        jobExecution.getStepExecutions().forEach(step ->
            log.info("    Step [{}] → {} | leídos={} escritos={} saltados={}",
                    step.getStepName(),
                    step.getExitStatus().getExitCode(),
                    step.getReadCount(),
                    step.getWriteCount(),
                    step.getSkipCount())
        );
    }
}
