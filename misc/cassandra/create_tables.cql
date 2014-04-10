DROP KEYSPACE IF EXISTS scheduler;

CREATE KEYSPACE scheduler WITH REPLICATION = {'class' : 'NetworkTopologyStrategy', 'datacenter1' : 3, 'datacenter2' : 3};

use scheduler;

/*
    Job table with id partition key. Useful for getting job by id.
*/
CREATE TABLE job (
    id        UUID PRIMARY KEY,
    task      TEXT,
    schedule  TIMESTAMP,
    status    TEXT);

/*
    Job table clustered by status and schedule. Allows faster querying of jobs by status and
    schedule, which is a common task.
*/
CREATE TABLE job_by_status_schedule (
    id        UUID,
    task      TEXT,
    schedule  TIMESTAMP,
    status    TEXT,
    PRIMARY KEY (status, schedule, id))
    WITH CLUSTERING ORDER BY (schedule ASC);