# Understanding the Meter Class Hierarchy

This document provides an architectural overview of the `Meter` class and the `MeterData` class that stores the data used in the `Meter` logic.

## Class Hierarchy

```
EventData (identity: sessionUuid, position, lastCurrentTime)
  └─ SystemData (JVM metrics: heap, GC, classloading, CPU)
       └─ MeterData (operation lifecycle data)
            └─ Meter (lifecycle logic + SLF4J logging)
```

- **`EventData`** — Base class providing identity fields: `sessionUuid`, `position` (sequential counter), and `lastCurrentTime` (timestamp).
- **`SystemData`** — Extends `EventData` with JVM and OS metrics: heap/non-heap memory, garbage collection, class loading, compilation time, CPU load.
- **`MeterData`** — Extends `SystemData` with all data fields specific to an operation's lifecycle, progress, outcome, and context.
- **`Meter`** — Extends `MeterData` with the behavioral logic for lifecycle management and SLF4J logger integration.

## `MeterData` — Operation Data Store

`MeterData` is a data transfer object (DTO) that extends `SystemData` and implements `MeterAnalysis`. It stores all raw data for a monitored operation.

### Operation Identity

| Field         | Type     | Description                                    |
|---------------|----------|------------------------------------------------|
| `category`    | `String` | Category derived from the logger name          |
| `operation`   | `String` | Name of the operation (may be `null`)          |
| `parent`      | `String` | Full ID of the parent operation (sub-operations) |
| `description` | `String` | Human-readable message describing the operation |

### Lifecycle Timestamps (in nanoseconds)

| Field        | Meaning                                           |
|--------------|---------------------------------------------------|
| `createTime` | When the Meter was created/scheduled               |
| `startTime`  | When the operation started (`start()`)             |
| `stopTime`   | When the operation terminated (`ok/reject/fail`)   |
| `timeLimit`  | Time threshold to flag the operation as "slow"     |

### Progress (Iterations)

| Field                | Meaning                              |
|----------------------|--------------------------------------|
| `currentIteration`   | Number of iterations completed       |
| `expectedIterations` | Total number of expected iterations  |

### Operation Outcome (Mutually Exclusive)

| Field         | Meaning                                                  |
|---------------|----------------------------------------------------------|
| `okPath`      | Success path identifier                                  |
| `rejectPath`  | Rejection reason (business rule)                         |
| `failPath`    | Failure reason (technical error, e.g., exception class)  |
| `failMessage` | Detailed failure message (e.g., exception message)       |

### Context

- `context` — `Map<String, String>` with arbitrary key-value pairs to enrich log messages.

### `MeterAnalysis` Interface (Default Methods)

This interface provides analytical logic computed over the raw data:

- **State queries:** `isStarted()`, `isStopped()`, `isOK()`, `isReject()`, `isFail()`
- **Computed durations:** `getExecutionTime()`, `getWaitingTime()`
- **Slowness detection:** `isSlow()` — checks whether execution time exceeds `timeLimit`
- **Progress rate:** `getIterationsPerSecond()`
- **Outcome path:** `getPath()` — returns the outcome path with precedence: fail > reject > ok

### Serialization

- `readableMessage()` — produces a human-readable string representation.
- `json5Message()` — produces a machine-parsable JSON5-encoded representation.
- `writeJson5()` / `readJson5()` — delegates to `MeterDataJson5` for JSON5 encoding/decoding.

### Unique ID

- `getFullID()` — returns a unique identifier combining `category`, `operation`, and `position` (e.g., `"com.example/doWork#42"`).

---

## `Meter` — Lifecycle Engine

`Meter` extends `MeterData` and adds all **behavioral logic** and **SLF4J integration**. It implements `MeterContext<Meter>`, `MeterExecutor<Meter>`, and `Closeable`.

### Loggers

| Logger          | Purpose                                    | Level  |
|-----------------|--------------------------------------------|--------|
| `messageLogger` | Human-readable messages                    | DEBUG/INFO/WARN/ERROR |
| `dataLogger`    | Machine-parsable data messages (JSON5)     | TRACE  |

Logger names are derived from the original logger name, decorated with prefixes/suffixes configured in `MeterConfig`.

### Lifecycle

```
[new Meter] → start() → progress()* → ok() / reject() / fail() / close()
```

| Method          | Log Level                 | Marker                          | When to Use                     |
|-----------------|---------------------------|---------------------------------|---------------------------------|
| `start()`       | DEBUG                     | `MSG_START`                     | Operation begins                |
| `progress()`    | INFO                      | `MSG_PROGRESS`                  | Intermediate progress (throttled) |
| `ok()` / `success()` | INFO (or WARN if slow) | `MSG_OK` / `MSG_SLOW_OK`       | Successful completion           |
| `reject(cause)` | INFO                      | `MSG_REJECT`                    | Expected rejection (business rule) |
| `fail(cause)`   | ERROR                     | `MSG_FAIL`                      | Unexpected technical failure    |
| `close()`       | ERROR                     | `MSG_FAIL` + path `"try-with-resources"` | Safety net via try-with-resources |

Each lifecycle event also emits a machine-parsable data message at TRACE level on `dataLogger`.

### Key Features

#### 1. Global Operation Counter

`EVENT_COUNTER` (`ConcurrentMap<String, AtomicLong>`) ensures a unique, time-ordered sequential position for each operation type. The key is `"category"` or `"category/operation"`.

#### 2. ThreadLocal Stack

Each `start()` pushes the Meter onto a `ThreadLocal<WeakReference<Meter>>` linked list, and each termination method restores the previous instance. `getCurrentInstance()` returns the active Meter for the current thread, or a dummy Meter if none is active.

#### 3. Progress Throttling

`progress()` only emits a log message if:
- The iteration count has advanced since the last progress report (`currentIteration > lastProgressIteration`), **and**
- The minimum period (`MeterConfig.progressPeriodMilliseconds`) has elapsed since the last report.

This prevents log flooding during tight loops.

#### 4. Slowness Detection

If `timeLimit > 0` and the execution time exceeds it, `ok()` logs at WARN level (with `MSG_SLOW_OK` marker) instead of INFO, and the data message uses `DATA_SLOW_OK` marker.

#### 5. Sub-operations

`sub(name)` creates a child `Meter` that:
- Inherits the parent's category
- Composes the operation name (e.g., `"parentOp/childOp"`)
- References the parent via `getFullID()`
- Copies the parent's context map

#### 6. Fluent Configuration

Chainable methods for configuring the Meter before `start()`:

- `m(msg)` — sets a human-readable description
- `limitMilliseconds(ms)` — sets a time limit for slowness detection
- `iterations(n)` — configures expected iteration count for progress tracking
- `ctx(key, value)` — adds context key-value pairs
- `path(id)` — sets the success path identifier

#### 7. Functional Execution (via `MeterExecutor`)

Methods that wrap a task with automatic lifecycle management:

- `run(Runnable)` — runs a task; marks `ok()` on success, `fail()` on exception
- `call(Callable)` — calls a task; stores result in context; marks `ok()` or `fail()`
- `safeCall(Callable)` — like `call()` but wraps checked exceptions in `RuntimeException`
- `runOrReject(Runnable, Class...)` — marks matching exceptions as `reject()` instead of `fail()`
- `callOrReject(Callable, Class...)` — same for callables
- `callOrRejectChecked(Callable)` — treats `RuntimeException` as fail; all other checked exceptions as reject

#### 8. Safety Nets

- **`close()`** (via `Closeable`) — if the Meter was not explicitly terminated before closing, it automatically marks the operation as `FAIL` with path `"try-with-resources"`. This ensures no operation goes untracked when used in a try-with-resources block.
- **`finalize()`** — detects Meters that are garbage-collected without being explicitly stopped, logging an error to indicate inconsistent API usage.

#### 9. Auto-correction

If `ok()`, `reject()`, or `fail()` are called without a prior `start()`, the `startTime` is automatically set to `stopTime`. This prevents errors but the operation duration will be reported as zero.

#### 10. Context Cleared After Log

`clearContext()` is called after each lifecycle event log emission, ensuring that context metadata is specific to the moment of emission (as per [TDR-0027](TDR-0027-context-as-event-metadata-and-post-emission-clearing.md)).

### Path Conversion

The static method `toPath(Object, boolean)` converts various object types into string path identifiers:
- `String` — used as-is
- `Enum` — uses `name()`
- `Throwable` — uses `getSimpleName()` or `getName()` depending on the flag
- Other objects — uses `toString()`

### Validation

All lifecycle transitions and argument checks are delegated to `MeterValidator`, which logs errors with appropriate markers (`INVALID_TRANSITION`, `INVALID_ARGUMENT`) without throwing exceptions, following the non-intrusive validation strategy described in [TDR-0017](TDR-0017-non-intrusive-validation-and-error-handling.md).

---

## Related Components

| Class/Interface    | Role                                                |
|--------------------|-----------------------------------------------------|
| `MeterData`        | Data store for operation lifecycle fields            |
| `MeterAnalysis`    | Analytical default methods over raw data             |
| `MeterContext`     | Context management (`ctx`/`unctx`) via default methods |
| `MeterExecutor`    | Functional execution wrappers via default methods    |
| `MeterConfig`      | Global configuration (log prefixes, progress period) |
| `MeterValidator`   | Non-intrusive lifecycle and argument validation      |
| `MeterDataFormatter` | Human-readable message formatting                 |
| `MeterDataJson5`   | JSON5 serialization/deserialization                  |
| `Markers`          | SLF4J Marker constants for all lifecycle events      |
| `SystemData`       | JVM/OS metric fields (parent of `MeterData`)         |
| `EventData`        | Base identity fields (parent of `SystemData`)        |
