# Meter Lifecycle Test Hierarchy

**Document:** `doc/meter-lifecycle-tests.md`  
**Purpose:** Enumerate and organize test groups for comprehensive coverage of Meter state transitions across all four resilience tiers.  
**Related TDRs:** [TDR-0019](TDR-0019-immutable-lifecycle-transitions.md), [TDR-0020](TDR-0020-three-outcome-types-ok-reject-fail.md), [TDR-0029](TDR-0029-resilient-state-transitions-with-chained-api.md), [meter-state-diagram.md](meter-state-diagram.md)

---

## Overview

This document defines a hierarchical test strategy for the `Meter` lifecycle to achieve comprehensive coverage of:

1. **Happy path** (Tier 1 - ✅ Valid state-changing) - Normal expected flows
2. **Attribute updates** (Tier 2 - ☑️ Valid non-state-changing) - Support operations
3. **State-correcting** (Tier 3 - ⚠️ Outside expected flow) - Violations that self-correct
4. **State-preserving** (Tier 4 - ❌ Invalid flow) - Rejections that preserve state

By organizing tests in groups from foundational (Group 1) to complex scenarios (Group 10), we ensure:
- **Reliability**: Each test has a solid foundation to build upon
- **Clarity**: Each test group focuses on a specific aspect of behavior
- **Maintainability**: Changes to one group don't cascade to others
- **Coverage**: All state transitions and error conditions are validated

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

### **Group 2: Pre-Start Configuration (☑️ Tier 2 - Valid Non-State-Changing)**

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

### **Group 3: Pre-Start Configuration - Invalid Operations (❌ Tier 4 - Invalid State-Preserving)**

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

### **Group 4: Post-Start Configuration (☑️ Tier 2 - Valid Non-State-Changing)**

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

### **Group 5: Post-Stop Configuration (❌ Tier 4 - Invalid State-Preserving)**

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

### **Group 6: Post-Stop Configuration - Rejected State (❌ Tier 4 - Invalid State-Preserving)**

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

### **Group 7: Post-Stop Configuration - Failed State (❌ Tier 4 - Invalid State-Preserving)**

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

### **Group 8: Valid Expected Flows (✅ Tier 1 - Valid State-Changing)**

**Purpose:** Validate normal lifecycle transitions without errors. These are the expected, successful paths through the state machine.

**Test Scenarios:**

1. **Created → Started → OK (simple)**
   - `new Meter().start() → ok()` → verify correct state transition
   - Verify INFO log with completion report

2. **Created → Started → OK with custom path**
   - `new Meter().start() → ok("success_path")`
   - `new Meter().start() → path("custom_path") → ok()`
   - Verify variations: String, Enum, Throwable, Object as path

3. **Created → Started → Rejected**
   - `new Meter().start() → reject("business_rule_violation")`
   - Verify variations: String, Enum, Throwable, Object as cause
   - Verify rejectPath recorded

4. **Created → Started → Failed**
   - `new Meter().start() → fail("technical_error")`
   - Verify variations: String, Enum, Throwable, Object as cause
   - Verify failPath recorded

5. **Created → Started → Failed (via try-with-resources)**
   - `try (Meter m = new Meter().start()) { ... }` without calling ok(), reject(), or fail()
   - Verify close() automatically triggers fail with path "try-with-resources"
   - Verify WARN/ERROR log for implicit failure

---

### **Group 9: State-Correcting Transitions (⚠️ Tier 3 - Outside Expected Flow)**

**Purpose:** Validate that Meter handles violations of expected flow gracefully by self-correcting. These calls violate the API contract but achieve valid state with error logs.

**Test Scenarios:**

1. **Terminate without starting (Created → OK/Rejected/Failed)**
   - `new Meter() → ok()` → logs INCONSISTENT_OK, but terminates correctly
   - `new Meter() → ok("custom_path")` → logs INCONSISTENT_OK
   - `new Meter() → reject("cause")` → logs INCONSISTENT_REJECT, terminates with rejection
   - `new Meter() → fail("cause")` → logs INCONSISTENT_FAIL, terminates with failure
   - Verify meter reaches terminated state despite violation

2. **Multiple termination attempts (first-termination-wins immutability)**
   - `start() → ok() → ok()` → second ok ignored, state unchanged
   - `start() → ok() → reject("cause")` → reject ignored
   - `start() → ok() → fail("cause")` → fail ignored
   - `start() → reject("cause1") → fail("cause2")` → fail ignored
   - `start() → fail("cause") → ok()` → ok ignored
   - Verify INCONSISTENT_* logs for extra attempts

---

### **Group 10: State-Preserving - Invalid Preconditions (❌ Tier 4a)**

**Purpose:** Validate that Meter rejects calls with invalid preconditions by preserving current state and logging errors.

**Test Scenarios:**

1. **Increment without starting**
   - `new Meter() → inc()` → logs INCONSISTENT_INCREMENT
   - `new Meter() → incBy(5)` → logs INCONSISTENT_INCREMENT
   - `new Meter() → incTo(10)` → logs INCONSISTENT_INCREMENT
   - Verify currentIteration unchanged

2. **Progress without starting**
   - `new Meter() → progress()` → logs INCONSISTENT_PROGRESS
   - Verify state unchanged

3. **Set path before starting**
   - `new Meter() → path("some_path")` → logs ILLEGAL
   - Verify okPath not defined

4. **Start twice**
   - `new Meter() → start() → start()` → second start ignored, logs ILLEGAL
   - Verify startTime unchanged

5. **Thread-local inconsistency**
   - Create Meter m1, create Meter m2, call `m1.ok()` when m2 is current
   - Logs INCONSISTENT_* for wrong thread-local context

---

### **Group 11: State-Preserving - Invalid Arguments (❌ Tier 4b)**

**Purpose:** Validate that Meter rejects calls with invalid arguments by preserving current state and logging errors.

**Test Scenarios:**

1. **Invalid values before starting**
   - `iterations(0)`, `iterations(-1)` → logs ILLEGAL
   - `limitMilliseconds(0)`, `limitMilliseconds(-1)` → logs ILLEGAL
   - `m(null)` when message required → logs ILLEGAL
   - `m("format", ...)` with null/invalid format → logs ILLEGAL

2. **Invalid values while started**
   - `incBy(0)`, `incBy(-1)` → logs ILLEGAL
   - `incTo(n)` where n <= currentIteration → logs ILLEGAL
   - `ok(null)` if path required → logs ILLEGAL
   - `reject(null)`, `fail(null)` → logs ILLEGAL
   - `path(null)` → logs ILLEGAL

3. **Invalid values on terminated meter**
   - `ok() → ok()`, `ok() → reject()`, `ok() → fail()` → logs INCONSISTENT_*
   - `ok() → inc()`, `ok() → progress()` → logs INCONSISTENT_*

4. **Invalid constructor arguments**
   - `new Meter(null)` → throws exception (@NonNull validation)

---

### **Group 12: Immutability Validation on Terminal States (State-Preserving)**

**Purpose:** Validate that operations on terminal states do not alter core outcome attributes, preserving the first-termination-wins guarantee.

**Test Scenarios:**

1. **On OK state**
   - `ok() → path("new_path")` → ignored
   - `ok() → iterations(100)` → ignored
   - `ok() → inc()` → ignored
   - Verify original outcome path preserved

2. **On Rejected state**
   - `reject() → path("new_path")` → ignored
   - `reject() → iterations(100)` → ignored
   - `reject() → inc()` → ignored
   - Verify original rejectPath preserved

3. **On Failed state**
   - `fail() → path("new_path")` → ignored
   - `fail() → iterations(100)` → ignored
   - `fail() → inc()` → ignored
   - Verify original failPath preserved

---

### **Group 13: Thread-Local Stack Management (Lifecycle Complete)**

**Purpose:** Validate correct behavior of thread-local Meter stack for single and nested Meters.

**Test Scenarios:**

1. **Single Meter (no nesting)**
   - `Meter m → start() → getCurrentInstance() returns m`
   - `m → ok() → getCurrentInstance() returns "unknown"`
   - Verify stack cleaned after test

2. **Nested Meters (parent-child)**
   - `Meter m1 → start() → Meter m2 → start() → m2.ok() → m1.ok()`
   - Verify m1 becomes current again after m2 terminates
   - Verify parent-child relationship maintained
   - Verify stack cleaned after test

3. **@ValidateCleanMeter validation**
   - Verify each test leaves stack clean
   - Test fails if non-unknown Meter left on stack
   - Provides descriptive error message for abandoned Meters

---

### **Group 14: Complex Combined Scenarios (All Tiers)**

**Purpose:** Validate realistic, complex flows that combine multiple aspects of Meter behavior.

**Test Scenarios:**

1. **Operation with progress and context**
   - `start() → m("starting") → iterations(100) → [loop: inc() → progress() throttled] → m("complete") → ok("done")`
   - Verify context, progress, and iterations captured

2. **Slow operation with timeLimit**
   - `start() → limitMilliseconds(100) → [operation >100ms] → ok()`
   - Verify `isSlow() == true`
   - Verify log changes from INFO to WARN

3. **Sub-operations**
   - `start() → sub("subtask1") → progress() → sub("subtask2") → ok()`
   - Verify sub-operations generate separate entries
   - Verify parent-child relationships

4. **Error recovery with state-correcting**
   - `start() → [business error] → ok() [called incorrectly] → verify terminates despite error`
   - Simulates real scenario where business code calls ok() out of order

---

## Test Coverage Summary

| Group | Focus | Tiers | Logs | Purpose |
|-------|-------|-------|------|---------|
| **1** | Initialization | ✅ Base | None | Foundation for all tests |
| **2** | Pre-Start Config | ☑️ Tier 2 | None | Attribute setup before start |
| **3** | Pre-Start Invalid | ❌ Tier 4 | ILLEGAL/INCONSISTENT_* | Invalid operations before start |
| **4** | Post-Start Config | ☑️ Tier 2 | None | Attribute updates during execution |
| **5** | Post-Stop Config (OK) | ❌ Tier 4 | ILLEGAL/INCONSISTENT_* | Invalid operations on OK state |
| **6** | Post-Stop Config (Rejected) | ❌ Tier 4 | ILLEGAL/INCONSISTENT_* | Invalid operations on Rejected state |
| **7** | Post-Stop Config (Failed) | ❌ Tier 4 | ILLEGAL/INCONSISTENT_* | Invalid operations on Failed state |
| **8** | Happy Path | ✅ Tier 1 | Normal | Valid expected flows |
| **9** | State-Correcting | ⚠️ Tier 3 | INCONSISTENT_* | Violations that self-correct |
| **10** | Bad Preconditions | ❌ Tier 4 | ILLEGAL/INCONSISTENT_* | Invalid call sequences |
| **11** | Bad Arguments | ❌ Tier 4 | ILLEGAL | Invalid argument values |
| **12** | Terminal Immutability | ❌ Tier 4 | None | Preserve terminal state |
| **13** | Thread-Local Stack | Mixed | Varies | Nesting & cleanup |
| **14** | Complex Scenarios | All | Varies | Realistic workflows |

---

## Implementation Guidelines

### Test Isolation with Group 1

Each test in Groups 2-10 should:
1. **Assume** Group 0 guarantees have been validated
2. **Trust** that `new Meter(logger).start()` works correctly
3. **Skip** redundant initialization checks
4. **Focus** exclusively on the group's primary concern

### Assertion Strategy

- **Group 1-2**: All assertions pass without logs (valid operations)
- **Group 3**: Assert state change + INCONSISTENT_* log (pre-start invalid)
- **Group 4**: All assertions pass without logs (post-start valid)
- **Group 5-7**: Assert state unchanged + ILLEGAL/INCONSISTENT_* log (post-stop invalid)
- **Group 8**: Assert terminal attributes preserved + no state change (happy path)
- **Group 9**: Assert state changes + error logs (state-correcting)
- **Group 10-11**: Assert state unchanged + ILLEGAL/INCONSISTENT_* log (invalid flow)
- **Group 12**: Assert thread-local references correct + stack cleaned
- **Group 13**: Assert combined behavior matches specification

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
