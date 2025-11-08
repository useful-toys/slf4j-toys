# SLF4J TOYS

*slf4j-toys* is a Java library that complements SLF4J with tools to promote clear, consistent, and structured logging practices, enabling simple but effective observability without complex or expensive tools.

## The Problem with Standard Logging

Standard logging practices using `logger.info()` or `logger.error()` often fail to answer a critical question: **"Did the operation succeed?"**

A log message like `logger.info("Finished processing user 'alice'")` is ambiguous. Did it succeed? Did it fail in an expected way? Was it slow? Answering these questions requires developers to follow a strict, custom logging pattern, which is difficult to establish and maintain across a team. This leads to inconsistent logs that are hard to parse and monitor.

## How slf4j-toys Solves It

*slf4j-toys* fills this gap by providing tools that create clear, structured, and machine-readable logs.

### A `Meter` for Semantic Logging
The core component is the **`Meter`**, a log message builder designed to clearly communicate the outcome of an operation. Instead of relying on ambiguous log levels, the `Meter` provides an API to record the result with clear semantics:
*   **`m.ok()`**: The operation succeeded and achieved its primary goal.
*   **`m.ok(path)`**: The operation succeeded, but through an alternative path (e.g., "Update" instead of "Insert"). This allows you to distinguish between different successful outcomes.
*   **`m.reject()`**: The operation terminated as expected but did not achieve its goal (a predicted business-level failure, not a technical one).
*   **`m.fail()`**: The operation failed due to an unexpected technical error.

#### Beyond Status: Measurement and Classification
The `Meter` also provides powerful features for analysis:
*   **Automatic Measurement**: It automatically measures execution time and can flag operations that exceed a defined threshold, helping to spot performance issues.
*   **Operation Counting**: For each unique operation (e.g., `dao/saveUser`), the `Meter` maintains a counter (`position`). This allows you to know that a specific log entry is for the 1st, 2nd, or 100th execution of that operation.
*   **Unique Identification**: Every operation is given a unique identifier. This combination of a stable identifier and an incrementing counter enables you to easily classify, aggregate, and perform statistical analysis on your logs using simple tools, without needing a complex monitoring platform.

### Other Tools
*   A **`LoggerFactory`** that provides useful factory methods for creating hierarchical loggers and integrating stream-based output.
*   A **`Watcher`** that produces periodic memory and CPU status reports for simple, passive monitoring.
*   A **`Reporter`** that generates detailed diagnostic reports about the host environment on demand.

## Usage Example

This structured approach promotes clean and organized log files where the outcome of each operation is explicit:
```
[main] 25/11/2015 14:35:14 INFO dao - OK [Insert]: dao/saveUser; 0.1s; 1.5MB; login=alice; 
[main] 25/11/2015 14:36:35 INFO dao - OK [Update]: dao/saveUser; 0.1s; 1.5MB; login=bob; 
[main] 25/11/2015 14:37:27 INFO dao - REJECT [Concurrent]: dao/saveUser; 0.1s; 1.5MB; login=bob; 
[main] 25/11/2015 14:38:52 INFO dao - FAIL [OutOfQuota]: dao/saveUser; 0.1s; 1.5MB
```

This is achieved with a clear and concise API:
```java
final Meter m = MeterFactory.getMeter(LOGGER, "saveUser").start();
try {
    // Check if record exists in database   
    boolean exist = ...;
    if (exist) {
        m.ok("Insert"); // Success via the "Insert" path
    } else {
        m.ok("Update"); // Success via the alternative "Update" path
    }
} catch (OutOfQuotaException e) {
    m.reject(e); // An expected business failure
} catch (SQLException e) {
    m.fail(e); // An unexpected technical failure
    throw new IllegalStateException(e);
}
```

## Installation

*slf4j-toys* is available from the [Maven Central repository](https://search.maven.org/artifact/org.usefultoys/slf4j-toys/1.9.0/jar).

**Gradle:**
```gradle
dependencies {
    implementation("org.usefultoys:slf4j-toys:1.9.0")
}
```

**Maven:**
```xml
<dependency>
    <groupId>org.usefultoys</groupId>
    <artifactId>slf4j-toys</artifactId>
    <version>1.9.0</version>
</dependency>
```

## Requirements

*   **Java 8** or newer.

## Design Considerations

* No dependencies other than SLF4J itself.
* Small footprint.
* Takes advantage of the SLF4J API capabilities and Logback features.

## Further Information

 * [Using the `LoggerFactory` for additional useful Logger use cases](docs/LoggerFactory-usage.md)
 * [Using the `Watcher` to monitor your application healt and resource usage](docs.md)
 * [Using the `Reporter` to generate reports about your host environment](docs/Reporter-usage.md)
 * [Using the `Logback Extensions` for semantig and colorful log messagens](docs/Logback-extensions.md)

## Similar Projects

*   [Perf4J](https://github.com/perf4j/perf4j): A performance logging library for Java.
*   [Speed4J](https://github.com/jalkanen/speed4j): A continuation and enhancement of Perf4J. See [comparison between slf4j-toys and Speed4J](docs/slf4j-toys-vs-speed4j.md).
