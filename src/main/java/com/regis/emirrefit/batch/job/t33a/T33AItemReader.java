package com.regis.emirrefit.batch.job.t33a;

import com.regis.emirrefit.batch.model.ImpalaRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.jdbc.core.JdbcTemplate;
import java.sql.ResultSetMetaData;
import java.util.Iterator;
import java.util.List;

public class T33AItemReader implements ItemReader<ImpalaRow> {

    private static final Logger log = LoggerFactory.getLogger(T33AItemReader.class);

    private final JdbcTemplate impalaJdbcTemplate;
    private final String reportingDate;
    private final String endDate;
    private final String recoStatusTable;
    private final String eeaCountriesSqlList;
    private Iterator<ImpalaRow> iterator;

    public T33AItemReader(JdbcTemplate impalaJdbcTemplate,
                          String reportingDate, String endDate,
                          String recoStatusTable, String eeaCountriesSqlList) {
        this.impalaJdbcTemplate = impalaJdbcTemplate;
        this.reportingDate = reportingDate;
        this.endDate = endDate;
        this.recoStatusTable = recoStatusTable;
        this.eeaCountriesSqlList = eeaCountriesSqlList;
    }

    @Override
    public ImpalaRow read() {
        if (iterator == null) {
            log.info("T-33a ejecutando query — reportingDate={}, endDate={}",
                    reportingDate, endDate);

            String query = """
                SELECT
                    REPORTING_TR_CODE,
                    REPORTING_DATE,
                    SUBMISSION_STATUS,
                    NON_SUBMISSION_REASON,
                    PAIRED_STATUS,
                    RECON_TYPE,
                    OTHER_TR_CODE,
                    DERIVATIVE_TYPE,
                    TRADE_STATUS,
                    TRADE_TYPE,
                    EEA_STATUS,
                    COUNT(*) AS NR_OF_UTI,
                    SUM(NR_OF_MATCHED_UTI) AS NR_OF_MATCHED_UTI,
                    SUM(NR_OF_RECONCILED_UTI) AS NR_OF_RECONCILED_UTI,
                    SUM(NR_OF_UNMATCHED_UTI) AS NR_OF_UNMATCHED_UTI,
                    '' AS COMMENTS
                FROM (
                    SELECT
                        'TRRGS' AS REPORTING_TR_CODE,
                        ? AS REPORTING_DATE,
                        CASE
                            WHEN rcncltnsts IN ('NORE', 'SSNE') THEN 'NOT_SUBMITTED'
                            ELSE 'SUBMITTED'
                        END AS SUBMISSION_STATUS,
                        CASE
                            WHEN rcncltnsts = 'NORE' THEN 'OTHER'
                            WHEN rcncltnsts = 'SSNE' AND othrctrptytp != 'L' THEN 'NON_LEI_OTHER_CPTY'
                            WHEN rcncltnsts = 'SSNE' THEN 'SINGLE_SIDED_NON_EEA_UNPAIRED_UTI'
                            ELSE NULL
                        END AS NON_SUBMISSION_REASON,
                        CASE
                            WHEN rcncltnsts IN ('NORE', 'SSNE', 'SSUN') THEN 'UNPAIRED'
                            ELSE 'PAIRED'
                        END AS PAIRED_STATUS,
                        CASE
                            WHEN LEFT(rcncltnsts,1) = 'D' THEN 'INTRA_TR'
                            WHEN LEFT(rcncltnsts,1) = 'S' AND rcncltnsts NOT IN ('SSUN','SSNE') THEN 'INTER_TR'
                            ELSE NULL
                        END AS RECON_TYPE,
                        CASE
                            WHEN LEFT(rcncltnsts,1) = 'D' THEN 'TRRGS'
                            WHEN rcncltnsts NOT IN ('NORE','SSUN','SSNE','') THEN rcncltnallctntr
                            ELSE NULL
                        END AS OTHER_TR_CODE,
                        CASE
                            WHEN nttyrspnsblforrpt IS NULL THEN 'ETD'
                            WHEN tradgvn = 'XOFF' THEN tradgvn
                            ELSE 'OTC'
                        END AS DERIVATIVE_TYPE,
                        CASE
                            WHEN cntrctstsdt >= ? OR cntrctstsdt IS NULL OR cntrctstsdt = '' THEN 'OUTSTANDING'
                            ELSE 'EXPIRED'
                        END AS TRADE_STATUS,
                        CASE
                            WHEN LEFT(rcncltnsts,1) = 'D' THEN 'DUAL_SIDED'
                            WHEN LEFT(rcncltnsts,1) = 'S' OR rcncltnsts = 'NORE' THEN 'SINGLE_SIDED'
                            ELSE NULL
                        END AS TRADE_TYPE,
                        CASE
                            WHEN othrctrptyctry IN ({eea})
                             AND rptgctrptyctry IN ({eea})
                            THEN 'Y'
                            ELSE 'N'
                        END AS EEA_STATUS,
                        CASE WHEN rcncltnsts IN ('DSMA','SSMA','DSRN','SSRN') THEN 1 ELSE 0 END AS NR_OF_MATCHED_UTI,
                        CASE WHEN rcncltnsts IN ('DPRV','DRPW','SPRV','SPRW') THEN 1 ELSE 0 END AS NR_OF_RECONCILED_UTI,
                        CASE WHEN rcncltnsts IN ('DSFA','SSFA','SSPA','DSNM','SSFN','DSFN') THEN 1 ELSE 0 END AS NR_OF_UNMATCHED_UTI
                    FROM {table} rseh
                    WHERE rseh.enhoriginaldt = ?
                ) td
                GROUP BY REPORTING_TR_CODE, REPORTING_DATE, SUBMISSION_STATUS,
                         NON_SUBMISSION_REASON, PAIRED_STATUS, RECON_TYPE,
                         OTHER_TR_CODE, DERIVATIVE_TYPE, TRADE_STATUS,
                         TRADE_TYPE, EEA_STATUS
                """
                .replace("{table}", recoStatusTable)
                .replace("{eea}", eeaCountriesSqlList);

            List<ImpalaRow> rows = impalaJdbcTemplate.query(
                    query,
                    ps -> {
                        ps.setString(1, reportingDate);  // REPORTING_DATE literal
                        ps.setString(2, endDate);        // cntrctstsdt >= ?
                        ps.setString(3, endDate);        // enhoriginaldt = ?
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

            log.info("T-33a filas leídas: {}", rows.size());
            iterator = rows.iterator();
        }

        return iterator.hasNext() ? iterator.next() : null;
    }
}
