package com.regis.emirrefit.batch.job.t32c;

import com.regis.emirrefit.batch.model.ImpalaRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class T32CItemProcessor implements ItemProcessor<ImpalaRow, ImpalaRow> {

    private static final Logger log = LoggerFactory.getLogger(T32CItemProcessor.class);
    private static final DateTimeFormatter ISO_OUT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Override
    public ImpalaRow process(ImpalaRow item) {

        // ── 1. Calcular DIFFERENCE (horas) ──────────────────────────────
        Instant completionInstant = parseUtcTimestamp(item.get("REPORT_COMPLETION_TIME"));
        Instant slaInstant = parseUtcTimestamp(item.get("SLA"));

        String difference = "";
        if (completionInstant != null && slaInstant != null) {
            long diffSeconds = ChronoUnit.SECONDS.between(slaInstant, completionInstant);
            BigDecimal diffHours = BigDecimal.valueOf(diffSeconds)
                    .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
            difference = diffHours.toPlainString();
        }

        // ── 2. Formatear timestamps a ISO ───────────────────────────────
        String genTime = formatToIso(item.get("REPORT_GENERATION_TIME"));
        String compTime = formatToIso(item.get("REPORT_COMPLETION_TIME"));

        // ── 3. Construir fila de salida con orden exacto del CSV ────────
        ImpalaRow output = new ImpalaRow();
        output.put("TR_CODE", item.get("TR_CODE"));
        output.put("REPORTING_DATE", item.get("REPORTING_DATE"));
        output.put("REGULATION_REFERENCE", item.get("REGULATION_REFERENCE"));
        output.put("REPORT_NAME", item.get("REPORT_NAME"));
        output.put("REPORT_TYPE", item.get("REPORT_TYPE"));
        output.put("REPORT_GENERATION_TIME", genTime);
        output.put("REPORT_COMPLETION_TIME", compTime);
        output.put("REPORT_PUBLICATION_TIME", item.get("REPORT_PUBLICATION_TIME"));
        output.put("DATE", item.get("SESSION"));          // SESSION → DATE
        output.put("SLA", item.get("SLA"));
        output.put("DIFFERENCE", difference);
        output.put("SLA_BREACH_ID", item.get("SLA_BREACH_ID"));

        return output;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers de parsing/formateo
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Intenta parsear un valor como timestamp UTC.
     * Soporta: ISO 8601 con Z/offset y timestamps Impala "yyyy-MM-dd HH:mm:ss.SSS".
     */
    private Instant parseUtcTimestamp(Object value) {
        if (value == null) return null;
        String s = value.toString().trim();
        if (s.isEmpty()) return null;

        // ISO con Z o offset → directo
        try {
            return Instant.parse(s);
        } catch (DateTimeParseException ignored) { }

        // Impala naive "2026-01-15 08:30:45.123" → tratar como UTC
        try {
            String iso = s.replace(' ', 'T');
            if (!iso.endsWith("Z") && !iso.contains("+")) {
                iso += "Z";
            }
            return ZonedDateTime.parse(iso, DateTimeFormatter.ISO_ZONED_DATE_TIME).toInstant();
        } catch (DateTimeParseException ignored) { }

        log.debug("No se pudo parsear timestamp: '{}'", s);
        return null;
    }

    private String formatToIso(Object value) {
        Instant inst = parseUtcTimestamp(value);
        if (inst == null) {
            return value != null ? value.toString() : "";
        }
        return ISO_OUT.format(inst.atZone(ZoneOffset.UTC));
    }
}
