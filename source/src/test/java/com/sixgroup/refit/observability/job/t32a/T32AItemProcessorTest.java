package com.sixgroup.refit.observability.job.t32a;

import com.sixgroup.refit.observability.model.ImpalaRow;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class T32AItemProcessorTest {

    private final T32AItemProcessor proc = new T32AItemProcessor("2026-04-30");

    @Test void addsStaticColumns() throws Exception {
        ImpalaRow in = new ImpalaRow();
        in.put("TOTAL_NR_TRADES_RECEIVED_FROM_START", 100L);
        in.put("TOTAL_NR_REPORTS_RECEIVED_FROM_START", 200L);
        ImpalaRow out = proc.process(in);
        assertEquals("TRRGS", out.get("TR_CODE"));
        assertEquals("2026-04-30", out.get("REPORTING_DATE"));
        assertEquals("EMIR", out.get("REGULATION_REFERENCE"));
        assertEquals("", out.get("COMMENTS"));
    }
    @Test void addsHdfsConstants() throws Exception {
        ImpalaRow in = new ImpalaRow();
        in.put("TOTAL_NR_TRADES_RECEIVED_FROM_START", 100L);
        in.put("TOTAL_NR_REPORTS_RECEIVED_FROM_START", 200L);
        ImpalaRow out = proc.process(in);
        assertTrue((Long) out.get("TOTAL_NR_TRADES_RECEIVED_FROM_START") > 100L);
        assertTrue((Long) out.get("TOTAL_NR_REPORTS_RECEIVED_FROM_START") > 200L);
    }
    @Test void nullInputValues() throws Exception {
        ImpalaRow in = new ImpalaRow();
        in.put("TOTAL_NR_TRADES_RECEIVED_FROM_START", null);
        in.put("TOTAL_NR_REPORTS_RECEIVED_FROM_START", null);
        ImpalaRow out = proc.process(in);
        assertTrue((Long) out.get("TOTAL_NR_TRADES_RECEIVED_FROM_START") > 0);
    }
    @Test void columnOrder() throws Exception {
        ImpalaRow in = new ImpalaRow();
        in.put("TOTAL_NR_TRADES_RECEIVED_FROM_START", 0L);
        in.put("TOTAL_NR_REPORTS_RECEIVED_FROM_START", 0L);
        var keys = proc.process(in).getColumns().keySet().stream().toList();
        assertEquals("TR_CODE", keys.get(0));
        assertEquals("COMMENTS", keys.get(keys.size()-1));
    }
}
