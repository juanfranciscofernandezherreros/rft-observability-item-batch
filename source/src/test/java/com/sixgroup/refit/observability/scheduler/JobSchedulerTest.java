package com.sixgroup.refit.observability.scheduler;

import com.sixgroup.refit.observability.api.JobLaunchService;
import com.sixgroup.refit.observability.api.dto.JobRunRequest;
import com.sixgroup.refit.observability.api.dto.JobRunResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobSchedulerTest {

    @Test void ejecutarJobsLaunchesFourJobs() throws Exception {
        var svc = mock(JobLaunchService.class);
        var resp = new JobRunResponse(); resp.setStatus("COMPLETED");
        when(svc.launch(anyString(), any(JobRunRequest.class))).thenReturn(resp);

        var scheduler = new JobScheduler(svc);
        setField(scheduler, "fecha", "2026-04-30");
        setField(scheduler, "endPeriod", "2026-03-31");
        setField(scheduler, "t32cStartDate", "2026-01-01 00:00:00.000");
        setField(scheduler, "t32cEndDate", "2026-03-31 00:00:00.000");
        setField(scheduler, "t33aReportingDate", "2026-04-15");
        setField(scheduler, "t33aEndDate", "2026-03-31");

        scheduler.ejecutarJobs();

        var nameCaptor = ArgumentCaptor.forClass(String.class);
        var reqCaptor = ArgumentCaptor.forClass(JobRunRequest.class);
        verify(svc, times(4)).launch(nameCaptor.capture(), reqCaptor.capture());

        var names = nameCaptor.getAllValues();
        assertTrue(names.contains("t32aJob"));
        assertTrue(names.contains("t32bJob"));
        assertTrue(names.contains("t32cJob"));
        assertTrue(names.contains("t33aJob"));
    }

    @Test void t32aRequestHasCorrectParams() throws Exception {
        var svc = mock(JobLaunchService.class);
        var resp = new JobRunResponse(); resp.setStatus("COMPLETED");
        when(svc.launch(anyString(), any(JobRunRequest.class))).thenReturn(resp);

        var scheduler = new JobScheduler(svc);
        setField(scheduler, "fecha", "2026-04-30");
        setField(scheduler, "endPeriod", "2026-03-31");
        setField(scheduler, "t32cStartDate", "s");
        setField(scheduler, "t32cEndDate", "e");
        setField(scheduler, "t33aReportingDate", "r");
        setField(scheduler, "t33aEndDate", "d");

        scheduler.ejecutarJobs();

        var reqCaptor = ArgumentCaptor.forClass(JobRunRequest.class);
        verify(svc, times(4)).launch(anyString(), reqCaptor.capture());
        var t32aReq = reqCaptor.getAllValues().get(0);
        assertEquals("2026-04-30", t32aReq.getFecha());
        assertEquals("2026-03-31", t32aReq.getEndPeriod());
    }

    @Test void t32cRequestHasStartEndDate() throws Exception {
        var svc = mock(JobLaunchService.class);
        var resp = new JobRunResponse(); resp.setStatus("COMPLETED");
        when(svc.launch(anyString(), any(JobRunRequest.class))).thenReturn(resp);

        var scheduler = new JobScheduler(svc);
        setField(scheduler, "fecha", "f");
        setField(scheduler, "endPeriod", "e");
        setField(scheduler, "t32cStartDate", "2026-01-01");
        setField(scheduler, "t32cEndDate", "2026-03-31");
        setField(scheduler, "t33aReportingDate", "r");
        setField(scheduler, "t33aEndDate", "d");

        scheduler.ejecutarJobs();

        var nameCaptor = ArgumentCaptor.forClass(String.class);
        var reqCaptor = ArgumentCaptor.forClass(JobRunRequest.class);
        verify(svc, times(4)).launch(nameCaptor.capture(), reqCaptor.capture());

        int idx = nameCaptor.getAllValues().indexOf("t32cJob");
        var t32cReq = reqCaptor.getAllValues().get(idx);
        assertEquals("2026-01-01", t32cReq.getStartDate());
        assertEquals("2026-03-31", t32cReq.getEndDate());
    }

    private void setField(Object obj, String name, String value) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }
}
