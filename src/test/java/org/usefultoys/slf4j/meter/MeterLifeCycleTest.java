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
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.slf4jtestmock.WithMockLoggerDebug;
import org.usefultoys.test.ResetMeterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.ValidateCleanMeter;
import org.usefultoys.test.WithLocale;

import static org.junit.jupiter.api.Assertions.*;

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
 */
@ValidateCharset
@ResetMeterConfig
@WithLocale("en")
@WithMockLogger
@ValidateCleanMeter
@WithMockLoggerDebug
class MeterLifeCycleTest {

    @Slf4jMock
    private Logger logger;

    enum TestEnum {
        VALUE1, VALUE2
    }

    static class TestObject {
        @Override
        public String toString() {
            return "testObjectString";
        }
    }

    private void assertMeterState(Meter meter, boolean started, boolean stopped, String okPath, String rejectPath, String failPath, String failMessage, long currentIteration, long expectedIterations, long timeLimitMilliseconds) {
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
    @DisplayName("Meter Initialization")
    class MeterInitialization {
        @Test
        @DisplayName("should create meter with expected initial state")
        void shouldCreateMeterWithExpectedInitialState() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // Then: meter has expected initial state
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEventCount(logger, 0);
        }

        @Test
        @DisplayName("should start meter successfully")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldStartMeterSuccessfully() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: start() is called
            meter.start();

            // Then: Meter is in executing state
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

            // Then: log messages recorded correctly
            AssertLogger.assertEvent(logger, 0, Level.DEBUG, Markers.MSG_START);
            AssertLogger.assertEvent(logger, 1, Level.TRACE, Markers.DATA_START);
        }

        @Test
        @DisplayName("should start meter in try-with-resources")
        void shouldStartMeterinTryWithResources1() {
            // Given: Meter is created in try-with-resources
            try (Meter m = new Meter(logger)) {
                // Then: meter has expected initial state
                assertMeterState(m, false, false, null, null, null, null, 0, 0, 0);
                
                // When: start() is called
                m.start();
                
                // Then: meter is in executing state
                assertMeterState(m, true, false, null, null, null, null, 0, 0, 0);
            }

            // Then: start log messages recorded correctly
            AssertLogger.assertEvent(logger, 0, Level.DEBUG, Markers.MSG_START);
            AssertLogger.assertEvent(logger, 1, Level.TRACE, Markers.DATA_START);
        }

        
        @Test
        @DisplayName("should start meter with chained call in try-with-resources")
        void shouldStartMeterinTryWithResources2() {
            // Given: Meter is created with chained start() in try-with-resources
            try (Meter m = new Meter(logger).start()) {
                // Then: meter is in executing state
                assertMeterState(m, true, false, null, null, null, null, 0, 0, 0);
            }

            // Then: start log messages recorded correctly
            AssertLogger.assertEvent(logger, 0, Level.DEBUG, Markers.MSG_START);
            AssertLogger.assertEvent(logger, 1, Level.TRACE, Markers.DATA_START);
        }
    }

    @Nested
    @DisplayName("Success Flow")
    class SuccessFlow {
        @Test
        @DisplayName("should follow normal success flow")
        void shouldFollowNormalSuccessFlow() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: ok() is called
            meter.ok();

            // Then: Meter is in stopped state
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow success flow with path (String)")
        void shouldFollowSuccessFlowWithPathString() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: ok("customPath") is called
            meter.ok("customPath");

            // Then: Meter is in stopped state with path set
            assertMeterState(meter, true, true, "customPath", null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow success flow with path (Enum)")
        void shouldFollowSuccessFlowWithPathEnum() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: ok(TestEnum.VALUE1) is called
            meter.ok(TestEnum.VALUE1);

            // Then: Meter is in stopped state with enum path
            assertMeterState(meter, true, true, TestEnum.VALUE1.name(), null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow success flow with path (Throwable)")
        void shouldFollowSuccessFlowWithPathThrowable() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: ok(exception) is called
            final Exception ex = new RuntimeException("error");
            meter.ok(ex);

            // Then: Meter is in stopped state with exception class as path
            assertMeterState(meter, true, true, ex.getClass().getSimpleName(), null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow success flow with path (Object)")
        void shouldFollowSuccessFlowWithPathObject() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: ok(object) is called
            final TestObject obj = new TestObject();
            meter.ok(obj);

            // Then: Meter is in stopped state with object toString as path
            assertMeterState(meter, true, true, obj.toString(), null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow success flow with path() method (String)")
        void shouldFollowSuccessFlowWithPathMethodString() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path("predefinedPath") is called
            meter.path("predefinedPath");

            // Then: path is set
            assertMeterState(meter, true, false, "predefinedPath", null, null, null, 0, 0, 0);

            // When: ok() is called
            meter.ok();

            // Then: Meter is in stopped state with path preserved
            assertMeterState(meter, true, true, "predefinedPath", null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow success flow with path() method (Enum)")
        void shouldFollowSuccessFlowWithPathMethodEnum() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path(TestEnum.VALUE2) is called
            meter.path(TestEnum.VALUE2);

            // Then: enum path is set
            assertMeterState(meter, true, false, TestEnum.VALUE2.name(), null, null, null, 0, 0, 0);

            // When: ok() is called
            meter.ok();

            // Then: Meter is in stopped state with enum path preserved
            assertMeterState(meter, true, true, TestEnum.VALUE2.name(), null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow success flow with path() method (Throwable)")
        void shouldFollowSuccessFlowWithPathMethodThrowable() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path(exception) is called
            final Exception ex = new IllegalArgumentException("invalid");
            meter.path(ex);

            // Then: exception class is set as path
            assertMeterState(meter, true, false, ex.getClass().getSimpleName(), null, null, null, 0, 0, 0);

            // When: ok() is called
            meter.ok();

            // Then: Meter is in stopped state with exception path preserved
            assertMeterState(meter, true, true, ex.getClass().getSimpleName(), null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow success flow with path() method (Object)")
        void shouldFollowSuccessFlowWithPathMethodObject() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path(object) is called
            final TestObject obj = new TestObject();
            meter.path(obj);

            // Then: object toString is set as path
            assertMeterState(meter, true, false, obj.toString(), null, null, null, 0, 0, 0);

            // When: ok() is called
            meter.ok();

            // Then: Meter is in stopped state with object path preserved
            assertMeterState(meter, true, true, obj.toString(), null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow success flow with path override")
        void shouldFollowSuccessFlowWithPathOverride() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path("initialPath") is called
            meter.path("initialPath");

            // Then: initial path is set
            assertMeterState(meter, true, false, "initialPath", null, null, null, 0, 0, 0);

            // When: ok("finalPath") is called (overriding initialPath)
            meter.ok("finalPath");

            // Then: final path overrides initial path
            assertMeterState(meter, true, true, "finalPath", null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should override path when path() is called twice")
        void shouldOverridePathWhenPathCalledTwice() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path("firstPath") is called
            meter.path("firstPath");

            // Then: first path is set
            assertMeterState(meter, true, false, "firstPath", null, null, null, 0, 0, 0);

            // When: path("secondPath") is called (should override firstPath)
            meter.path("secondPath");

            // Then: second path overrides first path
            assertMeterState(meter, true, false, "secondPath", null, null, null, 0, 0, 0);

            // When: ok() is called
            meter.ok();

            // Then: Meter is in stopped state with second path preserved
            assertMeterState(meter, true, true, "secondPath", null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should log error and keep previous path when path(non-null) then path(null)")
        void shouldKeepPreviousPathWhenPathNullAfterNonNull() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path("validPath") is called
            meter.path("validPath");

            // Then: path is set
            assertMeterState(meter, true, false, "validPath", null, null, null, 0, 0, 0);

            // When: path(null) is called (should log error and clear path)
            meter.path(null);

            // Then: path is cleared
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

            // When: ok() is called
            meter.ok();

            // Then: Meter is in stopped state
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertEvent(logger, 3, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 4, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should log error for ok(null) but complete with set path")
        void shouldCompleteWithPathWhenOkNullAfterPathNonNull() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path("validPath") is called
            meter.path("validPath");

            // Then: path is set
            assertMeterState(meter, true, false, "validPath", null, null, null, 0, 0, 0);

            // When: ok(null) is called (should log error but complete with validPath)
            meter.ok(null);

            // Then: Meter is in stopped state with validPath preserved
            assertMeterState(meter, true, true, "validPath", null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertEvent(logger, 3, Level.INFO, Markers.MSG_OK);
        }

        @Test
        @DisplayName("should follow success flow using success() alias")
        void shouldFollowSuccessFlowUsingSuccessAlias() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: success() is called
            meter.success();

            // Then: Meter is in stopped state
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow success flow using success(path) alias")
        void shouldFollowSuccessFlowUsingSuccessPathAlias() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: success("aliasPath") is called
            meter.success("aliasPath");

            // Then: Meter is in stopped state with path set
            assertMeterState(meter, true, true, "aliasPath", null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should log error and continue when path(null) is called")
        void shouldLogErrorAndContinueWhenPathNullIsCalled() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path(null) is called (should log error but not throw)
            meter.path(null);

            // Then: path remains null
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

            // When: ok() is called after null path
            meter.ok();

            // Then: Meter is in stopped state
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertEvent(logger, 3, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 4, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should log error and continue when ok(null) is called")
        void shouldLogErrorAndContinueWhenOkNullIsCalled() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: ok(null) is called (should log error but complete operation)
            meter.ok(null);

            // Then: Meter is in stopped state
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertEvent(logger, 3, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 4, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should log slow operation when limit is exceeded")
        void shouldLogSlowOperationWhenLimitIsExceeded() throws InterruptedException {
            // Given: a new Meter
            final Meter meter = new Meter(logger);
            
            // Then: meter has expected initial state
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            
            // When: limitMilliseconds(1) is called
            meter.limitMilliseconds(1);
            
            // Then: time limit is set
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 1);
            
            // When: start() is called
            meter.start();
            
            // Then: meter is in executing state with time limit
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 1);

            // When: operation takes longer than limit
            Thread.sleep(10);
            meter.ok();

            // Then: Meter is in stopped state with time limit preserved
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 1);

            // Then: MSG_SLOW_OK (WARN) should be logged instead of MSG_OK (INFO)
            AssertLogger.assertEvent(logger, 2, Level.WARN, Markers.MSG_SLOW_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_SLOW_OK);
        }
    }

    @Nested
    @DisplayName("Created State Path Calls")
    class CreatedStatePathCallTests {
        @Test
        @DisplayName("should modify okPath even when path(String) is called before start()")
        void shouldModifyPathEvenWhenPathStringBeforeStart() {
            // Given: a new, not yet started Meter
            final Meter meter = new Meter(logger);

            // When: path("pathId") is called before start()
            meter.path("pathId");

            // Then: path is set (path() does not validate preconditions)
            assertMeterState(meter, false, false, "pathId", null, null, null, 0, 0, 0);
        }

        @Test
        @DisplayName("should modify okPath even when path(Enum) is called before start()")
        void shouldModifyPathEvenWhenPathEnumBeforeStart() {
            // Given: a new, not yet started Meter
            final Meter meter = new Meter(logger);

            // When: path(TestEnum.VALUE1) is called before start()
            meter.path(TestEnum.VALUE1);

            // Then: path is set (path() does not validate preconditions)
            assertMeterState(meter, false, false, "VALUE1", null, null, null, 0, 0, 0);
        }

        @Test
        @DisplayName("should modify okPath even when path(Throwable) is called before start()")
        void shouldModifyPathEvenWhenPathThrowableBeforeStart() {
            // Given: a new, not yet started Meter
            final Meter meter = new Meter(logger);

            // When: path(new Exception()) is called before start()
            meter.path(new RuntimeException("test"));

            // Then: path is set (path() does not validate preconditions)
            assertMeterState(meter, false, false, "RuntimeException", null, null, null, 0, 0, 0);
        }

        @Test
        @DisplayName("should modify okPath even when path(Object) is called before start()")
        void shouldModifyPathEvenWhenPathObjectBeforeStart() {
            // Given: a new, not yet started Meter
            final Meter meter = new Meter(logger);

            // When: path(new TestObject()) is called before start()
            meter.path(new TestObject());

            // Then: path is set (path() does not validate preconditions)
            assertMeterState(meter, false, false, "testObjectString", null, null, null, 0, 0, 0);
        }

        @Test
        @DisplayName("should clear okPath when path(null) is called before start()")
        void shouldClearPathWhenPathNullBeforeStart() {
            // Given: a new, not yet started Meter with a path already set
            final Meter meter = new Meter(logger);
            meter.path("initialPath");
            
            // Then: path is set
            assertMeterState(meter, false, false, "initialPath", null, null, null, 0, 0, 0);

            // When: path(null) is called before start()
            meter.path(null);

            // Then: okPath should be cleared (null overwrites previous value)
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
        }
    }

    @Nested
    @DisplayName("Rejection Flow")
    class RejectionFlow {
        @Test
        @DisplayName("should follow rejection flow (String)")
        void shouldFollowRejectionFlowString() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: reject("businessRule") is called
            meter.reject("businessRule");

            // Then: Meter is in stopped state with reject path set
            assertMeterState(meter, true, true, null, "businessRule", null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }

        @Test
        @DisplayName("should follow rejection flow (Enum)")
        void shouldFollowRejectionFlowEnum() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: reject(TestEnum.VALUE1) is called
            meter.reject(TestEnum.VALUE1);

            // Then: Meter is in stopped state with enum reject path
            assertMeterState(meter, true, true, null, TestEnum.VALUE1.name(), null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }

        @Test
        @DisplayName("should follow rejection flow (Throwable)")
        void shouldFollowRejectionFlowThrowable() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: reject(exception) is called
            final Exception ex = new RuntimeException("rejected");
            meter.reject(ex);

            // Then: Meter is in stopped state with exception class as reject path
            assertMeterState(meter, true, true, null, ex.getClass().getSimpleName(), null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }

        @Test
        @DisplayName("should follow rejection flow (Object)")
        void shouldFollowRejectionFlowObject() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: reject(object) is called
            final TestObject obj = new TestObject();
            meter.reject(obj);

            // Then: Meter is in stopped state with object toString as reject path
            assertMeterState(meter, true, true, null, obj.toString(), null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }
    }

    @Nested
    @DisplayName("Failure Flow")
    class FailureFlow {
        @Test
        @DisplayName("should follow failure flow (Throwable)")
        void shouldFollowFailureFlowThrowable() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: fail(exception) is called
            final Exception ex = new RuntimeException("technical error");
            meter.fail(ex);

            // Then: Meter is in stopped state with exception details in fail path
            assertMeterState(meter, true, true, null, null, ex.getClass().getName(), ex.getMessage(), 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }

        @Test
        @DisplayName("should follow failure flow (String)")
        void shouldFollowFailureFlowString() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: fail("technical error") is called
            meter.fail("technical error");

            // Then: Meter is in stopped state with failure message
            assertMeterState(meter, true, true, null, null, "technical error", null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }

        @Test
        @DisplayName("should follow failure flow (Enum)")
        void shouldFollowFailureFlowEnum() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: fail(TestEnum.VALUE2) is called
            meter.fail(TestEnum.VALUE2);

            // Then: Meter is in stopped state with enum fail path
            assertMeterState(meter, true, true, null, null, TestEnum.VALUE2.name(), null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }

        @Test
        @DisplayName("should follow failure flow (Object)")
        void shouldFollowFailureFlowObject() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: fail(object) is called
            final TestObject obj = new TestObject();
            meter.fail(obj);

            // Then: Meter is in stopped state with object toString as fail path
            assertMeterState(meter, true, true, null, null, obj.toString(), null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }
    }

    @Nested
    @DisplayName("Try-With-Resources")
    class TryWithResources {
        @Test
        @DisplayName("should follow try-with-resources flow (implicit failure)")
        void shouldFollowTryWithResourcesFlowImplicitFailure() {
            final Meter meter;
            // Given: a new, started Meter withing try with resources
            try (Meter m = new Meter(logger).start()) {
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
            try (Meter m = new Meter(logger).start()) {
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
            try (Meter m = new Meter(logger).start()) {
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
            try (Meter m = new Meter(logger).start()) {
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
            try (Meter m = new Meter(logger).start()) {
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
    }

    @Nested
    @DisplayName("Stopped State Path Calls")
    class StoppedStatePathCallTests {
        @Test
        @DisplayName("should modify okPath even when path(String) is called after ok()")
        void shouldModifyPathEvenWhenPathStringAfterOk() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().ok();

            // When: path("newPath") is called after ok()
            meter.path("newPath");

            // Then: okPath should be modified (path() does not validate postcondition)
            assertMeterState(meter, true, true, "newPath", null, null, null, 0, 0, 0);
        }

        @Test
        @DisplayName("should modify okPath even when path(Enum) is called after ok()")
        void shouldModifyPathEvenWhenPathEnumAfterOk() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().ok();

            // When: path(TestEnum.VALUE1) is called after ok()
            meter.path(TestEnum.VALUE1);

            // Then: okPath should be modified
            assertMeterState(meter, true, true, TestEnum.VALUE1.name(), null, null, null, 0, 0, 0);
        }

        @Test
        @DisplayName("should modify okPath even when path(Throwable) is called after ok()")
        void shouldModifyPathEvenWhenPathThrowableAfterOk() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().ok();

            // When: path(new Exception()) is called after ok()
            meter.path(new RuntimeException("test"));

            // Then: okPath should be modified
            assertMeterState(meter, true, true, "RuntimeException", null, null, null, 0, 0, 0);
        }

        @Test
        @DisplayName("should modify okPath even when path(Object) is called after ok()")
        void shouldModifyPathEvenWhenPathObjectAfterOk() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().ok();

            // When: path(new TestObject()) is called after ok()
            meter.path(new TestObject());

            // Then: okPath should be modified
            assertMeterState(meter, true, true, "testObjectString", null, null, null, 0, 0, 0);
        }

        @Test
        @DisplayName("should modify okPath even when path(String) is called after reject()")
        void shouldModifyPathEvenWhenPathStringAfterReject() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().reject("businessRule");

            // When: path("newPath") is called after reject()
            meter.path("newPath");

            // Then: okPath should be modified
            assertMeterState(meter, true, true, "newPath", "businessRule", null, null, 0, 0, 0);
        }

        @Test
        @DisplayName("should modify okPath even when path(Enum) is called after reject()")
        void shouldModifyPathEvenWhenPathEnumAfterReject() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().reject("businessRule");

            // When: path(TestEnum.VALUE1) is called after reject()
            meter.path(TestEnum.VALUE1);

            // Then: okPath should be modified
            assertMeterState(meter, true, true, "VALUE1", "businessRule", null, null, 0, 0, 0);
        }

        @Test
        @DisplayName("should modify okPath even when path(Throwable) is called after reject()")
        void shouldModifyPathEvenWhenPathThrowableAfterReject() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().reject("businessRule");

            // When: path(new Exception()) is called after reject()
            meter.path(new RuntimeException("test"));

            // Then: okPath should be modified
            assertMeterState(meter, true, true, "RuntimeException", "businessRule", null, null, 0, 0, 0);
        }

        @Test
        @DisplayName("should modify okPath even when path(Object) is called after reject()")
        void shouldModifyPathEvenWhenPathObjectAfterReject() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().reject("businessRule");

            // When: path(new TestObject()) is called after reject()
            meter.path(new TestObject());

            // Then: okPath should be modified
            assertMeterState(meter, true, true, "testObjectString", "businessRule", null, null, 0, 0, 0);
        }

        @Test
        @DisplayName("should modify okPath even when path(String) is called after fail()")
        void shouldModifyPathEvenWhenPathStringAfterFail() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().fail("error occurred");

            // When: path("newPath") is called after fail()
            meter.path("newPath");

            // Then: okPath should be modified
            assertMeterState(meter, true, true, "newPath", null, "error occurred", null, 0, 0, 0);
        }

        @Test
        @DisplayName("should modify okPath even when path(Enum) is called after fail()")
        void shouldModifyPathEvenWhenPathEnumAfterFail() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().fail("error occurred");

            // When: path(TestEnum.VALUE1) is called after fail()
            meter.path(TestEnum.VALUE1);

            // Then: okPath should be modified
            assertMeterState(meter, true, true, "VALUE1", null, "error occurred", null, 0, 0, 0);
        }

        @Test
        @DisplayName("should modify okPath even when path(Throwable) is called after fail()")
        void shouldModifyPathEvenWhenPathThrowableAfterFail() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().fail("error occurred");

            // When: path(new Exception()) is called after fail()
            meter.path(new RuntimeException("test"));

            // Then: okPath should be modified
            assertMeterState(meter, true, true, "RuntimeException", null, "error occurred", null, 0, 0, 0);
        }

        @Test
        @DisplayName("should modify okPath even when path(Object) is called after fail()")
        void shouldModifyPathEvenWhenPathObjectAfterFail() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().fail("error occurred");

            // When: path(new TestObject()) is called after fail()
            meter.path(new TestObject());

            // Then: okPath should be modified
            assertMeterState(meter, true, true, "testObjectString", null, "error occurred", null, 0, 0, 0);
        }

        @Test
        @DisplayName("should clear okPath when path(null) is called after ok()")
        void shouldClearPathWhenPathNullAfterOk() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().ok("successPath");

            // When: path(null) is called after ok()
            meter.path(null);

            // Then: okPath should be cleared (null overwrites previous value)
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
        }

        @Test
        @DisplayName("should clear okPath when path(null) is called after reject()")
        void shouldClearPathWhenPathNullAfterReject() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().reject("businessRule");
            meter.path("rejectPath");

            // When: path(null) is called after reject()
            meter.path(null);

            // Then: okPath should be cleared (null overwrites previous value)
            assertMeterState(meter, true, true, null, "businessRule", null, null, 0, 0, 0);
        }

        @Test
        @DisplayName("should clear okPath when path(null) is called after fail()")
        void shouldClearPathWhenPathNullAfterFail() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().fail("error occurred");
            meter.path("failPath");

            // When: path(null) is called after fail()
            meter.path(null);

            // Then: okPath should be cleared (null overwrites previous value)
            assertMeterState(meter, true, true, null, null, "error occurred", null, 0, 0, 0);
        }
    }

    @Nested
    @DisplayName("Iteration Tracking")
    class IterationTracking {
        @Test
        @DisplayName("should track iterations with incBy and incTo")
        void shouldTrackIterationsWithIncByAndIncTo() {
            final Meter meter = new Meter(logger);
            meter.iterations(100);
            meter.start();

            // When: incBy(10) is called
            meter.incBy(10);
            assertMeterState(meter, true, false, null, null, null, null, 10, 100, 0);

            // When: incTo(50) is called
            meter.incTo(50);
            assertMeterState(meter, true, false, null, null, null, null, 50, 100, 0);

            meter.ok();
            assertMeterState(meter, true, true, null, null, null, null, 50, 100, 0);
        }
    }
}
