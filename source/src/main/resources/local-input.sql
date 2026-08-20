CREATE TABLE IF NOT EXISTS local_sql_test (
    id BIGINT PRIMARY KEY,
    description VARCHAR(255)
);

INSERT INTO local_sql_test (id, description)
VALUES (1, 'SQL batch executed locally');
