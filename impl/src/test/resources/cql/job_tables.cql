CREATE TABLE job (
  id        UUID,
  task      TEXT,
  schedule  TIMESTAMP,
  status    TEXT,
  PRIMARY KEY (id));

CREATE INDEX SCHEDULE_IDX ON job (schedule);
CREATE INDEX STATUS_IDX ON job (status);