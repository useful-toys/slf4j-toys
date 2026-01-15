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
    - Validate initial state after `new Meter()` using `assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0)`.
    - Validate state state transitions using `assertMeterState()`.
    - Validate analysis methods (`isSlow()`, `isStarted()`, `isStopped()`) when relevant to the test scenario.

    - On Group 2+ tests **DO NOT validate meter state after `new Meter()` and `meter.start()` in isolation.**   Assume these transitions are correct (validated in Group 1).

    - **EXCEPTION: When meter is configured before start(), MUST validate state after start():**
      - If there are configuration calls between `new Meter()` and `meter.start()` (e.g., `iterations()`, `limitMilliseconds()`, `path()`), **MUST validate meter state after `start()`**
      - This validation ensures the configuration was applied correctly and serves a pedagogical purpose to document the expected initial state
      - Example: If test calls `meter.iterations(15)` before `start()`, validate state after `start()` to confirm `expectedIterations = 15`
      - This makes the test self-documenting and clearly shows the customized initial state

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
    - Validate total log event count with `AssertLogger.assertEventCount()`.
    - Validate log count as zero (`AssertLogger.assertEventCount(logger, 0)`) when no events are expected.
    - Log validation (events and total count) must be in a separate block.
    - Start the log validation block with a `// Then:` comment explaining what is being validated.
    - **Always add a blank line before the log validation block** to separate it from state validations.

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
  
  // When: meter is terminated
  meter.ok();
  // Then: validate final state (mandatory validation)
  assertMeterState(meter, true, true, "first", null, null, null, 0, 0, 0);
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
  
  // When: path is overridden
  meter.path("second");
  // Then: validate path override (pedagogical validation)
  assertMeterState(meter, true, false, "second", null, null, null, 0, 0, 0);
  
  // When: meter is terminated
  meter.ok();
  // Then: validate final state (mandatory validation)
  assertMeterState(meter, true, true, "second", null, null, null, 0, 0, 0);
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


# Group 3 specific instructions

- **Meter State validateion:**
    - When using expected new Meter() -> start() flow, DO NOT validate meter state after `new Meter()` and `meter.start()`. Assume these are correct.

- **Log events validations:**
    - When using expected new Meter() -> start() flow, DO NOT validate events at indices 0 and 1 (from `start()`). Assume these are correct as tested in Group 1.

- **For try-with-resources block validations:**
    - DO NOT validate meter state at the beginning of the try block (initial state). Assume this is correct as tested in Group 1.
