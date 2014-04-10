DROP KEYSPACE IF EXISTS scheduler;

CREATE KEYSPACE scheduler WITH REPLICATION = {'class' : 'NetworkTopologyStrategy', 'datacenter1' : 3, 'datacenter2' : 3};

use scheduler;

/*
    Job table with id partition key and clustered by status and schedule.
    This allows for querying by id, status, and schedule. However, allow filtering will have to be
    used to retrieve dates before a specific date.
*/
CREATE TABLE job (
  id        UUID,
  task      TEXT,
  schedule  TIMESTAMP,
  status    TEXT,
  PRIMARY KEY (id, status, schedule))
  WITH CLUSTERING ORDER BY (status asc, schedule asc);