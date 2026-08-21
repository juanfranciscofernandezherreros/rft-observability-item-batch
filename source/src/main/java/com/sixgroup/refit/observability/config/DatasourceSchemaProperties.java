package com.sixgroup.refit.observability.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "component-config.kududb")
public class DatasourceSchemaProperties {

    @NotNull
    private String controlRefitSchema;

    @NotNull
    private String transcSchema;

    @NotNull
    private String accountMng;
}
