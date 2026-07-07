# TDR-0008: Flexible Execution Strategies (Push vs. Pull)

**Status**: Accepted
**Date**: 2026-01-03

## Context

The `Watcher` component is responsible for periodic system monitoring. However, different application architectures have different requirements and restrictions regarding thread management and task scheduling:
1.  **Standalone Applications**: Simple CLI or desktop apps often lack a scheduling framework and need a built-in way to run tasks.
2.  **Managed Environments (JavaEE/JakartaEE)**: These environments strictly discourage or forbid manual thread creation (`new Thread()`, `Executors.new*()`) to maintain container control over resources.
3.  **Modern Frameworks (Spring Boot)**: These provide their own robust scheduling abstractions (`@Scheduled`) and expect libraries to be "passive" components.
4.  **On-Demand/External Triggers**: Some environments prefer monitoring to be triggered by external probes (e.g., Kubernetes liveness probes, load balancer health checks, or cron jobs).

## Decision

We decided to implement a **Multi-Strategy Execution Model** that supports both "Push" and "Pull" patterns, while keeping the core `Watcher` logic passive.

### 1. Passive Core (`Runnable`)
Both `Watcher` and `Reporter` classes implement `java.lang.Runnable`. They contain no internal scheduling logic. This makes them "passive" components that can be plugged into any scheduling framework (Spring, EJB Timer, Quartz, custom application schedulers, or any other execution mechanism that accepts `Runnable` instances).

### 2. Push Strategy (Internal Scheduling)
For simple architectures, two instance-based controllers provide a built-in "Push" mechanism:
*   **`WatcherExecutorController`**: Uses a `ScheduledExecutorService` to push reports at fixed intervals.
*   **`WatcherTimerController`**: Uses a legacy `java.util.Timer` for environments where a full executor is not desired.
*   **Rationale**: Provides a "zero-config" way to get monitoring running in standalone apps while giving the application explicit ownership of the watcher instance and its lifecycle. Both controllers read `WatcherConfig` defaults when created and expose `create()` shortcuts plus a fluent `Builder`.

### 3. Pull Strategy (External Triggers)
For managed or probe-based environments, we provide a "Pull" mechanism via Servlets:
*   **`WatcherServlet` / `WatcherJavaxServlet`**: Exposes the `Watcher` execution via an HTTP GET request.
*   **`ReporterServlet` / `ReporterJavaxServlet`**: Exposes the `Reporter` execution via an HTTP GET request.
*   **Rationale**: 
    *   Complies with JavaEE restrictions by not creating background threads.
    *   Allows external monitoring systems to "pull" a status report on demand.
    *   Enables integration with infrastructure-level schedulers (e.g., `curl` via `cron`).
    *   Both `Watcher` and `Reporter` support both servlet specifications: Jakarta EE (modern) and javax (legacy).

## Consequences

**Positive**:
*   **Architectural Neutrality**: The library fits into any Java environment, from a simple `main` method to a complex enterprise application server.
*   **Resource Compliance**: By offering Servlet-based execution, we avoid common pitfalls in managed environments where manual thread creation leads to memory leaks or container instability.
*   **Flexibility**: Users can choose the strategy that best fits their operational model (e.g., fixed interval vs. on-demand).

**Negative**:
*   **Configuration Fragmentation**: Users must understand which strategy is appropriate for their environment (e.g., not using the built-in push controllers in a JavaEE app that manages its own threads).
*   **Security Surface**: The "Pull" strategy (Servlets) introduces a new HTTP endpoint that must be manually secured by the user to prevent information disclosure or DoS.

**Neutral**:
*   **Static Initialization Dependency**: As noted in [TDR-0005](./TDR-0005-robust-and-minimalist-configuration-mechanism.md), the remaining `WatcherSingleton.getDefaultWatcher()` pull path and `WatcherServlet` rely on the default instance, which captures configuration at class-loading time. The new push controllers capture `WatcherConfig` values when the controller is built, avoiding the static singleton limitation.

## Alternatives

*   **Internal-Only Scheduling**: Force the library to manage its own threads.
    *   **Rejected because**: Incompatible with JavaEE/JakartaEE standards and modern framework practices.
*   **External-Only Scheduling**: Provide only the `Runnable` and force users to implement the trigger.
    *   **Rejected because**: Increases the barrier to entry for simple use cases.

## Implementation

*   `Watcher` and `Reporter` implement `Runnable`, making them compatible with any scheduling or execution mechanism that accepts `Runnable` instances.
*   `WatcherExecutorController` and `WatcherTimerController` manage instance-based push execution; each owns a dedicated `Watcher` created from `WatcherConfig` at build time.
*   `WatcherSingleton` is `@Deprecated` and now only provides the default `Watcher` instance for the servlet pull path.
*   `WatcherServlet` (Jakarta) and `WatcherJavaxServlet` (Legacy) provide the HTTP bridge for Watcher.
*   `ReporterServlet` (Jakarta) and `ReporterJavaxServlet` (Legacy) provide the HTTP bridge for Reporter.

## References

*   [Watcher.java](../src/main/java/org/usefultoys/slf4j/watcher/Watcher.java)
*   [WatcherExecutorController.java](../src/main/java/org/usefultoys/slf4j/watcher/WatcherExecutorController.java)
*   [WatcherTimerController.java](../src/main/java/org/usefultoys/slf4j/watcher/WatcherTimerController.java)
*   [WatcherSingleton.java](../src/main/java/org/usefultoys/slf4j/watcher/WatcherSingleton.java)
*   [WatcherServlet.java](../src/main/java/org/usefultoys/slf4j/watcher/WatcherServlet.java)
*   [Wiki: JavaEE Use Case](../slf4j-toys.wiki/watcher/Watcher-use-case-javaee.md)
*   [Wiki: Spring Boot Use Case](../slf4j-toys.wiki/watcher/Watcher-use-case-spring-boot.md)
*   [TDR-0005: Robust and Minimalist Configuration Mechanism](./TDR-0005-robust-and-minimalist-configuration-mechanism.md)
*   [TDR-0036: Replace WatcherSingleton Push with Instance-Based Controllers](./TDR-0036-replace-watcher-singleton-push-with-controllers.md)
