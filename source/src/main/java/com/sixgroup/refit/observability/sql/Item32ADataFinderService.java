package com.sixgroup.refit.observability.sql;

import com.sixgroup.refit.observability.config.DatasourceSchemaProperties;
import com.sixgroup.refit.observability.dto.Item32ACountsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class Item32ADataFinderService {

    private final JdbcTemplate jdbcTemplate;
    private final DatasourceSchemaProperties schemaProperties;

    public Item32ACountsDto fetchCounts(final String endPeriod) {
        log.trace("Running Item T32A raw counts query for endPeriod: {}", endPeriod);

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

        final Item32ACountsDto counts = Item32ACountsDto.builder()
                .tradesFromImpala(tradesFromImpala)
                .reportsFromKudu(reportsFromKudu)
                .build();

        log.trace("Item32A raw counts fetched successfully: {}", counts);
        return counts;
    }
}
