# TDR-0019: Immutable Lifecycle Transitions

**Status**: Accepted  
**Date**: 2026-01-04

## Context

The `Meter` class tracks the lifecycle of application operations from creation through execution to termination. A key design challenge is ensuring predictable behavior when methods are called out of order or multiple times. For example:

*   What happens if `start()` is called twice?
*   What if `ok()` is called before `start()`?
*   What if `fail()` is called after `ok()` has already terminated the meter?
*   Should the library throw exceptions, silently ignore invalid transitions, or attempt to "fix" the state?

This decision is critical because the library is used in production environments where unexpected exceptions could disrupt application flow, yet developers need feedback when they misuse the API.

## Decision

We adopt an **immutable lifecycle transition model** with the following rules:

1.  **States are permanent once reached**: A `Meter` can transition from `Created → Started → Stopped`, but never backwards or sideways. Once `Stopped`, the state is frozen.
2.  **First termination wins**: The first call to `ok()`, `reject()`, or `fail()` determines the final state. Subsequent termination calls are ignored.
3.  **Invalid transitions are logged but not enforced**: Attempting invalid transitions (e.g., calling `start()` twice) logs a warning but does not throw exceptions or alter the state.
4.  **Validation is advisory, not blocking**: The `MeterValidator` class logs inconsistencies but never prevents method execution.

### State Machine

```
Created (startTime=0, stopTime=0)
   ↓ start()
Started (startTime!=0, stopTime=0)
   ↓ ok()/reject()/fail()
Stopped (stopTime!=0) → [OK | Rejected | Failed]
   ↓ (terminal - no further transitions)
```

### Transition Rules

| Current State | Method Called | Action | Logs Warning? |
|--------------|---------------|--------|---------------|
| Created | `start()` | Sets `startTime`, transitions to Started | No |
| Created | `ok()`/`reject()`/`fail()` | Ignored | Yes (`INCONSISTENT_*`) |
| Created | `progress()` | Ignored | Yes (`INCONSISTENT_PROGRESS`) |
| Started | `start()` | Ignored (already started) | Yes (`INCONSISTENT_START`) |
| Started | `ok()` (first) | Sets `stopTime`, transitions to OK | No |
| Started | `reject()` (first) | Sets `stopTime`, transitions to Rejected | No |
| Started | `fail()` (first) | Sets `stopTime`, transitions to Failed | No |
| Started | `progress()` | Logs progress, remains Started | No |
| Stopped | `start()` | Ignored | Yes (`INCONSISTENT_START`) |
| Stopped | `ok()`/`reject()`/`fail()` | Ignored (already stopped) | Yes (`INCONSISTENT_*`) |
| Stopped | `progress()` | Ignored | Yes (`INCONSISTENT_PROGRESS`) |

### Implementation Details

*   **`startTime` guard**: Methods like `ok()`, `reject()`, and `fail()` check `if (startTime == 0)` and return early if the meter wasn't started.
*   **`stopTime` guard**: These same methods also check `if (stopTime != 0)` and return early if the meter is already stopped.
*   **First-write-wins semantics**: Since termination methods set `stopTime` immediately, the first one to execute "wins" and prevents subsequent calls from modifying the state.
*   **Validation via `MeterValidator`**: All precondition checks are delegated to `MeterValidator`, which logs warnings using markers like `INCONSISTENT_START`, `INCONSISTENT_OK`, `INCONSISTENT_FAIL`.

## Consequences

**Positive**:
*   **Predictable behavior**: Developers can reason about the state machine easily—once stopped, a meter stays stopped.
*   **No exceptions in production**: Invalid transitions never crash the application (aligned with [TDR-0017](TDR-0017-non-intrusive-validation-and-error-handling.md)).
*   **First-call-wins is intuitive**: In nested try-catch blocks or complex control flow, the first explicit termination (e.g., `ok()` in the happy path) takes precedence over later calls (e.g., `fail()` in a finally block).
*   **Simplified testing**: Tests can verify that calling `ok()` after `fail()` has no effect, ensuring immutability.
*   **Thread-safe state transitions**: Since `stopTime` is set atomically (it's a `volatile long`), even concurrent calls to `ok()` and `fail()` will result in only one winning.

**Negative**:
*   **Silent failures for invalid usage**: Developers who accidentally call `ok()` twice won't see an exception, only a log message. If logs aren't monitored during development, this can lead to confusion.
*   **Verbose validation code**: Every termination method must include guards for `startTime` and `stopTime`, increasing code verbosity.
*   **No "reset" capability**: Once a `Meter` is stopped, it cannot be restarted or reused. This is intentional but means developers must create a new instance for a new operation.
*   **Potential for misleading logs**: If a developer sees a warning about "Meter already stopped" but the application continues running, they might not realize there's a bug in their usage pattern.

**Neutral**:
*   **Deterministic in single-threaded contexts**: In typical usage (one thread per `Meter`), the first-call-wins rule is unambiguous.
*   **Race conditions in multi-threaded misuse**: If multiple threads incorrectly share a `Meter` and call termination methods concurrently, one will win, but which one is non-deterministic. However, sharing a `Meter` across threads is already incorrect usage.

## Alternatives

### 1. **Fail-Fast with Exceptions**
Throw `IllegalStateException` when invalid transitions are attempted (e.g., `ok()` called twice).

**Rejected because**:
*   Violates the non-intrusive principle ([TDR-0017](TDR-0017-non-intrusive-validation-and-error-handling.md)): A monitoring library should not crash the application.
*   In complex control flow (e.g., try-catch-finally), it's easy to accidentally call termination methods multiple times, and an exception would force developers to add boilerplate checks everywhere.

### 2. **Last-Call-Wins**
Allow later calls to `ok()`, `reject()`, or `fail()` to overwrite the previous state.

**Rejected because**:
*   Unpredictable behavior: Developers couldn't rely on the state being stable after the first termination.
*   Logging would be inconsistent: A `Meter` could log as "OK" and then later as "Failed," creating confusion in log aggregation.
*   Thread-safety issues: Concurrent calls could "fight" over the final state indefinitely.

### 3. **Mutable State with Versioning**
Allow transitions but track a "version" counter that increments with each state change.

**Rejected because**:
*   Adds complexity for little benefit: Most use cases involve a single, predictable lifecycle.
*   Makes reasoning about the state harder: Is version 3 a failure or a success?
*   Conflicts with the principle of immutability, which simplifies testing and reasoning.

### 4. **Explicit State Enum**
Introduce an explicit `enum State { CREATED, STARTED, STOPPED }` instead of inferring state from `startTime` and `stopTime`.

**Rejected because**:
*   Redundant with timestamps: The state can already be inferred from `startTime != 0` and `stopTime != 0`.
*   Adds synchronization complexity: Updating both an enum and timestamps atomically would require locking.
*   Increases memory footprint slightly (though this is negligible).

### 5. **Resettable Meters**
Provide a `reset()` method to allow reusing a `Meter` for multiple operations.

**Rejected because**:
*   Breaks immutability guarantees: Logs and metrics would become harder to correlate with specific operations.
*   Encourages anti-patterns: Reusing meters could lead to subtle bugs where state from a previous operation leaks into a new one.
*   Conflicts with thread-local stacking: A reset meter could disrupt the `getCurrentInstance()` stack.

## Implementation

*   **Core logic**: [src/main/java/org/usefultoys/slf4j/meter/Meter.java](../src/main/java/org/usefultoys/slf4j/meter/Meter.java) — Methods like `start()`, `ok()`, `reject()`, `fail()` implement guards using `startTime` and `stopTime`.
*   **Validation**: [src/main/java/org/usefultoys/slf4j/meter/MeterValidator.java](../src/main/java/org/usefultoys/slf4j/meter/MeterValidator.java) — Methods like `validateStartPrecondition()` and `validateStopPrecondition()` log warnings for invalid transitions.
*   **State analysis**: [src/main/java/org/usefultoys/slf4j/meter/MeterAnalysis.java](../src/main/java/org/usefultoys/slf4j/meter/MeterAnalysis.java) — Provides query methods (`isStarted()`, `isStopped()`, `isOK()`, etc.) for inferring state.
*   **Tests**: [src/test/java/org/usefultoys/slf4j/meter/MeterStateAttributesTest.java](../src/test/java/org/usefultoys/slf4j/meter/MeterStateAttributesTest.java) — Validates state transitions and immutability.

## References

*   [TDR-0017: Non-Intrusive Validation and Error Handling](TDR-0017-non-intrusive-validation-and-error-handling.md) — Explains why we log instead of throwing exceptions.
*   [TDR-0015: ThreadLocal Stack for Context Propagation](TDR-0015-threadlocal-stack-for-context-propagation.md) — Describes how started meters are tracked per thread.
*   [TDR-0016: Artificial Throwable for Usage Reporting](TDR-0016-artificial-throwable-for-usage-reporting.md) — Explains how stack traces are logged for validation warnings.
*   [meter-state-diagram.md](meter-state-diagram.md) — Visual diagram of all valid and invalid state transitions.
