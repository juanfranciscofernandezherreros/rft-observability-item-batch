package com.regis.emirrefit.batch.job.t32c;

import com.regis.emirrefit.batch.model.ImpalaRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSetMetaData;
import java.util.Iterator;
import java.util.List;

public class T32CItemReader implements ItemReader<ImpalaRow> {

    private static final Logger log = LoggerFactory.getLogger(T32CItemReader.class);

    private final JdbcTemplate impalaJdbcTemplate;
    private final String date;
    private final String startDate;
    private final String endDate;
    private final String reportsFileOutgoingTable;
    private Iterator<ImpalaRow> iterator;

    public T32CItemReader(JdbcTemplate impalaJdbcTemplate,
                          String date, String startDate, String endDate,
                          String reportsFileOutgoingTable) {
        this.impalaJdbcTemplate = impalaJdbcTemplate;
        this.date = date;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reportsFileOutgoingTable = reportsFileOutgoingTable;
    }

    @Override
    public ImpalaRow read() {
        if (iterator == null) {
            log.info("T-32c ejecutando query — date={}, startDate={}, endDate={}",
                    date, startDate, endDate);

            String query = """
                WITH base AS (
                    SELECT
                        'TRRGS' AS TR_CODE,
                        ? AS REPORTING_DATE,
                        'EMIR' AS REGULATION_REFERENCE,
                        CASE
                            WHEN accountid IN ('eudritrace') THEN CONCAT(filetype, SUBSTR(outgoingfilename, INSTR(outgoingfilename, '_', 1, 2), INSTR(outgoingfilename, '-') - INSTR(outgoingfilename, '_', 1, 2)))
                            WHEN accountid IN ('eudri0uu0000','eudri4dc3000','eudri4ea0000','eudri5cb4000','eudri0pbp000') THEN CONCAT(filetype, SUBSTR(outgoingfilename, INSTR(outgoingfilename, '_', 1, 2), INSTR(outgoingfilename, '-') - INSTR(outgoingfilename, '_', 1, 2)))
                            WHEN accountid IN ('TRDTI','TRKDP','TRUVT') THEN substring(outgoingfilename,8,14)
                            ELSE CONCAT(filetype, '_', accountid)
                        END AS REPORT_NAME,
                        CASE
                            WHEN substring(outgoingfilename,14,5) = 'ESMAS' THEN 'ESMA'
                            WHEN (accountid IN ('eudritrace') AND substring(outgoingfilename,14,5) != 'ESMAS')
                              OR accountid IN ('eudri0uu0000','eudri4dc3000','eudri4ea0000','eudri5cb4000','eudri0pbp000') THEN 'NCA'
                            WHEN accountid IN ('TRDTI','TRKDP','TRUVT') THEN 'TR'
                            ELSE 'PARTICIPANT'
                        END AS REPORT_TYPE,
                        filetype AS INFORME,
                        creationtimestamp AS FECHA,
                        traceavailabilitytimestamp AS REPORT_PUBLICATION_TIME,
                        TO_DATE(reportingsessiontimestamp) AS SESSION,
                        CASE
                            WHEN filetype IN ('TAR030R','TAR108R','WARN000R','REC091R','TSR107R','TSR109R') THEN ''
                            WHEN (accountid IN ('eudritrace','eudri0uu0000','eudri4dc3000','eudri4ea0000','eudri5cb4000','eudri0pbp000') AND filetype != 'TPST000') THEN CONCAT(CAST(date_add(CAST(reportingsessiontimestamp AS DATE),1) AS STRING), 'T12:00:00Z')
                            WHEN (accountid IN ('eudritrace','eudri0uu0000','eudri4dc3000','eudri4ea0000','eudri5cb4000','eudri0pbp000') AND filetype = 'TPST000') THEN CONCAT(CAST(date_add(CAST(reportingsessiontimestamp AS DATE),1) AS STRING), 'T23:59:59Z')
                            WHEN outgoingfilename LIKE '%_I091_%' THEN CONCAT(CAST(date_add(CAST(reportingsessiontimestamp AS DATE),1) AS STRING), 'T01:00:00Z')
                            WHEN accountid LIKE 'eudbi%' THEN CONCAT(CAST(date_add(CAST(reportingsessiontimestamp AS DATE), CASE WHEN dayofweek(CAST(reportingsessiontimestamp AS DATE)) = 6 THEN 3 ELSE 1 END) AS STRING),'T06:00:00Z')
                            ELSE ''
                        END AS SLA
                    FROM {table}
                    WHERE reportingsessiontimestamp BETWEEN ? AND ?
                      AND outgoingfilename NOT LIKE '%_csv.zip%'
                      AND (filetype IN ('TD107','STAATR','TSR107','RL078','WARN000','RJCT000','TPST000','TSR109','TAR108','REC091','TAR030')
                           OR (filetype IN ('TAR030R','TAR108R','WARN000R','REC091R','TSR107R','TSR109R')
                               AND creationtimestamp BETWEEN ? AND ?
                               AND reportingsessiontimestamp BETWEEN ? AND ?))
                ),
                dedup AS (
                    SELECT
                        TR_CODE,
                        REPORTING_DATE,
                        REGULATION_REFERENCE,
                        REPORT_NAME,
                        REPORT_TYPE,
                        MIN(FECHA) OVER (PARTITION BY REPORT_NAME, SESSION) AS REPORT_GENERATION_TIME,
                        MAX(FECHA) OVER (PARTITION BY REPORT_NAME, SESSION) AS REPORT_COMPLETION_TIME,
                        MAX(REPORT_PUBLICATION_TIME) OVER (PARTITION BY REPORT_NAME, SESSION) AS REPORT_PUBLICATION_TIME,
                        SESSION,
                        SLA,
                        '' AS DIFFERENCE,
                        '' AS SLA_BREACH_ID,
                        ROW_NUMBER() OVER (PARTITION BY REPORT_NAME, SESSION ORDER BY FECHA DESC) AS RN
                    FROM base
                )
                SELECT
                    TR_CODE,
                    REPORTING_DATE,
                    REGULATION_REFERENCE,
                    REPORT_NAME,
                    REPORT_TYPE,
                    REPORT_GENERATION_TIME,
                    REPORT_COMPLETION_TIME,
                    REPORT_PUBLICATION_TIME,
                    SESSION,
                    SLA,
                    DIFFERENCE,
                    SLA_BREACH_ID
                FROM dedup
                WHERE RN = 1
                """.replace("{table}", reportsFileOutgoingTable);

            List<ImpalaRow> rows = impalaJdbcTemplate.query(
                    query,
                    ps -> {
                        ps.setString(1, date);       // REPORTING_DATE literal
                        ps.setString(2, startDate);  // WHERE BETWEEN ? AND ? (1st)
                        ps.setString(3, endDate);
                        ps.setString(4, startDate);  // creationtimestamp BETWEEN
                        ps.setString(5, endDate);
                        ps.setString(6, startDate);  // reportingsessiontimestamp BETWEEN (2nd)
                        ps.setString(7, endDate);
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

            log.info("T-32c filas leídas: {}", rows.size());
            iterator = rows.iterator();
        }

        return iterator.hasNext() ? iterator.next() : null;
    }
}
