# TDR-0035: PhantomReference Leak Detector for Forgotten Meters

**Status**: Accepted  
**Date**: 2026-07-02

## Context

A `Meter` describes an operation with an explicit lifecycle: it is `start()`-ed and must be terminated by exactly one of `ok()`, `reject()`, `fail()`, or `close()`. When application code starts a meter and never terminates it — a genuine misuse — the library should surface the mistake instead of hiding it.

Historically this was detected in `Object.finalize()`, which delegated to `MeterValidator.validateFinalize()`: when the JVM finalized an unreachable `Meter` that had been started but never stopped, an `ERROR` was logged. This mechanism has become untenable:

- **`finalize()` is deprecated for removal** (JEP 421). Code that overrides it will eventually stop compiling and, before that, emits deprecation warnings.
- **Finalization is pathological.** It runs on a single shared finalizer thread, is non-deterministic, delays reclamation by at least one extra GC cycle, and allows object resurrection.

Replacing it is constrained by hard design principles of this library:

- **The `Meter` must stay transparent to the application.** Following the non-intrusive philosophy of [TDR-0017](TDR-0017-non-intrusive-validation-and-error-handling.md), starting or stopping a meter must not acquire a library-owned lock or otherwise impose a side effect the application can observe as contention. Leak detection is a diagnostic aid; it must never become a synchronization point on the application's hot path.
- **No memory leak.** A forgotten, started meter must remain garbage-collectable. The thread-local lifecycle stack already holds meters through `WeakReference` precisely so a forgotten meter is never pinned (see [TDR-0015](TDR-0015-threadlocal-stack-for-context-propagation.md)); the leak detector must not reintroduce a strong retention path.
- **No background thread and no JVM shutdown hook.** A library-owned daemon thread or shutdown hook can pin a web-application class loader in servlet containers (Tomcat, TomEE, …), causing redeploy leaks.
- **Portability from Java 8 onward**, including future JDKs where finalization is removed entirely.

## Decision

Detect forgotten meters with a `PhantomReference` + `ReferenceQueue`, implemented in the package-private `MeterLeakDetector`. The `Meter` owns a single handle field and calls into the detector at the lifecycle boundaries:

- **On `start()`** (gated by `MeterConfig.detectLeaks` and skipping the `UNKNOWN` category), `register(this)` creates a `MeterReference` — a `PhantomReference<Meter>` that snapshots `fullID` and the message `Logger` — and adds it to a **static anchor set**.
- **On every explicit termination** (`ok()`/`reject()`/`fail()`/`close()`), `deregister(ref)` removes the reference from the anchor and calls `ref.clear()`.
- **Draining is opportunistic**: `register()` first calls `drain()`, on the caller's own thread. Any reference still present in the anchor when the garbage collector enqueues it is — by construction — a meter that was started and never stopped, and is reported with an `ERROR` under the `INVALID_ARGUMENT` marker (byte-for-byte equivalent to the former `finalize()` message).

```java
private static final ReferenceQueue<Meter> QUEUE = new ReferenceQueue<>();
private static final Set<MeterReference> ANCHOR = ConcurrentHashMap.newKeySet();

static MeterReference register(final Meter meter) {
    drain();
    final MeterReference ref = new MeterReference(meter, QUEUE);
    ANCHOR.add(ref);      // keeps the ref reachable independently of the meter
    return ref;
}

static void deregister(final MeterReference ref) {
    if (ref == null) return;
    if (ANCHOR.remove(ref)) {   // membership IS the "still registered" state
        ref.clear();            // a stopped meter is never enqueued
    }
}

static void drain() {
    Reference<? extends Meter> r;
    while ((r = QUEUE.poll()) != null) {
        final MeterReference ref = (MeterReference) r;
        if (ANCHOR.remove(ref)) {  // still anchored => never stopped => leak
            ref.reportLeak();
        }
    }
}
```

**The anchor set is mandatory, not bookkeeping.** The garbage collector only enqueues a `PhantomReference` whose *reference object* is itself reachable through a strong path **independent of its referent**. A `MeterReference` held only by its own `Meter` (for example, through a field on the meter) becomes unreachable at the same instant as the meter and is collected *with* it — never enqueued — so the leak would silently go unreported. The static `ConcurrentHashMap.newKeySet()` keeps every live registration reachable from a GC root independently of the meter, until the meter is either stopped (deregistered) or collected and drained.

The anchor set is a `ConcurrentHashMap`-backed set, so `add`/`remove` are CAS operations on independent bins — no library-owned monitor lock. The atomic `Set.remove(Object)` also claims each reference exactly once, providing idempotency and de-duplication between concurrent drains, so no separate per-reference flag or counter is required.

## Consequences

### Positive ✅

- **Forward-compatible**: no `finalize()` override anywhere; the mechanism relies only on `java.lang.ref`, stable since Java 2 and unaffected by JEP 421.
- **No lock on the hot path**: `start()`/`stop()` touch a lock-free concurrent set (per-bin CAS), never a monitor. This honors the transparency principle of [TDR-0017](TDR-0017-non-intrusive-validation-and-error-handling.md).
- **No background thread, no shutdown hook**: draining happens synchronously on whichever application thread starts the next meter, so nothing can pin a servlet container's class loader.
- **No memory leak**: the anchor holds the tiny `MeterReference`, never the `Meter`; a forgotten meter stays collectable, consistent with the weak thread-local stack of [TDR-0015](TDR-0015-threadlocal-stack-for-context-propagation.md).
- **Cheap in steady state**: for a correctly used meter the cost is two concurrent-set operations plus one small allocation per lifecycle, with **zero retained memory** (added on start, removed on stop). This is dwarfed by the timestamping, log-level checks, and metrics collection `start()`/`stop()` already perform.
- **Deterministic reporting in practice**: because `drain()` runs on the next `start()` on *any* thread, a leak surfaces at the next meter activity anywhere in the application, rather than at the whim of a finalizer thread.

### Negative ❌

- **Reporting is not immediate**: a leak is reported only after the GC collects the meter *and* some thread subsequently starts another meter to trigger a drain. In an application that stops creating meters entirely, an outstanding leak may go unreported until the next meter starts (bounded, but not real-time).
- **Irreducible shared state**: `register`/`deregister` mutate a process-wide set. It is lock-free, but it is not a zero-touch hot path; the transparency guarantee is "no monitor lock," not "no shared write."
- **Per-registration snapshot cost**: `register()` calls `Meter.getFullID()`, which runs `String.format(...)`, on every `start()` even when no leak ever occurs. This is the largest avoidable cost on the happy path and could be deferred into `reportLeak()` if it ever proves significant.
- **A subtle correctness footgun**: the design depends on the non-obvious GC reachability rule above. An earlier iteration that stored the reference only on the meter was silently broken; the requirement is now guarded by an assertion in `MeterThreadLocalWeakReferenceGcTest` that *fails* (rather than skips) if the warning is not produced.

### Neutral ⚖️

- **Gated by configuration**: `MeterConfig.detectLeaks` (system property `slf4jtoys.meter.detect.leaks`, default `true`) decides whether registration happens at all. When disabled, nothing is registered and `drain()` processes an empty queue.
- **Message parity**: the emitted message and its `INVALID_ARGUMENT` marker are identical to the former `finalize()` path, so log consumers and existing expectations are unaffected.

## Alternatives Considered

### ❌ Keep `Object.finalize()`

**Description**: Continue overriding `finalize()` to call `MeterValidator.validateFinalize()` when an unreachable, never-stopped meter is finalized.

**Why rejected**: `finalize()` is deprecated for removal (JEP 421) and will eventually break compilation. Even today it is non-deterministic, runs on the shared finalizer thread, delays reclamation, and permits resurrection. It offers no advantage over a `PhantomReference` while carrying every one of finalization's liabilities.

### ❌ PhantomReference registry as an intrusive linked list guarded by a lock

**Description**: The first replacement for `finalize()`. Each `MeterReference` was a node in a global doubly-linked list; `start()` and every stop mutated the list under a `synchronized(LOCK)` block. The list anchored the references (so it worked correctly) and let `deregister()` unlink a stopped meter so `drain()` would skip it.

**Why rejected**: it introduces a library-owned monitor lock on every `start()` and every stop. Under concurrency that lock becomes a contention point the application can observe — a side effect a diagnostic tool must never impose, directly violating the transparency principle of [TDR-0017](TDR-0017-non-intrusive-validation-and-error-handling.md). The chosen design keeps the anchor but replaces the locked list with a lock-free concurrent set.

### ❌ PhantomReference stored only on the Meter (no anchor)

**Description**: The most minimal design: `register()` creates the `MeterReference` and returns it to be kept in a field on the `Meter`; no static collection at all. It appears to eliminate shared state entirely.

**Why rejected**: it is silently **broken**. A `PhantomReference` reachable only through its own referent is collected together with the referent and is *never* enqueued, so leaks are never reported. This was confirmed empirically — the requirement test could only ever be skipped, never satisfied. Correct enqueueing requires an anchor that keeps the reference reachable independently of the meter, which is exactly what the chosen static set provides.

### ❌ Per-thread (ThreadLocal) anchor set

**Description**: Replace the global concurrent set with a `ThreadLocal<Set<MeterReference>>`, so `register`/`deregister` touch only the current thread's set and incur no cross-thread contention.

**Why rejected**: the `ReferenceQueue` is inherently global and `drain()` runs on an arbitrary thread — the one starting the next meter — which cannot claim a reference stored in another thread's set. Cross-thread termination (start on thread A, `ok()` on thread B) breaks deregistration, producing both false positives and a genuine leak in the origin thread's set. `ThreadLocal` state in pooled server threads is itself a classic retention hazard. The gain is marginal — `ConcurrentHashMap.newKeySet()` add/remove on distinct references hit distinct bins and contend almost never — so the correctness cost is not justified.

### ❌ Background daemon thread draining the queue

**Description**: The textbook `Reference` pattern: a dedicated daemon thread blocks on `ReferenceQueue.remove()` and reports leaks as references are enqueued, giving near-immediate detection.

**Why rejected**: it reintroduces a library-owned thread with a lifecycle to manage and, in servlet containers, a real risk of pinning the web-application class loader on redeploy. Opportunistic draining on the calling thread achieves the diagnostic goal with no thread to own, at the cost of immediacy the use case does not need.

### ❌ Auxiliary state for the test seam (`volatile registered` flag / `AtomicInteger` counter)

**Description**: Intermediate iterations carried a `volatile boolean registered` on each reference and/or a static `AtomicInteger` tracking the live count, to make the detector observable from unit tests.

**Why rejected**: both add work to every `start()`/`stop()` — a volatile write and/or a CAS — for the sole benefit of tests, contradicting the transparency goal. With the concurrent anchor, set membership already *is* the authoritative "still registered" state, and tests verify behavior directly through `drain()` plus log-event assertions and `Reference.enqueue()`. The auxiliary state was removed.

## Implementation

- [src/main/java/org/usefultoys/slf4j/meter/MeterLeakDetector.java](../src/main/java/org/usefultoys/slf4j/meter/MeterLeakDetector.java) — the detector: `QUEUE`, the `ANCHOR` set, `MeterReference`, and `register`/`deregister`/`drain`.
- [src/main/java/org/usefultoys/slf4j/meter/Meter.java](../src/main/java/org/usefultoys/slf4j/meter/Meter.java) — holds the `leakRef` handle; `start()` registers, `commonOk()`/`reject()`/`fail()`/`close()` deregister; the former `finalize()` override was removed.
- [src/main/java/org/usefultoys/slf4j/meter/MeterConfig.java](../src/main/java/org/usefultoys/slf4j/meter/MeterConfig.java) — `detectLeaks` flag, system property `slf4jtoys.meter.detect.leaks` (default `true`).
- [src/main/java/org/usefultoys/slf4j/meter/MeterValidator.java](../src/main/java/org/usefultoys/slf4j/meter/MeterValidator.java) — `validateFinalize()` removed.
- Tests: `MeterLeakDetectorTest` (deterministic register/deregister/drain behavior via `Reference.enqueue()`) and `MeterThreadLocalWeakReferenceGcTest` (end-to-end GC → enqueue → drain → `ERROR`, asserting the warning rather than assuming it).

## References

- [TDR-0015: ThreadLocal Stack for Context Propagation](TDR-0015-threadlocal-stack-for-context-propagation.md)
- [TDR-0017: Non-Intrusive Validation and Error Handling](TDR-0017-non-intrusive-validation-and-error-handling.md)
- [TDR-0019: Immutable Lifecycle Transitions](TDR-0019-immutable-lifecycle-transitions.md)
- [TDR-0024: Try-with-Resources Lifecycle Fit and Limitations](TDR-0024-try-with-resources-lifecycle-fit-and-limitations.md)
- [JEP 421: Deprecate Finalization for Removal](https://openjdk.org/jeps/421)
- [java.lang.ref.PhantomReference](https://docs.oracle.com/javase/8/docs/api/java/lang/ref/PhantomReference.html)
