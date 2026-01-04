# TDR-0026: Progress Policy (Throttling and Slowness Signaling)

**Status**: Accepted
**Date**: 2026-01-04

## Context

Progress reporting is valuable for long-running operations, but it has two conflicting requirements:

* **Visibility**: operators want periodic “still running” signals.
* **Efficiency**: progress logging must not flood logs or degrade performance.

Additionally, the library should help detect operations that are slow relative to an expected time limit.

## Decision

Progress reporting is governed by two policies:

1. **Time-based throttling**
   * `progress()` only emits a progress log periodically.
   * The period is configured by `MeterConfig.progressPeriodMilliseconds`.

2. **Work-based gating**
   * `progress()` only emits a progress log when progress has advanced (i.e., `currentIteration` increased since the last progress emission).

### Slowness signaling

`Meter` distinguishes “slow” behavior relative to a configured time limit:

* If a time limit is set (`limitMilliseconds(...)`) and the operation exceeds it:
  * OK termination logs at WARN with a “slow ok” marker.
  * Progress data logs can use a dedicated “slow progress” marker.

This allows systems to route slow-event logs differently from normal events.

## Consequences

**Positive**:

* **Reduces log flooding**: time-based throttling and iteration gating keep progress logs cheap.
* **Makes “slow” visible**: slow completion and slow progress can be routed/aggregated.

**Negative**:

* **Progress requires correct increments**: without `inc*` calls, there is no progress advancement to report.
* **Not a precise ETA**: the library reports progress state, but does not estimate completion time.

## Alternatives

* **Emit progress on every `progress()` call**: Rejected due to log flooding.
* **Only time-based throttling**: Rejected because it can still emit repeated progress logs without work advancement.

## Implementation

* `Meter.progress()` checks:
  * `currentIteration > lastProgressIteration`, and
  * `(now - lastProgressTime) > MeterConfig.progressPeriodMilliseconds`.
* `Meter.ok()` may log WARN if the operation exceeded the configured time limit.

## References

* [src/main/java/org/usefultoys/slf4j/meter/Meter.java](../src/main/java/org/usefultoys/slf4j/meter/Meter.java)
* [src/main/java/org/usefultoys/slf4j/meter/MeterConfig.java](../src/main/java/org/usefultoys/slf4j/meter/MeterConfig.java)
* [src/main/java/org/usefultoys/slf4j/meter/Markers.java](../src/main/java/org/usefultoys/slf4j/meter/Markers.java)
* [TDR-0022: Progress Model (Iterations and Increments)](TDR-0022-progress-model-iterations-and-increments.md)
