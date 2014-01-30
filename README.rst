=========
Scheduler
=========

:Date: 2014-1-29
:Author: Jonathan Inloes
:Version: 0.1
:Updated: 2014-01-30

Use Case
--------

We need a service that will schedule jobs to be executed at a provided date. The scheduler is
application agnostic in the sense that it does not know who is scheduling jobs. Currently,
the scheduler only supports running jobs that interact with a REST interface.

Architecture
------------

The system is designed to be fully distributed, highly available, and resilient to
failures. If a job is being executed and a scheduler node fails, then the job will be requeued to
be executed immediately by another scheduler node. The scheduler system is backed by a Zookeeper_
ensemble to provide the features previously described. The diagram below shows a typical workflow.

.. image:: docs/images/architecture.png
    :width: 400px
    :alt: scheduler architecture

REST API
^^^^^^^^^^

//TODO(jinloes) explain the requirements of each field is. required/optional, restrictions, etc
Queue a job to be run at the scheduled date::

    POST /api/v1/jobs

    Request:
    {
        "task": {
            "method": "POST",
            "uri": "http://www.myserver.com",
            "body": {
                ...
            },
            "expected_range": "200-300"
        },
        "schedule": "2014-01-24T12:28:27-08:00"
    }

    Response:
    {
        "id": <uuid>,
        "link": "/api/v1/jobs/<uuid>"
    }

Request Params::
    =================== ======== ==================================================================
    Param Path          Required Notes
    =================== ======== ==================================================================
    schedule            Yes      ISO8601 datetime string or value 'now' that tells the scheduler
                                 when the job should be run.
    =================== ======== ==================================================================
    task                Yes      Task object that contains information about the job being executed.
    =================== ======== ==================================================================
    task.method         Yes      Request method to perform. ie. GET, PUT, POST, DELETE
    =================== ======== ==================================================================
    task.uri            Yes      Uri execute request upon. The scheme is required. Currently,
                                 only the http scheme is supported.
    =================== ======== ==================================================================
    task.body           Yes      Request body to send.
    =================== ======== ==================================================================
    task.expected_range Yes      Expected request response code range. The bounds are inclusive.
    =================== ======== ==================================================================
Retrieve a job::

    GET /api/v1/jobs/{jobId}

    Response:
    {
        "id": <uuid>,
        "task": {
            "method": "POST",
            "uri": "http://www.myserver.com",
            "body": {
                ...
            },
            "expected_range": "200-300"
        },
        "schedule": "2014-01-24T12:28:27-08:00"
    }

.. Links:

.. _Zookeeper: http://zookeeper.apache.org/
