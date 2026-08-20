package com.sixgroup.refit.observability.reader;

import org.springframework.batch.item.ItemReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class Item32CReader implements ItemReader<Resource> {

    private static final Resource ITEM32C_SQL = new ClassPathResource("item32c.sql");

    private boolean read;

    @Override
    public Resource read() {
        if (read) {
            return null;
        }

        read = true;
        return ITEM32C_SQL;
    }
}
