DROP TABLE IF EXISTS opr_data;
CREATE TABLE opr_data (
    actntp       VARCHAR(10) NOT NULL,
    rptgtmstmpdt DATE        NOT NULL
);

DROP TABLE IF EXISTS record_status;
CREATE TABLE record_status (
    receiveddt DATE NOT NULL
);
