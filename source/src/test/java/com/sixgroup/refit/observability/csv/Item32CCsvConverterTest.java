package com.sixgroup.refit.observability.csv;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class Item32CCsvConverterTest {

    @Test
    void convertsSqlValuesToCsvPreservingNullEmptyAndCommaValues() throws Exception {
        String sql = """
                INSERT INTO schema.regu_report (regulatorid,reportcode,queryid,channel,reporttype,deliverydatefrom,deliverydateto,inserttmstmp,reportschedule,reportfrequencydaily,reportfrequencymonth,lastdayofmonth,reportformat,reportstatus,`partition`) VALUES
                ('account','WARN000','R1','TRACE','RRECU','2024-01-01 00:00:00.000000000','2024-01-02 00:00:00.000000000','2024-01-01 10:00:00.123456789','ADHO','TU,FR','',false,'XML','ACTIVE',NULL);
                """;

        Item32CCsvConverter converter = new Item32CCsvConverter(
                new ByteArrayResource(sql.getBytes(StandardCharsets.UTF_8)));

        String csv = converter.convertToCsv().getContentAsString(StandardCharsets.UTF_8);

        assertThat(csv).contains("\"TU,FR\"");
        assertThat(csv).contains(",\"\",\"false\",");
        assertThat(csv).contains("\"ACTIVE\",\\N");
    }
}
