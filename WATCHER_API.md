# Watcher API Documentation

The `Watcher` is a minimalist, embedded monitoring solution within the *slf4j-toys* library. It is designed for
applications that need simple, effective, and inexpensive application health monitoring without resorting to external
solutions that are often complex, commercial, or expensive. By periodically collecting system metrics and logging them
through SLF4J, the `Watcher` provides an easy way to observe an application's runtime behavior directly from its logs.

## Core Concepts

The `Watcher` is a `Runnable` task that, when executed, **collects Metrics** and prints a **human-readable summary**
(`INFO` level and `MSG_WATCHER` slf4j marker) and a **machine-parseable data** string (`TRACE` level and `DATA_WATCHER`
slf4j marker) sent to a configured SLF4J logger.

## Collected Data

By default, the `Watcher` collects basic runtime memory information, which includes used, total, and max memory.

* **Log Example**: `INFO myApp.health - Memory: 4.1MB 129.0MB 1.9GB`

It is possible to enable a much wider range of metrics, including CPU load, garbage collection, and detailed memory pool
stats. These are disabled by default to minimize overhead but can be activated via `SystemConfig` properties.

For a full list of available metrics and how to enable them, please see the **`SystemConfig` Properties** section under
**Configuration**.

### Correlation and Ordering

In addition to performance metrics, every `Watcher` log event may contain two important fields for tracking and
analysis:

* **`sessionUuid`**: A unique identifier generated once per JDK instance. This is essential in distributed
  environments (like microservices or clustered applications) where logs from multiple instances are aggregated. You can
  use this UUID to filter logs and analyze the behavior of a specific JDK instance.

* **`position`**: A simple counter (`long`) that starts at 1 and increments for each `Watcher` event within a given
  session. This allows you to reliably order log messages chronologically, even if log collection systems receive them
  out of order.

These fields are crucial for making sense of logs in a complex, multi-instance environment.

## Usage Patterns

There are several ways to use the `Watcher`, depending on your application's architecture.

### Use case 1: `WatcherSingleton`: The Quick and Simple Approach

For simple, standalone applications, the `WatcherSingleton` is the most convenient method. It manages a shared,
scheduled `Watcher` instance with minimal setup.

The singleton provides two mechanisms for scheduling:

* **Executor-based (Recommended)**: Uses a `java.util.concurrent.ScheduledExecutorService`. This is the modern and
  preferred approach due to better error handling and thread management.
* **Timer-based**: Uses the legacy `java.util.Timer`.

**Using the Executor (Recommended):**

```java
import org.usefultoys.slf4j.watcher.WatcherSingleton;

// At application startup
WatcherSingleton.startDefaultWatcherExecutor();

// At application shutdown
WatcherSingleton.

stopDefaultWatcherExecutor();
```

**Using the Timer (Legacy):**

```java
import org.usefultoys.slf4j.watcher.WatcherSingleton;

// At application startup
WatcherSingleton.startDefaultWatcherTimer();

// At application shutdown
WatcherSingleton.

stopDefaultWatcherTimer();
```

> **Note**: While `WatcherSingleton` is excellent for simple cases, it is **not recommended for container-managed
environments** like Java EE or Spring. In those cases, it's better to let the container manage the lifecycle of
> scheduled tasks.

### Use case 2: Manual Instantiation

For full control, you can instantiate and manage the `Watcher` yourself. This is the foundation for framework
integration.

```java
// Create a Watcher that logs to the "myApp.health" logger
Watcher myWatcher = new Watcher("myApp.health");

// Run it once manually
myWatcher.

run();
```

### Use case 3: Integration with Modern Frameworks

In enterprise and microservice environments, task scheduling should be handled by the framework to ensure proper
lifecycle management, thread pooling, and context propagation.

#### Java EE / Jakarta EE

In a Java EE or Jakarta EE environment (like WildFly, Open Liberty, or Payara), the best practice is to use an EJB
Timer. Create a startup singleton bean that injects and runs the `Watcher` on a schedule.

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

#### Spring Boot

In a Spring Boot application, the recommended approach is to use the built-in scheduling capabilities. Define a
`@Component` and use the `@Scheduled` annotation on a method that runs the `Watcher`.

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

### Use case 4: On-Demand Monitoring with `WatcherServlet`

For web applications, `slf4j-toys` provides the `WatcherServlet`, a simple way to trigger a health check on-demand via
an HTTP GET request. This is useful for:

* **Manual Checks**: An administrator can access the URL to get an immediate snapshot of the application's health.
* **Automated Probes**: An external monitoring system (like a load balancer or a health checker) can periodically call
  the URL.

When the servlet's URL is accessed, it simply runs the default `Watcher` instance, causing the current system status to
be printed to the logs.

#### How to Configure

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

#### :Warning: Security Warning

**It is critical to protect this endpoint.** Exposing an application's internal status to the public can be a security
risk. Ensure that only authorized users or systems can access this URL.

You should protect this servlet using the security mechanisms of your framework or application server, for example, by:

* Requiring a specific user role (`<security-constraint>`).
* Restricting access to specific IP addresses.
* Placing it behind a firewall or API gateway that handles authentication and authorization.

## Configuration

The `Watcher`'s behavior is highly configurable via Java System Properties. These should be set at application startup (
e.g., using the `-D` flag).
Alternatively, you can use global (statick) attributes in the `WatcherConfig` and `SystemConfig`
classes to configure the `Watcher` programmatically as soon as your application starts.

### `WatcherConfig` Properties

These properties control the `WatcherSingleton` and logger naming.

| Property                           | Default Value     | Description                                                                         |
|:-----------------------------------|:------------------|:------------------------------------------------------------------------------------|
| `slf4jtoys.watcher.name`           | `watcher`         | The base logger name for the default watcher used by `WatcherSingleton`             |
| `slf4jtoys.watcher.delay`          | `60000` (1 min)   | Initial delay before the first run. Supports units: `ms`, `s`, `m`, `h`.            |
| `slf4jtoys.watcher.period`         | `600000` (10 min) | Interval between subsequent runs. Supports units.                                   |
| `slf4jtoys.watcher.data.enabled`   | `false`           | Set to `true` to enable the `TRACE` level data logs with machine-parseable data.    |
| `slf4jtoys.watcher.message.prefix` | `""` (empty)      | A prefix added to the logger name for `INFO` messages human-readable summary.       |
| `slf4jtoys.watcher.message.suffix` | `""` (empty)      | A suffix added to the logger name for `INFO` messages human-readable summary.       
| `slf4jtoys.watcher.data.prefix`    | `""` (empty)      | A prefix added to the logger name for `TRACE` messages with machine-parseable data. |
| `slf4jtoys.watcher.data.suffix`    | `""` (empty)      | A suffix added to the logger name for `TRACE` messages with machine-parseable data. |

You may set message or data prefix and suffix in order to apply specific logger configution to the human-readable
summary and machine-parseable data logs, handling the case where you want to send them to different loggers.

### `SystemConfig` Properties

These boolean properties control which metrics are collected from the JVM's `MXBean`s. They are `false` by default to
minimize overhead. Set them to `true` to enable collection. Not all metrics are available on all platforms.

| Property                                    | Default | Description                                                         |
|:--------------------------------------------|:--------|:--------------------------------------------------------------------|
| `slf4jtoys.usePlatformManagedBean`          | `false` | Enables collection of system CPU load from `OperatingSystemMXBean`. |
| `slf4jtoys.useMemoryManagedBean`            | `false` | Enables detailed Heap/Non-Heap memory metrics from `MemoryMXBean`.  |
| `slf4jtoys.useClassLoadingManagedBean`      | `false` | Enables class loading metrics from `ClassLoadingMXBean`.            |
| `slf4jtoys.useCompilationManagedBean`       | `false` | Enables JIT compilation time metrics from `CompilationMXBean`.      |
| `slf4jtoys.useGarbageCollectionManagedBean` | `false` | Enables garbage collection metrics from `GarbageCollectorMXBean`.   |

### Example Configuration

To run the default watcher every 5 minutes, collecting CPU and detailed memory metrics, and sending machine-parseable
data logs to a
separate logger, you would start your application with these flags:

```bash
java -Dslf4jtoys.watcher.period=5m \
     -Dslf4jtoys.watcher.data.enabled=true \
     -Dslf4jtoys.watcher.data.prefix=data. \
     -Dslf4jtoys.usePlatformManagedBean=true \
     -Dslf4jtoys.useMemoryManagedBean=true \
     -jar my-application.jar
```

This configuration would result in:

* **INFO logs** going to `watcher`.
* **TRACE logs** going to `data.watcher` (to be handled by a dedicated logback appender writing to a specific file).

## Logging Behavior

The `Watcher` uses slf4j logging standards to provide strong control over how and where its logs are written.
Human-readable (`INFO`) and machine-parseable (`TRACE`) logs are produced by the `Watcher` and sent to a configured
logger. Additionally, human-readable logs are marked with a special `slf4j` marker (`MSG_WATCHER`) and machine-parseable
logs are marked with another special `slf4j` marker (`MSG_WATCHER_DATA`). This allows you to use SLF4J markers
to allow advanced filtering in logging frameworks like Logback.

### Logger Naming

When you create a `Watcher`, you provide a base name for its logger (e.g., `"app-health"`).

* The `WatcherSingleton` uses a default name of `"watcher"`, which can be overridden by the `slf4jtoys.watcher.name`
  system property.

You can further customize this by adding prefixes and suffixes, allowing you to direct human-readable (`INFO`) and
data (`TRACE`) messages to different loggers. This is configured via system properties like
`slf4jtoys.watcher.message.prefix` and `slf4jtoys.watcher.data.prefix`.

### SLF4J Markers for Filtering

Every log message produced by the `Watcher` includes an SLF4J `Marker`. This enables advanced filtering in logging
frameworks like Logback.

* **`MSG_WATCHER`**: This marker is attached to the human-readable `INFO` messages.
* **`DATA_WATCHER`**: This marker is attached to the machine-parseable `TRACE` messages.

### Advanced Logback Configuration Examples

Here are two methods to separate the machine-parseable data logs (`TRACE` level) into a dedicated file, which is useful
for automated processing.

#### Method 1: Filtering by Marker

This approach uses Logback's `MarkerFilter` to identify data logs. It works regardless of the logger's name and is very
precise.

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
        <appender-ref ref="STDOUT"/>
        <appender-ref ref="WATCHER_DATA_FILE"/>
    </root>
</configuration>
```

#### Method 2: Filtering by Logger Name Prefix

This approach uses the `WatcherConfig` properties to add a specific prefix to the data logger's name. You then configure
Logback to capture all logs under that name.

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
        <appender-ref ref="DATA_FILE"/>
    </logger>

    <root level="INFO">
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>
```
