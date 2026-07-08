# TDR-0038: Accept Per-Instance Watcher Position Sequences

**Status**: Accepted  
**Date**: 2026-07-08

## Context

[TDR-0036](./TDR-0036-replace-watcher-singleton-push-with-controllers.md) and [TDR-0037](./TDR-0037-migrate-watcher-servlet-pull-from-singleton.md) replaced the shared `WatcherSingleton` with instance-based owners: `WatcherExecutorController`, `WatcherTimerController`, `WatcherServlet`, and `WatcherJavaxServlet`. Each owner now creates its own `Watcher` instance via `new Watcher(name)`.

`Watcher` extends `WatcherData`, which extends `SystemData`, which extends `EventData`. `EventData` carries a `position` field that is incremented by `nextPosition()` on every `run()`. The Javadoc historically described `position` as *"a time-ordered sequential position for multiple occurrences of the same event within a session"*. In the singleton model this effectively meant one sequence per JVM/session. In the own-instance model the counter is **per `Watcher` instance**, while the destination logger (derived from `name` plus `WatcherConfig` prefixes/suffixes) and the `sessionUuid` are shared by all instances that use the same `name`.

If an application combines, for example, a `WatcherExecutorController.create()` and a `WatcherServlet` with the default `WatcherConfig.name` (`"watcher"`), the same logger receives two independent position sequences under the same `sessionUuid`. A downstream consumer using `(sessionUuid, position)` as a unique or ordering key would see collisions and apparent regressions in the sequence.

A related rigidity was the `WatcherConfig.dataEnabled` flag, which was evaluated once in the `Watcher` constructor and, when `false`, permanently assigned `NullLogger.INSTANCE` to the data logger. Changing `dataEnabled` at runtime had no effect on existing instances. That flag was removed; data logging is now controlled exclusively by the SLF4J logger level (the same model used by `Meter`).

## Decision

Keep the position counter per-instance. Do **not** introduce a global/shared counter per name, and do **not** add an instance discriminator to the event.

The project prioritizes:

*   **Minimalism**: a shared counter (`ConcurrentHashMap<String, AtomicLong>`, static state, or an instance registry) would reintroduce global mutable state that the own-instance migration explicitly removed.
*   **Performance**: a global counter would become a contention point for every watcher execution in the JVM.
*   **Explicit ownership**: the application controls how many watchers exist and what they are named; making that ownership visible is preferable to hiding it behind shared infrastructure.

The accepted trade-off is documented as a user responsibility: if a single ordered position sequence per logical watcher is required, the application must ensure only one `Watcher` instance uses that name.

## Consequences

**Positive**:

*   **No global state**: no shared counter, registry, or lock is needed for watcher execution.
*   **No performance bottleneck**: each instance increments its own field without coordination.
*   **Clear ownership**: the number of sequences equals the number of watcher instances, which the application explicitly creates.

**Negative / Accepted Risk**:

*   Two `Watcher` instances with the same `name` write interleaved `position` sequences to the same logger under the same `sessionUuid`. Downstream consumers that rely on `(sessionUuid, position)` uniqueness or monotonicity must be configured to use a unique name per instance, or the application must ensure only one instance uses each name.

## Alternatives Considered

*   **Global counter per name (`ConcurrentHashMap<String, AtomicLong>`)**: rejected because it reintroduces static shared state and contention, contradicting the own-instance design goals of TDR-0036/0037.
*   **Instance discriminator in the event**: rejected because it would change the event schema and increase complexity for a corner case that is better addressed by application-level naming discipline.
*   **Document only (this decision)**: accepted. Javadoc, README, wiki, and TDRs now state that `position` is per-instance and that duplicate names produce interleaved sequences.

## Implementation

*   Updated `src/main/java/org/usefultoys/slf4j/internal/EventData.java` Javadoc for `position`, constructors, and `nextPosition()`.
*   Updated `src/main/java/org/usefultoys/slf4j/watcher/Watcher.java` class-level Javadoc and removed the `dataEnabled`/snapshot branch; the data logger is now always created and data emission is controlled by `isTraceEnabled()`.
*   Updated `src/main/java/org/usefultoys/slf4j/watcher/WatcherConfig.java` to remove the `dataEnabled` field and `slf4jtoys.watcher.data.enabled` property.
*   Updated `src/main/java/org/usefultoys/slf4j/watcher/WatcherExecutorController.java` and `WatcherTimerController.java` class-level Javadoc.
*   Updated `src/main/java/org/usefultoys/slf4j/watcher/WatcherServlet.java` and `WatcherJavaxServlet.java` class-level Javadoc.
*   Updated `README.md` Watcher section.
*   Updated wiki pages describing watcher use cases.
*   Cross-referenced this decision from [TDR-0036](./TDR-0036-replace-watcher-singleton-push-with-controllers.md) and [TDR-0037](./TDR-0037-migrate-watcher-servlet-pull-from-singleton.md).

## References

*   [src/main/java/org/usefultoys/slf4j/internal/EventData.java](../src/main/java/org/usefultoys/slf4j/internal/EventData.java)
*   [src/main/java/org/usefultoys/slf4j/watcher/Watcher.java](../src/main/java/org/usefultoys/slf4j/watcher/Watcher.java)
*   [src/main/java/org/usefultoys/slf4j/watcher/WatcherExecutorController.java](../src/main/java/org/usefultoys/slf4j/watcher/WatcherExecutorController.java)
*   [src/main/java/org/usefultoys/slf4j/watcher/WatcherTimerController.java](../src/main/java/org/usefultoys/slf4j/watcher/WatcherTimerController.java)
*   [src/main/java/org/usefultoys/slf4j/watcher/WatcherServlet.java](../src/main/java/org/usefultoys/slf4j/watcher/WatcherServlet.java)
*   [src/main/java/org/usefultoys/slf4j/watcher/WatcherJavaxServlet.java](../src/main/java/org/usefultoys/slf4j/watcher/WatcherJavaxServlet.java)
*   [doc/TDR-0036-replace-watcher-singleton-push-with-controllers.md](./TDR-0036-replace-watcher-singleton-push-with-controllers.md)
*   [doc/TDR-0037-migrate-watcher-servlet-pull-from-singleton.md](./TDR-0037-migrate-watcher-servlet-pull-from-singleton.md)
