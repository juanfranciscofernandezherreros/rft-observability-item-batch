package com.regis.emirrefit.batch.job.t32a;

public class T32AOutput {
    private String trCode;
    private String reportingDate;
    private String regulationReference;
    private Long totalNrTradesReceivedFromStart;
    private Long totalNrReportsReceivedFromStart;
    private String comments;

    public String getTrCode() { return trCode; }
    public void setTrCode(String trCode) { this.trCode = trCode; }

    public String getReportingDate() { return reportingDate; }
    public void setReportingDate(String reportingDate) { this.reportingDate = reportingDate; }

    public String getRegulationReference() { return regulationReference; }
    public void setRegulationReference(String regulationReference) { this.regulationReference = regulationReference; }

    public Long getTotalNrTradesReceivedFromStart() { return totalNrTradesReceivedFromStart; }
    public void setTotalNrTradesReceivedFromStart(Long totalNrTradesReceivedFromStart) { this.totalNrTradesReceivedFromStart = totalNrTradesReceivedFromStart; }

    public Long getTotalNrReportsReceivedFromStart() { return totalNrReportsReceivedFromStart; }
    public void setTotalNrReportsReceivedFromStart(Long totalNrReportsReceivedFromStart) { this.totalNrReportsReceivedFromStart = totalNrReportsReceivedFromStart; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}