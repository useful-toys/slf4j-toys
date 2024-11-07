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

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.impl.TestLogger;
import org.slf4j.impl.TestLoggerEvent;
import org.usefultoys.slf4j.LoggerFactory;

/**
 * @author Daniel Felix Ferber
 */
@SuppressWarnings("UnusedAssignment")
public class MeterClosableTest {

    static final String meterCategory = "category";
    static final TestLogger logger = (TestLogger) LoggerFactory.getLogger(meterCategory);

    public MeterClosableTest() {
    }

    @BeforeClass
    public static void configureMeterSettings() {
        MeterConfig.progressPeriodMilliseconds = 0;
        MeterConfig.printCategory = false;
        MeterConfig.printStatus = true;
    }

    @Before
    public void clearEvents() {
        logger.clearEvents();
        MeterConfig.printCategory = false;
    }

    @Test
    public void testWithStartWithOk() {
        Meter m;
        try (Meter m2 = m = new Meter(logger, "testWithStartWithOk").start()) {
            Assert.assertEquals(m, Meter.getCurrentInstance());
            m2.ok();
        }

        Assert.assertTrue(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        Assert.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Markers.MSG_OK, stopEvent.getMarker());
        Assert.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Markers.DATA_OK, stopDataEvent.getMarker());
    }

    @Test
    public void testNoStartWithOk() {
        Meter m;
        try (Meter m2 = m = new Meter(logger, "testNoStartWithOk")) {
            Assert.assertNotEquals(m, Meter.getCurrentInstance());
            m2.ok();
        }

        Assert.assertTrue(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(3, logger.getEventCount());
        final TestLoggerEvent startErrorEvent = logger.getEvent(0);
        final TestLoggerEvent stopEvent = logger.getEvent(1);
        final TestLoggerEvent stopDataEvent = logger.getEvent(2);
        Assert.assertEquals(Markers.INCONSISTENT_OK, startErrorEvent.getMarker());
        Assert.assertEquals(Markers.MSG_OK, stopEvent.getMarker());
        Assert.assertEquals(Markers.DATA_OK, stopDataEvent.getMarker());
    }

    @Test
    public void testNoStartNoOk() {
        Meter m;
        try (Meter m2 = m = new Meter(logger, "testNoStartNoOk")) {
            Assert.assertNotEquals(m, Meter.getCurrentInstance());
        }

        Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertTrue(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals("try-with-resources", m.getFailPath());
        Assert.assertEquals(3, logger.getEventCount());
        final TestLoggerEvent startErrorEvent = logger.getEvent(0);
        final TestLoggerEvent stopEvent = logger.getEvent(1);
        final TestLoggerEvent stopDataEvent = logger.getEvent(2);
        Assert.assertEquals(Markers.INCONSISTENT_CLOSE, startErrorEvent.getMarker());
        Assert.assertEquals(Markers.MSG_FAIL, stopEvent.getMarker());
        Assert.assertEquals(Markers.DATA_FAIL, stopDataEvent.getMarker());
        Assert.assertTrue(stopEvent.getFormattedMessage().contains("try-with-resources"));
    }

    @Test
    public void testWithStartNoOk() {
        Meter m;
        try (Meter m2 = m = new Meter(logger, "testWithStartNoOk").start()) {
            Assert.assertEquals(m, Meter.getCurrentInstance());
        }

        Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertTrue(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals("try-with-resources", m.getFailPath());
        Assert.assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        Assert.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Markers.MSG_FAIL, stopEvent.getMarker());
        Assert.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Markers.DATA_FAIL, stopDataEvent.getMarker());
        Assert.assertTrue(stopEvent.getFormattedMessage().contains("try-with-resources"));
    }

    @Test
    public void testWithStartWithReject() {
        Meter m;
        try (Meter m2 = m = new Meter(logger, "testWithStartWithReject").start()) {
            Assert.assertEquals(m, Meter.getCurrentInstance());
            m2.reject("a");
        }

        Assert.assertFalse(m.isOK());
        Assert.assertTrue(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        Assert.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Markers.MSG_REJECT, stopEvent.getMarker());
        Assert.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Markers.DATA_REJECT, stopDataEvent.getMarker());
    }

    @Test
    public void testWithStartWithFail() {
        Meter m;
        try (Meter m2 = m = new Meter(logger, "testWithStartWithFail").start()) {
            Assert.assertEquals(m, Meter.getCurrentInstance());
            m2.fail("a");
        }

        Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertTrue(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        Assert.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Markers.MSG_FAIL, stopEvent.getMarker());
        Assert.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Markers.DATA_FAIL, stopDataEvent.getMarker());
    }

    @Test
    public void testWithStartWithException() {
        Meter m = null;
        try (Meter m2 = m = new Meter(logger, "testWithStartWithException").start()) {
            Assert.assertEquals(m, Meter.getCurrentInstance());
            throw new RuntimeException("someException");
        } catch (RuntimeException e) {
            // ignore
        }

        Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertTrue(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        Assert.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Markers.MSG_FAIL, stopEvent.getMarker());
        Assert.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Markers.DATA_FAIL, stopDataEvent.getMarker());
        Assert.assertTrue(stopEvent.getFormattedMessage().contains("try-with-resources"));
    }

}
