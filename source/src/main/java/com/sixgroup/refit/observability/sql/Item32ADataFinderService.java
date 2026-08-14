package com.sixgroup.refit.observability.sql;

import com.sixgroup.refit.observability.config.DatasourceSchemaProperties;
import com.sixgroup.refit.observability.config.Item32Properties;
import com.sixgroup.refit.observability.model.Item32AData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class Item32ADataFinderService {

    private final JdbcTemplate jdbcTemplate;
    private final Item32Properties item32Properties;
    private final DatasourceSchemaProperties schemaProperties;

    public Item32AData find(final String endPeriod, final String reportingDate) {
        log.trace("Running Item T32A for endPeriod: {}", endPeriod);

        // ===================================
        // Query 1: TOTAL_NR_TRADES_RECEIVED_FROM_START
        // ===================================
        final String query1 = """
                SELECT COUNT(*) AS TOTAL_NR_TRADES_RECEIVED_FROM_START
                FROM %s.opr_data
                WHERE actntp IN ('NEWT', 'POSC')
                AND rptgtmstmpdt <= ?
                """.formatted(schemaProperties.getTranscSchema());

        log.trace("=== SENDING PRIVATE QUERY 1 TO IMPALA ===");
        log.trace(query1.replace("?", "'" + endPeriod + "'"));
        log.trace("=========================================\n");

        Long tradesFromImpala = jdbcTemplate.queryForObject(query1, Long.class, endPeriod);
        tradesFromImpala = (tradesFromImpala != null) ? tradesFromImpala : 0L;

        final long totalNrTrades = tradesFromImpala + item32Properties.getItem32Aproperties().getInitialTotalTradesNew();

        // ===================================
        // Query 2: TOTAL_NR_REPORTS_RECEIVED_FROM_START
        // ===================================
        final String query2 = """
                SELECT COUNT(*) AS TOTAL_NR_REPORTS_RECEIVED_FROM_START
                FROM %s.record_status
                WHERE receiveddt <= ?
                """.formatted(schemaProperties.getControlRefitSchema());

        log.trace("=== SENDING PRIVATE QUERY 2 TO IMPALA ===");
        log.trace(query2.replace("?", "'" + endPeriod + "'"));
        log.trace("=========================================\n");

        Long reportsFromKudu = jdbcTemplate.queryForObject(query2, Long.class, endPeriod);
        reportsFromKudu = (reportsFromKudu != null) ? reportsFromKudu : 0L;

        final long totalNrReports = reportsFromKudu + item32Properties.getItem32Aproperties().getInitialTotalTradesAll();

        final Item32AData item32AData = Item32AData.builder()
            .reportingDate(reportingDate)
            .totalNrTrades(totalNrTrades)
            .totalNrReports(totalNrReports)
            .build();

        log.trace("Item32AData generated successfully: {}", item32AData);
        return item32AData;
    }
}
