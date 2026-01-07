# Meter Design Philosophy

This document outlines the design considerations and decisions for the `org.usefultoys.slf4j.meter.Meter` class, focusing on the trade-offs between Object-Oriented design purity, API usability, and high performance.

## Core Requirements

The `Meter` class is a cornerstone of this library and is intended for high-frequency, performance-critical scenarios. A `Meter` instance is often short-lived, created and disposed of hundreds of thousands or even millions of times in an application's lifecycle.

The key requirements for its design are:
1.  **High Performance:** Minimal memory allocation overhead and low impact on the Garbage Collector (GC).
2.  **Fluent API:** An ergonomic and intuitive API for the end-user (e.g., `meter.getDuration()`).
3.  **Clean Design:** Good separation of concerns to ensure maintainability.
4.  **Resilience:** The library must never interfere with the application's execution, even if used incorrectly.

## Design Options Considered

We analyzed three primary design patterns for implementing the `Meter`'s capabilities, specifically for `MeterAnalysis` (data interpretation) and `MeterContext` (contextual data management).

### Option 1: Interfaces with `default` Methods (The Chosen Approach)

This is the current implementation, where `Meter` implements interfaces that provide logic via `default` methods.

```java
public interface MeterAnalysis extends MeterData {
    default long getDurationMillis() {
        // logic using methods from MeterData
    }
}

public class Meter implements MeterAnalysis, MeterContext {
    // ... core lifecycle fields and methods ...
}
```

-   **Pros:**
    -   **Excellent Performance:** Only **one object** (`Meter`) is allocated per measurement, resulting in the minimum possible memory footprint and GC pressure.
    -   **Fluent API:** Provides the desired `meter.method()` syntax.
    -   **Good Conceptual Separation:** The interfaces (`MeterData`, `MeterAnalysis`, `MeterContext`) provide a clear conceptual separation of concerns, even if the concrete class implements all of them.

-   **Cons:**
    -   **Weaker SRP (Single Responsibility Principle):** The `Meter` class conceptually accumulates multiple responsibilities, becoming a "jack-of-all-trades" rather than a pure data-and-lifecycle object.

### Option 2: Composition with `@lombok.Delegate`

This approach favors composition over inheritance. The `Meter` class would hold instances of helper classes and delegate calls to them.

```java
public class Meter implements MeterData {
    @Delegate(types = MeterAnalysis.class)
    private final MeterAnalysis analysis = new DefaultMeterAnalysis(this);

    @Delegate(types = MeterContext.class)
    private final MeterContext context = new DefaultMeterContext();
}
```

-   **Pros:**
    -   **Superior OO Design:** Achieves excellent SRP by isolating each responsibility into its own dedicated class.
    -   **Maintains Fluent API:** The end-user API remains identical to Option 1.

-   **Cons:**
    -   **Performance Overhead:** This pattern would allocate 3-4 objects for each `Meter` instance. In a high-frequency scenario, this would significantly increase memory allocation and GC pressure, posing a potential performance risk. While modern JVMs can mitigate this with *Escape Analysis* and *Scalar Replacement*, relying on these optimizations is risky for a performance-critical library.

### Option 3: Static Utility Classes

This pattern separates logic and data in a functional style.

```java
public final class MeterAnalysis {
    private MeterAnalysis() {}
    public static long getDurationMillis(MeterData data) {
        // logic
    }
}

// Usage: MeterAnalysis.getDurationMillis(meter);
```

-   **Pros:**
    -   **Guaranteed Maximum Performance:** Zero object allocation overhead for the logic components.
    -   **Perfect SRP:** Complete separation of logic from data.

-   **Cons:**
    -   **Degraded API Usability:** The API becomes less ergonomic and less object-oriented (`Util.method(object)` vs. `object.method()`). This was considered a significant step back for user experience.

## Final Decision

After careful consideration, **Option 1 (Interfaces with `default` Methods)** was chosen.

While Option 2 offers a purer object-oriented design, the potential and unpredictable performance cost in high-throughput scenarios was deemed an unacceptable risk for a library of this nature. Option 3 provides the best performance but at the cost of a less intuitive API.

The current approach provides the most pragmatic and robust balance, delivering the **guaranteed high performance** and **fluent API** required, while still achieving a reasonable separation of concerns through interfaces.

## Resilience and Error Handling

A core design principle of `slf4j-toys` is **resilience**. The `Meter` and `Watcher` components are designed to be completely non-intrusive. Their primary goal is to provide observability into an application's lifecycle, but they must **never** cause the monitored operation to fail.

Even if the library is used incorrectly (e.g., invalid parameters, incorrect state transitions), it will not throw exceptions that could disrupt the application's flow. Instead of failing, it handles such cases gracefully:

1.  **Internal Errors are Suppressed:** Any internal exceptions within the library are caught and handled internally.
2.  **Warnings are Logged:** To alert the developer of incorrect usage, a warning is logged via SLF4J.
3.  **Caller-Pointing Stack Traces:** To make debugging trivial, these warnings include a stack trace from an artificial `CallerStackTraceThrowable`. This special throwable is not actually "thrown" but created solely to capture a clean stack trace that points directly to the line in the user's code where the invalid call was made. This avoids polluting the logs with the library's internal call stack and immediately highlights the source of the problem.

This approach ensures that the application remains stable, while still providing clear and actionable feedback to developers about improper use of the library.
