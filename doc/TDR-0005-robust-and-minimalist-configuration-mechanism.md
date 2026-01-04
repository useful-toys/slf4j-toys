# TDR-0005: Robust and Minimalist Configuration Mechanism

**Status**: Accepted
**Date**: 2026-01-03

## Context

A logging library needs to be configurable (e.g., enabling/disabling features, setting thresholds, defining logger names) without imposing a specific configuration framework on the host application. The configuration mechanism must be:
1.  **Zero-dependency**: No external libraries like Typesafe Config or Spring.
2.  **Standard**: Use well-known Java mechanisms.
3.  **Robust**: Handle invalid configurations gracefully without crashing the application.
4.  **Flexible**: Support both startup configuration and dynamic runtime updates.

## Decision

We implemented a centralized configuration mechanism based on **System Properties** and **Type-Safe Utility Classes**.

### Key Components

1.  **System Properties**: All configuration is driven by system properties prefixed with `slf4jtoys.*`. This is the standard way to configure Java libraries via CLI (`-D`) or environment variables.
2.  **Centralized Config Classes**: Properties are grouped into domain-specific utility classes:
    *   `SessionConfig`: Global session settings (UUID size, charset).
    *   `SystemConfig`: Controls which JVM metrics are collected (gracefully handles missing MXBeans).
    *   `MeterConfig`: Settings for operation measurement (prefixes, suffixes, progress periods).
    *   `ReporterConfig`: Controls diagnostic report content.
    *   `WatcherConfig`: Settings for background monitoring.
3.  **`ConfigParser` Utility**: A custom parser that handles:
    *   Type conversion (String, boolean, int, long).
    *   **Duration Parsing**: Supports units like `ms`, `s`, `m`, and `h` (e.g., `slf4jtoys.meter.progress.period=5s`).
    *   **Error Transparency**: Instead of throwing exceptions, `ConfigParser` records detailed error messages (including the property name, the invalid value, and the reason for failure) in a synchronized `initializationErrors` list. This allows developers to programmatically inspect configuration issues.
4.  **Static Initialization**: Config classes use a `static { init(); }` block to ensure properties are loaded as soon as the class is accessed.
5.  **Lifecycle Management**: Each config class provides `init()` and `reset()` methods:
    *   `init()`: Re-reads all relevant system properties, allowing for runtime updates if system properties are changed.
    *   `reset()`: Restores all configuration fields to their hardcoded default values.

## Consequences

**Positive**:
*   **Ease of Use**: Users can configure the library using standard JVM arguments.
*   **Application Safety**: Invalid configuration values (e.g., a string where a number is expected) never cause the application to crash; the library simply logs the error and uses a default.
*   **Observability**: The `initializationErrors` list provides a clear audit trail of what went wrong during configuration loading.
*   **Zero Footprint**: No extra dependencies are added to the project.
*   **Graceful Degradation**: Features relying on platform-specific MXBeans (configured via `SystemConfig`) fail silently or with a logged error if the platform does not support them.
*   **Dynamic Control**: Since config fields are public and non-final, they can be adjusted programmatically at runtime for advanced use cases.
*   **Discoverability**: All available properties are centralized in a few well-documented classes.

**Negative**:
*   **Global State**: Configuration is global to the JVM session, which might be a limitation in complex multi-tenant environments (though this is typical for logging libraries).
*   **Static Initialization Limitation**: Some components (e.g., `WatcherSingleton`, `WatcherServlet`) use static initialization to create singletons based on the initial system property values. Consequently, programmatic changes to configuration classes or calls to `init()`/`reset()` made *after* these singletons are initialized will not be reflected in their behavior. This is a known technical debt that will require refactoring in the future.
*   **Manual Sync**: Adding a new property requires updating the config class, the `init()` method, and the documentation.

**Neutral**:
*   **System Property Precedence**: System properties always take precedence over hardcoded defaults, which is the expected behavior for Java developers.
*   **Testing Strategy**: Due to the minimalist and manual nature of the configuration parsing logic, the implementation requires extensive test coverage to ensure robustness. This coverage is primarily provided by AI-generated test suites.

## Alternatives

*   **External Config Library (e.g., Typesafe Config)**:
    *   **Rejected because**: Introduces heavy dependencies and complex configuration file formats.
*   **Properties Files (`.properties`)**:
    *   **Rejected because**: Requires managing file locations and classpath issues, which is more complex than simple system properties.
*   **Environment Variables**:
    *   **Rejected because**: Less standard for fine-grained Java library configuration than system properties (though system properties can often be populated from env vars).

## Implementation

*   `ConfigParser` maintains a `Collections.synchronizedList` for errors.
*   `init()` methods are public, allowing users to force a reload of properties if needed.
*   Lombok `@UtilityClass` is used to enforce the utility pattern (private constructor, final class).

## References

*   [ConfigParser.java](../src/main/java/org/usefultoys/slf4j/utils/ConfigParser.java)
*   [MeterConfig.java](../src/main/java/org/usefultoys/slf4j/meter/MeterConfig.java)
*   [SystemConfig.java](../src/main/java/org/usefultoys/slf4j/SystemConfig.java)
*   [ReporterConfig.java](../src/main/java/org/usefultoys/slf4j/report/ReporterConfig.java)
*   [TDR-0002: Use of Lombok](./TDR-0002-use-of-lombok.md)
