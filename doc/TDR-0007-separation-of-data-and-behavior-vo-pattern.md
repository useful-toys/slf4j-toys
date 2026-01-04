# TDR-0007: Separation of Data and Behavior (VO Pattern)

**Status**: Accepted
**Date**: 2026-01-03

## Context

During the evolution of the library, a recurring design conflict emerged: should core classes like `MeterData` and `WatcherData` be **Pure Data Classes** (Value Objects/DTOs) or **Behavioral Classes**?

*   **Pure Data Classes (VO/DTO)**: Are simpler, more intuitive for data transport, and easier to serialize/deserialize. They follow the "Anemic Domain Model" pattern which is often practical for libraries focused on data collection.
*   **Behavioral Classes**: Are more cohesive and follow Object-Oriented principles by keeping data and the logic that operates on it together. However, this can lead to bloated classes that are hard to reuse in different contexts (e.g., a class that can both collect metrics and analyze them).

## Decision

We decided to adopt a **Hybrid VO Pattern** that prioritizes clean data structures while "injecting" behavior through external mechanisms.

### Implementation Strategy

1.  **VO-Style Data Classes**: Classes ending in `Data` (e.g., `SystemData`, `MeterData`, `WatcherData`) are designed as Value Objects. Their primary responsibility is to hold the state of a captured event.
2.  **Minimal Internal Behavior**: These classes contain only the most essential behavior, such as:
    *   Basic state transitions (e.g., `collectCurrentTime()`).
    *   Serialization/Deserialization hooks (delegating to internal utility classes).
3.  **Externalized Behavior**: Complex logic is added using two main patterns:
    *   **Mixins (Interfaces with Default Methods)**: Used for public analytical behavior. For example, `MeterData` implements `MeterAnalysis`, which provides calculated metrics (durations, rates) without bloating the `MeterData.java` source file.
    *   **Utility Classes**: Used for formatting and cross-cutting concerns. For example, `WatcherDataFormatter` handles the complex string building for human-readable messages, keeping `WatcherData` focused on the raw metrics.

## Consequences

**Positive**:
*   **Practicality**: The VO approach makes it very easy to transport, clone, and store snapshots of the system state.
*   **Cohesion via Mixins**: By using Mixins, we maintain the "feeling" of a behavioral object for the user (e.g., `meterData.getDuration()`) while keeping the implementation decoupled.
*   **Clean Serialization**: Serialization logic doesn't interfere with the data structure, making it easier to maintain the manual JSON implementation (see [TDR-0004](./TDR-0004-minimalist-manual-json-implementation.md)).
*   **Testability**: Data structures can be tested for correctness independently of the logic that formats or analyzes them.

**Negative**:
*   **Spread Logic**: A developer looking only at the `Data` class might not realize all the behaviors available to it, as they are defined in interfaces or utility classes.
*   **Boilerplate for Mixins**: Requires defining abstract getters in interfaces that the VO must implement (often satisfied by Lombok's `@Getter`).

## Alternatives

*   **Rich Domain Model**: Put all logic inside the `Data` classes.
    *   **Rejected because**: Leads to massive, unmaintainable files and makes it harder to separate "collection" logic from "analysis" logic.
*   **Strict Anemic Model**: Use only static utility methods for everything.
    *   **Rejected because**: Results in a poor developer experience (e.g., `MeterUtils.getDuration(data)` instead of `data.getDuration()`).

## References

*   [TDR-0001: Offloading Complexity to Interfaces](./TDR-0001-offloading-complexity-to-interfaces.md)
*   [TDR-0004: Minimalist Manual JSON Implementation](./TDR-0004-minimalist-manual-json-implementation.md)
*   [MeterData.java](../src/main/java/org/usefultoys/slf4j/meter/MeterData.java)
*   [WatcherData.java](../src/main/java/org/usefultoys/slf4j/watcher/WatcherData.java)
