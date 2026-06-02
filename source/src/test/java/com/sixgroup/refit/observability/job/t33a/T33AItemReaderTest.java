package com.sixgroup.refit.observability.job.t33a;

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
class T33AItemReaderTest {

    @Mock JdbcTemplate jdbc;

    @Test void readIterates() {
        var r1 = new ImpalaRow(); r1.put("REPORTING_TR_CODE","TRRGS");
        var r2 = new ImpalaRow(); r2.put("REPORTING_TR_CODE","TRRGS");
        doAnswer(i -> List.of(r1,r2)).when(jdbc).query(anyString(), any(PreparedStatementSetter.class), any(RowMapper.class));
        var reader = new T33AItemReader(jdbc, "2026-04-15", "2026-03-31", "t.reco", "'AT','BE'");
        assertNotNull(reader.read()); assertNotNull(reader.read()); assertNull(reader.read());
    }
    @Test void sqlContainsTable() {
        doAnswer(i -> { assertTrue(((String)i.getArgument(0)).contains("my_reco")); return List.of(); })
                .when(jdbc).query(anyString(), any(PreparedStatementSetter.class), any(RowMapper.class));
        new T33AItemReader(jdbc, "d", "e", "my_reco", "'AT'").read();
    }
    @Test void sqlContainsEea() {
        doAnswer(i -> { assertTrue(((String)i.getArgument(0)).contains("'ES','FR'")); return List.of(); })
                .when(jdbc).query(anyString(), any(PreparedStatementSetter.class), any(RowMapper.class));
        new T33AItemReader(jdbc, "d", "e", "t", "'ES','FR'").read();
    }
    @Test void noPlaceholdersRemain() {
        doAnswer(i -> {
            String sql = (String) i.getArgument(0);
            assertFalse(sql.contains("{table}")); assertFalse(sql.contains("{eea}"));
            return List.of();
        }).when(jdbc).query(anyString(), any(PreparedStatementSetter.class), any(RowMapper.class));
        new T33AItemReader(jdbc, "d", "e", "schema.tbl", "'AT'").read();
    }
}
