CREATE SCHEMA IF NOT EXISTS emir_refit_mbt_account_mng;

CREATE TABLE IF NOT EXISTS emir_refit_mbt_account_mng.regu_report (
    regulatorid VARCHAR(64),
    reportcode VARCHAR(32),
    queryid VARCHAR(32),
    channel VARCHAR(32),
    reporttype VARCHAR(32),
    deliverydatefrom TIMESTAMP(9),
    deliverydateto TIMESTAMP(9),
    inserttmstmp TIMESTAMP(9),
    reportschedule VARCHAR(8),
    reportfrequencydaily VARCHAR(8),
    reportfrequencymonth VARCHAR(8),
    lastdayofmonth BOOLEAN,
    reportformat VARCHAR(16),
    reportstatus VARCHAR(32),
    `partition` VARCHAR(255)
);
