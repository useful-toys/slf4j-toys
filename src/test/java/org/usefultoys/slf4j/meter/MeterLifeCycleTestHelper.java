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

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent.Level;
import org.usefultoys.slf4jtestmock.AssertLogger;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Helper class for Meter lifecycle tests, providing reusable log assertion logic and test scenarios.
 */
final class MeterLifeCycleTestHelper {

    private MeterLifeCycleTestHelper() {
        // Utility class
    }

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
        /* Validate createTime is always set */
        assertTrue(meter.getCreateTime() > 0, "createTime should be > 0");

        /* Validate startTime state */
        if (started) {
            assertTrue(meter.getStartTime() > 0, "startTime should be > 0");
        } else {
            assertEquals(0, meter.getStartTime(), "startTime should be 0");
        }

        /* Validate stopTime state */
        if (stopped) {
            assertTrue(meter.getStopTime() > 0, "stopTime should be > 0");
        } else {
            assertEquals(0, meter.getStopTime(), "stopTime should be 0");
        }

        /* Validate temporal ordering: createTime <= startTime <= stopTime */
        if (started && stopped) {
            assertTrue(meter.getCreateTime() <= meter.getStartTime(), 
                "createTime should be <= startTime");
            assertTrue(meter.getStartTime() <= meter.getStopTime(), 
                "startTime should be <= stopTime");
        } else if (started) {
            assertTrue(meter.getCreateTime() <= meter.getStartTime(), 
                "createTime should be <= startTime");
        }

        /* Validate outcome paths */
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

        /* Validate iteration and time limit configuration */
        assertEquals(currentIteration, meter.getCurrentIteration(), "currentIteration should match expected value: " + currentIteration);
        assertEquals(expectedIterations, meter.getExpectedIterations(), "expectedIterations should match expected value: " + expectedIterations);
        assertEquals(timeLimitMilliseconds * 1000 * 1000, meter.getTimeLimit(), "timeLimit should match expected value: " + timeLimitMilliseconds + "ms");

        /* Validate lastCurrentTime consistency */
        if (stopped) {
            assertTrue(meter.getLastCurrentTime() >= meter.getStopTime(), "lastCurrentTime should be >= stopTime");
        } else if (started) {
            assertTrue(meter.getLastCurrentTime() >= meter.getStartTime(), "lastCurrentTime should be >= startTime");
        } else {
            assertEquals(meter.getCreateTime(), meter.getLastCurrentTime(), "lastCurrentTime should be equal to createTime");
        }
    }

    static void assertMeterTime(final Meter meter, final long expectedCreateTime, final long expectedStartTime, final long expectedStopTime){
        if (expectedCreateTime != -1) assertEquals(expectedCreateTime, meter.getCreateTime(), "createTime should match expected value: " + expectedCreateTime);
        if (expectedStartTime != -1) assertEquals(expectedStartTime, meter.getStartTime(), "startTime should match expected value: " + expectedStartTime);
        if (expectedStopTime != -1) assertEquals(expectedStopTime, meter.getStopTime(), "stopTime should match expected value: " + expectedStopTime);
    }

    static void assertMeterStartTimeWindow(final Meter meter, final long minStartTime, final long maxStartTime){
        if (minStartTime != -1) assertTrue(meter.getStartTime() >= minStartTime, "startTime should be >= " + minStartTime);
        if (maxStartTime != -1) assertTrue(meter.getStartTime() <= maxStartTime, "startTime should be <= " + maxStartTime);
    }

    static void assertMeterStopTimeWindow(final Meter meter, final long minStopTime, final long maxStopTime){
        if (minStopTime != -1) assertTrue(meter.getStopTime() >= minStopTime, "stopTime should be >= " + minStopTime);
        if (maxStopTime != -1) assertTrue(meter.getStopTime() <= maxStopTime, "stopTime should be <= " + maxStopTime);
    }

    static void assertMeterCreateTimeWindow(final Meter meter, final long minCreateTime, final long maxCreateTime){
        if (minCreateTime != -1) assertTrue(meter.getCreateTime() >= minCreateTime, "createTime should be >= " + minCreateTime);
        if (maxCreateTime != -1) assertTrue(meter.getCreateTime() <= maxCreateTime, "createTime should be <= " + maxCreateTime);
    }

    static class TimeValidation {
        long beforeStart = -1;
        long afterStart = -1;
        long beforeStop = -1;
        long afterStop = -1;
        long beforeProgress = -1;
        long afterProgress = -1;
        long expectedCreateTime = -1;
        long expectedStartTime = -1;
        long expectedStopTime = -1;
        long expectedLastCurrentTime = -1;
    }

    TimeValidation fromStarted(final Meter meter) {
        final TimeValidation tv = new TimeValidation();
        tv.expectedCreateTime = meter.getCreateTime();
        tv.expectedStartTime = meter.getLastCurrentTime();
        return tv;
    }


    /**
     * Provides log level scenarios for parameterized tests.
     *
     * @return Stream of arguments containing (debugEnabled, infoEnabled, traceEnabled).
     */
    public static Stream<Level> logLevelScenarios() {
        return Stream.of(
                // Full logging
                Level.TRACE,
                Level.DEBUG,
                Level.INFO,
                Level.WARN,
                Level.ERROR,
                // No logging
                Level.NONE
        );
    }

    /**
     * Configures the mock loggers used by the Meter based on the provided flags.
     *
     * @param logger The base logger used to derive message and data loggers.
     * @param level  The log level to set for both loggers.
     */
    public static void configureLogger(final Logger logger, final Level level) {
        final Logger messageLogger = org.slf4j.LoggerFactory.getLogger(
                MeterConfig.messagePrefix + logger.getName() + MeterConfig.messageSuffix);
        final MockLogger mockMessageLogger = (MockLogger) messageLogger;
        mockMessageLogger.setLevel(level);

        final Logger dataLogger = org.slf4j.LoggerFactory.getLogger(
                MeterConfig.dataPrefix + logger.getName() + MeterConfig.dataSuffix);
        final MockLogger mockDataLogger = (MockLogger) dataLogger;
        mockDataLogger.setLevel(level);
    }

    /**
     * Represents an expected log event for assertion.
     */
    public static class ExpectedLogEvent {
        final Level level;
        final Marker marker;
        final String[] messageParts;

        ExpectedLogEvent(final Level level, final Marker marker, final String... messageParts) {
            this.level = level;
            this.marker = marker;
            this.messageParts = messageParts;
        }
    }

    /**
     * Factory method to create an ExpectedLogEvent.
     *
     * @param level        The expected log level.
     * @param marker       The expected marker.
     * @param messageParts Optional message parts (arguments) expected in the log.
     * @return A new ExpectedLogEvent instance.
     */
    public static ExpectedLogEvent event(final Level level, final Marker marker, final String... messageParts) {
        return new ExpectedLogEvent(level, marker, messageParts);
    }

    /**
     * Asserts that the logger contains the expected events, filtered by the enabled log levels.
     *
     * @param logger The logger to check.
     * @param level  The expected log level.
     * @param events The list of ideal expected events (assuming all levels enabled).
     */
    public static void assertLogs(final Logger logger, final Level level, final ExpectedLogEvent... events) {
        final List<ExpectedLogEvent> filteredEvents =
                Arrays.stream(events)
                        .filter(e -> e.level.ordinal() <= level.ordinal())
                        .collect(Collectors.toList());

        AssertLogger.assertEventCount(logger, filteredEvents.size());

        for (int i = 0; i < filteredEvents.size(); i++) {
            final ExpectedLogEvent e = filteredEvents.get(i);
            if (e.messageParts != null && e.messageParts.length > 0) {
                AssertLogger.assertEvent(logger, i, e.level, e.marker, e.messageParts);
            } else {
                AssertLogger.assertEvent(logger, i, e.level, e.marker);
            }
        }
    }
}
