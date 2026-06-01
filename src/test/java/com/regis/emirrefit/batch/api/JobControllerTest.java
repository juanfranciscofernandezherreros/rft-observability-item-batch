package com.regis.emirrefit.batch.api;

import com.regis.emirrefit.batch.api.dto.JobRunRequest;
import com.regis.emirrefit.batch.api.dto.JobRunResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobControllerTest {

    @Mock JobLaunchService svc;
    @InjectMocks JobController ctrl;

    @Test void listJobs() {
        when(svc.availableJobs()).thenReturn(Set.of("t32aJob","t32bJob"));
        var result = ctrl.listJobs();
        assertEquals(2, result.size()); assertTrue(result.contains("t32aJob"));
    }
    @Test void runJobOk() {
        var resp = new JobRunResponse(); resp.setStatus("COMPLETED");
        when(svc.launch(eq("t32aJob"), any())).thenReturn(resp);
        ResponseEntity<JobRunResponse> re = ctrl.runJob("t32aJob", new JobRunRequest());
        assertEquals(200, re.getStatusCode().value()); assertEquals("COMPLETED", re.getBody().getStatus());
    }
    @Test void runJobNotFound() {
        var resp = new JobRunResponse(); resp.setStatus("NOT_FOUND");
        when(svc.launch(eq("bad"), any())).thenReturn(resp);
        assertEquals(404, ctrl.runJob("bad", new JobRunRequest()).getStatusCode().value());
    }
    @Test void runJobNullBody() {
        var resp = new JobRunResponse(); resp.setStatus("COMPLETED");
        when(svc.launch(eq("t32aJob"), any())).thenReturn(resp);
        assertEquals(200, ctrl.runJob("t32aJob", null).getStatusCode().value());
    }
    @Test void getStatusOk() {
        var resp = new JobRunResponse(); resp.setStatus("COMPLETED"); resp.setExecutionId(42L);
        when(svc.status(42L)).thenReturn(resp);
        var re = ctrl.getStatus(42L);
        assertEquals(200, re.getStatusCode().value()); assertEquals(42L, re.getBody().getExecutionId());
    }
    @Test void getStatusNotFound() {
        var resp = new JobRunResponse(); resp.setStatus("NOT_FOUND");
        when(svc.status(999L)).thenReturn(resp);
        assertEquals(404, ctrl.getStatus(999L).getStatusCode().value());
    }
}
