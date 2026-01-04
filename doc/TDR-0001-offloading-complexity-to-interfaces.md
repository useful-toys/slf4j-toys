# TDR-0001: Offloading Complexity to Interfaces

**Status**: Accepted
**Date**: 2026-01-03

## Context

The `Meter` and `MeterData` classes are the core of the `slf4j-toys` library, responsible for operation lifecycle management and data collection. As the library evolved, several distinct sets of responsibilities emerged:
1.  **Core Lifecycle**: Starting, stopping, and reporting progress.
2.  **Context Management**: Attaching and removing key-value pairs to operations.
3.  **Functional Execution**: Running `Runnable` and `Callable` tasks with automatic lifecycle handling.
4.  **Data Analysis**: Calculating durations, success rates, and state queries from raw metrics.

Including all these responsibilities directly within `Meter` and `MeterData` would lead to "God Objects"—massive classes that are difficult to maintain, test, and understand. Furthermore, `MeterData` needs to remain a clean Data Transfer Object (DTO) while still providing analytical capabilities.

## Decision

We decided to offload specific responsibilities into dedicated interfaces with default methods, effectively using them as **Mixins** to simulate multiple inheritance of behavior. This is complemented by a **Hybrid Approach** using utility classes for internal logic:

*   **Mixins (Interfaces)**: Used for **Public API** behavior that we want to "inject" into the class.
    *   **`MeterContext<T>`**: Handles all context-related logic (fluent `ctx()` and `unctx()` methods).
    *   **`MeterExecutor<T>`**: Handles functional execution logic (`run()`, `call()`, `safeCall()`).
    *   **`MeterAnalysis`**: Handles calculations and state queries based on raw data.
*   **Utility Classes**: Used for **Internal/Cross-cutting** logic that doesn't belong to the public API or is too complex for default methods.
    *   **`MeterValidator`**: Centralizes complex validation rules and error logging, keeping `Meter` focused on lifecycle.
    *   **`*Json5` (e.g., `MeterDataJson5`)**: Handles serialization/deserialization, allowing for a parallel hierarchy of serializers that matches the data hierarchy without bloating the DTOs.

`Meter` and `MeterData` implement the mixin interfaces, providing only the necessary state-accessing methods (getters) or core logic implementations, while inheriting the complex "business logic" via default methods. Internal operations are delegated to the utility classes.

## Consequences

**Positive**:
*   **Separation of Concerns**: Each interface has a single, well-defined responsibility.
*   **Reduced Class Size**: `Meter` and `MeterData` source files remain focused on state management and core lifecycle.
*   **Improved Maintainability**: Changes to context logic or execution patterns are isolated in their respective interfaces.
*   **Reusability**: The analytical logic in `MeterAnalysis` can be reused by any class that can provide the required raw data.
*   **Fluent API**: Default methods allow maintaining a rich fluent API without bloating the implementation classes.
*   **Testability**: Responsibilities can be tested in isolation using simple mocks that implement the interface. This allowed extracting dedicated unit test classes for each subject (e.g., `MeterContextTest`, `MeterExecutorTest`).

**Negative**:
*   **Increased Hierarchy Complexity**: Developers need to navigate multiple files to understand the full capability of a `Meter`.
*   **Default Method Limitations**: Interfaces cannot hold state, so they rely on abstract getters that the implementing classes must provide.

**Neutral**:
*   **Java 8+ Requirement**: This pattern heavily relies on default methods, which is consistent with our Java 8+ compatibility goal.
*   **Implicit Contract & Coupling**: The interfaces establish an implicit contract where default methods rely on abstract methods (getters/setters/core logic) that the class must implement. This creates a bidirectional coupling between the interface's logic and the class's state.

## Alternatives

*   **Monolithic Classes**: Keep all logic in `Meter` and `MeterData`.
    *   **Rejected because**: Leads to unmaintainable code and violates the Single Responsibility Principle.
*   **Utility Classes**: Use static methods (e.g., `MeterUtils.ctx(meter, key, value)`).
    *   **Rejected because**: Breaks the fluent API pattern and makes the code less intuitive to use.
*   **Composition/Delegation**: Use helper objects inside `Meter`.
    *   **Rejected because**: Increases boilerplate (delegation methods) and memory overhead per `Meter` instance.

## Implementation

*   `MeterData` implements `MeterAnalysis`.
*   `Meter` implements `MeterContext<Meter>`, `MeterExecutor<Meter>`, and `Closeable`.
*   `MeterExecutor` extends `MeterAnalysis` to allow execution logic to query the meter's state (e.g., `isStarted()`).
*   Lombok `@Getter` is used in `MeterData` to automatically satisfy the abstract getters required by `MeterAnalysis`.

## References

*   [Meter Class Diagram](../docs/meter-class-diagram.md)
*   [Meter.java](../src/main/java/org/usefultoys/slf4j/meter/Meter.java)
*   [MeterData.java](../src/main/java/org/usefultoys/slf4j/meter/MeterData.java)
