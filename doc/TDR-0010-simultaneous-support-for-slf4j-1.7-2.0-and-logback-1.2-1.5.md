# TDR-0010: Simultaneous Support for SLF4J 1.7/2.0 and Logback 1.2-1.5

**Status**: Accepted
**Date**: 2026-01-03

## Context

The Java logging ecosystem is fragmented across different versions of the SLF4J API and its primary implementation, Logback.
1.  **SLF4J 1.7 vs. 2.0**: SLF4J 2.0 introduced a new Service Loader mechanism for bindings and a fluent API, but many legacy systems still rely on SLF4J 1.7.
2.  **Logback Versions**: 
    *   **1.2.x**: The standard for SLF4J 1.7 and Java 8.
    *   **1.3.x**: Supports SLF4J 2.0 while maintaining Java 8 compatibility.
    *   **1.4.x / 1.5.x**: Supports SLF4J 2.0 but requires Java 11+.

A library like `slf4j-toys` must be able to run in any of these environments using a **single JAR** to avoid forcing users to choose between multiple artifacts or deal with version-specific "shading".

## Decision

We decided to maintain a single JAR that is binary-compatible with both SLF4J 1.7 and 2.0, and validated against all major Logback versions (1.2, 1.3, 1.4, 1.5).

### Implementation Strategy

1.  **Baseline Compilation**: The library is compiled against **Java 8** and **SLF4J 2.0.x**. Since SLF4J 2.0 maintains strict backward compatibility with the 1.x `Logger` and `LoggerFactory` APIs, code compiled against 2.0 remains compatible with 1.7 at the bytecode level (as long as new 2.0-only features like the fluent API are avoided in the core logic).
2.  **Avoidance of Version-Specific SPIs**: The library does not implement SLF4J bindings or service providers itself. It remains a pure consumer of the `slf4j-api`. This allows the host application to provide whatever binding (SLF4J 1.7 static binding or SLF4J 2.0 ServiceLoader) is appropriate for its environment.
3.  **Extensive Multi-Version Testing**: Instead of creating different JARs, we use **Maven Profiles** to run the entire test suite against different combinations of SLF4J and Logback:
    *   **Profile `slf4j-1.7-javax`**: Validates compatibility with SLF4J 1.7.36 and Logback 1.2.13.
    *   **Profile `slf4j-2.0-javax`**: Validates compatibility with SLF4J 2.0.16 and Logback 1.3.14.
    *   **Profile `slf4j-2.0` (Default)**: Validates compatibility with SLF4J 2.0.16 and Logback 1.5.23.
5.  **No Classloader Conflicts**: Since the library only depends on the `slf4j-api` and does not bundle any logging implementation, it avoids the "multiple bindings" error. It simply uses whatever `Logger` implementation is provided at runtime.

## Consequences

**Positive**:
*   **Universal Compatibility**: A single `slf4j-toys.jar` works in legacy Java 8 / SLF4J 1.7 apps and modern Java 21 / SLF4J 2.0 apps.
*   **Simplified Distribution**: No need for classifiers (e.g., `-slf4j17`) or separate modules.
*   **Future-Proof**: The library is ready for SLF4J 2.0 features while remaining accessible to older systems.

**Negative**:
*   **API Restrictions**: We cannot use the new SLF4J 2.0 Fluent API (e.g., `logger.atInfo().log(...)`) in the library's core code, as this would cause `NoSuchMethodError` when running under SLF4J 1.7. ArchUnit tests enforce this restriction automatically.
*   **Build Maintenance**: The `pom.xml` must maintain complex profiles and dependency management to ensure all versions are tested correctly during CI/CD.

**Neutral**:
*   **Testing Overhead**: The CI/CD pipeline must run the test suite multiple times (once for each profile) to guarantee compatibility across the matrix.

## Alternatives

*   **Separate Artifacts**: Publish `slf4j-toys-1.7.jar` and `slf4j-toys-2.0.jar`.
    *   **Rejected because**: Increases maintenance burden and confuses users. Most users don't want to think about which version of a utility library matches their logging framework.
*   **Shading**: Bundle a specific version of SLF4J and rename the packages.
    *   **Rejected because**: Defeats the purpose of a logging facade. The library should participate in the application's existing logging configuration, not bring its own isolated logging world.

## Implementation

*   The `pom.xml` defines `slf4j.version` and `logback.version` as properties that are overridden by profiles.
*   The `maven-compiler-plugin` is fixed to Java 1.8 to ensure bytecode compatibility.
*   ArchUnit tests (`Slf4jCompatibilityArchTest`) automatically verify that no SLF4J 2.0-specific APIs are used in the codebase.
*   The CI/CD pipeline (GitHub Actions) is configured to run `mvn test` for each relevant profile.

## References

*   [pom.xml](../pom.xml)
*   [Slf4jCompatibilityArchTest.java](../src/test/java/org/usefultoys/slf4j/architecture/Slf4jCompatibilityArchTest.java)
*   [TDR-0009: Multi-Spec Servlet Support (javax vs. jakarta)](./TDR-0009-multi-spec-servlet-support-javax-vs-jakarta.md)
