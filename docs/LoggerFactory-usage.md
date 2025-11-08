# LoggerFactory API Documentation

The `org.usefultoys.slf4j.LoggerFactory` is a utility class in *slf4j-toys* that serves as an enhanced alternative to the standard `org.slf4j.LoggerFactory`. It provides convenient methods for two primary purposes:
1.  Creating loggers with a consistent, hierarchical naming structure.
2.  Generating large, structured log messages (such as tables or formatted data) as a single log entry by adapting standard Java `OutputStream` and `PrintStream` objects to the SLF4J framework.

This allows for more organized logger management and better integration with libraries that write to streams.

## 1. Hierarchical Logger Creation

A common best practice is to structure logger names hierarchically, often mirroring the application's package structure (e.g., `com.mycompany.service.UserService`). This allows for fine-grained log level configuration (e.g., setting `com.mycompany.service` to `DEBUG` level).

The `LoggerFactory` provides several overloaded `getLogger()` methods to simplify the creation of these structured names.

### Use Case 1: Get Logger by Class or by Name

This is the most common and standard way to get a logger, named after the fully qualified class name or an arbitray name.

**Example:**
```java
import org.usefultoys.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class UserService {
    // Logger will be named "com.mycompany.service.UserService"
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    // As an alternative, you can also use a custom shorter name:
    private static final Logger logger2 = LoggerFactory.getLogger("service.UserService");

    public void someMethod() {
        logger.info("Executing someMethod in UserService.");
    }
}
```

### Use Case 2: Get Logger for a Specific Feature or Sub-component

Sometimes, you want to create a "child" logger nested under a class logger to control the logging for a specific feature more granularly.

**Example:**
Imagine you want to trace database operations within `UserService` separately.

```java
import org.usefultoys.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    // Logger will be named "com.mycompany.service.UserService.database"
    private static final Logger dbLogger = LoggerFactory.getLogger(UserService.class, "database");

    public void saveUser(User user) {
        logger.info("Attempting to save user: {}", user.getName());
        dbLogger.trace("Executing INSERT statement for user.");
        // ... database logic ...
    }
}
```
This allows you to, for instance, enable `TRACE` level only for `com.mycompany.service.UserService.database` without flooding the logs with traces from the rest of `UserService`.

### Use Case 3: Get Logger Nested Under an Existing Logger

This is similar to the previous use case but works with a `Logger` instance directly, which can be useful in helper methods or when the parent class is not directly available.

**Example:**
```java
public class DatabaseHelper {
    public void performQuery(Logger parentLogger, String query) {
        // If parentLogger is "com.mycompany.service.UserService",
        // this logger will be "com.mycompany.service.UserService.queries"
        Logger queryLogger = LoggerFactory.getLogger(parentLogger, "queries");
        queryLogger.trace("Executing query: {}", query);
        // ...
    }
}
```

## 2. Redirecting Streams to SLF4J

The primary motivation for this feature is to allow the creation of large, structured or complex log messages as a single, cohesive log entry. Instead of generating many small log messages, you can use a stream to build a detailed, multi-line message.

This is ideal for logging structured data, such as tables with aligned columns or comma-separated values (CSV). The resulting single log entry is easy to read and can be copied and pasted directly into other tools, like a spreadsheet, for more detailed analysis.

Using a `PrintStream` is particularly convenient as it allows the use of familiar and powerful methods like `print`, `println`, and especially `printf` for string formatting with alignment and various data types.

The factory provides `get[Level]PrintStream()` and `get[Level]OutputStream()` methods for all standard log levels.

### Use Case 1: Logging Tabular Data

Imagine you want to log a summary of processed records in a table format.

**Example:**
```java
import org.usefultoys.slf4j.LoggerFactory;
import org.slf4j.Logger;
import java.io.PrintStream;
import java.util.List;

public class ReportGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ReportGenerator.class);

    public void generateProcessingReport(List<Record> records) {
        try (PrintStream infoStream = LoggerFactory.getInfoPrintStream(logger)) {
            infoStream.println("Processing Report:");
            infoStream.println("---------------------------------");
            infoStream.printf("%-10s | %-8s | %s%n", "ID", "STATUS", "MESSAGE");
            infoStream.println("---------------------------------");
            for (Record record : records) {
                infoStream.printf("%-10s | %-8s | %s%n", record.getId(), record.getStatus(), record.getMessage());
            }
            infoStream.println("---------------------------------");
        }
        // The try-with-resources block ensures the stream is closed,
        // and the entire buffered content is logged as one message.
    }
}
```
This produces a clean, single log entry that is much easier to read than multiple `logger.info()` calls.

### Use Case 2: Integrating with Stream-Based Libraries

This functionality is also perfect for integrating with legacy code or third-party libraries that are designed to write to a `PrintStream`. Instead of letting them write to the console (`System.out`), you can capture their output directly into your application's logs.

**Example:**
Imagine you are using a third-party library that provides a debugging utility to dump the state of a complex object to a stream.

```java
// A hypothetical third-party library class
class ThirdPartyJsonDebugger {
    public void debug(PrintStream ps) {
        ps.println("{");
        ps.println("  \"status\": \"active\",");
        ps.println("  \"values\": [1, 2, 3]");
        ps.println("}");
    }
}

// Your application code
import org.usefultoys.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class MyService {
    private static final Logger logger = LoggerFactory.getLogger(MyService.class);
    private final ThirdPartyJsonDebugger jsonDebugger = new ThirdPartyJsonDebugger();

    public void doSomething() {
        logger.debug("About to perform a complex operation. Dumping object state for debugging:");

        // Get a PrintStream that writes to our logger at DEBUG level
        try (PrintStream debugStream = LoggerFactory.getDebugPrintStream(logger)) {
            // Pass the stream to the third-party library
            jsonDebugger.debug(debugStream);
        }
        // The entire JSON debug output is now in your logs as a single DEBUG message,
        // associated with the 'MyService' logger.
    }
}
```
This approach seamlessly integrates external, stream-based tools into your structured logging workflow, ensuring all diagnostic information is captured in one place.
