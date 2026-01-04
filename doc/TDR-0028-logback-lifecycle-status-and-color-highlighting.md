# TDR-0028: Logback Lifecycle Status and Color Highlighting

**Status**: Accepted
**Date**: 2026-01-04

## Context

In production logs, the SLF4J/Logback level (TRACE/DEBUG/INFO/WARN/ERROR) is necessary but often not sufficient to understand the operational meaning of a line.

For `Meter`, many log lines represent **lifecycle events**:

* START
* PROGRESS
* OK (including "slow OK")
* REJECT
* FAIL

These lifecycle labels are orthogonal to the log level:

* OK is usually INFO, but may be WARN when slow.
* FAIL is ERROR.
* PROGRESS is INFO.
* START is DEBUG.

Operators benefit when lifecycle events are visible as a first-class signal in Logback layouts (as an additional label), not only implied by the message content.

Additionally, when logs are consumed directly in terminals, applying consistent colors for lifecycle events improves scanability.

## Decision

We enrich Logback output with **lifecycle status labels** derived from SLF4J markers.

### Status label enrichment

We implement a Logback pattern converter that maps SLF4J markers to a concise status string:

* For lifecycle message markers (`MSG_*`), the converter emits labels such as `START`, `PROGR`, `OK`, `SLOW`, `REJECT`, `FAIL`.
* For machine-parsable data markers (`DATA_*`), the converter emits an empty label to keep the log clean.
* For internal inconsistency markers, the converter emits a label such as `INCONSISTENT`.
* If a marker is not mapped, the converter falls back to the event log level.

This makes lifecycle information appear as a **specialized option** in Logback patterns without changing SLF4J semantics.

### Terminal color highlighting

We also provide Logback converters that colorize messages based on markers:

* `StatusHighlightConverter` colors the status (or the whole pattern segment it wraps) using distinct colors per lifecycle status.
* `MessageHighlightConverter` colorizes the message segment to differentiate lifecycle messages, data messages, and inconsistencies.

This is intended for terminal consumption and does not alter log content semantics.

## Consequences

**Positive**:

* **Faster triage**: operators can visually pick out FAIL/REJECT/OK/PROGRESS/START.
* **More expressive layouts**: status becomes a dedicated pattern field instead of being encoded in free-text messages.
* **Better signal separation**: data logs (TRACE, machine-parsable) can be intentionally de-emphasized.

**Negative**:

* **Logback-specific**: this enhancement is not portable to other SLF4J backends.
* **Requires pattern configuration**: users must opt in by adding converters to their Logback configuration.

## Implementation

* `StatusConverter` maps `Markers.MSG_*`, `Markers.DATA_*`, and inconsistency markers to status strings.
* `StatusHighlightConverter` assigns ANSI colors based on markers and falls back to level-based coloring.
* `MessageHighlightConverter` assigns ANSI colors based on markers.

## References

* [src/main/java/org/usefultoys/slf4j/logback/StatusConverter.java](../src/main/java/org/usefultoys/slf4j/logback/StatusConverter.java)
* [src/main/java/org/usefultoys/slf4j/logback/StatusHighlightConverter.java](../src/main/java/org/usefultoys/slf4j/logback/StatusHighlightConverter.java)
* [src/main/java/org/usefultoys/slf4j/logback/MessageHighlightConverter.java](../src/main/java/org/usefultoys/slf4j/logback/MessageHighlightConverter.java)
* [src/main/java/org/usefultoys/slf4j/meter/Markers.java](../src/main/java/org/usefultoys/slf4j/meter/Markers.java)
* [TDR-0020: Three Outcome Types (OK, REJECT, FAIL)](TDR-0020-three-outcome-types-ok-reject-fail.md)
