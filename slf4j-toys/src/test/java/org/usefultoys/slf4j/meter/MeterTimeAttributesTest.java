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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.impl.MockLogger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.SessionConfig;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the correct update of time-related attributes in the {@link Meter} class
 * across various lifecycle events.
 *
 * @author Daniel Felix Ferber
 */
public class MeterTimeAttributesTest {


    @BeforeAll
    static void validateConsistentCharset() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    MockLogger logger = (MockLogger) LoggerFactory.getLogger("Test");

    public MeterTimeAttributesTest() {
        logger.setEnabled(false);
    }


    @BeforeEach
    public void clearEvents() {
        logger.clearEvents();
    }

    /**
     * Helper method to assert time attributes at different stages.
     * This method asserts the *stored* time attributes of the Meter.
     * Dynamic "collected" times are asserted separately due to their nature.
     */
    private void assertStoredTimeAttributes(Meter m, long expectedCreateTime, long expectedStartTime, long expectedStopTime,
                                            long expectedWaitingTime, long expectedExecutionTime, long expectedLastProgressTime,
                                            String stage) {
        assertEquals(expectedCreateTime, m.getCreateTime(), stage + ": createTime should match expected");
        assertEquals(expectedStartTime, m.getStartTime(), stage + ": startTime should match expected");
        assertEquals(expectedStopTime, m.getStopTime(), stage + ": stopTime should match expected");
        assertEquals(expectedWaitingTime, m.getWaitingTime(), stage + ": waitingTime should match expected");
        assertEquals(expectedExecutionTime, m.getExecutionTime(), stage + ": executionTime should match expected");
        assertEquals(expectedLastProgressTime, m.getLastProgressTime(), stage + ": lastProgressTime should match expected");
    }


    @Test
    @DisplayName("Should correctly update time attributes on start and ok()")
    public void shouldUpdateTimesOnStartAndOk() {
        final long now0 = System.nanoTime();
        final Meter m = new Meter(logger);
        final long createTime = m.getCreateTime();

        // After creation, before start()
        assertStoredTimeAttributes(m, createTime, 0, 0, 0, 0, 0, "After creation");
        assertTrue(createTime >= now0, "createTime should be after now0");
        assertTrue(createTime <= System.nanoTime(), "createTime should be before current nano time");
        assertTrue(m.collectCurrentWaitingTime() > 0, "collectedWaitingTime should be positive if not started");
        assertEquals(0, m.collectCurrentExecutionTime(), "collectedExecutionTime should be 0 if not started");


        final long now1 = System.nanoTime();
        m.start();
        final long startTime = m.getStartTime();
        final long waitingTime = startTime - createTime;

        // After start()
        assertStoredTimeAttributes(m, createTime, startTime, 0, waitingTime, 0, startTime, "After start");
        assertTrue(startTime >= now1, "startTime should be after now1");
        assertTrue(startTime <= System.nanoTime(), "startTime should be before current nano time");
        assertEquals(waitingTime, m.collectCurrentWaitingTime(), "collectedWaitingTime should equal waitingTime if started");
        assertTrue(m.collectCurrentExecutionTime() > 0, "collectedExecutionTime should be positive if started but not stopped");


        final long now2 = System.nanoTime();
        m.ok();
        final long stopTime = m.getStopTime();
        final long executionTime = stopTime - startTime;

        // After ok()
        assertStoredTimeAttributes(m, createTime, startTime, stopTime, waitingTime, executionTime, startTime, "After ok");
        assertTrue(stopTime >= now2, "stopTime should be after now2");
        assertTrue(stopTime <= System.nanoTime(), "stopTime should be before current nano time");
        assertEquals(waitingTime, m.collectCurrentWaitingTime(), "collectedWaitingTime should equal waitingTime if stopped");
        assertEquals(executionTime, m.collectCurrentExecutionTime(), "collectedExecutionTime should equal executionTime if stopped");
    }

    @Test
    @DisplayName("Should correctly update time attributes on start and ok(flow)")
    public void shouldUpdateTimesOnStartAndOkFlow() {
        final long now0 = System.nanoTime();
        final Meter m = new Meter(logger);
        final long createTime = m.getCreateTime();

        // After creation, before start()
        assertStoredTimeAttributes(m, createTime, 0, 0, 0, 0, 0, "After creation");
        assertTrue(m.collectCurrentWaitingTime() > 0, "collectedWaitingTime should be positive if not started");
        assertEquals(0, m.collectCurrentExecutionTime(), "collectedExecutionTime should be 0 if not started");

        final long now1 = System.nanoTime();
        m.start();
        final long startTime = m.getStartTime();
        final long waitingTime = startTime - createTime;

        // After start()
        assertStoredTimeAttributes(m, createTime, startTime, 0, waitingTime, 0, startTime, "After start");
        assertEquals(waitingTime, m.collectCurrentWaitingTime(), "collectedWaitingTime should equal waitingTime if started");
        assertTrue(m.collectCurrentExecutionTime() > 0, "collectedExecutionTime should be positive if started but not stopped");

        final long now2 = System.nanoTime();
        m.ok("Flow");
        final long stopTime = m.getStopTime();
        final long executionTime = stopTime - startTime;

        // After ok(flow)
        assertStoredTimeAttributes(m, createTime, startTime, stopTime, waitingTime, executionTime, startTime, "After ok(flow)");
        assertEquals(waitingTime, m.collectCurrentWaitingTime(), "collectedWaitingTime should equal waitingTime if stopped");
        assertEquals(executionTime, m.collectCurrentExecutionTime(), "collectedExecutionTime should equal executionTime if stopped");
    }

    @Test
    @DisplayName("Should correctly update time attributes on start and reject()")
    public void shouldUpdateTimesOnStartAndReject() {
        final long now0 = System.nanoTime();
        final Meter m = new Meter(logger);
        final long createTime = m.getCreateTime();

        // After creation, before start()
        assertStoredTimeAttributes(m, createTime, 0, 0, 0, 0, 0, "After creation");
        assertTrue(m.collectCurrentWaitingTime() > 0, "collectedWaitingTime should be positive if not started");
        assertEquals(0, m.collectCurrentExecutionTime(), "collectedExecutionTime should be 0 if not started");

        final long now1 = System.nanoTime();
        m.start();
        final long startTime = m.getStartTime();
        final long waitingTime = startTime - createTime;

        // After start()
        assertStoredTimeAttributes(m, createTime, startTime, 0, waitingTime, 0, startTime, "After start");
        assertEquals(waitingTime, m.collectCurrentWaitingTime(), "collectedWaitingTime should equal waitingTime if started");
        assertTrue(m.collectCurrentExecutionTime() > 0, "collectedExecutionTime should be positive if started but not stopped");

        final long now2 = System.nanoTime();
        m.reject("Reason");
        final long stopTime = m.getStopTime();
        final long executionTime = stopTime - startTime;

        // After reject()
        assertStoredTimeAttributes(m, createTime, startTime, stopTime, waitingTime, executionTime, startTime, "After reject");
        assertEquals(waitingTime, m.collectCurrentWaitingTime(), "collectedWaitingTime should equal waitingTime if stopped");
        assertEquals(executionTime, m.collectCurrentExecutionTime(), "collectedExecutionTime should equal executionTime if stopped");
    }

    @Test
    @DisplayName("Should correctly update time attributes on start and fail()")
    public void shouldUpdateTimesOnStartAndFail() {
        final long now0 = System.nanoTime();
        final Meter m = new Meter(logger);
        final long createTime = m.getCreateTime();

        // After creation, before start()
        assertStoredTimeAttributes(m, createTime, 0, 0, 0, 0, 0, "After creation");
        assertTrue(m.collectCurrentWaitingTime() > 0, "collectedWaitingTime should be positive if not started");
        assertEquals(0, m.collectCurrentExecutionTime(), "collectedExecutionTime should be 0 if not started");

        final long now1 = System.nanoTime();
        m.start();
        final long startTime = m.getStartTime();
        final long waitingTime = startTime - createTime;

        // After start()
        assertStoredTimeAttributes(m, createTime, startTime, 0, waitingTime, 0, startTime, "After start");
        assertEquals(waitingTime, m.collectCurrentWaitingTime(), "collectedWaitingTime should equal waitingTime if started");
        assertTrue(m.collectCurrentExecutionTime() > 0, "collectedExecutionTime should be positive if started but not stopped");

        final long now2 = System.nanoTime();
        m.fail(new IllegalStateException("Error"));
        final long stopTime = m.getStopTime();
        final long executionTime = stopTime - startTime;

        // After fail()
        assertStoredTimeAttributes(m, createTime, startTime, stopTime, waitingTime, executionTime, startTime, "After fail");
        assertEquals(waitingTime, m.collectCurrentWaitingTime(), "collectedWaitingTime should equal waitingTime if stopped");
        assertEquals(executionTime, m.collectCurrentExecutionTime(), "collectedExecutionTime should equal executionTime if stopped");
    }

    @Test
    @DisplayName("Should correctly update time attributes on start and close() via try-with-resources")
    public void shouldHandleCloseViaTryWithResources() {
        final long now0 = System.nanoTime();
        Meter m = new Meter(logger);
        final long createTime = m.getCreateTime();

        // After creation, before start()
        assertStoredTimeAttributes(m, createTime, 0, 0, 0, 0, 0, "After creation");
        assertTrue(m.collectCurrentWaitingTime() > 0, "collectedWaitingTime should be positive if not started");
        assertEquals(0, m.collectCurrentExecutionTime(), "collectedExecutionTime should be 0 if not started");

        final long now1 = System.nanoTime();
        m.start();
        final long startTime = m.getStartTime();
        final long waitingTime = startTime - createTime;

        // After start()
        assertStoredTimeAttributes(m, createTime, startTime, 0, waitingTime, 0, startTime, "After start");
        assertEquals(waitingTime, m.collectCurrentWaitingTime(), "collectedWaitingTime should equal waitingTime if started");
        assertTrue(m.collectCurrentExecutionTime() > 0, "collectedExecutionTime should be positive if started but not stopped");

        final long now2 = System.nanoTime();
        try (Meter meterToClose = m) {
            // Meter is already 'm', so just let it close
        }
        final long stopTime = m.getStopTime();
        final long executionTime = stopTime - startTime;

        // After close()
        assertStoredTimeAttributes(m, createTime, startTime, stopTime, waitingTime, executionTime, startTime, "After close");
        assertTrue(stopTime >= now2, "stopTime should be after now2");
        assertTrue(stopTime <= System.nanoTime(), "stopTime should be before current nano time");
        assertEquals(waitingTime, m.collectCurrentWaitingTime(), "collectedWaitingTime should equal waitingTime if stopped");
        assertEquals(executionTime, m.collectCurrentExecutionTime(), "collectedExecutionTime should equal executionTime if stopped");
    }

    @Test
    @DisplayName("Should correctly update time attributes on start, progress(), and ok()")
    public void shouldUpdateTimesOnProgressAndOk() throws InterruptedException {
        final long now0 = System.nanoTime();
        final Meter m = new Meter(logger).iterations(100); // Set iterations for progress
        final long createTime = m.getCreateTime();

        // After creation, before start()
        assertStoredTimeAttributes(m, createTime, 0, 0, 0, 0, 0, "After creation");
        assertTrue(m.collectCurrentWaitingTime() > 0, "collectedWaitingTime should be positive if not started");
        assertEquals(0, m.collectCurrentExecutionTime(), "collectedExecutionTime should be 0 if not started");

        final long now1 = System.nanoTime();
        m.start();
        final long startTime = m.getStartTime();
        final long waitingTime = startTime - createTime;

        // After start()
        assertStoredTimeAttributes(m, createTime, startTime, 0, waitingTime, 0, startTime, "After start");
        assertEquals(waitingTime, m.collectCurrentWaitingTime(), "collectedWaitingTime should equal waitingTime if started");
        assertTrue(m.collectCurrentExecutionTime() > 0, "collectedExecutionTime should be positive if started but not stopped");

        Thread.sleep(MeterConfig.progressPeriodMilliseconds + 10); // Ensure progress logs
        final long now2 = System.nanoTime();
        m.incBy(10).progress();
        final long lastProgressTime1 = m.getLastProgressTime();
        final long expectedExecutionTime1 = m.getLastCurrentTime() - m.getStartTime();

        // After first progress()
        assertStoredTimeAttributes(m, createTime, startTime, 0, waitingTime, expectedExecutionTime1, lastProgressTime1, "After first progress");
        assertTrue(lastProgressTime1 >= now2, "lastProgressTime after progress should be after now2");
        assertTrue(lastProgressTime1 <= System.nanoTime(), "lastProgressTime after progress should be before current nano time");
        assertEquals(waitingTime, m.collectCurrentWaitingTime(), "collectedWaitingTime should equal waitingTime after progress");
        assertTrue(m.collectCurrentExecutionTime() > 0, "collectedExecutionTime should be positive after progress but not stopped");


        Thread.sleep(10); // Small delay before final stop
        final long now3 = System.nanoTime();
        m.ok();
        final long stopTime = m.getStopTime();
        final long executionTime = stopTime - startTime;

        // After ok()
        assertStoredTimeAttributes(m, createTime, startTime, stopTime, waitingTime, executionTime, lastProgressTime1, "After ok");
        assertTrue(stopTime >= now3, "stopTime should be after now3");
        assertTrue(stopTime <= System.nanoTime(), "stopTime should be before current nano time");
        assertEquals(waitingTime, m.collectCurrentWaitingTime(), "collectedWaitingTime should equal waitingTime if stopped");
        assertEquals(executionTime, m.collectCurrentExecutionTime(), "collectedExecutionTime should equal executionTime if stopped");
    }

    @Test
    @DisplayName("Should correctly update time attributes on start, progress(), and reject()")
    public void shouldUpdateTimesOnProgressAndReject() throws InterruptedException {
        final long now0 = System.nanoTime();
        final Meter m = new Meter(logger).iterations(100); // Set iterations for progress
        final long createTime = m.getCreateTime();

        // After creation, before start()
        assertStoredTimeAttributes(m, createTime, 0, 0, 0, 0, 0, "After creation");
        assertTrue(m.collectCurrentWaitingTime() > 0, "collectedWaitingTime should be positive if not started");
        assertEquals(0, m.collectCurrentExecutionTime(), "collectedExecutionTime should be 0 if not started");

        final long now1 = System.nanoTime();
        m.start();
        final long startTime = m.getStartTime();
        final long waitingTime = startTime - createTime;

        // After start()
        assertStoredTimeAttributes(m, createTime, startTime, 0, waitingTime, 0, startTime, "After start");
        assertEquals(waitingTime, m.collectCurrentWaitingTime(), "collectedWaitingTime should equal waitingTime if started");
        assertTrue(m.collectCurrentExecutionTime() > 0, "collectedExecutionTime should be positive if started but not stopped");

        Thread.sleep(MeterConfig.progressPeriodMilliseconds + 10); // Ensure progress logs
        final long now2 = System.nanoTime();
        m.incBy(10).progress();
        final long lastProgressTime1 = m.getLastProgressTime();
        final long expectedExecutionTime1 = m.getLastCurrentTime() - m.getStartTime();

        // After first progress()
        assertStoredTimeAttributes(m, createTime, startTime, 0, waitingTime, expectedExecutionTime1, lastProgressTime1, "After first progress");
        assertEquals(waitingTime, m.collectCurrentWaitingTime(), "collectedWaitingTime should equal waitingTime after progress");
        assertTrue(m.collectCurrentExecutionTime() > 0, "collectedExecutionTime should be positive after progress but not stopped");

        Thread.sleep(10); // Small delay before final stop
        final long now3 = System.nanoTime();
        m.reject("Reason");
        final long stopTime = m.getStopTime();
        final long executionTime = stopTime - startTime;

        // After reject()
        assertStoredTimeAttributes(m, createTime, startTime, stopTime, waitingTime, executionTime, lastProgressTime1, "After reject");
        assertEquals(waitingTime, m.collectCurrentWaitingTime(), "collectedWaitingTime should equal waitingTime if stopped");
        assertEquals(executionTime, m.collectCurrentExecutionTime(), "collectedExecutionTime should equal executionTime if stopped");
    }

    @Test
    @DisplayName("Should correctly update time attributes on start, progress(), and fail()")
    public void shouldUpdateTimesOnProgressAndFail() throws InterruptedException {
        final long now0 = System.nanoTime();
        final Meter m = new Meter(logger).iterations(100); // Set iterations for progress
        final long createTime = m.getCreateTime();

        // After creation, before start()
        assertStoredTimeAttributes(m, createTime, 0, 0, 0, 0, 0, "After creation");
        assertTrue(m.collectCurrentWaitingTime() > 0, "collectedWaitingTime should be positive if not started");
        assertEquals(0, m.collectCurrentExecutionTime(), "collectedExecutionTime should be 0 if not started");

        final long now1 = System.nanoTime();
        m.start();
        final long startTime = m.getStartTime();
        final long waitingTime = startTime - createTime;

        // After start()
        assertStoredTimeAttributes(m, createTime, startTime, 0, waitingTime, 0, startTime, "After start");
        assertEquals(waitingTime, m.collectCurrentWaitingTime(), "collectedWaitingTime should equal waitingTime if started");
        assertTrue(m.collectCurrentExecutionTime() > 0, "collectedExecutionTime should be positive if started but not stopped");

        Thread.sleep(MeterConfig.progressPeriodMilliseconds + 10); // Ensure progress logs
        final long now2 = System.nanoTime();
        m.incBy(10).progress();
        final long lastProgressTime1 = m.getLastProgressTime();
        final long expectedExecutionTime1 = m.getLastCurrentTime() - m.getStartTime();

        // After first progress()
        assertStoredTimeAttributes(m, createTime, startTime, 0, waitingTime, expectedExecutionTime1, lastProgressTime1, "After first progress");
        assertEquals(waitingTime, m.collectCurrentWaitingTime(), "collectedWaitingTime should equal waitingTime after progress");
        assertTrue(m.collectCurrentExecutionTime() > 0, "collectedExecutionTime should be positive after progress but not stopped");

        Thread.sleep(10); // Small delay before final stop
        final long now3 = System.nanoTime();
        m.fail(new IllegalStateException("Error"));
        final long stopTime = m.getStopTime();
        final long executionTime = stopTime - startTime;

        // After fail()
        assertStoredTimeAttributes(m, createTime, startTime, stopTime, waitingTime, executionTime, lastProgressTime1, "After fail");
        assertEquals(waitingTime, m.collectCurrentWaitingTime(), "collectedWaitingTime should equal waitingTime if stopped");
        assertEquals(executionTime, m.collectCurrentExecutionTime(), "collectedExecutionTime should equal executionTime if stopped");
    }

    @Test
    @DisplayName("Should correctly update time attributes on start, progress(), and close() via try-with-resources")
    public void shouldUpdateTimesOnProgressAndClose() throws InterruptedException {
        final long now0 = System.nanoTime();
        Meter m = new Meter(logger).iterations(100); // Set iterations for progress
        final long createTime = m.getCreateTime();

        // After creation, before start()
        assertStoredTimeAttributes(m, createTime, 0, 0, 0, 0, 0, "After creation");
        assertTrue(m.collectCurrentWaitingTime() > 0, "collectedWaitingTime should be positive if not started");
        assertEquals(0, m.collectCurrentExecutionTime(), "collectedExecutionTime should be 0 if not started");

        final long now1 = System.nanoTime();
        m.start();
        final long startTime = m.getStartTime();
        final long waitingTime = startTime - createTime;

        // After start()
        assertStoredTimeAttributes(m, createTime, startTime, 0, waitingTime, 0, startTime, "After start");
        assertEquals(waitingTime, m.collectCurrentWaitingTime(), "collectedWaitingTime should equal waitingTime if started");
        assertTrue(m.collectCurrentExecutionTime() > 0, "collectedExecutionTime should be positive if started but not stopped");

        Thread.sleep(MeterConfig.progressPeriodMilliseconds + 10); // Ensure progress logs
        final long now2 = System.nanoTime();
        m.incBy(10).progress();
        final long lastProgressTime1 = m.getLastProgressTime();
        final long expectedExecutionTime1 = m.getLastCurrentTime() - m.getStartTime();

        // After first progress()
        assertStoredTimeAttributes(m, createTime, startTime, 0, waitingTime, expectedExecutionTime1, lastProgressTime1, "After first progress");
        assertEquals(waitingTime, m.collectCurrentWaitingTime(), "collectedWaitingTime should equal waitingTime after progress");
        assertTrue(m.collectCurrentExecutionTime() > 0, "collectedExecutionTime should be positive after progress but not stopped");

        Thread.sleep(10); // Small delay before final stop
        final long now3 = System.nanoTime();
        try (Meter meterToClose = m) {
            // Meter is already 'm', so just let it close
        }
        final long stopTime = m.getStopTime();
        final long executionTime = stopTime - startTime;

        // After close()
        assertStoredTimeAttributes(m, createTime, startTime, stopTime, waitingTime, executionTime, lastProgressTime1, "After close");
        assertTrue(stopTime >= now3, "stopTime should be after now3");
        assertTrue(stopTime <= System.nanoTime(), "stopTime should be before current nano time");
        assertEquals(waitingTime, m.collectCurrentWaitingTime(), "collectedWaitingTime should equal waitingTime if stopped");
        assertEquals(executionTime, m.collectCurrentExecutionTime(), "collectedExecutionTime should equal executionTime if stopped");
    }

    @Test
    @DisplayName("Should correctly update time attributes on start, two progress() calls, and ok()")
    public void shouldUpdateTimesOnTwoProgressAndOk() throws InterruptedException {
        final long now0 = System.nanoTime();
        final Meter m = new Meter(logger).iterations(100); // Set iterations for progress
        final long createTime = m.getCreateTime();

        // After creation, before start()
        assertStoredTimeAttributes(m, createTime, 0, 0, 0, 0, 0, "After creation");
        assertTrue(m.collectCurrentWaitingTime() > 0, "collectedWaitingTime should be positive if not started");
        assertEquals(0, m.collectCurrentExecutionTime(), "collectedExecutionTime should be 0 if not started");

        final long now1 = System.nanoTime();
        m.start();
        final long startTime = m.getStartTime();
        final long waitingTime = startTime - createTime;

        // After start()
        assertStoredTimeAttributes(m, createTime, startTime, 0, waitingTime, 0, startTime, "After start");
        assertEquals(waitingTime, m.collectCurrentWaitingTime(), "collectedWaitingTime should equal waitingTime if started");
        assertTrue(m.collectCurrentExecutionTime() > 0, "collectedExecutionTime should be positive if started but not stopped");

        Thread.sleep(MeterConfig.progressPeriodMilliseconds + 10); // Ensure progress logs
        final long now2 = System.nanoTime();
        m.incBy(10).progress();
        final long lastProgressTime1 = m.getLastProgressTime();
        final long expectedExecutionTime1 = m.getLastCurrentTime() - m.getStartTime();

        // After first progress()
        assertStoredTimeAttributes(m, createTime, startTime, 0, waitingTime, expectedExecutionTime1, lastProgressTime1, "After first progress");
        assertTrue(lastProgressTime1 >= now2, "lastProgressTime after first progress should be after now2");
        assertTrue(lastProgressTime1 <= System.nanoTime(), "lastProgressTime after first progress should be before current nano time");
        assertEquals(waitingTime, m.collectCurrentWaitingTime(), "collectedWaitingTime should equal waitingTime after first progress");
        assertTrue(m.collectCurrentExecutionTime() > 0, "collectedExecutionTime should be positive after first progress but not stopped");

        Thread.sleep(MeterConfig.progressPeriodMilliseconds + 10); // Ensure progress logs
        final long now3 = System.nanoTime();
        m.incBy(10).progress();
        final long lastProgressTime2 = m.getLastProgressTime();
        final long expectedExecutionTime2 = m.getLastCurrentTime() - m.getStartTime();

        // After second progress()
        assertStoredTimeAttributes(m, createTime, startTime, 0, waitingTime, expectedExecutionTime2, lastProgressTime2, "After second progress");
        assertTrue(lastProgressTime2 >= now3, "lastProgressTime after second progress should be after now3");
        assertTrue(lastProgressTime2 > lastProgressTime1, "lastProgressTime should be updated after second progress");
        assertTrue(lastProgressTime2 <= System.nanoTime(), "lastProgressTime after second progress should be before current nano time");
        assertEquals(waitingTime, m.collectCurrentWaitingTime(), "collectedWaitingTime should equal waitingTime after second progress");
        assertTrue(m.collectCurrentExecutionTime() > 0, "collectedExecutionTime should be positive after second progress but not stopped");

        Thread.sleep(10); // Small delay before final stop
        final long now4 = System.nanoTime();
        m.ok();
        final long stopTime = m.getStopTime();
        final long executionTime = stopTime - startTime;

        // After ok()
        assertStoredTimeAttributes(m, createTime, startTime, stopTime, waitingTime, executionTime, lastProgressTime2, "After ok");
        assertTrue(stopTime >= now4, "stopTime should be after now4");
        assertTrue(stopTime <= System.nanoTime(), "stopTime should be before current nano time");
        assertEquals(waitingTime, m.collectCurrentWaitingTime(), "collectedWaitingTime should equal waitingTime if stopped");
        assertEquals(executionTime, m.collectCurrentExecutionTime(), "collectedExecutionTime should equal executionTime if stopped");
    }
}
