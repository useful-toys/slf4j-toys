# TDR-0030: Chained API Over Fluent API

**Status**: Accepted  
**Date**: 2026-01-09

## Context

When designing an API that supports method chaining and guides developers through a recommended sequence, two architectural patterns are commonly considered:

### **Fluent API (Typed Builders)**

A fluent API uses distinct return types to enforce call order at compile time. Each method returns a different interface or class that restricts which methods can be called next:

```java
// Fluent example (hypothetical):
StartedMeter m = new Meter(logger)
    .withOperation("doWork")      // MeterBuilder
    .build()                        // returns Meter
    .start();                       // returns StartedMeter

TerminatedMeter m2 = m.ok();       // StartedMeter.ok() returns TerminatedMeter
```

**Advantages of Fluent:**
- Enforces call order at compile time
- Type system guides developer to valid methods
- Compiler rejects out-of-order calls

**Disadvantages of Fluent:**
- Multiple types and variable shadowing
- Rigid patterns (incompatible with try-catch-finally, exception handlers)
- Violates minimalism: requires multiple variable assignments
- Produces boilerplate in practical usage

### **Chained API (Homogeneous Return)**

A chained API returns the same type from all methods, allowing a single instance to persist through the entire operation lifecycle:

```java
// Chained (current Meter):
Meter m = new Meter(logger).start();

// ... long operation code ...
m.inc();
m.progress();
m.incTo(50);
m.path("validation-passed");
m.ok();
// Same `m` throughout
```

**Advantages of Chained:**
- Single variable, no shadowing
- Minimal boilerplate
- Flexible: adapts to any runtime flow
- Natural lifecycle: state changes, but instance persists

**Disadvantages of Chained:**
- No compile-time enforcement of order
- Developers rely on documentation and logs
- IDE offers all methods in all states

## Decision

The `Meter` class uses a **chained API** rather than a fluent API.

### Rationale

The Meter is **not a builder**. It is a **first-class domain object that models an operation in progress**. The lifecycle of a Meter is:

1. **Creation**: `new Meter(logger)` — instantiated once
2. **Operation**: `m.start()`, `m.inc()`, `m.progress()`, etc. — used throughout operation code
3. **Termination**: `m.ok()`, `m.reject()`, `m.fail()` — called when operation ends

The same Meter instance persists across all these stages, with state changing but the reference remaining constant.

#### Practical Usage Pattern

Developers use Meter as follows:

```java
Meter m = new Meter(logger).start();

// ... long operation code (potentially hundreds of lines) ...

m.inc();                                  // update progress
m.progress();                             // report status
m.incTo(targetCount);                    // set iteration milestone
m.path("validation-passed");             // record decision path
m.ctx("userId", currentUserId);          // add context

// ... more operation code ...

if (success) {
    m.ok();
} else if (expectedFailure) {
    m.reject(cause);
} else {
    m.fail(exception);
}
```

This pattern requires:

1. **A single variable `m`** that remains the same type throughout
2. **Minimal boilerplate** in the operation code
3. **Transparency** — calls to `m.ok()` are clear and self-documenting
4. **Flexibility** — the same `m` works in any control flow (try-catch-finally, exception handlers, branches)

A fluent API would break this pattern:

```java
// Fluent (problematic):
StartedMeter m = new Meter(logger)
    .withOperation("doWork")
    .build()
    .start();

// ... long operation code ...

m.inc();                              // works, m is StartedMeter
m.progress();                         // works
m.incTo(50);                          // works

TerminatedMeter m2 = m.ok();          // variable type changed!
// Now m2 (TerminatedMeter), not m, should be used
// But code elsewhere still references m
// Variable shadowing, boilerplate, confusion
```

#### Alignment with Meter Philosophy

The Meter is designed to be:

- **Transparent**: The operation code should focus on business logic, not on meter bookkeeping
- **Minimalist**: Each call to the meter should be concise and unobtrusive
- **Non-intrusive**: Errors in meter usage should not break the application

A fluent API would require developers to track type changes across the operation, adding cognitive load and boilerplate, which directly contradicts these principles.

#### State as a Guide, Not Enforcement

Instead of compile-time enforcement of order:

- **State tracking** (`isStarted()`, `isStopped()`, `isOK()`, etc.) provides runtime insight
- **Logging** (via markers like `INCONSISTENT_START`, `ILLEGAL`) alerts developers to out-of-order usage
- **Resilience** (four-tier strategy from [TDR-0029](TDR-0029-resilient-state-transitions-with-chained-api.md)) gracefully handles violations

This approach is pragmatic: it allows the library to work with real-world code (which often has complex control flow) while still guiding developers toward correct usage.

#### Minimalismo: Chained vs. Fluent Verbosity

The Meter API prioritizes **minimalism and conciseness** in operation code. This design principle directly favors chained API over fluent:

**Chained (minimal):**
```java
Meter m = new Meter(logger).start();
// ... operation code ...
m.inc();
m.progress();
m.ok();
```

**Fluent (verbose):**
```java
Meter m = new MeterBuilder(logger)
    .withOperation("doWork")
    .build()
    .start();

// ... operation code ...
m.inc();
StartedMeter m2 = m.ok();  // type changed, reassignment required
// or: variable shadowing and confusion

m2.close();  // now must use m2, not m
```

The chained API achieves the same functionality with **zero boilerplate**, while fluent introduces:
- Multiple intermediate types
- Variable reassignments or shadowing
- Mental overhead tracking type changes
- Boilerplate that clutters operation code

For a monitoring/telemetry library, this boilerplate is especially undesirable because:
- It distracts from the actual operation being measured
- It contradicts the principle of transparency (meter instrumentation should be invisible to business logic)
- It increases code maintenance burden

The chained API keeps meter instrumentation **lightweight and unobtrusive**, allowing it to be used extensively without degrading code readability.

## Consequences

**Positive**:

* **Minimalism**: A single variable `m` throughout the operation
* **Transparency**: Code reads naturally — `m.ok()` is clear without type gymnastics
* **Flexibility**: Works with any control flow (try-catch-finally, exception handlers, early returns)
* **Single instance lifecycle**: One Meter object models one operation, with state changes but identity preserved
* **Pragmatism**: Functions in real-world code with unexpected flows
* **Simplicity**: Single type, no variable shadowing, no boilerplate

**Negative**:

* **No compile-time safety**: Out-of-order calls are not caught by the compiler
* **Relies on developer discipline**: Developers must read logs and documentation to understand correct usage
* **IDE does not restrict methods**: All methods appear in autocomplete in all states

**Neutral**:

* **Trade-off documented**: The [TDR-0029](TDR-0029-resilient-state-transitions-with-chained-api.md) strategy explicitly handles the trade-off between flexibility and guidance

## Alternatives

### 1. **Fluent API with Type Hierarchy**

Use distinct types (`MeterBuilder`, `StartedMeter`, `TerminatedMeter`, etc.) to enforce order at compile time.

**Rejected because**:

* **Multiple variable types** throughout operation code creates boilerplate and shadows variables
* **Incompatible with real-world control flow**: try-catch-finally patterns would require reassigning variables or multiple meter instances
* **Violates minimalism principle**: Developers would need to track type changes and reassign variables, adding cognitive load
* **Exception handling becomes complex**: If an exception occurs mid-operation, the meter type might not match the handler's expectations
* **Operation code becomes less transparent**: The focus shifts from business logic to meter type management

### 2. **Builder Pattern Only (No Enforcement After Build)**

Offer a fluent builder to configure the Meter before creation, then use a chained API for the operation:

```java
Meter m = MeterBuilder.create(logger)
    .withOperation("doWork")      // builder (fluent)
    .build()                        // returns Meter (chained)
    .start();

// Then chained API for the operation
m.inc();
m.ok();
```

**Partially adopted**:

* A fluent builder could be useful for complex Meter configuration before `build()`
* But once the Meter is created and operating, chained API is the right choice
* This is complementary, not contradictory, to the chained API decision

### 3. **Annotations for Validation**

Use annotations (e.g., `@ValidState("STARTED")`) to mark methods valid in specific states, with compile-time or runtime validation tools.

**Rejected because**:

* Adds complexity without addressing the core issue (variable types remain homogeneous anyway)
* Validation would still be runtime-based (tools like Checker Framework are optional)
* Does not solve the variable shadowing problem

## Implementation

The chained API is implemented in:

* [src/main/java/org/usefultoys/slf4j/meter/Meter.java](../src/main/java/org/usefultoys/slf4j/meter/Meter.java) — All methods return `this` (or `Meter`)
* [src/main/java/org/usefultoys/slf4j/meter/MeterAnalysis.java](../src/main/java/org/usefultoys/slf4j/meter/MeterAnalysis.java) — Query methods (`isStarted()`, `isStopped()`, etc.) allow state inspection
* [src/main/java/org/usefultoys/slf4j/meter/MeterValidator.java](../src/main/java/org/usefultoys/slf4j/meter/MeterValidator.java) — Validation logic emits logs for guidance

The four-tier resilience strategy ([TDR-0029](TDR-0029-resilient-state-transitions-with-chained-api.md)) provides practical guidance when usage deviates from expected patterns.

## References

* [TDR-0029: Resilient State Transitions with Chained API](TDR-0029-resilient-state-transitions-with-chained-api.md) — Explains how the chained API handles out-of-order calls gracefully
* [TDR-0017: Non-Intrusive Validation and Error Handling](TDR-0017-non-intrusive-validation-and-error-handling.md) — Explains the principle of not throwing exceptions in library code
* [TDR-0019: Immutable Lifecycle Transitions](TDR-0019-immutable-lifecycle-transitions.md) — Explains state transitions and immutability guarantees
* [src/main/java/org/usefultoys/slf4j/meter/Meter.java](../src/main/java/org/usefultoys/slf4j/meter/Meter.java) — Implementation of the chained API
