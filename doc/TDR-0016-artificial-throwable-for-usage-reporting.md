# TDR-0016: Artificial Throwable for Usage Reporting

**Status**: Accepted
**Date**: 2026-01-03

## Context
When a developer misuses the library (e.g., stopping a `Meter` twice, or closing it without starting it), simply logging an error message is often insufficient. The library must be very clear in reporting incorrect usage to the developer. They need to know exactly *where* in their code the incorrect call was made, what `Meter` instance caused the failure, and ideally where that `Meter` was created and where the invalida call was made. A standard log message only shows the library's internal stacktrace, not the caller's context. Without this information, developers waste time investigating library internals instead of locating and fixing their code.

## Decision
We implemented `CallerStackTraceThrowable`, an artificial `Throwable` designed to capture and manipulate the stack trace to point directly to the library's caller. This ensures that whenever an inconsistency is found, the logs point directly to the client code responsible.

### Implementation Details
1.  **Stack Trace Manipulation**: In its constructor, `CallerStackTraceThrowable` retrieves the current thread's stack trace and discards all frames belonging to the library's package (`org.usefultoys.slf4j`).
2.  **Targeted Reporting**: The resulting stack trace starts exactly at the first method outside the library that initiated the call.
3.  **No-op `fillInStackTrace`**: We override `fillInStackTrace()` to do nothing, as the stack trace is already manually set in the constructor. This also provides a slight performance optimization.
4.  **Usage in Validation**: `MeterValidator` creates and logs this throwable whenever an illegal API call or inconsistent state is detected.

## Consequences
**Positive**:
*   **Actionable Logs**: Developers can immediately see the exact line in their code that caused the library to log a warning or error, knowing precisely where to investigate and fix.
*   **Instance Traceability**: Combined with the error message containing the `Meter` instance details (e.g., meter ID, state), developers can identify which specific `Meter` instance failed and often trace back to where it was created via the stack trace.
*   **Cleaner Output**: Removes internal library implementation details from the stack trace, reducing noise.
*   **Ease of Debugging**: Significantly reduces the time needed to troubleshoot integration issues by pointing directly to client code.

**Negative**:
*   **Performance Cost**: Capturing a stack trace is a relatively expensive operation. However, since this is only done in "error" or "illegal usage" scenarios, the impact on the "happy path" is zero.
*   **Complexity**: Requires manual manipulation of `StackTraceElement` arrays.

## Alternatives
*   **Standard Logging**: Just log the message. **Rejected because** it lacks the necessary context for quick debugging.
*   **Throwing Exceptions**: Throw an `IllegalStateException`. **Rejected because** a logging library should never disrupt the application's flow (see [TDR-0017](TDR-0017-non-intrusive-validation-and-error-handling.md)).

## Implementation
*   [src/main/java/org/usefultoys/slf4j/CallerStackTraceThrowable.java](src/main/java/org/usefultoys/slf4j/CallerStackTraceThrowable.java): The core implementation of the artificial throwable.
*   [src/main/java/org/usefultoys/slf4j/meter/MeterValidator.java](src/main/java/org/usefultoys/slf4j/meter/MeterValidator.java): Uses the throwable to report illegal calls.

## References
*   [TDR-0017: Non-Intrusive Validation and Error Handling](TDR-0017-non-intrusive-validation-and-error-handling.md)
