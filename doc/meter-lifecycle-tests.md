# Meter Lifecycle Test Hierarchy

**Document:** `doc/meter-lifecycle-tests.md`  
**Purpose:** Enumerate and organize test groups for comprehensive coverage of Meter state transitions across all four resilience tiers.  
**Related TDRs:** [TDR-0019](TDR-0019-immutable-lifecycle-transitions.md), [TDR-0020](TDR-0020-three-outcome-types-ok-reject-fail.md), [TDR-0029](TDR-0029-resilient-state-transitions-with-chained-api.md), [meter-state-diagram.md](meter-state-diagram.md)

---

## Overview

This document defines a hierarchical test strategy for the `Meter` lifecycle to achieve comprehensive coverage of all four resilience tiers:

1. **Tier 1** (✅ Valid state-changing) - **Happy path:** Normal expected flows
2. **Tier 2** (☑️ Valid non-state-changing) - **Attribute updates:** Support operations
3. **Tier 3** (⚠️ State-correcting) - **Outside expected flow:** Violations that self-correct
4. **Tier 4** (❌ State-preserving) - **Invalid flow:** Rejections that preserve state

By organizing tests in groups from foundational (Group 1) to complex scenarios (Group 10), we ensure:
- **Reliability**: Each test has a solid foundation to build upon
- **Clarity**: Each test group focuses on a specific aspect of behavior
- **Maintainability**: Changes to one group don't cascade to others
- **Coverage**: All state transitions and error conditions are validated

## Test Coverage Summary

| Group | Focus | Tiers | Logs | Purpose |
|-------|-------|-------|------|---------|
| **1** | Initialization | ✅ Base | None | Foundation for all tests |
| **2** | Happy Path | ✅ Tier 1 | Normal | Valid expected flows |
| **3** | Try-With-Resources | ✅ Tier 1 + ⚠️ Tier 3 | Normal/INCONSISTENT_* | Resource management with/without start |
| **4** | Pre-Start Attribute Updates | ☑️ Tier 2 | None | Attribute setup before start |
| **5** | Pre-Start Termination | ⚠️ Tier 3 | INCONSISTENT_* | Termination without start (self-correcting) |
| **6** | Pre-Start Invalid Operations | ❌ Tier 4 | ILLEGAL/INCONSISTENT_* | Invalid operations before start |
| **7** | Post-Start Attribute Updates | ☑️ Tier 2 | None | Attribute updates during execution |
| **8** | Post-Start Termination | ✅ Tier 1 | Normal | Termination via ok/reject/fail with path variations |
| **9** | Post-Stop Invalid Operations (OK) | ❌ Tier 4 | ILLEGAL/INCONSISTENT_* | Invalid operations on OK state |
| **10** | Post-Stop Invalid Operations (Rejected) | ❌ Tier 4 | ILLEGAL/INCONSISTENT_* | Invalid operations on Rejected state |
| **11** | Post-Stop Invalid Operations (Failed) | ❌ Tier 4 | ILLEGAL/INCONSISTENT_* | Invalid operations on Failed state |
| **14** | Bad Arguments | ❌ Tier 4b | ILLEGAL | Invalid argument values |
| **15** | Terminal Immutability | ❌ Tier 4 | None | Preserve terminal state |
| **16** | Thread-Local Stack | Mixed | Varies | Nesting & cleanup |
| **17** | Complex Scenarios | All | Varies | Realistic workflows |

---

## Test Group Hierarchy

### **Group 1: Meter Initialization (Base Guarantee)**

**Purpose:** Validate that Meter is created and started correctly. This group is the foundation for all subsequent tests, ensuring initialization reliability.

**Test Scenarios:**

1. **Meter creation with logger**
   - `new Meter(logger)` → verify initial state (startTime = 0, stopTime = 0)
   - Verify default attributes: currentIteration = 0, expectedIterations = 0, timeLimit = 0
   - Verify timestamp: createTime > 0

2. **Meter creation with logger + operationName**
   - `new Meter(logger, "operationName")` → verify initial state
   - Verify operationName is captured correctly

3. **Meter creation with logger + operationName + parent**
   - `new Meter(logger, "operationName", "parent-id")` → verify initial state
   - Verify parent is captured correctly

4. **Start Meter successfully**
   - `new Meter(logger).start()` → transition Created → Started
   - Verify startTime != 0
   - Verify meter becomes current in thread-local stack (`Meter.getCurrentInstance()` returns this meter)
   - Verify DEBUG log with system status information

5. **Start Meter in try-with-resources (start in block)**
   - `try (Meter m = new Meter(logger)) { m.start(); }` → create meter in resource, start in block
   - Verify meter created successfully, then transitioned to started state
   - Verify try-with-resources + sequential start work correctly

6. **Start Meter with chained call in try-with-resources**
   - `try (Meter m = new Meter(logger).start()) { ... }` → chain start() at creation
   - Verify meter created and started in single expression
   - Verify chained API works within try-with-resources scope

---

### **Group 2: Happy Path (✅ Tier 1 - Valid State-Changing)**

**Purpose:** Validate normal lifecycle transitions without errors. These are the expected, successful paths through the state machine.

**Test Scenarios:**

1. **Created → Started → OK (simple)**
   - `new Meter().start() → ok()` → verify correct state transition
   - Verify INFO log with completion report

2. **Created → Started → OK with custom path**
   - `new Meter().start() → ok("success_path")` → verify okPath = "success_path"
   - `new Meter().start() → path("custom_path") → ok()` → verify okPath = "custom_path" (path() sets default, ok() uses it)
   - Verify variations of path() types and ok() arguments:
     - **String path**: `ok("success")`, `ok(new String("dynamic"))` → okPath as String
     - **Enum path**: `ok(SomeEnum.SUCCESS)` → okPath = enum toString()
     - **Throwable path**: `ok(new RuntimeException("cause message"))` → okPath = throwable message
     - **Object path**: `ok(new CustomObject())` → okPath = object toString()
   - **Path with successful ok (overriding default)**:
     - `new Meter().start() → path("default_path") → ok("override_path")` → verify okPath = "override_path" (ok() argument overrides path())
     - Verify INFO log shows final okPath in completion report
   - **Multiple successive path() calls before ok()**:
     - `new Meter().start() → path("first") → path("second") → path("third") → ok()` → verify okPath = "third" (last value wins)
     - Verify no error logs for valid path values
   - Verify all variations result in OK state with correct okPath recorded

3. **Created → Started → Rejected**
   - **Basic rejection with String cause**:
     - `new Meter().start() → reject("business_rule_violation")` → verify rejectPath = "business_rule_violation"
   - **Rejection with various cause types**:
     - **String cause**: `reject("validation_failed")`, `reject(new String("dynamic"))` → rejectPath as String
     - **Enum cause**: `reject(ValidationError.DUPLICATE_ENTRY)` → rejectPath = enum toString()
     - **Throwable cause**: `reject(new IllegalArgumentException("invalid input format"))` → rejectPath = throwable class name + message or just message
     - **Object cause**: `reject(new CustomErrorCode())` → rejectPath = object toString()
   - **Rejection after setting path() expectation** (rejection uses its own path):
     - `new Meter().start() → path("expected_ok_path") → reject("business_error")` → verify rejectPath = "business_error", okPath remains unset
   - Verify WARN log with rejection report including correct rejectPath
   - Verify meter transitions to Rejected state

4. **Created → Started → Failed**
   - **Basic failure with String cause**:
     - `new Meter().start() → fail("technical_error")` → verify failPath = "technical_error"
   - **Failure with various cause types**:
     - **String cause**: `fail("database_connection_timeout")`, `fail(new String("dynamic"))` → failPath as String
     - **Enum cause**: `fail(ErrorType.DATABASE_ERROR)` → failPath = enum toString()
     - **Throwable cause**: `fail(new SQLException("connection refused"))` → failPath = throwable class name + message or just message
     - **Object cause**: `fail(new ErrorDetails())` → failPath = object toString()
   - **Failure after setting path() expectation** (failure uses its own path):
     - `new Meter().start() → path("expected_ok_path") → fail("critical_error")` → verify failPath = "critical_error", okPath remains unset
   - Verify ERROR log with failure report including correct failPath
   - Verify meter transitions to Failed state

5. **Created → Started → OK with mixed iterations and progress**
   - `new Meter() → iterations(15) → start() → inc() × 5 → progress() → inc() × 5 → progress() → inc() x 5 → ok()`
   - Verify currentIteration incremented on each `inc()` call
   - Verify `progress()` calls logged periodically during execution (not throttled)
   - Verify progress does not change lifecycle state (remains Started)
   - Verify final currentIteration = 15
   - Verify `getIterationsPerSecond()` calculated correctly
   - Verify INFO log with completion report including iteration and progress metrics
   - Verify progress calls interleaved correctly with iteration increments
   - 
5. **Created → Started → OK with mixed iterations and consecutive progress**
    - `new Meter() → iterations(15) → start() → inc() × 5 → progress() → progress() → inc() x 5 → ok()`
    - Verify currentIteration incremented on each `inc()` call
    - Verify `progress()` calls logged periodically during execution (not throttled)
    - Verify progress does not change lifecycle state (remains Started)
    - Verify final currentIteration = 15
    - Verify `getIterationsPerSecond()` calculated correctly
    - Verify INFO log with completion report including iteration and progress metrics
    - Verify progress calls interleaved correctly with iteration increments

6. **Created → Started → OK with mixed iterations and progress with throttling **
    - `new Meter() → iterations(15) → start() → inc() × 5 → progress() → inc() × 5 → progress() → inc() x 5 → ok()`
    - Verify currentIteration incremented on each `inc()` call
    - Verify `progress()` calls logged periodically during execution (throttled)
    - Verify progress does not change lifecycle state (remains Started)
    - Verify final currentIteration = 15
    - Verify `getIterationsPerSecond()` calculated correctly
    - Verify INFO log with completion report including iteration and progress metrics
    - Verify progress calls interleaved
    - 
7. **Created → Started → OK with time limit (NOT slow)**
    - `new Meter().start() → limitMilliseconds(50) → [execute ~10ms] → ok()`
    - Verify `timeLimit = 50`
    - Verify `executionTime < timeLimit`
    - Verify `isSlow() = false`
    - Verify no slow operation warnings in INFO log
    - Verify completion report includes timing metrics

8. **Created → Started → OK with time limit (IS slow)**
    - `new Meter().start() → limitMilliseconds(50) → [execute ~100ms] → ok()`
    - Verify `timeLimit = 50`
    - Verify `executionTime > timeLimit`
    - Verify `isSlow() = true`
    - Verify slow operation warning included in INFO log
    - Verify completion report highlights slow timing

8. **Created → Started → Reject with time limit (IS slow)**
    - `new Meter().start() → limitMilliseconds(50) → [execute ~100ms] → ok()`
    - Verify `timeLimit = 50`
   - Verify `executionTime > timeLimit`
    - Verify `isSlow() = true`
    - Verify no slow operation warning included in INFO log

9. **Created → Started → OK with high iteration count + time limit (NOT slow)**
   - `new Meter() → iterations(15) → limitMilliseconds(50) → start() → inc() × 5 -> sleep(5) → progress() → inc() × 5 -> sleep(5) → progress() → inc() x 5 -> sleep(5) → ok()`
    - Verify 15 iterations completed successfully
    - Verify `timeLimit = 50`
    - Verify `executionTime < timeLimit` (e.g. 100 < 50)
    - Verify `isSlow() = false`
    - Verify `getIterationsPerSecond()` 
    - Verify INFO log shows high throughput without slow warnings

10. **Created → Started → OK with high iteration count + strict time limit (IS slow)**
    - `new Meter() → iterations(15) → limitMilliseconds(50) → start() → inc() × 5 -> sleep(40) → progress() → inc() × 5 -> sleep(20) → progress() → inc() x 5 -> sleep(10) → ok()`
    - Verify 15 iterations completed successfully
    - Verify `timeLimit = 50`
    - Verify `executionTime < timeLimit` (e.g. 100 < 50)
    - Verify `isSlow() = true`
    - 2nd progress call should report slow also.
    - Verify `getIterationsPerSecond()` 
    - Verify INFO log includes slow operation warning with timing details

11. **Created → Started → Rejected with iterations**
    - `new Meter().start() → inc() × 25 → reject("validation_failed")`
    - Verify currentIteration = 25 before rejection
    - Verify rejectPath = "validation_failed"
    - Verify WARN log with rejection report including iteration count
    - Verify `getIterationsPerSecond()` calculated for rejected operation

11. **Created → Started → Failed with iterations**
    - `new Meter().start() → inc() × 25 → reject("validation_failed")`
    - Verify currentIteration = 25 before rejection
    - Verify rejectPath = "validation_failed"
    - Verify WARN log with rejection report including iteration count
    - Verify `getIterationsPerSecond()` calculated for rejected operation

12. **Created → Started → Failed with progress tracking + time limit (slow)**
    - `new Meter().start() → limitMilliseconds(1000) → progress() → ... → [execute ~3000ms] → fail("timeout")`
    - Verify `timeLimit = 1000`
    - Verify `executionTime = 3000ms > timeLimit` (slow)
    - Verify `isSlow() = true`
    - Verify failPath = "timeout"
    - Verify ERROR log with failure report highlighting slow timeout
    - Verify progress metrics included in error log
---


### **Group 3: Try-With-Resources (All Lifecycle Scenarios)**

**Purpose:** Validate that Meter works correctly within try-with-resources blocks across all lifecycle scenarios. Tests cover both normal flows (with start()) and state-correcting flows (without start()), ensuring proper resource management and state transitions.

**Test Scenarios:**

1. **Try-with-resources WITH start() - Implicit close (normal flow)**
   - `try (Meter m = new Meter(logger).start()) { /* no explicit termination */ }` → implicit close() calls fail()
   - Verify meter transitions Created → Started → Failed
   - Verify ERROR log with implicit failure report
   - Verify try-with-resources cleanup happens correctly
   - Verify Meter removed from thread-local stack after block

2. **Try-with-resources WITH start() - Explicit ok()**
   - `try (Meter m = new Meter(logger).start()) { m.ok(); }` → explicit ok(), then close() does nothing
   - Verify meter transitions Created → Started → OK
   - Verify INFO log with completion report
   - Verify close() is no-op (no additional logs)
   - Test all ok() variations: `ok()`, `ok("success_path")`, `ok(SomeEnum.VALUE)`, `ok(throwable)`, `ok(object)`

3. **Try-with-resources WITH start() - Explicit reject()**
   - `try (Meter m = new Meter(logger).start()) { m.reject("business_error"); }` → explicit reject(), then close() does nothing
   - Verify meter transitions Created → Started → Rejected
   - Verify WARN log with rejection report
   - Verify close() is no-op (no additional logs)
   - Test all reject() variations: `reject(String)`, `reject(Enum)`, `reject(Throwable)`, `reject(Object)`

4. **Try-with-resources WITH start() - Explicit fail()**
   - `try (Meter m = new Meter(logger).start()) { m.fail("technical_error"); }` → explicit fail(), then close() does nothing
   - Verify meter transitions Created → Started → Failed
   - Verify ERROR log with failure report
   - Verify close() is no-op (no additional logs)
   - Test all fail() variations: `fail(String)`, `fail(Enum)`, `fail(Throwable)`, `fail(Object)`

5. **Try-with-resources WITHOUT start() - Implicit close (⚠️ Tier 3)**
   - `try (Meter m = new Meter(logger)) { /* no start(), no explicit termination */ }` → implicit close() calls fail() with auto-correction
   - Verify meter transitions Created → Failed (with startTime auto-initialized)
   - Verify INCONSISTENT_CLOSE + ERROR log for implicit failure
   - Verify "try-with-resources" marker in log
   - Verify Meter removed from thread-local stack after block

6. **Try-with-resources WITHOUT start() - Explicit ok() (⚠️ Tier 3)**
   - `try (Meter m = new Meter(logger)) { m.ok(); }` → explicit ok() without start(), then close() does nothing
   - Verify meter transitions Created → OK (with startTime auto-initialized)
   - Verify INCONSISTENT_OK + INFO log with completion report
   - Verify close() is no-op (no additional logs)
   - Test all ok() variations: `ok()`, `ok("success_path")`, `ok(Enum)`, `ok(Throwable)`, `ok(Object)`

7. **Try-with-resources WITHOUT start() - Explicit reject() (⚠️ Tier 3)**
   - `try (Meter m = new Meter(logger)) { m.reject("business_error"); }` → explicit reject() without start(), then close() does nothing
   - Verify meter transitions Created → Rejected (with startTime auto-initialized)
   - Verify INCONSISTENT_REJECT + WARN log with rejection report
   - Verify close() is no-op (no additional logs)
   - Test all reject() variations: `reject(String)`, `reject(Enum)`, `reject(Throwable)`, `reject(Object)`

8. **Try-with-resources WITHOUT start() - Explicit fail() (⚠️ Tier 3)**
   - `try (Meter m = new Meter(logger)) { m.fail("technical_error"); }` → explicit fail() without start(), then close() does nothing
   - Verify meter transitions Created → Failed (with startTime auto-initialized)
   - Verify INCONSISTENT_FAIL + ERROR log with failure report
   - Verify close() is no-op (no additional logs)
   - Test all fail() variations: `fail(String)`, `fail(Enum)`, `fail(Throwable)`, `fail(Object)`

7. **Try-with-resources WITH start() - Implicit close + iterations via inc()**
   - `try (Meter m = new Meter(logger).start()) { m.inc(); m.inc(); m.inc(); /* no explicit termination */ }` → implicit close() calls fail()
   - Verify meter transitions Created → Started → Failed
   - Verify currentIteration = 3 before failure
   - Verify ERROR log with implicit failure report including iteration count
   - Verify `getIterationsPerSecond()` calculated for failed operation
   - Verify try-with-resources cleanup happens correctly

8. **Try-with-resources WITH start() - Explicit ok() + iterations via incBy()**
    - `try (Meter m = new Meter(logger).start()) { m.incBy(10); m.incBy(15); m.ok(); }` → explicit ok(), then close() does nothing
    - Verify meter transitions Created → Started → OK
    - Verify currentIteration = 25 (10 + 15)
    - Verify INFO log with completion report showing iteration metrics
    - Verify close() is no-op (no additional logs)
    - Verify `getIterationsPerSecond()` accurately reflects throughput

9. **Try-with-resources WITH start() - Explicit fail() + progress tracking**
    - `try (Meter m = new Meter(logger).start()) { m.progress(); ... m.progress(); m.fail("timeout"); }` → explicit fail with progress tracking
    - Verify meter transitions Created → Started → Failed
    - Verify progress logs emitted during execution (throttled)
    - Verify ERROR log with failure report
    - Verify close() is no-op (no additional logs)
    - Verify progress state preserved in error context

10. **Try-with-resources WITH start() - Explicit reject() + time limit (NOT slow)**
    - `try (Meter m = new Meter(logger).start()) { m.limitMilliseconds(5000); m.inc() × 50; /* execute ~500ms */ m.reject("validation_failed"); }`
    - Verify meter transitions Created → Started → Rejected
    - Verify `timeLimit = 5000`
    - Verify `executionTime < timeLimit` (not slow)
    - Verify `isSlow() = false`
    - Verify currentIteration = 50
    - Verify WARN log with rejection report, no slow operation warnings
    - Verify close() is no-op (no additional logs)

11. **Try-with-resources WITH start() - Explicit ok() + time limit (IS slow)**
    - `try (Meter m = new Meter(logger).start()) { m.limitMilliseconds(500); /* execute ~2000ms */ m.ok(); }`
    - Verify meter transitions Created → Started → OK
    - Verify `timeLimit = 500`
    - Verify `executionTime > timeLimit` (slow)
    - Verify `isSlow() = true`
    - Verify INFO log with completion report highlighting slow operation
    - Verify close() is no-op (no additional logs)

12. **Try-with-resources WITH start() - Implicit close (no explicit termination) + high iterations + strict time limit**
    - `try (Meter m = new Meter(logger).start()) { m.iterations(500); m.limitMilliseconds(1000); m.inc() × 100; /* execute ~3000ms, no explicit termination */ }`
    - Verify meter transitions Created → Started → Failed (implicit via close)
    - Verify 100 iterations completed, 400 expected remaining
    - Verify `timeLimit = 1000`
    - Verify `executionTime ≈ 3000ms > timeLimit` (slow)
    - Verify `isSlow() = true`
    - Verify ERROR log with implicit failure, highlighting slow operation
    - Verify try-with-resources cleanup happens correctly

13. **Try-with-resources WITH start() - Explicit ok() + multiple progress() calls + time limit borderline**
    - `try (Meter m = new Meter(logger).start()) { m.limitMilliseconds(1000); m.progress(); ... m.progress(); /* execute ~1000ms */ m.ok(); }`
    - Verify meter transitions Created → Started → OK
    - Verify `timeLimit = 1000`
    - Verify `executionTime = 1000ms == timeLimit` (exactly at limit)
    - Verify `isSlow() = false` (only when executionTime **>** timeLimit)
    - Verify progress logs emitted during execution
    - Verify INFO log shows operation completed at exact limit, not slow

14. **Try-with-resources WITHOUT start() - Implicit close + implicit start with auto-correction**
    - `try (Meter m = new Meter(logger)) { m.inc(); /* no start(), no explicit termination */ }` → implicit close() with auto-correction
    - Verify meter transitions Created → Failed (via auto-correction and implicit close)
    - Verify `inc()` call logged as INCONSISTENT_INCREMENT (meter not started)
    - Verify currentIteration = 0 (increment was rejected/ignored)
    - Verify INCONSISTENT_CLOSE + ERROR log for implicit failure
    - Verify Meter removed from thread-local stack after block
    - Verify startTime auto-initialized during close()

15. **Try-with-resources WITHOUT start() - Explicit ok() + iterations setup (⚠️ Tier 3)**
    - `try (Meter m = new Meter(logger)) { m.iterations(100); m.ok(); }` → explicit ok() without start()
    - Verify meter transitions Created → OK (with startTime auto-initialized)
    - Verify expectedIterations = 100 (setup before termination)
    - Verify INCONSISTENT_OK + INFO log with completion report
    - Verify close() is no-op (no additional logs)
    - Verify expectedIterations preserved in OK state

16. **Try-with-resources WITHOUT start() - Explicit reject() + time limit configuration (⚠️ Tier 3)**
    - `try (Meter m = new Meter(logger)) { m.limitMilliseconds(5000); m.reject("business_error"); }`
    - Verify meter transitions Created → Rejected (with startTime auto-initialized)
    - Verify `timeLimit = 5000` (setup before termination)
    - Verify INCONSISTENT_REJECT + WARN log with rejection report
    - Verify close() is no-op (no additional logs)
    - Verify timeLimit preserved in Rejected state

17. **Try-with-resources WITHOUT start() - Implicit close + progress attempt (⚠️ Tier 3)**
    - `try (Meter m = new Meter(logger)) { m.progress(); /* no start(), no explicit termination */ }` → implicit close() with auto-correction
    - Verify meter transitions Created → Failed (via auto-correction and implicit close)
    - Verify `progress()` call logged as INCONSISTENT_PROGRESS (meter not started)
    - Verify INCONSISTENT_CLOSE + ERROR log for implicit failure
    - Verify try-with-resources cleanup happens correctly
    - Verify startTime auto-initialized during close()

**Coverage Summary for Try-With-Resources:**
- **WITH start() (basic)**: 4 scenarios (implicit close, explicit ok, explicit reject, explicit fail)
- **WITH start() (inc/progress/timeLimit)**: 6 scenarios (7-12)
- **WITHOUT start() (basic)**: 4 scenarios (implicit close, explicit ok, explicit reject, explicit fail)
- **WITHOUT start() (Tier 3 with config)**: 3 scenarios (15-17)
- **Total try-with-resources tests: 17 scenarios** (4 base + 6 advanced = 10 WITH start; 4 base + 3 config = 7 WITHOUT start)

---

### **Group 4: Pre-Start Attribute Updates (☑️ Tier 2 - Valid Non-State-Changing)**

**Purpose:** Validate operations that update support attributes BEFORE `start()`. These calls do not change lifecycle classification but prepare the Meter for execution. Tests verify attribute storage, override behavior, and graceful rejection of invalid arguments.

**Test Scenarios:**

1. **Set time limit**
   - `new Meter() → limitMilliseconds(5000)` → verify timeLimit = 5000 (before start)
   - `new Meter() → limitMilliseconds(100) → limitMilliseconds(5000)` → verify last value wins (timeLimit = 5000)
   - `new Meter() → limitMilliseconds(5000) → limitMilliseconds(0)` → verify invalid value ignored, preserves first valid value (timeLimit = 5000)
   - `new Meter() → limitMilliseconds(-1)` → verify negative value logs ILLEGAL, state unchanged
   - Verify no error logs for valid values, ILLEGAL logs for invalid values

2. **Set expected iterations**
   - `new Meter() → iterations(100)` → verify expectedIterations = 100 (before start)
   - `new Meter() → iterations(50) → iterations(100)` → verify last value wins (expectedIterations = 100)
   - `new Meter() → iterations(100) → iterations(0)` → verify invalid value ignored, preserves first valid value (expectedIterations = 100)
   - `new Meter() → iterations(-5)` → verify negative value logs ILLEGAL, state unchanged
   - Verify no error logs for valid values, ILLEGAL logs for invalid values

3. **Add descriptive message**
   - `new Meter() → m("starting operation")` → verify description = "starting operation" (before start)
   - `new Meter() → m("step 1") → m("step 2")` → verify last value wins (description = "step 2")
   - `new Meter() → m(null)` → verify null value logs ILLEGAL, state unchanged
   - `new Meter() → m("step 1") → m(null)` → verify null value logs ILLEGAL, preserves previous value (description = "step 1")
   - Verify no error logs for valid messages, ILLEGAL log for null

4. **Add formatted descriptive message**
   - `new Meter() → m("operation %s", "doWork")` → verify formatted description stored (before start)
   - `new Meter() → m("step %d", 1) → m("step %d", 2)` → verify last value wins (description = "step 2")
   - `new Meter() → m("valid: %s", "arg") → m(null, "arg")` → verify null format logs ILLEGAL, preserves previous value
   - `new Meter() → m("invalid format %z", "arg")` → verify invalid format string logs ILLEGAL, state unchanged
   - Verify no error logs for valid formats, ILLEGAL logs for null/invalid format

5. **Add contextual key-value pairs (using ctx method)**
   - `new Meter() → ctx("key1", "value1")` → verify context map contains key1=value1 (before start)
   - `new Meter() → ctx("key", "val1") → ctx("key", "val2")` → verify last value wins in context (key=val2)
   - `new Meter() → ctx("key", "valid") → ctx("key", null)` → verify null value replaced (key="<null>")
   - Verify multiple different keys can coexist: `ctx("key1", "val1") → ctx("key2", "val2")` both stored

6. **Chain multiple configurations**
   - `new Meter() → iterations(100) → limitMilliseconds(5000) → m("starting operation")` → verify all attributes set correctly
   - `new Meter() → m("op1") → limitMilliseconds(5000) → iterations(100) → m("op2")` → verify m() last value wins, iterations and limit preserved
   - `new Meter() → limitMilliseconds(5000) → iterations(100) → limitMilliseconds(0) → iterations(-1)` → verify invalid values ignored, all valid values preserved
   - Verify no error logs for valid values, ILLEGAL logs for each invalid attempt

---

### **Group 5: Pre-Start Termination (⚠️ Tier 3 - State-Correcting)**

**Purpose:** Validate that Meter handles termination calls (ok, reject, fail) on unstarted meters by self-correcting: logs INCONSISTENT_* errors but still achieves valid terminal state. These calls violate expected flow (should start first) but succeed with error logging.

**Test Scenarios:**

1. **OK without starting (Created → OK)**
   - `new Meter(logger) → ok()` → logs INCONSISTENT_OK, transitions to OK state
   - Verify meter reaches OK state despite missing start()
   - Verify startTime auto-initialized (not 0)
   - Verify stopTime > 0
   - Verify INFO log with completion report (includes INCONSISTENT_OK warning)
   - Verify okPath not defined (default)
   - Test path variations: `ok("success_path")`, `ok(SomeEnum.SUCCESS)`, `ok(new RuntimeException("cause"))`, `ok(new Object())`
   - Verify all variations log INCONSISTENT_OK but transition successfully
   - Verify path captured correctly for each variation (String, enum toString, throwable message, object toString)

2. **Reject without starting (Created → Rejected)**
   - `new Meter(logger) → reject("business_error")` → logs INCONSISTENT_REJECT, transitions to Rejected state
   - Verify meter reaches Rejected state despite missing start()
   - Verify startTime auto-initialized (not 0)
   - Verify stopTime > 0
   - Verify WARN log with rejection report (includes INCONSISTENT_REJECT warning)
   - Verify rejectPath = "business_error"
   - Test cause variations: `reject(SomeEnum.VALIDATION_ERROR)`, `reject(new IllegalArgumentException("invalid input"))`, `reject(new Object())`
   - Verify all variations log INCONSISTENT_REJECT but transition successfully
   - Verify cause captured correctly for each variation

3. **Fail without starting (Created → Failed)**
   - `new Meter(logger) → fail("technical_error")` → logs INCONSISTENT_FAIL, transitions to Failed state
   - Verify meter reaches Failed state despite missing start()
   - Verify startTime auto-initialized (not 0)
   - Verify stopTime > 0
   - Verify ERROR log with failure report (includes INCONSISTENT_FAIL warning)
   - Verify failPath = "technical_error"
   - Test cause variations: `fail(SomeEnum.DATABASE_ERROR)`, `fail(new SQLException("connection timeout"))`, `fail(new Object())`
   - Verify all variations log INCONSISTENT_FAIL but transition successfully
   - Verify cause captured correctly for each variation

4. **Pre-configured attributes preserved on self-correcting termination**
   - `new Meter(logger) → iterations(100) → limitMilliseconds(5000) → m("operation") → ok()` → logs INCONSISTENT_OK
   - Verify expectedIterations = 100 preserved in terminal state
   - Verify timeLimit = 5000 preserved
   - Verify description = "operation" preserved
   - Verify all attributes appear in completion report
   - Test same with reject() and fail()

6. **Context preserved on self-correcting termination**
   - `new Meter(logger) → ctx("user", "alice") → ctx("action", "import") → reject("validation_error")` → logs INCONSISTENT_REJECT
   - Verify context entries preserved in terminal state
   - Verify context appears in rejection report
   - Test same with ok() and fail()

7. **Path set before starting (rejected, then termination)**
   - `new Meter(logger) → path("custom") → ok()` → logs ILLEGAL (path before start), then INCONSISTENT_OK
   - Verify path("custom") rejected (logs ILLEGAL), okPath remains undefined after ok()
   - Verify meter still reaches OK state
   - Test same pattern with reject() and fail()

---

### **Group 6: Pre-Start Invalid Operations (❌ Tier 4 - State-Preserving)**

**Purpose:** Validate that Meter rejects invalid operations on unstarted Meters by preserving current state and logging errors. These calls have invalid preconditions (meter not yet started) or invalid arguments.

**Test Scenarios:**

1. **Increment operations without starting**
   - `new Meter() → inc()` → logs INCONSISTENT_INCREMENT, currentIteration unchanged
   - `new Meter() → incBy(5)` → logs INCONSISTENT_INCREMENT, currentIteration unchanged
   - `new Meter() → incTo(10)` → logs INCONSISTENT_INCREMENT, currentIteration unchanged
   - Verify meter remains in Created state

2. **Progress without starting**
   - `new Meter() → progress()` → logs INCONSISTENT_PROGRESS
   - Verify meter remains in Created state
   - Verify no progress report generated

3. **Set path before starting**
   - `new Meter() → path("some_path")` → logs ILLEGAL
   - Verify okPath not defined
   - Verify meter remains in Created state

---

### **Group 7: Post-Start Attribute Updates (☑️ Tier 2 - Valid Non-State-Changing)**

**Purpose:** Validate operations that update support attributes AFTER `start()`. These calls do not change lifecycle but support progress tracking and context management during execution. Tests verify attribute storage, override behavior, and graceful rejection of invalid arguments.

**Test Scenarios:**

1. **Update description with valid and invalid values**
   - `start() → m("step 1")` → verify description = "step 1"
   - `start() → m("step 1") → m("step 2")` → verify last message wins (description = "step 2")
   - `start() → m("valid") → m(null)` → verify null rejected (logs ILLEGAL), preserves "valid"
   - `start() → m("step %d", 1)` → verify formatted message (description = "step 1")
   - `start() → m("step %d", 1) → m("step %d", 2)` → verify last format wins (description = "step 2")
   - `start() → m("valid: %s", "arg") → m(null, "arg")` → verify null format rejected (logs ILLEGAL), preserves previous
   - `start() → m("invalid %z", "arg")` → verify invalid format logs ILLEGAL, description unchanged
   - `start() → m("valid") → m("invalid %z", "arg") → m("final")` → verify invalid attempt skipped, final overwrites

2. **Update iteration counters with valid and invalid values**
   - `start() → inc() → progress()` → verify currentIteration = 1, progress message logged (requires `MeterConfig.progressPeriodMilliseconds=0`)
   - `start() → inc() → inc() → progress()` → verify currentIteration = 2, progress message logged
   - `start() → incBy(5) → progress()` → verify currentIteration = 5, progress message logged
   - `start() → incBy(5) → incBy(3) → progress()` → verify currentIteration = 8, progress message logged
   - `start() → incTo(10) → progress()` → verify currentIteration = 10, progress message logged
   - `start() → incTo(10) → incTo(50) → progress()` → verify currentIteration = 50, progress message logged
   - `start() → incTo(100) → incTo(50) → progress()` → verify backward movement preserves current (currentIteration = 100), progress message logged
   - `start() → incBy(5) → incBy(0) → progress()` → verify 0 rejected (logs ILLEGAL), currentIteration = 5, progress message still logged
   - `start() → incBy(5) → incBy(-3) → progress()` → verify negative rejected (logs ILLEGAL), currentIteration = 5, progress message still logged
   - `start() → incBy(-5) → progress()` → verify negative rejected (logs ILLEGAL), currentIteration = 0, progress message still logged
   - `start() → incBy(0) → progress()` → verify zero rejected (logs ILLEGAL), currentIteration = 0, progress message still logged
   - `start() → incTo(10) → incTo(3) → progress()` → verify backward target rejected (logs ILLEGAL), currentIteration = 10, progress message still logged
   - `start() → incTo(-5) → progress()` → verify negative target rejected (logs ILLEGAL), currentIteration unchanged (0), progress message still logged
   - `start() → incTo(0) → progress()` → verify zero target rejected (logs ILLEGAL), currentIteration unchanged (0), progress message still logged
   - `start() → inc() → incBy(-1) → incTo(0) → inc() → progress()` → verify invalid attempts skipped, valid ones succeed, progress message logged
   - `start() → inc() → progress() → progress()` → verify second progress() does NOT generate message (no iteration change), currentIteration = 1 (requires `MeterConfig.progressPeriodMilliseconds=0`)
   - `start() → inc() → progress()` with `MeterConfig.progressPeriodMilliseconds=1000000` → verify progress() does NOT generate message (throttling active, insufficient time elapsed)
   - `start() → inc() → progress() → inc() → progress()` with `MeterConfig.progressPeriodMilliseconds=0` → verify both progress() calls generate messages (no throttling, iteration changed between them)

   **Note:** To observe progress messages in test assertions, set `MeterConfig.progressPeriodMilliseconds = 0` before starting the meter. This eliminates the throttling delay and allows immediate progress reporting on each call to `progress()`, making message validation possible in unit tests.

3. **Update context during execution**
   - `start() → ctx("key1", "value1")` → verify context has key1=value1
   - `start() → ctx("key", "val1") → ctx("key", "val2")` → verify last value wins (key=val2)
   - `start() → ctx("key", "valid") → ctx("key", null)` → verify null stored as "<null>" (key="<null>")
   - `start() → ctx("key1", "val1") → ctx("key2", "val2")` → verify multiple keys coexist
   - `start() → ctx(null)` → verify stores with "<null>" key (valid behavior)
   - `start() → ctx("key", null)` → verify stores "<null>" value (valid behavior)

4. **Set path with valid values**
   - `start() → path("custom_ok_path")` → verify okPath = "custom_ok_path"
   - `start() → path("path1") → path("path2")` → verify last path wins (okPath = "path2")
   - `start() → path("valid") → path(null)` → verify null rejected (logs ILLEGAL), preserves "valid"
   - `start() → path(null)` → verify null rejected (logs ILLEGAL), okPath unchanged/not defined
   - `start() → path(null) → path("valid")` → verify null rejected, then valid accepted (okPath = "valid")
   - Verify path variations work: String, Enum, Throwable, Object values

5. **Update time limit with valid and invalid values**
   - `start() → limitMilliseconds(5000)` → verify timeLimit = 5000
   - `start() → limitMilliseconds(100) → limitMilliseconds(5000)` → verify last value wins (timeLimit = 5000)
   - `start() → limitMilliseconds(5000) → limitMilliseconds(100)` → verify valid value accepted, last wins (timeLimit = 100)
   - `start() → limitMilliseconds(5000) → limitMilliseconds(0)` → verify 0 rejected (logs ILLEGAL), timeLimit = 5000
   - `start() → limitMilliseconds(5000) → limitMilliseconds(-1)` → verify negative rejected (logs ILLEGAL), timeLimit = 5000
   - `start() → limitMilliseconds(0)` → verify 0 rejected (logs ILLEGAL), timeLimit unchanged
   - `start() → limitMilliseconds(-5)` → verify negative rejected (logs ILLEGAL), timeLimit unchanged

6. **Update expected iterations with valid and invalid values**
   - `start() → iterations(100)` → verify expectedIterations = 100
   - `start() → iterations(50) → iterations(100)` → verify last value wins (expectedIterations = 100)
   - `start() → iterations(100) → iterations(50)` → verify valid value accepted, last wins (expectedIterations = 50)
   - `start() → iterations(100) → iterations(0)` → verify 0 rejected (logs ILLEGAL), expectedIterations = 100
   - `start() → iterations(100) → iterations(-5)` → verify negative rejected (logs ILLEGAL), expectedIterations = 100
   - `start() → iterations(0)` → verify 0 rejected (logs ILLEGAL), expectedIterations unchanged
   - `start() → iterations(-5)` → verify negative rejected (logs ILLEGAL), expectedIterations unchanged

7. **Chain operations mixing valid and invalid values**
   - `start() → m("op1") → ctx("user", "alice") → iterations(100) → inc()` → all valid, all succeed
   - `start() → limitMilliseconds(5000) → m("valid") → path("custom") → inc()` → all valid, all succeed
   - `start() → m("valid") → m(null) → ctx("key", "val") → inc()` → one invalid (m(null)), others succeed
   - `start() → iterations(100) → iterations(-1) → inc() → incBy(0) → inc()` → two invalid, valid ones succeed
   - `start() → path("path1") → incBy(5) → m("step") → path(null) → incBy(3)` → mixed valid/invalid, verify correct ones apply
   - `start() → m(null) → m("step1") → limitMilliseconds(-1) → limitMilliseconds(5000)` → invalid calls skipped, final values correct

---

### **Group 8: Post-Start Termination (✅ Tier 1 - Valid State-Changing)**

**Purpose:** Validate that meters started with various attribute combinations can be terminated via all four paths (ok, ok(path), reject(path), fail(path)) with different path type variations and path() call sequences. Tests verify correct state transitions, appropriate log levels, and outcome attribute storage.

**Test Scenarios:**

#### 1. **Termination via ok() - No Path Configuration**
   - `start() → ok()` → verify meter transitions to OK state, okPath unset, INFO log
   - `start() → m("operation") → ok()` → verify description preserved, okPath unset, INFO log
   - `start() → inc() × 5 → ok()` → verify currentIteration = 5 preserved, INFO log with metrics

#### 2. **Termination via ok(path) with Path Type Variations - Direct Path Argument**
   - **String path**: `start() → ok("success_outcome")` → verify okPath = "success_outcome", INFO log
   - **Enum path**: `start() → ok(OutcomeType.SUCCESS)` → verify okPath = enum toString(), INFO log
   - **Throwable path**: `start() → ok(new Exception("success_cause"))` → verify okPath = exception class name, INFO log
   - **Object path**: `start() → ok(new OutcomeObject())` → verify okPath = object toString(), INFO log
   - **Null path**: `start() → ok(null)` → verify okPath remains unset, logs ILLEGAL, completes with INFO log

#### 3. **Termination via ok() after Single path() Call**
   - `start() → path("configured_path") → ok()` → verify okPath = "configured_path", INFO log (path() sets default for ok())
   - `start() → path("configured_path") → ok("override_path")` → verify okPath = "override_path", INFO log (ok(path) overrides path())
   - `start() → m("step") → path("step_path") → ok()` → verify okPath = "step_path", description and path both preserved, INFO log

#### 4. **Termination via ok() after Multiple path() Calls (Last Wins)**
   - `start() → path("first") → path("second") → ok()` → verify okPath = "second", INFO log
   - `start() → path("first") → path("second") → path("third") → ok()` → verify okPath = "third", INFO log
   - `start() → path("first") → path("second") → ok("final_override")` → verify okPath = "final_override", INFO log (ok() overrides last path())

#### 5. **Termination via reject(path) with Path Type Variations**
   - **String cause**: `start() → reject("validation_error")` → verify rejectPath = "validation_error", INFO log
   - **Enum cause**: `start() → reject(RejectionReason.INVALID_INPUT)` → verify rejectPath = enum toString(), INFO log
   - **Throwable cause**: `start() → reject(new IllegalArgumentException("invalid format"))` → verify rejectPath = exception class name, INFO log
   - **Object cause**: `start() → reject(new ValidationError())` → verify rejectPath = object toString(), INFO log

#### 6. **Termination via reject(path) after Single path() Call**
   - `start() → path("ok_path") → reject("business_error")` → verify rejectPath = "business_error", okPath remains unset, INFO log
   - `start() → m("step") → path("ok_expectation") → reject("precondition_failed")` → verify rejectPath = "precondition_failed", okPath unset, INFO log
   - Verify reject() path is independent from path() call (path() doesn't affect reject outcome)

#### 7. **Termination via reject(path) after Multiple path() Calls**
   - `start() → path("first") → path("second") → reject("business_rule")` → verify rejectPath = "business_rule", okPath unset, INFO log
   - `start() → path("configured") → path("updated") → reject("validation_failed")` → verify rejectPath = "validation_failed", INFO log
   - Verify multiple path() calls don't affect reject() outcome

#### 8. **Termination via fail(path) with Path Type Variations**
   - **String cause**: `start() → fail("critical_error")` → verify failPath = "critical_error", ERROR log
   - **Enum cause**: `start() → fail(ErrorType.DATABASE_FAILURE)` → verify failPath = enum toString(), ERROR log
   - **Throwable cause**: `start() → fail(new SQLException("connection refused"))` → verify failPath = exception details, ERROR log
   - **Object cause**: `start() → fail(new SystemError())` → verify failPath = object toString(), ERROR log

#### 9. **Termination via fail(path) after Single path() Call**
   - `start() → path("success_expectation") → fail("timeout")` → verify failPath = "timeout", okPath remains unset, ERROR log
   - `start() → m("operation") → path("ok_path") → fail("unexpected_exception")` → verify failPath = "unexpected_exception", okPath unset, ERROR log
   - Verify fail() path is independent from path() call

#### 10. **Termination via fail(path) after Multiple path() Calls**
   - `start() → path("first") → path("second") → fail("system_error")` → verify failPath = "system_error", okPath unset, ERROR log
   - `start() → path("configured") → path("updated") → fail("critical_failure")` → verify failPath = "critical_failure", ERROR log
   - Verify multiple path() calls don't affect fail() outcome

#### 11. **Termination via ok() with success() Alias**
   - `start() → success()` → alias for ok(), verify meter transitions to OK state, okPath unset, INFO log
   - `start() → success("alias_path")` → verify okPath = "alias_path", INFO log
   - `start() → path("configured") → success()` → verify okPath = "configured", INFO log (path() sets default for success())

#### 12. **ok(null) vs path(null) Semantics - Different Behaviors**
   - `start() → path(null) → ok()` → verify path rejects null (logs ILLEGAL), okPath = null, INFO log for ok()
   - `start() → path("validPath") → ok(null)` → verify ok() ignores null (logs ILLEGAL), okPath = "validPath" preserved, INFO log (ok() does not overwrite with null)
   - `start() → ok(null)` → verify okPath = null, ILLEGAL logged, completes with INFO log
   - **Key difference**: ok(null) attempts completion with null, path(null) rejects null but path() can still proceed to ok()

#### 13. **fail(Throwable) with Separate className and message**
   - `start() → fail(new SQLException("connection refused"))` → verify failPath = "SQLException" (className), failMessage = "connection refused" (message), ERROR log
   - `start() → fail(new RuntimeException("null pointer"))` → verify failPath = "RuntimeException", failMessage = "null pointer" (separated), ERROR log
   - **Important**: Throwable cause captures className and message as separate attributes, not combined in failPath

#### 14. **Complex Chains with Attributes + Termination**
   - `start() → m("operation") → iterations(100) → inc() × 50 → path("checkpoint") → ok()` → verify description, iterations, okPath all preserved, INFO log
   - `start() → limitMilliseconds(5000) → inc() × 25 → path("expected") → reject("performance_degradation")` → verify timeLimit, iterations, rejectPath all correct, INFO log
   - `start() → m("critical") → ctx("user", "admin") → inc() × 10 → fail("auth_failure")` → verify description, context, iterations, failPath all preserved, ERROR log

#### 17. **Termination after No Operations (Minimal Meter)**
   - `start() → ok()` → verify clean transition with no additional attributes, INFO log
   - `start() → reject("no_work_done")` → verify rejectPath captured, INFO log
   - `start() → fail("no_work_done")` → verify failPath captured, ERROR log

#### 18. **Slow Operation Detection - Marker and Level Changes**
   - **ok() with slow**: `start() → limitMilliseconds(10) → [simulate 100ms] → ok()` → verify isSlow() = true, WARN log with MSG_SLOW_OK marker (not MSG_OK)
   - **reject() with slow**: `start() → limitMilliseconds(10) → [simulate 100ms] → reject("timeout")` → verify isSlow() = true, INFO log (normal level, may have MSG_SLOW_REJECT)
   - **fail() with slow**: `start() → limitMilliseconds(10) → [simulate 100ms] → fail("timeout")` → verify isSlow() = true, ERROR log with slow indication
   - **Important**: Slow detection changes log marker from MSG_* to MSG_SLOW_* for ok()

---

### **Group 9: Post-Stop Invalid Operations - OK State (❌ Tier 4 - State-Preserving)**

**Purpose:** Validate that operations on terminated meters (OK state) are rejected while preserving current state and logging errors. These calls have invalid preconditions (meter already stopped) and do not change state or outcome attributes.

**Test Scenarios:**

1. **Update description after stop (OK state)**
   - `start() → ok() → m("step 1")` → logs ILLEGAL (Meter already stopped), description unchanged
   - `start() → ok() → m("step %d", 1)` → logs ILLEGAL, description unchanged
   - `start() → ok() → m(null)` → logs ILLEGAL, description unchanged
   - `start() → ok("completion_path") → m("step 1")` → logs ILLEGAL, description unchanged
   - `start() → ok("completion_path") → m("step %d", 1)` → logs ILLEGAL, description unchanged
   - `start() → ok("completion_path") → m(null)` → logs ILLEGAL, description unchanged

2. **Increment operations after stop (OK state)**
   - `start() → ok() → inc()` → logs INCONSISTENT_INCREMENT (Meter already stopped), currentIteration unchanged (0)
   - `start() → ok() → incBy(5)` → logs INCONSISTENT_INCREMENT, currentIteration unchanged (0)
   - `start() → ok() → incTo(10)` → logs INCONSISTENT_INCREMENT, currentIteration unchanged (0)
   - `start() → ok("completion_path") → inc()` → logs INCONSISTENT_INCREMENT, currentIteration unchanged (0)
   - `start() → ok("completion_path") → incBy(5)` → logs INCONSISTENT_INCREMENT, currentIteration unchanged (0)
   - `start() → ok("completion_path") → incTo(10)` → logs INCONSISTENT_INCREMENT, currentIteration unchanged (0)

3. **Progress after stop (OK state)**
   - `start() → ok() → progress()` → logs INCONSISTENT_PROGRESS (Meter already stopped), no progress message
   - `start() → inc() → ok() → progress()` → logs INCONSISTENT_PROGRESS, no further progress logged
   - `start() → ok("completion_path") → progress()` → logs INCONSISTENT_PROGRESS, no progress message
   - `start() → inc() → ok("completion_path") → progress()` → logs INCONSISTENT_PROGRESS, no further progress logged

4. **Update context after stop (OK state)**
   - `start() → ok() → ctx("key1", "value1")` → logs ILLEGAL (Meter already stopped), context unchanged
   - `start() → ctx("key", "val") → ok() → ctx("key", "val2")` → logs ILLEGAL, context preserves original value
   - `start() → ok("completion_path") → ctx("key1", "value1")` → logs ILLEGAL, context unchanged
   - `start() → ctx("key", "val") → ok("completion_path") → ctx("key", "val2")` → logs ILLEGAL, context preserves original value

5. **Set path after stop (OK state)**
   - `start() → ok() → path("new_path")` → logs ILLEGAL (Meter already stopped), okPath unchanged/not redefined
   - `start() → ok("original_path") → path("new_path")` → logs ILLEGAL, okPath remains "original_path"
   - `start() → ok() → path(null)` → logs ILLEGAL, okPath unchanged
   - `start() → ok("completion_path") → path("new_path")` → logs ILLEGAL, okPath remains "completion_path"
   - `start() → ok("completion_path") → path(null)` → logs ILLEGAL, okPath unchanged

6. **Update time limit after stop (OK state)**
   - `start() → ok() → limitMilliseconds(5000)` → logs ILLEGAL (Meter already stopped), timeLimit unchanged
   - `start() → limitMilliseconds(100) → ok() → limitMilliseconds(5000)` → logs ILLEGAL, timeLimit remains 100
   - `start() → ok() → limitMilliseconds(0)` → logs ILLEGAL, timeLimit unchanged
   - `start() → ok() → limitMilliseconds(-1)` → logs ILLEGAL, timeLimit unchanged
   - `start() → ok("completion_path") → limitMilliseconds(5000)` → logs ILLEGAL, timeLimit unchanged
   - `start() → limitMilliseconds(100) → ok("completion_path") → limitMilliseconds(5000)` → logs ILLEGAL, timeLimit remains 100
   - `start() → ok("completion_path") → limitMilliseconds(0)` → logs ILLEGAL, timeLimit unchanged
   - `start() → ok("completion_path") → limitMilliseconds(-1)` → logs ILLEGAL, timeLimit unchanged

7. **Update expected iterations after stop (OK state)**
   - `start() → ok() → iterations(100)` → logs ILLEGAL (Meter already stopped), expectedIterations unchanged
   - `start() → iterations(50) → ok() → iterations(100)` → logs ILLEGAL, expectedIterations remains 50
   - `start() → ok() → iterations(0)` → logs ILLEGAL, expectedIterations unchanged
   - `start() → ok() → iterations(-5)` → logs ILLEGAL, expectedIterations unchanged
   - `start() → ok("completion_path") → iterations(100)` → logs ILLEGAL, expectedIterations unchanged
   - `start() → iterations(50) → ok("completion_path") → iterations(100)` → logs ILLEGAL, expectedIterations remains 50
   - `start() → ok("completion_path") → iterations(0)` → logs ILLEGAL, expectedIterations unchanged
   - `start() → ok("completion_path") → iterations(-5)` → logs ILLEGAL, expectedIterations unchanged

---

### **Group 10: Post-Stop Invalid Operations - Rejected State (❌ Tier 4 - State-Preserving)**

**Purpose:** Validate that operations on rejected meters (Rejected state) are rejected while preserving current state and logging errors. These calls have invalid preconditions (meter already stopped with rejection) and do not change state or outcome attributes.

**Test Scenarios:**

1. **Update description after reject (Rejected state)**
   - `start() → reject("business_error") → m("step 1")` → logs ILLEGAL (Meter already stopped), description unchanged
   - `start() → reject("business_error") → m("step %d", 1)` → logs ILLEGAL, description unchanged
   - `start() → reject("business_error") → m(null)` → logs ILLEGAL, description unchanged

2. **Increment operations after reject (Rejected state)**
   - `start() → reject("business_error") → inc()` → logs INCONSISTENT_INCREMENT (Meter already stopped), currentIteration unchanged (0)
   - `start() → reject("business_error") → incBy(5)` → logs INCONSISTENT_INCREMENT, currentIteration unchanged (0)
   - `start() → reject("business_error") → incTo(10)` → logs INCONSISTENT_INCREMENT, currentIteration unchanged (0)

3. **Progress after reject (Rejected state)**
   - `start() → reject("business_error") → progress()` → logs INCONSISTENT_PROGRESS (Meter already stopped), no progress message
   - `start() → inc() → reject("business_error") → progress()` → logs INCONSISTENT_PROGRESS, no further progress logged

4. **Update context after reject (Rejected state)**
   - `start() → reject("business_error") → ctx("key1", "value1")` → logs ILLEGAL (Meter already stopped), context unchanged
   - `start() → ctx("key", "val") → reject("business_error") → ctx("key", "val2")` → logs ILLEGAL, context preserves original value

5. **Set path after reject (Rejected state)**
   - `start() → reject("business_error") → path("new_path")` → logs ILLEGAL (Meter already stopped), rejectPath unchanged/not redefined
   - `start() → reject("original_error") → path("new_path")` → logs ILLEGAL, rejectPath remains "original_error"
   - `start() → reject("business_error") → path(null)` → logs ILLEGAL, rejectPath unchanged

6. **Update time limit after reject (Rejected state)**
   - `start() → reject("business_error") → limitMilliseconds(5000)` → logs ILLEGAL (Meter already stopped), timeLimit unchanged
   - `start() → limitMilliseconds(100) → reject("business_error") → limitMilliseconds(5000)` → logs ILLEGAL, timeLimit remains 100
   - `start() → reject("business_error") → limitMilliseconds(0)` → logs ILLEGAL, timeLimit unchanged
   - `start() → reject("business_error") → limitMilliseconds(-1)` → logs ILLEGAL, timeLimit unchanged

7. **Update expected iterations after reject (Rejected state)**
   - `start() → reject("business_error") → iterations(100)` → logs ILLEGAL (Meter already stopped), expectedIterations unchanged
   - `start() → iterations(50) → reject("business_error") → iterations(100)` → logs ILLEGAL, expectedIterations remains 50
   - `start() → reject("business_error") → iterations(0)` → logs ILLEGAL, expectedIterations unchanged
   - `start() → reject("business_error") → iterations(-5)` → logs ILLEGAL, expectedIterations unchanged

---

### **Group 11: Post-Stop Invalid Operations - Failed State (❌ Tier 4 - State-Preserving)**

**Purpose:** Validate that operations on failed meters (Failed state) are rejected while preserving current state and logging errors. These calls have invalid preconditions (meter already stopped with failure) and do not change state or outcome attributes.

**Test Scenarios:**

1. **Update description after fail (Failed state)**
   - `start() → fail("technical_error") → m("step 1")` → logs ILLEGAL (Meter already stopped), description unchanged
   - `start() → fail("technical_error") → m("step %d", 1)` → logs ILLEGAL, description unchanged
   - `start() → fail("technical_error") → m(null)` → logs ILLEGAL, description unchanged

2. **Increment operations after fail (Failed state)**
   - `start() → fail("technical_error") → inc()` → logs INCONSISTENT_INCREMENT (Meter already stopped), currentIteration unchanged (0)
   - `start() → fail("technical_error") → incBy(5)` → logs INCONSISTENT_INCREMENT, currentIteration unchanged (0)
   - `start() → fail("technical_error") → incTo(10)` → logs INCONSISTENT_INCREMENT, currentIteration unchanged (0)

3. **Progress after fail (Failed state)**
   - `start() → fail("technical_error") → progress()` → logs INCONSISTENT_PROGRESS (Meter already stopped), no progress message
   - `start() → inc() → fail("technical_error") → progress()` → logs INCONSISTENT_PROGRESS, no further progress logged

4. **Update context after fail (Failed state)**
   - `start() → fail("technical_error") → ctx("key1", "value1")` → logs ILLEGAL (Meter already stopped), context unchanged
   - `start() → ctx("key", "val") → fail("technical_error") → ctx("key", "val2")` → logs ILLEGAL, context preserves original value

5. **Set path after fail (Failed state)**
   - `start() → fail("technical_error") → path("new_path")` → logs ILLEGAL (Meter already stopped), failPath unchanged/not redefined
   - `start() → fail("original_error") → path("new_path")` → logs ILLEGAL, failPath remains "original_error"
   - `start() → fail("technical_error") → path(null)` → logs ILLEGAL, failPath unchanged

6. **Update time limit after fail (Failed state)**
   - `start() → fail("technical_error") → limitMilliseconds(5000)` → logs ILLEGAL (Meter already stopped), timeLimit unchanged
   - `start() → limitMilliseconds(100) → fail("technical_error") → limitMilliseconds(5000)` → logs ILLEGAL, timeLimit remains 100
   - `start() → fail("technical_error") → limitMilliseconds(0)` → logs ILLEGAL, timeLimit unchanged
   - `start() → fail("technical_error") → limitMilliseconds(-1)` → logs ILLEGAL, timeLimit unchanged

7. **Update expected iterations after fail (Failed state)**
   - `start() → fail("technical_error") → iterations(100)` → logs ILLEGAL (Meter already stopped), expectedIterations unchanged
   - `start() → iterations(50) → fail("technical_error") → iterations(100)` → logs ILLEGAL, expectedIterations remains 50
   - `start() → fail("technical_error") → iterations(0)` → logs ILLEGAL, expectedIterations unchanged
   - `start() → fail("technical_error") → iterations(-5)` → logs ILLEGAL, expectedIterations unchanged

---



## Implementation Guidelines

### Test Isolation with Group 1

Each test in Groups 2-15 should:
1. **Assume** Group 1 guarantees have been validated
2. **Trust** that `new Meter(logger).start()` works correctly
3. **Skip** redundant initialization checks
4. **Focus** exclusively on the group's primary concern

### Error Log Validation

Use `AssertLogger` for all log validation:
```java
// ✅ CORRECT: Semantic assertion
AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.WARN,
    "INCONSISTENT_INCREMENT");

// ❌ WRONG: Direct MockLogger assertion
assertEquals(1, mockLogger.getEventCount());
```

### Configuration Annotations

All test classes should use:
```java
@ValidateCharset
@ResetMeterConfig
@WithLocale("en")
@WithMockLogger
@ValidateCleanMeter
@SuppressWarnings({"AssignmentToStaticFieldFromInstanceMethod", "IOResourceOpenedButNotSafelyClosed", "TestMethodWithoutAssertion"})
class MeterLifeCycleTest { ... }
```

This ensures:
- Consistent charset across environments
- Clean configuration state for each test
- Consistent locale for string comparisons
- MockLogger for output validation
- Clean meter stack verification

---

## References

- [TDR-0019: Immutable Lifecycle Transitions](TDR-0019-immutable-lifecycle-transitions.md)
- [TDR-0020: Three Outcome Types (OK, REJECT, FAIL)](TDR-0020-three-outcome-types-ok-reject-fail.md)
- [TDR-0024: Try-With-Resources Lifecycle Fit and Limitations](TDR-0024-try-with-resources-lifecycle-fit-and-limitations.md)
- [TDR-0029: Resilient State Transitions with Chained API](TDR-0029-resilient-state-transitions-with-chained-api.md)
- [meter-state-diagram.md](meter-state-diagram.md) - Visual state machine representation
- [MeterLifeCycleTest.java](../src/test/java/org/usefultoys/slf4j/meter/MeterLifeCycleTest.java) - Test implementation
