package com.regis.emirrefit.batch.job.t32a;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class T32AOutputTest {
    @Test void allGettersSetters() {
        var o = new T32AOutput();
        o.setTrCode("TR"); o.setReportingDate("2026-04-30"); o.setRegulationReference("EMIR");
        o.setTotalNrTradesReceivedFromStart(1L); o.setTotalNrReportsReceivedFromStart(2L); o.setComments("c");
        assertEquals("TR", o.getTrCode());
        assertEquals("2026-04-30", o.getReportingDate());
        assertEquals("EMIR", o.getRegulationReference());
        assertEquals(1L, o.getTotalNrTradesReceivedFromStart());
        assertEquals(2L, o.getTotalNrReportsReceivedFromStart());
        assertEquals("c", o.getComments());
    }
    @Test void defaultsNull() {
        var o = new T32AOutput();
        assertNull(o.getTrCode()); assertNull(o.getTotalNrTradesReceivedFromStart());
    }
}
