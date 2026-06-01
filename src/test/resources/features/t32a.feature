Feature: T-32a Data Volume Totals
  Job que ejecuta dos queries COUNT(*) contra Impala, suma constantes HDFS
  y genera un CSV con los totales de trades y reports recibidos.

  Scenario: Generate T-32a CSV report successfully
    Given the job "t32aJob" with parameters:
      | fecha     | 2026-04-30 |
      | endPeriod | 2026-03-31 |
    When the job is launched
    Then the job status is "COMPLETED"
    And a CSV file exists at "target/test-output/ITEM32A.csv"
    And the CSV has a header row with columns:
      | TR_CODE                               |
      | REPORTING_DATE                        |
      | REGULATION_REFERENCE                  |
      | TOTAL_NR_TRADES_RECEIVED_FROM_START   |
      | TOTAL_NR_REPORTS_RECEIVED_FROM_START  |
      | COMMENTS                              |
    And the CSV has at least 1 data row
