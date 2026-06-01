package com.regis.emirrefit.batch.job.t32c;

import com.regis.emirrefit.batch.model.ImpalaRow;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class T32CItemProcessorTest {

    private final T32CItemProcessor proc = new T32CItemProcessor();

    private ImpalaRow base(String completion, String sla) {
        var r = new ImpalaRow();
        r.put("TR_CODE","TRRGS"); r.put("REPORTING_DATE","2026-04-30"); r.put("REGULATION_REFERENCE","EMIR");
        r.put("REPORT_NAME","TD107_TEST"); r.put("REPORT_TYPE","ESMA");
        r.put("REPORT_GENERATION_TIME","2026-03-15 08:00:00.000");
        r.put("REPORT_COMPLETION_TIME", completion);
        r.put("REPORT_PUBLICATION_TIME","2026-03-15 09:00:00.000");
        r.put("SESSION","2026-03-14"); r.put("SLA", sla);
        r.put("DIFFERENCE",""); r.put("SLA_BREACH_ID","");
        return r;
    }

    @Test void positiveDifference() {
        var o = proc.process(base("2026-03-15 10:00:00.000", "2026-03-15T08:00:00Z"));
        assertEquals("2.00", o.get("DIFFERENCE"));
    }
    @Test void negativeDifference() {
        var o = proc.process(base("2026-03-15 07:00:00.000", "2026-03-15T08:00:00Z"));
        assertEquals("-1.00", o.get("DIFFERENCE"));
    }
    @Test void emptyWhenSlaEmpty() {
        assertEquals("", proc.process(base("2026-03-15 10:00:00.000", "")).get("DIFFERENCE"));
    }
    @Test void emptyWhenCompletionNull() {
        assertEquals("", proc.process(base(null, "2026-03-15T12:00:00Z")).get("DIFFERENCE"));
    }
    @Test void emptyWhenBothNull() {
        var r = base(null, null);
        assertEquals("", proc.process(r).get("DIFFERENCE"));
    }
    @Test void formatsGenerationToIso() {
        var o = proc.process(base("2026-03-15 08:30:45.000", ""));
        assertEquals("2026-03-15T08:00:00Z", o.get("REPORT_GENERATION_TIME"));
        assertEquals("2026-03-15T08:30:45Z", o.get("REPORT_COMPLETION_TIME"));
    }
    @Test void renamesSessionToDate() {
        var o = proc.process(base("2026-03-15 08:00:00.000", ""));
        assertEquals("2026-03-14", o.get("DATE"));
        assertFalse(o.getColumns().containsKey("SESSION"));
    }
    @Test void columnOrder() {
        var keys = proc.process(base("2026-03-15 08:00:00.000", "")).getColumns().keySet().stream().toList();
        assertEquals("TR_CODE", keys.get(0));
        assertEquals("DATE", keys.get(8));
        assertEquals("SLA_BREACH_ID", keys.get(keys.size()-1));
        assertEquals(12, keys.size());
    }
    @Test void allNulls() {
        var r = new ImpalaRow();
        for (String k : new String[]{"TR_CODE","REPORTING_DATE","REGULATION_REFERENCE","REPORT_NAME",
                "REPORT_TYPE","REPORT_GENERATION_TIME","REPORT_COMPLETION_TIME","REPORT_PUBLICATION_TIME",
                "SESSION","SLA","DIFFERENCE","SLA_BREACH_ID"}) r.put(k, null);
        var o = proc.process(r);
        assertNotNull(o); assertEquals("", o.get("DIFFERENCE"));
    }
    @Test void isoTimestampPassedThrough() {
        var r = base("2026-03-15T10:00:00Z", "2026-03-15T08:00:00Z");
        r.put("REPORT_GENERATION_TIME", "2026-03-15T07:00:00Z");
        var o = proc.process(r);
        assertEquals("2026-03-15T07:00:00Z", o.get("REPORT_GENERATION_TIME"));
        assertEquals("2.00", o.get("DIFFERENCE"));
    }
}
