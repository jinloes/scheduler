# Scheduler

A distributed job scheduler built on Redis and Spring Boot 4.0.5.

## Git

Do not add `Co-Authored-By` trailers to commits or PRs.

## Architecture

- **Spring Boot 4.0.5** with Java 21
- **Gradle** (Groovy DSL) build system
- **Redis** for job storage, scheduling queue, and distributed locking

### Layer dependency rules

Dependencies flow strictly downward — lower layers must never import from higher ones:

```
web → service → repository → model
```

- `web` may import from `service` and `model`
- `service` may import from `repository` and `model`
- `repository` may import from `model`
- `model` has no internal imports

Violations (e.g. `service` importing from `web`) must be fixed by moving the shared type to a neutral layer or by having the higher layer pass primitives.

## Redis Data Model

| Key Pattern | Type | Purpose |
|---|---|---|
| `job:{uuid}` | Hash | Job fields: id, task (JSON), schedule, status, scheduledAt, createdAt |
| `jobs:pending` | Sorted Set | Pending jobs; score = scheduled time as epoch milliseconds |
| `jobs:execution` | List | Ready-to-run job IDs (FIFO queue consumed by executor threads) |
| `scheduler:locks:{lockName}` | String | `RedisLockRegistry` uses `scheduler:locks` as a key prefix; the full key is `scheduler:locks:scheduler:poll` |

## Job Lifecycle

```
PENDING → QUEUED → RUNNING → SUCCESS / ERROR
```

1. `POST /api/v1/jobs` saves job hash and adds ID to `jobs:pending` sorted set
2. `JobScheduler` polls every 5 s (holding a Redis distributed lock) for due jobs
3. Due jobs are atomically marked `QUEUED`, removed from `jobs:pending`, and pushed to `jobs:execution` via a single Lua script (up to `poll-batch-size` per poll)
4. `JobExecutor` worker threads (default 4) BLPOP from `jobs:execution` and make HTTP calls
5. Job status updated to `SUCCESS` or `ERROR` based on HTTP response code ranges; 4xx responses are non-retryable; 5xx and network errors are retried per `Task.maxRetries`

### Known limitations

- **No lock renewal**: `RedisLockRegistry` TTL is 30 s. If `processReadyJobs` takes longer than 30 s another instance can acquire the lock and duplicate work.

## API

### Schedule a Job

```
POST /api/v1/jobs
Content-Type: application/json

{
  "task": {
    "uri": "https://example.com/callback",
    "method": "POST",
    "body": { "key": "value" },
    "response_code_ranges": [{ "low": 200, "high": 299 }],
    "max_retries": 3,
    "retry_backoff_ms": 1000
  },
  "schedule": "2024-12-01T10:00:00Z"
}
```

`schedule` accepts ISO-8601 datetime strings or `"now"` for immediate execution.

**Response `201 Created`:**
```json
{ "id": "<uuid>", "link": "/api/v1/jobs/<uuid>" }
```

**Response `400 Bad Request`** (validation failure):
```json
{ "errors": ["task: must not be null", "schedule: must not be blank"] }
```

**Response `400 Bad Request`** (unparseable schedule):
```json
{ "error": "Invalid schedule format: use ISO-8601 (e.g. '2024-12-01T10:00:00Z') or 'now'" }
```

### Get Job Status

```
GET /api/v1/jobs/{jobId}
```

**Response `200 OK`:** full `Job` object including current `status`

**Response `404 Not Found`:** job ID does not exist (or TTL has expired)

## Configuration

| Property | Default | Constraints | Description |
|---|---|---|---|
| `REDIS_HOST` | `localhost` | — | Redis hostname |
| `REDIS_PORT` | `6379` | — | Redis port |
| `scheduler.poll-rate-ms` | `5000` | min 100 | Milliseconds between due-job polls (fixedDelay) |
| `scheduler.execution-threads` | `4` | min 1 | HTTP executor thread pool size |
| `scheduler.poll-batch-size` | `500` | min 1, max 10000 | Max due jobs fetched per poll cycle |

## Comments

Only add an inline comment when the **why** is non-obvious: a hidden constraint, a subtle invariant, a workaround, or behavior that would surprise a reader. Do not comment what the code does — well-named identifiers already do that.

## Javadoc

Add Javadoc to every `public` class and every `public` / `protected` method. Keep it concise — one sentence is enough when the name is already clear. Document:

- **What** the class/method is responsible for (not how it works internally)
- **Parameters** (`@param`) when their meaning or constraints aren't obvious from the name
- **Return value** (`@return`) when it isn't self-evident
- **Exceptions** (`@throws`) for checked exceptions and any unchecked exceptions callers should anticipate

Do **not** write Javadoc for private methods, trivial getters/setters, or anything already expressed by the signature.

## Lombok conventions

This project uses Lombok. Apply it consistently:

| Annotation | When to use |
|---|---|
| `@Slf4j` | Every class that logs — replaces `private static final Logger log = ...` |
| `@RequiredArgsConstructor` | Every class with constructor-injected `final` fields — replaces the explicit constructor |
| `@Getter` | Classes with many read-only fields (e.g. `Job`) — replaces individual getters |
| `@Setter` | Individual mutable fields only — avoid class-level `@Setter` |

Do **not** use `@Data` or `@Builder` — they generate `equals`/`hashCode`/`toString` that can cause issues with JPA proxies and circular references. Add those explicitly only if needed.

## Diagram

`docs/architecture.drawio` — importable into [Lucidchart](https://lucid.app) via **File → Import → Draw.io** or into [draw.io](https://app.diagrams.net) directly. Shows all components, Redis data structures, and data-flow edges.

**Keep this diagram in sync with every code change.** When adding or removing components, connections, or Redis keys, update the corresponding `mxCell` entries in `docs/architecture.drawio`. Cell IDs 2–20 are nodes; 100–109 are edges.

## Running Locally

```bash
# Start Redis
docker run -p 6379:6379 redis:7

# Run the app
./gradlew bootRun
```

## Building and Testing

```bash
./gradlew build
./gradlew test

# Apply Google Java Format
./gradlew spotlessApply
```

**Keep tests in sync with every code change.** When adding or modifying a class, update or add the corresponding test in `src/test/java/com/scheduler/`.

## Testing Conventions

- Every public class must have a matching test class; every public method must have a `@Nested` inner class named after it
- Test method names inside a `@Nested` class describe only the **scenario** — do not repeat the method name
- Target 100% line and branch coverage on all business logic; infrastructure code (thread pool lifecycle, `@PostConstruct`/`@PreDestroy`, `InterruptedException` handlers) may be excluded
- Use AssertJ `assertThat` for all assertions; avoid JUnit `assertTrue`/`assertEquals`
- Use `@ExtendWith(MockitoExtension.class)` with field-level `@Mock`; use `lenient()` in `@BeforeEach` only for shared infrastructure stubs that not all tests exercise

**Example structure:**

```java
class FooServiceTest {
  @Nested class DoSomething {
    @Test void succeeds_whenInputIsValid() { ... }
    @Test void throwsWhenInputIsNull() { ... }
  }

  @Nested class DoSomethingElse {
    @Test void returnsEmpty_whenNotFound() { ... }
  }
}
```

## Package Structure

```
src/main/java/com/scheduler/
├── SchedulerApplication.java        # Entry point; enables @Scheduled
├── config/
│   ├── RedisConfig.java             # RedisTemplate, LockRegistry, ObjectMapper, RestTemplate beans
│   └── SchedulerProperties.java     # Validated config properties (poll-rate-ms, execution-threads)
├── model/
│   ├── Job.java                     # Job entity (id, task, schedule, status, timestamps)
│   ├── Task.java                    # HTTP callback target (record)
│   ├── JobStatus.java               # PENDING / QUEUED / RUNNING / SUCCESS / ERROR
│   └── ResponseCodeRange.java       # Expected HTTP response code range (record)
├── repository/
│   └── JobRepository.java           # All Redis operations (hash, sorted set, list)
├── service/
│   ├── JobService.java
│   └── JobServiceImpl.java          # Schedule, retrieve, and process ready jobs
├── scheduler/
│   ├── JobScheduler.java            # @Scheduled poller with distributed Redis lock
│   ├── JobExecutor.java             # Thread pool blocking-polling the execution queue
│   ├── JobExecutionException.java   # Retryable failure during HTTP callback
│   └── NonRetryableJobException.java # Non-retryable failure (e.g. 4xx client errors)
└── web/
    ├── JobController.java            # POST /api/v1/jobs, GET /api/v1/jobs/{id}
    ├── JobRequest.java               # Request body for POST /api/v1/jobs
    ├── JobCreatedResponse.java       # Response body for 201 Created
    └── GlobalExceptionHandler.java   # Validation and error responses
```