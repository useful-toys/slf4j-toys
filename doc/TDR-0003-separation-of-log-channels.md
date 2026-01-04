# TDR-0003: Separation of Log Channels (Dual Logging)

**Status**: Accepted
**Date**: 2026-01-03

## Context

In production environments, logging serves two primary audiences:
1.  **Humans**: Developers and operators who need concise, readable summaries to understand application behavior and troubleshoot issues.
2.  **Machines**: Log aggregators, monitoring tools, and analytics platforms (e.g., ELK, Splunk, Prometheus) that require structured, parsable data for automated analysis and alerting.

Mixing these two types of output in a single stream often leads to logs that are either too verbose for humans or too difficult for machines to parse reliably.

## Decision

We implemented a **Dual Logging Strategy** that separates human-readable messages from machine-parsable data into distinct "channels". Each `Meter` instance manages two separate SLF4J loggers: `messageLogger` and `dataLogger`.

### Delegation of Responsibility

A key architectural decision was to **delegate the actual separation and routing logic to the SLF4J API and its underlying implementation** (e.g., Logback, Log4j2).
*   **Justification**: We want to avoid the complexity and responsibility of implementing filtering, routing, or multi-destination logging within the library itself. By providing the necessary "hooks" (distinct loggers, markers, and levels), we empower the user to leverage the full power of their chosen logging framework to handle these concerns. This keeps the library lightweight and focused on its core mission: measurement and data collection.

### Data Format Choice

For the machine-parsable channel, we chose **JSON** (specifically a JSON5-compatible format) as the data representation.
*   **Justification**: JSON is the industry standard for structured data exchange. It is natively supported by almost all log aggregators (Elasticsearch, Splunk, etc.) and has excellent library support across all programming languages. This ensures that `slf4j-toys` metrics can be easily integrated into any modern observability stack.

### Distinction Mechanisms

The channels can be distinguished and routed using three complementary mechanisms:

1.  **Logger Name Prefixes and Suffixes**:
    *   The name of the loggers can be decorated with configurable prefixes and suffixes (e.g., `msg.org.example.MyService` vs `data.org.example.MyService`; or `org.example.MyService.msg` vs `org.example.MyService.data`).
    *   Configurable via system properties: `slf4jtoys.meter.message.prefix/suffix` and `slf4jtoys.meter.data.prefix/suffix`.
    *   **Use case**: Routing logs to different files or appenders based on logger name patterns in the logging configuration.

2.  **SLF4J Markers**:
    *   Every log event is tagged with a specific `Marker` from the `Markers` utility class.
    *   Human messages use markers like `METER_MSG_START`, `METER_MSG_OK`, `METER_MSG_FAIL`.
    *   Machine data uses markers like `METER_DATA_START`, `METER_DATA_OK`, `METER_DATA_FAIL`.
    *   **Use case**: Fine-grained filtering and routing within logging frameworks (e.g., Logback's `MarkerFilter`).

3.  **Log Levels**:
    *   **Human Messages**: Use semantic levels (`DEBUG` for start, `INFO` for progress/success, `WARN` for slowness, `ERROR` for failures).
    *   **Machine Data**: Strictly uses the `TRACE` level.
    *   **Use case**: Ensuring that heavy machine data does not clutter standard logs unless explicitly requested by enabling `TRACE` level for the data channel.

## Consequences

**Positive**:
*   **Optimized Observability**: Humans get clear, formatted summaries; machines get structured JSON5 data.
*   **Reduced Library Complexity**: By delegating routing and filtering to SLF4J, the library remains focused on data collection without needing to implement complex logging logic.
*   **Selective Logging**: Provides the flexibility to record only human-readable logs, only machine-parsable logs, or both, depending on the environment's needs (e.g., only human logs in local development, only machine logs in high-traffic production).
*   **Storage and Routing Flexibility**: Logs can be routed to different devices, files, or network appenders (e.g., human logs to a local file for quick tailing, machine logs to a high-performance socket appender for ELK) without any code changes.
*   **Performance Control**: Machine data collection and logging can be completely disabled at the logging framework level with minimal overhead.
*   **Clean Separation**: Business logic logs remain separate from diagnostic/performance metrics.

**Negative**:
*   **Configuration Overhead**: Requires more sophisticated logging configuration to take full advantage of the separation.
*   **Increased Logger Instances**: Each instrumented class results in two logger instances instead of one.

**Neutral**:
*   **Standard Compliance**: Relies entirely on standard SLF4J features (Loggers, Levels, Markers), ensuring compatibility with any SLF4J-compliant backend.

## Alternatives

*   **Single Logger with Structured Logging**: Use a single logger and log everything as JSON.
    *   **Rejected because**: Makes logs very hard for humans to read directly in a terminal or simple text editor.
*   **MDC (Mapped Diagnostic Context)**: Put metrics in the MDC.
    *   **Rejected because**: MDC is thread-bound and not suitable for complex, multi-attribute data structures like `MeterData`. It also doesn't solve the human vs. machine readability conflict.

## Implementation

*   `Meter` constructor initializes `messageLogger` and `dataLogger` using the configured prefixes/suffixes.
*   `Meter.start()`, `ok()`, `fail()`, etc., call both loggers with their respective markers and message formats.
*   `MeterConfig` provides the centralized configuration for the prefixes and suffixes.

## References

*   [Markers.java](../src/main/java/org/usefultoys/slf4j/meter/Markers.java)
*   [Meter.java](../src/main/java/org/usefultoys/slf4j/meter/Meter.java)
*   [MeterConfig.java](../src/main/java/org/usefultoys/slf4j/meter/MeterConfig.java)
*   [TDR-0001: Offloading Complexity to Interfaces](./TDR-0001-offloading-complexity-to-interfaces.md)
