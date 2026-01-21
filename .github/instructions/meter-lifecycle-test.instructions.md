---
applyTo: "*/**/meter/MeterLifeCycle*Test.java
---

# Meter Lifecycle Test Instructions for AI Agents

## Core Principles

1. **Validate actual behavior, not ideal behavior**
2. **Use pedagogical validations** to make tests self-documenting
3. **Separate concerns** with clear comment blocks (`// Given:`, `// When:`, `// Then:`)
4. **Mandatory log validation** in all tests

---

## Meter State Validation

### Primary Validation Method

- **Use `assertMeterState(meter, isStarted, isStopped, okPath, rejectPath, failPath, failMessage, currentIteration, expectedIterations, timeLimitMilliseconds)` for all state checks**
- Validates: `startTime`, `stopTime`, `okPath`, `rejectPath`, `failPath`, `failMessage`, `currentIteration`, `expectedIterations`, `timeLimitMilliseconds`, `lastCurrentTime`, `createTime`
- Only use JUnit asserts for attributes NOT covered by `assertMeterState()`

### Attributes Not Covered by assertMeterState

```java
// For description:
assertEquals("expected description", meter.getDescription());

// For context entries:
assertEquals("expectedValue", meter.getContext().get("key"));
```

### When to Validate State

#### ✅ ALWAYS Validate

1. **Initial state after `new Meter()`** (Group 1 only):
   ```java
   assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
   ```

2. **After `start()` when meter was configured before start**:
   ```java
   // Given: a meter with 15 iterations configured
   meter.iterations(15);
   
   // When: meter is started
   meter.start();
   // Then: validate configuration was applied (pedagogical validation)
   assertMeterState(meter, true, false, null, null, null, null, 0, 15, 0);
   ```

3. **After operations that are the focus of the test** (pedagogical validation):
   ```java
   // When: first path is set
   meter.path("first");
   // Then: validate path was applied (pedagogical validation)
   assertMeterState(meter, true, false, "first", null, null, null, 0, 0, 0);
   ```

4. **After meter enters stopped state** (final validation):
   ```java
   // When: meter is terminated
   meter.ok();
   // Then: validate final state (mandatory validation)
   assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
   ```

5. **When test begins with a stopped meter** (pedagogical validation):
   ```java
   // Given: a meter that has been stopped with ok("completion_path")
   final Meter meter = new Meter(logger).start().ok("completion_path");
   // Then: validate meter is in stopped state (pedagogical validation)
   assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);
   ```

#### ❌ SKIP Validation (Group 2+)

- After `new Meter()` when immediately followed by `start()` (no configuration)
- After `start()` when no configuration occurred before it
- For intermediate operations that are just setup (not the test focus)

### Special Cases: Iteration and Progress Tests

```java
// First batch: 5 iterations
for (int i = 0; i < 5; i++) { meter.inc(); }
// Then: validate currentIteration after first batch (pedagogical validation)
assertMeterState(meter, true, false, null, null, null, null, 5, 15, 0);

meter.progress();
// Then: validate state after progress() - still running (pedagogical validation)
assertMeterState(meter, true, false, null, null, null, null, 5, 15, 0);
```

### Documenting Known Issues

When actual behavior differs from expected:

```java
// Then: logs ILLEGAL, okPath remains unset
// Will be fixed in future: Meter currently stores path from second termination call, 
// but should preserve the path from the first termination.
assertMeterState(meter, true, true, "second_path", null, null, null, 0, 0, 0);
```

---

## Log Event Validation

### Mandatory Structure

**Every test MUST have a log validation block**:

1. Blank line separating from state validations
2. `// Then:` comment describing what logs are expected
3. At minimum: `AssertLogger.assertEventCount(logger, N)`

### Validation Patterns

```java
// ✅ Simple test (no errors)
// Then: description attribute is stored correctly
assertEquals("step 1", meter.getDescription());
assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

// Then: logs only start events
AssertLogger.assertEventCount(logger, 2);

// ✅ Error test (with ILLEGAL)
// Then: null rejected, "valid" is preserved
assertEquals("valid", meter.getDescription());

// Then: logs start + ILLEGAL
AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
AssertLogger.assertEventCount(logger, 3);

// ✅ Progress test
// Then: validate state after progress
assertMeterState(meter, true, false, null, null, null, null, 5, 15, 0);

// Then: logs start + progress + DATA_PROGRESS
AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_PROGRESS);
AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_PROGRESS);
AssertLogger.assertEventCount(logger, 4);
```

### When to Validate Message Content

Only when message contains relevant information:

```java
// For description in logs:
AssertLogger.assertEvent(logger, 2, Level.TRACE, "operation description");

// For context in logs:
AssertLogger.assertEvent(logger, 2, Level.TRACE, "user", "alice");
AssertLogger.assertEvent(logger, 2, Level.TRACE, "action", "import");
```

---

## Test Structure and Comments

### Comment Block Rules

```java
// Given: [describe setup state]
// When: [describe action being tested]
// Then: [describe expected outcome]
```

### Given Comment Patterns

```java
// ✅ Start is part of setup (no configuration before start):
// Given: a new, started Meter
final Meter meter = new Meter(logger).start();

// ✅ Meter configured before start:
// Given: a meter with 15 iterations configured
final Meter meter = new Meter(logger);
meter.iterations(15);
meter.start();

// ✅ Stopped meter variations:
// Given: a meter that has been stopped with ok()
// Given: a meter that has been stopped with ok("completion_path")
// Given: a meter that has been stopped with reject("business_error")
// Given: a meter that has been stopped with fail("technical_error")
// Given: a meter with iterations configured, stopped with ok()
```

### Multi-Step Test Pattern

```java
// Given: a new, started Meter
final Meter meter = new Meter(logger).start();

// When: first path is set
meter.path("first");
// Then: validate path was applied (pedagogical validation)
assertMeterState(meter, true, false, "first", null, null, null, 0, 0, 0);

// When: path is overridden
meter.path("second");
// Then: validate path override (pedagogical validation)
assertMeterState(meter, true, false, "second", null, null, null, 0, 0, 0);

// When: meter is terminated
meter.ok();
// Then: validate final state (mandatory validation)
assertMeterState(meter, true, true, "second", null, null, null, 0, 0, 0);

// Then: logs start + ok + DATA_OK
AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
AssertLogger.assertEventCount(logger, 4);
```

---

## Try-With-Resources Tests

### External Reference Pattern

```java
final Meter meter;
try (final Meter m = new Meter(logger)) {
    meter = m; // Capture reference for post-try validation
    
    // Then: validate initial state (if configured before try)
    assertMeterState(meter, true, false, null, null, null, null, 0, 15, 0);
    
    // ... test operations ...
    
    // Then: validate state before try exit
    assertMeterState(meter, true, false, null, null, null, null, 5, 15, 0);
}

// Then: validate state after implicit close()
assertMeterState(meter, true, true, null, null, null, null, 5, 15, 0);

// Then: logs start + auto-ok + DATA_OK
AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
AssertLogger.assertEventCount(logger, 4);
```

---

## Time-Based Testing

```java
// Setup time source and progress throttling:
final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);
MeterConfig.progressPeriodMilliseconds = 50; // or 0 to disable throttling

final Meter meter = new Meter(logger).withTimeSource(timeSource);
meter.limitMilliseconds(100);

// Advance time during test:
timeSource.advanceMilliseconds(60);

// Validate slow meter detection:
assertTrue(meter.isSlow());  // when elapsed > limit
assertFalse(meter.isSlow()); // when elapsed < limit
```

---

## Group-Specific Rules

### Group 1: Initialization Tests

**ALWAYS validate**:
- Meter state after `new Meter()`
- Meter state after `start()`
- `getCurrentInstance()` before and after `start()`
- `getOperation()` when created with `operationName`
- `getParent()` when created with `parent`

### Group 2+: Normal Lifecycle Tests

**SKIP validation**:
- After `new Meter()` (assume correct)
- After `start()` (assume correct)
- Events at indices 0 and 1 from `start()` (assume correct)

**ALWAYS validate**:
- State after configuration before `start()`
- Intermediate states for operations that are the test focus
- Final state after termination

### Group 3: Try-With-Resources Tests

Follow Group 1 rules (validate all) if testing without `start()` (Tier 3).
Follow Group 2+ rules (skip initial validations) if testing with `start()` (Tier 1).

### Group 6: Post-Stop Invalid Operations

**ALWAYS validate**:
- Initial stopped state (pedagogical validation)
- State after invalid operation (should remain unchanged)
- ILLEGAL logs from rejected operations

---

## Special Annotations

```java
@ValidateCleanMeter(expectDirtyStack = true)
// Use when test calls start() without corresponding termination
```

---

## Code Generation Guidelines

- Use separate code blocks for each class
- Include `@author Co-authored-by: GitHub Copilot using <model name>` in Javadoc
- Think step-by-step, explain briefly, then output code
- Keep explanations short and focused on test intent

---

## Test Group Hierarchy Reference

### Group 1: Initialization
- Foundation for all tests
- Validates meter creation and startup
- **Always** validate all states

### Group 2: Happy Path
- Normal expected flows (Tier 1)
- Valid state transitions
- **Skip** initial validations, **validate** test focus and final state

### Group 3: Try-With-Resources
- Resource management patterns
- Both with and without `start()`
- Follow Group 1 or Group 2+ rules depending on tier

### Group 4: Pre-Start Attribute Updates
- Configuration before `start()` (Tier 2)
- **Validate** state after configuration, before `start()`

### Group 5: Pre-Start Termination
- Self-correcting termination without `start()` (Tier 3)
- Logs INCONSISTENT_* markers

### Group 6: Pre-Start Invalid Operations
- Invalid operations before `start()` (Tier 4)
- Logs ILLEGAL/INCONSISTENT_* markers
- **Validate** state remains unchanged

### Group 7: Post-Start Attribute Updates
- Configuration during execution (Tier 2)
- **Validate** pedagogically when operation is test focus

### Group 8: Post-Start Termination
- Normal termination paths (Tier 1)
- Variations: ok(), ok(path), reject(path), fail(message)

### Group 9: Post-Start Invalid Operations
- Invalid operations after `start()` (Tier 4)
- Logs ILLEGAL/INCONSISTENT_* markers

### Group 10-12: Post-Stop Attribute Updates
- Invalid operations on stopped meters (Tier 4)
- Separate groups for OK, Rejected, Failed states
- **Validate** initial stopped state and unchanged state after rejection

### Group 13: Post-Stop Invalid Termination
- Attempts to terminate already-stopped meters (Tier 4)
- Logs ILLEGAL markers

### Group 14: Pre-Start Terminated, Post-Stop Invalid Termination
- Termination chains without `start()` (Tier 4)
- Combines Tier 3 and Tier 4 scenarios
