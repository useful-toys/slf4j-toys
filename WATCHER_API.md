# Watcher API

The `Watcher` is a `Runnable` task that, when executed, performs the following actions:
1.  **Collects Metrics**: It gathers data from various `java.lang.management.*MXBean` interfaces.
2.  **Formats Messages**: It creates a human-readable summary (`INFO` level) and a machine-parseable data string (`TRACE` level).
3.  **Logs via SLF4J**: It sends these messages to a configured SLF4J logger.

## Usage Patterns

There are several ways to use the `Watcher`, depending on your application's architecture.

### 1. `WatcherSingleton`: The Quick and Simple Approach

For simple, standalone applications, the `WatcherSingleton` is the most convenient method. It manages a shared, scheduled `Watcher` instance with minimal setup.

```java
// At application startup
WatcherSingleton.startDefaultWatcher();

// At application shutdown
WatcherSingleton.stopDefaultWatcher();
```

The singleton can use either a `java.util.Timer` or a `java.util.concurrent.ScheduledExecutorService` to run the task. The `Executor` is generally preferred in modern applications due to better error handling and thread management. You can specify which to use when starting the watcher:

```java
// Preferred method, using a modern Executor
WatcherSingleton.startDefaultWatcher(true); // true for Executor, false for Timer

// The default is Timer for backward compatibility
WatcherSingleton.startDefaultWatcher(); // Uses Timer
```

> **Note**: While `WatcherSingleton` is excellent for simple cases, it is **not recommended for container-managed environments** like Java EE or Spring. In those cases, it's better to let the container manage the lifecycle of scheduled tasks.

### 2. Manual Instantiation

For full control, you can instantiate and manage the `Watcher` yourself. This is the foundation for framework integration.

```java
// Create a Watcher that logs to the "myApp.health" logger
Watcher myWatcher = new Watcher("myApp.health");

// Run it once manually
myWatcher.run();
```

## Integration with Modern Frameworks

In enterprise and microservice environments, task scheduling should be handled by the framework to ensure proper lifecycle management, thread pooling, and context propagation.

### Java EE / Jakarta EE

In a Java EE or Jakarta EE environment (like WildFly, Open Liberty, or Payara), the best practice is to use an EJB Timer. Create a startup singleton bean that injects and runs the `Watcher` on a schedule.

```java
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Schedule;
import javax.annotation.PostConstruct;
import org.usefultoys.slf4j.watcher.Watcher;

@Singleton
@Startup
public class AppHealthMonitor {

    private Watcher watcher;

    @PostConstruct
    public void init() {
        // Instantiate the watcher, optionally configure it via system properties
        this.watcher = new Watcher("app-health");
    }

    @Schedule(hour = "*", minute = "*/10", second = "0", persistent = false)
    public void runHealthCheck() {
        // Let the container execute the task
        watcher.run();
    }
}
```

### Spring Boot

In a Spring Boot application, the recommended approach is to use the built-in scheduling capabilities. Define a `@Component` and use the `@Scheduled` annotation on a method that runs the `Watcher`.

First, enable scheduling in your main application class:
```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

Then, create the scheduled component:
```java
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import org.usefultoys.slf4j.watcher.Watcher;

@Component
public class AppHealthMonitor {

    private final Watcher watcher = new Watcher("app-health");

    @Scheduled(fixedRateString = "${monitoring.health.check.rate:600000}") // 10 minutes default
    public void runHealthCheck() {
        watcher.run();
    }
}
```
You can make the rate configurable in your `application.properties` file:
```properties
# Rate in milliseconds for the health check
monitoring.health.check.rate=300000 # 5 minutes
```

## On-Demand Monitoring with `WatcherServlet`

For web applications, `slf4j-toys` provides the `WatcherServlet`, a simple way to trigger a health check on-demand via an HTTP GET request. This is useful for:
*   **Manual Checks**: An administrator can access the URL to get an immediate snapshot of the application's health.
*   **Automated Probes**: An external monitoring system (like a load balancer or a health checker) can periodically call the URL.

When the servlet's URL is accessed, it simply runs the default `Watcher` instance, causing the current system status to be printed to the logs.

### How to Configure

You can register the servlet in your application's `web.xml` deployment descriptor.

```xml
<servlet>
    <servlet-name>WatcherServlet</servlet-name>
    <servlet-class>org.usefultoys.slf4j.watcher.WatcherServlet</servlet-class>
</servlet>
<servlet-mapping>
    <servlet-name>WatcherServlet</servlet-name>
    <url-pattern>/admin/health-check</url-pattern>
</servlet-mapping>
```

### :warning: Security Warning

**It is critical to protect this endpoint.** Exposing an application's internal status to the public can be a security risk. Ensure that only authorized users or systems can access this URL.

You should protect this servlet using the security mechanisms of your framework or application server, for example, by:
*   Requiring a specific user role (`<security-constraint>`).
*   Restricting access to specific IP addresses.
*   Placing it behind a firewall or API gateway that handles authentication and authorization.

## Collected Data

The `Watcher` collects a wide range of metrics, which can be enabled or disabled via configuration.

### Default Metrics
By default, the `Watcher` only collects basic runtime memory information.

*   **Log Example**: `INFO myApp.health - Memory: 4.1MB 129.0MB 1.9GB`
*   **Format**: `Memory: [used] [total] [max]`

### Extended Metrics (via `SystemConfig`)
By enabling properties in `SystemConfig`, you can collect much more detailed data.

| Metric | `MXBean` Source | `SystemConfig` Property | Description |
| :--- | :--- | :--- | :--- |
| **System Load** | `OperatingSystemMXBean` | `usePlatformManagedBean` | CPU load of the whole system. |
| **Heap Memory** | `MemoryMXBean` | `useMemoryManagedBean` | Detailed Heap memory usage (used, committed, max). |
| **Non-Heap Memory** | `MemoryMXBean` | `useMemoryManagedBean` | Detailed Non-Heap memory usage. |
| **Pending Finalization**| `MemoryMXBean` | `useMemoryManagedBean` | Number of objects waiting for finalization. |
| **Class Loading** | `ClassLoadingMXBean` | `useClassLoadingManagedBean`| Number of loaded, unloaded, and total classes. |
| **JIT Compilation** | `CompilationMXBean` | `useCompilationManagedBean`| Total time spent in JIT compilation. |
| **Garbage Collection**| `GarbageCollectorMXBean`| `useGarbageCollectionManagedBean`| Total collection count and time across all GCs. |

The `TRACE` level log contains all enabled metrics in a compact, key-value format.

## Configuration

The `Watcher`'s behavior is highly configurable via Java System Properties. These should be set at application startup (e.g., using the `-D` flag).

### `WatcherConfig` Properties
These properties control the `WatcherSingleton` and logger naming.

| Property | Default Value | Description |
| :--- | :--- | :--- |
| `slf4jtoys.watcher.name` | `watcher` | The base logger name for the default watcher. |
| `slf4jtoys.watcher.delay` | `60000` (1 min) | Initial delay before the first run. Supports units: `ms`, `s`, `m`, `h`. |
| `slf4jtoys.watcher.period` | `600000` (10 min)| Interval between subsequent runs. Supports units. |
| `slf4jtoys.watcher.data.enabled`| `false` | Set to `true` to enable the `TRACE` level data logs. |
| `slf4jtoys.watcher.message.prefix`| `""` (empty) | A prefix added to the logger name for `INFO` messages. |
| `slf4jtoys.watcher.message.suffix`| `""` (empty) | A suffix added to the logger name for `INFO` messages. |
| `slf4jtoys.watcher.data.prefix` | `""` (empty) | A prefix added to the logger name for `TRACE` messages. |
| `slf4jtoys.watcher.data.suffix` | `""` (empty) | A suffix added to the logger name for `TRACE` messages. |

### `SystemConfig` Properties
These boolean properties control which metrics are collected. They are `false` by default to minimize overhead. Set them to `true` to enable collection.

| Property | Default | Description |
| :--- | :--- | :--- |
| `slf4jtoys.usePlatformManagedBean` | `false` | Enables collection of system CPU load. |
| `slf4jtoys.useMemoryManagedBean` | `false` | Enables detailed Heap/Non-Heap memory metrics. |
| `slf4jtoys.useClassLoadingManagedBean`| `false` | Enables class loading metrics. |
| `slf4jtoys.useCompilationManagedBean` | `false` | Enables JIT compilation time metrics. |
| `slf4jtoys.useGarbageCollectionManagedBean`| `false` | Enables garbage collection metrics. |

### Example Configuration

To run the default watcher every 5 minutes, collecting CPU and detailed memory metrics, and sending data logs to a separate logger, you would start your application with these flags:

```bash
java -Dslf4jtoys.watcher.period=5m \
     -Dslf4jtoys.watcher.data.enabled=true \
     -Dslf4jtoys.watcher.data.prefix=data. \
     -Dslf4jtoys.usePlatformManagedBean=true \
     -Dslf4jtoys.useMemoryManagedBean=true \
     -jar my-application.jar
```

This configuration would result in:
*   **INFO logs** going to `watcher`.
*   **TRACE logs** going to `data.watcher`.

## Logging Behavior

The `Watcher` provides strong control over how and where its logs are written.

### Logger Naming
When you create a `Watcher`, you provide a base name for its logger (e.g., `"app-health"`).
*   The `WatcherSingleton` uses a default name of `"watcher"`, which can be overridden by the `slf4jtoys.watcher.name` system property.

You can further customize this by adding prefixes and suffixes, allowing you to direct human-readable (`INFO`) and data (`TRACE`) messages to different loggers. This is configured via system properties like `slf4jtoys.watcher.message.prefix` and `slf4jtoys.watcher.data.prefix`.

### SLF4J Markers for Filtering
Every log message produced by the `Watcher` includes an SLF4J `Marker`. This enables advanced filtering in logging frameworks like Logback.
*   **`WATCHER`**: This marker is attached to the human-readable `INFO` messages.
*   **`WATCHER_DATA`**: This marker is attached to the machine-parseable `TRACE` messages.

### Advanced Logback Configuration Examples

Here are two methods to separate the machine-parseable data logs (`TRACE` level) into a dedicated file, which is useful for automated processing.

**Method 1: Filtering by Marker**

This approach uses Logback's `MarkerFilter` to identify data logs. It works regardless of the logger's name and is very precise.

First, ensure data logging is enabled via system property:
`-Dslf4jtoys.watcher.data.enabled=true`

Then, configure your `logback.xml`:
```xml
<configuration>
    <!-- Appender for WATCHER_DATA logs -->
    <appender name="WATCHER_DATA_FILE" class="ch.qos.logback.core.FileAppender">
        <file>watcher-data.log</file>
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} %msg%n</pattern>
        </encoder>
        <!-- Filter to accept only logs with the WATCHER_DATA marker -->
        <filter class="ch.qos.logback.classic.filter.MarkerFilter">
            <marker>WATCHER_DATA</marker>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
    </appender>

    <!-- Appender for all other application logs -->
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d %-5level [%thread] %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="TRACE">
        <appender-ref ref="STDOUT" />
        <appender-ref ref="WATCHER_DATA_FILE" />
    </root>
</configuration>
```

**Method 2: Filtering by Logger Name Prefix**

This approach uses the `WatcherConfig` properties to add a specific prefix to the data logger's name. You then configure Logback to capture all logs under that name.

First, configure the prefix and enable data logging via system properties:
`-Dslf4jtoys.watcher.data.enabled=true`
`-Dslf4jtoys.watcher.data.prefix=data.`

This will cause data logs from a watcher named `my-app` to be sent to a logger named `data.my-app`.

Then, configure your `logback.xml` to redirect logs from any logger starting with `data.` to a specific file.
```xml
<configuration>
    <!-- Appender for data logs -->
    <appender name="DATA_FILE" class="ch.qos.logback.core.FileAppender">
        <file>data.log</file>
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} %msg%n</pattern>
        </encoder>
    </appender>

    <!-- Appender for all other application logs -->
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d %-5level [%thread] %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!--
      Define a logger for the 'data' namespace.
      It will send logs to DATA_FILE and, with additivity=false,
      prevent them from also going to the root logger's appenders.
    -->
    <logger name="data" level="TRACE" additivity="false">
        <appender-ref ref="DATA_FILE" />
    </logger>

    <root level="INFO">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```
