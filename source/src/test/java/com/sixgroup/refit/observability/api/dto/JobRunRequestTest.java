package com.sixgroup.refit.observability.api.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JobRunRequestTest {
    @Test void allFields() {
        var r = new JobRunRequest();
        r.setFecha("f"); r.setEndPeriod("ep"); r.setStartDate("sd"); r.setEndDate("ed");
        r.setReportingDate("rd"); r.setOutputPath("op");
        assertEquals("f",r.getFecha()); assertEquals("ep",r.getEndPeriod());
        assertEquals("sd",r.getStartDate()); assertEquals("ed",r.getEndDate());
        assertEquals("rd",r.getReportingDate()); assertEquals("op",r.getOutputPath());
    }
    @Test void defaultsNull() {
        var r = new JobRunRequest();
        assertNull(r.getFecha()); assertNull(r.getEndPeriod()); assertNull(r.getStartDate());
        assertNull(r.getEndDate()); assertNull(r.getReportingDate()); assertNull(r.getOutputPath());
    }
}
