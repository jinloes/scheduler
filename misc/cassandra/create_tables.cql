DROP KEYSPACE IF EXISTS scheduler;

CREATE KEYSPACE scheduler WITH REPLICATION = {'class' : 'NetworkTopologyStrategy', 'datacenter1' : 3, 'datacenter2' : 3};

use scheduler;

CREATE TABLE job (
  id        UUID,
  task      TEXT,
  schedule  TIMESTAMP,
  status    TEXT,
  PRIMARY KEY (id));

CREATE INDEX SCHEDULE_IDX ON job (schedule);
CREATE INDEX STATUS_IDX ON job (status);