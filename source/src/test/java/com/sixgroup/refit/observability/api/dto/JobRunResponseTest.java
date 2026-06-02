package com.sixgroup.refit.observability.api.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JobRunResponseTest {
    @Test void allFields() {
        var r = new JobRunResponse();
        r.setExecutionId(1L); r.setInstanceId(2L); r.setJobName("j"); r.setStatus("s"); r.setExitCode("e"); r.setMessage("m");
        assertEquals(1L, r.getExecutionId()); assertEquals(2L, r.getInstanceId());
        assertEquals("j", r.getJobName()); assertEquals("s", r.getStatus());
        assertEquals("e", r.getExitCode()); assertEquals("m", r.getMessage());
    }
    @Test void defaultsNull() {
        var r = new JobRunResponse();
        assertNull(r.getExecutionId()); assertNull(r.getJobName()); assertNull(r.getStatus());
    }
}
