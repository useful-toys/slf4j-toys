# TDR-0021: Path Identifiers for Outcome Flows

**Status**: Accepted
**Date**: 2026-01-04

## Context

Even when an operation ends with the same high-level outcome (OK or REJECT), it can reach that outcome through different internal control flows.

Examples:

* OK can mean: fast path vs. slow path, cache hit vs. fallback execution, full processing vs. partial processing.
* REJECT can mean: validation rejected, authorization rejected, precondition not met.

Without a way to distinguish these flows, logs provide only the terminal class (OK/REJECT/FAIL) but not the “why” for the specific flow.

## Decision

We introduce the concept of **path identifiers**. A path is a short, stable identifier used to distinguish which control flow led to an outcome.

### OK paths

OK can optionally carry a path identifier:

* `path(pathId)` sets the OK path to be used by a later `ok()`.
* `ok(pathId)` sets the OK path and terminates immediately.
* `success(pathId)` is an alias of `ok(pathId)`.

If `ok()` (or `success()`) is called **without** specifying a path, then no explicit `okPath` is recorded. This absence is treated as an **implicit default OK path** (the “happy path”), meaning:

* the operation completed successfully through the expected, standard success flow, and
* no additional flow distinction was needed.

This is intentionally represented as `okPath == null` to avoid forcing callers to invent a name for the default flow.

### REJECT paths

REJECT always records a path identifier:

* `reject(cause)` records `rejectPath` derived from the provided cause.

REJECT represents an expected, business-driven termination, so a reason/path is required. In practice, a reject is often associated with a domain or business exception that interrupts the normal flow (e.g., validation rejected, authorization rejected, precondition not met).

### FAIL paths

FAIL records a path identifier:

* `fail(cause)` records `failPath` derived from the provided cause.

For `Throwable` causes, `failPath` is derived from the exception class name and `failMessage` stores the exception message.

### Path encoding

Path identifiers are strings derived from the input object:

* `String` → as-is
* `Enum` → `name()`
* `Throwable` → class name (simple name for OK/REJECT, fully qualified name for FAIL)
* other objects → `toString()`

## Consequences

**Positive**:

* **Flow-level observability**: Enables aggregation of OK and REJECT flows without introducing extra outcome categories.
* **Stable reporting vocabulary**: Encourages using short identifiers for analytics and dashboards.
* **No forced defaults**: Callers can omit OK path to represent the default happy flow.

**Neutral**:

* **Requires conventions**: Teams must choose consistent names for paths.
	* This is not necessarily a downside: it encourages identifying and documenting flows in advance (including reject flows that are expressed via business exceptions), creates a culture of deliberate planning, and results in more organized log messages.

**Negative**:

* **Security risk if misused**: `toString()` may expose sensitive data if path IDs are built from objects that contain secrets.

## Alternatives

* **Model each OK/REJECT flow as a distinct outcome type**: Rejected because it explodes the outcome taxonomy.
* **Always require OK path**: Rejected because it forces noise for the default success flow.

## Implementation

* `Meter.path(Object)` sets `okPath`.
* `Meter.ok(Object)` sets `okPath` and terminates.
* `Meter.ok()` terminates without setting `okPath` (implicit default).
* `Meter.reject(Object)` sets `rejectPath`.
* `Meter.fail(Object)` sets `failPath` and (for `Throwable`) sets `failMessage`.
* `MeterAnalysis.getPath()` returns `failPath`, `rejectPath`, or `okPath` in that precedence order.

## References

* [src/main/java/org/usefultoys/slf4j/meter/Meter.java](../src/main/java/org/usefultoys/slf4j/meter/Meter.java)
* [src/main/java/org/usefultoys/slf4j/meter/MeterAnalysis.java](../src/main/java/org/usefultoys/slf4j/meter/MeterAnalysis.java)
* [TDR-0020: Three Outcome Types (OK, REJECT, FAIL)](TDR-0020-three-outcome-types-ok-reject-fail.md)
* [TDR-0006: Security Considerations in Diagnostic Reporting](TDR-0006-security-considerations-in-diagnostic-reporting.md)
