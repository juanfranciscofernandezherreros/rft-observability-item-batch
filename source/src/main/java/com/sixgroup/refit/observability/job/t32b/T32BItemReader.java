package com.sixgroup.refit.observability.job.t32b;

import com.sixgroup.refit.observability.model.ImpalaRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.jdbc.core.JdbcTemplate;
import java.sql.ResultSetMetaData;
import java.util.Iterator;
import java.util.List;

public class T32BItemReader implements ItemReader<ImpalaRow> {

    private static final Logger log = LoggerFactory.getLogger(T32BItemReader.class);

    private final JdbcTemplate impalaJdbcTemplate;
    private final String endPeriod;
    private final String latestTradeStateTable;
    private Iterator<ImpalaRow> iterator;

    public T32BItemReader(JdbcTemplate impalaJdbcTemplate, String endPeriod,
                          String latestTradeStateTable) {
        this.impalaJdbcTemplate = impalaJdbcTemplate;
        this.endPeriod = endPeriod;
        this.latestTradeStateTable = latestTradeStateTable;
    }

    @Override
    public ImpalaRow read() {
        if (iterator == null) {
            log.info("T-32b ejecutando query — endPeriod={}", endPeriod);

            String query = """
                SELECT
                    COUNTERPARTY_ID,
                    COUNTERPARTY_COUNTRY,
                    SUBMITTING_ENTITY_ID,
                    DATA_QUALITY_CATEGORY,
                    SUM(OUTSTANDING_TRADES) AS NR_OUTSTANDING_TRADES,
                    SUM(NON_OUTSTANDING_TRADES) AS NR_NON_OUTSTANDING_TRADES
                FROM (
                    SELECT
                        rptgctrpty AS COUNTERPARTY_ID,
                        LEFT(rptgctrptyctry, 2) AS COUNTERPARTY_COUNTRY,
                        submitgagt AS SUBMITTING_ENTITY_ID,
                        CASE
                            WHEN rptgoblgtn IS NOT NULL THEN 'A'
                            WHEN (mdfctnsessiondt >= '2017-11-02' AND mdfctnsessiondt <= '2024-04-26') THEN 'B'
                            WHEN (insrtdttm >= '2017-11-02' AND rptgoblgtn IS NULL AND mdfctnsessiondt >= '2024-04-26') THEN 'B'
                            WHEN (mdfctnsessiondt >= '2015-11-02' AND mdfctnsessiondt < '2017-11-02') THEN 'C'
                            WHEN (insrtdttm >= '2015-11-02' AND insrtdttm < '2017-11-02' AND rptgoblgtn IS NULL) THEN 'C'
                            WHEN mdfctnsessiondt < '2015-11-02' THEN 'D'
                            WHEN (insrtdttm < '2015-11-02' AND mdfctnsessiondt >= '2024-04-26' AND rptgoblgtn IS NULL) THEN 'D'
                            ELSE ''
                        END AS DATA_QUALITY_CATEGORY,
                        CASE
                            WHEN ((cntrctstsdt >= ? OR cntrctstsdt = '' OR cntrctstsdt IS NULL) AND actntp != 'EROR') THEN 1 ELSE 0
                        END AS OUTSTANDING_TRADES,
                        CASE
                            WHEN ((cntrctstsdt < ? AND cntrctstsdt != '') OR actntp = 'EROR') THEN 1 ELSE 0
                        END AS NON_OUTSTANDING_TRADES
                    FROM {table}
                    WHERE insertsessiondt < ?
                ) latest
                GROUP BY COUNTERPARTY_ID, COUNTERPARTY_COUNTRY, SUBMITTING_ENTITY_ID, DATA_QUALITY_CATEGORY
                """.replace("{table}", latestTradeStateTable);

            List<ImpalaRow> rows = impalaJdbcTemplate.query(
                    query,
                    ps -> {
                        ps.setString(1, endPeriod);  // cntrctstsdt >= ?
                        ps.setString(2, endPeriod);  // cntrctstsdt < ?
                        ps.setString(3, endPeriod);  // insertsessiondt < ?
                    },
                    (rs, rowNum) -> {
                        ImpalaRow row = new ImpalaRow();
                        ResultSetMetaData meta = rs.getMetaData();
                        for (int i = 1; i <= meta.getColumnCount(); i++) {
                            row.put(meta.getColumnLabel(i).toUpperCase(), rs.getObject(i));
                        }
                        return row;
                    }
            );

            log.info("T-32b filas leídas: {}", rows.size());
            iterator = rows.iterator();
        }

        return iterator.hasNext() ? iterator.next() : null;
    }
}
