Feature: T-32c Reports SLA
  Job que ejecuta una query CTE sobre reports_file_outgoing, calcula la diferencia
  entre REPORT_COMPLETION_TIME y SLA en horas, y genera un CSV.

  Scenario: Generate T-32c CSV report successfully
    Given the job "t32cJob" with parameters:
      | fecha     | 2026-04-30                  |
      | startDate | 2026-01-01 00:00:00.000     |
      | endDate   | 2026-03-31 00:00:00.000     |
    When the job is launched
    Then the job status is "COMPLETED"
    And a CSV file exists at "target/test-output/ITEM32C.csv"
    And the CSV has a header row with columns:
      | TR_CODE                  |
      | REPORTING_DATE           |
      | REGULATION_REFERENCE     |
      | REPORT_NAME              |
      | REPORT_TYPE              |
      | REPORT_GENERATION_TIME   |
      | REPORT_COMPLETION_TIME   |
      | REPORT_PUBLICATION_TIME  |
      | DATE                     |
      | SLA                      |
      | DIFFERENCE               |
      | SLA_BREACH_ID            |
    And the CSV has at least 1 data row
