LoggerFactory Extensions

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

# Use Case 2: Get Logger for a Specific Feature or Sub-component

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
