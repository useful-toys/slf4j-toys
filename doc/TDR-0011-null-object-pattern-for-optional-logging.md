# TDR-0011: Null Object Pattern for Optional Logging

**Status**: Accepted
**Date**: 2026-01-03

## Context

The `slf4j-toys` library supports several optional logging features. For example, the `Watcher` can log both human-readable summaries and machine-parsable data. The latter can be disabled via configuration (`slf4jtoys.watcher.data.enabled=false`). 

Traditionally, optional components are handled by setting their references to `null`. However, this approach has several drawbacks:
1.  **Code Clutter**: Every usage of the optional component requires an explicit null check (e.g., `if (dataLogger != null) { ... }`).
2.  **Fragility**: Forgetting a single null check leads to `NullPointerException` at runtime.
3.  **Readability**: The core logic becomes obscured by defensive programming checks.

## Decision

We decided to implement the **Null Object Pattern** for optional logging components.

### Implementation Details

1.  **`NullLogger` Class**: We created a specialized implementation of the SLF4J `Logger` interface that silently discards all events.
    *   **Methods**: All `is...Enabled()` methods return `false`. All logging methods (`trace`, `debug`, `info`, etc.) are empty.
    *   **Singleton**: A single static instance (`NullLogger.INSTANCE`) is reused throughout the library to minimize memory overhead.
2.  **`NullOutputStream` and `NullPrintStream` Classes**: We created specialized implementations that discard all output data.
    *   **Usage**: When an `OutputStream` or `PrintStream` is mapped to a logger at a level that won't generate messages (e.g., logger level is higher than the stream's target level), the null object implementations are used instead of actual logging streams.
    *   **Benefit**: Prevents unnecessary overhead from writing to streams that would produce no logging output, while maintaining a consistent API where streams are always available.
3.  **Initialization**: In classes like `Watcher`, the logger fields are always initialized. If a specific logging channel is disabled, it is assigned the `NullLogger.INSTANCE` instead of `null`. Similarly, OutputStreams and PrintStreams mapped to disabled logging levels use `NullOutputStream.INSTANCE` or `NullPrintStream.INSTANCE`.
4.  **Usage**: The execution logic (e.g., `Watcher.run()`) interacts with the loggers without any null checks, relying on the `Logger` interface's contract.

## Consequences

**Positive**:
*   **Cleaner Code**: The core execution logic is focused on "what to do" rather than "checking if it can be done".
*   **Robustness**: Eliminates a whole class of `NullPointerException` bugs related to optional features.
*   **Performance**: Since `NullLogger` methods are empty and return constant values, the JVM's JIT compiler can easily inline and optimize these calls, often resulting in zero runtime overhead when a feature is disabled.
*   **Maintainability**: Adding new optional logging points is easier as no new null-check infrastructure is required.

**Negative**:
*   **Implementation Effort**: The `Logger` interface is quite large (over 50 methods), all of which had to be implemented in `NullLogger`.
*   **Subtle Logic**: Developers must remember to use `NullLogger.INSTANCE` instead of `null` during initialization.

**Neutral**:
*   **Internal Scope**: `NullLogger` is kept package-private (or internal) to avoid polluting the public API, as it is a design choice specific to the library's implementation.

## Alternatives

*   **Explicit Null Checks**:
    *   **Rejected because**: Leads to verbose and error-prone code.
*   **Optional (Java 8+)**: Use `Optional<Logger>`.
    *   **Rejected because**: While safer than nulls, it still requires `ifPresent()` or `orElse()` calls, which are more verbose than a direct method call on a Null Object.

## Implementation

*   `NullLogger` implements `org.slf4j.Logger`.
*   `NullOutputStream` extends `java.io.OutputStream` and discards all writes.
*   `NullPrintStream` extends `java.io.PrintStream` and discards all writes.
*   `Watcher` constructor uses `NullLogger.INSTANCE` when `dataEnabled` is false.
*   `LoggerOutputStream` and `LoggerPrintStream` use null object implementations when the target logging level is disabled.
*   `Watcher.run()` performs checks like `if (messageLogger.isInfoEnabled() || dataLogger.isTraceEnabled())` which naturally handle the disabled state via the `NullLogger`'s return values.

## References

*   [NullLogger.java](../src/main/java/org/usefultoys/slf4j/NullLogger.java)
*   [NullOutputStream.java](../src/main/java/org/usefultoys/slf4j/NullOutputStream.java)
*   [NullPrintStream.java](../src/main/java/org/usefultoys/slf4j/NullPrintStream.java)
*   [LoggerOutputStream.java](../src/main/java/org/usefultoys/slf4j/LoggerOutputStream.java)
*   [Watcher.java](../src/main/java/org/usefultoys/slf4j/watcher/Watcher.java)
*   [TDR-0008: Flexible Execution Strategies (Push vs. Pull)](./TDR-0008-flexible-execution-strategies-push-vs-pull.md)
