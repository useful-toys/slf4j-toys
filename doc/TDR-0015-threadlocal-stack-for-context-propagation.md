# TDR-0015: ThreadLocal Stack for Context Propagation

**Status**: Accepted
**Date**: 2026-01-03

## Context
In complex applications, operations are often nested (e.g., a web request calling a service, which calls a database). To provide meaningful diagnostics, it's useful to know which operation is currently active on a thread, allowing for parent-child relationship tracking and context propagation without explicitly passing `Meter` instances through every method call.

The primary goal is to allow the creation of 'sub' meters from a parent meter without forcing the parent to be passed as a parameter to child operations. This avoids coupling unrelated components solely for the purpose of monitoring.

## Decision
We implemented a `ThreadLocal` stack of `Meter` instances to track the "current" operation on each thread. This provides a transparent way to obtain the parent meter via the thread context.

### Implementation Details
1.  **`ThreadLocal<WeakReference<Meter>>`**: We use a `ThreadLocal` to store a reference to the most recently started `Meter` on the current thread.
2.  **Weak References**: To prevent memory leaks in case a `Meter` is not properly stopped (and thus not removed from the `ThreadLocal`), we store `WeakReference<Meter>` instead of direct references.
3.  **Linked List Stack**: Each `Meter` instance has a `previousInstance` field (also a `WeakReference`) that points to the `Meter` that was active before it. This forms a linked list that acts as a stack.
4.  **Lifecycle Management**:
    *   `start()`: Pushes the current `Meter` onto the stack and updates the `ThreadLocal`.
    *   `ok()`, `reject()`, `fail()`, `close()`: Pops the `Meter` from the stack by restoring the `previousInstance` to the `ThreadLocal`.
5.  **Context Retrieval**: `Meter.getCurrentInstance()` allows any code to retrieve the active `Meter` on the current thread. If no `Meter` is active, it returns a dummy instance (Null Object Pattern).

## Consequences
**Positive**:
*   **Automatic Hierarchy**: Enables automatic parent-child relationship tracking for nested operations.
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
