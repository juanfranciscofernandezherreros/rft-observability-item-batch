package com.sixgroup.refit.observability.dto;

import lombok.*;

/**
 * Raw counts as returned by the Kudu/Impala queries in
 * {@link com.sixgroup.refit.observability.sql.Item32ADataFinderService},
 * before the business enrichment (initial totals, reporting date) that
 * turns them into an {@link com.sixgroup.refit.observability.model.Item32AData} entity.
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Item32ACountsDto {
    private Long tradesFromImpala;
    private Long reportsFromKudu;
}
