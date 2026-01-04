# TDR-0009: Multi-Spec Servlet Support (javax vs. jakarta)

**Status**: Accepted
**Date**: 2026-01-03

## Context

The Java ecosystem underwent a major transition from the `javax.servlet` namespace (Java EE) to the `jakarta.servlet` namespace (Jakarta EE 9+). As a library that provides Servlet-based monitoring triggers, `slf4j-toys` needs to support both environments:
1.  **Legacy Systems**: Applications still running on older application servers (e.g., Tomcat 9, WildFly 26) that use `javax.servlet`.
2.  **Modern Systems**: Applications running on newer servers (e.g., Tomcat 10+, WildFly 27+) that use `jakarta.servlet`.

Supporting both namespaces in a single JAR is challenging because they are binary-incompatible (different package names).

## Decision

We decided to include **duplicate implementations** for both specifications within the same JAR, using Maven profiles to manage compilation and testing.

### Implementation Strategy

1.  **Parallel Classes**: We maintain two sets of identical (or nearly identical) classes:
    *   `WatcherServlet` and `ReportServlet` (using `jakarta.servlet`).
    *   `WatcherJavaxServlet` and `ReportJavaxServlet` (using `javax.servlet`).
2.  **Maven Profiles**: The `pom.xml` defines profiles to handle the different dependencies:
    *   **`slf4j-2.0` (Default)**: Includes `jakarta.servlet-api` and compiles the Jakarta-based classes.
    *   **`slf4j-2.0-javax`**: Includes `javax.servlet-api` and uses `maven-compiler-plugin` to **exclude** the Jakarta classes from compilation to avoid errors.
3.  **Classloader Safety**: Since the classes have different names and package-level imports, they can coexist in the same JAR. A `NoClassDefFoundError` will only occur if an application explicitly tries to load a class whose underlying specification (JAR) is missing from the classpath. If the user doesn't use the Servlet feature, or uses the one matching their environment, no error occurs.

## Consequences

**Positive**:
*   **Simplicity**: Avoids the complexity of creating separate artifacts (e.g., `slf4j-toys-jakarta.jar` vs `slf4j-toys-javax.jar`) or using Maven classifiers.
*   **Single Artifact**: Users only need to manage one dependency version.
*   **Zero Runtime Overhead**: The unused classes are simply ignored by the JVM if not referenced.

**Negative**:
*   **Code Duplication**: Changes to the Servlet logic must be manually synchronized between the `javax` and `jakarta` versions.
*   **Build Complexity**: The `pom.xml` requires careful configuration of `excludes` and `testExcludes` in the compiler and surefire plugins to ensure the build passes in both profiles.
*   **JAR Bloat**: The JAR contains a few extra small classes, though the impact is negligible (a few kilobytes).

**Neutral**:
*   **Developer Responsibility**: The user must ensure they register the correct Servlet class (`WatcherServlet` vs `WatcherJavaxServlet`) in their `web.xml` or via annotations, matching their container's specification.

## Alternatives

*   **Shading/Bytecode Transformation**: Use a tool like Eclipse Transformer to generate a Jakarta version of the JAR during the build.
    *   **Rejected because**: Adds significant complexity to the build pipeline and makes debugging harder.
*   **Separate Modules**: Split the project into `slf4j-toys-core`, `slf4j-toys-javax`, and `slf4j-toys-jakarta`.
    *   **Rejected because**: Overkill for just two or three classes. It would significantly increase the maintenance burden of the project structure.
*   **Reflection-based Proxy**: Use reflection to detect the available Servlet API at runtime.
    *   **Rejected because**: Leads to "string-ly typed" code that is hard to maintain, test, and lacks compile-time safety.

## Implementation

*   The `pom.xml` uses `<optional>true</optional>` and `<scope>provided</scope>` for both Servlet APIs to ensure they are not transitively forced upon users.
*   The `slf4j-2.0-javax` profile explicitly excludes Jakarta classes from the build to prevent compilation failures when only the `javax` API is present.

## References

*   [pom.xml](../pom.xml)
*   [WatcherServlet.java](../src/main/java/org/usefultoys/slf4j/watcher/WatcherServlet.java)
*   [WatcherJavaxServlet.java](../src/main/java/org/usefultoys/slf4j/watcher/WatcherJavaxServlet.java)
