# TDR-0029: Resilient State Transitions with Chainable API

**Status**: Accepted  
**Date**: 2026-01-09

## Context

The `Meter` class provides a **chainable API** (methods return `this` for method chaining) designed to guide developers through a recommended lifecycle: `Created → Started → (OK | Rejected | Failed)`. Unlike true fluent APIs that enforce call order at compile time, the `Meter` API suggests the intended sequence through documentation and contract, but does not prevent out-of-order calls.

However, real-world usage is unpredictable:

* Developers accidentally call methods out of order (e.g., `ok()` before `start()`)
* Exception handlers may call termination methods in unexpected ways
* Complex control flow (nested try-catch-finally blocks) can lead to multiple termination attempts
* Lazy developers may skip intermediate steps or misunderstand the intended sequence

A strict enforcement approach (throwing exceptions for out-of-order calls) would violate the non-intrusive principle ([TDR-0017](TDR-0017-non-intrusive-validation-and-error-handling.md)) and risk breaking production applications. Conversely, allowing arbitrary calls to modify state without validation would make the library unreliable and unpredictable.

The challenge is to balance two competing goals:

1. **Guide correct usage**: Encourage developers toward the recommended lifecycle sequence through clear method contracts and documentation.
2. **Maintain resilience**: Handle out-of-order or invalid calls gracefully without crashing or entering an invalid state.

## Decision

We adopt a **four-tier resilience strategy** that gracefully handles both expected and unexpected method calls while maintaining a valid state at all times:

### Tier 1: ✅ Valid State-Changing (Expected Flow)

**Definition**: A call that changes the lifecycle in the normal sequence (typically `Created → Started → (OK | Rejected | Failed)`), by updating state-driving attributes.

**Behavior**:
* No validation errors are logged.
* The state transition is applied immediately.
* Example: `start()` when in `Created`, or `ok()` when in `Started`.

**Guarantees**:
* The call always succeeds and updates the state as intended.
* No side effects or surprises.

---

### Tier 2: ☑️ Valid Non-State-Changing (Expected Flow)

**Definition**: A call used while the meter is in a normal state for that call. It does not change the lifecycle classification, but may update supporting attributes.

**Behavior**:
* No validation errors are logged.
* The supporting attribute is updated (e.g., `currentIteration`, `expectedIterations`, `okPath`).
* The lifecycle state remains unchanged.
* Example: `inc()` when in `Started`, or `progress()` when in `Started`.

**Guarantees**:
* The call always succeeds and updates the supporting attribute.
* The lifecycle state is stable.

---

### Tier 3: ⚠️ State-Correcting (Outside Expected Flow)

**Definition**: A call that violates the expected API contract, but is accepted and applied to correct the state to a valid configuration while maintaining resilience. The call may change the lifecycle state and/or update attributes. An error log entry is emitted (e.g., `ILLEGAL` or `INCONSISTENT_*`).

**Behavior**:
* A warning or error is logged to alert developers that the call violates the API contract.
* The call is still executed to maintain non-intrusive behavior and correct the state.
* The state transition or attribute update is applied to restore validity.
* Example: `start()` called twice (logs `INCONSISTENT_START` but resets `startTime` to now), or `ok()` called from `Created` (logs `INCONSISTENT_OK` but still terminates the meter as OK).

**Guarantees**:
* The meter remains in a valid state after the call (first-termination-wins ensures immutability).
* Developers get feedback (via logs) that their usage violates the contract.
* The application continues running without exceptions.
* The operation is still tracked and reported, even if the call was out of order.

**Trade-off**:
* State-correcting behavior allows violations to be tolerated with diagnostic logging. It prioritizes resilience over strict contract enforcement.

---

### Tier 4: ❌ State-Preserving (Invalid Flow)

**Definition**: A call made when preconditions or arguments are not met. The call is rejected and ignored to preserve the current valid state. An error log entry is emitted (e.g., `ILLEGAL` or `INCONSISTENT_*`).

**Behavior**:
* A warning or error is logged to alert developers that preconditions or arguments are invalid.
* The call is rejected and has no effect on the state or attributes.
* Example: `inc()` called from `Created` (logs `INCONSISTENT_INCREMENT` but does nothing), or `ok(null)` with a null path argument (logs `ILLEGAL` but does not transition).

**Guarantees**:
* The meter remains in exactly the same state (state is preserved).
* No partial or side effects occur.
* Invalid arguments never corrupt the state.
* The application continues running without exceptions.

---

## Implementation Strategy

### State Guards

Every state-affecting method implements **precondition checks** to determine which tier applies:

1. **Check if the call matches the expected state** → Tier 1 or 2 (apply the change)
2. **Check if the call is outside the expected flow** → Tier 3 (apply the change with warning)
3. **Check if the call has invalid arguments or prerequisites** → Tier 4 (ignore with error)

### Example: The `ok()` Method

```java
public void ok() {
    // Tier 1/2 check: Is the meter Started?
    if (startTime != 0 && stopTime == 0) {
        // Valid state-changing: apply the termination
        stopTime = System.currentTimeMillis();
        validateStopPrecondition(this, Markers.MSG_OK);
        // ... logging and data emission ...
        return;
    }
    
    // Tier 3 check: Is the meter Created?
    if (startTime == 0) {
        // Outside expected flow: apply the termination anyway
        stopTime = System.currentTimeMillis();
        validateStopPrecondition(this, Markers.INCONSISTENT_OK);
        // ... logging and data emission ...
        return;
    }
    
    // Tier 4 check: Is the meter already Stopped?
    if (stopTime != 0) {
        // Ignored: do nothing
        validateStopPrecondition(this, Markers.INCONSISTENT_OK);
        // ... log warning but do not change state ...
    }
}
```

### Immutability and First-Write-Wins Strategy

This resilience model rigidly enforces the **Immutable Lifecycle Transitions** defined in [TDR-0019](TDR-0019-immutable-lifecycle-transitions.md).

*   **Logic**: The `stopTime` attribute acts as a "write-once" barrier.
*   **First-Write-Wins**: The first call to any terminal method (`ok()`, `reject()`, or `fail()`) sets `stopTime` to a non-zero value.
*   **Immutability**: Once `stopTime != 0`, the categorical state of the meter (OK/Rejected/Failed) and its outcome path (`okPath`, `rejectPath`, `failPath`) become **immutable**.
*   **Thread Safety**: Since `stopTime` is a `volatile long` (or accessed via synchronized/atomic means where applicable), concurrent termination attempts are handled atomically. The winner sets the state; losers fall into **Tier 4** (State-Preserving) and are ignored (with a warning log).

This ensures that a Meter reported as "OK" cannot later change to "Failed" due to a subsequent exception handler or finally block.

### Logging and Diagnostics

* **Tier 1/2**: No log entry (normal usage).
* **Tier 3**: Logs with `INCONSISTENT_*` markers to indicate discouraged but tolerated usage.
* **Tier 4**: Logs with `ILLEGAL` or `INCONSISTENT_*` markers to indicate ignored calls.

All logging uses markers from [src/main/java/org/usefultoys/slf4j/meter/Markers.java](../src/main/java/org/usefultoys/slf4j/meter/Markers.java) for aggregation and filtering.

---

## Consequences

**Positive**:

* **Non-intrusive**: No exceptions disrupt production applications.
* **Forgiving but guided**: Incorrect usage is tolerated but flagged in logs.
* **State invariant**: The `Meter` is always in a valid state, even after misuse.
* **Debuggable**: Developers can detect their mistakes by monitoring `INCONSISTENT_*` and `ILLEGAL` markers.
* **Testable**: Test suites can verify that invalid transitions are logged correctly without throwing exceptions.
* **Contract preserved**: The recommended usage pattern is clear and works as designed; out-of-order usage is discouraged via logging but not prevented by the API.

**Negative**:

* **Silent success with logging**: Developers who don't monitor logs may not realize they're using the API incorrectly.
* **Verbose validation logic**: Every method must implement guards for all four tiers, increasing code complexity.
* **Potential for masking bugs**: Tier 3 behavior (applying the change anyway) could hide developer confusion about the intended order.
* **First-termination-wins semantics**: Multiple termination attempts (Tier 4) silently ignore the later ones, which could be surprising if a developer expects the last call to win.

**Neutral**:

* **Performance**: Guard checks are simple `if` statements and have negligible overhead.
* **Memory**: No additional state is needed to track "which tier was applied."

---

## Alternatives

### 1. **Strict Enforcement (Exceptions)**

Throw `IllegalStateException` for any out-of-order call (Tier 3 and 4 would throw).

**Rejected because**:

* Violates non-intrusive principle ([TDR-0017](TDR-0017-non-intrusive-validation-and-error-handling.md)).
* Complex control flow (try-catch-finally) would require defensive checks everywhere.
* Production applications could crash due to accidental misuse.

### 2. **No Enforcement (All Calls Succeed)**

Allow any call in any state to modify the state arbitrarily (no Tier 3 or 4 distinction).

**Rejected because**:

* State becomes unpredictable: later calls could overwrite earlier terminations.
* Logging would be inconsistent (e.g., a meter could be logged as OK and then later as FAIL).
* Thread-safety issues with concurrent calls to `ok()` and `fail()`.

### 3. **Explicit State Enum**

Introduce an explicit `enum State` and check it explicitly in each method.

**Rejected because**:

* Redundant with existing `startTime` and `stopTime` checks.
* Adds synchronization complexity (must update both enum and timestamps atomically).
* Increases code verbosity without benefit.

### 4. **Tier 2 Only (No Tier 3)**

Allow only Tier 1, 2, and 4. Tier 3 (outside expected flow) is always ignored.

**Rejected because**:

* Prevents resilience in common cases (e.g., multiple termination attempts in try-catch-finally).
* First-termination-wins becomes too strict; later legitimate recovery attempts would be ignored.
* Reduces the library's ability to report operations that end unexpectedly but successfully (via Tier 3 fallback).

---

## References

* [TDR-0017: Non-Intrusive Validation and Error Handling](TDR-0017-non-intrusive-validation-and-error-handling.md) — Explains the non-intrusive principle.
* [TDR-0019: Immutable Lifecycle Transitions](TDR-0019-immutable-lifecycle-transitions.md) — Explains first-termination-wins and state immutability.
* [TDR-0020: Three Outcome Types (OK, REJECT, FAIL)](TDR-0020-three-outcome-types-ok-reject-fail.md) — Explains the three terminal states.
* [meter-state-diagram.md](meter-state-diagram.md) — Visual diagram of all valid and invalid state transitions, organized by the four tiers.
* [src/main/java/org/usefultoys/slf4j/meter/Meter.java](../src/main/java/org/usefultoys/slf4j/meter/Meter.java) — Implementation of lifecycle methods with tier guards.
* [src/main/java/org/usefultoys/slf4j/meter/MeterValidator.java](../src/main/java/org/usefultoys/slf4j/meter/MeterValidator.java) — Validation logic for each tier.
* [src/main/java/org/usefultoys/slf4j/meter/Markers.java](../src/main/java/org/usefultoys/slf4j/meter/Markers.java) — SLF4J markers for logging validation warnings.
