# TDR-0041: Single-Thread Meter Lifecycle Contract

> **Note**: Originally published as TDR-0037, renumbered to TDR-0041 because the number 0037 was
> accidentally assigned twice (see [TDR-0037: Migrate WatcherServlet Pull Path from WatcherSingleton](TDR-0037-migrate-watcher-servlet-pull-from-singleton.md)).

**Status**: Accepted  
**Date**: 2026-07-07

## Context

A `Meter` is started (`start()`) and terminated (`ok()`/`reject()`/`fail()`/`close()`) by application code, and the library never specified explicitly whether both ends of that lifecycle must happen on the same thread. The answer was implicit and scattered:

- [TDR-0015](TDR-0015-threadlocal-stack-for-context-propagation.md) notes, in one consequence line, that the context-propagation stack "only works within a single thread".
- `MeterValidator.validateStopPrecondition` warns (`INVALID_TRANSITION`: *"Meter stopped before the current instance on the thread, possible mismatched start/stop calls"*) whenever the meter being stopped is not the top of the current thread's stack — which is always the case for a cross-thread stop — but proceeds anyway.
- All lifecycle state (`startTime`, `stopTime`, paths, the leak-detector handle `leakRef`) lives in plain, non-`volatile` instance fields with no library-provided synchronization.

The ambiguity had a real cost: during the design of the leak detector ([TDR-0035](TDR-0035-phantomreference-leak-detector-for-forgotten-meters.md)), a per-thread anchor alternative was rejected with the argument that cross-thread termination is a "supported pattern" — overstating the actual contract and demonstrating that an unrecorded decision drifts. This TDR records the contract explicitly.

## Decision

**The `Meter` lifecycle is single-thread-owned by design: the thread that calls `start()` is expected to call the termination method. Cross-thread termination is *tolerated, not supported*.**

Precisely, when a meter is started on thread A and terminated on thread B:

1. **The measurement core stays correct.** `startTime`/`stopTime`, outcome paths, and the emitted log record are instance state, not thread state; given safe publication of the meter between the threads (an `ExecutorService` submission, a `BlockingQueue`, a `Thread.start()` — any normal happens-before edge), the record produced is accurate.
2. **The library warns but proceeds.** `validateStopPrecondition` logs `INVALID_TRANSITION` ("Meter stopped before the current instance on the thread") and the termination continues — consistent with the non-intrusive philosophy of [TDR-0017](TDR-0017-non-intrusive-validation-and-error-handling.md): diagnostics never abort application flow.
3. **The context-propagation stack degrades, on both threads.** The stop executes `localThreadInstance.set(previousInstance)` on thread B, injecting thread A's previous meter chain into B's slot — `Meter.getCurrentInstance()` on B may then return a meter belonging to A's context. Meanwhile A's slot still holds the now-stopped meter until another meter starts on A or the `WeakReference` self-heals after collection ([TDR-0015](TDR-0015-threadlocal-stack-for-context-propagation.md)). Parent/child attribution and `ctx()` enrichment around the handoff are therefore unreliable.
4. **Safe publication is the application's responsibility.** The library adds no `volatile`/synchronization to lifecycle fields for this case; an unsynchronized handoff is a data race on *all* meter state, not just one field, and is outside the contract.
5. **Without safe publication, the leak detector can produce a false positive.** The [TDR-0035](TDR-0035-phantomreference-leak-detector-for-forgotten-meters.md) registration handle (`leakRef`) is a plain, non-`volatile` field like the rest of the lifecycle state. If thread B's read of `leakRef` is stale (sees `null` or an outdated value) because the handoff from A skipped a happens-before edge, `deregister()` is skipped or given a value it no longer recognizes, so the meter stays anchored after a *correct* termination and is later reported as a false "Meter never stopped" `ERROR` once the GC collects it. This differs from the former `finalize()` mechanism, which re-read the meter's live state (`getStopTime()`) at report time and was therefore effectively immune to this specific race — this is a genuinely new failure mode introduced by the phantom-reference design, not a restatement of the context-stack degradation in points 1–3.

**Corollary for library features:** a diagnostic feature must never turn this tolerated-but-degraded usage into a *hard failure or false accusation*. This is the correct justification for the global (not per-thread) anchor in [TDR-0035](TDR-0035-phantomreference-leak-detector-for-forgotten-meters.md): with a `ThreadLocal` anchor, every cross-thread stop would fail to deregister and later emit a **false** "Meter never stopped" `ERROR` — converting a warned-about-but-working pattern into a false positive. With the global anchor, deregistration works from any thread and the only degradation remains the context stack, as designed.

## Consequences

### Positive ✅

- **The contract is explicit and citable.** Future designs (detectors, watchers, executors) can reference this TDR instead of re-deriving — or misstating — the threading model.
- **Hot path stays free of defensive synchronization.** Not supporting unsynchronized cross-thread lifecycles means no `volatile` reads/writes or fences are added to `start()`/stop for a pattern the library does not endorse, preserving the transparency principle of [TDR-0017](TDR-0017-non-intrusive-validation-and-error-handling.md).
- **Misuse is visible.** The existing `INVALID_TRANSITION` warning already surfaces cross-thread (and mismatched same-thread) stops in the log, with no new mechanism.

### Negative ❌

- **Async workflows need manual care.** Code that legitimately spans threads (futures, reactive pipelines) must either keep the whole lifecycle on one thread, or accept degraded context propagation and provide its own safe publication. The library offers no built-in async context bridge (already noted as "Thread Affinity" in [TDR-0015](TDR-0015-threadlocal-stack-for-context-propagation.md)).
- **"Tolerated" is subtle.** Users may read the warning as a bug in their code even when the emitted metrics are correct; conversely they may ignore genuinely mismatched start/stop pairs on a single thread, since both produce the same message.

### Neutral ⚖️

- **No behavior change.** This TDR documents existing behavior; the only code artifact is the threading-contract note in the `Meter` class javadoc.

## Alternatives Considered

### ❌ Fully support cross-thread lifecycles

**Description**: Make all lifecycle state `volatile` (or use atomics), and redesign the context stack so a stop on thread B correctly repairs both threads' stacks.

**Why rejected**: the stack cannot be repaired from another thread without inter-thread coordination (thread B cannot write thread A's `ThreadLocal` slot), so "full support" would require replacing the `ThreadLocal` design of [TDR-0015](TDR-0015-threadlocal-stack-for-context-propagation.md) with a shared, synchronized structure — paying contention on every `start()`/stop of every application, to serve a minority pattern that already works for its measurement core. Directly violates [TDR-0017](TDR-0017-non-intrusive-validation-and-error-handling.md)'s transparency principle.

### ❌ Hard-fail (throw or refuse) on cross-thread termination

**Description**: Detect that the stopping thread is not the owner and reject the termination (return early or throw), keeping the stack always consistent.

**Why rejected**: refusing the stop would leave the meter unstopped — manufacturing the exact "forgotten meter" misuse the library tries to detect — and throwing would break the resilience rule that `Meter` never disturbs application flow ([TDR-0017](TDR-0017-non-intrusive-validation-and-error-handling.md), [TDR-0029](TDR-0029-resilient-state-transitions-with-chained-api.md)). Warn-and-proceed preserves the correct measurement while flagging the suspect usage.

### ❌ Leave the contract implicit

**Description**: Keep relying on the validator warning and the one-line note in TDR-0015.

**Why rejected**: it already failed once — [TDR-0035](TDR-0035-phantomreference-leak-detector-for-forgotten-meters.md) initially justified a design choice by calling cross-thread termination a "supported pattern". An implicit contract gets re-guessed differently by each future design discussion.

## Implementation

- [src/main/java/org/usefultoys/slf4j/meter/Meter.java](../src/main/java/org/usefultoys/slf4j/meter/Meter.java) — threading-contract note in the class javadoc; stack push/pop in `start()` and the termination methods; `checkCurrentInstance()`.
- [src/main/java/org/usefultoys/slf4j/meter/MeterValidator.java](../src/main/java/org/usefultoys/slf4j/meter/MeterValidator.java) — `validateStopPrecondition` emits the `INVALID_TRANSITION` warning for stops that do not match the current thread's stack top.
- [doc/TDR-0035](TDR-0035-phantomreference-leak-detector-for-forgotten-meters.md) — the per-thread-anchor rejection now cites this contract instead of a "supported pattern" claim.

## References

- [TDR-0015: ThreadLocal Stack for Context Propagation](TDR-0015-threadlocal-stack-for-context-propagation.md)
- [TDR-0017: Non-Intrusive Validation and Error Handling](TDR-0017-non-intrusive-validation-and-error-handling.md)
- [TDR-0029: Resilient State Transitions with Chained API](TDR-0029-resilient-state-transitions-with-chained-api.md)
- [TDR-0035: PhantomReference Leak Detector for Forgotten Meters](TDR-0035-phantomreference-leak-detector-for-forgotten-meters.md)
