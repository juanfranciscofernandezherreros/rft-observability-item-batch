package com.sixgroup.refit.observability.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Parámetros de ejecución de un job de reporting")
public class JobRunRequest {

    @Schema(description = "Fecha de reporte / REPORTING_DATE", example = "2026-04-30")
    private String fecha;

    @Schema(description = "Fecha de corte (T-32a, T-32b)", example = "2026-03-31")
    private String endPeriod;

    @Schema(description = "Inicio de ventana (T-32c)", example = "2026-01-01 00:00:00.000")
    private String startDate;

    @Schema(description = "Fin de ventana (T-32c) / enhoriginaldt (T-33a)", example = "2026-03-31 00:00:00.000")
    private String endDate;

    @Schema(description = "REPORTING_DATE para T-33a", example = "2026-04-15")
    private String reportingDate;

    @Schema(description = "Ruta de salida del CSV (opcional; si se omite usa la de config)",
            example = "/data/out/TRRGS_EMIR_ITEM32A.csv")
    private String outputPath;

    public String getFecha() { return fecha; }
    public void setFecha(String v) { this.fecha = v; }
    public String getEndPeriod() { return endPeriod; }
    public void setEndPeriod(String v) { this.endPeriod = v; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String v) { this.startDate = v; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String v) { this.endDate = v; }
    public String getReportingDate() { return reportingDate; }
    public void setReportingDate(String v) { this.reportingDate = v; }
    public String getOutputPath() { return outputPath; }
    public void setOutputPath(String v) { this.outputPath = v; }
}
