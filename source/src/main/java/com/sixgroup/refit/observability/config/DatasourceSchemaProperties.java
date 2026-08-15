package com.sixgroup.refit.observability.config;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "component-config.kududb")
public class DatasourceSchemaProperties {
    @NotNull
    private String controlRefitSchema;

    @NotNull
    private String transcSchema;
}
