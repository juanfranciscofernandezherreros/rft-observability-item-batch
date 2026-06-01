package com.regis.emirrefit.batch.job.t33a;

import com.regis.emirrefit.batch.model.ImpalaRow;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class T33AItemProcessorTest {

    private final T33AItemProcessor proc = new T33AItemProcessor();

    private ImpalaRow full() {
        var r = new ImpalaRow();
        r.put("REPORTING_TR_CODE","TRRGS"); r.put("REPORTING_DATE","2026-04-15");
        r.put("SUBMISSION_STATUS","SUBMITTED"); r.put("NON_SUBMISSION_REASON",null);
        r.put("PAIRED_STATUS","PAIRED"); r.put("RECON_TYPE","INTRA_TR");
        r.put("OTHER_TR_CODE","TRRGS"); r.put("DERIVATIVE_TYPE","OTC");
        r.put("TRADE_STATUS","OUTSTANDING"); r.put("TRADE_TYPE","DUAL_SIDED");
        r.put("EEA_STATUS","Y");
        r.put("NR_OF_UTI",500L); r.put("NR_OF_MATCHED_UTI",480L);
        r.put("NR_OF_RECONCILED_UTI",10L); r.put("NR_OF_UNMATCHED_UTI",10L);
        r.put("COMMENTS","");
        return r;
    }

    @Test void columnCount() { assertEquals(16, proc.process(full()).getColumns().size()); }
    @Test void columnOrder() {
        var keys = proc.process(full()).getColumns().keySet().stream().toList();
        assertEquals("REPORTING_TR_CODE", keys.get(0)); assertEquals("COMMENTS", keys.get(15));
        assertEquals("NR_OF_UTI", keys.get(11));
    }
    @Test void stringPassThrough() {
        var o = proc.process(full());
        assertEquals("TRRGS", o.get("REPORTING_TR_CODE")); assertEquals("SUBMITTED", o.get("SUBMISSION_STATUS"));
        assertEquals("Y", o.get("EEA_STATUS"));
    }
    @Test void longConversion() {
        var in = full(); in.put("NR_OF_UTI","1,500"); in.put("NR_OF_MATCHED_UTI",480);
        var o = proc.process(in);
        assertEquals(1500L, o.get("NR_OF_UTI")); assertEquals(480L, o.get("NR_OF_MATCHED_UTI"));
    }
    @Test void nullCounters() {
        var in = full(); in.put("NR_OF_UTI",null); in.put("NR_OF_UNMATCHED_UTI",null);
        var o = proc.process(in);
        assertNull(o.get("NR_OF_UTI")); assertNull(o.get("NR_OF_UNMATCHED_UTI"));
    }
    @Test void emptyStringCounter() {
        var in = full(); in.put("NR_OF_UTI",""); in.put("NR_OF_MATCHED_UTI","bad");
        var o = proc.process(in);
        assertNull(o.get("NR_OF_UTI")); assertNull(o.get("NR_OF_MATCHED_UTI"));
    }
    @Test void commentsAlwaysEmpty() {
        var in = full(); in.put("COMMENTS","overwrite me");
        assertEquals("", proc.process(in).get("COMMENTS"));
    }
    @Test void nullStrings() {
        var in = full(); in.put("NON_SUBMISSION_REASON",null); in.put("RECON_TYPE",null);
        var o = proc.process(in);
        assertNull(o.get("NON_SUBMISSION_REASON")); assertNull(o.get("RECON_TYPE"));
    }
}
