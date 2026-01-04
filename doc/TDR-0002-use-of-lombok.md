# TDR-0002: Use of Lombok to Reduce Boilerplate

**Status**: Accepted
**Date**: 2026-01-03

## Context

Java is known for requiring significant boilerplate code, such as getters, setters, constructors, and null checks. In a library like `slf4j-toys`, which involves many data-holding classes (e.g., `MeterData`, `SystemData`, `EventData`), this boilerplate can quickly obscure the actual business logic and increase the maintenance burden. We need a way to keep the codebase concise, readable, and focused on its core logging and measurement responsibilities.

## Decision

We decided to use **Project Lombok** to automate the generation of common Java patterns through annotations.

Key annotations used across the project:
*   **`@Getter`**: Automatically generates getter methods for fields.
*   **`@NoArgsConstructor` / `@AllArgsConstructor`**: Generates constructors with specific access levels (often `PROTECTED` for DTOs).
*   **`@NonNull`**: Generates runtime null checks for method parameters and constructors.
*   **`@AccessLevel`**: Fine-grained control over the visibility of generated members.
*   **`@SneakyThrows`**: Simplifies exception handling in functional interfaces and internal logic where checked exceptions are known to be safe or need wrapping.

## Consequences

**Positive**:
*   **Conciseness**: Significantly reduces the number of lines of code in data classes.
*   **Readability**: Developers can see the fields and logic without being distracted by dozens of standard getters and constructors.
*   **Consistency**: Ensures that all getters and constructors follow the same pattern and naming conventions.
*   **Maintainability**: Adding or removing a field automatically updates the associated getters and constructors.
*   **Safety**: `@NonNull` provides a consistent and declarative way to enforce API contracts.

**Negative**:
*   **Tooling Dependency**: Requires developers to install a Lombok plugin in their IDE (IntelliJ, VS Code, etc.).
*   **Build Complexity**: Requires configuring the `maven-compiler-plugin` to handle annotation processing.
*   **"Hidden" Code**: Since the code is generated at compile time, it's not visible in the source file, which can occasionally confuse developers unfamiliar with Lombok.

**Neutral**:
*   **Bytecode Manipulation**: Lombok modifies the AST during compilation, which is a stable and well-supported technique in the Java ecosystem.

## Alternatives

*   **Manual Implementation**: Write all getters, constructors, and null checks by hand.
    *   **Rejected because**: High maintenance cost and prone to human error (e.g., forgetting to update a constructor after adding a field).
*   **IDE Code Generation**: Use IDE features to generate the code once.
    *   **Rejected because**: The boilerplate still exists in the source file, cluttering the view and requiring manual updates when fields change.
*   **Java Records (Java 14+)**: Use the native `record` keyword.
    *   **Rejected because**: The project maintains compatibility with **Java 8**, and records do not support class inheritance (which is essential for our `EventData` -> `SystemData` -> `MeterData` hierarchy).

## Implementation

*   Lombok is added as a `provided` dependency in `pom.xml`.
*   The `maven-compiler-plugin` is configured to recognize Lombok during the build process.
*   Coding standards (defined in `AI-INSTRUCTIONS.md`) encourage the use of `final` fields combined with Lombok getters to promote immutability.

## References

*   [Project Lombok Official Site](https://projectlombok.org/)
*   [MeterData.java](../src/main/java/org/usefultoys/slf4j/meter/MeterData.java)
*   [SystemData.java](../src/main/java/org/usefultoys/slf4j/internal/SystemData.java)
*   [AI-INSTRUCTIONS.md](../AI-INSTRUCTIONS.md)
