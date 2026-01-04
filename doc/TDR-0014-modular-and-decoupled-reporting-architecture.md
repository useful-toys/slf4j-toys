# TDR-0014: Modular and Decoupled Reporting Architecture

**Status**: Accepted
**Date**: 2026-01-03

## Context
The `Reporter` component is responsible for gathering and logging a wide variety of diagnostic information (Memory, JVM, OS, JDBC, Network, etc.). A monolithic implementation would be difficult to maintain, test, and extend. Furthermore, some reports might be time-consuming or require specific resources, and users may want to control how and when these reports are executed.

## Decision
We implemented a modular architecture where each diagnostic report is a standalone class implementing the `Runnable` interface.

### Key Architectural Features

1.  **Standalone Report Modules**: Each report (e.g., `ReportMemory`, `ReportVM`, `ReportJdbcConnection`) is an independent unit of work. This allows them to be used individually or composed into a larger report.
2.  **Executor-based Delegation**: The main `Reporter` class does not execute reports directly. Instead, it iterates over enabled modules and submits them to a `java.util.concurrent.Executor`.
3.  **Execution Strategy Flexibility**:
    *   **Synchronous**: A `sameThreadExecutor` is provided for simple, blocking execution (default for `runDefaultReport()`).
    *   **Asynchronous/Parallel**: Users can provide a thread pool or a managed executor (e.g., in JavaEE) to run reports in the background or in parallel.
4.  **Hook Methods for Testability**: The `Reporter` class uses protected methods (e.g., `getNetworkInterfaces()`) to provide seams for mocking system resources in unit tests without relying on complex mocking frameworks or reflection.
5.  **Internal Provider Pattern**: Some complex modules (like `ReportMemory`) use internal interfaces (e.g., `MemoryInfoProvider`) to abstract the source of information, further enhancing testability.

## Consequences

**Positive**:
*   **Extensibility**: Adding a new report type is as simple as creating a new `Runnable` class and adding a configuration flag.
*   **Testability**: Individual modules can be unit-tested in isolation. System resources can be mocked via hook methods or internal providers.
*   **Environment Adaptability**: The library can be used in environments with strict threading policies (like JavaEE or reactive frameworks) by providing a compatible `Executor`.
*   **Separation of Concerns**: The `Reporter` class manages the "what" (which reports to run), while the modules manage the "how" (how to gather and format the data).

**Negative**:
*   **Boilerplate**: Each module requires a similar structure (constructor, `run` method), leading to some repetitive code.
*   **Technical Debt (Serialization)**: The `Reporter` class implements `Serializable` but contains a non-transient `Logger` field. Since SLF4J loggers are generally not serializable, this implementation is fragile and likely a legacy artifact that should be revisited.

## Implementation
*   [src/main/java/org/usefultoys/slf4j/report/Reporter.java](src/main/java/org/usefultoys/slf4j/report/Reporter.java): The coordinator and executor delegator.
*   [src/main/java/org/usefultoys/slf4j/report/ReportMemory.java](src/main/java/org/usefultoys/slf4j/report/ReportMemory.java): Example of a modular report with an internal provider.
*   [src/test/java/org/usefultoys/slf4j/report/ReporterTest.java](src/test/java/org/usefultoys/slf4j/report/ReporterTest.java): Demonstrates testing via hook methods and custom executors.

## References
*   [TDR-0006: Security Considerations in Diagnostic Reporting](TDR-0006-security-considerations-in-diagnostic-reporting.md)
*   [TDR-0009: Multi-Spec Servlet Support (javax vs. jakarta)](TDR-0009-multi-spec-servlet-support-javax-vs-jakarta.md)
*   [TDR-0013: Enhanced LoggerFactory and Stream-to-Log Mapping](TDR-0013-enhanced-loggerfactory-and-streams.md)
