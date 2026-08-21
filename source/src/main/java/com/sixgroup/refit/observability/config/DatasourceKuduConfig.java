package com.sixgroup.refit.observability.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;

@Configuration
@ConditionalOnProperty(prefix = "spring.datasource.kududb", name = "jdbc-url")
@RequiredArgsConstructor
public class DatasourceKuduConfig {

    private final DatasourceKuduProperties properties;

    @Bean("kudurc-ds")
    public DataSource kuduDataSource() {
        return DataSourceBuilder.create()
                .url(properties.getJdbcUrl())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .driverClassName(properties.getDriverClassName())
                .build();
    }

    @Bean("kudurc-trm")
    PlatformTransactionManager kuduTransactionManager(@Qualifier("kudurc-ds") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
