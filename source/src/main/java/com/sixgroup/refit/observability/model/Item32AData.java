package com.sixgroup.refit.observability.model;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Item32AData {
    private String reportingDate;
    private Long totalNrTrades;
    private Long totalNrReports;
}
