CREATE TABLE job (
    id        UUID PRIMARY KEY,
    task      TEXT,
    schedule  TIMESTAMP,
    status    TEXT);

CREATE TABLE job_by_status_schedule (
    id        UUID,
    task      TEXT,
    schedule  TIMESTAMP,
    status    TEXT,
    PRIMARY KEY (status, schedule, id))
    WITH CLUSTERING ORDER BY (schedule ASC);