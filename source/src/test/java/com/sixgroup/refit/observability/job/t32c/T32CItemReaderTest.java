package com.sixgroup.refit.observability.job.t32c;

import com.sixgroup.refit.observability.model.ImpalaRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
class T32CItemReaderTest {

    @Mock JdbcTemplate jdbc;

    @Test void readThenNull() {
        var row = new ImpalaRow(); row.put("REPORT_NAME","TD107");
        doAnswer(i -> List.of(row)).when(jdbc).query(anyString(), any(PreparedStatementSetter.class), any(RowMapper.class));
        var reader = new T32CItemReader(jdbc, "2026-04-30", "2026-01-01", "2026-03-31", "t.reports");
        assertNotNull(reader.read()); assertNull(reader.read());
    }
    @Test void emptyResult() {
        doAnswer(i -> List.of()).when(jdbc).query(anyString(), any(PreparedStatementSetter.class), any(RowMapper.class));
        assertNull(new T32CItemReader(jdbc, "d", "s", "e", "t.r").read());
    }
    @Test void sqlContainsTable() {
        doAnswer(i -> { assertTrue(((String)i.getArgument(0)).contains("custom_tbl")); return List.of(); })
                .when(jdbc).query(anyString(), any(PreparedStatementSetter.class), any(RowMapper.class));
        new T32CItemReader(jdbc, "d", "s", "e", "custom_tbl").read();
    }
    @Test void noRequery() {
        doAnswer(i -> List.of()).when(jdbc).query(anyString(), any(PreparedStatementSetter.class), any(RowMapper.class));
        var reader = new T32CItemReader(jdbc, "d", "s", "e", "t");
        assertNull(reader.read()); assertNull(reader.read()); // still null, no 2nd query
    }
}
