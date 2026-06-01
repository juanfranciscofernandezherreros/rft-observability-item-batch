package com.regis.emirrefit.batch.api;

import com.regis.emirrefit.batch.api.dto.JobRunRequest;
import com.regis.emirrefit.batch.api.dto.JobRunResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobLaunchServiceTest {

    @Mock JobLauncher launcher;
    @Mock JobExplorer explorer;
    @Mock Job mockJob;

    JobLaunchService svc;

    @BeforeEach void setUp() {
        svc = new JobLaunchService(launcher, explorer, Map.of("t32aJob", mockJob));
    }

    private JobExecution completedExec(long id, String jobName) {
        var exec = new JobExecution(id);
        exec.setJobInstance(new JobInstance(id, jobName));
        exec.setStatus(BatchStatus.COMPLETED);
        exec.setExitStatus(ExitStatus.COMPLETED);
        return exec;
    }

    @Test void availableJobs() {
        assertTrue(svc.availableJobs().contains("t32aJob"));
    }

    @Test void launchOk() throws Exception {
        when(launcher.run(eq(mockJob), any(JobParameters.class))).thenReturn(completedExec(1L, "t32aJob"));
        var req = new JobRunRequest(); req.setFecha("2026-04-30"); req.setEndPeriod("2026-03-31");
        var resp = svc.launch("t32aJob", req);
        assertEquals("COMPLETED", resp.getStatus());
        assertEquals(1L, resp.getExecutionId());
        assertEquals("t32aJob", resp.getJobName());
        assertNotNull(resp.getExitCode());
        assertNotNull(resp.getMessage());
    }

    @Test void launchUnknown() {
        var resp = svc.launch("missing", new JobRunRequest());
        assertEquals("NOT_FOUND", resp.getStatus());
        assertTrue(resp.getMessage().contains("Disponibles"));
        assertEquals("missing", resp.getJobName());
    }

    @Test void launchException() throws Exception {
        when(launcher.run(eq(mockJob), any())).thenThrow(new RuntimeException("boom"));
        var resp = svc.launch("t32aJob", new JobRunRequest());
        assertEquals("FAILED_TO_LAUNCH", resp.getStatus());
        assertTrue(resp.getMessage().contains("boom"));
    }

    @Test void launchAllNullParams() throws Exception {
        when(launcher.run(eq(mockJob), any())).thenReturn(completedExec(1L, "t32aJob"));
        assertEquals("COMPLETED", svc.launch("t32aJob", new JobRunRequest()).getStatus());
    }

    @Test void launchWithOutputPath() throws Exception {
        when(launcher.run(eq(mockJob), any())).thenReturn(completedExec(1L, "t32aJob"));
        var req = new JobRunRequest(); req.setOutputPath("/tmp/out.csv");
        assertEquals("COMPLETED", svc.launch("t32aJob", req).getStatus());
    }

    @Test void launchBlankParams() throws Exception {
        when(launcher.run(eq(mockJob), any())).thenReturn(completedExec(1L, "t32aJob"));
        var req = new JobRunRequest(); req.setFecha(""); req.setEndPeriod("   ");
        assertEquals("COMPLETED", svc.launch("t32aJob", req).getStatus());
    }

    @Test void launchWithAllParams() throws Exception {
        when(launcher.run(eq(mockJob), any())).thenReturn(completedExec(1L, "t32aJob"));
        var req = new JobRunRequest();
        req.setFecha("f"); req.setEndPeriod("ep"); req.setStartDate("sd");
        req.setEndDate("ed"); req.setReportingDate("rd"); req.setOutputPath("op");
        assertEquals("COMPLETED", svc.launch("t32aJob", req).getStatus());
    }

    @Test void statusFound() {
        when(explorer.getJobExecution(5L)).thenReturn(completedExec(5L, "t32bJob"));
        var resp = svc.status(5L);
        assertEquals("COMPLETED", resp.getStatus());
        assertEquals(5L, resp.getExecutionId());
        assertEquals("t32bJob", resp.getJobName());
    }

    @Test void statusNotFound() {
        when(explorer.getJobExecution(999L)).thenReturn(null);
        var resp = svc.status(999L);
        assertEquals("NOT_FOUND", resp.getStatus());
        assertEquals(999L, resp.getExecutionId());
    }
}
