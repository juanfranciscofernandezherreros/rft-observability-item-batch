package com.regis.emirrefit.batch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "emir")
public class EmirQueryProperties {

    private String trCode = "TRRGS";
    private String regulationReference = "EMIR";
    private Tables tables = new Tables();
    private List<String> eeaCountries = List.of(
            "AT", "BE", "BG", "CY", "CZ", "DE", "DK", "EE", "GR", "ES",
            "FI", "FR", "HR", "HU", "IE", "IS", "IT", "LI", "LT", "LU",
            "LV", "MT", "NL", "NO", "PL", "PT", "RO", "SE", "SI", "SK");

    public static class Tables {
        /** T-32a query 1: trades. */
        private String oprData = "emir_refit_int_transc.opr_data";
        /** T-32a query 2: reports. */
        private String recordStatus = "emir_refit_int_control_refit.record_status";
        /** T-32b: latest trade state. */
        private String latestTradeState = "emir_refit_int_transc.latest_trade_state";
        /** T-32c: outgoing files. */
        private String reportsFileOutgoing = "emir_refit_int_control_refit.reports_file_outgoing";
        /** T-33a: reconciliation status. */
        private String recoStatusEnhHist = "emir_refit_int_reco.reco_status_enh_hist_hdfs";

        public String getOprData() { return oprData; }
        public void setOprData(String v) { this.oprData = v; }
        public String getRecordStatus() { return recordStatus; }
        public void setRecordStatus(String v) { this.recordStatus = v; }
        public String getLatestTradeState() { return latestTradeState; }
        public void setLatestTradeState(String v) { this.latestTradeState = v; }
        public String getReportsFileOutgoing() { return reportsFileOutgoing; }
        public void setReportsFileOutgoing(String v) { this.reportsFileOutgoing = v; }
        public String getRecoStatusEnhHist() { return recoStatusEnhHist; }
        public void setRecoStatusEnhHist(String v) { this.recoStatusEnhHist = v; }
    }

    public String getTrCode() { return trCode; }
    public void setTrCode(String v) { this.trCode = v; }
    public String getRegulationReference() { return regulationReference; }
    public void setRegulationReference(String v) { this.regulationReference = v; }
    public Tables getTables() { return tables; }
    public void setTables(Tables tables) { this.tables = tables; }
    public List<String> getEeaCountries() { return eeaCountries; }
    public void setEeaCountries(List<String> v) { this.eeaCountries = v; }

    public String eeaCountriesSqlList() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < eeaCountries.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append('\'').append(eeaCountries.get(i)).append('\'');
        }
        return sb.toString();
    }
}
