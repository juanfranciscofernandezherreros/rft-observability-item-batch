package com.sixgroup.refit.observability.config;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("component-config.item32")
public class Item32Properties {
    @NotNull
    private Item32AProperties item32Aproperties;

    @Getter
    @Setter
    public static class Item32AProperties {

        @NotNull
        private Long initialTotalTradesNew;
        @NotNull
        private Long initialTotalTradesAll;
    }
}
