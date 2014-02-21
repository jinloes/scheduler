=========
Scheduler
=========

:Date: 2014-1-29
:Author: Jonathan Inloes
:Version: 0.1
:Updated: 2014-02-20

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

How to run
----------

java -jar scheduler.jar -D<**System Property**>=<value>

Configuration
^^^^^^^^^^^^^

The following system options can be specified

================================== ================================================= ==============
Parameter                          Description                                       Default
================================== ================================================= ==============
zookeeper.connect_url              Comma separated list of Zookeeper host:port pairs localhost:2181
zookeeper.wait_time_ms             Milliseconds to wait for Zookeeper connection     100
zookeeper.sleep_between_retries_ms Milliseconds to wait between connection attempts  5
================================== ================================================= ==============


REST API
--------

Schedule a job
^^^^^^^^^^^^^^
POST Request::

    POST /api/v1/jobs
    {
        "task": {
            "method": "POST",
            "uri": "http://www.myserver.com",
            "body": {
                ...
            },
            "response_code_ranges": [
                {
                    "start": 200,
                    "end": 300
                }
            ]
        },
        "schedule": "2014-01-24T12:28:27-08:00"
    }

POST Response::

    {
        "id": <uuid>,
        "link": "/api/v1/jobs/<uuid>"
    }

Request Params
^^^^^^^^^^^^^^

The following request params are supported by the POST /api/v1/jobs endpoint:

================================== ======== ===================================================================
Param Path                         Required Notes
================================== ======== ===================================================================
schedule                           Yes      `ISO 8601`_ datetime string or value 'now' that tells the scheduler
                                            when the job should be run.
task                               Yes      Task object that contains information about the job being
                                            executed.
task.method                        Yes      Request method to perform. ie. GET, PUT, POST, DELETE
task.uri                           Yes      Uri execute request upon. The scheme is required. Currently,
                                            only the http scheme is supported.
task.body                          Yes      Request body to send.
task.response_code_ranges          No       A list of expected response code ranges. If no value is provided,
                                            then no restriction will be placed on the response code.
task.response_code_ranges[*].start No       Inclusive expected response code range start. If no value is
                                            provided, then minimum integer value will be assumed. If **start**
                                            is greater than **end**, then **start* will be assumed to be the
                                            end.
task.response_code_ranges[*].end   No       Inclusive expected response code range end. If no value is
                                            provided, then maximum integer value will be assumed. If **end**
                                            is less than **start**, then **end** will be assumed to be the
                                            **start**.
================================== ======== ===================================================================

Validation Error Response
^^^^^^^^^^^^^^^^^^^^^^^^^
If validation fails on a job post, then the response will be::

    Code: 406
    {
        "errors": [
            {
                "field": <field name ie. "task.uri">,
                "message": <error message ie. "Invalid date.">
            }
        ]
    }

where **field name** is the name of the field that contained the error and **message** is the validation error message

Retrieve a job
^^^^^^^^^^^^^^

GET Request::

    GET /api/v1/jobs/{jobId}

GET Response::

    {
        "id": <uuid>,
        "task": {
            "method": "POST",
            "uri": "http://www.myserver.com",
            "body": {
                #Job request body
            },
            "response_code_ranges": [
                {
                    "start": 200,
                    "end": 300
                }
            ]
        },
        "schedule": "2014-01-24T12:28:27-08:00"
    }

Error Messages
^^^^^^^^^^^^^^

Error messages outside of validation will be returned in the following format::

    {
        "message": <error message>
    }

Common Response Codes
^^^^^^^^^^^^^^^^^^^^^
    * 201 - Job successfully queued
    * 406 - Request body validation failed, check the **errors** field for field/error message pair
    * 500 - Unexpected error has occurred, check the **message** field for error message

.. Links:

.. _Zookeeper: http://zookeeper.apache.org/
.. _ISO 8601: http://en.wikipedia.org/wiki/ISO_8601
