package com.sixgroup.refit.observability.job.t32b;

import com.sixgroup.refit.observability.model.ImpalaRow;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class T32BItemProcessorTest {

    private final T32BItemProcessor proc = new T32BItemProcessor("2026-04-30");

    private ImpalaRow input() {
        var r = new ImpalaRow();
        r.put("COUNTERPARTY_ID","LEI"); r.put("COUNTERPARTY_COUNTRY","ES");
        r.put("SUBMITTING_ENTITY_ID","S1"); r.put("DATA_QUALITY_CATEGORY","A");
        r.put("NR_OUTSTANDING_TRADES",100L); r.put("NR_NON_OUTSTANDING_TRADES",50L);
        return r;
    }
    @Test void staticColumns() {
        var o = proc.process(input());
        assertEquals("TRRGS",o.get("TR_CODE")); assertEquals("2026-04-30",o.get("REPORTING_DATE"));
        assertEquals("EMIR",o.get("REGULATION_REFERENCE")); assertEquals("",o.get("COMMENTS"));
    }
    @Test void columnOrder() {
        var keys = proc.process(input()).getColumns().keySet().stream().toList();
        assertEquals("TR_CODE",keys.get(0)); assertEquals("COMMENTS",keys.get(keys.size()-1));
        assertEquals(10, keys.size());
    }
    @Test void numericConversion() {
        var in = input(); in.put("NR_OUTSTANDING_TRADES","1,234"); in.put("NR_NON_OUTSTANDING_TRADES","567");
        var o = proc.process(in);
        assertEquals(1234L, o.get("NR_OUTSTANDING_TRADES")); assertEquals(567L, o.get("NR_NON_OUTSTANDING_TRADES"));
    }
    @Test void nullNumeric() {
        var in = input(); in.put("NR_OUTSTANDING_TRADES",null); in.put("NR_NON_OUTSTANDING_TRADES",null);
        var o = proc.process(in);
        assertNull(o.get("NR_OUTSTANDING_TRADES"));
    }
    @Test void emptyStringNumeric() {
        var in = input(); in.put("NR_OUTSTANDING_TRADES",""); in.put("NR_NON_OUTSTANDING_TRADES","bad");
        var o = proc.process(in);
        assertNull(o.get("NR_OUTSTANDING_TRADES")); assertNull(o.get("NR_NON_OUTSTANDING_TRADES"));
    }
    @Test void integerInput() {
        var in = input(); in.put("NR_OUTSTANDING_TRADES", 42);
        assertEquals(42L, proc.process(in).get("NR_OUTSTANDING_TRADES"));
    }
}
