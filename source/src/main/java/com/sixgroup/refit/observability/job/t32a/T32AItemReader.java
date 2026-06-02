package com.sixgroup.refit.observability.job.t32a;

import com.sixgroup.refit.observability.model.ImpalaRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.jdbc.core.JdbcTemplate;

public class T32AItemReader implements ItemReader<ImpalaRow> {

    private static final Logger log = LoggerFactory.getLogger(T32AItemReader.class);

    private final JdbcTemplate impalaJdbcTemplate;
    private final String endPeriod;
    private final String oprDataTable;
    private final String recordStatusTable;
    private boolean read = false;

    public T32AItemReader(JdbcTemplate impalaJdbcTemplate, String endPeriod,
                          String oprDataTable, String recordStatusTable) {
        this.impalaJdbcTemplate = impalaJdbcTemplate;
        this.endPeriod = endPeriod;
        this.oprDataTable = oprDataTable;
        this.recordStatusTable = recordStatusTable;
    }

    @Override
    public ImpalaRow read() {
        if (read) {
            return null;
        }

        // ── Query 1: total trades ───────────────────────────────────────
        String query1 = """
                SELECT COUNT(*) AS TOTAL_NR_TRADES_RECEIVED_FROM_START
                FROM {table}
                WHERE actntp IN ('NEWT', 'POSC')
                  AND rptgtmstmpdt <= ?
                """.replace("{table}", oprDataTable);
        log.info("T-32a Query 1 — endPeriod={}", endPeriod);
        Long totalTrades = impalaJdbcTemplate.queryForObject(query1, Long.class, endPeriod);

        // ── Query 2: total reports ──────────────────────────────────────
        String query2 = """
                SELECT COUNT(*) AS TOTAL_NR_REPORTS_RECEIVED_FROM_START
                FROM {table}
                WHERE receiveddt <= ?
                """.replace("{table}", recordStatusTable);
        log.info("T-32a Query 2 — endPeriod={}", endPeriod);
        Long totalReports = impalaJdbcTemplate.queryForObject(query2, Long.class, endPeriod);

        // ── Combinar ────────────────────────────────────────────────────
        ImpalaRow row = new ImpalaRow();
        row.put("TOTAL_NR_TRADES_RECEIVED_FROM_START", totalTrades);
        row.put("TOTAL_NR_REPORTS_RECEIVED_FROM_START", totalReports);

        log.info("T-32a resultado → trades={}, reports={}", totalTrades, totalReports);
        read = true;
        return row;
    }
}
