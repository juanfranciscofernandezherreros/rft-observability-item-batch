Feature: T-33a Reconciliation Statistics
  Job que ejecuta una query agrupada sobre reco_status_enh_hist_hdfs y genera
  un CSV con estadísticas de reconciliación por tipo de trade y estado.

  Scenario: Generate T-33a CSV report successfully
    Given the job "t33aJob" with parameters:
      | reportingDate | 2026-04-15 |
      | endDate       | 2026-03-31 |
    When the job is launched
    Then the job status is "COMPLETED"
    And a CSV file exists at "target/test-output/ITEM33A.csv"
    And the CSV has a header row with columns:
      | REPORTING_TR_CODE      |
      | REPORTING_DATE         |
      | SUBMISSION_STATUS      |
      | NON_SUBMISSION_REASON  |
      | PAIRED_STATUS          |
      | RECON_TYPE             |
      | OTHER_TR_CODE          |
      | DERIVATIVE_TYPE        |
      | TRADE_STATUS           |
      | TRADE_TYPE             |
      | EEA_STATUS             |
      | NR_OF_UTI              |
      | NR_OF_MATCHED_UTI      |
      | NR_OF_RECONCILED_UTI   |
      | NR_OF_UNMATCHED_UTI    |
      | COMMENTS               |
    And the CSV has at least 1 data row
