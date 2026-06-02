package com.sixgroup.refit.observability.listener;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class JobListenerTest {

    private final JobListener listener = new JobListener();

    private JobExecution exec() {
        var je = new JobExecution(1L);
        je.setJobInstance(new JobInstance(10L, "testJob"));
        je.setStatus(BatchStatus.COMPLETED);
        je.setStartTime(LocalDateTime.of(2026,1,1,8,0));
        return je;
    }

    @Test void beforeJobDoesNotThrow() {
        assertDoesNotThrow(() -> listener.beforeJob(exec()));
    }

    @Test void afterJobWithEndTime() {
        var je = exec();
        je.setEndTime(LocalDateTime.of(2026,1,1,8,0,5));
        assertDoesNotThrow(() -> listener.afterJob(je));
    }

    @Test void afterJobNullEndTime() {
        var je = exec();
        je.setEndTime(null);
        assertDoesNotThrow(() -> listener.afterJob(je));
    }

    @Test void afterJobWithStepExecutions() {
        var je = exec();
        je.setEndTime(LocalDateTime.of(2026,1,1,8,1));
        var step = je.createStepExecution("step1");
        step.setExitStatus(ExitStatus.COMPLETED);
        step.setReadCount(10);
        step.setWriteCount(10);
        assertDoesNotThrow(() -> listener.afterJob(je));
        assertFalse(je.getStepExecutions().isEmpty());
    }
}
