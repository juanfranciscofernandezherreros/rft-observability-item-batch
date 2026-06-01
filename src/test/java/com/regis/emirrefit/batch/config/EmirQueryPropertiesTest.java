package com.regis.emirrefit.batch.config;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class EmirQueryPropertiesTest {
    @Test void defaults()      { var p=new EmirQueryProperties(); assertEquals("TRRGS",p.getTrCode()); assertEquals("EMIR",p.getRegulationReference()); assertFalse(p.getEeaCountries().isEmpty()); }
    @Test void setTrCode()     { var p=new EmirQueryProperties(); p.setTrCode("X"); assertEquals("X",p.getTrCode()); }
    @Test void setRegRef()     { var p=new EmirQueryProperties(); p.setRegulationReference("M"); assertEquals("M",p.getRegulationReference()); }
    @Test void setEea()        { var p=new EmirQueryProperties(); p.setEeaCountries(List.of("ES")); assertEquals(1,p.getEeaCountries().size()); }
    @Test void sqlList()       { var p=new EmirQueryProperties(); p.setEeaCountries(List.of("AT","BE")); assertEquals("'AT','BE'",p.eeaCountriesSqlList()); }
    @Test void sqlListSingle() { var p=new EmirQueryProperties(); p.setEeaCountries(List.of("DE")); assertEquals("'DE'",p.eeaCountriesSqlList()); }
    @Test void setTablesObj()  { var p=new EmirQueryProperties(); var t=new EmirQueryProperties.Tables(); t.setOprData("x"); p.setTables(t); assertEquals("x",p.getTables().getOprData()); }

    @Test void tablesDefaults() {
        var t=new EmirQueryProperties().getTables();
        assertTrue(t.getOprData().contains("opr_data"));
        assertTrue(t.getRecordStatus().contains("record_status"));
        assertTrue(t.getLatestTradeState().contains("latest_trade_state"));
        assertTrue(t.getReportsFileOutgoing().contains("reports_file_outgoing"));
        assertTrue(t.getRecoStatusEnhHist().contains("reco_status"));
    }
    @Test void tablesSetters() {
        var t=new EmirQueryProperties.Tables();
        t.setOprData("a"); t.setRecordStatus("b"); t.setLatestTradeState("c"); t.setReportsFileOutgoing("d"); t.setRecoStatusEnhHist("e");
        assertEquals("a",t.getOprData()); assertEquals("b",t.getRecordStatus()); assertEquals("c",t.getLatestTradeState()); assertEquals("d",t.getReportsFileOutgoing()); assertEquals("e",t.getRecoStatusEnhHist());
    }
}
