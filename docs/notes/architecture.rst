=============================
Scheduler Architecture Update
=============================

:Date: 2014-3-24
:Author: Jonathan Inloes
:Version: 0.1
:Updated: 2014-03-24

Purpose
-------

To provide a better architecture for guaranteed execution of a job

Problem
-------

The initial design of the scheduler queued jobs in distributed delay queue using Zookeeper.
This was a major issue because the queue would be lost if Zookeeper was restarted.

Solution
--------

In short, the system needs to be separated into an executor and storage tier.
The executor tier will be backed by Zookeeper for guaranteed execution
(unless complete Zookeeper ensemble failure), and the storage will be backed
by Cassandra to guarantee data will be highly available (unless complete Cassandra data
center failure).

Job data should be stored in Cassandra. Cassandra will provide guaranteed storage for our jobs in
the sense that if a Cassandra cluster goes down, the data will still be available on restart or
through another data center.

Jobs will be queued using a `leader elected`_ scheduling node that will poll Cassandra for jobs
waiting to be executed. If one of the leader elected nodes fails, then the Curator recipe will
select another node to run the polling task. The Curator leader election recipe will guarantee that
there will always be a node selecting jobs to execute.

When a job is ready to be executed, the job will be read and queued up in a
queue_ managed by Zookeeper. The Zookeeper managed queue_ will guarantee a job will be executed
because the Curator recipe will re add a job to the queue_ if a scheduler node fails in the queue_
consumer's consume method.

Typical Flow
------------

#. API request to schedule migration is made
#. Job request is made and sent as a POST to the scheduler
#. Scheduler receives the job and saves it in Cassandra
#. Scheduler `leader elected`_ task finds a job to execute and adds the job to the queue_
#. Scheduler queue_ consumer receives the job and executes the job


Fault Tolerance
---------------

- Cassandra data center crashes
    - The scheduler would switch to one of the other Cassandra data centers to queue up jobs
    - Data is also preserved if ALL the centers were to simultaneously crash
    - In complete failure, jobs might not be updated to a finished state while executing, that is, a job might execute but there would be no way to update it's state in Cassandra
- A scheduler node crashes
    - A new node wil be `leader elected`_ and take control of job selection tasks
    - Nodes that crash while executing a job would have the job reassigned to another node
- Zookeeper node crashes
    - Scheduler would failover to another working Zookeeper node
    - There would be no data loss in complete failure since the data is stored in Cassandra
    - Current executing jobs would finish normally, however no new jobs could be executed in complete failure

.. _leader elected: http://curator.apache.org/curator-recipes/leader-election.html
.. _queue: http://curator.apache.org/curator-recipes/distributed-queue.html
