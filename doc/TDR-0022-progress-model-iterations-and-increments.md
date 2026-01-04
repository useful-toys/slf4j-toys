# TDR-0022: Progress Model (Iterations and Increments)

**Status**: Accepted
**Date**: 2026-01-04

## Context

Many important operations are long-running and include internal loops or a sequence of steps. For those operations, a single start/end log is often insufficient:

* Operators need to see that the process is still alive.
* Developers need to correlate performance behavior with how much work has already been done.
* Higher-level orchestration code may need a lightweight way to express progress (for diagnostics and operational visibility) without introducing a new progress-tracking subsystem. This is not intended to be a mechanism for communicating progress to end users.

However, progress reporting must also remain lightweight and should not flood logs.

## Decision

The `Meter` supports a progress model based on **iterations** and **increments**:

1. A caller may provide an **expected total** amount of work via `iterations(expectedIterations)`.
2. As work is performed, the caller updates the current progress counter using:
   * `inc()` for “one more unit of work completed”,
   * `incBy(increment)` for “N more units completed”, or
   * `incTo(currentIteration)` for “we are now at absolute position X”.
3. The progress state can be reported using `progress()`.

The unit of work is intentionally generic:

* “iterations” can represent loop cycles (e.g., items processed), or
* “steps” can represent discrete workflow phases (e.g., stage 3 of 10).

### Progress emission is explicit (no background timer)

Progress messages are emitted only when `progress()` is called programmatically. `Meter` does not run a background task to generate periodic progress logs on its own.

To avoid flooding logs when progress advances quickly, `progress()` is throttled and gated:

* **Time-based throttling**: `progress()` only logs after a minimum time interval has elapsed (`MeterConfig.progressPeriodMilliseconds`).
* **Work-based gating**: `progress()` only logs if progress has advanced since the last emitted progress message.

As a consequence:

* `inc()`, `incBy(...)`, and `incTo(...)` update counters but do not emit log messages by themselves.
* Consecutive emitted progress messages can represent multiple increments that happened between them.
* Progress output includes rate information (iterations per second and time per iteration) once the execution time justifies progress reporting.

### Dynamic totals

The expected total can be refined during execution. This supports cases where the operation discovers new information and adjusts the estimated work. Practically, this means:

* calling `iterations(expectedIterations)` again can update the expected value,
* the expected total may increase or decrease as better estimates become available.

## Consequences

**Positive**:

* **Cheap progress tracking**: no new types beyond `Meter` are required.
* **Supports coarse and fine-grained progress**: works with both loops and step-based workflows.
* **Handles evolving estimates**: expected totals can be refined as the operation learns more.

**Negative**:

* **No enforcement of semantics**: the library cannot know what a unit means; consistency is up to the caller.
* **Misleading progress is possible**: incorrect increments or unstable estimates can confuse operators.

## Alternatives

* **Separate progress API**: Rejected because it increases surface area and integration complexity.
* **Force a fixed total**: Rejected because many operations do not know the total up-front.

## Implementation

* `Meter.iterations(long)` sets `expectedIterations`.
* `Meter.inc()`, `Meter.incBy(long)`, `Meter.incTo(long)` update `currentIteration`.
* `Meter.progress()` is the method that emits progress logs (and it is throttled by `MeterConfig.progressPeriodMilliseconds`).
* Validation is advisory and logs illegal calls (e.g., non-positive increments) without throwing.

## References

* [src/main/java/org/usefultoys/slf4j/meter/Meter.java](../src/main/java/org/usefultoys/slf4j/meter/Meter.java)
* [src/main/java/org/usefultoys/slf4j/meter/MeterValidator.java](../src/main/java/org/usefultoys/slf4j/meter/MeterValidator.java)
* [TDR-0026: Progress Policy (Throttling and Slowness Signaling)](TDR-0026-progress-policy-throttling-and-slowness.md)
