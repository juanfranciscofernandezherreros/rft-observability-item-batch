package com.regis.emirrefit.batch.job.t32b;

import com.regis.emirrefit.batch.model.ImpalaRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class T32BItemProcessor implements ItemProcessor<ImpalaRow, ImpalaRow> {

    private static final Logger log = LoggerFactory.getLogger(T32BItemProcessor.class);

    private final String fecha;

    public T32BItemProcessor(String fecha) {
        this.fecha = fecha;
    }

    @Override
    public ImpalaRow process(ImpalaRow item) {

        ImpalaRow output = new ImpalaRow();

        // ── Columnas estáticas ──────────────────────────────────────────
        output.put("TR_CODE", "TRRGS");
        output.put("REPORTING_DATE", fecha);
        output.put("REGULATION_REFERENCE", "EMIR");

        // ── Columnas dinámicas del Reader ───────────────────────────────
        output.put("COUNTERPARTY_ID", item.get("COUNTERPARTY_ID"));
        output.put("COUNTERPARTY_COUNTRY", item.get("COUNTERPARTY_COUNTRY"));
        output.put("SUBMITTING_ENTITY_ID", item.get("SUBMITTING_ENTITY_ID"));
        output.put("DATA_QUALITY_CATEGORY", item.get("DATA_QUALITY_CATEGORY"));

        // ── Conversión numérica (replica pd.to_numeric → Int64) ────────
        output.put("NR_OUTSTANDING_TRADES", toLong(item.get("NR_OUTSTANDING_TRADES")));
        output.put("NR_NON_OUTSTANDING_TRADES", toLong(item.get("NR_NON_OUTSTANDING_TRADES")));

        // ── Columna final vacía ─────────────────────────────────────────
        output.put("COMMENTS", "");

        return output;
    }

    /**
     * Convierte un valor (puede ser Number, String con comas, null) a Long.
     * Replica el comportamiento de:
     *   pd.to_numeric(df[col].astype(str).str.replace(',', ''), errors='coerce').astype('Int64')
     */
    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            String s = value.toString().replace(",", "").trim();
            if (s.isEmpty()) {
                return null;
            }
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            log.warn("No se pudo convertir a Long: '{}'", value);
            return null;
        }
    }
}
