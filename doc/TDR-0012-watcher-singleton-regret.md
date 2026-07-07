# TDR-0012: Watcher Singleton Implementation Regret

**Status**: Superseded (push resolved by [TDR-0036](./TDR-0036-replace-watcher-singleton-push-with-controllers.md); pull path still provisional)
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
In [TDR-0036](./TDR-0036-replace-watcher-singleton-push-with-controllers.md), the push side of `WatcherSingleton` was removed and replaced by instance-based `WatcherExecutorController` and `WatcherTimerController`. `WatcherSingleton` now only provides the default `Watcher` instance for the servlet pull path (`WatcherServlet` / `WatcherJavaxServlet`) and is marked `@Deprecated`.

The negative consequences above no longer apply to push execution. They still apply to the remaining `getDefaultWatcher()` pull path until the servlet is migrated to use its own `Watcher` instance.

## Alternatives
*   **Dependency Injection**: Instead of a singleton, the `Watcher` could be injected into components that need it. This would solve the testing and configuration issues but would require a DI framework or more boilerplate code.
*   **Instance Management**: Allow the creation of multiple `Watcher` instances and let the application manage them. The "default" instance could be managed by the application's lifecycle container rather than a static singleton.

## Implementation
The push implementation was removed from [src/main/java/org/usefultoys/slf4j/watcher/WatcherSingleton.java](src/main/java/org/usefultoys/slf4j/watcher/WatcherSingleton.java). The class now only retains `getDefaultWatcher()` as a temporary compatibility bridge for the servlet pull path. New code should use `WatcherExecutorController`, `WatcherTimerController`, or create a dedicated `Watcher` instance.

## References
*   [src/main/java/org/usefultoys/slf4j/watcher/WatcherSingleton.java](src/main/java/org/usefultoys/slf4j/watcher/WatcherSingleton.java)
*   [src/main/java/org/usefultoys/slf4j/watcher/WatcherConfig.java](src/main/java/org/usefultoys/slf4j/watcher/WatcherConfig.java)
*   [doc/TDR-0005-configuration-mechanism.md](doc/TDR-0005-configuration-mechanism.md)
*   [doc/TDR-0036-replace-watcher-singleton-push-with-controllers.md](./TDR-0036-replace-watcher-singleton-push-with-controllers.md)
