package com.sixgroup.refit.observability.job.common;

import com.sixgroup.refit.observability.model.ImpalaRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.stream.Collectors;

public class CsvItemWriter implements ItemWriter<ImpalaRow> {

    private static final Logger log = LoggerFactory.getLogger(CsvItemWriter.class);

    private final String outputPath;
    private final String delimiter;
    private boolean headerWritten = false;

    public CsvItemWriter(String outputPath, String delimiter) {
        this.outputPath = outputPath;
        this.delimiter = delimiter;
    }

    @Override
    public void write(Chunk<? extends ImpalaRow> chunk) throws Exception {
        if (chunk.isEmpty()) {
            return;
        }

        Path path = Paths.get(outputPath);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        try (BufferedWriter bw = Files.newBufferedWriter(path,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

            for (ImpalaRow row : chunk.getItems()) {
                Map<String, Object> cols = row.getColumns();

                if (!headerWritten) {
                    String header = String.join(delimiter, cols.keySet());
                    bw.write(header);
                    bw.newLine();
                    headerWritten = true;
                    log.info("Cabecera CSV escrita: {}", header);
                }

                String line = cols.values().stream()
                        .map(v -> v != null ? v.toString() : "")
                        .collect(Collectors.joining(delimiter));
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            log.error("Error escribiendo CSV en {}: {}", outputPath, e.getMessage());
            throw e;
        }

        log.info("CSV actualizado en {} con {} filas", outputPath, chunk.size());
    }
}
