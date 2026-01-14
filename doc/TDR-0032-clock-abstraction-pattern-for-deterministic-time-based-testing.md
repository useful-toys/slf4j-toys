# TDR-0032: Clock Abstraction Pattern for Deterministic Time-Based Testing

**Status:** Accepted  
**Date:** 2026-01-13  
**Author:** Daniel Felix Ferber  
**Co-author:** GitHub Copilot using gpt-4o

---

## Context

The `Meter` class and related components (`EventData`, `MeterData`) rely heavily on time measurements for:
- **Duration calculation**: Measuring operation execution time
- **Slowness detection**: Comparing execution time against configured time limits
- **Progress throttling**: Limiting the frequency of progress reports based on elapsed time
- **Lifecycle tracking**: Recording timestamps for start, progress, and termination events

All these features originally used `System.nanoTime()` directly, which creates a significant challenge for **deterministic testing**. Tests that validate time-dependent behavior (e.g., "operation should be marked as slow if it exceeds 15ms") become **non-deterministic** because:
1. Actual thread delays (`Thread.sleep(15)`) don't guarantee precise timing
2. Test execution time varies across different environments and system loads
3. Tests may fail randomly due to timing issues beyond the test's control

### The Problem: Non-Deterministic Time-Based Tests

Consider this test scenario:
```java
meter.limitMilliseconds(15);
meter.start();
// Do some work that should take > 15ms
Thread.sleep(10);  // ← Not guaranteed to sleep exactly 10ms
meter.progress();  // ← May or may not detect slowness
```

The test has **uncertainty** because:
- `Thread.sleep(10)` might sleep for 9ms, 10ms, or 12ms
- The next line might execute at 14ms, 15ms, or 16ms relative to start
- Test results depend on system scheduler behavior and load

### Requirements

1. **Deterministic Testing**: Tests should produce consistent results regardless of actual time passage
2. **No Mocks**: Avoid heavy mocking frameworks; prefer lightweight, explicit abstractions
3. **Backward Compatibility**: Existing production code should continue using real system time
4. **Minimal API Changes**: The change should not break existing meter constructors or methods
5. **Clear Semantics**: The abstraction should have a single, focused responsibility

---

## Decision

We introduce the **Clock Abstraction Pattern** via a `TimeSource` interface that abstracts time measurement. This allows:
- **Production code** to use real system time via `SystemTimeSource`
- **Test code** to use controllable time sources that can be advanced manually

### Design

#### 1. TimeSource Interface

```java
public interface TimeSource {
    /**
     * Returns the current time in nanoseconds.
     * The absolute value has no meaning; only differences are significant.
     */
    long nanoTime();
}
```

**Responsibilities:**
- Single method: `nanoTime()` returning the current time in nanoseconds
- No dependencies on threading, scheduling, or I/O
- Stateless from caller's perspective

#### 2. SystemTimeSource (Default Implementation)

```java
public final class SystemTimeSource implements TimeSource {
    public static final SystemTimeSource INSTANCE = new SystemTimeSource();
    
    private SystemTimeSource() {}
    
    @Override
    public long nanoTime() {
        return System.nanoTime();
    }
}
```

**Characteristics:**
- Singleton pattern (immutable, thread-safe)
- Direct delegation to `System.nanoTime()`
- Used by default in all production scenarios

#### 3. Integration into EventData

```java
public class EventData {
    private TimeSource timeSource = SystemTimeSource.INSTANCE;
    
    protected final long collectCurrentTime() {
        return lastCurrentTime = timeSource.nanoTime();
    }
    
    public EventData withTimeSource(final TimeSource timeSource) {
        this.timeSource = timeSource;
        return this;
    }
}
```

**Key points:**
- `timeSource` field is `transient` (not serialized)
- Defaults to `SystemTimeSource.INSTANCE`
- `withTimeSource()` method allows injection (builder pattern)
- Called before `start()` in tests

#### 4. Integration into Meter

```java
public class Meter extends MeterData {
    public Meter withTimeSource(final TimeSource timeSource) {
        super.withTimeSource(timeSource);
        return this;
    }
}
```

**Key points:**
- Delegates to parent class `EventData.withTimeSource()`
- Returns `Meter` for method chaining
- Maintains fluent API consistency
- No constructor signature changes (backward compatible)

---

## Usage

### Production Code (No Changes Required)

```java
Meter meter = new Meter(logger, "operation");
meter.limitMilliseconds(100).start();
// ... operation executes ...
meter.ok();
```

**Behavior:** Uses `SystemTimeSource.INSTANCE` automatically.

### Test Code (Deterministic Testing)

```java
@Test
void testSlownessDetection() {
    final TestTimeSource timeSource = new TestTimeSource();
    
    final Meter meter = new Meter(logger, "operation")
        .limitMilliseconds(15)
        .withTimeSource(timeSource);  // ← Inject test time source
    
    timeSource.setNanoTime(1000);
    meter.start();  // Starts at t=1000ns
    
    timeSource.setNanoTime(20_000_000);  // Advance to t=20ms
    meter.progress();  // Should detect slowness (20ms > 15ms)
    
    // Assert that slowness was logged...
}
```

**Benefits:**
- **Deterministic**: Time progresses exactly as specified
- **Fast**: No actual thread delays
- **Isolated**: Test doesn't depend on system scheduler
- **Readable**: Time progression is explicit in test code

---

## Alternatives Considered

### 1. Mock System.nanoTime() with Mockito

**Rejected because:**
- Requires mocking static methods (PowerMock or Mockito inline)
- Adds heavy dependency for a simple abstraction
- Less explicit in test code
- Violates "no mocks" requirement from AI-PROJECT-INSTRUCTIONS.md

### 2. Extend Meter Class in Tests

**Rejected because:**
- Breaks encapsulation (exposes internal time collection)
- Requires test-specific subclasses for every tested class
- Not maintainable as more classes need time abstraction

### 3. System Property to Control Time

**Rejected because:**
- Global state affects all tests (not isolated)
- Difficult to control precisely
- Still requires actual time passage

### 4. Pass Clock to Every Method

**Rejected because:**
- Pollutes API (every method needs a clock parameter)
- Breaks backward compatibility
- Poor separation of concerns

---

## Consequences

### Positive

1. **Deterministic Testing**: Time-based tests are now reliable and fast
2. **No External Dependencies**: Pure Java, no mocking frameworks
3. **Backward Compatible**: Existing code works without changes
4. **Clear Semantics**: Single-purpose interface with obvious responsibility
5. **Easy to Test**: Test time sources are trivial to implement
6. **Fluent API Preserved**: `withTimeSource()` chains with other builder methods

### Negative

1. **Additional Abstraction**: One more interface to understand
2. **Manual Injection Required**: Tests must explicitly call `withTimeSource()`
3. **Not Automatic**: Developers must remember to use test time sources

### Neutral

1. **Transient Field**: `timeSource` not serialized (acceptable, it's infrastructure)
2. **Thread Safety**: Caller responsible for setting time source before concurrent use (documented)

---

## Implementation Notes

### Test Time Source Example

```java
public class TestTimeSource implements TimeSource {
    private long currentNanoTime = 0;
    
    public void setNanoTime(final long nanoTime) {
        currentNanoTime = nanoTime;
    }
    
    public void advance(final long nanoDelta) {
        this.currentNanoTime += nanoDelta;
    }
    
    @Override
    public long nanoTime() {
        return curr
```

### Precondition Validation

The `withTimeSource()` method should be called **before** `start()`. If called after, the meter may have already collected timestamps using the default time source, leading to inconsistent behavior. This is documented in the Javadoc but not enforced programmatically (performance consideration).

### Serialization

The `timeSource` field is marked `transient` because:
- Time sources are infrastructure, not domain data
- Deserialized meters should use the default system time source
- Test time sources are not meant to be serialized

---

## Related Documents

- **TDR-0001**: Offloading Complexity to Interfaces (design philosophy)
- **TDR-0018**: Portable Access to Platform-Specific Metrics (system metrics)
- **TDR-0024**: Try-With-Resources Lifecycle Fit and Limitations (lifecycle patterns)
- **meter-lifecycle-tests.md**: Test documentation showing time-based testing patterns

---

## Revision History

| Version | Date       | Author              | Changes                          |
|---------|------------|---------------------|----------------------------------|
| 1.0     | 2026-01-13 | Daniel Felix Ferber | Initial decision record          |

