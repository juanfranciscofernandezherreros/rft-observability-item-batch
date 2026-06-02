package com.sixgroup.refit.observability.job.t32b;

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
class T32BItemReaderTest {

    @Mock JdbcTemplate jdbc;

    @Test void readIterates() {
        var r1 = new ImpalaRow(); r1.put("COUNTERPARTY_ID","A");
        var r2 = new ImpalaRow(); r2.put("COUNTERPARTY_ID","B");
        doAnswer(i -> List.of(r1,r2)).when(jdbc).query(anyString(), any(PreparedStatementSetter.class), any(RowMapper.class));
        var reader = new T32BItemReader(jdbc, "2026-03-31", "t.latest");
        assertNotNull(reader.read()); assertNotNull(reader.read()); assertNull(reader.read());
    }
    @Test void emptyResult() {
        doAnswer(i -> List.of()).when(jdbc).query(anyString(), any(PreparedStatementSetter.class), any(RowMapper.class));
        assertNull(new T32BItemReader(jdbc, "2026-03-31", "t.latest").read());
    }
    @Test void sqlContainsTable() {
        doAnswer(i -> { assertTrue(((String)i.getArgument(0)).contains("my_tbl")); return List.of(); })
                .when(jdbc).query(anyString(), any(PreparedStatementSetter.class), any(RowMapper.class));
        new T32BItemReader(jdbc, "2026-03-31", "my_tbl").read();
    }
}
