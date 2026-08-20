package com.sixgroup.refit.observability.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.core.io.Resource;

public class Item32CProcessor implements ItemProcessor<Resource, Resource> {

    @Override
    public Resource process(final Resource sqlFile) {
        if (sqlFile == null || !sqlFile.exists() || !sqlFile.isReadable()) {
            throw new IllegalStateException("item32c SQL file is missing or unreadable");
        }

        return sqlFile;
    }
}
