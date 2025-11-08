# Logback Extensions API Documentation

The `org.usefultoys.slf4j.logback` package provides a set of custom Logback `Converter` classes that enhance your application's logs with better formatting and ANSI color highlighting. These extensions are particularly powerful when used with the `Meter` and `Watcher` components, as they can visually distinguish different types of log events in a console that supports ANSI colors.

## Core Concepts

Logback allows you to define custom keywords (called "conversion words") for your log patterns. This package provides converters that you can register to add the following capabilities:
*   **Status Formatting**: Display a short, aligned status word based on the `Meter`'s state (e.g., `OK`, `FAIL`, `SLOW`).
*   **Color Highlighting**: Apply colors to the status or the entire log message based on the event's `Marker` or log level, making it easy to spot important events like errors or slow operations.

## Design Philosophy: Replacing Log Level with Meter Status

A core design principle of `slf4j-toys` is to provide richer, more semantic information than standard log levels can offer. While `INFO`, `WARN`, and `ERROR` are useful, they don't fully describe the business outcome of an operation.
*   Does `INFO` mean success, or just a step in a process?
*   Is a `WARN` a technical problem, or a business-level rejection that is expected?

The `Meter` component solves this by providing explicit statuses like `OK`, `FAIL`, `REJECT`, and `SLOW`. The converters in this package are designed to bring that semantic richness directly into your logs, replacing the generic log level.

The `StatusConverter` (`%status`) is the key to this. It is designed to be a "smart" replacement for `%level`:
1.  If a log event comes from a `Meter`, it displays the `Meter`'s status (e.g., `OK`, `FAIL`).
2.  If the log event is a standard one (e.g., `logger.info("Application starting...")`), it intelligently **falls back** to displaying the standard log level (`INFO`).

This allows you to completely replace `%level` with `%status` in your log pattern, creating a cleaner and more informative log output without losing information for standard log messages.

## Available Converters

### 1. `StatusConverter`
*   **Conversion Word**: `status`
*   **Description**: A smart replacement for Logback's `%level`. It inspects the SLF4J `Marker` and outputs a `Meter` status if available; otherwise, it falls back to the standard log level.
*   **Output Examples**: `START`, `OK`, `FAIL`, `SLOW`, `REJECT`, `INFO`, `WARN`.

### 2. `StatusHighlightConverter`
*   **Conversion Word**: `highlightStatus`
*   **Description**: A composite converter that wraps another pattern (typically `%status`) and colors its output based on the `Meter` status or log level.
    *   `OK` becomes bright green.
    *   `FAIL` becomes bright red.
    *   `SLOW` becomes bright yellow.
    *   `REJECT` becomes bright magenta.

### 3. `MessageHighlightConverter`
*   **Conversion Word**: `highlightMessage`
*   **Description**: Colors the entire log message it wraps to provide more or less visual emphasis.
    *   Important `Meter` messages are colored bright white.
    *   Data-only messages are colored gray to be less prominent.
    *   Errors or inconsistencies are colored red.

## How to Use

To use these extensions, you need to configure them in your `logback.xml` file.

### Step 1: Register the Custom Converters

First, you must tell Logback about the new conversion words and which classes handle them.

```xml
<configuration>
    <conversionRule conversionWord="status" converterClass="org.usefultoys.slf4j.logback.StatusConverter" />
    <conversionRule conversionWord="highlightStatus" converterClass="org.usefultoys.slf4j.logback.StatusHighlightConverter" />
    <conversionRule conversionWord="highlightMessage" converterClass="org.usefultoys.slf4j.logback.MessageHighlightConverter" />

    <!-- ... rest of configuration ... -->
</configuration>
```

### Step 2: Use the Converters in a Pattern

The recommended approach is to replace the standard `%level` keyword with the new `%status` keyword to get richer semantic information.

This example shows a pattern for a console appender that implements this philosophy.

```xml
<configuration>
    <!-- (conversionRule definitions from Step 1 go here) -->

    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <!--
              This pattern replaces '%level' with '%highlightStatus(%-6status)'.
              The status will be padded to 6 characters for alignment and colored.
            -->
            <pattern>%d{HH:mm:ss.SSS} %highlightStatus(%-6status) %-25.25logger{24} %highlightMessage(%msg)%n</pattern>
        </encoder>
    </appender>

    <root level="TRACE">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

### Example Log Output

With the configuration above, your console logs become much more descriptive:

**Meter-based logs:**
```
14:35:14.123 [bright green]OK    [/bright green] service.UserService       [bright white]Login successful for user 'alice'[/bright white]
14:35:14.456 [bright red]FAIL  [/bright red] service.UserService       [bright white]Database connection failed[/bright white]
14:35:15.789 [bright yellow]SLOW  [/bright yellow] service.PaymentGateway    [bright white]Payment processing took 2500ms[/bright white]
```

**Standard logs (fallback in action):**
```
10:00:00.000 [bright green]INFO  [/bright green] com.example.Main          [white]Application starting up...[/white]
10:00:01.234 [bright red]ERROR [/bright red] com.example.DBConnector   [red]Failed to connect to database.[/red]
```

This makes the log output significantly easier to parse visually, allowing you to distinguish between business-level outcomes and standard application lifecycle events at a glance.
