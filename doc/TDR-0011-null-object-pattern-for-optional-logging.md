# TDR-0011: Null Object Pattern for Optional Logging

**Status**: Accepted
**Date**: 2026-01-03

## Context

The `slf4j-toys` library supports optional logging components that may be disabled at runtime. For example, `LoggerOutputStream` and `LoggerPrintStream` map an `OutputStream` or `PrintStream` to a logger at a specific level; when that level is disabled, the stream must still accept writes without producing output.

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
3.  **Initialization**: In classes like `LoggerOutputStream` and `LoggerPrintStream`, the underlying stream is always initialized. If the target logging level is disabled, the stream is assigned `NullOutputStream.INSTANCE` or `NullPrintStream.INSTANCE` instead of `null`. Similarly, any code that conditionally holds a `Logger` reference may use `NullLogger.INSTANCE` when the channel is permanently disabled.
4.  **Usage**: Client code interacts with the logger or stream without any null checks, relying on the interface contract.

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
*   `LoggerOutputStream` and `LoggerPrintStream` use null object implementations when the target logging level is disabled.
*   Code that conditionally holds a `Logger` reference uses `NullLogger.INSTANCE` when the channel is permanently disabled, so callers can invoke `is...Enabled()` and logging methods without null checks.

## References

*   [NullLogger.java](../src/main/java/org/usefultoys/slf4j/NullLogger.java)
*   [NullOutputStream.java](../src/main/java/org/usefultoys/slf4j/NullOutputStream.java)
*   [NullPrintStream.java](../src/main/java/org/usefultoys/slf4j/NullPrintStream.java)
*   [LoggerOutputStream.java](../src/main/java/org/usefultoys/slf4j/LoggerOutputStream.java)
*   [Watcher.java](../src/main/java/org/usefultoys/slf4j/watcher/Watcher.java)
*   [TDR-0008: Flexible Execution Strategies (Push vs. Pull)](./TDR-0008-flexible-execution-strategies-push-vs-pull.md)
