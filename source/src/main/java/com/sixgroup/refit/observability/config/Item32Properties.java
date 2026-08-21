package com.sixgroup.refit.observability.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties("component-config.item32")
public class Item32Properties {

    @NotNull
    private String trCode;
    @NotNull
    private String regulationReference;
    @NotNull
    private Item32AProperties item32Aproperties;
    @NotNull
    private Item32BProperties item32BProperties;
    @NotNull
    private Item32C item32C;

    @Getter
    @Setter
    public static class Item32AProperties {

        @NotNull
        private Long initialTotalTradesNew;
        @NotNull
        private Long initialTotalTradesAll;
        @NotNull
        private String fileNamePattern;
    }

    @Getter
    @Setter
    public static class Item32BProperties {

        @NotNull
        private String fileNamePattern;
    }

    @Getter
    @Setter
    public static class Item32C {

        @NotNull
        private String fileNamePattern;

        private String customerAccountPattern;

        private String regulatorAccountPattern;
    }
}
