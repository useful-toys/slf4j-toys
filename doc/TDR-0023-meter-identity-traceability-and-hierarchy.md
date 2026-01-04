# TDR-0023: Meter Identity, Traceability, and Hierarchy

**Status**: Accepted
**Date**: 2026-01-04

## Context

Operational logs are only useful if individual executions can be correlated over time.

For `Meter` events, correlation must answer:

* Which operation is being measured?
* Which execution instance is this (first run, second run, …)?
* Is this a sub-operation of another operation?

Without stable identity, log aggregation becomes ambiguous, especially when multiple operations run concurrently or repeatedly.

In modern deployments the same application is often replicated across multiple JVM processes (e.g., containers/pods behind a load balancer). When logs are centralized, identity must also help distinguish which process produced which events.

## Decision

A `Meter` execution is identified by a combination of:

* **Session UUID**: a short UUID for the current process/session.
* **Category**: derived from the logger name.
* **Operation**: an optional sub-identifier under the category.
* **Position**: a monotonically increasing counter per (category[/operation]) key.

The resulting identity is represented as:

* `category#position` when `operation` is absent, or
* `category/operation#position` when `operation` is present.

### Thread-safe position generation

The `position` counter is generated in a thread-safe way. Because meters can be created concurrently, the counter must be safe under concurrent access.

The counter is JVM-wide (within a single process) and keyed per operation (category[/operation]) so that positions are sequential for each operation key.

### Hierarchy (parent and sub-operations)

A `Meter` may create a **sub-operation** using `sub(suboperationName)`.

* The sub-operation inherits the category.
* The sub-operation records the parent’s full ID in `parent`.
* The sub-operation name is composed using `/` to preserve a stable hierarchy.

This enables representing large tasks as:

* one top-level meter, and
* multiple sub-meters for internal phases.

## Consequences

**Positive**:

* **Strong correlation**: each execution instance has a unique, time-ordered identity.
* **Supports nested operations**: parent/sub relationships are explicit and machine-parsable.
* **Good log readability**: IDs are compact and stable for dashboards.
* **Encourages intentional process mapping**: consistent naming of operations and sub-operations nudges teams to explicitly map, identify, and document their processes, resulting in more structured and understandable logs.
* **Thread-safe identity under concurrency**: `position` generation is safe when meters are created in multiple threads.
* **Cross-process correlation**: the session UUID helps distinguish events from different JVM processes in centralized logs and enables grouping events that came from the same process instance.

**Negative**:

* **In-memory counters**: positions are tracked per JVM process; they are not globally unique across restarts.
* **Noisy hierarchy if inconsistent**: if sub-operation naming is inconsistent, the hierarchy and aggregated reporting can become fragmented.

## Alternatives

* **Only UUIDs**: Rejected because they are less readable and harder to scan in logs.
* **Only timestamps**: Rejected because they are not stable identifiers and are harder to correlate.

## Implementation

* `Meter` uses `Session.shortSessionUuid()` as the session UUID.
* A per-operation counter (`EVENT_COUNTER`) generates `position` for each (category[/operation]) key using a `ConcurrentMap` of `AtomicLong` to ensure thread safety.
* `MeterData.getFullID()` formats the full ID.
* `Meter.sub(String)` creates a child meter and stores the parent ID.

## References

* [src/main/java/org/usefultoys/slf4j/meter/Meter.java](../src/main/java/org/usefultoys/slf4j/meter/Meter.java)
* [src/main/java/org/usefultoys/slf4j/meter/MeterData.java](../src/main/java/org/usefultoys/slf4j/meter/MeterData.java)
* [src/main/java/org/usefultoys/slf4j/meter/MeterFactory.java](../src/main/java/org/usefultoys/slf4j/meter/MeterFactory.java)
* [TDR-0015: ThreadLocal Stack for Context Propagation](TDR-0015-threadlocal-stack-for-context-propagation.md)
