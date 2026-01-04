# TDR-0004: Minimalist Manual JSON Implementation

**Status**: Accepted
**Date**: 2026-01-03

## Context

The `slf4j-toys` library aims to be a lightweight, zero-dependency extension for SLF4J. While we decided to use JSON for machine-parsable data logging (see [TDR-0003](./TDR-0003-separation-of-log-channels.md)), adding a full-featured JSON library (like Jackson or Gson) would significantly increase the library's footprint and introduce transitive dependencies. We need a way to produce and consume structured data while maintaining absolute minimalism.

## Decision

We decided to implement a **custom, minimalistic JSON serializer and parser** manually, without using any external libraries.

### Implementation Details

1.  **Manual Serialization**: Data is written directly to a `StringBuilder` using `String.format()` or simple string concatenation. This ensures maximum performance and zero overhead.
2.  **Regex-based Parsing**: Deserialization is performed using a set of pre-compiled regular expressions (`java.util.regex.Pattern`) to extract specific keys and values from the JSON string.

## Consequences

**Positive**:
*   **Zero Dependencies**: The library remains pure SLF4J, avoiding "dependency hell" for users.
*   **Minimal Footprint**: No extra JARs are required, keeping the library size extremely small.
*   **High Performance**: Manual string building and targeted regex matching are faster than general-purpose JSON reflection-based libraries for our specific use case.

**Negative**:
*   **Flat Structure Only**: The manual implementation and regex parser do not support nested objects or complex hierarchies. The JSON must be a simple collection of key-value pairs.
*   **Strict Format Requirements**: The parser is sensitive to the exact format of the JSON string. It expects a specific style (often referred to as JSON5-like, sometimes omitting quotes for brevity).
*   **Limited Data Types**: Only simple types (Strings, Numbers, Booleans) and simple tuples are supported.
*   **Maintenance Burden**: Any change to the data structure requires manual updates to both the `write` and `read` methods in the corresponding `*Json5` utility classes.
*   **Testing Requirements**: Since the implementation is manual and custom, it requires extensive unit test coverage to ensure correctness and prevent regressions in both serialization and parsing. Fortunately, this high-coverage requirement is mitigated by using AI to generate comprehensive test suites.

**Neutral**:
*   **Acceptable Trade-off**: While the limitations are significant, the current logging requirements of the library are fully met within these bounds. The simplicity of the data being logged (metrics and status) does not require complex JSON features.

## Alternatives

*   **External JSON Library (Jackson/Gson)**:
    *   **Rejected because**: Violates the minimalism goal and introduces unwanted dependencies.
*   **Java's Built-in JSON API (javax.json)**:
    *   **Rejected because**: It is not part of the standard SE 8 JDK and would still require an implementation JAR.
*   **Binary Format (Protobuf/Avro)**:
    *   **Rejected because**: Not human-readable and requires complex tooling/dependencies.

## Implementation

*   Serialization and deserialization logic is isolated in package-private utility classes: `EventDataJson5`, `SystemDataJson5`, and `MeterDataJson5`.
*   These classes use pre-compiled `Pattern` objects for efficiency.
*   The `writeJson5` and `readJson5` methods in the data classes delegate to these utilities.

## References

*   [EventDataJson5.java](../src/main/java/org/usefultoys/slf4j/internal/EventDataJson5.java)
*   [SystemDataJson5.java](../src/main/java/org/usefultoys/slf4j/internal/SystemDataJson5.java)
*   [MeterDataJson5.java](../src/main/java/org/usefultoys/slf4j/meter/MeterDataJson5.java)
*   [TDR-0003: Separation of Log Channels (Dual Logging)](./TDR-0003-separation-of-log-channels.md)
