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

    private static final String TOTAL_NR_TRADES_QUERY = """
            SELECT COUNT(*) AS TOTAL_NR_TRADES_RECEIVED_FROM_START
            FROM %s.opr_data
            WHERE actntp IN ('NEWT', 'POSC')
            AND rptgtmstmpdt <= ?
            """;

    private static final String TOTAL_NR_REPORTS_QUERY = """
            SELECT COUNT(*) AS TOTAL_NR_REPORTS_RECEIVED_FROM_START
            FROM %s.record_status
            WHERE receiveddt <= ?
            """;

    private final JdbcTemplate jdbcTemplate;
    private final DatasourceSchemaProperties schemaProperties;

    public Item32ACountsDto fetchCounts(final String endPeriod) {
        log.trace("Running Item T32A raw counts query for endPeriod: {}", endPeriod);

        final long tradesFromImpala = countFor(
                TOTAL_NR_TRADES_QUERY.formatted(schemaProperties.getTranscSchema()), endPeriod);
        final long reportsFromKudu = countFor(
                TOTAL_NR_REPORTS_QUERY.formatted(schemaProperties.getControlRefitSchema()), endPeriod);

        final Item32ACountsDto counts = Item32ACountsDto.builder()
                .tradesFromImpala(tradesFromImpala)
                .reportsFromKudu(reportsFromKudu)
                .build();

        log.trace("Item32A raw counts fetched successfully: {}", counts);
        return counts;
    }

    private long countFor(final String query, final String endPeriod) {
        log.trace("Sending query to Impala [endPeriod={}]: {}", endPeriod, query);
        final Long count = jdbcTemplate.queryForObject(query, Long.class, endPeriod);
        return count != null ? count : 0L;
    }
}
