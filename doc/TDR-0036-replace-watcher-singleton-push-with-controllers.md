# TDR-0036: Replace WatcherSingleton Push with Instance-Based Controllers

**Status**: Accepted
**Date**: 2026-07-07

## Context

`WatcherSingleton` (TDR-0012) provided a global default `Watcher` and static methods to start/stop periodic push execution via `ScheduledExecutorService` or `Timer`. This design created well-documented problems:

*   **Configuration rigidity**: the cached `Watcher` instance captured `WatcherConfig` values at first access, ignoring later changes (`.glm-findings/02`).
*   **Concurrency race**: the same `Watcher` instance could be invoked concurrently by the executor thread, the timer thread and HTTP servlet threads (`.glm-findings/03`).
*   **Lifecycle leaks**: the `Timer` was non-daemon, blocking JVM shutdown if `stopDefaultWatcherTimer()` was not called (`.glm-findings/05`).
*   **Hidden global state**: callers depended on a static singleton instead of an explicit instance (TDR-0005, TDR-0012).

## Decision

Replace the push side of `WatcherSingleton` with two first-class, instance-based controllers:

*   `WatcherExecutorController` — uses `ScheduledExecutorService`.
*   `WatcherTimerController` — uses `Timer`.

Each controller:

*   Owns its own `Watcher` instance, created at construction time from the configured `name`.
*   Reads defaults from `WatcherConfig` (`name`, `delayMilliseconds`, `periodMilliseconds`) when the builder or `create()` is invoked (late creation).
*   Provides `create()` static shortcuts and a fluent `Builder`.
*   Implements `AutoCloseable` for safe lifecycle management.
*   Uses a daemon thread named after the watcher, fixing the shutdown-blocking issue.

`WatcherSingleton` is reduced to the minimum needed by `WatcherServlet`/`WatcherJavaxServlet` (pull): only `getDefaultWatcher()` remains, marked `@Deprecated`.

## Consequences

**Positive**:

*   **Explicit lifecycle**: the application owns controller instances, eliminating hidden global-state dependencies.
*   **Testability**: each test creates independent controllers; there is no shared singleton state to leak.
*   **Configuration flexibility**: `WatcherConfig` can be changed before building a controller; each controller snapshots the config values it needs.
*   **Race elimination**: push controllers no longer share a `Watcher` instance with each other or with the servlet pull path.
*   **Resource safety**: daemon threads and `AutoCloseable` reduce the risk of blocking shutdown or leaking schedulers.

**Negative / Breaking**:

*   Callers of `WatcherSingleton.startDefaultWatcherExecutor()` and `WatcherSingleton.startDefaultWatcherTimer()` must migrate to `WatcherExecutorController.create().start()` / `WatcherTimerController.create().start()`. This is an intentional breaking change for a deprecated API.

## Alternatives Considered

*   **Keep `WatcherSingleton` as an adapter delegating to controllers**: rejected because the user requested removing the maximum from `WatcherSingleton`; keeping dead push methods would perpetuate the deprecated API.
*   **Introduce a common `WatcherController` interface**: rejected; the two controllers are simple enough to remain standalone `AutoCloseable` classes.
*   **Use Lombok `@Builder`**: attempted. Lombok 1.18.46 cannot cleanly exclude internal lifecycle fields (`executor`, `task`, `timer`, `timerTask`) from the generated builder, nor does it support default values on constructor parameters in this Java-8-compatible codebase. A hand-written `Builder` was chosen instead; it remains fluent and captures `WatcherConfig` defaults at builder-creation time.

## Implementation

*   New classes: `src/main/java/org/usefultoys/slf4j/watcher/WatcherExecutorController.java`, `WatcherTimerController.java`.
*   Reduced class: `src/main/java/org/usefultoys/slf4j/watcher/WatcherSingleton.java`.
*   New tests: `src/test/java/org/usefultoys/slf4j/watcher/WatcherExecutorControllerTest.java`, `WatcherTimerControllerTest.java`.
*   Slimmed test: `src/test/java/org/usefultoys/slf4j/watcher/WatcherSingletonTest.java`.
*   Documentation: updated `README.md` and this TDR.

## References

*   [src/main/java/org/usefultoys/slf4j/watcher/WatcherExecutorController.java](../src/main/java/org/usefultoys/slf4j/watcher/WatcherExecutorController.java)
*   [src/main/java/org/usefultoys/slf4j/watcher/WatcherTimerController.java](../src/main/java/org/usefultoys/slf4j/watcher/WatcherTimerController.java)
*   [src/main/java/org/usefultoys/slf4j/watcher/WatcherSingleton.java](../src/main/java/org/usefultoys/slf4j/watcher/WatcherSingleton.java)
*   [doc/TDR-0012-watcher-singleton-regret.md](./TDR-0012-watcher-singleton-regret.md)
*   [doc/TDR-0008-flexible-execution-strategies-push-vs-pull.md](./TDR-0008-flexible-execution-strategies-push-vs-pull.md)
*   [doc/TDR-0005-robust-and-minimalist-configuration-mechanism.md](./TDR-0005-robust-and-minimalist-configuration-mechanism.md)
*   `.glm-findings/02-watcher-caches-dataEnabled.md`
*   `.glm-findings/03-watcher-run-thread-safety.md`
*   `.glm-findings/05-watchersingleton-timer-non-daemon.md`
