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

- **For try-with-resources block validations:**
    - Use an external variable to hold the meter reference at the start of the try block.
    - After the try block exits, validate the final meter state using the external variable.
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
    - Total log event count with `AssertLogger.assertEventCount()`.
    - Validate log count as zero (`AssertLogger.assertEventCount(logger, 0)`) when no events are expected.
    - Log validation (events and total count) must be in a separate block.
    - Start the log validation block with a `// Then:` comment explaining what is being validated.

- **Test structure with comments:**
    - Use separate `// Then:` comment blocks for different types of validations (e.g., state validation, log validation, getCurrentInstance validation).
    - Each `// Then:` comment should clearly describe what is being validated in that block.
    - Each `// Given:` and `// When:` comment should clearly describe the setup and action being performed.

# Group 1 specific instructions

This instructions override General instructions for Group 1 tests.

## Group 1: Meter Initialization (Base Guarantee)

Tests validate that Meter is created and started correctly. This group is the foundation for all subsequent tests, ensuring initialization reliability.

- **Validate Meter attributes:**
    - When a meter is created with `operationName`, validate that `getOperation()` returns the correct value.
    - When a meter is created with `parent`, validate that `getParent()` returns the correct value.

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


