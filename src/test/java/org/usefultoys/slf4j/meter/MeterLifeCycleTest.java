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
import org.usefultoys.test.ResetMeterConfig;
import org.usefultoys.test.ValidateCharset;
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

    private void assertMeterState(Meter meter, boolean started, boolean stopped, String okPath, String rejectPath, String failPath, String failMessage, long currentIteration, long expectedIterations) {
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
    @DisplayName("Success Flow")
    class SuccessFlow {
        @Test
        @DisplayName("should follow normal success flow")
        void shouldFollowNormalSuccessFlow() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);
            assertMeterState(meter, false, false, null, null, null, null, 0, 0);

            // When: start() is called
            meter.start();
            assertMeterState(meter, true, false, null, null, null, null, 0, 0);
            AssertLogger.assertEvent(logger, 0, Level.DEBUG, Markers.MSG_START);
            AssertLogger.assertEvent(logger, 1, Level.TRACE, Markers.DATA_START);

            // When: ok() is called
            meter.ok();
            assertMeterState(meter, true, true, null, null, null, null, 0, 0);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow success flow with path (String)")
        void shouldFollowSuccessFlowWithPathString() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);
            assertMeterState(meter, false, false, null, null, null, null, 0, 0);

            // When: start() is called
            meter.start();
            assertMeterState(meter, true, false, null, null, null, null, 0, 0);

            // When: ok("customPath") is called
            meter.ok("customPath");
            assertMeterState(meter, true, true, "customPath", null, null, null, 0, 0);

            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow success flow with path (Enum)")
        void shouldFollowSuccessFlowWithPathEnum() {
            final Meter meter = new Meter(logger);
            meter.start();
            meter.ok(TestEnum.VALUE1);
            assertMeterState(meter, true, true, TestEnum.VALUE1.name(), null, null, null, 0, 0);
        }

        @Test
        @DisplayName("should follow success flow with path (Throwable)")
        void shouldFollowSuccessFlowWithPathThrowable() {
            final Meter meter = new Meter(logger);
            meter.start();
            final Exception ex = new RuntimeException("error");
            meter.ok(ex);
            assertMeterState(meter, true, true, ex.getClass().getSimpleName(), null, null, null, 0, 0);
        }

        @Test
        @DisplayName("should follow success flow with path (Object)")
        void shouldFollowSuccessFlowWithPathObject() {
            final Meter meter = new Meter(logger);
            meter.start();
            final TestObject obj = new TestObject();
            meter.ok(obj);
            assertMeterState(meter, true, true, obj.toString(), null, null, null, 0, 0);
        }

        @Test
        @DisplayName("should follow success flow with path() method (String)")
        void shouldFollowSuccessFlowWithPathMethodString() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);
            assertMeterState(meter, false, false, null, null, null, null, 0, 0);

            // When: start() is called
            meter.start();
            assertMeterState(meter, true, false, null, null, null, null, 0, 0);

            // When: path("predefinedPath") is called
            meter.path("predefinedPath");
            assertMeterState(meter, true, false, "predefinedPath", null, null, null, 0, 0);

            // When: ok() is called
            meter.ok();
            assertMeterState(meter, true, true, "predefinedPath", null, null, null, 0, 0);

            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow success flow with path() method (Enum)")
        void shouldFollowSuccessFlowWithPathMethodEnum() {
            final Meter meter = new Meter(logger);
            meter.start();
            meter.path(TestEnum.VALUE2);
            assertMeterState(meter, true, false, TestEnum.VALUE2.name(), null, null, null, 0, 0);
            meter.ok();
            assertMeterState(meter, true, true, TestEnum.VALUE2.name(), null, null, null, 0, 0);
        }

        @Test
        @DisplayName("should follow success flow with path() method (Throwable)")
        void shouldFollowSuccessFlowWithPathMethodThrowable() {
            final Meter meter = new Meter(logger);
            meter.start();
            final Exception ex = new IllegalArgumentException("invalid");
            meter.path(ex);
            assertMeterState(meter, true, false, ex.getClass().getSimpleName(), null, null, null, 0, 0);
            meter.ok();
            assertMeterState(meter, true, true, ex.getClass().getSimpleName(), null, null, null, 0, 0);
        }

        @Test
        @DisplayName("should follow success flow with path() method (Object)")
        void shouldFollowSuccessFlowWithPathMethodObject() {
            final Meter meter = new Meter(logger);
            meter.start();
            final TestObject obj = new TestObject();
            meter.path(obj);
            assertMeterState(meter, true, false, obj.toString(), null, null, null, 0, 0);
            meter.ok();
            assertMeterState(meter, true, true, obj.toString(), null, null, null, 0, 0);
        }

        @Test
        @DisplayName("should follow success flow with path override")
        void shouldFollowSuccessFlowWithPathOverride() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);
            assertMeterState(meter, false, false, null, null, null, null, 0, 0);

            // When: start() is called
            meter.start();
            assertMeterState(meter, true, false, null, null, null, null, 0, 0);

            // When: path("initialPath") is called
            meter.path("initialPath");
            assertMeterState(meter, true, false, "initialPath", null, null, null, 0, 0);

            // When: ok("finalPath") is called (overriding initialPath)
            meter.ok("finalPath");
            assertMeterState(meter, true, true, "finalPath", null, null, null, 0, 0);

            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow success flow using success() alias")
        void shouldFollowSuccessFlowUsingSuccessAlias() {
            final Meter meter = new Meter(logger);
            meter.start();

            // When: success() is called
            meter.success();
            assertMeterState(meter, true, true, null, null, null, null, 0, 0);

            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
        }

        @Test
        @DisplayName("should follow success flow using success(path) alias")
        void shouldFollowSuccessFlowUsingSuccessPathAlias() {
            final Meter meter = new Meter(logger);
            meter.start();

            // When: success("aliasPath") is called
            meter.success("aliasPath");
            assertMeterState(meter, true, true, "aliasPath", null, null, null, 0, 0);

            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
        }
    }

    @Nested
    @DisplayName("Rejection Flow")
    class RejectionFlow {
        @Test
        @DisplayName("should follow rejection flow (String)")
        void shouldFollowRejectionFlowString() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);
            assertMeterState(meter, false, false, null, null, null, null, 0, 0);

            // When: start() is called
            meter.start();
            assertMeterState(meter, true, false, null, null, null, null, 0, 0);

            // When: reject("businessRule") is called
            meter.reject("businessRule");
            assertMeterState(meter, true, true, null, "businessRule", null, null, 0, 0);

            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }

        @Test
        @DisplayName("should follow rejection flow (Enum)")
        void shouldFollowRejectionFlowEnum() {
            final Meter meter = new Meter(logger);
            meter.start();
            meter.reject(TestEnum.VALUE1);
            assertMeterState(meter, true, true, null, TestEnum.VALUE1.name(), null, null, 0, 0);
        }

        @Test
        @DisplayName("should follow rejection flow (Throwable)")
        void shouldFollowRejectionFlowThrowable() {
            final Meter meter = new Meter(logger);
            meter.start();
            final Exception ex = new RuntimeException("rejected");
            meter.reject(ex);
            assertMeterState(meter, true, true, null, ex.getClass().getSimpleName(), null, null, 0, 0);
        }

        @Test
        @DisplayName("should follow rejection flow (Object)")
        void shouldFollowRejectionFlowObject() {
            final Meter meter = new Meter(logger);
            meter.start();
            final TestObject obj = new TestObject();
            meter.reject(obj);
            assertMeterState(meter, true, true, null, obj.toString(), null, null, 0, 0);
        }
    }

    @Nested
    @DisplayName("Failure Flow")
    class FailureFlow {
        @Test
        @DisplayName("should follow failure flow (Throwable)")
        void shouldFollowFailureFlowThrowable() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);
            assertMeterState(meter, false, false, null, null, null, null, 0, 0);

            // When: start() is called
            meter.start();
            assertMeterState(meter, true, false, null, null, null, null, 0, 0);

            final Exception ex = new RuntimeException("technical error");

            // When: fail(ex) is called
            meter.fail(ex);
            assertMeterState(meter, true, true, null, null, ex.getClass().getName(), ex.getMessage(), 0, 0);

            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }

        @Test
        @DisplayName("should follow failure flow (String)")
        void shouldFollowFailureFlowString() {
            final Meter meter = new Meter(logger);
            meter.start();
            meter.fail("technical error");
            assertMeterState(meter, true, true, null, null, "technical error", null, 0, 0);
        }

        @Test
        @DisplayName("should follow failure flow (Enum)")
        void shouldFollowFailureFlowEnum() {
            final Meter meter = new Meter(logger);
            meter.start();
            meter.fail(TestEnum.VALUE2);
            assertMeterState(meter, true, true, null, null, TestEnum.VALUE2.name(), null, 0, 0);
        }

        @Test
        @DisplayName("should follow failure flow (Object)")
        void shouldFollowFailureFlowObject() {
            final Meter meter = new Meter(logger);
            meter.start();
            final TestObject obj = new TestObject();
            meter.fail(obj);
            assertMeterState(meter, true, true, null, null, obj.toString(), null, 0, 0);
        }
    }

    @Nested
    @DisplayName("Try-With-Resources")
    class TryWithResources {
        @Test
        @DisplayName("should follow try-with-resources flow (implicit failure)")
        void shouldFollowTryWithResourcesFlowImplicitFailure() {
            final Meter meter;
            // When: Meter is used in try-with-resources and not explicitly stopped
            try (Meter m = new Meter(logger)) {
                assertMeterState(m, false, false, null, null, null, null, 0, 0);
                m.start();
                assertMeterState(m, true, false, null, null, null, null, 0, 0);
                meter = m;
                // do nothing
            }

            // Then: it should be automatically failed on close()
            assertMeterState(meter, true, true, null, null, "try-with-resources", null, 0, 0);

            AssertLogger.assertEvent(logger, 0, Level.DEBUG, Markers.MSG_START);
            AssertLogger.assertEvent(logger, 1, Level.TRACE, Markers.DATA_START);
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }

        @Test
        @DisplayName("should follow try-with-resources flow (explicit success)")
        void shouldFollowTryWithResourcesFlowExplicitSuccess() {
            final Meter meter;
            // When: Meter is used in try-with-resources and explicitly stopped with ok()
            try (Meter m = new Meter(logger)) {
                m.start();
                m.ok();
                meter = m;
            }

            // Then: it should remain in success state after close()
            assertMeterState(meter, true, true, null, null, null, null, 0, 0);

            AssertLogger.assertEvent(logger, 0, Level.DEBUG, Markers.MSG_START);
            AssertLogger.assertEvent(logger, 1, Level.TRACE, Markers.DATA_START);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow try-with-resources flow (explicit rejection)")
        void shouldFollowTryWithResourcesFlowExplicitRejection() {
            final Meter meter;
            // When: Meter is used in try-with-resources and explicitly stopped with reject()
            try (Meter m = new Meter(logger)) {
                m.start();
                m.reject("rejected");
                meter = m;
            }

            // Then: it should remain in rejection state after close()
            assertMeterState(meter, true, true, null, "rejected", null, null, 0, 0);

            AssertLogger.assertEvent(logger, 0, Level.DEBUG, Markers.MSG_START);
            AssertLogger.assertEvent(logger, 1, Level.TRACE, Markers.DATA_START);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }

        @Test
        @DisplayName("should follow try-with-resources flow (explicit failure)")
        void shouldFollowTryWithResourcesFlowExplicitFailure() {
            final Meter meter;
            // When: Meter is used in try-with-resources and explicitly stopped with fail()
            try (Meter m = new Meter(logger)) {
                m.start();
                m.fail("failed");
                meter = m;
            }

            // Then: it should remain in failure state after close()
            assertMeterState(meter, true, true, null, null, "failed", null, 0, 0);

            AssertLogger.assertEvent(logger, 0, Level.DEBUG, Markers.MSG_START);
            AssertLogger.assertEvent(logger, 1, Level.TRACE, Markers.DATA_START);
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
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
            assertMeterState(meter, true, false, null, null, null, null, 10, 100);

            // When: incTo(50) is called
            meter.incTo(50);
            assertMeterState(meter, true, false, null, null, null, null, 50, 100);

            meter.ok();
            assertMeterState(meter, true, true, null, null, null, null, 50, 100);
        }
    }

    @Nested
    @DisplayName("Advanced Features")
    class AdvancedFeatures {
        @Test
        @DisplayName("should log slow operation when limit is exceeded")
        void shouldLogSlowOperationWhenLimitIsExceeded() throws InterruptedException {
            final Meter meter = new Meter(logger);
            // Given: a very short time limit
            meter.limitMilliseconds(1);
            meter.start();

            // When: operation takes longer than limit
            Thread.sleep(10);
            meter.ok();

            // Then: MSG_SLOW_OK (WARN) should be logged instead of MSG_OK (INFO)
            AssertLogger.assertEvent(logger, 2, Level.WARN, Markers.MSG_SLOW_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_SLOW_OK);
        }
    }
}
