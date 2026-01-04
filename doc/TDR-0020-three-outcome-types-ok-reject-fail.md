# TDR-0020: Three Outcome Types (OK, REJECT, FAIL)

**Status**: Accepted
**Date**: 2026-01-04

## Context

A `Meter` models the lifecycle of an operation. When the operation finishes, it must be classified in a way that is useful for both:

* **Observability** (what happened and how often), and
* **Diagnosis** (what should developers investigate).

A binary outcome model (success vs. failure) is insufficient because many operations intentionally end without producing the desired business result. Those cases are not “errors” in the technical sense.

Examples:

* **Expected business termination**: Authentication denied, validation rejected, a cache miss, a user cancels a flow.
* **Unexpected technical error**: `IOException`, `NullPointerException`, database timeouts.

Without a distinct classification, logs become noisy and misleading:

* Expected outcomes look like errors and inflate alert rates.
* Real technical problems are drowned out by “expected failures”.

## Decision

The `Meter` defines **three** terminal outcomes:

1. **OK**: The operation completed successfully.
2. **REJECT**: The operation terminated **as expected** due to a business rule or expected condition (an unsuccessful but non-exceptional outcome).
3. **FAIL**: The operation terminated due to an **unexpected technical error**.

The API reflects this separation:

* `ok()` / `ok(pathId)` (and aliases `success()` / `success(pathId)`) for OK.
* `reject(cause)` for REJECT.
* `fail(cause)` for FAIL.

The model is also reflected in the data attributes:

* `okPath`, `rejectPath`, and `failPath` are mutually exclusive.

### REJECT: expected alternative flow or business exception

REJECT covers outcomes that are **not the desired business result**, but are still **expected** and usually **actionable without investigation**.

In practice, REJECT commonly appears in two forms:

* **Alternative expected flow**: the operation selects a different, valid branch (e.g., feature disabled, precondition not met, idempotency detected a duplicate and short-circuited, cache miss that intentionally ends the flow).
* **Interruption via a business/domain exception**: a domain rule is enforced by throwing an exception that is considered expected by the caller (e.g., validation exception, authorization exception, business rule violation, insufficient funds, duplicate request).

### FAIL: unexpected condition or unexpected exception

FAIL covers outcomes that indicate a **technical problem** or an **unexpected situation** that should usually trigger investigation.

In practice, FAIL commonly appears in two forms:

* **Unexpected condition detected by the operation**: the code detects an internal inconsistency or invariant violation (e.g., impossible state, missing mandatory data, unexpected null, corrupted input that should have been validated earlier).
* **Unexpected exception thrown by application code or a framework dependency**: runtime bugs or infrastructure problems (e.g., `NullPointerException`, `IllegalStateException`, `IOException`, timeouts, database driver errors, servlet container failures).

### Time limits and slow signaling

In addition to classifying the outcome, operators often need to identify **performance degradation**: an operation can still end as OK but take longer than expected.

To support this, `Meter` allows configuring an expected upper bound:

* `limitMilliseconds(timeLimit)` sets an execution time threshold.

When the threshold is exceeded:

* OK termination is logged with a dedicated "slow OK" marker and elevated visibility (WARN).
* Progress data can be emitted with a dedicated "slow progress" marker.

Note: There is currently **no** dedicated "slow reject" marker or log-level variant. A rejected operation is logged as REJECT regardless of duration. This is a deliberate documentation choice to match the current library behavior.

## Consequences

**Positive**:

* **Clear operational semantics**: REJECT can be tracked and aggregated without being treated as a system error.
* **Better alerting hygiene**: FAIL indicates technical problems and should correlate with incident investigation.
* **More expressive analytics**: dashboards can separate OK rate, reject rate (business outcomes), and fail rate (technical reliability).

**Negative**:

* **Requires discipline**: call sites must decide between REJECT and FAIL consistently.
* **Potential misclassification**: if developers map business outcomes to FAIL (or technical errors to REJECT), logs and metrics become misleading.

## Alternatives

* **Binary model (success/failure)**: Rejected because it conflates expected business termination with technical errors.
* **Many outcome categories**: Rejected because it complicates the API and makes aggregation harder.

## Implementation

* `Meter.ok(...)`, `Meter.reject(...)`, `Meter.fail(...)` implement the terminal state and the mutually exclusive `*Path` fields.
* Logging levels are aligned with semantics:
  * OK is logged at INFO (or WARN when slow).
  * REJECT is logged at INFO.
  * FAIL is logged at ERROR.
* Slowness is controlled by `limitMilliseconds(...)`:
  * Slow OK is signaled via `Markers.MSG_SLOW_OK` (message) and `Markers.DATA_SLOW_OK` (data).
  * Slow progress is signaled via `Markers.DATA_SLOW_PROGRESS` (data).

## References

* [src/main/java/org/usefultoys/slf4j/meter/Meter.java](../src/main/java/org/usefultoys/slf4j/meter/Meter.java)
* [src/main/java/org/usefultoys/slf4j/meter/MeterData.java](../src/main/java/org/usefultoys/slf4j/meter/MeterData.java)
* [TDR-0017: Non-Intrusive Validation and Error Handling](TDR-0017-non-intrusive-validation-and-error-handling.md)
* [TDR-0019: Immutable Lifecycle Transitions](TDR-0019-immutable-lifecycle-transitions.md)
