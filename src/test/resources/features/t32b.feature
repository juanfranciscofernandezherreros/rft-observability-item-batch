Feature: T-32b Data Volume by Counterparty
  Job que ejecuta una query agrupada contra latest_trade_state y genera un CSV
  con trades outstanding/non-outstanding por counterparty y categoría de calidad.

  Scenario: Generate T-32b CSV report successfully
    Given the job "t32bJob" with parameters:
      | fecha     | 2026-04-30 |
      | endPeriod | 2026-03-31 |
    When the job is launched
    Then the job status is "COMPLETED"
    And a CSV file exists at "target/test-output/ITEM32B.csv"
    And the CSV has a header row with columns:
      | TR_CODE                 |
      | REPORTING_DATE          |
      | REGULATION_REFERENCE    |
      | COUNTERPARTY_ID         |
      | COUNTERPARTY_COUNTRY    |
      | SUBMITTING_ENTITY_ID    |
      | DATA_QUALITY_CATEGORY   |
      | NR_OUTSTANDING_TRADES   |
      | NR_NON_OUTSTANDING_TRADES |
      | COMMENTS                |
    And the CSV has at least 1 data row
