=========
Scheduler
=========

:Date: 2014-1-29
:Author: Jonathan Inloes
:Version: 0.1
:Updated: 2014-01-29

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
ensemble to provide the features previously described. The diagram below

.. image:: docs/images/architecture.png
    :width: 400px
    :alt: scheduler architecture

REST API
^^^^^^^^^^

Queue a job to be run at the scheduled date::

    POST /jobs
    
    Request:
    {
        "task": {
            "method": "POST",
            "uri": "http://www.myserver.com",
            "body": {
                "user": "marco",
                "foo": "bar"
            },
            "expected_range": "200-300"
        },
        "schedule": "2014-01-24T12:28:27-08:00" #ISO8601 datetime or value 'now'
    }
    
    Response:
    {
        "id": <uuid>,
        "link": "/jobs/<uuid>"
    }

Retrieve a job::

    GET /jobs/{jobId}

    Response:
    {
        "id": <uuid>,
        "task": {
            "method": "POST",
            "uri": "http://www.myserver.com",
            "body": {
                "user": "marco",
                "foo": "bar"
            },
            "expected_range": "200-300"
        },
        "schedule": "2014-01-24T12:28:27-08:00"
    }

.. Links:

.. _Zookeeper: http://zookeeper.apache.org/
