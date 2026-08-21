package com.sixgroup.refit.observability.csv;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Item32CCsvConverter {

    public static final String NULL_TOKEN = "\\N";

    private static final String[] HEADERS = {
            "regulatorid", "reportcode", "queryid", "channel", "reporttype",
            "deliverydatefrom", "deliverydateto", "inserttmstmp", "reportschedule",
            "reportfrequencydaily", "reportfrequencymonth", "lastdayofmonth",
            "reportformat", "reportstatus", "partition"
    };

    private final Resource sqlResource;

    public Item32CCsvConverter() {
        this(new ClassPathResource("item32c.sql"));
    }

    Item32CCsvConverter(Resource sqlResource) {
        this.sqlResource = sqlResource;
    }

    public Resource convertToCsv() {
        try {
            Path csv = Files.createTempFile("item32c-", ".csv");
            csv.toFile().deleteOnExit();

            try (BufferedReader reader = new BufferedReader(
                    new java.io.InputStreamReader(sqlResource.getInputStream(), StandardCharsets.UTF_8));
                 BufferedWriter writer = Files.newBufferedWriter(csv, StandardCharsets.UTF_8)) {

                writer.write(String.join(",", HEADERS));
                writer.newLine();

                String line;
                int row = 0;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (!trimmed.startsWith("(")) {
                        continue;
                    }

                    if (trimmed.endsWith(",") || trimmed.endsWith(";")) {
                        trimmed = trimmed.substring(0, trimmed.length() - 1);
                    }
                    if (!trimmed.endsWith(")")) {
                        throw new IllegalStateException("Invalid item32c SQL tuple: " + trimmed);
                    }

                    List<String> values = parseTuple(trimmed.substring(1, trimmed.length() - 1));
                    if (values.size() != HEADERS.length) {
                        throw new IllegalStateException(
                                "Expected " + HEADERS.length + " columns in item32c row but got " + values.size());
                    }

                    writer.write(values.stream().map(Item32CCsvConverter::escapeCsv).reduce((a, b) -> a + "," + b).orElse(""));
                    writer.newLine();
                    row++;
                }

                if (row == 0) {
                    throw new IllegalStateException("item32c.sql did not contain any VALUES rows");
                }
            }

            return new FileSystemResource(csv);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to convert item32c.sql to CSV", e);
        }
    }

    private static List<String> parseTuple(String tuple) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        boolean quotedValue = false;

        for (int i = 0; i < tuple.length(); i++) {
            char c = tuple.charAt(i);
            if (inQuotes) {
                if (c == '\'') {
                    if (i + 1 < tuple.length() && tuple.charAt(i + 1) == '\'') {
                        current.append('\'');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '\'') {
                inQuotes = true;
                quotedValue = true;
            } else if (c == ',') {
                result.add(normalize(current.toString(), quotedValue));
                current.setLength(0);
                quotedValue = false;
            } else {
                current.append(c);
            }
        }

        if (inQuotes) {
            throw new IllegalStateException("Unclosed quoted value in item32c.sql");
        }
        result.add(normalize(current.toString(), quotedValue));
        return result;
    }

    private static String normalize(String raw, boolean quoted) {
        if (quoted) {
            return raw;
        }
        String value = raw.trim();
        return "NULL".equalsIgnoreCase(value) ? null : value;
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return NULL_TOKEN;
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
