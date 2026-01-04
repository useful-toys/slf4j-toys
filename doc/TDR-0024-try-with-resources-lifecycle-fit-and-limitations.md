# TDR-0024: try-with-resources Lifecycle Fit and Limitations

**Status**: Accepted
**Date**: 2026-01-04

## Context

A common source of monitoring gaps is forgetting to close/terminate instrumentation objects in all control-flow paths.

Java’s `try-with-resources` provides a robust language-level mechanism to ensure that `close()` is executed even when:

* there are early returns,
* exceptions are thrown, or
* complex branching occurs.

Because `Meter` models the lifecycle of an operation, it is desirable to align its termination semantics with `try-with-resources`.

## Decision

`Meter` implements `Closeable` and supports usage in a `try-with-resources` block.

If an operation reaches the end of the `try` block without an explicit terminal call (`ok()`, `reject(cause)`, or `fail(cause)`), then `close()` automatically classifies the operation as:

* **FAIL**, with failure path `"try-with-resources"`.

This ensures the invariant: **every started `Meter` used in try-with-resources will emit a terminal event**, even if the caller forgets to call a termination method.

However, it is important to note what this pattern *does not* do: it only guarantees failure notification on implicit close. It does not make `start()` automatic (callers still need to call it in the resource declaration), and it does not make `ok()`/`reject(...)`/`fail(...)` automatic (callers still need to explicitly choose and call the intended terminal method inside the `try` block). In other words, `try-with-resources` can demarcate an operation block, but it still leaves significant lifecycle responsibility to the developer.

## Limitations and “Exceptions” (Practical Edge Cases)

This alignment has intentional limitations that callers must understand.

1. **close() cannot infer intent**
   * If the operation ends “successfully” but the caller simply forgot to call `ok()`, `close()` will record a FAIL.
   * Therefore, callers must still explicitly call `ok()` / `reject()` / `fail()` to encode the intended outcome.

2. **Thrown exceptions are not automatically classified**
   * If code inside the try block throws and the exception propagates, `close()` runs, but it still cannot decide whether the exception should map to REJECT or FAIL.
   * Without a catch that classifies the outcome, `close()` will record FAIL with `"try-with-resources"`, while the original exception still propagates.

3. **Outcome may be logged before “final” application behavior**
   * If a caller calls `ok()` (or `reject()`/`fail()`) and then later throws from a `finally` block, the `Meter` outcome is already frozen (first termination wins).
   * This is correct for immutability, but it means callers must structure code so that the chosen outcome truly represents the final behavior.

4. **Does not solve async/multi-thread lifecycle**
   * try-with-resources scopes `close()` to the current thread and lexical block.
   * It cannot automatically cover work that continues asynchronously after the block ends.

5. **Start is still explicit**
   * `try-with-resources` guarantees `close()`, not `start()`.
   * A meter that is created but never started can still produce validation warnings depending on how it is used.

## Consequences

**Positive**:

* **Reliable termination**: ensures there is always a terminal event for instrumented operations.
* **Less boilerplate**: reduces the need for manual `finally` blocks to guarantee termination.

**Negative**:

* **Intent must still be explicit**: classification defaults to FAIL when termination is forgotten.
* **Not a replacement for exception classification**: callers still need to map domain exceptions to REJECT vs FAIL.

## Conclusion

Using `Meter` in a `try-with-resources` block is useful only for a small set of very simple cases where the main requirement is “always terminate” and where detailed failure diagnostics are not required.

For most real operations, the recommended alternative is to wrap the operation using the provided `Runnable`/`Callable` (lambda) wrapper methods (see `MeterExecutor`). Those wrappers can manage the lifecycle more completely, covering the intended OK/REJECT/FAIL variants and providing automatic exception classification strategies.

## Examples

These examples are intentionally minimal and focus on how lifecycle termination is achieved.

### try-with-resources (simple, minimal diagnostics)

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.usefultoys.slf4j.meter.Meter;

class Example {
   private static final Logger LOG = LoggerFactory.getLogger(Example.class);

   void doWork() {
      try (Meter meter = new Meter(LOG).start()) {
         // ... work ...
         meter.ok();
      }
   }
}
```

### Runnable/Callable wrappers (recommended)

Using wrappers reduces boilerplate and provides consistent OK/REJECT/FAIL handling.

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.usefultoys.slf4j.meter.Meter;

class Example {
   private static final Logger LOG = LoggerFactory.getLogger(Example.class);

   static class BusinessException extends Exception {
      BusinessException(String message) { super(message); }
   }

   void doWork() {
      // OK on normal completion; FAIL on RuntimeException.
      new Meter(LOG, "doWork").run(() -> {
         // ... work ...
      });

      // REJECT for specific exception types; FAIL otherwise (always rethrows).
      // This is useful for “business exceptions” that are expected outcomes.
      new Meter(LOG, "doWork").callOrReject(() -> {
         // ... work that may throw ...
         throw new BusinessException("validation rejected");
      }, BusinessException.class);
   }
}
```

## Alternatives

* **Do nothing on close()**: Rejected because it would silently drop terminal events.
* **Always classify close() as OK**: Rejected because it would hide forgotten terminal calls and over-report success.

## Implementation

* `Meter.close()` sets `failPath = "try-with-resources"` if the meter was not explicitly stopped.

## References

* [src/main/java/org/usefultoys/slf4j/meter/Meter.java](../src/main/java/org/usefultoys/slf4j/meter/Meter.java)
* [TDR-0019: Immutable Lifecycle Transitions](TDR-0019-immutable-lifecycle-transitions.md)
* [TDR-0025: Wrapping Operations with Lambdas (Runnable/Callable)](TDR-0025-wrapping-operations-with-lambdas.md)
