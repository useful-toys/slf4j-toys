# TDR-0033: Derived State from Attributes

**Status**: Accepted
**Date**: 2026-01-15

## Context
The `Meter` class represents the lifecycle of an operation, transitioning through states such as `Created`, `Started`, and terminating in `OK`, `Rejected`, or `Failed`. 

A common design pattern for stateful objects is to maintain an explicit `status` attribute (e.g., an Enum with values `CREATED`, `STARTED`, `OK`, `REJECTED`, `FAILED`). However, the `Meter` also tracks other essential data associated with these states:
*   `startTime`: When the operation transitioned to `Started`.
*   `stopTime`: When the operation transitioned to a terminal state.
*   `okPath`, `rejectPath`, `failPath`: The specific outcome details.

Maintaing an explicit `status` attribute alongside these data fields introduces "Double Maintenance" and the risk of inconsistent states (e.g., `status = STARTED` but `stopTime > 0`, or `status = OK` but `okPath` is null).

## Decision
We do not use an explicit `Status` attribute or Enum to track the lifecycle state of a `Meter`. Instead, the state is **derived** (inferred) directly from the values of `startTime`, `stopTime`, and the outcome path attributes (`okPath`, `rejectPath`, `failPath`).

### State Inference Logic

The state is determined by examining the attributes in the following order:

1.  **Created**: `startTime == 0` AND `stopTime == 0`
    *   The meter has been instantiated but not yet started.
2.  **Started**: `startTime > 0` AND `stopTime == 0`
    *   The meter is currently running.
3.  **Terminated (OK/Reject/Fail)**: `stopTime > 0`
    *   The specific terminal state is determined by which path attribute is set (mutually exclusive by design):
    *   **OK**: `okPath != null` (Success)
    *   **Rejected**: `rejectPath != null` (Business exception/rejection)
    *   **Failed**: `failPath != null` (Technical failure)

*Note: If `startTime == 0` but `stopTime > 0`, the Meter is considered terminated (likely auto-corrected via Tier 3 resilience, treating `startTime` as implicit).*

## Consequences

**Positive**:
*   **Single Source of Truth**: The state is defined solely by the presence of data. It is impossible to have a "ghost state" where the status says one thing but the data says another.
*   **Simplified Serialization**: The `MeterData` DTO is flat and simple. There is no need to serialize a redundant state enum.
*   **Memory Efficiency**: Eliminates the overhead of storing an additional object reference for the status enum.
*   **Atomic Transitions (mostly)**: Setting `stopTime` effectively freezes the state for external observers (assuming volatile visibility), and setting the path attributes completes the detail.

**Negative**:
*   **Implicit Logic**: Code that needs to check the state must perform boolean checks on timestamps rather than a simple `switch` on an enum.
*   **Validation Complexity**: The validation logic (`MeterValidator`) acts as the guardian of consistency, ensuring that we don't end up with invalid combinations (e.g., `stopTime > 0` but all paths are null).
*   **Documentation Requirement**: The usage of 0 as a sentinel value for timestamps must be clearly understood by developers.

## Alternatives
*   **Explicit Enum**: Defined a `MeterState` enum. **Rejected** to avoid data redundancy and synchronization issues. The `MeterState.java` file may exist in the codebase as a vestige or reserved for future helper logic, but acts as a DTO/Logic separation boundary rather than the source of truth.

## References
*   [TDR-0019: Immutable Lifecycle Transitions](TDR-0019-immutable-lifecycle-transitions.md)
*   [TDR-0020: Three Outcome Types](TDR-0020-three-outcome-types-ok-reject-fail.md)
*   [TDR-0007: Separation of Data and Behavior](TDR-0007-separation-of-data-and-behavior-vo-pattern.md)

