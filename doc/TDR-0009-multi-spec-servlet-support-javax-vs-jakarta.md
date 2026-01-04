# TDR-0009: Multi-Spec Servlet Support (javax vs. jakarta vs. no Servlet at all)

**Status**: Accepted
**Date**: 2026-01-03

## Context

The Java ecosystem underwent a major transition from the `javax.servlet` namespace (Java EE) to the `jakarta.servlet` namespace (Jakarta EE 9+). The `slf4j-toys` library needs to support multiple deployment environments:
1.  **JavaSE Applications**: Standalone applications without servlet containers (e.g., CLI tools, desktop apps, microservices without HTTP endpoints).
2.  **Legacy Servlet Systems**: Applications running on older application servers (e.g., Tomcat 9, WildFly 26) that use `javax.servlet`.
3.  **Modern Servlet Systems**: Applications running on newer servers (e.g., Tomcat 10+, WildFly 27+) that use `jakarta.servlet`.

The library must work correctly in all three scenarios, providing Servlet-based monitoring triggers when available while remaining fully functional in pure JavaSE environments. Supporting both servlet namespaces in a single JAR is challenging because they are binary-incompatible (different package names).

## Decision

We decided to include **duplicate implementations** for both servlet specifications within the same JAR, while maintaining full compatibility with pure JavaSE environments, using Maven profiles to manage compilation and testing.

### Implementation Strategy

1.  **Core Functionality is Servlet-Independent**: The core components (`Watcher`, `Reporter`, `Meter`, etc.) implement `Runnable` and have no servlet dependencies, making them fully functional in pure JavaSE environments.
2.  **Optional Servlet Integration**: We maintain two sets of identical (or nearly identical) servlet classes for environments that use servlet containers:
    *   `WatcherServlet` and `ReporterServlet` (using `jakarta.servlet`).
    *   `WatcherJavaxServlet` and `ReporterJavaxServlet` (using `javax.servlet`).
3.  **Maven Profiles**: The `pom.xml` defines profiles to handle the different dependencies:
    *   **`slf4j-2.0` (Default)**: Includes `jakarta.servlet-api` and compiles the Jakarta-based classes.
    *   **`slf4j-2.0-javax`**: Includes `javax.servlet-api` and uses `maven-compiler-plugin` to **exclude** the Jakarta classes from compilation to avoid errors.
4.  **Optional Dependencies**: Both servlet APIs are marked as `<optional>true</optional>` and `<scope>provided</scope>`, ensuring they are not transitively forced upon users and allowing the library to function without them.
5.  **Classloader Safety**: Since the servlet classes have different names and package-level imports, they can coexist in the same JAR. A `NoClassDefFoundError` will only occur if an application explicitly tries to load a servlet class whose underlying specification (JAR) is missing from the classpath. If the user doesn't use the servlet feature, or uses the one matching their environment, no error occurs.

## Consequences

**Positive**:
*   **Environment Flexibility**: The library works in pure JavaSE, legacy servlet (javax), and modern servlet (jakarta) environments without requiring different artifacts.
*   **Simplicity**: Avoids the complexity of creating separate artifacts (e.g., `slf4j-toys-jakarta.jar` vs `slf4j-toys-javax.jar`) or using Maven classifiers.
*   **Single Artifact**: Users only need to manage one dependency version regardless of their environment.
*   **Zero Runtime Overhead**: The unused servlet classes are simply ignored by the JVM if not referenced. In pure JavaSE environments, the servlet APIs are never loaded.
*   **No Forced Dependencies**: Servlet APIs are optional, so JavaSE applications don't get unnecessary transitive dependencies.

**Negative**:
*   **Code Duplication**: Changes to the Servlet logic must be manually synchronized between the `javax` and `jakarta` versions.
*   **Build Complexity**: The `pom.xml` requires careful configuration of `excludes` and `testExcludes` in the compiler and surefire plugins to ensure the build passes in both profiles.
*   **JAR Bloat**: The JAR contains a few extra small classes, though the impact is negligible (a few kilobytes).

**Neutral**:
*   **Developer Responsibility**: In servlet environments, the user must ensure they register the correct Servlet class (`WatcherServlet` vs `WatcherJavaxServlet`) in their `web.xml` or via annotations, matching their container's specification. In pure JavaSE environments, servlet classes are simply not used.

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
