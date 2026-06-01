package com.regis.emirrefit.batch.job.t33a;

import com.regis.emirrefit.batch.model.ImpalaRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class T33AItemProcessor implements ItemProcessor<ImpalaRow, ImpalaRow> {

    private static final Logger log = LoggerFactory.getLogger(T33AItemProcessor.class);

    @Override
    public ImpalaRow process(ImpalaRow item) {

        ImpalaRow output = new ImpalaRow();

        // ── Columnas de agrupación (strings) ────────────────────────────
        output.put("REPORTING_TR_CODE",     item.get("REPORTING_TR_CODE"));
        output.put("REPORTING_DATE",        item.get("REPORTING_DATE"));
        output.put("SUBMISSION_STATUS",     item.get("SUBMISSION_STATUS"));
        output.put("NON_SUBMISSION_REASON", item.get("NON_SUBMISSION_REASON"));
        output.put("PAIRED_STATUS",         item.get("PAIRED_STATUS"));
        output.put("RECON_TYPE",            item.get("RECON_TYPE"));
        output.put("OTHER_TR_CODE",         item.get("OTHER_TR_CODE"));
        output.put("DERIVATIVE_TYPE",       item.get("DERIVATIVE_TYPE"));
        output.put("TRADE_STATUS",          item.get("TRADE_STATUS"));
        output.put("TRADE_TYPE",            item.get("TRADE_TYPE"));
        output.put("EEA_STATUS",            item.get("EEA_STATUS"));

        // ── Contadores numéricos ────────────────────────────────────────
        output.put("NR_OF_UTI",             toLong(item.get("NR_OF_UTI")));
        output.put("NR_OF_MATCHED_UTI",     toLong(item.get("NR_OF_MATCHED_UTI")));
        output.put("NR_OF_RECONCILED_UTI",  toLong(item.get("NR_OF_RECONCILED_UTI")));
        output.put("NR_OF_UNMATCHED_UTI",   toLong(item.get("NR_OF_UNMATCHED_UTI")));

        // ── Columna final vacía ─────────────────────────────────────────
        output.put("COMMENTS", "");

        return output;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            String s = value.toString().replace(",", "").trim();
            return s.isEmpty() ? null : Long.parseLong(s);
        } catch (NumberFormatException e) {
            log.warn("No se pudo convertir a Long: '{}'", value);
            return null;
        }
    }
}
