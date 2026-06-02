package com.sixgroup.refit.observability.job.t32a;

import com.sixgroup.refit.observability.model.ImpalaRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class T32AItemReaderTest {

    @Mock JdbcTemplate jdbc;

    @Test void readThenNull() {
        when(jdbc.queryForObject(contains("opr_data"), eq(Long.class), anyString())).thenReturn(100L);
        when(jdbc.queryForObject(contains("record_status"), eq(Long.class), anyString())).thenReturn(200L);
        var r = new T32AItemReader(jdbc, "2026-03-31", "s.opr_data", "s.record_status");
        ImpalaRow row = r.read();
        assertNotNull(row);
        assertEquals(100L, row.get("TOTAL_NR_TRADES_RECEIVED_FROM_START"));
        assertEquals(200L, row.get("TOTAL_NR_REPORTS_RECEIVED_FROM_START"));
        assertNull(r.read());
    }
    @Test void queryContainsTable() {
        when(jdbc.queryForObject(contains("my_schema.opr"), eq(Long.class), anyString())).thenReturn(0L);
        when(jdbc.queryForObject(contains("my_schema.rec"), eq(Long.class), anyString())).thenReturn(0L);
        var r = new T32AItemReader(jdbc, "2026-03-31", "my_schema.opr", "my_schema.rec");
        assertNotNull(r.read());
    }
    @Test void handlesNull() {
        when(jdbc.queryForObject(anyString(), eq(Long.class), anyString())).thenReturn(null);
        var r = new T32AItemReader(jdbc, "2026-03-31", "t.o", "t.r");
        assertNotNull(r.read());
    }
}
