# SLF4J TOYS

*slf4j-toys* is a Java library that complements SLF4J with tools to promote **semantical logging**.

[![Build Status](https://github.com/useful-toys/slf4j-toys/actions/workflows/build.yml/badge.svg)](https://github.com/useful-toys/slf4j-toys/actions/workflows/build.yml) [![Codecov](https://codecov.io/gh/useful-toys/slf4j-toys/branch/master/graph/badge.svg)](https://codecov.io/gh/useful-toys/slf4j-toys) [![Qodana](https://github.com/useful-toys/slf4j-toys/actions/workflows/qodana_code_quality.yml/badge.svg)](https://github.com/useful-toys/slf4j-toys/actions/workflows/qodana_code_quality.yml)[![CodeQL](https://github.com/useful-toys/slf4j-toys/actions/workflows/github-code-scanning/codeql/badge.svg)](https://github.com/useful-toys/slf4j-toys/actions/workflows/github-code-scanning/codeql)

Instead of relying on ambiguous log levels and cofusing log messages, the `Meter` defines a clear **life cycle** for operations,
with well defined output, with clear semantics. Log files will be clean and organized, allowing easy monitoring and failure investigation.
The log will nearly be self-explanatory and tell the story of your application.
Additionally, log files will be parsable, allowing automatic analysis and data mining.

## The Problem with Standard Logging

Standard logging practices using `logger.info()` or `logger.error()` often fail to answer a critical question: **"Did the operation succeed?"**

A log message like `logger.info("Finished processing user 'alice'")` is ambiguous. Did it succeed? Did it fail in an expected way? Was it slow? Answering these questions requires developers to follow a strict, custom logging pattern, which is difficult to establish and maintain across a team. This leads to inconsistent logs that are hard to parse and monitor.

## How slf4j-toys Solves It with Semantic Loggig

*slf4j-toys* fills this gap by providing a clear **Life Cycle** for every operation, provinding tools that create clear, structured, and machine-readable logs messages.

* **START**: The operation has just started.
* **OK**: The operation succeeded and achieved its primary goal.
* **OK (path)**: The operation succeeded achieved its primary goal, but through an alternative path (e.g., "update" instead of "insert", "cache hit" instead of "query"). This allows you to distinguish between different successful outcomes.
* **REJECT**: The operation did not succeed, terminated as expected but did not achieve its goal, as some businesses condition war not met (e.g., out of quota, cannot insert as user already exists, etc.).
* **FAIL**: The operation failed due to an unexpected technical error.

Messages will have a clear state within the life cycle and provide additional information about the operation.
Messages will be clear, consistent, and structured, enabling simple but effective observability without complex or expensive tools.

[Using the `Meter` for semantic logging](docs/Meter-usage.md)

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

 * [Using the `Meter` for semantic logging](docs/Meter-usage.md)
 * [Using the `Watcher` to monitor your application healt and resource usage](docs.md)
 * [Using the `Reporter` to generate reports about your host environment](docs/Reporter-usage.md)
 * [Using the `Logback Extensions` for semantig and colorful log messagens](docs/Logback-extensions.md)

## Similar Projects

*   [Perf4J](https://github.com/perf4j/perf4j): A performance logging library for Java.
*   [Speed4J](https://github.com/jalkanen/speed4j): A continuation and enhancement of Perf4J. See [comparison between slf4j-toys and Speed4J](docs/slf4j-toys-vs-speed4j.md).
