package com.rivermeadow.scheduler.dao;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.beust.jcommander.internal.Lists;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rivermeadow.api.exception.MessageArgumentException;
import com.rivermeadow.api.model.Job;
import com.rivermeadow.api.util.ErrorCodes;
import com.rivermeadow.scheduler.model.JobImpl;
import com.rivermeadow.scheduler.model.TaskImpl;

import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Repository;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;

/**
 * Cassandra implementation of {@link JobDAO}.
 */
@Repository
public class CassandraJobDAO implements JobDAO {
    private static final String JOB_TABLE = "job";
    private static final String ID_COL = "id";
    private static final String SCHEDULE_COL = "schedule";
    private static final String STATUS_COL = "status";
    private static final String TASK_COL = "task";
    private final Session session;
    private final ObjectMapper objectMapper;
    private final RowConverter rowConverter = new RowConverter();

    @Autowired
    public CassandraJobDAO(Session session, ObjectMapper objectMapper) {
        this.session = session;
        this.objectMapper = objectMapper;
    }

    @Override
    public void saveJob(Job job) {
        try {
            session.execute(
                    insertInto(JOB_TABLE)
                            .value(ID_COL, job.getId())
                            .value(SCHEDULE_COL, job.getSchedule().toDate())
                            .value(STATUS_COL, job.getStatus().toString())
                                    //TODO(jinloes) does it make sense to save the task object a
                                    // a json
                                    // object or should i make a whole another table?
                            .value(TASK_COL, objectMapper.writeValueAsString(job.getTask())));
        } catch (JsonProcessingException e) {
            throw new MessageArgumentException(ErrorCodes.JOB_SAVE_FAILED, e);
        }
    }

    @Override
    public Job getJob(String id) {
        ResultSet rs = session.execute(
                select(ID_COL, STATUS_COL, TASK_COL, SCHEDULE_COL)
                        .from(JOB_TABLE)
                        .where(eq(ID_COL, UUID.fromString(id))));
        return rowConverter.convert(rs.one());
    }

    @Override
    public List<Job> getJobsBeforeNow() {
        List<Job> jobs = Lists.newArrayList();
        ResultSet rs = session.execute(
                select(ID_COL, STATUS_COL, TASK_COL, SCHEDULE_COL)
                        .from(JOB_TABLE)
                        .allowFiltering()
                        .where(lte(SCHEDULE_COL, new Date()))
                        .and(eq(STATUS_COL, Job.Status.PENDING.toString())));
        for (Row row : rs) {
            jobs.add(rowConverter.convert(row));
        }
        return jobs;
    }

    @Override
    public void updateJob(Job job) {
        try {
            session.execute(
                    update(JOB_TABLE)
                            .with(set(SCHEDULE_COL, job.getSchedule().toDate()))
                            .and(set(STATUS_COL, job.getStatus().toString()))
                            .and(set(TASK_COL, objectMapper.writeValueAsString(job.getTask())))
                            .where(eq(ID_COL, job.getId())));
        } catch (JsonProcessingException e) {
            throw new MessageArgumentException(ErrorCodes.JOB_UPDATE_FAILED, e, job.getId());
        }

    }

    @Override
    public void updateStatus(UUID jobId, Job.Status status) {
        session.execute(
                update(JOB_TABLE)
                        .with(set(STATUS_COL, status.toString()))
                        .where(eq(ID_COL, jobId)));
    }

    private final class RowConverter implements Converter<Row, Job> {
       @Override
        public Job convert(Row row) {
            if (row == null) {
                return null;
            }
           UUID jobId = row.getUUID(ID_COL);
            try {
                TaskImpl task = objectMapper.readValue(row.getString(TASK_COL), TaskImpl.class);
                Date date = row.getDate(SCHEDULE_COL);
                return new JobImpl(jobId, task,
                        ISODateTimeFormat.dateTimeNoMillis().print(date.getTime()),
                        Job.Status.parse(row.getString(STATUS_COL)));
            } catch (IOException e) {
                throw new MessageArgumentException(ErrorCodes.JOB_GET_FAILED, e,
                        jobId != null ? jobId.toString() : null);
            }
        }
    }
}
