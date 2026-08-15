package com.sixgroup.refit.observability.config;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableJpaRepositories(
    entityManagerFactoryRef = "kudurc-em",
    transactionManagerRef = "kudurc-trm",
    basePackages = "com.sixgroup.refit.observability.item37.creator.modules.infrastructure.repository.kudu"
)
@EnableTransactionManagement
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

    @Bean("kudurc-em")
    LocalContainerEntityManagerFactoryBean kuduDbEntityManagerFactory(EntityManagerFactoryBuilder builder, @Qualifier("kudurc-ds") DataSource dataSource) {
        final HashMap<String, String> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put(DatasourceKuduProperties.HIBERNATE_DIALECT, properties.getDialect());
        objectObjectHashMap.put(DatasourceKuduProperties.HIBERNATE_DDL_AUTO, properties.getDdlAuto());
        return builder
            .dataSource(dataSource)
            .packages("com.sixgroup.refit.observability")
            .properties(objectObjectHashMap)
            .build();
    }

    @Bean("kudurc-trm")
    PlatformTransactionManager kuduTransactionManager(@Qualifier("kudurc-em") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder() {
        return new EntityManagerFactoryBuilder(new HibernateJpaVendorAdapter(), new HashMap<>(), null);
    }
}
