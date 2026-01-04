# TDR-0018: Portable Access to Platform-Specific Metrics

**Status**: Accepted
**Date**: 2026-01-03

## Context
The standard Java Management Extensions (JMX) API (`java.lang.management`) provides basic system metrics, but some valuable information (like system CPU load) is only available through platform-specific extensions (e.g., `com.sun.management`). The library needs to collect these metrics when available without sacrificing portability or causing `NoClassDefFoundError` on non-Oracle/OpenJDK JVMs.

## Decision
We implemented a "safe-cast" strategy in `SystemMetricsCollector` to access platform-specific MXBeans.

### Implementation Details
1.  **Dependency Injection**: `SystemMetricsCollector` receives standard MXBean interfaces (e.g., `OperatingSystemMXBean`) in its constructor.
2.  **Runtime Type Checking**: Before accessing extended features, the collector checks if the bean is an instance of the platform-specific class using `instanceof`.
3.  **Conditional Execution**:
    ```java
    if (this.osBean instanceof com.sun.management.OperatingSystemMXBean) {
        final com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) this.osBean;
        data.systemLoad = sunOsBean.getSystemCpuLoad();
    }
    ```
4.  **Configuration Toggle**: A system property (`slf4jtoys.usePlatformManagedBean`) allows users to disable this behavior if it causes issues in their environment.
5.  **Graceful Degradation**: If the platform-specific bean is not available, the library simply skips those metrics, leaving the corresponding fields in `SystemData` at their default values.

## Consequences
**Positive**:
*   **Rich Metrics**: Provides access to high-value metrics like CPU load and process memory usage on supported JVMs.
*   **Portability**: The code compiles and runs on any Java 8+ compliant JVM, even those without `com.sun.management` support.
*   **Safety**: Avoids runtime errors by using `instanceof` and catching potential `SecurityException` (via `SystemConfig`).

**Negative**:
*   **Compiler Warnings**: Requires `@SuppressWarnings("Since15")` or similar to handle references to classes that might not be available in all target environments during compilation.
*   **Incomplete Data**: On some JVMs (e.g., IBM J9, GraalVM in some configurations), these metrics might be missing, leading to gaps in the diagnostic data.

## Alternatives
*   **Reflection**: Use reflection to call `getSystemCpuLoad()`. **Rejected because** it's slower and less type-safe than a direct cast with `instanceof`.
*   **Native Libraries**: Use JNI to get system metrics. **Rejected because** it violates the "zero-dependency" and "pure Java" goals of the library.

## Implementation
*   [src/main/java/org/usefultoys/slf4j/internal/SystemMetricsCollector.java](src/main/java/org/usefultoys/slf4j/internal/SystemMetricsCollector.java): Implements the safe-cast logic for CPU and memory metrics.
*   [src/main/java/org/usefultoys/slf4j/SystemConfig.java](src/main/java/org/usefultoys/slf4j/SystemConfig.java): Provides the `usePlatformManagedBean` toggle.

## References
*   [Java OperatingSystemMXBean Documentation](https://docs.oracle.com/javase/8/docs/jre/api/management/extension/com/sun/management/OperatingSystemMXBean.html)
