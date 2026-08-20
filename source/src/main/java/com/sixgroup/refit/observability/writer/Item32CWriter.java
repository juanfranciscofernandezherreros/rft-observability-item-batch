package com.sixgroup.refit.observability.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;

@Slf4j
@RequiredArgsConstructor
public class Item32CWriter implements ItemWriter<Resource> {

    private final DataSource dataSource;

    @Override
    public void write(final Chunk<? extends Resource> chunk) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            for (Resource sqlFile : chunk) {
                log.info("Executing item32c SQL file: {}", sqlFile.getDescription());
                ScriptUtils.executeSqlScript(connection, sqlFile);
            }
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
