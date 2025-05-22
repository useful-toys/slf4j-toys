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


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.SessionConfig;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Daniel Felix Ferber
 */
@SuppressWarnings("UnusedAssignment")
public class MeterClosableTest {

    static final String meterCategory = "category";
    static final MockLogger logger = (MockLogger) LoggerFactory.getLogger(meterCategory);

    public MeterClosableTest() {
    }

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeAll
    public static void setupMeterSettings() {
        MeterConfig.progressPeriodMilliseconds = 0;
        MeterConfig.printCategory = false;
        MeterConfig.printStatus = true;
    }

    @AfterAll
    public static void tearDownMeterSettings() {
        MeterConfig.reset();
    }

    @BeforeEach
    void setupLogger() {
        logger.clearEvents();
        logger.setEnabled(true);
    }

    @AfterEach
    void clearLogger() {
        logger.clearEvents();
        logger.setEnabled(true);
    }

    @Test
    public void testWithStartWithOk() {
        final Meter m;
        try (final Meter m2 = m = new Meter(logger, "testWithStartWithOk").start()) {
            assertEquals(m, Meter.getCurrentInstance());
            m2.ok();
        }

        assertTrue(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertFalse(m.isSlow());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailPath());
        assertEquals(4, logger.getEventCount());
        final MockLoggerEvent startEvent = logger.getEvent(0);
        final MockLoggerEvent startDataEvent = logger.getEvent(1);
        final MockLoggerEvent stopEvent = logger.getEvent(2);
        final MockLoggerEvent stopDataEvent = logger.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_OK, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_OK, stopDataEvent.getMarker());
    }

    @Test
    public void testNoStartWithOk() {
        final Meter m;
        try (final Meter m2 = m = new Meter(logger, "testNoStartWithOk")) {
            assertNotEquals(m, Meter.getCurrentInstance());
            m2.ok();
        }

        assertTrue(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertFalse(m.isSlow());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailPath());
        assertEquals(3, logger.getEventCount());
        final MockLoggerEvent startErrorEvent = logger.getEvent(0);
        final MockLoggerEvent stopEvent = logger.getEvent(1);
        final MockLoggerEvent stopDataEvent = logger.getEvent(2);
        assertEquals(Markers.INCONSISTENT_OK, startErrorEvent.getMarker());
        assertEquals(Markers.MSG_OK, stopEvent.getMarker());
        assertEquals(Markers.DATA_OK, stopDataEvent.getMarker());
    }

    @Test
    public void testNoStartWithOkAndPath() {
        final Meter m;
        try (final Meter m2 = m = new Meter(logger, "testNoStartWithOk")) {
            assertNotEquals(m, Meter.getCurrentInstance());
            m2.ok("a");
        }

        assertTrue(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertFalse(m.isSlow());
        assertEquals("a", m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailPath());
        assertEquals(3, logger.getEventCount());
        final MockLoggerEvent startErrorEvent = logger.getEvent(0);
        final MockLoggerEvent stopEvent = logger.getEvent(1);
        final MockLoggerEvent stopDataEvent = logger.getEvent(2);
        assertEquals(Markers.INCONSISTENT_OK, startErrorEvent.getMarker());
        assertEquals(Markers.MSG_OK, stopEvent.getMarker());
        assertEquals(Markers.DATA_OK, stopDataEvent.getMarker());
    }

    @Test
    public void testNoStartNoOk() {
        final Meter m;
        try (final Meter m2 = m = new Meter(logger, "testNoStartNoOk")) {
            assertNotEquals(m, Meter.getCurrentInstance());
        }

        assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertTrue(m.isFail());
        assertFalse(m.isSlow());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        // The Meter with try catch pattern cannot know if an exception happened or not.
        // Therefore, it assumes failure unless ok() is explicitly called.
        assertEquals("try-with-resources", m.getFailPath());
        assertEquals(3, logger.getEventCount());
        final MockLoggerEvent startErrorEvent = logger.getEvent(0);
        final MockLoggerEvent stopEvent = logger.getEvent(1);
        final MockLoggerEvent stopDataEvent = logger.getEvent(2);
        assertEquals(Markers.INCONSISTENT_CLOSE, startErrorEvent.getMarker());
        assertEquals(Markers.MSG_FAIL, stopEvent.getMarker());
        assertEquals(Markers.DATA_FAIL, stopDataEvent.getMarker());
        assertTrue(stopEvent.getFormattedMessage().contains("try-with-resources"));
    }

    @Test
    public void testWithStartNoOk() {
        final Meter m;
        try (final Meter m2 = m = new Meter(logger, "testWithStartNoOk").start()) {
            assertEquals(m, Meter.getCurrentInstance());
        }

        assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertTrue(m.isFail());
        assertFalse(m.isSlow());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertEquals("try-with-resources", m.getFailPath());
        assertEquals(4, logger.getEventCount());
        final MockLoggerEvent startEvent = logger.getEvent(0);
        final MockLoggerEvent startDataEvent = logger.getEvent(1);
        final MockLoggerEvent stopEvent = logger.getEvent(2);
        final MockLoggerEvent stopDataEvent = logger.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_FAIL, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_FAIL, stopDataEvent.getMarker());
        assertTrue(stopEvent.getFormattedMessage().contains("try-with-resources"));
    }

    @Test
    public void testWithStartWithReject() {
        final Meter m;
        try (final Meter m2 = m = new Meter(logger, "testWithStartWithReject").start()) {
            assertEquals(m, Meter.getCurrentInstance());
            m2.reject("a");
        }

        assertFalse(m.isOK());
        assertTrue(m.isReject());
        assertFalse(m.isFail());
        assertFalse(m.isSlow());
        assertNull(m.getOkPath());
        assertEquals("a", m.getRejectPath());
        assertNull(m.getFailPath());
        assertEquals(4, logger.getEventCount());
        final MockLoggerEvent startEvent = logger.getEvent(0);
        final MockLoggerEvent startDataEvent = logger.getEvent(1);
        final MockLoggerEvent stopEvent = logger.getEvent(2);
        final MockLoggerEvent stopDataEvent = logger.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_REJECT, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_REJECT, stopDataEvent.getMarker());
    }

    @Test
    public void testWithStartWithFail() {
        final Meter m;
        try (final Meter m2 = m = new Meter(logger, "testWithStartWithFail").start()) {
            assertEquals(m, Meter.getCurrentInstance());
            m2.fail("a");
        }

        assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertTrue(m.isFail());
        assertFalse(m.isSlow());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertEquals("a", m.getFailPath());
        assertEquals(4, logger.getEventCount());
        final MockLoggerEvent startEvent = logger.getEvent(0);
        final MockLoggerEvent startDataEvent = logger.getEvent(1);
        final MockLoggerEvent stopEvent = logger.getEvent(2);
        final MockLoggerEvent stopDataEvent = logger.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_FAIL, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_FAIL, stopDataEvent.getMarker());
    }

    @Test
    public void testWithStartWithException() {
        Meter m = null;
        try (final Meter m2 = m = new Meter(logger, "testWithStartWithException").start()) {
            assertEquals(m, Meter.getCurrentInstance());
            throw new RuntimeException("someException");
        } catch (final RuntimeException e) {
            // ignore
        }

        assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertTrue(m.isFail());
        assertFalse(m.isSlow());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        // The Meter with try catch pattern cannot get the exception itself, but reports "try-with-resources" failure.
        assertEquals("try-with-resources", m.getFailPath());
        assertEquals(4, logger.getEventCount());
        final MockLoggerEvent startEvent = logger.getEvent(0);
        final MockLoggerEvent startDataEvent = logger.getEvent(1);
        final MockLoggerEvent stopEvent = logger.getEvent(2);
        final MockLoggerEvent stopDataEvent = logger.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_FAIL, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_FAIL, stopDataEvent.getMarker());
        assertTrue(stopEvent.getFormattedMessage().contains("try-with-resources"));
    }
}
