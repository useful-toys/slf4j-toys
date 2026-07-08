# TDR-0037: Migrate WatcherServlet Pull Path from WatcherSingleton

**Status**: Accepted
**Date**: 2026-07-07

## Context

`WatcherSingleton` was reduced to a single `getDefaultWatcher()` method in [TDR-0036](./TDR-0036-replace-watcher-singleton-push-with-controllers.md), serving only the servlet pull path (`WatcherServlet` and `WatcherJavaxServlet`). That temporary bridge still suffered from the problems documented in [TDR-0012](./TDR-0012-watcher-singleton-regret.md) and `.glm-findings/03-watcher-run-thread-safety.md`:

*   **Shared mutable state**: the same `Watcher` instance was returned to every caller, so concurrent HTTP requests could execute `Watcher.run()` concurrently.
*   **Configuration rigidity**: the watcher name and logger settings were captured lazily on first access and never refreshed.
*   **Hidden global state**: the servlet depended on a static singleton instead of an explicit, container-managed instance.

## Decision

Remove `WatcherSingleton` entirely and make `WatcherServlet`/`WatcherJavaxServlet` own their `Watcher` instance:

*   Each servlet creates its own `Watcher` during `init(ServletConfig)`.
*   By default the watcher name is read from `WatcherConfig.name`; the optional `slf4jtoys.watcher.name` servlet `<init-param>` overrides it.
*   Logger prefixes/suffixes and the data logger flag are read from `WatcherConfig` at watcher construction time, matching the snapshot semantics of `WatcherExecutorController` and `WatcherTimerController`.
*   `runWatcher()` guards execution with a non-blocking {@link java.util.concurrent.locks.ReentrantLock#tryLock()} on a private instance lock. Concurrent `doGet` invocations that arrive while a collection is already in progress are skipped and answered with HTTP 429 (`Too Many Requests`) instead of being queued; only one request per servlet instance performs the actual JMX collection at a time.

## Consequences

**Positive**:

*   **No shared pull state**: each servlet instance has its own watcher, eliminating the cross-request race condition.
*   **Predictable configuration**: the watcher is configured once during `init()`, when the servlet configuration is available.
*   **Cleaner lifecycle**: the watcher is created and owned by the servlet instance, with no static singleton to leak between deployments.
*   **Consistency**: pull servlets now follow the same "own your watcher" pattern as push controllers.
*   **Reduced DoS amplification**: the non-blocking lock prevents servlet-container threads from queuing behind a long-running JMX collection.

**Negative / Breaking**:

*   `WatcherSingleton` and `WatcherSingleton.getDefaultWatcher()` are removed. Any external caller using them must migrate to `new Watcher(WatcherConfig.name)` or to `WatcherServlet`/`WatcherJavaxServlet` for the pull path.
*   The servlets now require `init(ServletConfig)` to be invoked by the container before `doGet`. This is standard servlet behavior, but direct unit tests must call `servlet.init(mock(ServletConfig.class))` before exercising `doGet`.

## Alternatives Considered

*   **Keep `WatcherSingleton` and only add synchronization to `Watcher.run()`**: rejected because it would perpetuate the global singleton and configuration-rigidity problems documented in [TDR-0012](./TDR-0012-watcher-singleton-regret.md).
*   **Make `Watcher.run()` synchronized**: rejected because it serializes all watcher executions globally for a given instance and still leaves the configuration-rigidity issue. Instance ownership and a private per-servlet lock solve both problems more cleanly.
*   **Synchronize `runWatcher()` with a blocking monitor (queue concurrent requests)**: rejected because, while it prevents concurrent `Watcher.run()` calls, it ties up servlet-container threads behind the lock, amplifying the DoS surface flagged in SEC-001. A non-blocking `tryLock()` with HTTP 429 skip semantics avoids the queue and keeps the response honest.
*   **Allow init-params for delay/period in the servlet**: rejected. The servlet is pull-only; scheduling parameters belong to the push controllers (`WatcherExecutorController`, `WatcherTimerController`).

## Implementation

*   Updated `src/main/java/org/usefultoys/slf4j/watcher/WatcherServlet.java` to create a private `Watcher` in `init(ServletConfig)`, read the optional `slf4jtoys.watcher.name` init-param, and guard `runWatcher()` with a non-blocking lock (skip-if-busy, HTTP 429).
*   Updated `src/main/java/org/usefultoys/slf4j/watcher/WatcherJavaxServlet.java` with the same change for the `javax.servlet` API.
*   Removed `src/main/java/org/usefultoys/slf4j/watcher/WatcherSingleton.java`.
*   Removed `src/test/java/org/usefultoys/slf4j/watcher/WatcherSingletonTest.java`.
*   Updated `WatcherServletTest` and `WatcherJavaxServletTest` to call `init(ServletConfig)` and added tests for the init-param override and fallback.
*   Updated `README.md`, `doc/TDR-0012-watcher-singleton-regret.md`, `doc/TDR-0005-robust-and-minimalist-configuration-mechanism.md`, `doc/TDR-0008-flexible-execution-strategies-push-vs-pull.md`, `doc/TDR-0036-replace-watcher-singleton-push-with-controllers.md`, and the wiki pages.

## References

*   [src/main/java/org/usefultoys/slf4j/watcher/WatcherServlet.java](../src/main/java/org/usefultoys/slf4j/watcher/WatcherServlet.java)
*   [src/main/java/org/usefultoys/slf4j/watcher/WatcherJavaxServlet.java](../src/main/java/org/usefultoys/slf4j/watcher/WatcherJavaxServlet.java)
*   [src/main/java/org/usefultoys/slf4j/watcher/Watcher.java](../src/main/java/org/usefultoys/slf4j/watcher/Watcher.java)
*   [src/main/java/org/usefultoys/slf4j/watcher/WatcherConfig.java](../src/main/java/org/usefultoys/slf4j/watcher/WatcherConfig.java)
*   [doc/TDR-0012-watcher-singleton-regret.md](./TDR-0012-watcher-singleton-regret.md)
*   [doc/TDR-0036-replace-watcher-singleton-push-with-controllers.md](./TDR-0036-replace-watcher-singleton-push-with-controllers.md)
*   `.glm-findings/03-watcher-run-thread-safety.md`
