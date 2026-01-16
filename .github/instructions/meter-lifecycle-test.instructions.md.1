---
applyTo: "*/**/meter/MeterLifeCycleTest.java
---



# MeterLifeCycleTest Instructions

# General instructions

- **Special annotations:**
    - Use `@ValidateCleanMeter(expectDirtyStack = true)` for tests that call `start()` without a corresponding termination (`ok()`, `reject()`, `fail()`, or implicit close()). This ensures that the meter is properly cleaned up after the test.

- **Meter State validateion:**
    - Focus on these attributes: `startTime`, `stopTime`, `okPath`, `rejectPath`, `failPath`, `failMessage`, `currentIteration`, `expectedIterations`, `timeLimitMilliseconds`, `lastCurrentTime`, `createTime`.
    - Prefer `assertMeterState()` to validate these attributes.
    - Only use JUnit asserts to validate conditions that cannot be checked with `assertMeterState()`.
    - **When actual Meter behavior diverges from expected behavior:**
      - Tests must validate the **actual observed behavior** (not the ideal expected behavior)
      - Add a comment immediately before the assertion explaining the divergence: `// Will be fixed in future: <explanation>`
      - The explanation should describe what the correct behavior should be and how the current behavior differs
      - Example: If Meter currently modifies path attributes on invalid operations (when it shouldn't), the test should assert the modified value and include the comment
      - This documents known issues while ensuring tests reflect reality and pass consistently
      - Example pattern:
        ```java
        // Then: logs ILLEGAL, okPath remains unset
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        assertMeterState(meter, true, true, "second_path", null, null, null, 0, 0, 0);
        ```
    - **For attributes NOT covered by `assertMeterState()` (e.g., `description`, `context`):**
      - Use `assertEquals()` to validate `description` when it's set via `m()` method: `assertEquals("expected description", meter.getDescription())`
      - Use `assertEquals()` to validate individual context entries: `assertEquals("expectedValue", meter.getContext().get("key"))`
      - These validations should appear in the same `// Then:` block as `assertMeterState()` when validating meter state
    - Validate initial state after `new Meter()` using `assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0)`.
    - Validate state state transitions using `assertMeterState()`.
    - Validate analysis methods (`isSlow()`, `isStarted()`, `isStopped()`) when relevant to the test scenario.

    - On Group 2+ tests **DO NOT validate meter state after `new Meter()` and `meter.start()` in isolation.**   Assume these transitions are correct (validated in Group 1).

    - **EXCEPTION: When meter is configured before start(), MUST validate state after start():**
      - If there are configuration calls between `new Meter()` and `meter.start()` (e.g., `iterations()`, `limitMilliseconds()`, `path()`), **MUST validate meter state after `start()`**
      - This validation ensures the configuration was applied correctly and serves a pedagogical purpose to document the expected initial state
      - Example: If test calls `meter.iterations(15)` before `start()`, validate state after `start()` to confirm `expectedIterations = 15`
      - This makes the test self-documenting and clearly shows the customized initial state

    - **PEDAGOGICAL VALIDATION: When test begins with a meter in stopped state:**
      - **ALWAYS validate meter state immediately after creating a stopped meter** using `assertMeterState()`.
      - This validation serves a pedagogical purpose: documents the expected initial stopped state before testing subsequent operations.
      - Add a `// Then:` comment explaining this is a pedagogical validation of the stopped state.
      - **ALWAYS validate meter state after operations** to confirm expected state changes (or that state remained unchanged for invalid operations).
      - This dual validation (before and after) makes tests self-documenting and reinforces the operation's effect on meter state.
      - Applies to any test where `// Given:` establishes a stopped meter, regardless of how it was stopped (ok/reject/fail) or whether it has configured attributes (iterations, timeLimit, context, etc.).
      - **Pattern:**
        ```java
        // Given: a meter that has been stopped with ok("completion_path")
        final Meter meter = new Meter(logger).start().ok("completion_path");
        // Then: validate meter is in stopped state with okPath set (pedagogical validation)
        assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);
        
        // When: m(null) is called after stop
        meter.m(null);
        
        // Then: description unchanged (null), meter state unchanged
        assertNull(meter.getDescription());
        assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);
        
        // Then: logs ILLEGAL
        AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
        ```

    - **DO validate meter state after intermediate operations when they are the focus of the test scenario**, even if this creates some redundancy with other tests. These intermediate validations serve a **pedagogical purpose** to:
      - Document the expected behavior at each step
      - Make the test self-explanatory and easier to understand
      - Reinforce that the operation was applied correctly before moving to the next step
      - Provide immediate feedback if an intermediate operation fails
    - **When to add intermediate validations:**
      - When testing attribute modifications (e.g., `path()`, `inc()`, `iterations()`, `limitMilliseconds()`)
      - When testing sequences where the intermediate state is relevant to understanding the flow
      - When the test name explicitly mentions the intermediate operation (e.g., "should override path when path() called multiple times")

    - **When to skip intermediate validations:**
      - When the test focuses only on the final outcome
      - When intermediate operations are just setup for the real test scenario
      - After `new Meter()` and `start()` when no configuration calls occurred between them (these are always assumed correct)

    - **Pedagogical validations in multi-step iteration/progress tests:**
      - **After iteration batches (before `progress()`):** Add `assertMeterState()` to validate `currentIteration` reflects the cumulative `inc()` calls
      - **After `progress()` calls:** Add `assertMeterState()` to document that meter state remains Started (not stopped) and iterations are preserved
      - **Purpose:** These intermediate validations make the test self-documenting, showing the expected state evolution through the operation lifecycle
      - **Pattern:**
        ```java
        // First batch: 5 iterations
        for (int i = 0; i < 5; i++) { meter.inc(); }
        // Then: validate currentIteration after first batch (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 5, 15, 0);
        
        meter.progress();
        // Then: validate state after progress() - still running (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 5, 15, 0);
        ```
      - **When to apply:** In tests with multiple iteration batches and `progress()` calls (e.g., "mixed iterations and progress" tests)
      - **Benefit:** Makes it explicit that `progress()` does NOT change `currentIteration` and does NOT terminate the meter

- **For try-with-resources block validations:**
    - Use an external variable to hold the meter reference at the start of the try block.
    - Use `assertMeterState()` to validate the meter state at the beginning of the try block (initial state).
    - Use `assertMeterState()` to validate the meter state at the end of the try block (before the try block exits).
    - Use `assertMeterState()` to validate the meter state at the end of the try block (after the try block exits, using the external variable).
    - Validate final state after try-with-resources implicit close().
    - Verify that auto-fail via implicit close() occurred with correct logs.
  
- **For time-based validations:** (`isSlow`, progress throttling)
    - Create Meter with TestTimeSource: `new Meter(logger).withTimeSource(timeSource)` where `timeSource = new TestTimeSource(TestTimeSource.DAY1)`.
    - Advance time during test execution using `timeSource.advanceMiliseconds(N)` to simulate elapsed time between operations.
    - Configure `MeterConfig.progressPeriodMilliseconds` before meter creation:
      - Set to `0` to disable throttling (all progress() calls will log)
      - Set to positive value (e.g., `50`) to enable throttling (progress() only logs if time elapsed >= threshold)
    - Validate `isSlow()` when testing time limits:
      - `assertTrue(meter.isSlow())` when execution time > time limit
      - `assertFalse(meter.isSlow())` when execution time < time limit

- **Log events validations:**
    - Validate log events, checking Marker and Level. Only validate message and parameters when relevant to the test scenario.
    - **Validate message content when it contains relevant information:**
      - When logs include meter attributes (description, context), validate they appear in the log events
      - Example for description in logs: `AssertLogger.assertEvent(logger, 2, Level.TRACE, "operation description")`
      - Example for context in logs: `AssertLogger.assertEvent(logger, 2, Level.TRACE, "user", "alice")` and `AssertLogger.assertEvent(logger, 2, Level.TRACE, "action", "import")`
      - This ensures that configured attributes are properly logged in DATA_OK, DATA_REJECT, and DATA_FAIL events
    - Validate total log event count with `AssertLogger.assertEventCount()`.
    - Validate log count as zero (`AssertLogger.assertEventCount(logger, 0)`) when no events are expected.
    
    - **MANDATORY log validation block structure (applies to ALL tests):**
      - Log validation must ALWAYS be in a separate block from state validations
      - ALWAYS add a blank line before the log validation block to separate it from state validations
      - ALWAYS start the log validation block with a `// Then:` comment explaining what is being validated
      - ALWAYS include at least `AssertLogger.assertEventCount(logger, N)` to validate total event count
      - This structure applies even when there are no specific events to validate (only assertEventCount)
    
    - **Log validation block patterns:**
      - **Simple tests (no error events):** State validation + blank line + `// Then: logs only start events` + `AssertLogger.assertEventCount(logger, 2)`
      - **Error tests (with ILLEGAL, INCONSISTENT_*):** State validation + blank line + `// Then: logs start + ILLEGAL` + specific event assertions + `AssertLogger.assertEventCount(logger, N)`
      - **Progress tests:** State validation + blank line + `// Then: logs start + progress + DATA_PROGRESS` + specific event assertions + `AssertLogger.assertEventCount(logger, N)`

- **Test structure with comments:**
    - Use separate `// Then:` comment blocks for different types of validations (e.g., state validation, log validation, getCurrentInstance validation).
    - Each `// Then:` comment should clearly describe what is being validated in that block.
    - Each `// Given:` and `// When:` comment should clearly describe the setup and action being performed.
    - **Given comment patterns:**
      - When `start()` is part of the setup (no meter configuration between constructor and `start()`), use: `// Given: a new, started Meter`
      - When meter is configured before `start()`, use: `// Given: a new Meter` followed by configuration, then start
      - When additional context is needed (e.g., with iterations, time limits), describe the full setup: `// Given: a meter with iterations and time limit configured`
    - **For tests with sequence of operations (stepwise testing):**
     - Use multiple `// When:` and `// Then:` blocks to document intermediate states and progressions:
     - Don't add blank lines between `// When:` and `// Then:` blocks to maintain logical flow if they are the same sequence.


**Example of "new, started Meter" pattern (start is part of setup):**
```java
  // Given: a new, started Meter
  final Meter meter = new Meter(logger);
  meter.start();
  
  // When: first path is set
  meter.path("first");
  // Then: validate path was applied (pedagogical validation)
  assertMeterState(meter, true, false, "first", null, null, null, 0, 0, 0);
  
  // Then: logs only start events
  AssertLogger.assertEventCount(logger, 2);
  
  // When: meter is terminated
  meter.ok();
  // Then: validate final state (mandatory validation)
  assertMeterState(meter, true, true, "first", null, null, null, 0, 0, 0);
  
  // Then: logs start + ok + DATA_OK
  AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
  AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
  AssertLogger.assertEventCount(logger, 4);
```

**Example of configured meter before start (start is NOT part of setup):**
```java
  // Given: a meter with iterations and time limit configured
  final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);
  final Meter meter = new Meter(logger).withTimeSource(timeSource);
  meter.iterations(15);
  meter.limitMilliseconds(100);
  
  // When: meter is started and executes
  meter.start();
  // ... test execution
```

**Example of sequence of operations and intermediate validations:**
```java
  // Given: a new, started Meter
  final Meter meter = new Meter(logger);
  meter.start();
  
  // When: first path is set
  meter.path("first");
  // Then: validate path was applied (pedagogical validation)
  assertMeterState(meter, true, false, "first", null, null, null, 0, 0, 0);
  
  // Then: logs only start events
  AssertLogger.assertEventCount(logger, 2);
  
  // When: path is overridden
  meter.path("second");
  // Then: validate path override (pedagogical validation)
  assertMeterState(meter, true, false, "second", null, null, null, 0, 0, 0);
  
  // Then: logs only start events (no additional logs from path override)
  AssertLogger.assertEventCount(logger, 2);
  
  // When: meter is terminated
  meter.ok();
  // Then: validate final state (mandatory validation)
  assertMeterState(meter, true, true, "second", null, null, null, 0, 0, 0);
  
  // Then: logs start + ok + DATA_OK
  AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
  AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
  AssertLogger.assertEventCount(logger, 4);
  ```

**Example contrasting simple test vs error test:**
```java
// ✅ SIMPLE TEST (no error events) - Always has blank line + comment before assertEventCount
@Test
@DisplayName("should set description after start()")
void shouldSetDescriptionAfterStart() {
    // Given: a new, started Meter
    final Meter meter = new Meter(logger).start();
    
    // When: m("step 1") is called after start()
    meter.m("step 1");
    
    // Then: description attribute is stored correctly and meter remains in Started state
    assertEquals("step 1", meter.getDescription());
    assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
    
    // Then: logs only start events  ← MANDATORY: blank line + comment even for simple assertEventCount
    AssertLogger.assertEventCount(logger, 2);
}

// ✅ ERROR TEST (with ILLEGAL event) - Same structure, but with specific event validations
@Test
@DisplayName("should preserve valid message when null value attempted after start()")
void shouldPreserveValidMessageWhenNullValueAttemptedAfterStart() {
    // Given: a new, started Meter
    final Meter meter = new Meter(logger).start();
    
    // When: m("valid") is called, then m(null) is attempted
    meter.m("valid");
    meter.m(null);
    
    // Then: null rejected, "valid" is preserved
    assertEquals("valid", meter.getDescription());
    
    // Then: logs start + ILLEGAL  ← MANDATORY: blank line + comment + specific events + assertEventCount
    AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
    AssertLogger.assertEventCount(logger, 3);
}
```

# Group 1 specific instructions

This instructions override General instructions for Group 1 tests.

## Group 1: Meter Initialization (Base Guarantee)

Tests validate that Meter is created and started correctly. This group is the foundation for all subsequent tests, ensuring initialization reliability.

- **Validate Meter attributes:**
    - When a meter is created with `operationName`, validate that `getOperation()` returns the correct value.
    - When a meter is created with `parent`, validate that `getParent()` returns the correct value.
    - **All tests in Group 1 must validate meter state after `new Meter()` and `meter.start()`** using `assertMeterState()`.

- **Validate getCurrentInstance():**
    - **All tests in Group 1 must validate `getCurrentInstance()`** before any operations.
    - Before `start()`: `Meter.getCurrentInstance()` returns the unknown meter (category = `Meter.UNKNOWN_LOGGER_NAME`).
    - After `start()`: `Meter.getCurrentInstance()` returns the started meter instance.


# Group 2 specific instructions


Tests validate normal lifecycle transitions without errors. These are the expected, successful paths through the state machine with various path type variations.

- **Meter State validateion:**
    - DO NOT validate meter state after `new Meter()` and `meter.start()`. Assume these are correct.
    - Explore path values with different types: String, Enum, Throwable, and Object paths.
    - Explore setting path and then overriding the path with another path type before termination.
    - Explore setting path and then overriding the path with termination (ok, reject, fail). Also validate failMessage when applicable.
    - Explore multiple iterations with correct expected iterations.
    - Explore try-with-resources implicit close() with successful completion.
    - Explore progress throttling with zero and nonzero `MeterConfig.progressPeriodMilliseconds` and mocked execution time to test progress log throttling (will produce logs only when time elapsed >= progressPeriodMilliseconds).
    - Explore progress throttling with zero and nonzero elapsed time increments to test progress log throttling (will produce logs only when time increments happended since last progress call).
   - Explore progress throttling with zero and nonzero iteration increments to test progress log throttling (will produce logs only when increment happended since last progress call).
    - Explore meter execution with time limits and mocked execution time to test slow meter detection.

- **Log events validations:**
    - DO NOT validate events at indices 0 and 1 (from `start()`). Assume these are correct as tested in Group 1.

- ** Chained Meter calls**
   - As start() method is not expected to be validated with `assertMeterState()`, and since relevant validation  


# Group 6 specific instructions

This instructions override General instructions for Group 6 tests.

## Group 6: Post-Stop Invalid Operations (Tier 4)

Tests validate that operations after meter termination (OK, Reject, or Fail states) are properly rejected.

- **Initial stopped state validation:**
    - Follow the general "PEDAGOGICAL VALIDATION: When test begins with a meter in stopped state" rule.
    - Use chained calls to reach stopped state: `meter.start().ok()`, `meter.start().ok("path")`, `meter.start().reject("cause")`, `meter.start().fail("reason")`
    - With configuration: `meter.start().inc().ok()`, `meter.start().ctx("key", "val").ok()`, `meter.start().limitMilliseconds(100).ok()`, `meter.start().iterations(50).ok()`

- **Given comment patterns for stopped meters:**
    - Basic stopped meter: `// Given: a meter that has been stopped with ok()`
    - With path: `// Given: a meter that has been stopped with ok("completion_path")`
    - With reject: `// Given: a meter that has been stopped with reject("business_error")`
    - With fail: `// Given: a meter that has been stopped with fail("technical_error")`
    - With configuration: `// Given: a meter with [attribute], stopped with ok()`

**Example following general pedagogical validation rule:**
```java
  @Test
  @DisplayName("should reject m() after ok() - logs ILLEGAL")
  void shouldRejectMAfterOk() {
      // Given: a meter that has been stopped with ok()
      final Meter meter = new Meter(logger).start().ok();
      // Then: validate meter is in stopped state (pedagogical validation)
      assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
      
      // When: m() is called after stop
      meter.m("step 1");
      
      // Then: description unchanged (null), meter state unchanged
      assertNull(meter.getDescription());
      assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
      
      // Then: logs ILLEGAL
      AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
      AssertLogger.assertEventCount(logger, 5);
  }
```

# Group 3 specific instructions

This instructions override General instructions for Group 3 tests.

## Group 3: Try-With-Resources (Tier 1 + Tier 3)

Tests validate Meter behavior within try-with-resources blocks, including both normal flows (Tier 1: with start()) and state-correcting flows (Tier 3: without start()).

- **Meter State validation:**
    - When using expected new Meter() -> start() flow (Tier 1), DO NOT validate meter state after `new Meter()` and `meter.start()`. Assume these are correct.
    - **Validation before try block:** Only validate meter state before entering the try block if there are configuration calls before the try block (e.g., `iterations()`, `limitMilliseconds()`). If meter is created directly in the try-with-resources statement, no pre-try validation is needed.
    - **Intermediate validations within try block:** Apply the same pedagogical validation rules as non-try-with-resources tests:
      - Validate state after intermediate operations that are the focus of the test (e.g., after `path()` call)
      - Validate state after termination calls (ok(), reject(), fail()) when called within the try block
      - These intermediate validations serve the same pedagogical purpose: documenting expected behavior at each step
    - **Validation after try block:** Always validate final state after try-with-resources implicit close() using the external meter reference.

- **Log events validations:**
    - When using expected new Meter() -> start() flow (Tier 1), DO NOT validate events at indices 0 and 1 (from `start()`). Assume these are correct as tested in Group 1.
    - When testing state-correcting flows (Tier 3: without start()), validate all log events starting from index 0 (INCONSISTENT_* markers).
