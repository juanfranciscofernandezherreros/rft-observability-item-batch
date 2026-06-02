package com.sixgroup.refit.observability.cucumber.steps;

import com.sixgroup.refit.observability.api.JobLaunchService;
import com.sixgroup.refit.observability.api.dto.JobRunRequest;
import com.sixgroup.refit.observability.api.dto.JobRunResponse;
import com.sixgroup.refit.observability.model.ImpalaRow;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class JobStepDefinitions {

    @Autowired
    private JobLaunchService jobLaunchService;

    @Autowired
    @Qualifier("impalaJdbcTemplate")
    private JdbcTemplate mockImpalaJdbcTemplate;

    private String jobName;
    private JobRunRequest request;
    private JobRunResponse response;

    @Before
    public void setup() throws IOException {
        // Limpiar directorio de salida entre escenarios
        Path outputDir = Path.of("target/test-output");
        if (Files.exists(outputDir)) {
            try (Stream<Path> walk = Files.walk(outputDir)) {
                walk.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (Exception ignored) { }
                    });
            }
        }

        reset(mockImpalaJdbcTemplate);

        when(mockImpalaJdbcTemplate.queryForObject(
                anyString(), eq(Long.class), anyString()))
                .thenReturn(12345L);

        doAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            if (sql.contains("COUNTERPARTY_ID")) {
                return List.of(buildT32bRow());
            }
            if (sql.contains("REPORT_NAME")) {
                return List.of(buildT32cRow());
            }
            if (sql.contains("REPORTING_TR_CODE")) {
                return List.of(buildT33aRow());
            }
            return List.of();
        }).when(mockImpalaJdbcTemplate).query(
                anyString(),
                any(PreparedStatementSetter.class),
                any(RowMapper.class));
    }

    @Given("the job {string} with parameters:")
    public void theJobWithParameters(String jobName, DataTable table) {
        this.jobName = jobName;
        this.request = new JobRunRequest();

        Map<String, String> params = table.asMap(String.class, String.class);
        request.setFecha(params.get("fecha"));
        request.setEndPeriod(params.get("endPeriod"));
        request.setStartDate(params.get("startDate"));
        request.setEndDate(params.get("endDate"));
        request.setReportingDate(params.get("reportingDate"));
        request.setOutputPath(params.get("outputPath"));
    }

    @When("the job is launched")
    public void theJobIsLaunched() {
        response = jobLaunchService.launch(jobName, request);
        assertNotNull(response, "La respuesta del job no debe ser null");
    }

    @Then("the job status is {string}")
    public void theJobStatusIs(String expectedStatus) {
        assertEquals(expectedStatus, response.getStatus(),
                "Estado esperado del job " + jobName);
    }

    @And("a CSV file exists at {string}")
    public void csvFileExistsAt(String path) {
        Path csvPath = Path.of(path);
        assertTrue(Files.exists(csvPath),
                "El fichero CSV no existe: " + csvPath.toAbsolutePath());
        assertTrue(Files.isRegularFile(csvPath),
                "La ruta no es un fichero regular: " + csvPath);
    }

    @And("the CSV has a header row with columns:")
    public void csvHasHeaderWithColumns(DataTable expectedColumns) throws IOException {
        Path csvPath = findCsvOutput();
        List<String> expected = expectedColumns.asList();

        try (BufferedReader br = Files.newBufferedReader(csvPath)) {
            String headerLine = br.readLine();
            assertNotNull(headerLine, "El CSV está vacío (sin cabecera)");
            String[] actualCols = headerLine.split(";");
            for (String col : expected) {
                boolean found = false;
                for (String actual : actualCols) {
                    if (actual.trim().equals(col.trim())) {
                        found = true;
                        break;
                    }
                }
                assertTrue(found, "Columna '" + col + "' no encontrada en cabecera: " + headerLine);
            }
        }
    }

    @And("the CSV has at least {int} data row")
    public void csvHasAtLeastNDataRows(int minRows) throws IOException {
        Path csvPath = findCsvOutput();
        try (BufferedReader br = Files.newBufferedReader(csvPath)) {
            br.readLine(); // saltar cabecera
            int count = 0;
            while (br.readLine() != null) {
                count++;
            }
            assertTrue(count >= minRows,
                    "Se esperaban al menos " + minRows + " fila(s) de datos, pero hay " + count);
        }
    }

    private Path findCsvOutput() throws IOException {
        Path outputDir = Path.of("target/test-output");
        assertTrue(Files.exists(outputDir), "Directorio de salida no existe");
        try (Stream<Path> files = Files.list(outputDir)) {
            return files.filter(p -> p.toString().endsWith(".csv"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("No se encontró ningún CSV en " + outputDir));
        }
    }

    private ImpalaRow buildT32bRow() {
        ImpalaRow row = new ImpalaRow();
        row.put("COUNTERPARTY_ID", "LEI1234567890");
        row.put("COUNTERPARTY_COUNTRY", "ES");
        row.put("SUBMITTING_ENTITY_ID", "SUBMIT001");
        row.put("DATA_QUALITY_CATEGORY", "A");
        row.put("NR_OUTSTANDING_TRADES", 150L);
        row.put("NR_NON_OUTSTANDING_TRADES", 30L);
        return row;
    }

    private ImpalaRow buildT32cRow() {
        ImpalaRow row = new ImpalaRow();
        row.put("TR_CODE", "TRRGS");
        row.put("REPORTING_DATE", "2026-04-30");
        row.put("REGULATION_REFERENCE", "EMIR");
        row.put("REPORT_NAME", "TD107_ESMAS_TEST");
        row.put("REPORT_TYPE", "ESMA");
        row.put("REPORT_GENERATION_TIME", "2026-03-15 08:00:00.000");
        row.put("REPORT_COMPLETION_TIME", "2026-03-15 08:30:00.000");
        row.put("REPORT_PUBLICATION_TIME", "2026-03-15 09:00:00.000");
        row.put("SESSION", "2026-03-14");
        row.put("SLA", "2026-03-15T12:00:00Z");
        row.put("DIFFERENCE", "");
        row.put("SLA_BREACH_ID", "");
        return row;
    }

    private ImpalaRow buildT33aRow() {
        ImpalaRow row = new ImpalaRow();
        row.put("REPORTING_TR_CODE", "TRRGS");
        row.put("REPORTING_DATE", "2026-04-15");
        row.put("SUBMISSION_STATUS", "SUBMITTED");
        row.put("NON_SUBMISSION_REASON", null);
        row.put("PAIRED_STATUS", "PAIRED");
        row.put("RECON_TYPE", "INTRA_TR");
        row.put("OTHER_TR_CODE", "TRRGS");
        row.put("DERIVATIVE_TYPE", "OTC");
        row.put("TRADE_STATUS", "OUTSTANDING");
        row.put("TRADE_TYPE", "DUAL_SIDED");
        row.put("EEA_STATUS", "Y");
        row.put("NR_OF_UTI", 500L);
        row.put("NR_OF_MATCHED_UTI", 480L);
        row.put("NR_OF_RECONCILED_UTI", 10L);
        row.put("NR_OF_UNMATCHED_UTI", 10L);
        row.put("COMMENTS", "");
        return row;
    }
}
