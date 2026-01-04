# TDR-0017: Non-Intrusive Validation and Error Handling

**Status**: Accepted
**Date**: 2026-01-03

## Context
As a logging and monitoring library, `slf4j-toys` is an auxiliary component of the application. Its primary goal is to provide visibility, but it must never become a source of instability. If the library is misused or encounters an internal error, it should fail gracefully without affecting the main application's execution. Significant effort was invested to make the library as transparent as possible for the client code.

## Decision
We adopted a "non-intrusive" philosophy for validation and error handling, centralized in the `MeterValidator` class. This ensures that the library remains a silent observer that does not interfere with the application's core logic.

### Key Principles
1.  **Never Throw Exceptions**: No method in the public API (except for constructors in some cases) should throw checked or unchecked exceptions.
2.  **Log Instead of Crash**: If an invalid state is detected (e.g., `Meter` started twice), the library logs an error message with a `CallerStackTraceThrowable` (see [TDR-0016](TDR-0016-artificial-throwable-for-usage-reporting.md)) but continues execution.
3.  **Defensive Internal Catching**: Internal logic (e.g., in `start()`, `ok()`, `progress()`) is wrapped in `try-catch` blocks that catch `Exception`. Any caught exception is logged as a "bug" but not rethrown.
4.  **Validation Offloading**: Validation logic is moved out of the main `Meter` class into `MeterValidator` to keep the core logic clean and focused.
5.  **Silent Failure for Optional Features**: If a feature like "data logging" is disabled, the library uses Null Objects (see [TDR-0011](TDR-0011-null-object-pattern-for-optional-logging.md)) to avoid null checks and potential `NullPointerException`.

## Consequences
**Positive**:
*   **Application Stability**: The library is "safe" to use; it will not crash the application even if used incorrectly.
*   **Developer Feedback**: Inconsistent usage is still reported via logs, allowing developers to fix issues during development.
*   **Clean Core Logic**: The `Meter` class remains focused on its lifecycle management, while `MeterValidator` handles the "defensive" aspects.

**Negative**:
*   **Silent Bugs**: If logs are not monitored, incorrect usage might go unnoticed for a long time.
*   **Performance Overhead**: The use of `try-catch` blocks and validation methods adds a small overhead to every call.
*   **Code Verbosity**: Requires wrapping many methods in `try-catch` and calling validator methods.

## Alternatives
*   **Fail-Fast**: Throw exceptions immediately upon detecting misuse. **Rejected because** it violates the principle that a monitoring tool should not be a point of failure.
*   **Assertions**: Use Java `assert` statements. **Rejected because** assertions are often disabled in production, leaving the library vulnerable to inconsistent states.

## Implementation
*   [src/main/java/org/usefultoys/slf4j/meter/MeterValidator.java](src/main/java/org/usefultoys/slf4j/meter/MeterValidator.java): Centralizes all validation and error logging logic.
*   [src/main/java/org/usefultoys/slf4j/meter/Meter.java](src/main/java/org/usefultoys/slf4j/meter/Meter.java): Uses `MeterValidator` and wraps its own methods in `try-catch` blocks.

## References
*   [TDR-0011: Null Object Pattern for Optional Logging](TDR-0011-null-object-pattern-for-optional-logging.md)
*   [TDR-0016: Artificial Throwable for Usage Reporting](TDR-0016-artificial-throwable-for-usage-reporting.md)
