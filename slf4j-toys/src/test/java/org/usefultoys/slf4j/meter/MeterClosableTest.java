/*
 * Copyright 2024 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.usefultoys.slf4j.meter;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.SessionConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.nio.charset.Charset;

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
    public static void configureMeterSettings() {
        MeterConfig.progressPeriodMilliseconds = 0;
        MeterConfig.printCategory = false;
        MeterConfig.printStatus = true;
    }

    @BeforeEach
    public void clearEvents() {
        logger.clearEvents();
        MeterConfig.printCategory = false;
    }

    @Test
    public void testWithStartWithOk() {
        Meter m;
        try (Meter m2 = m = new Meter(logger, "testWithStartWithOk").start()) {
            Assertions.assertEquals(m, Meter.getCurrentInstance());
            m2.ok();
        }

        Assertions.assertTrue(m.isOK());
        Assertions.assertFalse(m.isReject());
        Assertions.assertFalse(m.isFail());
        Assertions.assertFalse(m.isSlow());
        Assertions.assertEquals(4, logger.getEventCount());
        final MockLoggerEvent startEvent = logger.getEvent(0);
        final MockLoggerEvent startDataEvent = logger.getEvent(1);
        final MockLoggerEvent stopEvent = logger.getEvent(2);
        final MockLoggerEvent stopDataEvent = logger.getEvent(3);
        Assertions.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assertions.assertEquals(Markers.MSG_OK, stopEvent.getMarker());
        Assertions.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assertions.assertEquals(Markers.DATA_OK, stopDataEvent.getMarker());
    }

    @Test
    public void testNoStartWithOk() {
        Meter m;
        try (Meter m2 = m = new Meter(logger, "testNoStartWithOk")) {
            Assertions.assertNotEquals(m, Meter.getCurrentInstance());
            m2.ok();
        }

        Assertions.assertTrue(m.isOK());
        Assertions.assertFalse(m.isReject());
        Assertions.assertFalse(m.isFail());
        Assertions.assertFalse(m.isSlow());
        Assertions.assertEquals(3, logger.getEventCount());
        final MockLoggerEvent startErrorEvent = logger.getEvent(0);
        final MockLoggerEvent stopEvent = logger.getEvent(1);
        final MockLoggerEvent stopDataEvent = logger.getEvent(2);
        Assertions.assertEquals(Markers.INCONSISTENT_OK, startErrorEvent.getMarker());
        Assertions.assertEquals(Markers.MSG_OK, stopEvent.getMarker());
        Assertions.assertEquals(Markers.DATA_OK, stopDataEvent.getMarker());
    }

    @Test
    public void testNoStartNoOk() {
        Meter m;
        try (Meter m2 = m = new Meter(logger, "testNoStartNoOk")) {
            Assertions.assertNotEquals(m, Meter.getCurrentInstance());
        }

        Assertions.assertFalse(m.isOK());
        Assertions.assertFalse(m.isReject());
        Assertions.assertTrue(m.isFail());
        Assertions.assertFalse(m.isSlow());
        Assertions.assertEquals("try-with-resources", m.getFailPath());
        Assertions.assertEquals(3, logger.getEventCount());
        final MockLoggerEvent startErrorEvent = logger.getEvent(0);
        final MockLoggerEvent stopEvent = logger.getEvent(1);
        final MockLoggerEvent stopDataEvent = logger.getEvent(2);
        Assertions.assertEquals(Markers.INCONSISTENT_CLOSE, startErrorEvent.getMarker());
        Assertions.assertEquals(Markers.MSG_FAIL, stopEvent.getMarker());
        Assertions.assertEquals(Markers.DATA_FAIL, stopDataEvent.getMarker());
        Assertions.assertTrue(stopEvent.getFormattedMessage().contains("try-with-resources"));
    }

    @Test
    public void testWithStartNoOk() {
        Meter m;
        try (Meter m2 = m = new Meter(logger, "testWithStartNoOk").start()) {
            Assertions.assertEquals(m, Meter.getCurrentInstance());
        }

        Assertions.assertFalse(m.isOK());
        Assertions.assertFalse(m.isReject());
        Assertions.assertTrue(m.isFail());
        Assertions.assertFalse(m.isSlow());
        Assertions.assertEquals("try-with-resources", m.getFailPath());
        Assertions.assertEquals(4, logger.getEventCount());
        final MockLoggerEvent startEvent = logger.getEvent(0);
        final MockLoggerEvent startDataEvent = logger.getEvent(1);
        final MockLoggerEvent stopEvent = logger.getEvent(2);
        final MockLoggerEvent stopDataEvent = logger.getEvent(3);
        Assertions.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assertions.assertEquals(Markers.MSG_FAIL, stopEvent.getMarker());
        Assertions.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assertions.assertEquals(Markers.DATA_FAIL, stopDataEvent.getMarker());
        Assertions.assertTrue(stopEvent.getFormattedMessage().contains("try-with-resources"));
    }

    @Test
    public void testWithStartWithReject() {
        Meter m;
        try (Meter m2 = m = new Meter(logger, "testWithStartWithReject").start()) {
            Assertions.assertEquals(m, Meter.getCurrentInstance());
            m2.reject("a");
        }

        Assertions.assertFalse(m.isOK());
        Assertions.assertTrue(m.isReject());
        Assertions.assertFalse(m.isFail());
        Assertions.assertFalse(m.isSlow());
        Assertions.assertEquals(4, logger.getEventCount());
        final MockLoggerEvent startEvent = logger.getEvent(0);
        final MockLoggerEvent startDataEvent = logger.getEvent(1);
        final MockLoggerEvent stopEvent = logger.getEvent(2);
        final MockLoggerEvent stopDataEvent = logger.getEvent(3);
        Assertions.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assertions.assertEquals(Markers.MSG_REJECT, stopEvent.getMarker());
        Assertions.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assertions.assertEquals(Markers.DATA_REJECT, stopDataEvent.getMarker());
    }

    @Test
    public void testWithStartWithFail() {
        Meter m;
        try (Meter m2 = m = new Meter(logger, "testWithStartWithFail").start()) {
            Assertions.assertEquals(m, Meter.getCurrentInstance());
            m2.fail("a");
        }

        Assertions.assertFalse(m.isOK());
        Assertions.assertFalse(m.isReject());
        Assertions.assertTrue(m.isFail());
        Assertions.assertFalse(m.isSlow());
        Assertions.assertEquals(4, logger.getEventCount());
        final MockLoggerEvent startEvent = logger.getEvent(0);
        final MockLoggerEvent startDataEvent = logger.getEvent(1);
        final MockLoggerEvent stopEvent = logger.getEvent(2);
        final MockLoggerEvent stopDataEvent = logger.getEvent(3);
        Assertions.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assertions.assertEquals(Markers.MSG_FAIL, stopEvent.getMarker());
        Assertions.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assertions.assertEquals(Markers.DATA_FAIL, stopDataEvent.getMarker());
    }

    @Test
    public void testWithStartWithException() {
        Meter m = null;
        try (Meter m2 = m = new Meter(logger, "testWithStartWithException").start()) {
            Assertions.assertEquals(m, Meter.getCurrentInstance());
            throw new RuntimeException("someException");
        } catch (RuntimeException e) {
            // ignore
        }

        Assertions.assertFalse(m.isOK());
        Assertions.assertFalse(m.isReject());
        Assertions.assertTrue(m.isFail());
        Assertions.assertFalse(m.isSlow());
        Assertions.assertEquals(4, logger.getEventCount());
        final MockLoggerEvent startEvent = logger.getEvent(0);
        final MockLoggerEvent startDataEvent = logger.getEvent(1);
        final MockLoggerEvent stopEvent = logger.getEvent(2);
        final MockLoggerEvent stopDataEvent = logger.getEvent(3);
        Assertions.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assertions.assertEquals(Markers.MSG_FAIL, stopEvent.getMarker());
        Assertions.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assertions.assertEquals(Markers.DATA_FAIL, stopDataEvent.getMarker());
        Assertions.assertTrue(stopEvent.getFormattedMessage().contains("try-with-resources"));
    }

}
