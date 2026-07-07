# TDR-0012: Watcher Singleton Implementation Regret

**Status**: Resolved (push resolved by [TDR-0036](./TDR-0036-replace-watcher-singleton-push-with-controllers.md); pull path resolved by [TDR-0037](./TDR-0037-migrate-watcher-servlet-pull-from-singleton.md))
**Date**: 2026-01-03
**Updated**: 2026-07-07

## Context
The `Watcher` component was designed to monitor system resources and application health. To simplify its usage in standard applications, a `WatcherSingleton` was implemented to provide a globally accessible, default instance that could be easily started and stopped via a background executor or timer.

## Decision (Historical)
We originally implemented `WatcherSingleton` as a utility class (using Lombok's `@UtilityClass`) that lazily initializes a single `Watcher` instance and manages its lifecycle (start/stop) using either a `ScheduledExecutorService` or a `Timer`.

## Consequences
**Positive**:
*   **Ease of Use**: Developers can start system monitoring with a single method call (`WatcherSingleton.startDefaultWatcherExecutor()`).
*   **Centralized Management**: Provides a single point of control for the default monitoring behavior.

**Negative**:
*   **Test Complexity**: The singleton pattern makes unit and integration testing difficult. State leaks between tests, and it's hard to isolate the `Watcher` behavior or mock its dependencies without affecting other tests.
*   **Configuration Rigidity**: The singleton instance is initialized once (lazily). If configuration properties (like `slf4jtoys.watcher.name`) are changed at runtime after the singleton has been accessed, the existing instance does not reflect these changes.
*   **Lifecycle Issues**: In containerized or modular environments (like JavaEE/JakartaEE), static singletons can lead to memory leaks if not properly shut down, as they are tied to the ClassLoader's lifecycle.
*   **Hidden Dependencies**: Classes using `WatcherSingleton` have a hidden dependency on a global state, making the code harder to reason about and refactor.

## Resolution
In [TDR-0036](./TDR-0036-replace-watcher-singleton-push-with-controllers.md), the push side of `WatcherSingleton` was removed and replaced by instance-based `WatcherExecutorController` and `WatcherTimerController`. In [TDR-0037](./TDR-0037-migrate-watcher-servlet-pull-from-singleton.md), the pull side was migrated: `WatcherServlet` and `WatcherJavaxServlet` now create their own `Watcher` instances during `init(ServletConfig)`. `WatcherSingleton` was removed entirely.

The negative consequences above no longer apply. New code should use `WatcherExecutorController`, `WatcherTimerController`, create a dedicated `Watcher` instance, or use `WatcherServlet`/`WatcherJavaxServlet` for pull-mode diagnostics.

## Alternatives
*   **Dependency Injection**: Instead of a singleton, the `Watcher` could be injected into components that need it. This solves the testing and configuration issues but requires a DI framework or more boilerplate code.
*   **Instance Management**: Allow the creation of multiple `Watcher` instances and let the application manage them. The "default" instance can be managed by the application's lifecycle container rather than a static singleton.

## Implementation
`WatcherSingleton` was removed from [src/main/java/org/usefultoys/slf4j/watcher/WatcherSingleton.java](../src/main/java/org/usefultoys/slf4j/watcher/WatcherSingleton.java). `WatcherServlet` and `WatcherJavaxServlet` now instantiate their own `Watcher` during `init(ServletConfig)`, optionally overriding the name via the `slf4jtoys.watcher.name` init-param. Concurrent calls to `runWatcher()` are serialized with an internal lock.

## References
*   [src/main/java/org/usefultoys/slf4j/watcher/WatcherServlet.java](../src/main/java/org/usefultoys/slf4j/watcher/WatcherServlet.java)
*   [src/main/java/org/usefultoys/slf4j/watcher/WatcherJavaxServlet.java](../src/main/java/org/usefultoys/slf4j/watcher/WatcherJavaxServlet.java)
*   [src/main/java/org/usefultoys/slf4j/watcher/WatcherConfig.java](src/main/java/org/usefultoys/slf4j/watcher/WatcherConfig.java)
*   [doc/TDR-0005-configuration-mechanism.md](doc/TDR-0005-configuration-mechanism.md)
*   [doc/TDR-0036-replace-watcher-singleton-push-with-controllers.md](./TDR-0036-replace-watcher-singleton-push-with-controllers.md)
*   [doc/TDR-0037-migrate-watcher-servlet-pull-from-singleton.md](./TDR-0037-migrate-watcher-servlet-pull-from-singleton.md)
