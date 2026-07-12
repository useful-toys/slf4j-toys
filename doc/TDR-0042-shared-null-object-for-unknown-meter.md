# TDR-0042: Shared Null-Object for the Unknown Meter

**Status**: Accepted
**Date**: 2026-07-12

## Context

[TDR-0015](TDR-0015-threadlocal-stack-for-context-propagation.md) established that `Meter.getCurrentInstance()` must never return `null`: when no meter is active on the current thread, it returns a "dummy" `Meter` instead, following the Null Object Pattern already used elsewhere in the library ([TDR-0011](TDR-0011-null-object-pattern-for-optional-logging.md)). The original implementation allocated that dummy freshly on every miss:

```java
if (current == null) {
    return new Meter(LoggerFactory.getLogger(UNKNOWN_LOGGER_NAME));
}
```

This had two problems, both surfaced while reviewing the `Meter` construction/lifecycle hot path for GC pressure:

1. **Allocation on a defensive read path.** Every call to `getCurrentInstance()` with no active meter pays the full `Meter` constructor: two derived-logger lookups with string concatenation, an `extractNextPosition("???", null)` call that increments the shared `EVENT_COUNTER` under the `"???"` key, and a creation timestamp. Code that defensively reads `getCurrentInstance()` on every request (to attach context if an operation happens to be running) pays this on every miss, and the ever-growing `"???"` position falsely suggests, in emitted data, that millions of `"???"` operations occurred.
2. **The dummy is a *mutable* Null Object.** `Meter` is not immutable, and the returned dummy is a real, fully functional `Meter`. Calling `start()`, `m()`, `ctx()`, `ok()`, `reject()`, `fail()`, `close()`, `limitMilliseconds()`, or `iterations()` on it silently succeeds and mutates real state on a throwaway instance — none of the existing preconditions in `MeterValidator` reject these on a freshly constructed meter, because "never started" and "just constructed" are indistinguishable to them. This is harmless only because a *new* dummy is discarded after each call; it becomes a correctness hazard the moment allocation is removed and the dummy is shared.

## Decision

**`getCurrentInstance()` returns a single, process-wide shared instance of `UnknownMeter`, a private `Meter` subclass whose state-mutating methods are overridden to log an `INVALID_TRANSITION` and return `this` (or, for `void` methods, simply return) instead of mutating shared state.**

```java
private static final Meter UNKNOWN_INSTANCE = new UnknownMeter();

public static Meter getCurrentInstance() {
    final WeakReference<Meter> ref = localThreadInstance.get();
    final Meter current = ref == null ? null : ref.get();
    if (current == null) {
        return UNKNOWN_INSTANCE;
    }
    return current;
}
```

`UnknownMeter` overrides: `start()`, `commonOk(Object)` (covers `ok()`/`ok(Object)`/`success()`/`success(Object)` — they all funnel into it), `reject(Object)`, `fail(Object)`, `close()`, `putContext(String, Object)`/`putContext(String)` (covers `ctx(...)`/`unctx(...)` via `MeterContext`'s default methods), `m(String)`/`m(String, Object...)`, `limitMilliseconds(long)`, `iterations(long)`, and `sub(String)`. Each override calls a private `denied()` helper that logs the same `INVALID_TRANSITION` marker already used for other invalid-lifecycle calls (e.g. calling `start()` twice), then returns `this` unchanged.

`inc()`, `incBy()`, `incTo()`, `progress()`, and `path(Object)` need **no** override: their existing preconditions (`validateIncPrecondition`, `validateProgressPrecondition`, `validatePathPrecondition`) already reject a meter with `startTime == 0`, and `UNKNOWN_INSTANCE` can never be started — `start()` is itself denied — so `startTime` stays `0` forever and these methods are already safe no-ops.

**Overriding, not a flag.** The alternative of adding a boolean check (e.g. `if (meter.isUnknownMeter()) { ... }`) inside each `MeterValidator` precondition was rejected: that check would run on *every* call to *every* real `Meter`, adding a branch to the hot path in order to save an allocation on a cold miss path — backwards for a change motivated by performance. Overriding instead relies on the virtual dispatch the JVM already performs for these non-`final` instance methods; real `Meter` instances never reach `UnknownMeter` code and pay nothing extra, while `UNKNOWN_INSTANCE` alone carries the rejection logic.

**`sub()` becomes absorbing, not delegating.** Previously, `getCurrentInstance().sub("child")` (the implementation of `MeterFactory.getCurrentSubMeter`) built a real, usable `"???/child"` meter even with no active parent — silently manufacturing a meaningless hierarchy entry. `UnknownMeter.sub(...)` now returns `UNKNOWN_INSTANCE` itself and logs the same `INVALID_TRANSITION`, i.e. `UNKNOWN_INSTANCE.sub(x) == UNKNOWN_INSTANCE`. Requesting a sub-operation with no active parent is exactly the same class of misuse as calling `ok()` with no active meter, and is now reported the same way.

### Follow-up: throttling misuse reports against the shared instance

Reporting every misuse unconditionally, as designed above, has a cost concentrated entirely on the shared singleton: a defensive call pattern like `Meter.getCurrentInstance().progress()` in a hot loop with no active meter would log an `ERROR` and allocate a `CallerStackTraceThrowable` (which walks and filters the current stack trace) on *every single call*, flooding the error channel and burning CPU on a cold-path diagnostic that, past the first occurrence, adds no new information.

**Decision:** two package-private hooks on `Meter`, both no-ops for real meters and both overridden by `UnknownMeter`:

```java
boolean shouldReportInvalidUsage() { return true; }   // real meters: always report
String invalidUsageDiagnosis() { return null; }        // real meters: keep the caller-supplied message
```

`MeterValidator.logInvalidTransition`/`logInvalidState` consult `shouldReportInvalidUsage()` (after `getMessageLogger().isErrorEnabled()`, so the throwable allocation is gated by both — real meters pay a trivial `true` check on this already-cold path, never on the hot path of correct usage) and, if the report proceeds, `invalidUsageDiagnosis()` to override the message. `UnknownMeter` implements a time-based rate limit — a static `AtomicLong nextAllowedReportNanos` advanced via a CAS loop on `System.nanoTime()`, and a static `AtomicLong suppressedCount` incremented on every throttled occurrence and drained into the next allowed report's message ("N similar reports suppressed"). The interval is configurable via `MeterConfig.noopReportIntervalMilliseconds` (default 60000 ms; `0` disables throttling; negative disables reporting entirely), following the same system-property convention as every other `MeterConfig` value.

The same `invalidUsageDiagnosis()` override also fixes a latent inaccuracy: `inc()`/`incBy()`/`incTo()`/`progress()`/`path(Object)` are not overridden by `UnknownMeter` (their own preconditions already reject `startTime == 0`, which is permanently true for the singleton), but the message their preconditions produce — *"Meter not yet started, must call start() first"* — is misleading for a meter that was never meant to be started at all. `UnknownMeter.invalidUsageDiagnosis()` replaces it, and any other message routed through these two helpers, with the accurate *"no operation is active on the current thread"*.

#### Alternatives considered for the throttle

- ❌ **Log once, ever.** Simpler, but a single silent misuse pattern early in an application's life would forever suppress evidence of a *different*, later misuse pattern — the throttle needs to keep reporting periodically, not retire permanently after the first occurrence.
- ❌ **Per-call-site deduplication (keyed by stack trace or caller location).** Would distinguish "many different bugs calling `getCurrentInstance()` unsafely" from "one bug calling it in a loop", which is valuable, but requires capturing and hashing a stack trace (or caller class/method) on every occurrence — reintroducing the exact per-call cost (`CallerStackTraceThrowable` capture) the throttle exists to avoid, just to decide whether to throttle.
- ❌ **Rely on a backend `DuplicateMessageFilter`** (e.g. Logback's `DuplicateMessageFilter`, or an application-side equivalent). Rejected because it is opt-in, backend-specific configuration external to this library — the whole point of `UnknownMeter` is that the shared instance is *safe by default*, not safe only when the application also configures its logging backend correctly. It also still pays the `CallerStackTraceThrowable` allocation before the backend ever sees the event to decide whether to drop it.
- ❌ **Downgrade the shared instance's misuse level** (e.g. `WARN` instead of `ERROR`, expecting operators to tune the appLogger threshold down). Rejected: it weakens the diagnostic for every application, including those that never hit the high-frequency pattern this throttle protects against, to work around a problem only some applications have; the throttle solves the actual frequency problem without diluting severity.

The throttle's own runtime state (`nextAllowedReportNanos`, `suppressedCount`) is reset by `MeterConfig.reset()`, alongside the config properties — necessary so that test isolation relying on that reset (e.g. via the existing `@ResetMeterConfig` extension) always observes the first misuse report immediately, regardless of throttle state accumulated by a previous test.

## Consequences

### Positive ✅

- **No more per-call allocation on the miss path.** `getCurrentInstance()` with no active meter now returns the same object every time, eliminating the constructor cost (logger lookups, concatenation, timestamp) and the `EVENT_COUNTER["???"]` inflation that misrepresented emitted data.
- **Misuse becomes visible instead of silently mutating a throwaway object.** Calling a lifecycle method on "no active meter" now produces the same `INVALID_TRANSITION` diagnostic already used for other invalid transitions, consistent with the non-intrusive validation philosophy of [TDR-0017](TDR-0017-non-intrusive-validation-and-error-handling.md): the call never throws, but the misuse is no longer invisible.
- **Zero hot-path cost for real meters.** The rejection logic lives entirely in `UnknownMeter`'s overrides; ordinary `Meter` instances and their call sites are unaffected and remain monomorphic.
- **Safe to share across threads.** Because every mutating path is neutralized, the single static instance can be handed out to any number of threads concurrently with no synchronization and no data-race risk — unlike a plain shared `Meter`, which would have been a mutable object shared without coordination.

### Negative ❌

- **Behavioral change for the sub-meter fallback.** `MeterFactory.getCurrentSubMeter(name)` called with no active parent used to return a distinct, real `"???/name"` meter; it now returns the shared `UNKNOWN_INSTANCE` unchanged (category `"???"`, no operation). Call sites relying on the former fallback's identity or operation name observe different behavior. Verified against the full test suite (2839 tests, both `slf4j-2.0` and `slf4j-2.0,with-logback` profiles); one existing test (`MeterFactoryTest.shouldCreateSubMeterFromFallbackWhenNoCurrentMeterIsStarted`) was updated to assert the new absorbing behavior.
- **Larger, harder-to-forget override surface.** Every future state-mutating method added to `Meter` must be remembered and added to `UnknownMeter`'s overrides, or it silently mutates the shared singleton again. This risk is bounded by the fact that all current mutators already follow the `if (!MeterValidator.validateXxx(this)) return this;` guard-clause pattern, making the override list mechanically derivable by inspection, and by `MeterUnknownInstanceTest`, which exercises every currently known mutator against the singleton. `MeterUnknownInstanceOverrideInvariantTest` closes the remaining gap for *future* additions: it reflectively enumerates every public, `Meter`/`void`-returning method declared on `Meter` and fails loudly if that set drifts from a hand-maintained invoker map, rather than silently missing a newly added method the way a purely hand-listed test would.

### Neutral ⚖️

- **`UNKNOWN_INSTANCE`'s message/data loggers are fixed at class-load time.** Like other library statics, the loggers it reports through are derived from `MeterConfig.messagePrefix`/`messageSuffix` at construction, not re-read afterward — the same class of trade-off already accepted for `Meter`'s own logger fields.
- **This does not change the single-thread lifecycle contract** ([TDR-0041](TDR-0041-single-thread-meter-lifecycle-contract.md)) or the `WeakReference`-based stack ([TDR-0015](TDR-0015-threadlocal-stack-for-context-propagation.md)); it only replaces what is handed out on a stack *miss*.

## Alternatives Considered

### ❌ Return `null` instead of a Null Object

**Description**: Change `getCurrentInstance()` to return `null` when no meter is active, and let callers check.

**Why rejected**: breaking change to a documented non-null contract. `MeterFactory.getCurrentMeter()`/`getCurrentSubMeter()` are annotated `@NonNull`, and `getCurrentSubMeter` calls `.sub(...)` directly on the result — a `null` return would turn every parent-less sub-meter request into an `NullPointerException`. Dozens of existing tests also assert on `getCurrentInstance().getCategory()` unconditionally.

### ❌ Per-thread cached dummy (`ThreadLocal<Meter>`)

**Description**: Keep allocating a real, mutable dummy, but cache one per thread via a second `ThreadLocal`, lazily created on first miss.

**Why rejected**: reduces allocation from "per call" to "per thread" but does not address the mutability hazard — a shared-within-a-thread dummy is still silently mutable by `start()`/`ctx()`/etc., just with a smaller blast radius. It also adds a second `ThreadLocal` (with its own thread-pool retention profile) for a problem the shared-and-neutralized singleton solves more completely with less state.

### ❌ Plain shared singleton, no overrides

**Description**: Allocate `UNKNOWN_INSTANCE` once and return it from every miss, without changing `Meter`'s mutating methods.

**Why rejected**: `Meter` is mutable and carries no internal synchronization ([TDR-0041](TDR-0041-single-thread-meter-lifecycle-contract.md) already documents that lifecycle fields assume single-thread ownership). A plain shared instance turns any stray `getCurrentInstance().ctx(...)`-style call from *any* thread into a cross-thread data race on shared state — an unacceptable trade for saving one allocation.

### ❌ `isUnknownMeter()` flag with guards in `MeterValidator`

**Description**: Add a boolean-returning identity check and branch on it at the top of each relevant `MeterValidator` precondition method.

**Why rejected**: every one of those preconditions already runs on every call from every real `Meter` in the application. Adding a branch there to special-case a single shared instance imposes a permanent hot-path cost to solve a cold-path (miss-only) allocation problem — the opposite of the change's own motivation. Virtual dispatch via subclassing achieves the same rejection semantics at zero cost to real meters.

## Implementation

- [src/main/java/org/usefultoys/slf4j/meter/Meter.java](../src/main/java/org/usefultoys/slf4j/meter/Meter.java) — `UNKNOWN_INSTANCE` field, `getCurrentInstance()` miss path, the nested `UnknownMeter` class with its overrides, the `shouldReportInvalidUsage()`/`invalidUsageDiagnosis()` hooks, and `UnknownMeter`'s throttle fields/overrides.
- [src/main/java/org/usefultoys/slf4j/meter/MeterValidator.java](../src/main/java/org/usefultoys/slf4j/meter/MeterValidator.java) — `logInvalidTransition`/`logInvalidState` gate the `CallerStackTraceThrowable` allocation on `isErrorEnabled()` and `shouldReportInvalidUsage()`, and consult `invalidUsageDiagnosis()` to override the message.
- [src/main/java/org/usefultoys/slf4j/meter/MeterConfig.java](../src/main/java/org/usefultoys/slf4j/meter/MeterConfig.java) — `noopReportIntervalMilliseconds` property; `reset()` also resets the throttle's runtime state via `Meter.resetNoopReportThrottle()`.
- [src/test/java/org/usefultoys/slf4j/meter/MeterUnknownInstanceTest.java](../src/test/java/org/usefultoys/slf4j/meter/MeterUnknownInstanceTest.java) — asserts single-instance identity (repeated calls and across threads), the unknown category, and that every overridden mutator logs `INVALID_TRANSITION` without changing observable state.
- [src/test/java/org/usefultoys/slf4j/meter/MeterUnknownInstanceOverrideInvariantTest.java](../src/test/java/org/usefultoys/slf4j/meter/MeterUnknownInstanceOverrideInvariantTest.java) — reflective safety net: enumerates every qualifying method on `Meter` and cross-checks it against a hand-maintained invoker map, then asserts each one leaves `UNKNOWN_INSTANCE`'s observable state unchanged.
- [src/test/java/org/usefultoys/slf4j/meter/MeterUnknownInstanceThrottleTest.java](../src/test/java/org/usefultoys/slf4j/meter/MeterUnknownInstanceThrottleTest.java) — throttle behavior: immediate first report, suppression within the interval, suppressed-count embedding after the interval elapses, `0`/negative interval edge cases, real meters never throttled, no throwable allocated when `ERROR` is disabled.
- [src/test/java/org/usefultoys/slf4j/meter/MeterFactoryTest.java](../src/test/java/org/usefultoys/slf4j/meter/MeterFactoryTest.java) — `shouldCreateSubMeterFromFallbackWhenNoCurrentMeterIsStarted` updated for the absorbing `sub()` behavior.

## References

- [TDR-0011: Null Object Pattern for Optional Logging](TDR-0011-null-object-pattern-for-optional-logging.md)
- [TDR-0015: ThreadLocal Stack for Context Propagation](TDR-0015-threadlocal-stack-for-context-propagation.md)
- [TDR-0017: Non-Intrusive Validation and Error Handling](TDR-0017-non-intrusive-validation-and-error-handling.md)
- [TDR-0041: Single-Thread Meter Lifecycle Contract](TDR-0041-single-thread-meter-lifecycle-contract.md)
