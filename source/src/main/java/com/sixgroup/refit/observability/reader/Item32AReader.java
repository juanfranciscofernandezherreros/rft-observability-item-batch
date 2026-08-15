package com.sixgroup.refit.observability.reader;

import com.sixgroup.refit.observability.dto.Item32ACountsDto;
import com.sixgroup.refit.observability.sql.Item32ADataFinderService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The reader IS the Item T32A query: it delegates to
 * {@link Item32ADataFinderService#fetchCounts}, which runs the two
 * Impala/Kudu COUNT queries, and hands the single raw-counts DTO to the
 * step once before signalling end-of-data.
 */
@RequiredArgsConstructor
public class Item32AReader implements ItemReader<Item32ACountsDto> {

    private final Item32ADataFinderService item32ADataFinderService;
    private final String endPeriod;
    private final AtomicBoolean alreadyRead = new AtomicBoolean(false);

    @Override
    public Item32ACountsDto read() {
        if (alreadyRead.compareAndSet(false, true)) {
            return item32ADataFinderService.fetchCounts(endPeriod);
        }
        return null;
    }
}
