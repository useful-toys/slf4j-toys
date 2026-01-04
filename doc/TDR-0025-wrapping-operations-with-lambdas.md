# TDR-0025: Wrapping Operations with Lambdas (Runnable/Callable)

**Status**: Accepted
**Date**: 2026-01-04

## Context

A frequent reason for inconsistent `Meter` usage is control-flow complexity:

* forgetting to call `start()` or a terminal method on all paths,
* calling terminal methods multiple times (try/catch/finally),
* inconsistent classification of exceptions as REJECT vs FAIL.

Java’s functional interfaces (`Runnable`, `Callable`) provide a natural way to express “an operation” as a single unit. If the `Meter` can wrap these operations, then lifecycle management becomes consistent and less error-prone.

This is especially useful when the operation already exists as a method (or a short sequence of statements) and callers simply want to add metering behavior without rewriting the control flow. Wrapping an existing method call (or method reference) into a lambda provides a minimal, local change that adds `Meter` lifecycle logging and consistent exception classification.

It is also possible to use `try-with-resources` with `Meter` because it implements `Closeable`. However, this pattern only guarantees that *a failure will be reported* when the block exits without an explicit terminal method: `close()` will emit a FAIL with the path `"try-with-resources"`.

This does not make lifecycle usage transparent: developers still need to call `start()` at the beginning (typically in the resource declaration) and still need to explicitly call `ok()`, `reject(...)`, or `fail(...)` inside the `try` block to encode the intended outcome.

In contrast, the lambda wrappers are preferred when the goal is to make `Meter` usage truly transparent at call sites: the wrapper method can perform `start()` and will always perform the appropriate terminal call (OK/REJECT/FAIL) based on the chosen policy.

## Decision

We provide `MeterExecutor`, a mix-in interface that encapsulates lifecycle control around functional execution.

A `Meter` implementing `MeterExecutor` offers default methods that:

* start the meter if needed,
* execute the provided lambda (`Runnable` or `Callable`),
* store a return value into the context when applicable, and
* classify outcomes by mapping exceptions to REJECT or FAIL.

### Context integration

In cases where the lambda does not have direct access to the `Meter` instance (for example, when wrapping an existing method reference), code running inside the wrapper can still access the current meter by calling `Meter.getCurrentInstance()`. When invoked inside the wrapped execution (same thread), it resolves to the meter started by the wrapper.

When a `Callable` returns a value, wrapper methods store it into the meter context under a standard key:

* `"result"` (`MeterExecutor.CONTEXT_RESULT`).

This supports common diagnostics such as “what was returned” without requiring callers to manually call `ctx("result", value)`.

This convenience requires care: return values may contain sensitive information. Wrappers that store the return value do so automatically and without inspecting the value. If the return value can contain secrets or personal data, prefer wrapper methods that do not store results (e.g., `run(...)`) or explicitly remove/avoid context entries, and follow the guidance in the security-related references.

### Exception classification strategies

Different wrappers exist to encode different classification policies:

* `run(Runnable)`
  * OK on normal completion.
  * FAIL on `RuntimeException`.

* `runOrReject(Runnable, Class<? extends Exception>...)`
  * REJECT if exception type matches one of the configured classes.
  * FAIL otherwise.
  * Always rethrows the original exception.

* `call(Callable<T>)`
  * Stores result in context under `"result"`.
  * FAIL on any thrown `Exception` (and rethrows).

* `callOrRejectChecked(Callable<T>)`
  * FAIL on `RuntimeException`.
  * REJECT on checked exceptions.

* `callOrReject(Callable<T>, Class<? extends Exception>...)`
  * REJECT if exception type matches one of the configured classes.
  * FAIL otherwise.

* `safeCall(Callable<T>)`
  * FAIL on `RuntimeException`.
  * FAIL on checked exceptions and wraps them into a new `RuntimeException`.

* `safeCall(Class<E extends RuntimeException>, Callable<T>)`
  * FAIL on checked exceptions and wraps them into a caller-provided `RuntimeException` subclass.
  * If the wrapper exception cannot be created, logs an error and falls back to a generic `RuntimeException`.

This “menu of wrappers” is intentional: different operations (and different teams) need different classification rules.

### Explicit termination inside wrapped code

Even when using a wrapper, the wrapped `Runnable`/`Callable` may still call `path()`, `ok()`, `ok(path)`, `reject(cause)`, or `fail(cause)` directly.

On normal completion, wrappers only call `ok()` if the meter is not already stopped. Therefore, if the wrapped code explicitly terminates the meter and then returns normally, that explicit termination takes precedence and the wrapper will not apply an additional OK termination.

However, if the wrapped code terminates the meter and then an exception escapes, the wrapper will still apply its exception policy (REJECT/FAIL) as it rethrows the exception. To avoid inconsistent or confusing termination sequences, prefer one of these styles:

* **Preferred**: do not call terminal methods inside the lambda; let the wrapper terminate based on its policy.
* **If explicitly terminating**: ensure the lambda does not let exceptions escape after termination (or handle them inside the lambda).

## Consequences

**Positive**:

* **Consistent lifecycle**: fewer missing terminal events and fewer double-termination mistakes.
* **Explicit policy**: classification logic becomes visible in the chosen wrapper method.
* **Less boilerplate**: removes repetitive try/catch patterns around `Meter`.

**Negative**:

* **Policy selection required**: developers must choose the correct wrapper method; the library cannot know domain semantics.
* **Result logging risk**: storing `result` in context can expose sensitive data if not used carefully.

## Alternatives

* **Require manual lifecycle management**: Rejected because it is easy to get wrong in complex control-flow.
* **Single wrapper method with many flags**: Rejected because it would be harder to read at call sites.
* **Use `try-with-resources` as the main pattern**: Rejected because it only guarantees FAIL notification on implicit close and still requires explicit `start()` and explicit terminal calls for correct classification.

## Implementation

* `MeterExecutor` defines default wrappers (`run`, `call`, `runOrReject`, `callOrRejectChecked`, `callOrReject`, `safeCall`).
* `Meter` implements `MeterExecutor` and provides the lifecycle operations and context storage.

## References

* [src/main/java/org/usefultoys/slf4j/meter/MeterExecutor.java](../src/main/java/org/usefultoys/slf4j/meter/MeterExecutor.java)
* [src/main/java/org/usefultoys/slf4j/meter/Meter.java](../src/main/java/org/usefultoys/slf4j/meter/Meter.java)
* [TDR-0020: Three Outcome Types (OK, REJECT, FAIL)](TDR-0020-three-outcome-types-ok-reject-fail.md)
* [TDR-0024: try-with-resources Lifecycle Fit and Limitations](TDR-0024-try-with-resources-lifecycle-fit-and-limitations.md)
* [TDR-0006: Security Considerations in Diagnostic Reporting](TDR-0006-security-considerations-in-diagnostic-reporting.md)
