package com.rivermeadow.scheduler.dao;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.rivermeadow.scheduler.model.Job;

import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.joda.time.format.ISODateTimeFormat;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;

/**
 * Tests for {@link com.rivermeadow.scheduler.dao.JobDAO}.
 */
public class JobDAOTest {
    private static final String CASSANDRA_KEYSPACE = "job_dao_test";
    private static String CASSANDRA_HOST = "127.0.0.1";
    private static int CASSANDRA_PORT = 9142;
    private JobDAO jobDAO;
    private CQLDataLoader dataLoader;

    @BeforeSuite
    public void init() {
        try {
            EmbeddedCassandraServerHelper.startEmbeddedCassandra();
            System.setProperty("cassandra.hosts", CASSANDRA_HOST);
            System.setProperty("cassandra.port", Integer.toString(CASSANDRA_PORT));
            System.setProperty("cassandra.keyspace", CASSANDRA_KEYSPACE);
            dataLoader = new CQLDataLoader(CASSANDRA_HOST, CASSANDRA_PORT);
            dataLoader.load(new ClassPathCQLDataSet("cql/job_tables.cql", true, true,
                    CASSANDRA_KEYSPACE));
            jobDAO = new CassandraJobDAO(dataLoader.getSession(), new ObjectMapper());
        } catch (Exception e) {
            throw new RuntimeException("failed to start cassandra", e);
        }
    }

    @Test
    public void testGetJobsBeforeDate() {
        dataLoader.load(new ClassPathCQLDataSet("cql/get_jobs_before_date.cql", false, false));
        List<Job> jobs = jobDAO.getJobsBeforeDate(Job.Status.PENDING,
                ISODateTimeFormat.dateTimeNoMillis().parseDateTime("2014-04-10T06:08:48Z").toDate(),
                100);
        List<Job> expected = Lists.newArrayList(
                jobDAO.getById(UUID.fromString("4e463d81-c0d5-11e3-8a33-0800200c9a66")),
                jobDAO.getById(UUID.fromString("4e463d80-c0d5-11e3-8a33-0800200c9a66")),
                jobDAO.getById(UUID.fromString("4e463d82-c0d5-11e3-8a33-0800200c9a66")));
        assertThat(jobs, is(expected));
    }
}
