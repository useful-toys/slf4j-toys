/*
 * Copyright 2025 Daniel Felix Ferber
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
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper.TimeRecord;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.slf4jtestmock.WithMockLoggerDebug;
import org.usefultoys.test.ResetMeterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.ValidateCleanMeter;
import org.usefultoys.test.WithLocale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper.assertMeterCreateTime;
import static org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper.assertMeterStartTime;
import static org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper.assertMeterStartTimePreserved;
import static org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper.recordCreateWithWindow;
import static org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper.recordStartWithWindow;

/**
 * Unit tests for {@link Meter} initialization and construction.
 * <p>
 * This test class validates the initial state of Meter instances after construction
 * but before start(). It ensures that all state attributes have correct default values
 * and that the Meter is properly configured for subsequent lifecycle operations.
 * <p>
 * <b>Test Coverage:</b>
 * <ul>
 *   <li><b>Constructor Variants:</b> Tests all constructor overloads (logger only, logger + operation, logger + operation + parent)</li>
 *   <li><b>Initial State:</b> Verifies all state attributes are correctly initialized (timestamps = 0, paths = null, etc.)</li>
 *   <li><b>getCurrentInstance():</b> Verifies that before start(), getCurrentInstance() returns the unknown meter</li>
 *   <li><b>No Logging:</b> Confirms that no log events are emitted during construction (only start() triggers logging)</li>
 *   <li><b>Start Transition:</b> Validates successful transition from Created to Started state</li>
 *   <li><b>Try-With-Resources:</b> Tests Meter behavior when used in try-with-resources blocks</li>
 * </ul>
 * <p>
 * <b>State Tested:</b> Created → Started (initial transition only)
 * <p>
 * <b>Related Tests:</b>
 * <ul>
 *   <li>{@link MeterLifeCycleHappyPathTest} - Normal success flow (ok)</li>
 *   <li>{@link MeterLifeCyclePreStartConfigurationTest} - Configuration before start</li>
 *   <li>{@link MeterLifeCyclePostStartTerminationTest} - Termination operations after start</li>
 * </ul>
 *
 * @author Co-authored-by: GitHub Copilot using Claude 3.5 Sonnet
 */
@ValidateCharset
@ResetMeterConfig
@WithLocale("en")
@WithMockLogger
@ValidateCleanMeter
@WithMockLoggerDebug
@DisplayName("Group 1: Meter Initialization (Base Guarantee)")
@SuppressWarnings({"AssignmentToStaticFieldFromInstanceMethod", "IOResourceOpenedButNotSafelyClosed", "TestMethodWithoutAssertion"})
class MeterLifeCycleInitializationTest {

    @SuppressWarnings("NonConstantLogger")
    @Slf4jMock
    Logger logger;

    @Test
    @DisplayName("should create meter with logger - initial state")
    void shouldCreateMeterWithLoggerInitialState() {
        // Given: a new Meter with logger only
        final TimeRecord tv = new TimeRecord();
        final Meter meter = recordCreateWithWindow(tv, ()->new Meter(logger));

        // Then: meter has expected initial state (startTime = 0, stopTime = 0)
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
        // Then: meter should preserve create time and zero start/stop times
        assertMeterCreateTime(meter, tv);

        // Then: before start(), getCurrentInstance() returns unknown meter
        final Meter currentBeforeStart = Meter.getCurrentInstance();
        assertEquals(Meter.UNKNOWN_LOGGER_NAME, currentBeforeStart.getCategory(),
                "before start(), getCurrentInstance() should return unknown meter");

        // Then: no logs emitted before start()
        AssertLogger.assertEventCount(logger, 0);
    }

    @Test
    @DisplayName("should create meter with logger + operationName")
    void shouldCreateMeterWithOperationName() {
        // Given: a new Meter with logger and operationName
        final String operationName = "testOperation";
        final TimeRecord tv = new TimeRecord();
        final Meter meter = recordCreateWithWindow(tv, () -> new Meter(logger, operationName));

        // Then: meter has expected initial state
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
        // Then: meter should preserve create time and zero start/stop times
        assertMeterCreateTime(meter, tv);

        // Then: operationName is captured correctly
        assertEquals(operationName, meter.getOperation(), "operation name should match");

        // Then: before start(), getCurrentInstance() returns unknown meter
        final Meter currentBeforeStart = Meter.getCurrentInstance();
        assertEquals(Meter.UNKNOWN_LOGGER_NAME, currentBeforeStart.getCategory(),
                "before start(), getCurrentInstance() should return unknown meter");

        // Then: no logs emitted before start()
        AssertLogger.assertEventCount(logger, 0);
    }

    @Test
    @DisplayName("should create meter with logger + operationName + parent")
    void shouldCreateMeterWithParent() {
        // Given: a new Meter with logger, operationName, and parent
        final String operationName = "childOperation";
        final String parentId = "parent-meter-id";
        final TimeRecord tv = new TimeRecord();
        final Meter meter = recordCreateWithWindow(tv, () -> new Meter(logger, operationName, parentId));

        // Then: meter has expected initial state
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
        // Then: meter should preserve create time and zero start/stop times
        assertMeterCreateTime(meter, tv);

        // Then: operationName and parent are captured correctly
        assertEquals(operationName, meter.getOperation(), "operation name should match");
        assertEquals(parentId, meter.getParent(), "parent should match");

        // Then: before start(), getCurrentInstance() returns unknown meter
        final Meter currentBeforeStart = Meter.getCurrentInstance();
        assertEquals(Meter.UNKNOWN_LOGGER_NAME, currentBeforeStart.getCategory(),
                "before start(), getCurrentInstance() should return unknown meter");

        // Then: no logs emitted before start()
        AssertLogger.assertEventCount(logger, 0);
    }

    @Test
    @DisplayName("should start meter successfully")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldStartMeterSuccessfully() {
        // Given: a new Meter
        final TimeRecord tr = new TimeRecord();
        final Meter meter = recordCreateWithWindow(tr, () -> new Meter(logger));

        // Then: meter has expected initial state (Created state)
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
        // Then: meter should preserve create time and zero start/stop times
        assertMeterCreateTime(meter, tr);

        // Then: before start(), getCurrentInstance() returns unknown meter
        final Meter currentBeforeStart = Meter.getCurrentInstance();
        assertEquals(Meter.UNKNOWN_LOGGER_NAME, currentBeforeStart.getCategory(),
                "before start(), getCurrentInstance() should return unknown meter");

        // When: start() is called
        MeterLifeCycleTestHelper.recordStartWithWindow(tr, () -> meter.start());

        // Then: Meter transitions from Created to Started
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        // Then: meter should have valid create time and start time with zero stop time
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // Then: after start(), meter becomes current in thread-local and is returned by getCurrentInstance()
        final Meter currentAfterStart = Meter.getCurrentInstance();
        assertEquals(meter, currentAfterStart, "after start(), meter should be current in thread-local");

        // Then: logs start events (MSG_START + DATA_START)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.DEBUG, Markers.MSG_START);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.TRACE, Markers.DATA_START);
        AssertLogger.assertEventCount(logger, 2);
    }

    @Test
    @DisplayName("should start meter in try-with-resources (start in block)")
    void shouldStartMeterInTryWithResourcesSequential() {
        final TimeRecord tr = new TimeRecord();
        final Meter meterRef;

        // Given: Meter is created in try-with-resources (capture create time)
        try (final Meter m = recordCreateWithWindow(tr, () -> new Meter(logger))) {
            meterRef = m;

            // Then: meter has expected initial state before start (Created state)
            MeterLifeCycleTestHelper.assertMeterState(m, false, false, null, null, null, null, 0, 0, 0);
            // Then: meter should preserve create time and zero start/stop times
            assertMeterCreateTime(m, tr);

            // Then: before start(), getCurrentInstance() returns unknown meter
            final Meter currentBeforeStart = Meter.getCurrentInstance();
            assertEquals(Meter.UNKNOWN_LOGGER_NAME, currentBeforeStart.getCategory(),
                    "before start(), getCurrentInstance() should return unknown meter");

            // When: start() is called in the block (capture start time)
            recordStartWithWindow(tr, m::start);

            // Then: meter is transitioned to executing state (Started state)
            MeterLifeCycleTestHelper.assertMeterState(m, true, false, null, null, null, null, 0, 0, 0);
            // Then: meter should preserve create time and have valid start time with zero stop time
            assertMeterStartTime(m, tr);

            // Then: after start(), meter becomes current in thread-local
            final Meter currentAfterStart = Meter.getCurrentInstance();
            assertEquals(m, currentAfterStart, "after start(), meter should be current in thread-local");
        }

        // Then: after try-with-resources, meter is in Failed state (auto-fail via close())
        MeterLifeCycleTestHelper.assertMeterState(meterRef, true, true, null, null, "try-with-resources", null, 0, 0, 0);
        assertMeterStartTimePreserved(meterRef, tr);

        // Then: logs start events + auto-fail events (MSG_START + DATA_START + MSG_FAIL + DATA_FAIL)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.DEBUG, Markers.MSG_START);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.TRACE, Markers.DATA_START);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should start meter with chained call in try-with-resources")
    void shouldStartMeterInTryWithResourcesChained() {
        final TimeRecord tr = new TimeRecord();
        final Meter meterRef;

        // Given: Meter is created and started with chained call in try-with-resources (capture create/start time)
        try (final Meter m = recordStartWithWindow(tr, ()-> recordCreateWithWindow(tr, () -> new Meter(logger)).start())) {
            meterRef = m;

            // Then: meter is in executing state (created and started in single expression - Started state)
            MeterLifeCycleTestHelper.assertMeterState(m, true, false, null, null, null, null, 0, 0, 0);
            // Then: meter should preserve create time and have valid start time with zero stop time'
            MeterLifeCycleTestHelper.assertMeterStartTime(m, tr);

            // Then: meter becomes current in thread-local after start()
            final Meter currentAfterStart = Meter.getCurrentInstance();
            assertEquals(m, currentAfterStart, "after start(), meter should be current in thread-local");
        }

        // Then: after try-with-resources, meter is in Failed state (auto-fail via close())
        MeterLifeCycleTestHelper.assertMeterState(meterRef, true, true, null, null, "try-with-resources", null, 0, 0, 0);
        // Then: meter should preserve create time and start time
        assertMeterStartTimePreserved(meterRef, tr);

        // Then: logs start events + auto-fail events (MSG_START + DATA_START + MSG_FAIL + DATA_FAIL)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.DEBUG, Markers.MSG_START);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.TRACE, Markers.DATA_START);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEventCount(logger, 4);
    }
}
