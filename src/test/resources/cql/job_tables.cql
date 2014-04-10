CREATE TABLE job (
  id        UUID,
  task      TEXT,
  schedule  TIMESTAMP,
  status    TEXT,
  PRIMARY KEY (id, status, schedule))
  WITH CLUSTERING ORDER BY (status asc, schedule asc);