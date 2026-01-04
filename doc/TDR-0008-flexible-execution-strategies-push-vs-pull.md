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
The `Watcher` class implements `java.lang.Runnable`. It contains no internal scheduling logic. This makes it a "passive" component that can be plugged into any scheduling framework (Spring, EJB Timer, Quartz).

### 2. Push Strategy (Internal Scheduling)
For simple architectures, `WatcherSingleton` provides a built-in "Push" mechanism:
*   **`startDefaultWatcherExecutor()`**: Uses a `ScheduledExecutorService` to push reports at fixed intervals.
*   **`startDefaultWatcherTimer()`**: Uses a legacy `java.util.Timer` for environments where a full executor is not desired.
*   **Rationale**: Provides a "zero-config" way to get monitoring running in standalone apps.

### 3. Pull Strategy (External Triggers)
For managed or probe-based environments, we provide a "Pull" mechanism via Servlets:
*   **`WatcherServlet` / `WatcherJavaxServlet`**: Exposes the `Watcher` execution via an HTTP GET request.
*   **Rationale**: 
    *   Complies with JavaEE restrictions by not creating background threads.
    *   Allows external monitoring systems to "pull" a status report on demand.
    *   Enables integration with infrastructure-level schedulers (e.g., `curl` via `cron`).

## Consequences

**Positive**:
*   **Architectural Neutrality**: The library fits into any Java environment, from a simple `main` method to a complex enterprise application server.
*   **Resource Compliance**: By offering Servlet-based execution, we avoid common pitfalls in managed environments where manual thread creation leads to memory leaks or container instability.
*   **Flexibility**: Users can choose the strategy that best fits their operational model (e.g., fixed interval vs. on-demand).

**Negative**:
*   **Configuration Fragmentation**: Users must understand which strategy is appropriate for their environment (e.g., not using `WatcherSingleton`'s executor in a JavaEE app).
*   **Security Surface**: The "Pull" strategy (Servlets) introduces a new HTTP endpoint that must be manually secured by the user to prevent information disclosure or DoS.

**Neutral**:
*   **Static Initialization Dependency**: As noted in [TDR-0005](./TDR-0005-robust-and-minimalist-configuration-mechanism.md), the `WatcherSingleton` and `WatcherServlet` rely on the default instance, which captures configuration at class-loading time.

## Alternatives

*   **Internal-Only Scheduling**: Force the library to manage its own threads.
    *   **Rejected because**: Incompatible with JavaEE/JakartaEE standards and modern framework practices.
*   **External-Only Scheduling**: Provide only the `Runnable` and force users to implement the trigger.
    *   **Rejected because**: Increases the barrier to entry for simple use cases.

## Implementation

*   `Watcher` implements `Runnable`.
*   `WatcherSingleton` manages the lifecycle of the default instance and its optional internal executors.
*   `WatcherServlet` (Jakarta) and `WatcherJavaxServlet` (Legacy) provide the HTTP bridge.

## References

*   [Watcher.java](../src/main/java/org/usefultoys/slf4j/watcher/Watcher.java)
*   [WatcherSingleton.java](../src/main/java/org/usefultoys/slf4j/watcher/WatcherSingleton.java)
*   [WatcherServlet.java](../src/main/java/org/usefultoys/slf4j/watcher/WatcherServlet.java)
*   [Wiki: JavaEE Use Case](../slf4j-toys.wiki/watcher/Watcher-use-case-javaee.md)
*   [Wiki: Spring Boot Use Case](../slf4j-toys.wiki/watcher/Watcher-use-case-spring-boot.md)
*   [TDR-0005: Robust and Minimalist Configuration Mechanism](./TDR-0005-robust-and-minimalist-configuration-mechanism.md)
