# TDR-0015: ThreadLocal Stack for Context Propagation

**Status**: Accepted
**Date**: 2026-01-03

## Context
In complex applications, operations are often nested (e.g., a web request calling a service, which calls a database). Each operation typically has its own `Meter` instance to track its metrics independently. However, without a thread-level context, there is no automatic way to determine which operation (Meter) is the "parent" and which is a "child", or to establish the parent-child relationship hierarchy.

For example, when a database operation starts, how would it know which service operation initiated it, or which web request that service was handling? Passing the parent `Meter` explicitly through every method call would solve this but at the cost of coupling unrelated components solely for monitoring purposes.

Additionally, as a nested operation is being executed through multiple method calls, there is often a need to add contextual information to the operation (via `Meter.ctx()` methods) from within these methods. However, requiring the `Meter` to be explicitly passed as a parameter to every method would be intrusive and tightly couple the business logic to monitoring concerns.

The primary goals are:
1. Allow child operations to automatically obtain their parent `Meter` from the thread context, enabling nested operation tracking without explicit parameter passing.
2. Allow any code executing within an operation to add context to that operation via `Meter.getCurrentInstance().ctx(...)` without requiring the `Meter` to be explicitly passed through the entire call chain.

## Decision
We implemented a `ThreadLocal` stack of `Meter` instances to track the "current" operation on each thread. This provides a transparent way to obtain the parent meter via the thread context.

### Implementation Details
1.  **`ThreadLocal<WeakReference<Meter>>`**: We use a `ThreadLocal` to store a reference to the most recently started `Meter` on the current thread. This is the "current" operation in the hierarchy.
2.  **Nested Meter Stack**: When a new operation starts, its `Meter` instance becomes the "current" operation on the thread. When the new `Meter` is created, it automatically captures the previous `Meter` (the parent), forming a parent-child relationship.
3.  **Weak References**: To prevent memory leaks in case a `Meter` is not properly stopped (and thus not removed from the `ThreadLocal`), we store `WeakReference<Meter>` instead of direct references.
4.  **Linked List Stack**: Each `Meter` instance has a `previousInstance` field (also a `WeakReference`) that points to the `Meter` that was active before it. This forms a linked list that acts as a stack, allowing traversal from child to parent operations.
5.  **Lifecycle Management**:
    *   `start()`: Pushes the current `Meter` onto the stack (by capturing the previous instance) and updates the `ThreadLocal` to point to this new `Meter`.
    *   `ok()`, `reject()`, `fail()`, `close()`: Pops the `Meter` from the stack by restoring the `previousInstance` to the `ThreadLocal`, making the parent operation current again.
6.  **Context Retrieval**: `Meter.getCurrentInstance()` allows any code to retrieve the active (current) `Meter` on the current thread without passing it as a parameter. This enables child operations to automatically obtain their parent. If no `Meter` is active, it returns a dummy instance (Null Object Pattern).

## Consequences
**Positive**:
*   **Automatic Hierarchy**: Enables automatic parent-child relationship tracking for nested operations without explicit parameter passing.
*   **Context-Free Context Addition**: Any code within an operation can add contextual information via `Meter.getCurrentInstance()` methods such as `ctx(...)`, `increment(...)`, and `path(...)` without requiring the `Meter` to be explicitly passed through method signatures. This keeps business logic decoupled from monitoring infrastructure.
*   **Context Propagation**: Allows logging utilities or other components to enrich logs with the current operation's ID or context without explicit parameter passing.
*   **Safety**: `WeakReference` ensures that unstopped meters don't prevent garbage collection or cause permanent memory leaks in long-lived threads (like thread pools).
*   **Robustness**: `getCurrentInstance()` always returns a valid object, avoiding null checks in client code.

**Negative**:
*   **Overhead**: Every `start()` and `stop` operation involves `ThreadLocal` access and object allocation (`WeakReference`).
*   **Complexity**: Managing the stack manually in the lifecycle methods is error-prone and requires careful validation (handled by `MeterValidator`).
*   **Thread Affinity**: This mechanism only works within a single thread. Context propagation across asynchronous boundaries (e.g., `CompletableFuture`, `ParallelStream`) requires manual intervention.

## Alternatives
*   **Explicit Passing**: Pass the `Meter` instance as a parameter to every method. **Rejected because** it's too intrusive and clutters the business logic.
*   **MDC (Mapped Diagnostic Context)**: Use SLF4J's MDC. **Rejected because** MDC only stores strings and doesn't provide the rich object-oriented context and lifecycle tracking offered by `Meter`.

## Implementation
*   [src/main/java/org/usefultoys/slf4j/meter/Meter.java](src/main/java/org/usefultoys/slf4j/meter/Meter.java): Implements the stack logic using `localThreadInstance` and `previousInstance`.
*   `Meter.getCurrentInstance()`: Provides the entry point for context retrieval.

## References
*   [TDR-0011: Null Object Pattern for Optional Logging](TDR-0011-null-object-pattern-for-optional-logging.md)
*   [TDR-0017: Non-Intrusive Validation and Error Handling](TDR-0017-non-intrusive-validation-and-error-handling.md)
