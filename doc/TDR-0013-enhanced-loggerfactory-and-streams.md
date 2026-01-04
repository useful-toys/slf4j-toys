# TDR-0013: Enhanced LoggerFactory and Stream-to-Log Mapping

**Status**: Accepted
**Date**: 2026-01-03

## Context
The standard SLF4J `LoggerFactory` is limited to basic logger retrieval by name or class. In complex applications, there is often a need for more structured logger naming conventions (e.g., sub-loggers for specific operations) and the ability to redirect legacy `OutputStream` or `PrintStream` output (from third-party libraries or system output) into the logging framework.

## Decision
We implemented an alternative `org.usefultoys.slf4j.LoggerFactory` that extends the capabilities of the original SLF4J factory.

Key features include:
1.  **Hierarchical Logger Naming**: Methods to create loggers based on a parent class or logger plus a suffix (e.g., `getLogger(MyClass.class, "network")`), facilitating fine-grained logging control.
2.  **Stream-to-Log Mapping**: Integration with `LoggerOutputStream` and `PrintStream` to capture byte/character streams and emit them as log events.
3.  **Level-Aware Streams**: Factory methods like `getTracePrintStream(Logger)` or `getInfoOutputStream(Logger)` that return specialized streams for different SLF4J levels.
4.  **Optimization via Null Objects**: If a specific log level is disabled, the factory returns a `NullPrintStream` or `NullOutputStream`, avoiding unnecessary buffering and string conversion overhead.

## Consequences
**Positive**:
*   **Improved Observability**: Allows capturing output from legacy code or external tools that only write to `System.out` or `System.err`.
*   **Consistent Naming**: Encourages a consistent hierarchical naming pattern for sub-components or specific features within a class.
*   **Performance**: By returning "Null" objects when logging is disabled, we minimize the performance impact of capturing stream data.
*   **Developer Productivity**: Reduces boilerplate code needed to wrap streams into loggers manually.

**Negative**:
*   **Naming Collision**: Having a class named `LoggerFactory` in a different package than `org.slf4j.LoggerFactory` can lead to import confusion if not handled carefully by the developer.
*   **Memory Overhead**: `LoggerOutputStream` uses a `ByteArrayOutputStream` to buffer data until `close()` is called, which could be an issue for extremely large outputs if not managed correctly.

## Alternatives
*   **SLF4J Extensions**: Use existing SLF4J extension libraries. **Rejected because** we wanted to maintain zero external dependencies and provide a seamless experience within the `slf4j-toys` ecosystem.
*   **Manual Wrapping**: Force developers to write their own `OutputStream` wrappers. **Rejected because** it's a common requirement that benefits from a standardized, optimized implementation.

## Implementation
*   [src/main/java/org/usefultoys/slf4j/LoggerFactory.java](src/main/java/org/usefultoys/slf4j/LoggerFactory.java): The main entry point for enhanced logger and stream creation.
*   [src/main/java/org/usefultoys/slf4j/LoggerOutputStream.java](src/main/java/org/usefultoys/slf4j/LoggerOutputStream.java): Internal buffer that redirects to SLF4J on `close()`.
*   [src/main/java/org/usefultoys/slf4j/NullPrintStream.java](src/main/java/org/usefultoys/slf4j/NullPrintStream.java) and [src/main/java/org/usefultoys/slf4j/NullOutputStream.java](src/main/java/org/usefultoys/slf4j/NullOutputStream.java): No-op implementations for disabled log levels.

## References
*   [SLF4J API](https://www.slf4j.org/api/org/slf4j/LoggerFactory.html)
*   [TDR-0011: Null Object Pattern for Optional Logging](TDR-0011-null-object-pattern-for-optional-logging.md)
