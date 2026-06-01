package com.regis.emirrefit.batch.job.t32a;

import com.regis.emirrefit.batch.model.ImpalaRow;
import org.springframework.batch.item.ItemProcessor;

public class T32AItemProcessor implements ItemProcessor<ImpalaRow, ImpalaRow> {

    private static final long NUMBER_OF_TRADES_HDFS = 20002398670L;
    private static final long NUMBER_OF_REPORTS_HDFS = 45243713141L;
    private final String fecha;

    public T32AItemProcessor(String fecha) {
        this.fecha = fecha;
    }

    @Override
    public ImpalaRow process(ImpalaRow item) throws Exception {
        Long rawTrades = (Long) item.get("TOTAL_NR_TRADES_RECEIVED_FROM_START");
        Long rawReports = (Long) item.get("TOTAL_NR_REPORTS_RECEIVED_FROM_START");
        long totalTrades = (rawTrades != null ? rawTrades : 0L) + NUMBER_OF_TRADES_HDFS;
        long totalReports = (rawReports != null ? rawReports : 0L) + NUMBER_OF_REPORTS_HDFS;
        ImpalaRow output = new ImpalaRow();
        output.put("TR_CODE", "TRRGS");
        output.put("REPORTING_DATE", fecha);
        output.put("REGULATION_REFERENCE", "EMIR");
        output.put("TOTAL_NR_TRADES_RECEIVED_FROM_START", totalTrades);
        output.put("TOTAL_NR_REPORTS_RECEIVED_FROM_START", totalReports);
        output.put("COMMENTS", ""); // Columna vacía

        return output;
    }
}
