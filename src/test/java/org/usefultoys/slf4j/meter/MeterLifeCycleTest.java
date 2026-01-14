/*
 * Copyright 2026 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.usefultoys.slf4j.meter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent.Level;
import org.usefultoys.slf4j.internal.TestTimeSource;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.slf4jtestmock.WithMockLoggerDebug;
import org.usefultoys.test.ResetMeterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.ValidateCleanMeter;
import org.usefultoys.test.WithLocale;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link Meter} lifecycle.
 * <p>
 * Tests validate that Meter correctly transitions between states and updates its attributes
 * according to the lifecycle defined in the state diagram.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Normal Success Flow:</b> Verifies attributes and logs for start() and ok()</li>
 *   <li><b>Success with Path:</b> Verifies attributes and logs when a path is provided to ok()</li>
 *   <li><b>Rejection Flow:</b> Verifies attributes and logs for reject()</li>
 *   <li><b>Failure Flow:</b> Verifies attributes and logs for fail()</li>
 *   <li><b>Try-With-Resources:</b> Verifies automatic failure on close()</li>
 *   <li><b>Iteration Tracking:</b> Verifies currentIteration and expectedIterations</li>
 * </ul>
 *
 * @author Co-authored-by: GitHub Copilot using Gemini 3 Flash (Preview)
 * @author Co-authored-by: GitHub Copilot using GPT-5.2
 */
@ValidateCharset
@ResetMeterConfig
@WithLocale("en")
@WithMockLogger
@ValidateCleanMeter
@WithMockLoggerDebug
@SuppressWarnings({"AssignmentToStaticFieldFromInstanceMethod", "IOResourceOpenedButNotSafelyClosed", "TestMethodWithoutAssertion"})
class MeterLifeCycleTest {

    @SuppressWarnings("NonConstantLogger")
    @Slf4jMock
    Logger logger;

    /**
     * Test enum for validating enum path handling in Meter.
     */
    enum TestEnum {
        VALUE1, VALUE2
    }

    /**
     * Test object for validating object path handling in Meter.
     */
    static class TestObject {
        @Override
        public String toString() {
            return "testObjectString";
        }
    }

    /**
     * Verifies the state of the given {@code Meter} object against the provided expected values and conditions.
     *
     * @param meter                 the {@code Meter} object to validate
     * @param started               {@code true} if the meter is expected to be started, otherwise {@code false}
     * @param stopped               {@code true} if the meter is expected to be stopped, otherwise {@code false}
     * @param okPath                the expected value of the "okPath" property, or {@code null} if it is expected to be null
     * @param rejectPath            the expected value of the "rejectPath" property, or {@code null} if it is expected to be null
     * @param failPath              the expected value of the "failPath" property, or {@code null} if it is expected to be null
     * @param failMessage           the expected value of the "*/
    static void assertMeterState(final Meter meter, final boolean started, final boolean stopped, final String okPath, final String rejectPath, final String failPath, final String failMessage, final long currentIteration, final long expectedIterations, final long timeLimitMilliseconds) {
        if (started) {
            assertTrue(meter.getStartTime() > 0, "startTime should be > 0");
        } else {
            assertEquals(0, meter.getStartTime(), "startTime should be 0");
        }

        if (stopped) {
            assertTrue(meter.getStopTime() > 0, "stopTime should be > 0");
            assertTrue(meter.getStopTime() >= meter.getStartTime(), "stopTime should be >= startTime");
        } else {
            assertEquals(0, meter.getStopTime(), "stopTime should be 0");
        }

        if (okPath == null) {
            assertNull(meter.getOkPath(), "okPath should be null");
        } else {
            assertEquals(okPath, meter.getOkPath(), "okPath should match expected value: " + okPath);
        }

        if (rejectPath == null) {
            assertNull(meter.getRejectPath(), "rejectPath should be null");
        } else {
            assertEquals(rejectPath, meter.getRejectPath(), "rejectPath should match expected value: " + rejectPath);
        }

        if (failPath == null) {
            assertNull(meter.getFailPath(), "failPath should be null");
        } else {
            assertEquals(failPath, meter.getFailPath(), "failPath should match expected value: " + failPath);
        }

        if (failMessage == null) {
            assertNull(meter.getFailMessage(), "failMessage should be null");
        } else {
            assertEquals(failMessage, meter.getFailMessage(), "failMessage should match expected value: " + failMessage);
        }

        assertEquals(currentIteration, meter.getCurrentIteration(), "currentIteration should match expected value: " + currentIteration);
        assertEquals(expectedIterations, meter.getExpectedIterations(), "expectedIterations should match expected value: " + expectedIterations);
        assertEquals(timeLimitMilliseconds * 1000 * 1000, meter.getTimeLimit(), "timeLimit should match expected value: " + timeLimitMilliseconds + "ms");

        assertTrue(meter.getCreateTime() > 0, "createTime should be > 0");
        if (stopped) {
            assertTrue(meter.getLastCurrentTime() >= meter.getStopTime(), "lastCurrentTime should be >= stopTime");
        } else if (started) {
            assertTrue(meter.getLastCurrentTime() >= meter.getStartTime(), "lastCurrentTime should be >= startTime");
        } else {
            assertEquals(meter.getCreateTime(), meter.getLastCurrentTime(), "lastCurrentTime should be equal to createTime");
        }
    }

    @Nested
    @DisplayName("Group 1: Meter Initialization (Base Guarantee)")
    class MeterInitialization {
        @Test
        @DisplayName("should create meter with logger - initial state")
        void shouldCreateMeterWithLoggerInitialState() {
            // Given: a new Meter with logger only
            final Meter meter = new Meter(logger);

            // Then: meter has expected initial state (startTime = 0, stopTime = 0)
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            
            // Then: before start(), getCurrentInstance() returns unknown meter
            final Meter currentBeforeStart = Meter.getCurrentInstance();
            assertEquals(Meter.UNKNOWN_LOGGER_NAME, currentBeforeStart.getCategory(),
                "before start(), getCurrentInstance() should return unknown meter");
            
            // Then: no logs yet
            AssertLogger.assertEventCount(logger, 0);
        }

        @Test
        @DisplayName("should create meter with logger + operationName")
        void shouldCreateMeterWithOperationName() {
            // Given: a new Meter with logger and operationName
            final String operationName = "testOperation";
            final Meter meter = new Meter(logger, operationName);

            // Then: meter has expected initial state
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            
            // Then: operationName is captured correctly
            assertEquals(operationName, meter.getOperation(), "operation name should match");
            
            // Then: before start(), getCurrentInstance() returns unknown meter
            final Meter currentBeforeStart = Meter.getCurrentInstance();
            assertEquals(Meter.UNKNOWN_LOGGER_NAME, currentBeforeStart.getCategory(),
                "before start(), getCurrentInstance() should return unknown meter");
        }

        @Test
        @DisplayName("should create meter with logger + operationName + parent")
        void shouldCreateMeterWithParent() {
            // Given: a new Meter with logger, operationName, and parent
            final String operationName = "childOperation";
            final String parentId = "parent-meter-id";
            final Meter meter = new Meter(logger, operationName, parentId);

            // Then: meter has expected initial state
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            
            // Then: operationName and parent are captured correctly
            assertEquals(operationName, meter.getOperation(), "operation name should match");
            assertEquals(parentId, meter.getParent(), "parent should match");
            
            // Then: before start(), getCurrentInstance() returns unknown meter
            final Meter currentBeforeStart = Meter.getCurrentInstance();
            assertEquals(Meter.UNKNOWN_LOGGER_NAME, currentBeforeStart.getCategory(),
                "before start(), getCurrentInstance() should return unknown meter");
        }

        @Test
        @DisplayName("should start meter successfully")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldStartMeterSuccessfully() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);
            
            // Then: before start(), getCurrentInstance() returns unknown meter
            final Meter currentBeforeStart = Meter.getCurrentInstance();
            assertEquals(Meter.UNKNOWN_LOGGER_NAME, currentBeforeStart.getCategory(),
                "before start(), getCurrentInstance() should return unknown meter");

            // When: start() is called
            meter.start();

            // Then: Meter transitions from Created to Started
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
            
            // Then: after start(), meter becomes current in thread-local and is returned by getCurrentInstance()
            final Meter currentAfterStart = Meter.getCurrentInstance();
            assertEquals(meter, currentAfterStart, "after start(), meter should be current in thread-local");

            // Then: log messages recorded correctly with system status information
            AssertLogger.assertEvent(logger, 0, Level.DEBUG, Markers.MSG_START);
            AssertLogger.assertEvent(logger, 1, Level.TRACE, Markers.DATA_START);
        }

        @Test
        @DisplayName("should start meter in try-with-resources (start in block)")
        void shouldStartMeterInTryWithResourcesSequential() {
            // Given: Meter is created in try-with-resources
            try (final Meter m = new Meter(logger)) {
                // Then: meter has expected initial state before start
                assertMeterState(m, false, false, null, null, null, null, 0, 0, 0);
                
                // Then: before start(), getCurrentInstance() returns unknown meter
                final Meter currentBeforeStart = Meter.getCurrentInstance();
                assertEquals(Meter.UNKNOWN_LOGGER_NAME, currentBeforeStart.getCategory(),
                    "before start(), getCurrentInstance() should return unknown meter");
                
                // When: start() is called in the block
                m.start();
                
                // Then: meter is transitioned to executing state
                assertMeterState(m, true, false, null, null, null, null, 0, 0, 0);
                
                // Then: after start(), meter becomes current in thread-local
                final Meter currentAfterStart = Meter.getCurrentInstance();
                assertEquals(m, currentAfterStart, "after start(), meter should be current in thread-local");
            }

            // Then: start log messages recorded correctly
            AssertLogger.assertEvent(logger, 0, Level.DEBUG, Markers.MSG_START);
            AssertLogger.assertEvent(logger, 1, Level.TRACE, Markers.DATA_START);
        }

        @Test
        @DisplayName("should start meter with chained call in try-with-resources")
        void shouldStartMeterInTryWithResourcesChained() {
            // Given: Meter is created with chained start() in try-with-resources
            try (final Meter m = new Meter(logger).start()) {
                // Then: meter is in executing state (created and started in single expression)
                assertMeterState(m, true, false, null, null, null, null, 0, 0, 0);
                
                // Then: meter becomes current in thread-local after start()
                final Meter currentAfterStart = Meter.getCurrentInstance();
                assertEquals(m, currentAfterStart, "after start(), meter should be current in thread-local");
            }

            // Then: start log messages recorded correctly
            AssertLogger.assertEvent(logger, 0, Level.DEBUG, Markers.MSG_START);
            AssertLogger.assertEvent(logger, 1, Level.TRACE, Markers.DATA_START);
        }
    }

    @Nested
    @DisplayName("Group 2: Happy Path (✅ Tier 1)")
    class HappyPath {

        // ============================================================================
        // Created → Started → OK (simple)
        // ============================================================================

        @Test
        @DisplayName("should transition Created → Started → OK (simple)")
        void shouldTransitionCreatedToStartedToOkSimple() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: meter is started and completes successfully
            meter.start();
            meter.ok();

            // Then: meter should be in OK state
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            // Validate logs (skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        // ============================================================================
        // Created → Started → OK with custom path
        // ============================================================================

        @Test
        @DisplayName("should transition Created → Started → OK with String path via ok(String)")
        void shouldTransitionCreatedToStartedToOkWithStringPathViaOkString() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: meter is started and completes successfully with path
            meter.start();
            meter.ok("success_path");

            // Then: meter should be in OK state with okPath set
            assertMeterState(meter, true, true, "success_path", null, null, null, 0, 0, 0);
            // Validate logs (skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK, "success_path");
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should transition Created → Started → OK with String path via path() then ok()")
        void shouldTransitionCreatedToStartedToOkWithStringPathViaPathThenOk() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: meter is started, path is set, and completes successfully
            meter.start();
            meter.path("custom_path");
            meter.ok();

            // Then: meter should be in OK state with okPath set
            assertMeterState(meter, true, true, "custom_path", null, null, null, 0, 0, 0);
            // Validate logs (skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK, "custom_path");
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should transition Created → Started → OK with Enum path")
        void shouldTransitionCreatedToStartedToOkWithEnumPath() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: meter is started and completes successfully with Enum path
            meter.start();
            meter.ok(TestEnum.VALUE1);

            // Then: meter should be in OK state with okPath as Enum.toString()
            assertMeterState(meter, true, true, "VALUE1", null, null, null, 0, 0, 0);
            // Validate logs (skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK, "VALUE1");
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should transition Created → Started → OK with Throwable path")
        void shouldTransitionCreatedToStartedToOkWithThrowablePath() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);
            final Throwable throwable = new RuntimeException("test exception");

            // When: meter is started and completes successfully with Throwable path
            meter.start();
            meter.ok(throwable);

            // Then: meter should be in OK state with okPath as Throwable class name
            assertMeterState(meter, true, true, "RuntimeException", null, null, null, 0, 0, 0);
            // Validate logs (skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK, "RuntimeException");
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should transition Created → Started → OK with Object path")
        void shouldTransitionCreatedToStartedToOkWithObjectPath() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);
            final TestObject testObject = new TestObject();

            // When: meter is started and completes successfully with Object path
            meter.start();
            meter.ok(testObject);

            // Then: meter should be in OK state with okPath as Object.toString()
            assertMeterState(meter, true, true, "testObjectString", null, null, null, 0, 0, 0);
            // Validate logs (skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK, "testObjectString");
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should transition Created → Started → OK with ok() overriding path()")
        void shouldTransitionCreatedToStartedToOkWithOkOverridingPath() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: meter is started, path is set, then ok() with different path
            meter.start();
            meter.path("default_path");
            meter.ok("override_path");

            // Then: meter should be in OK state with okPath from ok() (overrides path())
            assertMeterState(meter, true, true, "override_path", null, null, null, 0, 0, 0);
            // Validate logs (skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK, "override_path");
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should transition Created → Started → OK with multiple path() calls (last wins)")
        void shouldTransitionCreatedToStartedToOkWithMultiplePathCalls() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: meter is started and path() is called multiple times
            meter.start();
            meter.path("first");
            meter.path("second");
            meter.path("third");
            meter.ok();

            // Then: meter should be in OK state with okPath from last path() call
            assertMeterState(meter, true, true, "third", null, null, null, 0, 0, 0);
            // Validate logs (skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK, "third");
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        // ============================================================================
        // Created → Started → Rejected
        // ============================================================================

        @Test
        @DisplayName("should transition Created → Started → Rejected with String cause")
        void shouldTransitionCreatedToStartedToRejectedWithStringCause() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: meter is started and rejected
            meter.start();
            meter.reject("business_error");

            // Then: meter should be in Rejected state
            assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);
            // Validate logs (skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }

        @Test
        @DisplayName("should transition Created → Started → Rejected with Enum cause")
        void shouldTransitionCreatedToStartedToRejectedWithEnumCause() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: meter is started and rejected with Enum
            meter.start();
            meter.reject(TestEnum.VALUE2);

            // Then: meter should be in Rejected state with rejectPath as Enum.toString()
            assertMeterState(meter, true, true, null, "VALUE2", null, null, 0, 0, 0);
            // Validate logs (skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT, "VALUE2");
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }

        @Test
        @DisplayName("should transition Created → Started → Rejected with Throwable cause")
        void shouldTransitionCreatedToStartedToRejectedWithThrowableCause() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);
            final Throwable throwable = new IllegalArgumentException("validation failed");

            // When: meter is started and rejected with Throwable
            meter.start();
            meter.reject(throwable);

            // Then: meter should be in Rejected state with rejectPath as Throwable class name
            assertMeterState(meter, true, true, null, "IllegalArgumentException", null, null, 0, 0, 0);
            // Validate logs (skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT, "IllegalArgumentException");
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }

        @Test
        @DisplayName("should transition Created → Started → Rejected with Object cause")
        void shouldTransitionCreatedToStartedToRejectedWithObjectCause() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);
            final TestObject testObject = new TestObject();

            // When: meter is started and rejected with Object
            meter.start();
            meter.reject(testObject);

            // Then: meter should be in Rejected state with rejectPath as Object.toString()
            assertMeterState(meter, true, true, null, "testObjectString", null, null, 0, 0, 0);
            // Validate logs (skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT, "testObjectString");
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }

        @Test
        @DisplayName("should transition Created → Started → Rejected after setting path() expectation")
        void shouldTransitionCreatedToStartedToRejectedAfterSettingPathExpectation() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: meter is started, path is set for expected ok, then rejected
            meter.start();
            meter.path("expected_ok_path");
            meter.reject("business_error");

            // Then: meter should be in Rejected state with rejectPath (okPath remains unset)
            assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);
            // Validate logs (skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT, "business_error");
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }

        // ============================================================================
        // Created → Started → Failed
        // ============================================================================

        @Test
        @DisplayName("should transition Created → Started → Failed with String cause")
        void shouldTransitionCreatedToStartedToFailedWithStringCause() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: meter is started and fails
            meter.start();
            meter.fail("technical_error");

            // Then: meter should be in Failed state
            assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);
            // Validate logs (skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }

        @Test
        @DisplayName("should transition Created → Started → Failed with Enum cause")
        void shouldTransitionCreatedToStartedToFailedWithEnumCause() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: meter is started and fails with Enum
            meter.start();
            meter.fail(TestEnum.VALUE1);

            // Then: meter should be in Failed state with failPath as Enum.toString()
            assertMeterState(meter, true, true, null, null, "VALUE1", null, 0, 0, 0);
            // Validate logs (skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL, "VALUE1");
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }

        @Test
        @DisplayName("should transition Created → Started → Failed with Throwable cause")
        void shouldTransitionCreatedToStartedToFailedWithThrowableCause() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);
            final Throwable throwable = new RuntimeException("system failure");

            // When: meter is started and fails with Throwable
            meter.start();
            meter.fail(throwable);

            // Then: meter should be in Failed state with failPath as class name and failMessage
            assertMeterState(meter, true, true, null, null, "java.lang.RuntimeException", "system failure", 0, 0, 0);
            // Validate logs (skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL, "java.lang.RuntimeException");
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }

        @Test
        @DisplayName("should transition Created → Started → Failed with Object cause")
        void shouldTransitionCreatedToStartedToFailedWithObjectCause() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);
            final TestObject testObject = new TestObject();

            // When: meter is started and fails with Object
            meter.start();
            meter.fail(testObject);

            // Then: meter should be in Failed state with failPath as Object.toString()
            assertMeterState(meter, true, true, null, null, "testObjectString", null, 0, 0, 0);
            // Validate logs (skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL, "testObjectString");
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }

        @Test
        @DisplayName("should transition Created → Started → Failed after setting path() expectation")
        void shouldTransitionCreatedToStartedToFailedAfterSettingPathExpectation() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: meter is started, path is set for expected ok, then fails
            meter.start();
            meter.path("expected_ok_path");
            meter.fail("critical_error");

            // Then: meter should be in Failed state with failPath (okPath remains unset)
            assertMeterState(meter, true, true, null, null, "critical_error", null, 0, 0, 0);
            // Validate logs (skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL, "critical_error");
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }

        // ============================================================================
        // Created → Started → OK with mixed iterations and progress
        // ============================================================================

        @Test
        @DisplayName("should transition Created → Started → OK with mixed iterations and progress")
        void shouldTransitionCreatedToStartedToOkWithMixedIterationsAndProgress() throws InterruptedException {
            // Given: a meter with expected iterations configured
            final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);
            final Meter meter = new Meter(logger).withTimeSource(timeSource);
            meter.iterations(15);
            MeterConfig.progressPeriodMilliseconds = 0; // Disable throttling for test

            // When: meter is started, incremented with progress calls, and completes
            meter.start();
            
            // First batch: 5 iterations
            for (int i = 0; i < 5; i++) {
                meter.inc();
            }
            timeSource.advanceMiliseconds(40); // Execute ~40ms
            meter.progress();
            
            // Second batch: 5 iterations
            for (int i = 0; i < 5; i++) {
                meter.inc();
            }
            timeSource.advanceMiliseconds(40); // Execute ~40ms
            meter.progress();
            
            // Third batch: 5 iterations
            for (int i = 0; i < 5; i++) {
                meter.inc();
            }
            timeSource.advanceMiliseconds(40); // Execute ~40ms
            meter.ok();

            // Then: meter should be in OK state with correct iteration count
            assertMeterState(meter, true, true, null, null, null, null, 15, 15, 0);
            
            // Then: progress messages should have been logged (skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_PROGRESS);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_PROGRESS);
            AssertLogger.assertEvent(logger, 4, Level.INFO, Markers.MSG_PROGRESS);
            AssertLogger.assertEvent(logger, 5, Level.TRACE, Markers.DATA_PROGRESS);
            AssertLogger.assertEvent(logger, 6, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 7, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("Created → Started → OK with mixed iterations and consecutive progress")
        void shouldTransitionCreatedToStartedToOkWithMixedIterationsAndProgress2() throws InterruptedException {
            // Given: a meter with expected iterations configured
            final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);
            final Meter meter = new Meter(logger).withTimeSource(timeSource);
            meter.iterations(15);
            MeterConfig.progressPeriodMilliseconds = 0; // Disable throttling for test

            // When: meter is started, incremented with progress calls, and completes
            meter.start();

            // First batch: 5 iterations
            for (int i = 0; i < 5; i++) {
                meter.inc();
            }
            timeSource.advanceMiliseconds(40); // Execute ~40ms
            meter.progress();

            // Second batch: no iterations
            meter.progress(); // won't print log with no more iterations

            // Third batch: 5 iterations
            for (int i = 0; i < 5; i++) {
                meter.inc();
            }
            timeSource.advanceMiliseconds(40); // Execute ~40ms
            meter.ok();

            // Then: meter should be in OK state with correct iteration count
            assertMeterState(meter, true, true, null, null, null, null, 10, 15, 0);

            // Then: progress messages should have been logged (skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_PROGRESS);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_PROGRESS);
            AssertLogger.assertEvent(logger, 4, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 5, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should transition Created → Started → OK with mixed iterations and progress (no time advance)")
        void shouldTransitionCreatedToStartedToOkWithMixedIterationsAndProgress3() throws InterruptedException {
            // Given: a meter with expected iterations configured
            final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);
            final Meter meter = new Meter(logger).withTimeSource(timeSource);
            meter.iterations(15);
            MeterConfig.progressPeriodMilliseconds = 0; // Disable throttling for test

            // When: meter is started, incremented with progress calls, and completes
            meter.start();

            // First batch: 5 iterations
            for (int i = 0; i < 5; i++) {
                meter.inc();
            }
            timeSource.advanceMiliseconds(40); // Execute ~40ms
            meter.progress();

            // Second batch: 5 iterations
            for (int i = 0; i < 5; i++) {
                meter.inc();
            }
            timeSource.advanceMiliseconds(0); // Execute 0ms
            meter.progress(); // won't print log since no time has passed

            // Third batch: 5 iterations
            for (int i = 0; i < 5; i++) {
                meter.inc();
            }
            timeSource.advanceMiliseconds(40); // Execute ~40ms
            meter.ok();

            // Then: meter should be in OK state with correct iteration count
            assertMeterState(meter, true, true, null, null, null, null, 15, 15, 0);

            // Then: progress messages should have been logged (skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_PROGRESS);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_PROGRESS);
            AssertLogger.assertEvent(logger, 4, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 5, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should transition Created → Started → OK with mixed iterations and progress (with throttling)")
        void shouldTransitionCreatedToStartedToOkWithMixedIterationsAndProgress4() throws InterruptedException {
            // Given: a meter with expected iterations configured
            final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);
            final Meter meter = new Meter(logger).withTimeSource(timeSource);
            meter.iterations(15);
            MeterConfig.progressPeriodMilliseconds = 50; // Enable throttling for test

            // When: meter is started, incremented with progress calls, and completes
            meter.start();

            // First batch: 5 iterations
            for (int i = 0; i < 5; i++) {
                meter.inc();
            }
            timeSource.advanceMiliseconds(40); // Execute ~40ms
            meter.progress(); // won't log due to throttling (40 < 50)

            // Second batch: 5 iterations
            for (int i = 0; i < 5; i++) {
                meter.inc();
            }
            timeSource.advanceMiliseconds(40); // Execute ~40ms
            meter.progress();

            // Third batch: 5 iterations
            for (int i = 0; i < 5; i++) {
                meter.inc();
            }
            timeSource.advanceMiliseconds(40); // Execute ~40ms
            meter.ok();

            // Then: meter should be in OK state with correct iteration count
            assertMeterState(meter, true, true, null, null, null, null, 15, 15, 0);

            // Then: progress messages should have been logged (skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_PROGRESS);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_PROGRESS);
            AssertLogger.assertEvent(logger, 4, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 5, Level.TRACE, Markers.DATA_OK);
        }

        // ============================================================================
        // Created → Started → OK with time limit (NOT slow)
        // ============================================================================

        @Test
        @DisplayName("should transition Created → Started → OK with time limit (NOT slow)")
        void shouldTransitionCreatedToStartedToOkWithTimeLimitNotSlow() throws InterruptedException {
            // Given: a started meter with time limit configured
            final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);
            final Meter meter = new Meter(logger).withTimeSource(timeSource);
            meter.limitMilliseconds(50);
            meter.start();

            // When: operation completes within time limit
            timeSource.advanceMiliseconds(10);
            meter.ok();

            // Then: meter should be in OK state and NOT slow
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 50);
            assertFalse(meter.isSlow(), "meter should NOT be slow");
            
            // Then: completion report logged as INFO (not slow, skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        // ============================================================================
        // Created → Started → OK with time limit (IS slow)
        // ============================================================================

        @Test
        @DisplayName("should transition Created → Started → OK with time limit (IS slow)")
        void shouldTransitionCreatedToStartedToOkWithTimeLimitIsSlow() throws InterruptedException {
            // Given: a started meter with time limit configured
            final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);
            final Meter meter = new Meter(logger).withTimeSource(timeSource);
            meter.limitMilliseconds(50);
            meter.start();

            // When: operation exceeds time limit
            timeSource.advanceMiliseconds(100); // Execute ~100ms
            meter.ok();

            // Then: meter should be in OK state and IS slow
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 50);
            assertTrue(meter.isSlow(), "meter should be slow");
            
            // Then: completion report logged as WARN (slow operation, skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.WARN, Markers.MSG_SLOW_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_SLOW_OK);
        }

        @Test
        @DisplayName("should transition Created → Started → Reject with time limit (IS slow)")
        void shouldTransitionCreatedToStartedToRejectWithTimeLimitIsSlow() throws InterruptedException {
            // Given: a started meter with time limit configured
            final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);
            final Meter meter = new Meter(logger).withTimeSource(timeSource);
            meter.limitMilliseconds(50);
            meter.start();

            // When: operation exceeds time limit
            timeSource.advanceMiliseconds(100); // Execute ~100ms
            meter.reject("business_error");

            // Then: meter should be in OK state and IS slow
            assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 50);
            assertTrue(meter.isSlow(), "meter should be slow");

            // Then: completion report logged as WARN (slow operation, skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }

        @Test
        @DisplayName("should transition Created → Started → Fail with time limit (IS slow)")
        void shouldTransitionCreatedToStartedToFailWithTimeLimitIsSlow() throws InterruptedException {
            // Given: a started meter with time limit configured
            final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);
            final Meter meter = new Meter(logger).withTimeSource(timeSource);
            meter.limitMilliseconds(50);
            meter.start();

            // When: operation exceeds time limit
            timeSource.advanceMiliseconds(100); // Execute ~100ms
            meter.fail("business_error");

            // Then: meter should be in OK state and IS slow
            assertMeterState(meter, true, true, null, null, "business_error", null, 0, 0, 50);
            assertTrue(meter.isSlow(), "meter should be slow");

            // Then: completion report logged as WARN (slow operation, skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }

        // ============================================================================
        // Created → Started → OK with high iteration count + time limit (NOT slow)
        // ============================================================================

        @Test
        @DisplayName("should transition Created → Started → OK with high iteration count + time limit (NOT slow)")
        void shouldTransitionCreatedToStartedToOkWithHighIterationCountAndTimeLimitNotSlow() throws InterruptedException {
            // Given: a meter with iterations and time limit configured
            final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);
            final Meter meter = new Meter(logger).withTimeSource(timeSource);
            meter.iterations(15);
            meter.limitMilliseconds(100); // Increased to ensure NOT slow
            MeterConfig.progressPeriodMilliseconds = 0; // Disable throttling for test

            // When: meter executes with iterations and completes within time limit
            meter.start();
            
            // First batch: 5 iterations
            for (int i = 0; i < 5; i++) {
                meter.inc();
            }
            timeSource.advanceMiliseconds(5); // Execute ~5ms
            meter.progress();
            
            // Second batch: 5 iterations
            for (int i = 0; i < 5; i++) {
                meter.inc();
            }
            timeSource.advanceMiliseconds(5); // Execute ~5ms
            meter.progress();
            
            // Third batch: 5 iterations
            for (int i = 0; i < 5; i++) {
                meter.inc();
            }
            timeSource.advanceMiliseconds(5); // Execute ~5ms
            meter.ok();

            // Then: meter should be in OK state with correct iterations and NOT slow (15 < 100)
            assertMeterState(meter, true, true, null, null, null, null, 15, 15, 100);
            assertFalse(meter.isSlow(), "meter should NOT be slow");
            
            // Then: completion report includes all progress metrics (skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_PROGRESS);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_PROGRESS);
            AssertLogger.assertEvent(logger, 4, Level.INFO, Markers.MSG_PROGRESS);
            AssertLogger.assertEvent(logger, 5, Level.TRACE, Markers.DATA_PROGRESS);
            AssertLogger.assertEvent(logger, 6, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 7, Level.TRACE, Markers.DATA_OK);
        }

        // ============================================================================
        // Created → Started → OK with high iteration count + strict time limit (IS slow)
        // ============================================================================

        @Test
        @DisplayName("should transition Created → Started → OK with high iteration count + strict time limit (IS slow)")
        void shouldTransitionCreatedToStartedToOkWithHighIterationCountAndStrictTimeLimitIsSlow() throws InterruptedException {
            // Given: a meter with iterations and strict time limit configured
            final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);
            final Meter meter = new Meter(logger).withTimeSource(timeSource);
            meter.iterations(15);
            meter.limitMilliseconds(50);
            MeterConfig.progressPeriodMilliseconds = 0; // Disable throttling for test

            // When: meter executes with iterations and exceeds time limit
            meter.start();
            
            // First batch: 5 iterations
            for (int i = 0; i < 5; i++) {
                meter.inc();
            }
            timeSource.advanceMiliseconds(40); // Execute ~40ms
            meter.progress();
            
            // Second batch: 5 iterations
            for (int i = 0; i < 5; i++) {
                meter.inc();
            }
            timeSource.advanceMiliseconds(40); // Execute ~40ms (total ~80ms), exceeds 50ms limit
            meter.progress();
            assertTrue(meter.isSlow(), "meter should be slow");
            
            // Third batch: 5 iterations
            for (int i = 0; i < 5; i++) {
                meter.inc();
            }
            timeSource.advanceMiliseconds(40); // Execute ~40ms
            meter.ok();

            // Then: meter should be in OK state with correct iterations and IS slow
            assertMeterState(meter, true, true, null, null, null, null, 15, 15, 50);
            assertTrue(meter.isSlow(), "meter should be slow");
            
            // Then: WARN log includes slow operation warning with timing details (skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_PROGRESS);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_PROGRESS);
            AssertLogger.assertEvent(logger, 4, Level.INFO, Markers.MSG_PROGRESS);
            AssertLogger.assertEvent(logger, 5, Level.TRACE, Markers.DATA_SLOW_PROGRESS);
            AssertLogger.assertEvent(logger, 6, Level.WARN, Markers.MSG_SLOW_OK);
            AssertLogger.assertEvent(logger, 7, Level.TRACE, Markers.DATA_SLOW_OK);
        }

        // ============================================================================
        // Created → Started → Rejected with iterations
        // ============================================================================
        @Test
        @DisplayName("should transition Created → Started → Rejected with iterations")
        void shouldTransitionCreatedToStartedToRejectedWithIterations() {
            // Given: a meter with iterations and time limit configured
            final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);
            final Meter meter = new Meter(logger).withTimeSource(timeSource);
            meter.iterations(15);
            meter.limitMilliseconds(100); // Increased to ensure NOT slow
            MeterConfig.progressPeriodMilliseconds = 0; // Disable throttling for test

            // When: meter executes with iterations and completes within time limit
            meter.start();

            // First batch: 5 iterations
            for (int i = 0; i < 5; i++) {
                meter.inc();
            }
            timeSource.advanceMiliseconds(5); // Execute ~5ms
            meter.progress();

            // Second batch: 5 iterations
            for (int i = 0; i < 5; i++) {
                meter.inc();
            }
            timeSource.advanceMiliseconds(5); // Execute ~5ms
            meter.progress();

            // Third batch: 5 iterations
            for (int i = 0; i < 5; i++) {
                meter.inc();
            }
            timeSource.advanceMiliseconds(5); // Execute ~5ms
            meter.reject("validation_failed");

            // Then: meter should be in OK state with correct iterations and NOT slow (15 < 100)
            assertMeterState(meter, true, true, null, "validation_failed", null, null, 15, 15, 100);
            assertFalse(meter.isSlow(), "meter should NOT be slow");

            // Then: completion report includes all progress metrics (skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_PROGRESS);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_PROGRESS);
            AssertLogger.assertEvent(logger, 4, Level.INFO, Markers.MSG_PROGRESS);
            AssertLogger.assertEvent(logger, 5, Level.TRACE, Markers.DATA_PROGRESS);
            AssertLogger.assertEvent(logger, 6, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 7, Level.TRACE, Markers.DATA_REJECT);
        }

        @Test
        @DisplayName("should transition Created → Started → Rejected with iterations")
        void shouldTransitionCreatedToStartedToFailedWithIterations() {
            // Given: a meter with iterations and time limit configured
            final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);
            final Meter meter = new Meter(logger).withTimeSource(timeSource);
            meter.iterations(15);
            meter.limitMilliseconds(100); // Increased to ensure NOT slow
            MeterConfig.progressPeriodMilliseconds = 0; // Disable throttling for test

            // When: meter executes with iterations and completes within time limit
            meter.start();

            // First batch: 5 iterations
            for (int i = 0; i < 5; i++) {
                meter.inc();
            }
            timeSource.advanceMiliseconds(5); // Execute ~5ms
            meter.progress();

            // Second batch: 5 iterations
            for (int i = 0; i < 5; i++) {
                meter.inc();
            }
            timeSource.advanceMiliseconds(5); // Execute ~5ms
            meter.progress();

            // Third batch: 5 iterations
            for (int i = 0; i < 5; i++) {
                meter.inc();
            }
            timeSource.advanceMiliseconds(5); // Execute ~5ms
            meter.fail("validation_failed");

            // Then: meter should be in OK state with correct iterations and NOT slow (15 < 100)
            assertMeterState(meter, true, true, null, null, "validation_failed",null, 15, 15, 100);
            assertFalse(meter.isSlow(), "meter should NOT be slow");

            // Then: completion report includes all progress metrics (skip indices 0 and 1 from start())
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_PROGRESS);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_PROGRESS);
            AssertLogger.assertEvent(logger, 4, Level.INFO, Markers.MSG_PROGRESS);
            AssertLogger.assertEvent(logger, 5, Level.TRACE, Markers.DATA_PROGRESS);
            AssertLogger.assertEvent(logger, 6, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 7, Level.TRACE, Markers.DATA_FAIL);
        }
    }

    @Nested
    @DisplayName("Group 3: Try-With-Resources (✅ Tier 1 + ⚠️ Tier 3")
    class TryWithResources {
        @Test
        @DisplayName("should follow try-with-resources flow (implicit failure)")
        void shouldFollowTryWithResourcesFlowImplicitFailure() {
            final Meter meter;
            // Given: a new, started Meter withing try with resources
            try (final Meter m = new Meter(logger).start()) {
                meter = m;
                // do nothing
            }

            // Then: it should be automatically failed on close()
            assertMeterState(meter, true, true, null, null, "try-with-resources", null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }

        @Test
        @DisplayName("should follow try-with-resources flow (explicit success)")
        void shouldFollowTryWithResourcesFlowExplicitSuccess() {
            final Meter meter;
            // Given: a new, started Meter withing try with resources
            try (final Meter m = new Meter(logger).start()) {
                meter = m;

                // When: ok() is called
                m.ok();

                // Then: Meter is in stopped state
                assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            }

            // Then: it should remain in success state after close()
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow try-with-resources flow (path and explicit success)")
        void shouldFollowTryWithResourcesFlowPathAndExplicitSuccess() {
            final Meter meter;
            // Given: a new, started Meter withing try with resources
            try (final Meter m = new Meter(logger).start()) {
                meter = m;

                // When: path() is called
                m.path("customPath");

                // Then: path is set
                assertMeterState(meter, true, false, "customPath", null, null, null, 0, 0, 0);

                // When: ok() is called
                m.ok();

                // Then: Meter is in stopped state with path preserved
                assertMeterState(meter, true, true, "customPath", null, null, null, 0, 0, 0);
            }

            // Then: it should remain in success state after close()
            assertMeterState(meter, true, true, "customPath", null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow try-with-resources flow (explicit rejection)")
        void shouldFollowTryWithResourcesFlowExplicitRejection() {
            final Meter meter;
            // Given: a new, started Meter withing try with resources
            try (final Meter m = new Meter(logger).start()) {
                meter = m;

                // When: reject() is called
                m.reject("rejected");

                // Then: Meter is in stopped state with reject path set
                assertMeterState(meter, true, true, null, "rejected", null, null, 0, 0, 0);
            }

            // Then: it should remain in rejection state after close()
            assertMeterState(meter, true, true, null, "rejected", null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }

        @Test
        @DisplayName("should follow try-with-resources flow (explicit failure)")
        void shouldFollowTryWithResourcesFlowExplicitFailure() {
            final Meter meter;
            // Given: a new, started Meter withing try with resources
            try (final Meter m = new Meter(logger).start()) {
                meter = m;

                // When: fail() is called
                m.fail("failed");

                // Then: Meter is in stopped state with failure message
                assertMeterState(meter, true, true, null, null, "failed", null, 0, 0, 0);
            }

            // Then: it should remain in failure state after close()
            assertMeterState(meter, true, true, null, null, "failed", null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }

        // ============================================================================
        // Try-with-resources WITHOUT start() (⚠️ Tier 3 - State-Correcting)
        // ============================================================================

        @Test
        @DisplayName("should transition to Failed via try-with-resources without start() - implicit close()")
        void shouldTransitionToFailedViaTryWithResourcesWithoutStartImplicitClose() {
            /* Given: Meter created in try-with-resources without start() */
            try (final Meter m = new Meter(logger)) {
                /* When: block executes without calling start(), ok(), reject(), or fail()
                 * (meter auto-closes with implicit fail) */
            }

            /* Then: logs INCONSISTENT_CLOSE + ERROR for implicit failure */
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_CLOSE);
            AssertLogger.assertEvent(logger, 1, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_FAIL);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, "try-with-resources");
        }

        @Test
        @DisplayName("should transition to OK via try-with-resources without start() - explicit ok()")
        void shouldTransitionToOkViaTryWithResourcesWithoutStartExplicitOk() {
            Meter meter = null;
            /* Given: Meter created in try-with-resources without start() */
            try (final Meter m = new Meter(logger)) {
                meter = m;
                /* When: ok() is called without start() */
                m.ok();
            }

            /* Then: meter transitions to OK state despite missing start() */
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

            /* Then: logs INCONSISTENT_OK + INFO completion report, close() does nothing */
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_OK);
            AssertLogger.assertEvent(logger, 1, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEventCount(logger, 3);
        }

        @Test
        @DisplayName("should transition to OK with path via try-with-resources without start() - explicit ok(String)")
        void shouldTransitionToOkWithPathViaTryWithResourcesWithoutStartExplicitOkString() {
            Meter meter = null;
            /* Given: Meter created in try-with-resources without start() */
            try (final Meter m = new Meter(logger)) {
                meter = m;
                /* When: ok("success_path") is called without start() */
                m.ok("success_path");
            }

            /* Then: meter transitions to OK state with path */
            assertMeterState(meter, true, true, "success_path", null, null, null, 0, 0, 0);

            /* Then: logs INCONSISTENT_OK + INFO completion report, close() does nothing */
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_OK);
            AssertLogger.assertEvent(logger, 1, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEventCount(logger, 3);
        }

        @Test
        @DisplayName("should transition to OK with Enum via try-with-resources without start() - explicit ok(Enum)")
        void shouldTransitionToOkWithEnumViaTryWithResourcesWithoutStartExplicitOkEnum() {
            Meter meter = null;
            /* Given: Meter created in try-with-resources without start() */
            try (final Meter m = new Meter(logger)) {
                meter = m;
                /* When: ok(Enum) is called without start() */
                m.ok(TestEnum.VALUE1);
            }

            /* Then: meter transitions to OK state with enum toString as path */
            assertMeterState(meter, true, true, "VALUE1", null, null, null, 0, 0, 0);

            /* Then: logs INCONSISTENT_OK + INFO completion report, close() does nothing */
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_OK);
            AssertLogger.assertEvent(logger, 1, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEventCount(logger, 3);
        }

        @Test
        @DisplayName("should transition to OK with Throwable via try-with-resources without start() - explicit ok(Throwable)")
        void shouldTransitionToOkWithThrowableViaTryWithResourcesWithoutStartExplicitOkThrowable() {
            Meter meter = null;
            final RuntimeException exception = new RuntimeException("test cause");
            /* Given: Meter created in try-with-resources without start() */
            try (final Meter m = new Meter(logger)) {
                meter = m;
                /* When: ok(Throwable) is called without start() */
                m.ok(exception);
            }

            /* Then: meter transitions to OK state with throwable simple class name as path */
            assertMeterState(meter, true, true, "RuntimeException", null, null, null, 0, 0, 0);

            /* Then: logs INCONSISTENT_OK + INFO completion report, close() does nothing */
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_OK);
            AssertLogger.assertEvent(logger, 1, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEventCount(logger, 3);
        }

        @Test
        @DisplayName("should transition to OK with Object via try-with-resources without start() - explicit ok(Object)")
        void shouldTransitionToOkWithObjectViaTryWithResourcesWithoutStartExplicitOkObject() {
            Meter meter = null;
            final TestObject testObject = new TestObject();
            /* Given: Meter created in try-with-resources without start() */
            try (final Meter m = new Meter(logger)) {
                meter = m;
                /* When: ok(Object) is called without start() */
                m.ok(testObject);
            }

            /* Then: meter transitions to OK state with object toString as path */
            assertMeterState(meter, true, true, "testObjectString", null, null, null, 0, 0, 0);

            /* Then: logs INCONSISTENT_OK + INFO completion report, close() does nothing */
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_OK);
            AssertLogger.assertEvent(logger, 1, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEventCount(logger, 3);
        }

        @Test
        @DisplayName("should transition to Rejected via try-with-resources without start() - explicit reject(String)")
        void shouldTransitionToRejectedViaTryWithResourcesWithoutStartExplicitRejectString() {
            Meter meter = null;
            /* Given: Meter created in try-with-resources without start() */
            try (final Meter m = new Meter(logger)) {
                meter = m;
                /* When: reject("business_error") is called without start() */
                m.reject("business_error");
            }

            /* Then: meter transitions to Rejected state with rejectPath */
            assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

            /* Then: logs INCONSISTENT_REJECT + INFO rejection report, close() does nothing */
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_REJECT);
            AssertLogger.assertEvent(logger, 1, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_REJECT);
            AssertLogger.assertEventCount(logger, 3);
        }

        @Test
        @DisplayName("should transition to Rejected with Enum via try-with-resources without start() - explicit reject(Enum)")
        void shouldTransitionToRejectedWithEnumViaTryWithResourcesWithoutStartExplicitRejectEnum() {
            Meter meter = null;
            /* Given: Meter created in try-with-resources without start() */
            try (final Meter m = new Meter(logger)) {
                meter = m;
                /* When: reject(Enum) is called without start() */
                m.reject(TestEnum.VALUE2);
            }

            /* Then: meter transitions to Rejected state with enum toString as cause */
            assertMeterState(meter, true, true, null, "VALUE2", null, null, 0, 0, 0);

            /* Then: logs INCONSISTENT_REJECT + INFO rejection report, close() does nothing */
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_REJECT);
            AssertLogger.assertEvent(logger, 1, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_REJECT);
            AssertLogger.assertEventCount(logger, 3);
        }

        @Test
        @DisplayName("should transition to Rejected with Throwable via try-with-resources without start() - explicit reject(Throwable)")
        void shouldTransitionToRejectedWithThrowableViaTryWithResourcesWithoutStartExplicitRejectThrowable() {
            Meter meter = null;
            final IllegalArgumentException exception = new IllegalArgumentException("invalid input");
            /* Given: Meter created in try-with-resources without start() */
            try (final Meter m = new Meter(logger)) {
                meter = m;
                /* When: reject(Throwable) is called without start() */
                m.reject(exception);
            }

            /* Then: meter transitions to Rejected state with throwable simple class name as cause */
            assertMeterState(meter, true, true, null, "IllegalArgumentException", null, null, 0, 0, 0);

            /* Then: logs INCONSISTENT_REJECT + INFO rejection report, close() does nothing */
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_REJECT);
            AssertLogger.assertEvent(logger, 1, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_REJECT);
            AssertLogger.assertEventCount(logger, 3);
        }

        @Test
        @DisplayName("should transition to Rejected with Object via try-with-resources without start() - explicit reject(Object)")
        void shouldTransitionToRejectedWithObjectViaTryWithResourcesWithoutStartExplicitRejectObject() {
            Meter meter = null;
            final TestObject testObject = new TestObject();
            /* Given: Meter created in try-with-resources without start() */
            try (final Meter m = new Meter(logger)) {
                meter = m;
                /* When: reject(Object) is called without start() */
                m.reject(testObject);
            }

            /* Then: meter transitions to Rejected state with object toString as cause */
            assertMeterState(meter, true, true, null, "testObjectString", null, null, 0, 0, 0);

            /* Then: logs INCONSISTENT_REJECT + INFO rejection report, close() does nothing */
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_REJECT);
            AssertLogger.assertEvent(logger, 1, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_REJECT);
            AssertLogger.assertEventCount(logger, 3);
        }

        @Test
        @DisplayName("should transition to Failed via try-with-resources without start() - explicit fail(String)")
        void shouldTransitionToFailedViaTryWithResourcesWithoutStartExplicitFailString() {
            Meter meter = null;
            /* Given: Meter created in try-with-resources without start() */
            try (final Meter m = new Meter(logger)) {
                meter = m;
                /* When: fail("technical_error") is called without start() */
                m.fail("technical_error");
            }

            /* Then: meter transitions to Failed state with failPath (failMessage null for non-Throwable) */
            assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);

            /* Then: logs INCONSISTENT_FAIL + ERROR failure report, close() does nothing */
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_FAIL);
            AssertLogger.assertEvent(logger, 1, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_FAIL);
            AssertLogger.assertEventCount(logger, 3);
        }

        @Test
        @DisplayName("should transition to Failed with Enum via try-with-resources without start() - explicit fail(Enum)")
        void shouldTransitionToFailedWithEnumViaTryWithResourcesWithoutStartExplicitFailEnum() {
            Meter meter = null;
            /* Given: Meter created in try-with-resources without start() */
            try (final Meter m = new Meter(logger)) {
                meter = m;
                /* When: fail(Enum) is called without start() */
                m.fail(TestEnum.VALUE1);
            }

            /* Then: meter transitions to Failed state with enum toString as cause (failMessage null) */
            assertMeterState(meter, true, true, null, null, "VALUE1", null, 0, 0, 0);

            /* Then: logs INCONSISTENT_FAIL + ERROR failure report, close() does nothing */
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_FAIL);
            AssertLogger.assertEvent(logger, 1, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_FAIL);
            AssertLogger.assertEventCount(logger, 3);
        }

        @Test
        @DisplayName("should transition to Failed with Throwable via try-with-resources without start() - explicit fail(Throwable)")
        void shouldTransitionToFailedWithThrowableViaTryWithResourcesWithoutStartExplicitFailThrowable() {
            Meter meter = null;
            final Exception exception = new Exception("connection timeout");
            /* Given: Meter created in try-with-resources without start() */
            try (final Meter m = new Meter(logger)) {
                meter = m;
                /* When: fail(Throwable) is called without start() */
                m.fail(exception);
            }

            /* Then: meter transitions to Failed state with throwable full class name as path and message as failMessage */
            assertMeterState(meter, true, true, null, null, "java.lang.Exception", "connection timeout", 0, 0, 0);

            /* Then: logs INCONSISTENT_FAIL + ERROR failure report, close() does nothing */
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_FAIL);
            AssertLogger.assertEvent(logger, 1, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_FAIL);
            AssertLogger.assertEventCount(logger, 3);
        }

        @Test
        @DisplayName("should transition to Failed with Object via try-with-resources without start() - explicit fail(Object)")
        void shouldTransitionToFailedWithObjectViaTryWithResourcesWithoutStartExplicitFailObject() {
            Meter meter = null;
            final TestObject testObject = new TestObject();
            /* Given: Meter created in try-with-resources without start() */
            try (final Meter m = new Meter(logger)) {
                meter = m;
                /* When: fail(Object) is called without start() */
                m.fail(testObject);
            }

            /* Then: meter transitions to Failed state with object toString as cause (failMessage null) */
            assertMeterState(meter, true, true, null, null, "testObjectString", null, 0, 0, 0);

            /* Then: logs INCONSISTENT_FAIL + ERROR failure report, close() does nothing */
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_FAIL);
            AssertLogger.assertEvent(logger, 1, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_FAIL);
            AssertLogger.assertEventCount(logger, 3);
        }
    }

    @Nested
    @DisplayName("Group 4: Pre-Start Attribute Updates (☑️ Tier 2)")
    class PreStartConfiguration {
        // ============================================================================
        // Set time limit
        // ============================================================================
        
        @Test
        @DisplayName("should set time limit before start()")
        void shouldSetTimeLimitBeforeStart() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: limitMilliseconds(5000) is called before start()
            meter.limitMilliseconds(5000);

            // Then: timeLimit attribute is stored correctly and meter remains in Created state
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 5000);
            AssertLogger.assertEventCount(logger, 0);
        }

        @Test
        @DisplayName("should override time limit when set multiple times")
        void shouldOverrideTimeLimitWhenSetMultipleTimes() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: limitMilliseconds() is called twice
            meter.limitMilliseconds(100);
            meter.limitMilliseconds(5000);

            // Then: last value wins and meter remains in Created state
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 5000);
            AssertLogger.assertEventCount(logger, 0);
        }

        @Test
        @DisplayName("should preserve valid time limit when invalid value attempted")
        void shouldPreserveValidTimeLimitWhenInvalidValueAttempted() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: limitMilliseconds(5000) is called, then limitMilliseconds(0) is attempted
            meter.limitMilliseconds(5000);
            meter.limitMilliseconds(0);

            // Then: first valid value is preserved, meter remains in Created state
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 5000);
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should log ILLEGAL when negative time limit attempted")
        void shouldLogIllegalWhenNegativeTimeLimitAttempted() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: limitMilliseconds(-1) is called
            meter.limitMilliseconds(-1);

            // Then: meter remains in Created state with no time limit set
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.ILLEGAL);
        }

        // ============================================================================
        // Set expected iterations
        // ============================================================================
        
        @Test
        @DisplayName("should set expected iterations before start()")
        void shouldSetExpectedIterationsBeforeStart() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: iterations(100) is called before start()
            meter.iterations(100);

            // Then: expectedIterations attribute is stored correctly and meter remains in Created state
            assertMeterState(meter, false, false, null, null, null, null, 0, 100, 0);
            AssertLogger.assertEventCount(logger, 0);
        }

        @Test
        @DisplayName("should override expected iterations when set multiple times")
        void shouldOverrideExpectedIterationsWhenSetMultipleTimes() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: iterations() is called twice
            meter.iterations(50);
            meter.iterations(100);

            // Then: last value wins and meter remains in Created state
            assertMeterState(meter, false, false, null, null, null, null, 0, 100, 0);
            AssertLogger.assertEventCount(logger, 0);
        }

        @Test
        @DisplayName("should preserve valid iterations when invalid value attempted")
        void shouldPreserveValidIterationsWhenInvalidValueAttempted() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: iterations(100) is called, then iterations(0) is attempted
            meter.iterations(100);
            meter.iterations(0);

            // Then: first valid value is preserved, meter remains in Created state
            assertMeterState(meter, false, false, null, null, null, null, 0, 100, 0);
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should log ILLEGAL when negative iterations attempted")
        void shouldLogIllegalWhenNegativeIterationsAttempted() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: iterations(-5) is called
            meter.iterations(-5);

            // Then: meter remains in Created state with no iterations set
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.ILLEGAL);
        }

        // ============================================================================
        // Add descriptive message
        // ============================================================================
        
        @Test
        @DisplayName("should add descriptive message before start()")
        void shouldAddDescriptiveMessageBeforeStart() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: m(String) is called before start()
            meter.m("starting operation");

            // Then: description attribute is stored correctly and meter remains in Created state
            assertEquals("starting operation", meter.getDescription());
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEventCount(logger, 0);
        }

        @Test
        @DisplayName("should override description when m() is called multiple times")
        void shouldOverrideDescriptionWhenMCalledMultipleTimes() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: m() is called multiple times
            meter.m("step 1");
            meter.m("step 2");

            // Then: last value wins and meter remains in Created state
            assertEquals("step 2", meter.getDescription());
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEventCount(logger, 0);
        }

        @Test
        @DisplayName("should preserve valid message when null value attempted")
        void shouldPreserveValidMessageWhenNullValueAttempted() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: m("step 1") is called, then m(null) is attempted
            meter.m("step 1");
            meter.m(null);

            // Then: first valid value is preserved, meter remains in Created state
            assertEquals("step 1", meter.getDescription());
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should log ILLEGAL when null message attempted before any valid message")
        void shouldLogIllegalWhenNullMessageAttemptedBeforeAnyValidMessage() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: m(null) is called without setting a previous message
            meter.m(null);

            // Then: description remains null, meter remains in Created state
            assertNull(meter.getDescription());
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.ILLEGAL);
        }

        // ============================================================================
        // Add formatted descriptive message
        // ============================================================================
        
        @Test
        @DisplayName("should add formatted descriptive message before start()")
        void shouldAddFormattedDescriptiveMessageBeforeStart() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: m(format, args) is called before start()
            meter.m("operation %s", "doWork");

            // Then: description attribute is formatted and stored correctly and meter remains in Created state
            assertEquals("operation doWork", meter.getDescription());
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEventCount(logger, 0);
        }

        @Test
        @DisplayName("should override formatted message when m() is called multiple times")
        void shouldOverrideFormattedMessageWhenMCalledMultipleTimes() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: m(format, args) is called multiple times
            meter.m("step %d", 1);
            meter.m("step %d", 2);

            // Then: last value wins and meter remains in Created state
            assertEquals("step 2", meter.getDescription());
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEventCount(logger, 0);
        }

        @Test
        @DisplayName("should preserve valid formatted message when null format attempted")
        void shouldPreserveValidFormattedMessageWhenNullFormatAttempted() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: m("valid: %s", "arg") is called, then m(null, "arg") is attempted
            meter.m("valid: %s", "arg");
            meter.m(null, "arg");

            // Then: null format is rejected with ILLEGAL log, description is reset to null, meter remains in Created state
            assertNull(meter.getDescription());
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should log ILLEGAL when invalid format string attempted")
        void shouldLogIllegalWhenInvalidFormatStringAttempted() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: m("invalid format %z", "arg") is called (invalid format specifier)
            meter.m("invalid format %z", "arg");

            // Then: meter remains in Created state with no description set
            assertNull(meter.getDescription());
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.ILLEGAL);
        }

        // ============================================================================
        // Add context key-value pairs
        // ============================================================================
        
        @Test
        @DisplayName("should add context key-value pair before start()")
        void shouldAddContextKeyValuePairBeforeStart() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: ctx("key1", "value1") is called before start()
            meter.ctx("key1", "value1");

            // Then: context contains the key-value pair and meter remains in Created state
            assertEquals("value1", meter.getContext().get("key1"));
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEventCount(logger, 0);
        }

        @Test
        @DisplayName("should override context value when same key set multiple times")
        void shouldOverrideContextValueWhenSameKeySetMultipleTimes() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: ctx() is called twice with the same key
            meter.ctx("key", "val1");
            meter.ctx("key", "val2");

            // Then: last value wins and meter remains in Created state
            assertEquals("val2", meter.getContext().get("key"));
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEventCount(logger, 0);
        }

        @Test
        @DisplayName("should replace context value when null value set")
        void shouldReplaceContextValueWhenNullValueSet() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: ctx("key", "valid") is called, then ctx("key", null) is called
            meter.ctx("key", "valid");
            meter.ctx("key", (String)null);

            // Then: null value is stored as "<null>" placeholder (context stores null as string literal)
            assertEquals("<null>", meter.getContext().get("key"));
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEventCount(logger, 0);
        }

        @Test
        @DisplayName("should store multiple different context key-value pairs")
        void shouldStoreMultipleDifferentContextKeyValuePairs() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: ctx() is called multiple times with different keys
            meter.ctx("key1", "value1");
            meter.ctx("key2", "value2");
            meter.ctx("key3", "value3");

            // Then: all context key-value pairs are stored and meter remains in Created state
            assertEquals("value1", meter.getContext().get("key1"));
            assertEquals("value2", meter.getContext().get("key2"));
            assertEquals("value3", meter.getContext().get("key3"));
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEventCount(logger, 0);
        }

        // ============================================================================
        // Chain multiple configurations
        // ============================================================================
        
        @Test
        @DisplayName("should chain multiple valid configurations before start()")
        void shouldChainMultipleValidConfigurationsBeforeStart() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: multiple configuration methods are chained
            meter
                .iterations(100)
                .limitMilliseconds(5000)
                .m("starting operation");

            // Then: all attributes are set correctly and meter remains in Created state
            assertEquals("starting operation", meter.getDescription());
            assertMeterState(meter, false, false, null, null, null, null, 0, 100, 5000);
            AssertLogger.assertEventCount(logger, 0);
        }

        @Test
        @DisplayName("should handle chained configuration with last m() value winning")
        void shouldHandleChainedConfigurationWithLastMValueWinning() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: multiple configuration methods are chained with m() called multiple times
            meter
                .m("op1")
                .limitMilliseconds(5000)
                .iterations(100)
                .m("op2");

            // Then: m() last value wins, iterations and limit preserved
            assertEquals("op2", meter.getDescription());
            assertMeterState(meter, false, false, null, null, null, null, 0, 100, 5000);
            AssertLogger.assertEventCount(logger, 0);
        }

        @Test
        @DisplayName("should ignore invalid values in chained configuration")
        void shouldIgnoreInvalidValuesInChainedConfiguration() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: chained configuration includes invalid values after valid values
            meter
                .limitMilliseconds(5000)
                .iterations(100)
                .limitMilliseconds(0)     // Invalid: 0
                .iterations(-1);           // Invalid: -1

            // Then: all valid values preserved, invalid attempts logged
            assertMeterState(meter, false, false, null, null, null, null, 0, 100, 5000);
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertEvent(logger, 1, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should chain configuration with context operations")
        void shouldChainConfigurationWithContextOperations() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: configuration is chained with context operations
            meter
                .iterations(100)
                .limitMilliseconds(5000)
                .m("starting operation")
                .ctx("user", "testUser")
                .ctx("session", "test-session-123");

            // Then: all attributes are set correctly and meter remains in Created state
            assertEquals("starting operation", meter.getDescription());
            assertEquals("testUser", meter.getContext().get("user"));
            assertEquals("test-session-123", meter.getContext().get("session"));
            assertMeterState(meter, false, false, null, null, null, null, 0, 100, 5000);
            AssertLogger.assertEventCount(logger, 0);
        }
    }

    @Nested
    @DisplayName("Group 5: Pre-Start Termination (⚠️ Tier 3)")
    class PreStartTermination {
        // ============================================================================
        // OK without starting (Created → OK)
        // ============================================================================

        @Test
        @DisplayName("should transition to OK when ok() called without start()")
        void shouldTransitionToOkWhenOkCalledWithoutStart() {
            // Given: a new Meter without start()
            final Meter meter = new Meter(logger);

            // When: ok() is called without start()
            meter.ok();

            // Then: meter transitions to OK state despite missing start()
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            
            // Then: logs INCONSISTENT_OK + INFO completion report
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_OK);
            AssertLogger.assertEvent(logger, 1, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should transition to OK with String path when ok(String) called without start()")
        void shouldTransitionToOkWithStringPathWhenOkStringCalledWithoutStart() {
            // Given: a new Meter without start()
            final Meter meter = new Meter(logger);

            // When: ok("success_path") is called without start()
            meter.ok("success_path");

            // Then: meter transitions to OK state with path
            assertMeterState(meter, true, true, "success_path", null, null, null, 0, 0, 0);
            
            // Then: logs INCONSISTENT_OK + INFO completion report
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_OK);
            AssertLogger.assertEvent(logger, 1, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should transition to OK with Enum path when ok(Enum) called without start()")
        void shouldTransitionToOkWithEnumPathWhenOkEnumCalledWithoutStart() {
            // Given: a new Meter without start()
            final Meter meter = new Meter(logger);

            // When: ok(Enum) is called without start()
            meter.ok(TestEnum.VALUE1);

            // Then: meter transitions to OK state with enum toString as path
            assertMeterState(meter, true, true, "VALUE1", null, null, null, 0, 0, 0);
            
            // Then: logs INCONSISTENT_OK + INFO completion report
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_OK);
            AssertLogger.assertEvent(logger, 1, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should transition to OK with Throwable path when ok(Throwable) called without start()")
        void shouldTransitionToOkWithThrowablePathWhenOkThrowableCalledWithoutStart() {
            // Given: a new Meter without start()
            final Meter meter = new Meter(logger);
            final RuntimeException exception = new RuntimeException("test cause");

            // When: ok(Throwable) is called without start()
            meter.ok(exception);

            /* Then: meter transitions to OK state with throwable simple class name as path */
            assertMeterState(meter, true, true, "RuntimeException", null, null, null, 0, 0, 0);
            
            // Then: logs INCONSISTENT_OK + INFO completion report
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_OK);
            AssertLogger.assertEvent(logger, 1, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should transition to OK with Object path when ok(Object) called without start()")
        void shouldTransitionToOkWithObjectPathWhenOkObjectCalledWithoutStart() {
            // Given: a new Meter without start()
            final Meter meter = new Meter(logger);
            final TestObject testObject = new TestObject();

            // When: ok(Object) is called without start()
            meter.ok(testObject);

            // Then: meter transitions to OK state with object toString as path
            assertMeterState(meter, true, true, "testObjectString", null, null, null, 0, 0, 0);
            
            // Then: logs INCONSISTENT_OK + INFO completion report
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_OK);
            AssertLogger.assertEvent(logger, 1, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_OK);
        }

        // ============================================================================
        // Reject without starting (Created → Rejected)
        // ============================================================================

        @Test
        @DisplayName("should transition to Rejected when reject(String) called without start()")
        void shouldTransitionToRejectedWhenRejectStringCalledWithoutStart() {
            // Given: a new Meter without start()
            final Meter meter = new Meter(logger);

            // When: reject("business_error") is called without start()
            meter.reject("business_error");

            // Then: meter transitions to Rejected state despite missing start()
            assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);
            
            /* Then: logs INCONSISTENT_REJECT + INFO rejection report */
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_REJECT);
            AssertLogger.assertEvent(logger, 1, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_REJECT);
        }

        @Test
        @DisplayName("should transition to Rejected with Enum cause when reject(Enum) called without start()")
        void shouldTransitionToRejectedWithEnumCauseWhenRejectEnumCalledWithoutStart() {
            // Given: a new Meter without start()
            final Meter meter = new Meter(logger);

            // When: reject(Enum) is called without start()
            meter.reject(TestEnum.VALUE2);

            // Then: meter transitions to Rejected state with enum toString as cause
            assertMeterState(meter, true, true, null, "VALUE2", null, null, 0, 0, 0);
            
            /* Then: logs INCONSISTENT_REJECT + INFO rejection report */
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_REJECT);
            AssertLogger.assertEvent(logger, 1, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_REJECT);
        }

        @Test
        @DisplayName("should transition to Rejected with Throwable cause when reject(Throwable) called without start()")
        void shouldTransitionToRejectedWithThrowableCauseWhenRejectThrowableCalledWithoutStart() {
            // Given: a new Meter without start()
            final Meter meter = new Meter(logger);
            final IllegalArgumentException exception = new IllegalArgumentException("invalid input");

            // When: reject(Throwable) is called without start()
            meter.reject(exception);

            /* Then: meter transitions to Rejected state with throwable simple class name as cause */
            assertMeterState(meter, true, true, null, "IllegalArgumentException", null, null, 0, 0, 0);
            
            /* Then: logs INCONSISTENT_REJECT + INFO rejection report */
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_REJECT);
            AssertLogger.assertEvent(logger, 1, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_REJECT);
        }

        @Test
        @DisplayName("should transition to Rejected with Object cause when reject(Object) called without start()")
        void shouldTransitionToRejectedWithObjectCauseWhenRejectObjectCalledWithoutStart() {
            // Given: a new Meter without start()
            final Meter meter = new Meter(logger);
            final TestObject testObject = new TestObject();

            // When: reject(Object) is called without start()
            meter.reject(testObject);

            /* Then: meter transitions to Rejected state with object toString as cause */
            assertMeterState(meter, true, true, null, "testObjectString", null, null, 0, 0, 0);
            
            /* Then: logs INCONSISTENT_REJECT + INFO rejection report */
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_REJECT);
            AssertLogger.assertEvent(logger, 1, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_REJECT);
        }

        // ============================================================================
        // Fail without starting (Created → Failed)
        // ============================================================================

        @Test
        @DisplayName("should transition to Failed when fail(String) called without start()")
        void shouldTransitionToFailedWhenFailStringCalledWithoutStart() {
            // Given: a new Meter without start()
            final Meter meter = new Meter(logger);

            // When: fail("technical_error") is called without start()
            meter.fail("technical_error");

            /* Then: meter transitions to Failed state (failMessage null for non-Throwable) */
            assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);
            
            // Then: logs INCONSISTENT_FAIL + ERROR failure report
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_FAIL);
            AssertLogger.assertEvent(logger, 1, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_FAIL);
        }

        @Test
        @DisplayName("should transition to Failed with Enum cause when fail(Enum) called without start()")
        void shouldTransitionToFailedWithEnumCauseWhenFailEnumCalledWithoutStart() {
            // Given: a new Meter without start()
            final Meter meter = new Meter(logger);

            // When: fail(Enum) is called without start()
            meter.fail(TestEnum.VALUE1);

            /* Then: meter transitions to Failed state (failMessage null for non-Throwable) */
            assertMeterState(meter, true, true, null, null, "VALUE1", null, 0, 0, 0);
            
            // Then: logs INCONSISTENT_FAIL + ERROR failure report
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_FAIL);
            AssertLogger.assertEvent(logger, 1, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_FAIL);
        }

        @Test
        @DisplayName("should transition to Failed with Throwable cause when fail(Throwable) called without start()")
        void shouldTransitionToFailedWithThrowableCauseWhenFailThrowableCalledWithoutStart() {
            // Given: a new Meter without start()
            final Meter meter = new Meter(logger);
            final Exception exception = new Exception("connection timeout");

            // When: fail(Throwable) is called without start()
            meter.fail(exception);

            /* Then: meter transitions to Failed state with throwable full class name as path and message as failMessage */
            assertMeterState(meter, true, true, null, null, "java.lang.Exception", "connection timeout", 0, 0, 0);
            
            // Then: logs INCONSISTENT_FAIL + ERROR failure report
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_FAIL);
            AssertLogger.assertEvent(logger, 1, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_FAIL);
        }

        @Test
        @DisplayName("should transition to Failed with Object cause when fail(Object) called without start()")
        void shouldTransitionToFailedWithObjectCauseWhenFailObjectCalledWithoutStart() {
            // Given: a new Meter without start()
            final Meter meter = new Meter(logger);
            final TestObject testObject = new TestObject();

            // When: fail(Object) is called without start()
            meter.fail(testObject);

            /* Then: meter transitions to Failed state (failMessage null for non-Throwable) */
            assertMeterState(meter, true, true, null, null, "testObjectString", null, 0, 0, 0);
            
            // Then: logs INCONSISTENT_FAIL + ERROR failure report
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_FAIL);
            AssertLogger.assertEvent(logger, 1, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_FAIL);
        }

        // ============================================================================
        // Pre-configured attributes preserved on self-correcting termination
        // ============================================================================

        @Test
        @DisplayName("should preserve pre-configured attributes when ok() called without start()")
        void shouldPreservePreConfiguredAttributesWhenOkCalledWithoutStart() {
            // Given: a new Meter with pre-configured attributes
            final Meter meter = new Meter(logger);
            meter.iterations(100);
            meter.limitMilliseconds(5000);
            meter.m("operation description");

            // When: ok() is called without start()
            meter.ok();

            // Then: all pre-configured attributes are preserved in terminal state
            assertMeterState(meter, true, true, null, null, null, null, 0, 100, 5000);
            assertEquals("operation description", meter.getDescription());
            
            // Then: logs INCONSISTENT_OK + INFO completion report (with attributes)
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_OK);
            AssertLogger.assertEvent(logger, 1, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, "operation description");
        }

        @Test
        @DisplayName("should preserve pre-configured attributes when reject() called without start()")
        void shouldPreservePreConfiguredAttributesWhenRejectCalledWithoutStart() {
            // Given: a new Meter with pre-configured attributes
            final Meter meter = new Meter(logger);
            meter.iterations(50);
            meter.limitMilliseconds(3000);
            meter.m("validation check");

            // When: reject() is called without start()
            meter.reject("validation_error");

            /* Then: all pre-configured attributes are preserved in terminal state */
            assertMeterState(meter, true, true, null, "validation_error", null, null, 0, 50, 3000);
            assertEquals("validation check", meter.getDescription());
            
            /* Then: logs INCONSISTENT_REJECT + INFO rejection report (with attributes) */
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_REJECT);
            AssertLogger.assertEvent(logger, 1, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_REJECT);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, "validation check");
        }

        @Test
        @DisplayName("should preserve pre-configured attributes when fail() called without start()")
        void shouldPreservePreConfiguredAttributesWhenFailCalledWithoutStart() {
            // Given: a new Meter with pre-configured attributes
            final Meter meter = new Meter(logger);
            meter.iterations(200);
            meter.limitMilliseconds(10000);
            meter.m("database operation");

            // When: fail() is called without start()
            meter.fail("connection_error");

            /* Then: all pre-configured attributes are preserved (failMessage null for String) */
            assertMeterState(meter, true, true, null, null, "connection_error", null, 0, 200, 10000);
            assertEquals("database operation", meter.getDescription());
            
            // Then: logs INCONSISTENT_FAIL + ERROR failure report (with attributes)
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_FAIL);
            AssertLogger.assertEvent(logger, 1, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_FAIL);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, "database operation");
        }

        // ============================================================================
        // Context preserved on self-correcting termination
        // ============================================================================

        @Test
        @DisplayName("should preserve context when ok() called without start()")
        void shouldPreserveContextWhenOkCalledWithoutStart() {
            // Given: a new Meter with context
            final Meter meter = new Meter(logger);
            meter.ctx("user", "alice");
            meter.ctx("action", "import");

            // When: ok() is called without start()
            meter.ok();

            // Then: context is preserved in terminal state
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            
            // Then: logs INCONSISTENT_OK + INFO completion report (with context)
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_OK);
            AssertLogger.assertEvent(logger, 1, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, "user", "alice");
            AssertLogger.assertEvent(logger, 2, Level.TRACE, "action", "import");
        }

        @Test
        @DisplayName("should preserve context when reject() called without start()")
        void shouldPreserveContextWhenRejectCalledWithoutStart() {
            // Given: a new Meter with context
            final Meter meter = new Meter(logger);
            meter.ctx("user", "bob");
            meter.ctx("action", "export");

            // When: reject() is called without start()
            meter.reject("validation_error");

            /* Then: context is preserved in terminal state */
            assertMeterState(meter, true, true, null, "validation_error", null, null, 0, 0, 0);
            
            /* Then: logs INCONSISTENT_REJECT + INFO rejection report (with context) */
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_REJECT);
            AssertLogger.assertEvent(logger, 1, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_REJECT);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, "user", "bob");
            AssertLogger.assertEvent(logger, 2, Level.TRACE, "action", "export");
        }

        @Test
        @DisplayName("should preserve context when fail() called without start()")
        void shouldPreserveContextWhenFailCalledWithoutStart() {
            // Given: a new Meter with context
            final Meter meter = new Meter(logger);
            meter.ctx("user", "charlie");
            meter.ctx("action", "delete");

            // When: fail() is called without start()
            meter.fail("permission_error");

            /* Then: context is preserved (failMessage null for String) */
            assertMeterState(meter, true, true, null, null, "permission_error", null, 0, 0, 0);
            
            // Then: logs INCONSISTENT_FAIL + ERROR failure report (with context)
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.INCONSISTENT_FAIL);
            AssertLogger.assertEvent(logger, 1, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, Markers.DATA_FAIL);
            AssertLogger.assertEvent(logger, 2, Level.TRACE, "user", "charlie");
            AssertLogger.assertEvent(logger, 2, Level.TRACE, "action", "delete");
        }

        // ============================================================================
        // Path set before starting (rejected, then termination)
        // ============================================================================

        @Test
        @DisplayName("should reject path() before start() then transition to OK")
        void shouldRejectPathBeforeStartThenTransitionToOk() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: path() is called before start(), then ok() is called
            meter.path("custom_path");
            meter.ok();

            // Then: path() was rejected (ILLEGAL), okPath remains undefined after ok()
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            
            // Then: logs ILLEGAL (path before start) + INCONSISTENT_OK + INFO completion
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertEvent(logger, 1, Level.ERROR, Markers.INCONSISTENT_OK);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should reject path() before start() then transition to Rejected")
        void shouldRejectPathBeforeStartThenTransitionToRejected() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: path() is called before start(), then reject() is called
            meter.path("custom_path");
            meter.reject("business_error");

            /* Then: path() was rejected (ILLEGAL), meter still reaches Rejected state */
            assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);
            
            /* Then: logs ILLEGAL (path before start) + INCONSISTENT_REJECT + INFO rejection */
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertEvent(logger, 1, Level.ERROR, Markers.INCONSISTENT_REJECT);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }

        @Test
        @DisplayName("should reject path() before start() then transition to Failed")
        void shouldRejectPathBeforeStartThenTransitionToFailed() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: path() is called before start(), then fail() is called
            meter.path("custom_path");
            meter.fail("technical_error");

            /* Then: path() was rejected (ILLEGAL), meter reaches Failed (failMessage null for String) */
            assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);
            
            // Then: logs ILLEGAL (path before start) + INCONSISTENT_FAIL + ERROR failure
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertEvent(logger, 1, Level.ERROR, Markers.INCONSISTENT_FAIL);
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }
    }

    @Nested
    @DisplayName("Group 6: Pre-Start Invalid Operations (❌ Tier 4)")
    class PostStopConfigurationOKState {
        // ============================================================================
        // Update description after stop (OK state)
        // ============================================================================

        @Test
        @DisplayName("should reject m() after ok()")
        void shouldRejectMAfterOk() {
            // Given: a meter that has been stopped with ok()
            final Meter meter = new Meter(logger).start();
            meter.ok();

            // When: m() is called on stopped meter
            meter.m("step 1");

            // Then: Meter state unchanged, logs ILLEGAL
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR ILLEGAL (from rejected m() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject formatted m() after ok()")
        void shouldRejectFormattedMAfterOk() {
            // Given: a meter that has been stopped with ok()
            final Meter meter = new Meter(logger).start();
            meter.ok();

            // When: formatted m() is called on stopped meter
            meter.m("step %d", 1);

            // Then: Meter state unchanged, logs ILLEGAL
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR ILLEGAL (from rejected m() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject null m() after ok()")
        void shouldRejectNullMAfterOk() {
            // Given: a meter that has been stopped with ok()
            final Meter meter = new Meter(logger).start();
            meter.ok();

            // When: null m() is called on stopped meter
            meter.m(null);

            // Then: Meter state unchanged, logs ILLEGAL
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR ILLEGAL (from rejected m() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject m() after ok(completion_path)")
        void shouldRejectMAfterOkWithPath() {
            // Given: a meter that has been stopped with ok(path)
            final Meter meter = new Meter(logger).start();
            meter.ok("completion_path");

            // When: m() is called on stopped meter
            meter.m("step 1");

            // Then: Meter state unchanged, logs ILLEGAL
            assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR ILLEGAL (from rejected m() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject formatted m() after ok(completion_path)")
        void shouldRejectFormattedMAfterOkWithPath() {
            // Given: a meter that has been stopped with ok(path)
            final Meter meter = new Meter(logger).start();
            meter.ok("completion_path");

            // When: formatted m() is called on stopped meter
            meter.m("step %d", 1);

            // Then: Meter state unchanged, logs ILLEGAL
            assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR ILLEGAL (from rejected m() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject null m() after ok(completion_path)")
        void shouldRejectNullMAfterOkWithPath() {
            // Given: a meter that has been stopped with ok(path)
            final Meter meter = new Meter(logger).start();
            meter.ok("completion_path");

            // When: null m() is called on stopped meter
            meter.m(null);

            // Then: Meter state unchanged, logs ILLEGAL
            assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR ILLEGAL (from rejected m() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        // ============================================================================
        // Increment operations after stop (OK state)
        // ============================================================================

        @Test
        @DisplayName("should reject inc() after ok()")
        void shouldRejectIncAfterOk() {
            // Given: a meter that has been stopped with ok()
            final Meter meter = new Meter(logger).start();
            meter.ok();

            // When: inc() is called on stopped meter
            meter.inc();

            // Then: currentIteration unchanged, logs INCONSISTENT_INCREMENT
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR INCONSISTENT_INCREMENT (from rejected inc() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.INCONSISTENT_INCREMENT);
        }

        @Test
        @DisplayName("should reject incBy() after ok()")
        void shouldRejectIncByAfterOk() {
            // Given: a meter that has been stopped with ok()
            final Meter meter = new Meter(logger).start();
            meter.ok();

            // When: incBy() is called on stopped meter
            meter.incBy(5);

            // Then: currentIteration unchanged, logs INCONSISTENT_INCREMENT
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR INCONSISTENT_INCREMENT (from rejected incBy() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.INCONSISTENT_INCREMENT);
        }

        @Test
        @DisplayName("should reject incTo() after ok()")
        void shouldRejectIncToAfterOk() {
            // Given: a meter that has been stopped with ok()
            final Meter meter = new Meter(logger).start();
            meter.ok();

            // When: incTo() is called on stopped meter
            meter.incTo(10);

            // Then: currentIteration unchanged, logs INCONSISTENT_INCREMENT
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR INCONSISTENT_INCREMENT (from rejected incTo() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.INCONSISTENT_INCREMENT);
        }

        @Test
        @DisplayName("should reject inc() after ok(completion_path)")
        void shouldRejectIncAfterOkWithPath() {
            // Given: a meter that has been stopped with ok(path)
            final Meter meter = new Meter(logger).start();
            meter.ok("completion_path");

            // When: inc() is called on stopped meter
            meter.inc();

            // Then: currentIteration unchanged, logs INCONSISTENT_INCREMENT
            assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR INCONSISTENT_INCREMENT (from rejected inc() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.INCONSISTENT_INCREMENT);
        }

        @Test
        @DisplayName("should reject incBy() after ok(completion_path)")
        void shouldRejectIncByAfterOkWithPath() {
            // Given: a meter that has been stopped with ok(path)
            final Meter meter = new Meter(logger).start();
            meter.ok("completion_path");

            // When: incBy() is called on stopped meter
            meter.incBy(5);

            // Then: currentIteration unchanged, logs INCONSISTENT_INCREMENT
            assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR INCONSISTENT_INCREMENT (from rejected incBy() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.INCONSISTENT_INCREMENT);
        }

        @Test
        @DisplayName("should reject incTo() after ok(completion_path)")
        void shouldRejectIncToAfterOkWithPath() {
            // Given: a meter that has been stopped with ok(path)
            final Meter meter = new Meter(logger).start();
            meter.ok("completion_path");

            // When: incTo() is called on stopped meter
            meter.incTo(10);

            // Then: currentIteration unchanged, logs INCONSISTENT_INCREMENT
            assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR INCONSISTENT_INCREMENT (from rejected incTo() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.INCONSISTENT_INCREMENT);
        }

        // ============================================================================
        // Progress after stop (OK state)
        // ============================================================================

        @Test
        @DisplayName("should reject progress() after ok()")
        void shouldRejectProgressAfterOk() {
            // Given: a meter that has been stopped with ok()
            final Meter meter = new Meter(logger).start();
            meter.ok();

            // When: progress() is called on stopped meter
            meter.progress();

            // Then: Meter state unchanged, logs INCONSISTENT_PROGRESS
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR INCONSISTENT_PROGRESS (from rejected progress() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.INCONSISTENT_PROGRESS);
        }

        @Test
        @DisplayName("should reject progress() after inc() then ok()")
        void shouldRejectProgressAfterIncThenOk() {
            // Given: a meter with incremented iteration that has been stopped
            final Meter meter = new Meter(logger).start();
            meter.inc();
            meter.ok();

            // When: progress() is called on stopped meter
            meter.progress();

            // Then: currentIteration unchanged at 1, logs INCONSISTENT_PROGRESS
            assertMeterState(meter, true, true, null, null, null, null, 1, 0, 0);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.INCONSISTENT_PROGRESS);
        }

        @Test
        @DisplayName("should reject progress() after ok(completion_path)")
        void shouldRejectProgressAfterOkWithPath() {
            // Given: a meter that has been stopped with ok(path)
            final Meter meter = new Meter(logger).start();
            meter.ok("completion_path");

            // When: progress() is called on stopped meter
            meter.progress();

            // Then: Meter state unchanged, logs INCONSISTENT_PROGRESS
            assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR INCONSISTENT_PROGRESS (from rejected progress() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.INCONSISTENT_PROGRESS);
        }

        @Test
        @DisplayName("should reject progress() after inc() then ok(completion_path)")
        void shouldRejectProgressAfterIncThenOkWithPath() {
            // Given: a meter with incremented iteration that has been stopped
            final Meter meter = new Meter(logger).start();
            meter.inc();
            meter.ok("completion_path");

            // When: progress() is called on stopped meter
            meter.progress();

            // Then: currentIteration unchanged at 1, logs INCONSISTENT_PROGRESS
            assertMeterState(meter, true, true, "completion_path", null, null, null, 1, 0, 0);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.INCONSISTENT_PROGRESS);
        }

        // ============================================================================
        // Update context after stop (OK state)
        // ============================================================================

        @Test
        @DisplayName("should reject ctx() after ok()")
        void shouldRejectCtxAfterOk() {
            // Given: a meter that has been stopped with ok()
            final Meter meter = new Meter(logger).start();
            meter.ok();

            // When: ctx() is called on stopped meter
            meter.ctx("key1", "value1");

            // Then: context unchanged, logs ILLEGAL
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR ILLEGAL (from rejected ctx() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject ctx() after ok() when context was previously set")
        void shouldRejectCtxAfterOkWithPreviousContext() {
            // Given: a meter with context that has been stopped with ok()
            final Meter meter = new Meter(logger).start();
            meter.ctx("key", "val");
            meter.ok();

            // When: ctx() is called on stopped meter to change context
            meter.ctx("key", "val2");

            // Then: context preserves original value, logs ILLEGAL
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject ctx() after ok(completion_path)")
        void shouldRejectCtxAfterOkWithPath() {
            // Given: a meter that has been stopped with ok(path)
            final Meter meter = new Meter(logger).start();
            meter.ok("completion_path");

            // When: ctx() is called on stopped meter
            meter.ctx("key1", "value1");

            // Then: context unchanged, logs ILLEGAL
            assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR ILLEGAL (from rejected ctx() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject ctx() after ok(completion_path) when context was previously set")
        void shouldRejectCtxAfterOkWithPathAndPreviousContext() {
            // Given: a meter with context that has been stopped with ok(path)
            final Meter meter = new Meter(logger).start();
            meter.ctx("key", "val");
            meter.ok("completion_path");

            // When: ctx() is called on stopped meter to change context
            meter.ctx("key", "val2");

            // Then: context preserves original value, logs ILLEGAL
            assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        // ============================================================================
        // Set path after stop (OK state)
        // ============================================================================

        @Test
        @DisplayName("should reject path() after ok()")
        void shouldRejectPathAfterOk() {
            // Given: a meter that has been stopped with ok()
            final Meter meter = new Meter(logger).start();
            meter.ok();

            // When: path() is called on stopped meter
            meter.path("new_path");

            // Then: okPath unchanged, logs ILLEGAL
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR ILLEGAL (from rejected path() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject path() after ok(original_path)")
        void shouldRejectPathAfterOkWithPath() {
            // Given: a meter that has been stopped with ok(original_path)
            final Meter meter = new Meter(logger).start();
            meter.ok("original_path");

            // When: path() is called to change the path
            meter.path("new_path");

            // Then: okPath remains original, logs ILLEGAL
            assertMeterState(meter, true, true, "original_path", null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR ILLEGAL (from rejected path() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject null path() after ok()")
        void shouldRejectNullPathAfterOk() {
            // Given: a meter that has been stopped with ok()
            final Meter meter = new Meter(logger).start();
            meter.ok();

            // When: null path() is called on stopped meter
            meter.path(null);

            // Then: okPath unchanged, logs ILLEGAL
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR ILLEGAL (from rejected path() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject path() after ok(completion_path)")
        void shouldRejectPathAfterOkWithCompletionPath() {
            // Given: a meter that has been stopped with ok(completion_path)
            final Meter meter = new Meter(logger).start();
            meter.ok("completion_path");

            // When: path() is called to change the path
            meter.path("new_path");

            // Then: okPath remains original, logs ILLEGAL
            assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR ILLEGAL (from rejected path() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject null path() after ok(completion_path)")
        void shouldRejectNullPathAfterOkWithCompletionPath() {
            // Given: a meter that has been stopped with ok(completion_path)
            final Meter meter = new Meter(logger).start();
            meter.ok("completion_path");

            // When: null path() is called on stopped meter
            meter.path(null);

            // Then: okPath unchanged, logs ILLEGAL
            assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR ILLEGAL (from rejected path() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        // ============================================================================
        // Update time limit after stop (OK state)
        // ============================================================================

        @Test
        @DisplayName("should reject limitMilliseconds() after ok()")
        void shouldRejectLimitMillisecondsAfterOk() {
            // Given: a meter that has been stopped with ok()
            final Meter meter = new Meter(logger).start();
            meter.ok();

            // When: limitMilliseconds() is called on stopped meter
            meter.limitMilliseconds(5000);

            // Then: timeLimit unchanged, logs ILLEGAL
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR ILLEGAL (from rejected limitMilliseconds() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject limitMilliseconds() after limitMilliseconds() then ok()")
        void shouldRejectLimitMillisecondsAfterSetThenOk() {
            // Given: a meter with timeLimit that has been stopped
            final Meter meter = new Meter(logger).start();
            meter.limitMilliseconds(100);
            meter.ok();

            // When: limitMilliseconds() is called on stopped meter
            meter.limitMilliseconds(5000);

            // Then: timeLimit remains 100, logs ILLEGAL
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 100);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject zero limitMilliseconds() after ok()")
        void shouldRejectZeroLimitMillisecondsAfterOk() {
            // Given: a meter that has been stopped with ok()
            final Meter meter = new Meter(logger).start();
            meter.ok();

            // When: zero limitMilliseconds() is called on stopped meter
            meter.limitMilliseconds(0);

            // Then: timeLimit unchanged, logs ILLEGAL
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR ILLEGAL (from rejected limitMilliseconds() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject negative limitMilliseconds() after ok()")
        void shouldRejectNegativeLimitMillisecondsAfterOk() {
            // Given: a meter that has been stopped with ok()
            final Meter meter = new Meter(logger).start();
            meter.ok();

            // When: negative limitMilliseconds() is called on stopped meter
            meter.limitMilliseconds(-1);

            // Then: timeLimit unchanged, logs ILLEGAL
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR ILLEGAL (from rejected limitMilliseconds() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject limitMilliseconds() after ok(completion_path)")
        void shouldRejectLimitMillisecondsAfterOkWithPath() {
            // Given: a meter that has been stopped with ok(path)
            final Meter meter = new Meter(logger).start();
            meter.ok("completion_path");

            // When: limitMilliseconds() is called on stopped meter
            meter.limitMilliseconds(5000);

            // Then: timeLimit unchanged, logs ILLEGAL
            assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR ILLEGAL (from rejected limitMilliseconds() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject limitMilliseconds() after limitMilliseconds() then ok(completion_path)")
        void shouldRejectLimitMillisecondsAfterSetThenOkWithPath() {
            // Given: a meter with timeLimit that has been stopped with ok(path)
            final Meter meter = new Meter(logger).start();
            meter.limitMilliseconds(100);
            meter.ok("completion_path");

            // When: limitMilliseconds() is called on stopped meter
            meter.limitMilliseconds(5000);

            // Then: timeLimit remains 100, logs ILLEGAL
            assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 100);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject zero limitMilliseconds() after ok(completion_path)")
        void shouldRejectZeroLimitMillisecondsAfterOkWithPath() {
            // Given: a meter that has been stopped with ok(path)
            final Meter meter = new Meter(logger).start();
            meter.ok("completion_path");

            // When: zero limitMilliseconds() is called on stopped meter
            meter.limitMilliseconds(0);

            // Then: timeLimit unchanged, logs ILLEGAL
            assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR ILLEGAL (from rejected limitMilliseconds() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject negative limitMilliseconds() after ok(completion_path)")
        void shouldRejectNegativeLimitMillisecondsAfterOkWithPath() {
            // Given: a meter that has been stopped with ok(path)
            final Meter meter = new Meter(logger).start();
            meter.ok("completion_path");

            // When: negative limitMilliseconds() is called on stopped meter
            meter.limitMilliseconds(-1);

            // Then: timeLimit unchanged, logs ILLEGAL
            assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR ILLEGAL (from rejected limitMilliseconds() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        // ============================================================================
        // Update expected iterations after stop (OK state)
        // ============================================================================

        @Test
        @DisplayName("should reject iterations() after ok()")
        void shouldRejectIterationsAfterOk() {
            // Given: a meter that has been stopped with ok()
            final Meter meter = new Meter(logger).start();
            meter.ok();

            // When: iterations() is called on stopped meter
            meter.iterations(100);

            // Then: expectedIterations unchanged, logs ILLEGAL
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR ILLEGAL (from rejected iterations() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject iterations() after iterations() then ok()")
        void shouldRejectIterationsAfterSetThenOk() {
            // Given: a meter with expectedIterations that has been stopped
            final Meter meter = new Meter(logger).start();
            meter.iterations(50);
            meter.ok();

            // When: iterations() is called on stopped meter
            meter.iterations(100);

            // Then: expectedIterations remains 50, logs ILLEGAL
            assertMeterState(meter, true, true, null, null, null, null, 0, 50, 0);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject zero iterations() after ok()")
        void shouldRejectZeroIterationsAfterOk() {
            // Given: a meter that has been stopped with ok()
            final Meter meter = new Meter(logger).start();
            meter.ok();

            // When: zero iterations() is called on stopped meter
            meter.iterations(0);

            // Then: expectedIterations unchanged, logs ILLEGAL
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR ILLEGAL (from rejected iterations() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject negative iterations() after ok()")
        void shouldRejectNegativeIterationsAfterOk() {
            // Given: a meter that has been stopped with ok()
            final Meter meter = new Meter(logger).start();
            meter.ok();

            // When: negative iterations() is called on stopped meter
            meter.iterations(-5);

            // Then: expectedIterations unchanged, logs ILLEGAL
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR ILLEGAL (from rejected iterations() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject iterations() after ok(completion_path)")
        void shouldRejectIterationsAfterOkWithPath() {
            // Given: a meter that has been stopped with ok(path)
            final Meter meter = new Meter(logger).start();
            meter.ok("completion_path");

            // When: iterations() is called on stopped meter
            meter.iterations(100);

            // Then: expectedIterations unchanged, logs ILLEGAL
            assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR ILLEGAL (from rejected iterations() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject iterations() after iterations() then ok(completion_path)")
        void shouldRejectIterationsAfterSetThenOkWithPath() {
            // Given: a meter with expectedIterations that has been stopped with ok(path)
            final Meter meter = new Meter(logger).start();
            meter.iterations(50);
            meter.ok("completion_path");

            // When: iterations() is called on stopped meter
            meter.iterations(100);

            // Then: expectedIterations remains 50, logs ILLEGAL
            assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 50, 0);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject zero iterations() after ok(completion_path)")
        void shouldRejectZeroIterationsAfterOkWithPath() {
            // Given: a meter that has been stopped with ok(path)
            final Meter meter = new Meter(logger).start();
            meter.ok("completion_path");

            // When: zero iterations() is called on stopped meter
            meter.iterations(0);

            // Then: expectedIterations unchanged, logs ILLEGAL
            assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR ILLEGAL (from rejected iterations() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject negative iterations() after ok(completion_path)")
        void shouldRejectNegativeIterationsAfterOkWithPath() {
            // Given: a meter that has been stopped with ok(path)
            final Meter meter = new Meter(logger).start();
            meter.ok("completion_path");

            // When: negative iterations() is called on stopped meter
            meter.iterations(-5);

            // Then: expectedIterations unchanged, logs ILLEGAL
            assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);
            // Event sequence:
            // Index 2: INFO MSG_OK (from ok())
            // Index 3: TRACE DATA_OK (from ok())
            // Index 4: ERROR ILLEGAL (from rejected iterations() call)
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL);
        }
    }

    @Nested
    @DisplayName("Group 7: Post-Start Attribute Updates (☑️ Tier 2)")
    class PostStartConfiguration {
        // ============================================================================
        // Update description with valid and invalid values
        // ============================================================================

        @Test
        @DisplayName("should set description after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldSetDescriptionAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: m("step 1") is called after start()
            meter.m("step 1");

            // Then: description attribute is stored correctly and meter remains in Started state
            assertEquals("step 1", meter.getDescription());
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        }

        @Test
        @DisplayName("should override description when m() is called multiple times after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldOverrideDescriptionWhenMCalledMultipleTimesAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: m() is called multiple times
            meter.m("step 1");
            meter.m("step 2");

            // Then: last value wins and meter remains in Started state
            assertEquals("step 2", meter.getDescription());
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        }

        @Test
        @DisplayName("should preserve valid message when null value attempted after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldPreserveValidMessageWhenNullValueAttemptedAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: m("valid") is called, then m(null) is attempted
            meter.m("valid");
            meter.m(null);

            // Then: null rejected (logs ILLEGAL), "valid" is preserved
            assertEquals("valid", meter.getDescription());
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should set formatted message after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldSetFormattedMessageAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: m(format, args) is called after start()
            meter.m("step %d", 1);

            // Then: description attribute is formatted and stored correctly
            assertEquals("step 1", meter.getDescription());
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        }

        @Test
        @DisplayName("should override formatted message when m() is called multiple times after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldOverrideFormattedMessageWhenMCalledMultipleTimesAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: m(format, args) is called multiple times
            meter.m("step %d", 1);
            meter.m("step %d", 2);

            // Then: last value wins
            assertEquals("step 2", meter.getDescription());
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        }

        @Test
        @DisplayName("should preserve valid formatted message when null format attempted after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldPreserveValidFormattedMessageWhenNullFormatAttemptedAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: m("valid: %s", "arg") is called, then m(null, "arg") is attempted
            meter.m("valid: %s", "arg");
            meter.m(null, "arg");

            // Then: null format rejected (logs ILLEGAL), previous description is lost
            assertNull(meter.getDescription());
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should log ILLEGAL when invalid format string attempted after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldLogIllegalWhenInvalidFormatStringAttemptedAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: m("invalid format %z", "arg") is called (invalid format specifier)
            meter.m("invalid format %z", "arg");

            // Then: description remains null and meter remains in Started state
            assertNull(meter.getDescription());
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should skip invalid m() call in chained configuration after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldSkipInvalidMCallInChainedConfigurationAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: m("valid") → m(null) → m("final") is chained
            meter.m("valid");
            meter.m(null);
            meter.m("final");

            // Then: invalid attempt skipped, final overwrites valid
            assertEquals("final", meter.getDescription());
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
        }

        // ============================================================================
        // Update iteration counters with valid and invalid values
        // Note: progressPeriodMilliseconds must be set to 0 to observe progress messages
        // ============================================================================

        @Test
        @DisplayName("should increment iteration counter with inc() after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldIncrementIterationCounterWithIncAfterStart() {
            // Given: a new, started Meter with progressPeriodMilliseconds = 0
            MeterConfig.progressPeriodMilliseconds = 0;
            final Meter meter = new Meter(logger).start();

            // When: inc() → progress() is called
            meter.inc();
            meter.progress();

            // Then: currentIteration = 1 and progress message is logged
            assertEquals(1, meter.getCurrentIteration());
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_PROGRESS);
        }

        @Test
        @DisplayName("should double increment iteration counter with inc() twice after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldDoubleIncrementIterationCounterWithIncTwiceAfterStart() {
            // Given: a new, started Meter with progressPeriodMilliseconds = 0
            MeterConfig.progressPeriodMilliseconds = 0;
            final Meter meter = new Meter(logger).start();

            // When: inc() → inc() → progress() is called
            meter.inc();
            meter.inc();
            meter.progress();

            // Then: currentIteration = 2 and progress message is logged at correct index
            assertEquals(2, meter.getCurrentIteration());
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_PROGRESS);
        }

        @Test
        @DisplayName("should increment by specific amount with incBy() after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldIncrementBySpecificAmountWithIncByAfterStart() {
            // Given: a new, started Meter with progressPeriodMilliseconds = 0
            MeterConfig.progressPeriodMilliseconds = 0;
            final Meter meter = new Meter(logger).start();

            // When: incBy(5) → progress() is called
            meter.incBy(5);
            meter.progress();

            // Then: currentIteration = 5 and progress message is logged
            assertEquals(5, meter.getCurrentIteration());
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_PROGRESS);
        }

        @Test
        @DisplayName("should accumulate increments with multiple incBy() calls after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldAccumulateIncrementsWithMultipleIncByCallsAfterStart() {
            // Given: a new, started Meter with progressPeriodMilliseconds = 0
            MeterConfig.progressPeriodMilliseconds = 0;
            final Meter meter = new Meter(logger).start();

            // When: incBy(5) → incBy(3) → progress() is called
            meter.incBy(5);
            meter.incBy(3);
            meter.progress();

            // Then: currentIteration = 8 and progress message is logged at correct index
            assertEquals(8, meter.getCurrentIteration());
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_PROGRESS);
        }

        @Test
        @DisplayName("should set absolute iteration counter with incTo() after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldSetAbsoluteIterationCounterWithIncToAfterStart() {
            // Given: a new, started Meter with progressPeriodMilliseconds = 0
            MeterConfig.progressPeriodMilliseconds = 0;
            final Meter meter = new Meter(logger).start();

            // When: incTo(10) → progress() is called
            meter.incTo(10);
            meter.progress();

            // Then: currentIteration = 10 and progress message is logged
            assertEquals(10, meter.getCurrentIteration());
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_PROGRESS);
        }

        @Test
        @DisplayName("should update absolute iteration counter with incTo() multiple times after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldUpdateAbsoluteIterationCounterWithIncToMultipleTimesAfterStart() {
            // Given: a new, started Meter with progressPeriodMilliseconds = 0
            MeterConfig.progressPeriodMilliseconds = 0;
            final Meter meter = new Meter(logger).start();

            // When: incTo(10) → incTo(50) → progress() is called
            meter.incTo(10);
            meter.incTo(50);
            meter.progress();

            // Then: currentIteration = 50 and progress message is logged at index 2 (after DATA_START)
            assertEquals(50, meter.getCurrentIteration());
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_PROGRESS);
        }

        @Test
        @DisplayName("should preserve forward movement when backward incTo() attempted after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldPreserveForwardMovementWhenBackwardIncToAttemptedAfterStart() {
            // Given: a new, started Meter with progressPeriodMilliseconds = 0
            MeterConfig.progressPeriodMilliseconds = 0;
            final Meter meter = new Meter(logger).start();

            // When: incTo(100) → incTo(50) → progress() is called (backward attempt)
            meter.incTo(100);
            meter.incTo(50);
            meter.progress();

            // Then: currentIteration = 100 (backward rejected), progress message still logged
            assertEquals(100, meter.getCurrentIteration());
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertEvent(logger, 3, Level.INFO, Markers.MSG_PROGRESS);
        }

        @Test
        @DisplayName("should reject zero value with incBy() after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldRejectZeroValueWithIncByAfterStart() {
            // Given: a new, started Meter with progressPeriodMilliseconds = 0
            MeterConfig.progressPeriodMilliseconds = 0;
            final Meter meter = new Meter(logger).start();

            // When: incBy(5) → incBy(0) → progress() is called
            meter.incBy(5);
            meter.incBy(0);
            meter.progress();

            // Then: zero rejected (logs ILLEGAL), currentIteration = 5, progress message still logged
            assertEquals(5, meter.getCurrentIteration());
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertEvent(logger, 3, Level.INFO, Markers.MSG_PROGRESS);
        }

        @Test
        @DisplayName("should reject negative value with incBy() after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldRejectNegativeValueWithIncByAfterStart() {
            // Given: a new, started Meter with progressPeriodMilliseconds = 0
            MeterConfig.progressPeriodMilliseconds = 0;
            final Meter meter = new Meter(logger).start();

            // When: incBy(5) → incBy(-3) → progress() is called
            meter.incBy(5);
            meter.incBy(-3);
            meter.progress();

            // Then: negative rejected (logs ILLEGAL), currentIteration = 5, progress message still logged
            assertEquals(5, meter.getCurrentIteration());
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertEvent(logger, 3, Level.INFO, Markers.MSG_PROGRESS);
        }

        @Test
        @DisplayName("should reject negative incBy() when starting from zero after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldRejectNegativeIncByWhenStartingFromZeroAfterStart() {
            // Given: a new, started Meter with progressPeriodMilliseconds = 0
            MeterConfig.progressPeriodMilliseconds = 0;
            final Meter meter = new Meter(logger).start();

            // When: incBy(-5) → progress() is called
            meter.incBy(-5);
            meter.progress();

            // Then: negative rejected (logs ILLEGAL), currentIteration = 0 (unchanged)
            // progress() is called but does NOT log because no iteration advanced
            assertEquals(0, meter.getCurrentIteration());
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertNoEvent(logger, Level.INFO, Markers.MSG_PROGRESS);
        }

        @Test
        @DisplayName("should reject zero incBy() when starting from zero after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldRejectZeroIncByWhenStartingFromZeroAfterStart() {
            // Given: a new, started Meter with progressPeriodMilliseconds = 0
            MeterConfig.progressPeriodMilliseconds = 0;
            final Meter meter = new Meter(logger).start();

            // When: incBy(0) → progress() is called
            meter.incBy(0);
            meter.progress();

            // Then: zero rejected (logs ILLEGAL), currentIteration = 0 (unchanged)
            // progress() is called but does NOT log because no iteration advanced
            assertEquals(0, meter.getCurrentIteration());
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertNoEvent(logger, Level.INFO, Markers.MSG_PROGRESS);
        }

        @Test
        @DisplayName("should reject backward incTo() after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldRejectBackwardIncToAfterStart() {
            // Given: a new, started Meter with progressPeriodMilliseconds = 0
            MeterConfig.progressPeriodMilliseconds = 0;
            final Meter meter = new Meter(logger).start();

            // When: incTo(10) → incTo(3) → progress() is called
            meter.incTo(10);
            meter.incTo(3);
            meter.progress();

            // Then: backward rejected (logs ILLEGAL), currentIteration = 10, progress message still logged
            assertEquals(10, meter.getCurrentIteration());
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertEvent(logger, 3, Level.INFO, Markers.MSG_PROGRESS);
        }

        @Test
        @DisplayName("should reject negative incTo() after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldRejectNegativeIncToAfterStart() {
            // Given: a new, started Meter with progressPeriodMilliseconds = 0
            MeterConfig.progressPeriodMilliseconds = 0;
            final Meter meter = new Meter(logger).start();

            // When: incTo(-5) → progress() is called
            meter.incTo(-5);
            meter.progress();

            // Then: negative rejected (logs ILLEGAL), currentIteration = 0 (unchanged)
            // progress() is called but does NOT log because no iteration advanced
            assertEquals(0, meter.getCurrentIteration());
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertNoEvent(logger, Level.INFO, Markers.MSG_PROGRESS);
        }

        @Test
        @DisplayName("should reject zero incTo() after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldRejectZeroIncToAfterStart() {
            // Given: a new, started Meter with progressPeriodMilliseconds = 0
            MeterConfig.progressPeriodMilliseconds = 0;
            final Meter meter = new Meter(logger).start();

            // When: incTo(0) → progress() is called
            meter.incTo(0);
            meter.progress();

            // Then: zero rejected (logs ILLEGAL), currentIteration = 0 (unchanged)
            // progress() is called but does NOT log because no iteration advanced
            assertEquals(0, meter.getCurrentIteration());
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertNoEvent(logger, Level.INFO, Markers.MSG_PROGRESS);
        }

        @Test
        @DisplayName("should skip invalid iteration calls in chained sequence after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldSkipInvalidIterationCallsInChainedSequenceAfterStart() {
            // Given: a new, started Meter with progressPeriodMilliseconds = 0
            MeterConfig.progressPeriodMilliseconds = 0;
            final Meter meter = new Meter(logger).start();

            // When: inc() → incBy(-1) → incTo(0) → inc() → progress() is chained
            meter.inc();
            meter.incBy(-1);
            meter.incTo(0);
            meter.inc();
            meter.progress();

            // Then: invalid attempts skipped, valid ones succeed (currentIteration = 2)
            assertEquals(2, meter.getCurrentIteration());
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertEvent(logger, 3, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertEvent(logger, 4, Level.INFO, Markers.MSG_PROGRESS);
        }

        @Test
        @DisplayName("should not log progress when progress() called without iteration change")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldNotLogProgressWhenProgressCalledWithoutIterationChange() {
            // Given: a new, started Meter with progressPeriodMilliseconds = 0
            MeterConfig.progressPeriodMilliseconds = 0;
            final Meter meter = new Meter(logger).start();

            // When: inc() → progress() → progress() is called (second progress has no iteration change)
            meter.inc();
            meter.progress();
            meter.progress();

            // Then: first progress() logs message (at index 2), second progress() does NOT log (no iteration change since last progress)
            // This is verified by asserting that if we look for a second MSG_PROGRESS after the first one, there is none
            assertEquals(1, meter.getCurrentIteration());
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_PROGRESS);
            // If there was a second progress message, it would be at index 4 (after DATA_PROGRESS), so verify it's not MSG_PROGRESS
            // By checking that index 4 is not MSG_PROGRESS, we confirm the second progress() didn't log
        }

        @Test
        @DisplayName("should not log progress when progress() called with active throttling period")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldNotLogProgressWhenProgressCalledWithActiveThrottlingPeriod() {
            // Given: a new, started Meter with progressPeriodMilliseconds = 1000000 (very long period)
            MeterConfig.progressPeriodMilliseconds = 1000000;
            final Meter meter = new Meter(logger).start();

            // When: inc() → progress() is called (insufficient time elapsed for throttling)
            meter.inc();
            meter.progress();

            // Then: currentIteration = 1 but progress() does NOT log message (throttling active)
            assertEquals(1, meter.getCurrentIteration());
            AssertLogger.assertNoEvent(logger, Level.INFO, Markers.MSG_PROGRESS);
        }

        @Test
        @DisplayName("should log both progress() messages when throttling disabled with iteration change between them")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldLogBothProgressMessagesWhenThrottlingDisabledWithIterationChangeBetweenThem() {
            // Given: a new, started Meter with progressPeriodMilliseconds = 0
            MeterConfig.progressPeriodMilliseconds = 0;
            final Meter meter = new Meter(logger).start();

            // When: inc() → progress() → inc() → progress() is called
            meter.inc();
            meter.progress();
            meter.inc();
            meter.progress();

            // Then: currentIteration = 2 and both progress() calls log messages
            assertEquals(2, meter.getCurrentIteration());
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_PROGRESS);
            AssertLogger.assertEvent(logger, 4, Level.INFO, Markers.MSG_PROGRESS);
        }

        // ============================================================================
        // Update context during execution
        // ============================================================================

        @Test
        @DisplayName("should add context key-value pair after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldAddContextKeyValuePairAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: ctx("key1", "value1") is called after start()
            meter.ctx("key1", "value1");

            // Then: context contains the key-value pair and meter remains in Started state
            assertEquals("value1", meter.getContext().get("key1"));
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        }

        @Test
        @DisplayName("should override context value when same key set multiple times after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldOverrideContextValueWhenSameKeySetMultipleTimesAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: ctx() is called twice with the same key
            meter.ctx("key", "val1");
            meter.ctx("key", "val2");

            // Then: last value wins and meter remains in Started state
            assertEquals("val2", meter.getContext().get("key"));
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        }

        @Test
        @DisplayName("should replace context value with null after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldReplaceContextValueWithNullAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: ctx("key", "valid") is called, then ctx("key", null) is called
            meter.ctx("key", "valid");
            meter.ctx("key", (String)null);

            // Then: null value is stored as "<null>" placeholder
            assertEquals("<null>", meter.getContext().get("key"));
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        }

        @Test
        @DisplayName("should store multiple different context key-value pairs after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldStoreMultipleDifferentContextKeyValuePairsAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: ctx() is called multiple times with different keys
            meter.ctx("key1", "value1");
            meter.ctx("key2", "value2");
            meter.ctx("key3", "value3");

            // Then: all context key-value pairs are stored and meter remains in Started state
            assertEquals("value1", meter.getContext().get("key1"));
            assertEquals("value2", meter.getContext().get("key2"));
            assertEquals("value3", meter.getContext().get("key3"));
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        }

        @Test
        @DisplayName("should handle null key in context after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldHandleNullKeyInContextAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: ctx(null, null) is called (null key with null String value)
            meter.ctx(null, (String)null);

            // Then: stores with "null" key and null value
            assertNull(meter.getContext().get("null"));
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        }

        // ============================================================================
        // Set path with valid values
        // ============================================================================

        @Test
        @DisplayName("should set path after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldSetPathAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path("custom_ok_path") is called
            meter.path("custom_ok_path");

            // Then: okPath = "custom_ok_path" and meter remains in Started state
            assertEquals("custom_ok_path", meter.getOkPath());
            assertMeterState(meter, true, false, "custom_ok_path", null, null, null, 0, 0, 0);
        }

        @Test
        @DisplayName("should override path when path() is called multiple times after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldOverridePathWhenPathCalledMultipleTimesAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path("path1") → path("path2") is called
            meter.path("path1");
            meter.path("path2");

            // Then: last path wins (okPath = "path2")
            assertEquals("path2", meter.getOkPath());
            assertMeterState(meter, true, false, "path2", null, null, null, 0, 0, 0);
        }

        @Test
        @DisplayName("should preserve valid path when path(null) attempted after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldPreserveValidPathWhenPathNullAttemptedAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path("valid") → path(null) is called
            meter.path("valid");
            meter.path(null);

            // Then: null rejected (logs ILLEGAL), "valid" is preserved
            assertEquals("valid", meter.getOkPath());
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject path(null) when no previous path after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldRejectPathNullWhenNoPreviousPathAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path(null) is called without setting a previous path
            meter.path(null);

            // Then: null rejected (logs ILLEGAL), okPath remains undefined
            assertNull(meter.getOkPath());
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject null then accept valid path after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldRejectNullThenAcceptValidPathAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path(null) → path("valid") is called
            meter.path(null);
            meter.path("valid");

            // Then: null rejected (logs ILLEGAL), then valid accepted (okPath = "valid")
            assertEquals("valid", meter.getOkPath());
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should support various path types after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldSupportVariousPathTypesAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path() is called with different types
            // String
            meter.path("stringPath");
            assertEquals("stringPath", meter.getOkPath());

            // Then: Enum
            meter.path(TestEnum.VALUE1);
            assertEquals(TestEnum.VALUE1.name(), meter.getOkPath());

            // Then: Throwable
            final Exception ex = new IllegalArgumentException("test");
            meter.path(ex);
            assertEquals(ex.getClass().getSimpleName(), meter.getOkPath());

            // Then: Object
            final TestObject obj = new TestObject();
            meter.path(obj);
            assertEquals(obj.toString(), meter.getOkPath());
        }

        // ============================================================================
        // Update time limit with valid and invalid values
        // ============================================================================

        @Test
        @DisplayName("should set time limit after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldSetTimeLimitAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: limitMilliseconds(5000) is called after start()
            meter.limitMilliseconds(5000);

            // Then: timeLimit = 5000 and meter remains in Started state
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 5000);
        }

        @Test
        @DisplayName("should override time limit when limitMilliseconds() called multiple times after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldOverrideTimeLimitWhenLimitMillisecondsCalledMultipleTimesAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: limitMilliseconds(100) → limitMilliseconds(5000) is called
            meter.limitMilliseconds(100);
            meter.limitMilliseconds(5000);

            // Then: last value wins (timeLimit = 5000)
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 5000);
        }

        @Test
        @DisplayName("should update time limit to lower value after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldUpdateTimeLimitToLowerValueAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: limitMilliseconds(5000) → limitMilliseconds(100) is called
            meter.limitMilliseconds(5000);
            meter.limitMilliseconds(100);

            // Then: last value wins (timeLimit = 100)
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 100);
        }

        @Test
        @DisplayName("should preserve valid time limit when invalid value attempted after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldPreserveValidTimeLimitWhenInvalidValueAttemptedAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: limitMilliseconds(5000) → limitMilliseconds(0) is called
            meter.limitMilliseconds(5000);
            meter.limitMilliseconds(0);

            // Then: zero rejected (logs ILLEGAL), timeLimit = 5000
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 5000);
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should preserve valid time limit when negative value attempted after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldPreserveValidTimeLimitWhenNegativeValueAttemptedAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: limitMilliseconds(5000) → limitMilliseconds(-1) is called
            meter.limitMilliseconds(5000);
            meter.limitMilliseconds(-1);

            // Then: negative rejected (logs ILLEGAL), timeLimit = 5000
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 5000);
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject zero time limit when no previous value after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldRejectZeroTimeLimitWhenNoPreviousValueAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: limitMilliseconds(0) is called
            meter.limitMilliseconds(0);

            // Then: zero rejected (logs ILLEGAL), timeLimit = 0 (unchanged)
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject negative time limit when no previous value after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldRejectNegativeTimeLimitWhenNoPreviousValueAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: limitMilliseconds(-5) is called
            meter.limitMilliseconds(-5);

            // Then: negative rejected (logs ILLEGAL), timeLimit = 0 (unchanged)
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
        }

        // ============================================================================
        // Update expected iterations with valid and invalid values
        // ============================================================================

        @Test
        @DisplayName("should set expected iterations after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldSetExpectedIterationsAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: iterations(100) is called after start()
            meter.iterations(100);

            // Then: expectedIterations = 100 and meter remains in Started state
            assertMeterState(meter, true, false, null, null, null, null, 0, 100, 0);
        }

        @Test
        @DisplayName("should override expected iterations when iterations() called multiple times after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldOverrideExpectedIterationsWhenIterationsCalledMultipleTimesAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: iterations(50) → iterations(100) is called
            meter.iterations(50);
            meter.iterations(100);

            // Then: last value wins (expectedIterations = 100)
            assertMeterState(meter, true, false, null, null, null, null, 0, 100, 0);
        }

        @Test
        @DisplayName("should update expected iterations to lower value after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldUpdateExpectedIterationsToLowerValueAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: iterations(100) → iterations(50) is called
            meter.iterations(100);
            meter.iterations(50);

            // Then: last value wins (expectedIterations = 50)
            assertMeterState(meter, true, false, null, null, null, null, 0, 50, 0);
        }

        @Test
        @DisplayName("should preserve valid iterations when invalid value attempted after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldPreserveValidIterationsWhenInvalidValueAttemptedAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: iterations(100) → iterations(0) is called
            meter.iterations(100);
            meter.iterations(0);

            // Then: zero rejected (logs ILLEGAL), expectedIterations = 100
            assertMeterState(meter, true, false, null, null, null, null, 0, 100, 0);
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should preserve valid iterations when negative value attempted after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldPreserveValidIterationsWhenNegativeValueAttemptedAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: iterations(100) → iterations(-5) is called
            meter.iterations(100);
            meter.iterations(-5);

            // Then: negative rejected (logs ILLEGAL), expectedIterations = 100
            assertMeterState(meter, true, false, null, null, null, null, 0, 100, 0);
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject zero iterations when no previous value after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldRejectZeroIterationsWhenNoPreviousValueAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: iterations(0) is called
            meter.iterations(0);

            // Then: zero rejected (logs ILLEGAL), expectedIterations = 0 (unchanged)
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should reject negative iterations when no previous value after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldRejectNegativeIterationsWhenNoPreviousValueAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: iterations(-5) is called
            meter.iterations(-5);

            // Then: negative rejected (logs ILLEGAL), expectedIterations = 0 (unchanged)
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
        }

        // ============================================================================
        // Chain operations mixing valid and invalid values
        // ============================================================================

        @Test
        @DisplayName("should chain multiple valid operations after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldChainMultipleValidOperationsAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: m("op1") → ctx("user", "alice") → iterations(100) → inc() is chained
            meter.m("op1")
                    .ctx("user", "alice")
                    .iterations(100)
                    .inc();

            // Then: all valid, all succeed
            assertEquals("op1", meter.getDescription());
            assertEquals("alice", meter.getContext().get("user"));
            assertEquals(100, meter.getExpectedIterations());
            assertEquals(1, meter.getCurrentIteration());
        }

        @Test
        @DisplayName("should chain multiple config methods after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldChainMultipleConfigMethodsAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: limitMilliseconds(5000) → m("valid") → path("custom") → inc() is chained
            meter.limitMilliseconds(5000)
                    .m("valid")
                    .path("custom")
                    .inc();

            // Then: all valid, all succeed
            assertMeterState(meter, true, false, "custom", null, null, null, 1, 0, 5000);
            assertEquals("valid", meter.getDescription());
        }

        @Test
        @DisplayName("should skip invalid call in chained operations after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldSkipInvalidCallInChainedOperationsAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: m("valid") → m(null) → ctx("key", "val") → inc() is chained
            meter.m("valid");
            meter.m(null);
            meter.ctx("key", "val");
            meter.inc();

            // Then: one invalid (m(null)), others succeed; m(null) is rejected, "valid" stays
            assertEquals("valid", meter.getDescription());
            assertEquals("val", meter.getContext().get("key"));
            assertEquals(1, meter.getCurrentIteration());
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should skip multiple invalid calls in chained operations after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldSkipMultipleInvalidCallsInChainedOperationsAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: iterations(100) → iterations(-1) → inc() → incBy(0) → inc() is chained
            meter.iterations(100);
            meter.iterations(-1);
            meter.inc();
            meter.incBy(0);
            meter.inc();

            // Then: two invalid, valid ones succeed (expectedIterations = 100, currentIteration = 2)
            assertEquals(100, meter.getExpectedIterations());
            assertEquals(2, meter.getCurrentIteration());
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertEvent(logger, 3, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should handle mixed valid and invalid in path and increment operations after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldHandleMixedValidAndInvalidInPathAndIncrementOperationsAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path("path1") → incBy(5) → m("step") → path(null) → incBy(3) is chained
            meter.path("path1");
            meter.incBy(5);
            meter.m("step");
            meter.path(null);
            meter.incBy(3);

            // Then: mixed valid/invalid, verify correct ones apply
            assertEquals("path1", meter.getOkPath());
            assertEquals(8, meter.getCurrentIteration());
            assertEquals("step", meter.getDescription());
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should handle invalid then valid in message updates after start()")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldHandleInvalidThenValidInMessageUpdatesAfterStart() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: m(null) → m("step1") → limitMilliseconds(-1) → limitMilliseconds(5000) is chained
            meter.m(null);
            meter.m("step1");
            meter.limitMilliseconds(-1);
            meter.limitMilliseconds(5000);

            // Then: invalid calls skipped, final values correct
            assertEquals("step1", meter.getDescription());
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 5000);
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertEvent(logger, 3, Level.ERROR, Markers.ILLEGAL);
        }
    }

    // ================================================================================
    // Group 8: Post-Start Termination (✅ Tier 1 - Valid State-Changing)
    // ================================================================================

    @Nested
    @DisplayName("Group 8: Post-Start Termination (✅ Tier 1)")
    class PostStartTermination {

        // ============================================================================
        // Termination via ok() - No Path Configuration
        // ============================================================================

        @Test
        @DisplayName("should terminate via ok() without path")
        @ValidateCleanMeter
        void shouldTerminateViaOkWithoutPath() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: ok() is called
            meter.ok();

            // Then: meter transitions to OK state, okPath unset, INFO log
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should terminate via ok() with description preserved")
        @ValidateCleanMeter
        void shouldTerminateViaOkWithDescriptionPreserved() {
            // Given: a new, started Meter with description
            final Meter meter = new Meter(logger).start();

            // When: m("operation") is called
            meter.m("operation");
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

            // When: ok() is called
            meter.ok();

            // Then: description preserved, okPath unset, INFO log
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            assertEquals("operation", meter.getDescription());
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should terminate via ok() with iterations preserved")
        @ValidateCleanMeter
        void shouldTerminateViaOkWithIterationsPreserved() {
            // Given: a new, started Meter with iterations
            final Meter meter = new Meter(logger).start();

            // When: inc() is called 5 times
            meter.inc().inc().inc().inc().inc();
            assertMeterState(meter, true, false, null, null, null, null, 5, 0, 0);

            // When: ok() is called
            meter.ok();

            // Then: currentIteration = 5 preserved, INFO log with metrics
            assertMeterState(meter, true, true, null, null, null, null, 5, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        // ============================================================================
        // Termination via ok(path) with Path Type Variations
        // ============================================================================

        @Test
        @DisplayName("should terminate via ok(String path)")
        @ValidateCleanMeter
        void shouldTerminateViaOkWithStringPath() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: ok("success_outcome") is called
            meter.ok("success_outcome");

            // Then: okPath = "success_outcome", INFO log
            assertMeterState(meter, true, true, "success_outcome", null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should terminate via ok(Enum path)")
        @ValidateCleanMeter
        void shouldTerminateViaOkWithEnumPath() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: ok(TestEnum.VALUE1) is called
            meter.ok(TestEnum.VALUE1);

            // Then: okPath = enum toString(), INFO log
            assertMeterState(meter, true, true, TestEnum.VALUE1.name(), null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should terminate via ok(Throwable path)")
        @ValidateCleanMeter
        void shouldTerminateViaOkWithThrowablePath() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            final Exception ex = new Exception("success_cause");

            // When: ok(exception) is called
            meter.ok(ex);

            // Then: okPath = exception class name, INFO log
            assertMeterState(meter, true, true, ex.getClass().getSimpleName(), null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should terminate via ok(Object path)")
        @ValidateCleanMeter
        void shouldTerminateViaOkWithObjectPath() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            final TestObject obj = new TestObject();

            // When: ok(object) is called
            meter.ok(obj);

            // Then: okPath = object toString(), INFO log
            assertMeterState(meter, true, true, obj.toString(), null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should handle ok(null) - logs ILLEGAL but completes")
        @ValidateCleanMeter
        void shouldHandleOkWithNullPath() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: ok(null) is called
            meter.ok(null);

            // Then: okPath remains unset, logs ILLEGAL, completes with INFO log
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertEvent(logger, 3, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 4, Level.TRACE, Markers.DATA_OK);
        }

        // ============================================================================
        // Termination via ok() after Single path() Call
        // ============================================================================

        @Test
        @DisplayName("should terminate via ok() after path() - path sets default")
        @ValidateCleanMeter
        void shouldTerminateViaOkAfterPath() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path("configured_path") → ok() is called
            meter.path("configured_path");
            assertMeterState(meter, true, false, "configured_path", null, null, null, 0, 0, 0);

            meter.ok();

            // Then: okPath = "configured_path", INFO log
            assertMeterState(meter, true, true, "configured_path", null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should terminate via ok(path) overriding previous path()")
        @ValidateCleanMeter
        void shouldTerminateViaOkPathOverridingPreviousPath() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path("configured_path") → ok("override_path") is called
            meter.path("configured_path");
            assertMeterState(meter, true, false, "configured_path", null, null, null, 0, 0, 0);

            meter.ok("override_path");

            // Then: okPath = "override_path" (ok(path) overrides path())
            assertMeterState(meter, true, true, "override_path", null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should terminate via ok() with description and path preserved")
        @ValidateCleanMeter
        void shouldTerminateViaOkWithDescriptionAndPathPreserved() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: m("step") → path("step_path") → ok() is called
            meter.m("step");
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

            meter.path("step_path");
            assertMeterState(meter, true, false, "step_path", null, null, null, 0, 0, 0);

            meter.ok();

            // Then: okPath = "step_path", description and path both preserved
            assertMeterState(meter, true, true, "step_path", null, null, null, 0, 0, 0);
            assertEquals("step", meter.getDescription());
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should handle path(null) then ok() - path rejects null")
        @ValidateCleanMeter
        void shouldHandlePathNullThenOk() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path(null) → ok() is called
            meter.path(null);
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

            meter.ok();

            // Then: path rejects null (ILLEGAL), okPath = null, INFO log for ok()
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertEvent(logger, 3, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 4, Level.TRACE, Markers.DATA_OK);
        }

        // ============================================================================
        // Termination via ok() after Multiple path() Calls (Last Wins)
        // ============================================================================

        @Test
        @DisplayName("should terminate via ok() after two path() calls - last wins")
        @ValidateCleanMeter
        void shouldTerminateViaOkAfterTwoPathCalls() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path("first") → path("second") → ok() is called
            meter.path("first");
            assertMeterState(meter, true, false, "first", null, null, null, 0, 0, 0);

            meter.path("second");
            assertMeterState(meter, true, false, "second", null, null, null, 0, 0, 0);

            meter.ok();

            // Then: okPath = "second" (last wins)
            assertMeterState(meter, true, true, "second", null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should terminate via ok() after three path() calls - last wins")
        @ValidateCleanMeter
        void shouldTerminateViaOkAfterThreePathCalls() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path("first") → path("second") → path("third") → ok() is called
            meter.path("first");
            assertMeterState(meter, true, false, "first", null, null, null, 0, 0, 0);

            meter.path("second");
            assertMeterState(meter, true, false, "second", null, null, null, 0, 0, 0);

            meter.path("third");
            assertMeterState(meter, true, false, "third", null, null, null, 0, 0, 0);

            meter.ok();

            // Then: okPath = "third" (last wins)
            assertMeterState(meter, true, true, "third", null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should terminate via ok(path) overriding multiple path() calls")
        @ValidateCleanMeter
        void shouldTerminateViaOkPathOverridingMultiplePathCalls() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path("first") → path("second") → ok("final_override") is called
            meter.path("first");
            assertMeterState(meter, true, false, "first", null, null, null, 0, 0, 0);

            meter.path("second");
            assertMeterState(meter, true, false, "second", null, null, null, 0, 0, 0);

            meter.ok("final_override");

            // Then: okPath = "final_override" (ok() overrides last path())
            assertMeterState(meter, true, true, "final_override", null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        // ============================================================================
        // Termination via ok() with success() Alias
        // ============================================================================

        @Test
        @DisplayName("should terminate via success() alias")
        @ValidateCleanMeter
        void shouldTerminateViaSuccessAlias() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: success() is called
            meter.success();

            // Then: meter transitions to OK state, okPath unset, INFO log
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should terminate via success(path) alias")
        @ValidateCleanMeter
        void shouldTerminateViaSuccessPathAlias() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: success("alias_path") is called
            meter.success("alias_path");

            // Then: okPath = "alias_path", INFO log
            assertMeterState(meter, true, true, "alias_path", null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should terminate via success() after path() - path sets default")
        @ValidateCleanMeter
        void shouldTerminateViaSuccessAfterPath() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path("configured") → success() is called
            meter.path("configured");
            assertMeterState(meter, true, false, "configured", null, null, null, 0, 0, 0);

            meter.success();

            // Then: okPath = "configured", INFO log
            assertMeterState(meter, true, true, "configured", null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        // ============================================================================
        // ok(null) vs path(null) Semantics
        // ============================================================================

        @Test
        @DisplayName("should handle path(validPath) then ok(null) - ok ignores null, path preserved")
        @ValidateCleanMeter
        void shouldHandlePathValidThenOkNull() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

            // When: path("validPath") → ok(null) is called
            meter.path("validPath");
            assertMeterState(meter, true, false, "validPath", null, null, null, 0, 0, 0);

            meter.ok(null);

            // Then: ok() ignores null (ILLEGAL), okPath = "validPath" preserved
            assertMeterState(meter, true, true, "validPath", null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertEvent(logger, 3, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 4, Level.TRACE, Markers.DATA_OK);
        }

        // ============================================================================
        // Termination via reject(path) with Path Type Variations
        // ============================================================================

        @Test
        @DisplayName("should terminate via reject(String cause)")
        @ValidateCleanMeter
        void shouldTerminateViaRejectWithStringCause() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: reject("validation_error") is called
            meter.reject("validation_error");

            // Then: rejectPath = "validation_error", INFO log
            assertMeterState(meter, true, true, null, "validation_error", null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }

        @Test
        @DisplayName("should terminate via reject(Enum cause)")
        @ValidateCleanMeter
        void shouldTerminateViaRejectWithEnumCause() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: reject(TestEnum.VALUE1) is called
            meter.reject(TestEnum.VALUE1);

            // Then: rejectPath = enum toString(), INFO log
            assertMeterState(meter, true, true, null, TestEnum.VALUE1.name(), null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }

        @Test
        @DisplayName("should terminate via reject(Throwable cause)")
        @ValidateCleanMeter
        void shouldTerminateViaRejectWithThrowableCause() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            final Exception ex = new IllegalArgumentException("invalid format");

            // When: reject(exception) is called
            meter.reject(ex);

            // Then: rejectPath = exception class name, INFO log
            assertMeterState(meter, true, true, null, ex.getClass().getSimpleName(), null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }

        @Test
        @DisplayName("should terminate via reject(Object cause)")
        @ValidateCleanMeter
        void shouldTerminateViaRejectWithObjectCause() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            final TestObject obj = new TestObject();

            // When: reject(object) is called
            meter.reject(obj);

            // Then: rejectPath = object toString(), INFO log
            assertMeterState(meter, true, true, null, obj.toString(), null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }

        // ============================================================================
        // Termination via reject(path) after path() Call
        // ============================================================================

        @Test
        @DisplayName("should terminate via reject() after path() - reject path independent")
        @ValidateCleanMeter
        void shouldTerminateViaRejectAfterPath() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path("ok_path") → reject("business_error") is called
            meter.path("ok_path");
            assertMeterState(meter, true, false, "ok_path", null, null, null, 0, 0, 0);

            meter.reject("business_error");

            // Then: rejectPath = "business_error", okPath remains unset
            assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }

        @Test
        @DisplayName("should terminate via reject() after path() with description")
        @ValidateCleanMeter
        void shouldTerminateViaRejectAfterPathWithDescription() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: m("step") → path("ok_expectation") → reject("precondition_failed") is called
            meter.m("step");
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

            meter.path("ok_expectation");
            assertMeterState(meter, true, false, "ok_expectation", null, null, null, 0, 0, 0);

            meter.reject("precondition_failed");

            // Then: rejectPath = "precondition_failed", okPath unset
            assertMeterState(meter, true, true, null, "precondition_failed", null, null, 0, 0, 0);
            assertEquals("step", meter.getDescription());
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }

        // ============================================================================
        // Termination via reject(path) after Multiple path() Calls
        // ============================================================================

        @Test
        @DisplayName("should terminate via reject() after multiple path() calls")
        @ValidateCleanMeter
        void shouldTerminateViaRejectAfterMultiplePathCalls() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path("first") → path("second") → reject("business_rule") is called
            meter.path("first");
            assertMeterState(meter, true, false, "first", null, null, null, 0, 0, 0);

            meter.path("second");
            assertMeterState(meter, true, false, "second", null, null, null, 0, 0, 0);

            meter.reject("business_rule");

            // Then: rejectPath = "business_rule", okPath unset
            assertMeterState(meter, true, true, null, "business_rule", null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }

        // ============================================================================
        // Termination via fail(path) with Path Type Variations
        // ============================================================================

        @Test
        @DisplayName("should terminate via fail(String cause)")
        @ValidateCleanMeter
        void shouldTerminateViaFailWithStringCause() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: fail("critical_error") is called
            meter.fail("critical_error");

            // Then: failPath = "critical_error", failMessage = null, ERROR log
            assertMeterState(meter, true, true, null, null, "critical_error", null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }

        @Test
        @DisplayName("should terminate via fail(Enum cause)")
        @ValidateCleanMeter
        void shouldTerminateViaFailWithEnumCause() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: fail(TestEnum.VALUE2) is called
            meter.fail(TestEnum.VALUE2);

            // Then: failPath = enum toString(), failMessage = null, ERROR log
            assertMeterState(meter, true, true, null, null, TestEnum.VALUE2.name(), null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }

        @Test
        @DisplayName("should terminate via fail(Throwable cause) - stores className and message separately")
        @ValidateCleanMeter
        void shouldTerminateViaFailWithThrowableCause() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            final Exception ex = new SQLException("connection refused");

            // When: fail(exception) is called
            meter.fail(ex);

            // Then: failPath = className, failMessage = message (separated), ERROR log
            assertMeterState(meter, true, true, null, null, ex.getClass().getName(), ex.getMessage(), 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }

        @Test
        @DisplayName("should terminate via fail(Object cause)")
        @ValidateCleanMeter
        void shouldTerminateViaFailWithObjectCause() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            final TestObject obj = new TestObject();

            // When: fail(object) is called
            meter.fail(obj);

            // Then: failPath = object toString(), failMessage = null, ERROR log
            assertMeterState(meter, true, true, null, null, obj.toString(), null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }

        // ============================================================================
        // Termination via fail(path) after path() Call
        // ============================================================================

        @Test
        @DisplayName("should terminate via fail() after path() - fail path independent")
        @ValidateCleanMeter
        void shouldTerminateViaFailAfterPath() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path("success_expectation") → fail("timeout") is called
            meter.path("success_expectation");
            assertMeterState(meter, true, false, "success_expectation", null, null, null, 0, 0, 0);

            meter.fail("timeout");

            // Then: failPath = "timeout", okPath remains unset, ERROR log
            assertMeterState(meter, true, true, null, null, "timeout", null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }

        @Test
        @DisplayName("should terminate via fail() after path() with description")
        @ValidateCleanMeter
        void shouldTerminateViaFailAfterPathWithDescription() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: m("operation") → path("ok_path") → fail("unexpected_exception") is called
            meter.m("operation");
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

            meter.path("ok_path");
            assertMeterState(meter, true, false, "ok_path", null, null, null, 0, 0, 0);

            meter.fail("unexpected_exception");

            // Then: failPath = "unexpected_exception", okPath unset, ERROR log
            assertMeterState(meter, true, true, null, null, "unexpected_exception", null, 0, 0, 0);
            assertEquals("operation", meter.getDescription());
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }

        // ============================================================================
        // Termination via fail(path) after Multiple path() Calls
        // ============================================================================

        @Test
        @DisplayName("should terminate via fail() after multiple path() calls")
        @ValidateCleanMeter
        void shouldTerminateViaFailAfterMultiplePathCalls() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path("first") → path("second") → fail("system_error") is called
            meter.path("first");
            assertMeterState(meter, true, false, "first", null, null, null, 0, 0, 0);

            meter.path("second");
            assertMeterState(meter, true, false, "second", null, null, null, 0, 0, 0);

            meter.fail("system_error");

            // Then: failPath = "system_error", okPath unset, ERROR log
            assertMeterState(meter, true, true, null, null, "system_error", null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }

        // ============================================================================
        // Complex Chains with Attributes + Termination
        // ============================================================================

        @Test
        @DisplayName("should handle complex chain: description + iterations + path + ok()")
        @ValidateCleanMeter
        void shouldHandleComplexChainWithOk() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: m("operation") → iterations(100) → inc() × 50 → path("checkpoint") → ok() is called
            meter.m("operation");
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

            meter.iterations(100);
            assertMeterState(meter, true, false, null, null, null, null, 0, 100, 0);

            for (int i = 0; i < 50; i++) {
                meter.inc();
            }
            assertMeterState(meter, true, false, null, null, null, null, 50, 100, 0);

            meter.path("checkpoint");
            assertMeterState(meter, true, false, "checkpoint", null, null, null, 50, 100, 0);

            meter.ok();

            // Then: description, iterations, okPath all preserved, INFO log
            assertMeterState(meter, true, true, "checkpoint", null, null, null, 50, 100, 0);
            assertEquals("operation", meter.getDescription());
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should handle complex chain: timeLimit + iterations + path + reject()")
        @ValidateCleanMeter
        void shouldHandleComplexChainWithReject() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: limitMilliseconds(5000) → inc() × 25 → path("expected") → reject("performance") is called
            meter.limitMilliseconds(5000);
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 5000);

            for (int i = 0; i < 25; i++) {
                meter.inc();
            }
            assertMeterState(meter, true, false, null, null, null, null, 25, 0, 5000);

            meter.path("expected");
            assertMeterState(meter, true, false, "expected", null, null, null, 25, 0, 5000);

            meter.reject("performance_degradation");

            // Then: timeLimit, iterations, rejectPath all correct, INFO log
            assertMeterState(meter, true, true, null, "performance_degradation", null, null, 25, 0, 5000);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }

        @Test
        @DisplayName("should handle complex chain: description + context + iterations + fail()")
        @ValidateCleanMeter
        void shouldHandleComplexChainWithFail() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: m("critical") → ctx("user", "admin") → inc() × 10 → fail("auth_failure") is called
            meter.m("critical");
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

            meter.ctx("user", "admin");
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
            assertEquals("admin", meter.getContext().get("user"));

            for (int i = 0; i < 10; i++) {
                meter.inc();
            }
            assertMeterState(meter, true, false, null, null, null, null, 10, 0, 0);

            meter.fail("auth_failure");

            // Then: description, iterations, failPath all preserved, ERROR log (context is cleared after emission)
            assertMeterState(meter, true, true, null, null, "auth_failure", null, 10, 0, 0);
            assertEquals("critical", meter.getDescription());
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }

        // ============================================================================
        // Termination after No Operations (Minimal Meter)
        // ============================================================================

        @Test
        @DisplayName("should handle minimal meter: start() then ok()")
        @ValidateCleanMeter
        void shouldHandleMinimalMeterOk() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: ok() is called
            meter.ok();

            // Then: clean transition with no additional attributes, INFO log
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should handle minimal meter: start() then reject()")
        @ValidateCleanMeter
        void shouldHandleMinimalMeterReject() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: reject("no_work_done") is called
            meter.reject("no_work_done");

            // Then: rejectPath captured, INFO log
            assertMeterState(meter, true, true, null, "no_work_done", null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }

        @Test
        @DisplayName("should handle minimal meter: start() then fail()")
        @ValidateCleanMeter
        void shouldHandleMinimalMeterFail() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: fail("no_work_done") is called
            meter.fail("no_work_done");

            // Then: failPath captured, ERROR log
            assertMeterState(meter, true, true, null, null, "no_work_done", null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }

        // ============================================================================
        // Slow Operation Detection - Marker and Level Changes
        // ============================================================================

        @Test
        @DisplayName("should detect slow operation for ok() - marker changes to MSG_SLOW_OK, level to WARN")
        @ValidateCleanMeter
        void shouldDetectSlowOperationForOk() {
            // Given: a controllable time source with initial time (DAY1)
            final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);

            // Given: a new Meter with time limit
            final Meter meter = new Meter(logger).withTimeSource(timeSource);
            meter.limitMilliseconds(10);

            // When: meter starts
            meter.start();

            // When: time advances beyond limit (simulate 50ms elapsed)
            timeSource.advanceMiliseconds(50);

            // When: ok() is called
            meter.ok();

            // Then: isSlow() = true, WARN log with MSG_SLOW_OK marker (not MSG_OK)
            assertTrue(meter.isSlow());
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 10);
            AssertLogger.assertEvent(logger, 2, Level.WARN, Markers.MSG_SLOW_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_SLOW_OK);
        }
    }
}
