package com.sixgroup.refit.observability.processor;

import com.sixgroup.refit.observability.config.Item32Properties;
import com.sixgroup.refit.observability.dto.Item32ACountsDto;
import com.sixgroup.refit.observability.model.Item32AData;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

/**
 * Turns the raw Kudu/Impala counts DTO into the Item32AData entity by
 * applying the business rule (adding each series' initial total) and
 * stamping the reporting date.
 */
@RequiredArgsConstructor
public class Item32AProcessor implements ItemProcessor<Item32ACountsDto, Item32AData> {

    private final Item32Properties item32Properties;
    private final String reportingDate;

    @Override
    public Item32AData process(Item32ACountsDto counts) {
        final long totalNrTrades = counts.getTradesFromImpala()
                + item32Properties.getItem32Aproperties().getInitialTotalTradesNew();
        final long totalNrReports = counts.getReportsFromKudu()
                + item32Properties.getItem32Aproperties().getInitialTotalTradesAll();

        return Item32AData.builder()
                .reportingDate(reportingDate)
                .totalNrTrades(totalNrTrades)
                .totalNrReports(totalNrReports)
                .build();
    }
}
