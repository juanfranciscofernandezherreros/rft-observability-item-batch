package com.regis.emirrefit.batch.api;

import com.regis.emirrefit.batch.api.dto.JobRunRequest;
import com.regis.emirrefit.batch.api.dto.JobRunResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class JobLaunchService {

    private static final Logger log = LoggerFactory.getLogger(JobLaunchService.class);

    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final Map<String, Job> jobs;

    public JobLaunchService(JobLauncher jobLauncher,
                            JobExplorer jobExplorer,
                            Map<String, Job> jobs) {
        this.jobLauncher = jobLauncher;
        this.jobExplorer = jobExplorer;
        this.jobs = jobs;
    }

    public java.util.Set<String> availableJobs() {
        return jobs.keySet();
    }

    public JobRunResponse launch(String jobName, JobRunRequest req) {
        Job job = jobs.get(jobName);
        if (job == null) {
            JobRunResponse error = new JobRunResponse();
            error.setJobName(jobName);
            error.setStatus("NOT_FOUND");
            error.setMessage("Job desconocido. Disponibles: " + jobs.keySet());
            return error;
        }

        JobParametersBuilder pb = new JobParametersBuilder();
        addIfPresent(pb, "fecha", req.getFecha());
        addIfPresent(pb, "endPeriod", req.getEndPeriod());
        addIfPresent(pb, "startDate", req.getStartDate());
        addIfPresent(pb, "endDate", req.getEndDate());
        addIfPresent(pb, "reportingDate", req.getReportingDate());
        addIfPresent(pb, "outputPath", req.getOutputPath());
        // run.id único → nueva instancia en cada llamada
        pb.addLong("run.id", System.currentTimeMillis());

        JobParameters params = pb.toJobParameters();
        log.info("Lanzando {} con parámetros {}", jobName, params);

        JobRunResponse resp = new JobRunResponse();
        resp.setJobName(jobName);
        try {
            JobExecution exec = jobLauncher.run(job, params);
            resp.setExecutionId(exec.getId());
            resp.setInstanceId(exec.getJobInstance().getInstanceId());
            resp.setStatus(exec.getStatus().toString());
            resp.setExitCode(exec.getExitStatus().getExitCode());
            resp.setMessage("Job lanzado correctamente");
        } catch (Exception e) {
            log.error("Error lanzando {}: {}", jobName, e.getMessage(), e);
            resp.setStatus("FAILED_TO_LAUNCH");
            resp.setMessage(e.getMessage());
        }
        return resp;
    }

    public JobRunResponse status(Long executionId) {
        JobExecution exec = jobExplorer.getJobExecution(executionId);
        JobRunResponse resp = new JobRunResponse();
        if (exec == null) {
            resp.setExecutionId(executionId);
            resp.setStatus("NOT_FOUND");
            resp.setMessage("No existe ejecución con ese ID");
            return resp;
        }
        resp.setExecutionId(exec.getId());
        resp.setInstanceId(exec.getJobInstance().getInstanceId());
        resp.setJobName(exec.getJobInstance().getJobName());
        resp.setStatus(exec.getStatus().toString());
        resp.setExitCode(exec.getExitStatus().getExitCode());
        return resp;
    }

    private void addIfPresent(JobParametersBuilder pb, String key, String value) {
        if (value != null && !value.isBlank()) {
            pb.addString(key, value);
        }
    }
}
