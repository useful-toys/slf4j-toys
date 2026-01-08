# Meter State Diagram

This document describes the lifecycle states and transitions for the `Meter` class, based on the attributes defined in `MeterData`.

## Class Hierarchy

```mermaid
classDiagram
    direction TB

    %% Superclasses (Above)
    class EventData {
        + sessionUuid: String
        + position: long
        + lastCurrentTime: long
    }

    class SystemData {
        <<abstract>>
    }

    class MeterData {
        + category: String
        + operation: String
        + parent: String
        + description: String
        + createTime: long
        + startTime: long
        + stopTime: long
        + timeLimit: long
        + currentIteration: long
        + expectedIterations: long
        + okPath: String
        + rejectPath: String
        + failPath: String
        + failMessage: String
        + context: Map$String,String$
        + getFullID() String
        + collectCurrentWaitingTime() long
        + collectCurrentExecutionTime() long
        + readableMessage() String
        + json5Message() String
    }

    class Meter {
        + messageLogger: Logger«final»
        + dataLogger: Logger«final»
        ~ lastProgressTime: long
        - lastProgressIteration: long
        - previousInstance: WeakReference$Meter$
    }

    %% Mixins (Below)
    class MeterAnalysis {
        <<interface>>
        + isStarted(): boolean
        + isStopped(): boolean
        + isOK(): boolean
        + isReject(): boolean
        + isFail(): boolean
        + getPath(): String
        + getIterationsPerSecond(): double
        + getExecutionTime(): long
        + getWaitingTime(): long
        + isSlow(): boolean
    }

    %% Hierarchy
    EventData <|-- SystemData
    SystemData <|-- MeterData
    MeterData <|-- Meter
    
    %% Mixin Implementations
    MeterAnalysis <|.. MeterData
```

**Visibility symbols:**  
  - `+` public  
  - `-` private  
  - `#` protected  
  - `~` package-private (default)


## States

A `Meter` instance is always in one of the following states, determined by its `MeterData` attributes:

| State | Description | Condition (MeterData) |
|-------|-------------|-----------------------|
| **Created** | Initial state after instantiation | `startTime == 0 && stopTime == 0` |
| **Started** | Operation in progress | `startTime != 0 && stopTime == 0` |
| **Stopped (OK)** | Successful completion | `stopTime != 0 && failPath == null && rejectPath == null` |
| **Stopped (Rejected)** | Business rule rejection | `stopTime != 0 && rejectPath != null` |
| **Stopped (Failed)** | Technical failure | `stopTime != 0 && failPath != null` |

States are mutually exclusive and collectively exhaustive.
The state is queried from the `MeterAnalysis` mixin interface which implements que state methods based on `MeterData` attributes.

## State Transition Diagram

Only valid transitions are shown.

```mermaid
stateDiagram-v2
    [*] --> Created
    
    Created --> Started: ✅ start()
    Created --> Created: iterations(n)
    
    Started --> OK: ✅ ok() / ok(pathId)
    Started --> Rejected: ✅ reject()
    Started --> Failed: ✅ fail()
    Started --> Failed: ✅ close()
    Started --> Started: inc() / incBy() / incTo()<br/>iterations(n) / path(pathId)<br/>progress()

    OK --> [*]: ✅ close()
    Rejected --> [*]: ✅ close()
    Failed --> [*]: ✅ close()

    OK --> [*]
    Rejected --> [*]
    Failed --> [*]

```

## Transitions

The following table details all state-related method calls for `Meter`.

**Legend:**
- ✅ **Valid state-changing (expected flow):** A valid call that changes the lifecycle in the normal sequence (typically `Created → Started → (OK | Rejected | Failed)`), by updating state-driving attributes.
- ☑️ **Valid non-state-changing (expected flow):** A valid call used while the meter is in a normal state for that call. It does not change the lifecycle classification, but may update supporting attributes.
- ⚠️ **Outside expected flow (discouraged):** The call is executed even though it is outside the normal sequence, to keep the API non-intrusive and resilient. It may still change the lifecycle classification (Termination (resilience)) and/or update attributes (Applied but discouraged). An error log entry is typically emitted (e.g., `ILLEGAL` or `INCONSISTENT_*`).
- ❌ **Ignored (invalid call):** Preconditions and/or arguments are not met, so the call is ignored to preserve resilience. No transition/attribute change is applied for that call. An error log entry is typically emitted (e.g., `ILLEGAL` or `INCONSISTENT_*`).

**Definitions:**
- **Lifecycle classification:** The state returned by `isStarted()`, `isStopped()`, `isOK()`, `isReject()`, `isFail()`, and `isSlow()`, based on `MeterData` attributes.
- **State-changing:** Any call that can change the lifecycle classification and/or the reported outcome path (notably `startTime`, `stopTime`, `rejectPath`, `failPath`, `okPath`, and `timeLimit`).
- **Non-state-changing:** Calls that do not change the lifecycle classification, but may still update supporting attributes (e.g., `expectedIterations`, `currentIteration`, progress bookkeeping).
- **Expected flow:** Calls made when the meter is in a state where that call is normally valid and intended to be used.
- **Outside expected flow:** Calls made when the meter is in a state where that call is normally not intended, but is still applied for resilience.
- **Invalid call:** Calls made when preconditions/arguments are invalid for that call, so the call is ignored to preserve resilience.
- **Resilience:** The ability of the `Meter` to handle incorrect usage gracefully (non-intrusive behavior), without throwing exceptions or entering an invalid internal state.

Some methods (as description, context) are not included in the table as they do not affect the lifecycle state.

The table below is intentionally exhaustive: for each lifecycle state (`Created`, `Started`, `OK`, `Rejected`, `Failed`), it lists the behavior of every relevant call (including calls that are invalid or discouraged).

Note: A call may be valid for a given state, but still be invalid if its arguments are invalid. For example, `Created → iterations(n) → Created` is valid when `n > 0`, but becomes an ❌ ignored call (with an error log entry) when `n <= 0`.

|  | From State | Call | To State | Notes |
|:--:|:---|:---|:---|:---|
| ☑️ | **Created** | `iterations(n)` | **Created** | When `n > 0`: sets `expectedIterations`.<br/>Otherwise: logs `ILLEGAL` (Non-positive argument).
| ☑️ | **Created** | `limitMilliseconds(n)` | **Created** | When `n > 0`: sets `timeLimit`.<br/>Otherwise: logs `ILLEGAL` (Non-positive argument).
| ❌ | **Created** | `path(okPath)` | **Created** | Ignored. Logs `ILLEGAL` (Meter path but not started).
| ❌ | **Created** | `inc()` | **Created** | Ignored. Logs `INCONSISTENT_INCREMENT` (Meter not started).
| ❌ | **Created** | `incBy(n)` | **Created** | Ignored. Logs `INCONSISTENT_INCREMENT` (Meter not started).
| ❌ | **Created** | `incTo(n)` | **Created** | Ignored. Logs `INCONSISTENT_INCREMENT` (Meter not started).
| ✅ | **Created** | `start()` | **Started** | Normal start. Sets `startTime` and enables thread-local propagation.
| ❌ | **Created** | `progress()` | **Created** | Ignored. Logs `INCONSISTENT_PROGRESS` (Meter progress but not started).
| ⚠️ | **Created** | `ok()` | **OK** | Termination (resilience). Logs `INCONSISTENT_OK` (Meter stopped but not started).
| ⚠️ | **Created** | `ok(okPath)` | **OK** | Termination (resilience). When `okPath == null`: logs `ILLEGAL` (Null argument).<br/>Otherwise: logs `INCONSISTENT_OK` (Meter stopped but not started).
| ⚠️ | **Created** | `reject(cause)` | **Rejected** | Termination (resilience). When `cause == null`: logs `ILLEGAL` (Null argument).<br/>Otherwise: logs `INCONSISTENT_REJECT` (Meter stopped but not started).
| ⚠️ | **Created** | `fail(cause)` | **Failed** | Termination (resilience). When `cause == null`: logs `ILLEGAL` (Null argument).<br/>Otherwise: logs `INCONSISTENT_FAIL` (Meter stopped but not started).
| ⚠️ | **Created** | `close()` | **Failed** | Termination (resilience). Logs `INCONSISTENT_CLOSE` (Meter stopped but not started).
| ❌ | **Created** | `finalize()` | **Created** | Ignored. `validateFinalize` logs only when Started and not Stopped.
| ⚠️ | **Started** | `start()` | **Started** | Applied but discouraged. Logs `INCONSISTENT_START` (Meter already started). Resets `startTime` to now.
| ⚠️ | **Started** | `iterations(n)` | **Started** | When `n > 0`: sets/overrides `expectedIterations`.<br/>Otherwise: logs `ILLEGAL` (Non-positive argument).
| ☑️ | **Started** | `limitMilliseconds(n)` | **Started** | When `n > 0`: sets/overrides `timeLimit`.<br/>Otherwise: logs `ILLEGAL` (Non-positive argument).
| ☑️ | **Started** | `path(okPath)` | **Started** | When `okPath == null`: logs `ILLEGAL` (Null argument).<br/>Otherwise: sets/overrides `okPath`.
| ☑️ | **Started** | `inc()` | **Started** | Increments `currentIteration`.
| ☑️ | **Started** | `incBy(n)` | **Started** | When `n > 0`: adds `n` to `currentIteration`.<br/>Otherwise: logs `ILLEGAL` (Non-positive increment).
| ☑️ | **Started** | `incTo(n)` | **Started** | When `n > 0` and `n > currentIteration`: sets `currentIteration = n`.<br/>Otherwise when `n <= 0`: logs `ILLEGAL` (Non-positive argument).<br/>Otherwise: logs `ILLEGAL` (Non-forward increment).
| ☑️ | **Started** | `progress()` | **Started** | May log progress periodically (only when progress advanced and throttling allows).
| ✅ | **Started** | `ok()` | **OK** | Normal termination.
| ✅ | **Started** | `ok(okPath)` | **OK** | When okPath == null: logs `ILLEGAL` (Null argument).<br/>Otherwise: normal termination with path.
| ✅ | **Started** | `reject(cause)` | **Rejected** | When cause == null: logs `ILLEGAL` (Null argument).<br/>Otherwise: normal rejection termination.
| ✅ | **Started** | `fail(cause)` | **Failed** | When cause == null: logs `ILLEGAL` (Null argument).<br/>Otherwise: normal failure termination.
| ✅ | **Started** | `close()` | **Failed** | Auto-fail for try-with-resources when not explicitly stopped.
| ⚠️ | **Started** | `finalize()` | **Started** | Termination (resilience). Logs `INCONSISTENT_FINALIZED` (Meter started but never stopped).
| ⚠️ | **OK** | `start()` | **OK** | Applied but discouraged. Logs `INCONSISTENT_START` (Meter already started). Resets `startTime` to now.
| ⚠️ | **OK** | `iterations(n)` | **OK** | Applied but discouraged. When `n > 0`: sets `expectedIterations`.<br/>Otherwise: logs `ILLEGAL` (Non-positive argument).
| ⚠️ | **OK** | `limitMilliseconds(n)` | **OK** | Applied but discouraged. When `n > 0`: sets `timeLimit`.<br/>Otherwise: logs `ILLEGAL` (Non-positive argument).
| ❌ | **OK** | `path(okPath)` | **OK** | Ignored. Logs `ILLEGAL` (Meter path but already stopped).
| ⚠️ | **OK** | `inc()` | **OK** | Applied but discouraged: increments `currentIteration`.
| ⚠️ | **OK** | `incBy(n)` | **OK** | Applied but discouraged. When `n > 0`: increments `currentIteration`.<br/>Otherwise: logs `ILLEGAL` (Non-positive increment).
| ⚠️ | **OK** | `incTo(n)` | **OK** | Applied but discouraged. When `n > 0` and `n > currentIteration`: sets `currentIteration`.<br/>Otherwise when `n <= 0`: logs `ILLEGAL` (Non-positive argument).<br/>Otherwise: logs `ILLEGAL` (Non-forward increment).
| ⚠️ | **OK** | `progress()` | **OK** | Applied but discouraged: may still log progress (subject to throttling).
| ⚠️ | **OK** | `ok()` | **OK** | Termination (resilience). Logs `INCONSISTENT_OK` (Meter already stopped).
| ⚠️ | **OK** | `ok(okPath)` | **OK** | Termination (resilience). When `okPath == null`: logs `ILLEGAL` (Null argument).<br/>Otherwise: logs `INCONSISTENT_OK` (Meter already stopped).
| ⚠️ | **OK** | `reject(cause)` | **Rejected** | Termination (resilience). When `cause == null`: logs `ILLEGAL` (Null argument).<br/>Otherwise: logs `INCONSISTENT_REJECT` (Meter already stopped).
| ⚠️ | **OK** | `fail(cause)` | **Failed** | Termination (resilience). When `cause == null`: logs `ILLEGAL` (Null argument).<br/>Otherwise: logs `INCONSISTENT_FAIL` (Meter already stopped).
| ❌ | **OK** | `close()` | **OK** | Ignored. `close()` returns immediately when already stopped.
| ❌ | **OK** | `finalize()` | **OK** | Ignored. `validateFinalize` logs only when Started and not Stopped.
| ⚠️ | **Rejected** | `start()` | **Rejected** | Applied but discouraged. Logs `INCONSISTENT_START` (Meter already started). Resets `startTime` to now.
| ⚠️ | **Rejected** | `iterations(n)` | **Rejected** | Applied but discouraged. When `n > 0`: sets `expectedIterations`.<br/>Otherwise: logs `ILLEGAL` (Non-positive argument).
| ⚠️ | **Rejected** | `limitMilliseconds(n)` | **Rejected** | Applied but discouraged. When `n > 0`: sets `timeLimit`.<br/>Otherwise: logs `ILLEGAL` (Non-positive argument).
| ❌ | **Rejected** | `path(okPath)` | **Rejected** | Ignored. Logs `ILLEGAL` (Meter path but already stopped).
| ⚠️ | **Rejected** | `inc()` | **Rejected** | Applied but discouraged: increments `currentIteration`.
| ⚠️ | **Rejected** | `incBy(n)` | **Rejected** | Applied but discouraged. When `n > 0`: increments `currentIteration`.<br/>Otherwise: logs `ILLEGAL` (Non-positive increment).
| ⚠️ | **Rejected** | `incTo(n)` | **Rejected** | Applied but discouraged. When `n > 0` and `n > currentIteration`: sets `currentIteration`.<br/>Otherwise when `n <= 0`: logs `ILLEGAL` (Non-positive argument).<br/>Otherwise: logs `ILLEGAL` (Non-forward increment).
| ⚠️ | **Rejected** | `progress()` | **Rejected** | Applied but discouraged: may still log progress (subject to throttling).
| ⚠️ | **Rejected** | `ok()` | **OK** | Termination (resilience). Logs `INCONSISTENT_OK` (Meter already stopped).
| ⚠️ | **Rejected** | `ok(okPath)` | **OK** | Termination (resilience). When `okPath == null`: logs `ILLEGAL` (Null argument).<br/>Otherwise: logs `INCONSISTENT_OK` (Meter already stopped).
| ⚠️ | **Rejected** | `reject(cause)` | **Rejected** | Termination (resilience). When `cause == null`: logs `ILLEGAL` (Null argument).<br/>Otherwise: logs `INCONSISTENT_REJECT` (Meter already stopped).
| ⚠️ | **Rejected** | `fail(cause)` | **Failed** | Termination (resilience). When `cause == null`: logs `ILLEGAL` (Null argument).<br/>Otherwise: logs `INCONSISTENT_FAIL` (Meter already stopped).
| ❌ | **Rejected** | `close()` | **Rejected** | Ignored. `close()` returns immediately when already stopped.
| ❌ | **Rejected** | `finalize()` | **Rejected** | Ignored. `validateFinalize` logs only when Started and not Stopped.
| ⚠️ | **Failed** | `start()` | **Failed** | Applied but discouraged. Logs `INCONSISTENT_START` (Meter already started). Resets `startTime` to now.
| ⚠️ | **Failed** | `iterations(n)` | **Failed** | Applied but discouraged. When `n > 0`: sets `expectedIterations`.<br/>Otherwise: logs `ILLEGAL` (Non-positive argument).
| ⚠️ | **Failed** | `limitMilliseconds(n)` | **Failed** | Applied but discouraged. When `n > 0`: sets `timeLimit`.<br/>Otherwise: logs `ILLEGAL` (Non-positive argument).
| ❌ | **Failed** | `path(okPath)` | **Failed** | Ignored. Logs `ILLEGAL` (Meter path but already stopped).
| ⚠️ | **Failed** | `inc()` | **Failed** | Applied but discouraged: increments `currentIteration`.
| ⚠️ | **Failed** | `incBy(n)` | **Failed** | Applied but discouraged. When `n > 0`: increments `currentIteration`.<br/>Otherwise: logs `ILLEGAL` (Non-positive increment).
| ⚠️ | **Failed** | `incTo(n)` | **Failed** | Applied but discouraged. When `n > 0` and `n > currentIteration`: sets `currentIteration`.<br/>Otherwise when `n <= 0`: logs `ILLEGAL` (Non-positive argument).<br/>Otherwise: logs `ILLEGAL` (Non-forward increment).
| ⚠️ | **Failed** | `progress()` | **Failed** | Applied but discouraged: may still log progress (subject to throttling).
| ⚠️ | **Failed** | `ok()` | **OK** | Termination (resilience). Logs `INCONSISTENT_OK` (Meter already stopped).
| ⚠️ | **Failed** | `ok(okPath)` | **OK** | Termination (resilience). When `okPath == null`: logs `ILLEGAL` (Null argument).<br/>Otherwise: logs `INCONSISTENT_OK` (Meter already stopped).
| ⚠️ | **Failed** | `reject(cause)` | **Rejected** | Termination (resilience). When `cause == null`: logs `ILLEGAL` (Null argument).<br/>Otherwise: logs `INCONSISTENT_REJECT` (Meter already stopped).
| ⚠️ | **Failed** | `fail(cause)` | **Failed** | Termination (resilience). When `cause == null`: logs `ILLEGAL` (Null argument).<br/>Otherwise: logs `INCONSISTENT_FAIL` (Meter already stopped).
| ❌ | **Failed** | `close()` | **Failed** | Ignored. `close()` returns immediately when already stopped.
| ❌ | **Failed** | `finalize()` | **Failed** | Ignored. `validateFinalize` logs only when Started and not Stopped.

## State Query Methods

| Method | Returns `true` when | Condition (MeterData) |
|--------|---------------------|-----------------------|
| `isStarted()` | Operation has started | `startTime != 0` |
| `isStopped()` | Operation has finished | `stopTime != 0` |
| `isOK()` | Stopped successfully | `stopTime != 0 && failPath == null && rejectPath == null` |
| `isReject()` | Stopped with rejection | `stopTime != 0 && rejectPath != null` |
| `isFail()` | Stopped with failure | `stopTime != 0 && failPath != null` |
| `isSlow()` | Execution time exceeds limit | `timeLimit != 0 && startTime != 0 && (executionTime > timeLimit)` |

> **Note**: `executionTime` is `stopTime - startTime` if stopped, or `now - startTime` if still running.

## Validation and Error Handling

All state validations are handled by [MeterValidator.java](../src/main/java/org/usefultoys/slf4j/meter/MeterValidator.java):

- **`validateStartPrecondition()`**: Ensures meter hasn't already started (checks `startTime == 0`).
- **`validateStopPrecondition()`**: Ensures meter was started and not already stopped (checks `stopTime == 0`).
- **`validateProgressPrecondition()`**: Ensures meter has been started (checks `startTime != 0`).
- **`validateIncPrecondition()`**: Ensures meter has been started before incrementing (checks `startTime != 0`).
- **`validatePathPrecondition()`**: Ensures meter has been started before setting path (checks `startTime != 0`).
- **`validatePathArgument()`**: Logs an error if path identifiers are null (non-blocking).
- **`validateIncBy()` / `validateIncToArguments()`**: Validates increment values and forward progress.
- **`validateFinalize()`**: Detects meters that were started but never stopped (logs error during garbage collection).

## Thread-Local Stack Management

When a `Meter` is started, it becomes the **current instance** for the thread. Nested meters are supported via a thread-local stack. When a meter stops, the previous meter in the stack becomes current again.

```java
Meter.getCurrentInstance()  // returns the most recently started meter
```

## References

- Implementation: [Meter.java](../src/main/java/org/usefultoys/slf4j/meter/Meter.java)
- State analysis: [MeterAnalysis.java](../src/main/java/org/usefultoys/slf4j/meter/MeterAnalysis.java)
- Validation: [MeterValidator.java](../src/main/java/org/usefultoys/slf4j/meter/MeterValidator.java)
- Data model: [MeterData.java](../src/main/java/org/usefultoys/slf4j/meter/MeterData.java)
- Tests: [MeterStateAttributesTest.java](../src/test/java/org/usefultoys/slf4j/meter/MeterStateAttributesTest.java)
