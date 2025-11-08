# Meter API Documentation

The `Meter` is the flagship component of *slf4j-toys*, designed to bring semantic clarity and structured data to your
application logs. It goes beyond traditional log levels by explicitly tracking the outcome of operations, providing rich
context, and enabling statistical analysis without complex external monitoring tools.

## Design Philosophy: Semantic Logging for Operations

Traditional logging often falls short in conveying the true outcome of an application's operations. A generic `INFO`
message like "Executing user registration..." doesn't tell you if it was a success, a rejection due to business rules, or a
technical failure. Relying on developers to consistently follow custom logging patterns for outcomes is notoriously
difficult and leads to ambiguous, hard-to-parse logs.

The `Meter` addresses this by providing a structured API that forces clarity:

* **`OK`**: The operation succeeded as intended to achieve its goal. This is the expected outcome.
* **`REJECT`**: The operation terminated as expected, but did not achieve its goal (e.g., a business rule rejection).
  This is a *predicted* outcome, not a technical error.
* **`FAIL`**: The operation failed due to an *unexpected* technical error.

This semantic logging allows you to understand the "story" of each operation directly from your logs, making debugging,
monitoring, and auditing significantly easier.

The sequence of all logging messages tells you the story of what has happend while the application was running.

## Core Concepts

A `Meter` tracks the lifecycle of a single operation, from its creation to its final outcome.

### 1. Operation Lifecycle: `start()`, `ok()`, `reject()`, `fail()`

Every operation tracked by a `Meter` follows a simple lifecycle:

* **`MeterFactory.getMeter(...).start()`**: Marks the beginning of an operation, recording its start time.
* **`m.ok(...)`**: Marks a successful completion achieving the operation's goal.
* **`m.reject(...)`**: Marks a completion where the operation was rejected (e.g., invalid business preconditions).
* **`m.fail(...)`**: Marks a completion due to an unexpected error.

**Example:**

```java
final Meter m = MeterFactory.getMeter(LOGGER, "processOrder").start();
try {
        // ... business logic ...
        m.ok(); // Operation successful
} catch (ValidationException e){
        m.reject(e); // Business rule rejection
} catch(Exception e){
        m.fail(e); // Technical failure
}
```

### 2. Unique Identification and Counting

Each `Meter` instance is uniquely identified and tracked, enabling powerful correlation and statistical analysis:

* **`category`**: Derived from the logger name (e.g., `com.mycompany.service.OrderService`).
* **`operation`**: A specific name for the task (e.g., `"processOrder"`).
* **`sessionUuid`**: A unique ID for the application instance (inherited from `Session`), crucial for correlating logs
  across distributed systems.
* **`position`**: An atomic counter that increments for each execution of a specific `category/operation` pair. This
  allows you to know if a log entry is for the 1st, 5th, or 1000th execution of that operation within the application's
  lifetime.

This combination (`sessionUuid`, `category`, `operation`, `position`) provides a unique identifier for every single
operation execution, allowing for precise tracking and statistical analysis without needing complex external monitoring
tools.

### 3. Alternative Success Paths: `ok(path)`

Often, an operation can succeed in multiple, distinct ways. The `m.ok(path)` method allows you to differentiate these
successful outcomes.

**Example:**

```java
// ...
if (cacheHit) {
        m.ok("CacheHit"); // Success via cache
} else {
        m.ok("DatabaseLookup"); // Success via database
}
// ...
```

### 4. Performance Measurement and Thresholds: `limitMilliseconds()`

The `Meter` automatically measures the execution time of an operation. You can define a `timeLimit` to automatically
flag slow operations.

* **`m.limitMilliseconds(200)`**: Sets a warning threshold of 200ms. If the operation takes longer, its `OK` status will
  be logged as `OK (Slow)`.

### 5. Contextual Data: `ctx()`

You can attach arbitrary key-value pairs to a `Meter` using the `ctx()` methods. This enriches your log messages with
relevant business or technical context.

**Example:**

```java
m.ctx("userId",userId).ctx("orderId",orderId);
```

Just take care not to include sensitive data in the context, data that could be used to identify a specific user,
or memomry intensive data that will result in a large log messages.

### 6. Iterations and Progress: `iterations()`, `inc()`, `progress()`

For long-running operations that involve multiple steps or iterations, the `Meter` can track progress and report it
periodically.

* **`m.iterations(100)`**: Declares that the operation has 100 expected iterations.
* **`m.inc()` / `m.incBy(n)` / `m.incTo(n)`**: Increments the current iteration count.
* **`m.progress()`**: Logs a progress message periodically (controlled by `MeterConfig.progressPeriodMilliseconds`),
  showing current iteration, speed, and estimated time.

### 7. Automatic Cleanup with `try-with-resources`

The `Meter` implements `Closeable`, making it ideal for use in `try-with-resources` blocks. If an exception occurs and
the `Meter` hasn't been explicitly `ok()`, `reject()`, or `fail()`ed, the `close()` method will automatically mark it as
`fail()`, ensuring no operation goes untracked.

**Example:**

```java
try(Meter m = MeterFactory.getMeter(LOGGER, "criticalTask").start()){
        // ... some code ...
        m.ok(); // If this line is reached, it's OK
} // If an exception occurs before m.ok(), it will automatically be m.fail()
```

## Usage Examples

### Example 1: Basic Operation with Success and Failure

```java
import org.usefultoys.slf4j.meter.Meter;
import org.usefultoys.slf4j.meter.MeterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);

    public boolean processPayment(String userId, double amount) {
        final Meter m = MeterFactory.getMeter(LOGGER, "processPayment")
                .ctx("userId", userId)
                .ctx("amount", amount)
                .limitMilliseconds(500) // Warn if takes longer than 500ms
                .start();
        try {
            // Simulate payment processing
            Thread.sleep((long) (Math.random() * 1000)); // Random delay
            if (amount > 1000) {
                throw new IllegalArgumentException("Amount too high");
            }
            m.ok();
            return true;
        } catch (IllegalArgumentException e) {
            m.reject("InvalidAmount"); // Business rejection
            return false;
        } catch (InterruptedException e) {
            m.fail(e); // Technical failure
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            m.fail(e); // Generic technical failure
            return false;
        }
    }
}
```

### Example 2: Long-Running Operation with Progress Tracking

```java
import org.usefultoys.slf4j.meter.Meter;
import org.usefultoys.slf4j.meter.MeterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchProcessor.class);

    public void processLargeFile(int totalRecords) {
        final Meter m = MeterFactory.getMeter(LOGGER, "processFile")
                .iterations(totalRecords)
                .start();
        try {
            for (int i = 0; i < totalRecords; i++) {
                // Simulate processing a record
                Thread.sleep(10);
                m.inc(); // Increment processed count
                m.progress(); // Log progress periodically
            }
            m.ok();
        } catch (Exception e) {
            m.fail(e);
        }
    }
}
```

## Configuration (`MeterConfig`)

The `Meter`'s behavior is highly configurable via static fields in the `MeterConfig` class or, more commonly, via Java
System Properties at application startup.

| System Property                   | `MeterConfig` Field          | Default      | Description                                                                      |
|:----------------------------------|:-----------------------------|:-------------|:---------------------------------------------------------------------------------|
| `slf4jtoys.meter.message.prefix`  | `messagePrefix`              | `""` (empty) | Prefix for the logger name used for human-readable messages.                     |
| `slf4jtoys.meter.message.suffix`  | `messageSuffix`              | `""` (empty) | Suffix for the logger name used for human-readable messages.                     |
| `slf4jtoys.meter.data.prefix`     | `dataPrefix`                 | `""` (empty) | Prefix for the logger name used for encoded data messages.                       |
| `slf4jtoys.meter.data.suffix`     | `dataSuffix`                 | `""` (empty) | Suffix for the logger name used for encoded data messages.                       |
| `slf4jtoys.meter.progress.period` | `progressPeriodMilliseconds` | `2000` (2s)  | Minimum interval between progress messages. Supports units: `ms`, `s`, `m`, `h`. |
| `slf4jtoys.meter.print.category`  | `printCategory`              | `false`      | Include the logger category in readable messages.                                |
| `slf4jtoys.meter.print.status`    | `printStatus`                | `true`       | Include the operation status (`OK`, `FAIL`, etc.) in readable messages.          |
| `slf4jtoys.meter.print.position`  | `printPosition`              | `false`      | Include the operation's position (counter) in readable messages.                 |
| `slf4jtoys.meter.print.load`      | `printLoad`                  | `false`      | Include CPU load in readable messages.                                           |
| `slf4jtoys.meter.print.memory`    | `printMemory`                | `false`      | Include memory usage in readable messages.                                       |

**Example Configuration:**
To enable printing of category, position, and memory usage in `Meter` logs, and set a progress reporting period of 5
seconds:

```bash
java -Dslf4jtoys.meter.print.category=true \
     -Dslf4jtoys.meter.print.position=true \
     -Dslf4jtoys.meter.print.memory=true \
     -Dslf4jtoys.meter.progress.period=5s \
     -jar my-application.jar
```

## Logging Behavior

The `Meter` leverages SLF4J `Markers` and configurable logger names to provide flexible logging.

### Logger Naming

When you create a `Meter` (e.g., `MeterFactory.getMeter(LOGGER, "myOperation")`), it uses the provided `LOGGER`'s name
as its base category. You can then apply prefixes and suffixes to direct human-readable messages (logged at `DEBUG`/
`INFO`/`WARN`/`ERROR` levels) and encoded data messages (logged at `TRACE` level) to different loggers.

* **Human-readable messages**: Go to `[messagePrefix][baseLoggerName][messageSuffix]`
* **Encoded data messages**: Go to `[dataPrefix][baseLoggerName][dataSuffix]`

These prefixes/suffixes are configured via `MeterConfig` properties (e.g., `slf4jtoys.meter.message.prefix`,
`slf4jtoys.meter.data.prefix`).

### SLF4J Markers for Filtering

Every log message produced by the `Meter` includes a specific SLF4J `Marker`. These markers are crucial for advanced
filtering and routing in logging frameworks like Logback.

* **`METER_MSG_START`**: For `start()` messages.
* **`METER_MSG_OK`**: For `ok()` messages.
* **`METER_MSG_SLOW_OK`**: For `ok()` messages that exceeded the time limit.
* **`METER_MSG_REJECT`**: For `reject()` messages.
* **`METER_MSG_FAIL`**: For `fail()` messages.
* **`METER_MSG_PROGRESS`**: For `progress()` messages.
* **`METER_DATA_START`**, **`METER_DATA_OK`**, etc.: Corresponding markers for the `TRACE` level encoded data messages.

These markers, along with the custom Logback converters (`%status`, `%highlightStatus`, `%highlightMessage`), allow you
to create highly visual and filterable log outputs. For more details on using these markers and converters, refer to
the [Logback Extensions API Documentation](docs/LOGBACK_EXTENSIONS_API.md).
