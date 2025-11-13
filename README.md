# SLF4J TOYS

*slf4j-toys* is a Java library that complements SLF4J by promoting **semantic logging** to provide clear **lifecycle** status changes for application operations and runtime state.

[![Build Status](https://github.com/useful-toys/slf4j-toys/actions/workflows/build.yml/badge.svg)](https://github.com/useful-toys/slf4j-toys/actions/workflows/build.yml) [![Codecov](https://codecov.io/gh/useful-toys/slf4j-toys/branch/master/graph/badge.svg)](https://codecov.io/gh/useful-toys/slf4j-toys) [![Qodana](https://github.com/useful-toys/slf4j-toys/actions/workflows/qodana_code_quality.yml/badge.svg)](https://github.com/useful-toys/slf4j-toys/actions/workflows/qodana_code_quality.yml)[![CodeQL](https://github.com/useful-toys/slf4j-toys/actions/workflows/github-code-scanning/codeql/badge.svg)](https://github.com/useful-toys/slf4j-toys/actions/workflows/github-code-scanning/codeql)

## Overview

By adopting `slf4j-toys`, you gain:
*   **Reduced coding effort** for consistent logging.
*   **Clean, organized, and parsable log files** for easy monitoring, failure investigation, and data analysis.
*   **Detailed runtime and system reports** via the `Watcher` and `Reporter` components.

The library introduces:
*   **Meter**: For tracking the **lifecycle** of application operations (e.g., `OK`, `REJECT`, `FAIL`).
*   **Watcher**: For reporting the state of the Java runtime and underlying infrastructure.
*   **Reporter**: For generating diagnostic reports.

## How slf4j-toys Solves It with Semantic Logging

*slf4j-toys* fills the gap of ambiguous standard logging by providing a clear **Life Cycle** for every operation, offering tools that create clear, structured, and machine-readable log messages.

*   **START**: The operation has just started.
*   **OK**: The operation succeeded and achieved its primary goal.
*   **OK (path)**: The operation succeeded and achieved its primary goal, but through an alternative path (e.g., "update" instead of "insert", "cache hit" instead of "query"). This allows you to distinguish between different successful outcomes.
*   **REJECT**: The operation did not succeed, terminated as expected but did not achieve its goal, as some business condition was not met (e.g., out of quota, cannot insert as user already exists, etc.).
*   **FAIL**: The operation failed due to an unexpected technical error.

Messages will have a clear state within the life cycle and provide additional information about the operation. They will be clear, consistent, and structured, enabling simple but effective observability without complex or expensive tools.

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

## Further Information

For detailed concepts, usage guides, and advanced configurations, please refer to the [SLF4J-TOYS Wiki](https://github.com/useful-toys/slf4j-toys/wiki).
