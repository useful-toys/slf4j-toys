# TDR-0027: Context as Event Metadata and Post-Emission Clearing

**Status**: Accepted
**Date**: 2026-01-04

## Context

Operational diagnostics often require more than timing and status:

* input parameters and key decisions,
* identifiers that allow correlating with external systems,
* relevant results or intermediate values.

A `Meter` should support attaching such metadata without requiring new types or a separate logging framework.

At the same time, context must remain lightweight and should not accumulate unbounded state.

## Decision

A `Meter` maintains a **context map** (key-value pairs) as **event metadata**.

Context is intended for:

* operation parameters,
* decisions and control-flow flags,
* return values (including automatic `result` integration in some wrappers).

### Context population

Call sites can add context via:

* `ctx(...)` convenience overloads (from `MeterContext`), or
* direct context storage (`putContext(...)`).

Values are stored using `toString()` (with `<null>` placeholders for `null`).

### Post-emission clearing (delta context)

After emitting each lifecycle log message (e.g., `start()`, `progress()`, `ok()`, `reject()`, `fail()`, `close()`), the meter clears the context.

This means:

* Each lifecycle message includes **only the context added since the previous emitted message**.
* For an operation that emits multiple messages (START, PROGRESS, OK/REJECT/FAIL), the **overall context for the whole operation is the sum of the context deltas** across messages.

This design reduces payload size per message and encourages adding context close to the event it explains.

### Security considerations

Context is written to logs. Therefore:

* **Do not place sensitive data** (passwords, tokens, secrets, personal data) into meter context.
* Because values are rendered via `toString()`, implementations must ensure `toString()` does not reveal secrets.

## Consequences

**Positive**:

* **High diagnostic value**: context can capture parameters and results with minimal friction.
* **Keeps messages small**: delta clearing prevents repeated logging of all previous context.
* **Supports progressive enrichment**: context can be added as the operation advances.

**Negative**:

* **Requires caller awareness**: context is not durable across messages; if callers want the same key to appear again, they must re-add it.
* **Risk of sensitive leakage**: misuse of `toString()` and context keys can expose secrets.

## Alternatives

* **Never clear context**: Rejected because it would repeat the full accumulated context on each progress/termination message and grow unbounded.
* **Separate per-event context objects**: Rejected because it would add complexity and more allocation.

## Implementation

* `MeterData.putContext(...)` stores string values via `toString()`.
* `Meter` clears context after emitting logs in lifecycle methods.
* `MeterContext` provides convenience methods for adding and removing context (`ctx(...)`, `unctx(...)`).

## References

* [src/main/java/org/usefultoys/slf4j/meter/Meter.java](../src/main/java/org/usefultoys/slf4j/meter/Meter.java)
* [src/main/java/org/usefultoys/slf4j/meter/MeterData.java](../src/main/java/org/usefultoys/slf4j/meter/MeterData.java)
* [src/main/java/org/usefultoys/slf4j/meter/MeterContext.java](../src/main/java/org/usefultoys/slf4j/meter/MeterContext.java)
* [TDR-0006: Security Considerations in Diagnostic Reporting](TDR-0006-security-considerations-in-diagnostic-reporting.md)
