package com.sixgroup.refit.observability.writer;

import com.sixgroup.refit.observability.model.Item32CRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

@Slf4j
public class Item32CWriter implements ItemWriter<Item32CRow> {

    private static final String NULL_TOKEN = "\\N";
    private static final String COLUMNS = "(regulatorid,reportcode,queryid,channel,reporttype,deliverydatefrom,deliverydateto,inserttmstmp,reportschedule,reportfrequencydaily,reportfrequencymonth,lastdayofmonth,reportformat,reportstatus,`partition`)";
    private static final String VALUES = " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private final JdbcTemplate jdbcTemplate;
    private final String insertSql;

    public Item32CWriter(DataSource dataSource, String targetTable) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.insertSql = "INSERT INTO " + validateTable(targetTable) + " " + COLUMNS + VALUES;
    }

    @Override
    public void write(final Chunk<? extends Item32CRow> chunk) {
        List<? extends Item32CRow> rows = chunk.getItems();
        log.info("Inserting {} item32c rows into Impala", rows.size());
        jdbcTemplate.batchUpdate(insertSql, rows, rows.size(), Item32CWriter::bindRow);
    }

    private static void bindRow(PreparedStatement ps, Item32CRow row) throws SQLException {
        setNullableString(ps, 1, row.regulatorId());
        setNullableString(ps, 2, row.reportCode());
        setNullableString(ps, 3, row.queryId());
        setNullableString(ps, 4, row.channel());
        setNullableString(ps, 5, row.reportType());
        setNullableTimestamp(ps, 6, row.deliveryDateFrom());
        setNullableTimestamp(ps, 7, row.deliveryDateTo());
        setNullableTimestamp(ps, 8, row.insertTimestamp());
        setNullableString(ps, 9, row.reportSchedule());
        setNullableString(ps, 10, row.reportFrequencyDaily());
        setNullableString(ps, 11, row.reportFrequencyMonth());
        setNullableBoolean(ps, 12, row.lastDayOfMonth());
        setNullableString(ps, 13, row.reportFormat());
        setNullableString(ps, 14, row.reportStatus());
        setNullableString(ps, 15, row.partition());
    }

    private static void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (isNull(value)) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    private static void setNullableTimestamp(PreparedStatement ps, int index, String value) throws SQLException {
        if (isNull(value)) {
            ps.setNull(index, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(index, Timestamp.valueOf(value));
        }
    }

    private static void setNullableBoolean(PreparedStatement ps, int index, String value) throws SQLException {
        if (isNull(value)) {
            ps.setNull(index, Types.BOOLEAN);
        } else {
            ps.setBoolean(index, Boolean.parseBoolean(value));
        }
    }

    private static boolean isNull(String value) {
        return value == null || NULL_TOKEN.equals(value);
    }

    private static String validateTable(String targetTable) {
        if (targetTable == null || !targetTable.matches("[A-Za-z0-9_]+\\.[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("Invalid item32c target table: " + targetTable);
        }
        return targetTable;
    }
}
