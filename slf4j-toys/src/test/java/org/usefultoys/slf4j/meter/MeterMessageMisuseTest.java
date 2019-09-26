/*
 * Copyright 2019 Daniel Felix Ferber
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
import org.usefultoys.slf4j.meter.Meter.IllegalMeterUsage;

/**
 *
 * @author Daniel Felix Ferber
 */
@SuppressWarnings("UnusedAssignment")
public class MeterMessageMisuseTest {

    final String title = "Example of meter misuse.";
    final String meterName = "name";
    final TestLogger logger = (TestLogger) LoggerFactory.getLogger(meterName);
    static final String stackTraceEvidence = MeterMessageMisuseTest.class.getName();

    @BeforeClass
    public static void configureMeterSettings() {
        System.setProperty("meter.progress.period", "0ms");
    }

    @Before
    public void clearEvents() {
        logger.clearEvents();
    }

    @Test
    public void testMeterAlreadyStopped1() {
        final Meter m = new Meter(logger).m(title).start();
        m.ok();
        m.ok();

        Assert.assertTrue(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Markers.MSG_OK, stopEvent1.getMarker());
        Assert.assertEquals(Markers.DATA_OK, stopDataEvent1.getMarker());
        Assert.assertEquals(Markers.MSG_OK, stopEvent2.getMarker());
        Assert.assertEquals(Markers.DATA_OK, stopDataEvent2.getMarker());
        Assert.assertEquals(Markers.INCONSISTENT_OK, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped2() {
        final Meter m = new Meter(logger).m(title).start();
        m.ok();
        m.ok("OK");

        Assert.assertTrue(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Markers.MSG_OK, stopEvent1.getMarker());
        Assert.assertEquals(Markers.DATA_OK, stopDataEvent1.getMarker());
        Assert.assertEquals(Markers.MSG_OK, stopEvent2.getMarker());
        Assert.assertEquals(Markers.DATA_OK, stopDataEvent2.getMarker());
        Assert.assertEquals(Markers.INCONSISTENT_OK, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped3() {
        final Meter m = new Meter(logger).m(title).start();
        m.ok();
        m.reject("REJ");

        Assert.assertFalse(m.isOK());
        Assert.assertTrue(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Markers.MSG_OK, stopEvent1.getMarker());
        Assert.assertEquals(Markers.DATA_OK, stopDataEvent1.getMarker());
        Assert.assertEquals(Markers.MSG_REJECT, stopEvent2.getMarker());
        Assert.assertEquals(Markers.DATA_REJECT, stopDataEvent2.getMarker());
        Assert.assertEquals(Markers.INCONSISTENT_REJECT, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped4() {
        final Meter m = new Meter(logger).m(title).start();
        m.ok();
        m.fail(new IllegalStateException());

        Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertTrue(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Markers.MSG_OK, stopEvent1.getMarker());
        Assert.assertEquals(Markers.DATA_OK, stopDataEvent1.getMarker());
        Assert.assertEquals(Markers.MSG_FAIL, stopEvent2.getMarker());
        Assert.assertEquals(Markers.DATA_FAIL, stopDataEvent2.getMarker());
        Assert.assertEquals(Markers.INCONSISTENT_FAIL, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped5() {
        final Meter m = new Meter(logger).m(title).start();
        m.ok("OK");
        m.ok();

        Assert.assertTrue(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Markers.MSG_OK, stopEvent1.getMarker());
        Assert.assertEquals(Markers.DATA_OK, stopDataEvent1.getMarker());
        Assert.assertEquals(Markers.MSG_OK, stopEvent2.getMarker());
        Assert.assertEquals(Markers.DATA_OK, stopDataEvent2.getMarker());
        Assert.assertEquals(Markers.INCONSISTENT_OK, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped6() {
        final Meter m = new Meter(logger).m(title).start();
        m.ok("OK");
        m.ok("OK");

        Assert.assertTrue(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Markers.MSG_OK, stopEvent1.getMarker());
        Assert.assertEquals(Markers.DATA_OK, stopDataEvent1.getMarker());
        Assert.assertEquals(Markers.MSG_OK, stopEvent2.getMarker());
        Assert.assertEquals(Markers.DATA_OK, stopDataEvent2.getMarker());
        Assert.assertEquals(Markers.INCONSISTENT_OK, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped7() {
        final Meter m = new Meter(logger).m(title).start();
        m.ok("OK");
        m.reject("REJ");

        Assert.assertFalse(m.isOK());
        Assert.assertTrue(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Markers.MSG_OK, stopEvent1.getMarker());
        Assert.assertEquals(Markers.DATA_OK, stopDataEvent1.getMarker());
        Assert.assertEquals(Markers.MSG_REJECT, stopEvent2.getMarker());
        Assert.assertEquals(Markers.DATA_REJECT, stopDataEvent2.getMarker());
        Assert.assertEquals(Markers.INCONSISTENT_REJECT, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped8() {
        final Meter m = new Meter(logger).m(title).start();
        m.ok("OK");
        m.fail(new IllegalStateException());

        Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertTrue(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Markers.MSG_OK, stopEvent1.getMarker());
        Assert.assertEquals(Markers.DATA_OK, stopDataEvent1.getMarker());
        Assert.assertEquals(Markers.MSG_FAIL, stopEvent2.getMarker());
        Assert.assertEquals(Markers.DATA_FAIL, stopDataEvent2.getMarker());
        Assert.assertEquals(Markers.INCONSISTENT_FAIL, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped9() {
        final Meter m = new Meter(logger).m(title).start();
        m.reject("REJ");
        m.ok();

        Assert.assertTrue(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Markers.MSG_REJECT, stopEvent1.getMarker());
        Assert.assertEquals(Markers.DATA_REJECT, stopDataEvent1.getMarker());
        Assert.assertEquals(Markers.MSG_OK, stopEvent2.getMarker());
        Assert.assertEquals(Markers.DATA_OK, stopDataEvent2.getMarker());
        Assert.assertEquals(Markers.INCONSISTENT_OK, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped10() {
        final Meter m = new Meter(logger).m(title).start();
        m.reject("REJ");
        m.ok("OK");

        Assert.assertTrue(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Markers.MSG_REJECT, stopEvent1.getMarker());
        Assert.assertEquals(Markers.DATA_REJECT, stopDataEvent1.getMarker());
        Assert.assertEquals(Markers.MSG_OK, stopEvent2.getMarker());
        Assert.assertEquals(Markers.DATA_OK, stopDataEvent2.getMarker());
        Assert.assertEquals(Markers.INCONSISTENT_OK, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped11() {
        final Meter m = new Meter(logger).m(title).start();
        m.reject("REJ");
        m.reject("REJ");

        Assert.assertFalse(m.isOK());
        Assert.assertTrue(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Markers.MSG_REJECT, stopEvent1.getMarker());
        Assert.assertEquals(Markers.DATA_REJECT, stopDataEvent1.getMarker());
        Assert.assertEquals(Markers.MSG_REJECT, stopEvent2.getMarker());
        Assert.assertEquals(Markers.DATA_REJECT, stopDataEvent2.getMarker());
        Assert.assertEquals(Markers.INCONSISTENT_REJECT, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped12() {
        final Meter m = new Meter(logger).m(title).start();
        m.reject("REJ");
        m.fail(new IllegalStateException());

        Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertTrue(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Markers.MSG_REJECT, stopEvent1.getMarker());
        Assert.assertEquals(Markers.DATA_REJECT, stopDataEvent1.getMarker());
        Assert.assertEquals(Markers.MSG_FAIL, stopEvent2.getMarker());
        Assert.assertEquals(Markers.DATA_FAIL, stopDataEvent2.getMarker());
        Assert.assertEquals(Markers.INCONSISTENT_FAIL, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped13() {
        final Meter m = new Meter(logger).m(title).start();
        m.fail(new IllegalStateException());
        m.ok();

        Assert.assertTrue(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Markers.MSG_FAIL, stopEvent1.getMarker());
        Assert.assertEquals(Markers.DATA_FAIL, stopDataEvent1.getMarker());
        Assert.assertEquals(Markers.MSG_OK, stopEvent2.getMarker());
        Assert.assertEquals(Markers.DATA_OK, stopDataEvent2.getMarker());
        Assert.assertEquals(Markers.INCONSISTENT_OK, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped14() {
        final Meter m = new Meter(logger).m(title).start();
        m.fail(new IllegalStateException());
        m.ok("OK");

        Assert.assertTrue(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Markers.MSG_FAIL, stopEvent1.getMarker());
        Assert.assertEquals(Markers.DATA_FAIL, stopDataEvent1.getMarker());
        Assert.assertEquals(Markers.MSG_OK, stopEvent2.getMarker());
        Assert.assertEquals(Markers.DATA_OK, stopDataEvent2.getMarker());
        Assert.assertEquals(Markers.INCONSISTENT_OK, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped15() {
        final Meter m = new Meter(logger).m(title).start();
        m.fail(new IllegalStateException());
        m.reject("REJ");

        Assert.assertFalse(m.isOK());
        Assert.assertTrue(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Markers.MSG_FAIL, stopEvent1.getMarker());
        Assert.assertEquals(Markers.DATA_FAIL, stopDataEvent1.getMarker());
        Assert.assertEquals(Markers.MSG_REJECT, stopEvent2.getMarker());
        Assert.assertEquals(Markers.DATA_REJECT, stopDataEvent2.getMarker());
        Assert.assertEquals(Markers.INCONSISTENT_REJECT, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped16() {
        final Meter m = new Meter(logger).m(title).start();
        m.fail(new IllegalStateException());
        m.fail(new IllegalStateException());

        Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertTrue(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Markers.MSG_FAIL, stopEvent1.getMarker());
        Assert.assertEquals(Markers.DATA_FAIL, stopDataEvent1.getMarker());
        Assert.assertEquals(Markers.MSG_FAIL, stopEvent2.getMarker());
        Assert.assertEquals(Markers.DATA_FAIL, stopDataEvent2.getMarker());
        Assert.assertEquals(Markers.INCONSISTENT_FAIL, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof IllegalMeterUsage);
    }

    @Test
    public void testMeterStoppedButNotStarted1() {
        final Meter m = new Meter(logger).m(title).ok();

        Assert.assertTrue(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(3, logger.getEventCount());
        final TestLoggerEvent stopEvent = logger.getEvent(1);
        final TestLoggerEvent stopDataEvent = logger.getEvent(2);
        final TestLoggerEvent messageEvent = logger.getEvent(0);
        Assert.assertEquals(Markers.MSG_OK, stopEvent.getMarker());
        Assert.assertEquals(Markers.DATA_OK, stopDataEvent.getMarker());
        Assert.assertEquals(Markers.INCONSISTENT_OK, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof IllegalMeterUsage);
    }

    @Test
    public void testMeterStoppedButNotStarted2() {
        final Meter m = new Meter(logger).m(title).ok("ok");

        Assert.assertTrue(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(3, logger.getEventCount());
        final TestLoggerEvent stopEvent = logger.getEvent(1);
        final TestLoggerEvent stopDataEvent = logger.getEvent(2);
        final TestLoggerEvent messageEvent = logger.getEvent(0);
        Assert.assertEquals(Markers.MSG_OK, stopEvent.getMarker());
        Assert.assertEquals(Markers.DATA_OK, stopDataEvent.getMarker());
        Assert.assertEquals(Markers.INCONSISTENT_OK, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof IllegalMeterUsage);
    }

    @Test
    public void testMeterStoppedButNotStarted3() {
        final Meter m = new Meter(logger).m(title).reject("bad");

        Assert.assertFalse(m.isOK());
        Assert.assertTrue(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(3, logger.getEventCount());
        final TestLoggerEvent stopEvent = logger.getEvent(1);
        final TestLoggerEvent stopDataEvent = logger.getEvent(2);
        final TestLoggerEvent messageEvent = logger.getEvent(0);
        Assert.assertEquals(Markers.MSG_REJECT, stopEvent.getMarker());
        Assert.assertEquals(Markers.DATA_REJECT, stopDataEvent.getMarker());
        Assert.assertEquals(Markers.INCONSISTENT_REJECT, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof IllegalMeterUsage);
    }

    @Test
    public void testMeterStoppedButNotStarted4() {
        final Meter m = new Meter(logger).m(title).fail(new IllegalStateException());

        Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertTrue(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(3, logger.getEventCount());
        final TestLoggerEvent stopEvent = logger.getEvent(1);
        final TestLoggerEvent stopDataEvent = logger.getEvent(2);
        final TestLoggerEvent messageEvent = logger.getEvent(0);
        Assert.assertEquals(Markers.MSG_FAIL, stopEvent.getMarker());
        Assert.assertEquals(Markers.DATA_FAIL, stopDataEvent.getMarker());
        Assert.assertEquals(Markers.INCONSISTENT_FAIL, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStarted() {
        final Meter m = new Meter(logger).m(title).start().start().ok();

        Assert.assertTrue(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent1 = logger.getEvent(0);
        final TestLoggerEvent startDataEvent1 = logger.getEvent(1);
        final TestLoggerEvent messageEvent = logger.getEvent(2);
        final TestLoggerEvent startEvent2 = logger.getEvent(3);
        final TestLoggerEvent startDataEvent2 = logger.getEvent(4);
        final TestLoggerEvent stopEvent = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent = logger.getEvent(6);
        Assert.assertEquals(Markers.MSG_START, startEvent1.getMarker());
        Assert.assertEquals(Markers.DATA_START, startDataEvent1.getMarker());
        Assert.assertEquals(Markers.MSG_START, startEvent2.getMarker());
        Assert.assertEquals(Markers.DATA_START, startDataEvent2.getMarker());
        Assert.assertEquals(Markers.MSG_OK, stopEvent.getMarker());
        Assert.assertEquals(Markers.DATA_OK, stopDataEvent.getMarker());
        Assert.assertEquals(Markers.INCONSISTENT_START, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof IllegalMeterUsage);
    }

    @Test
    public void testMeterNotStopped() throws InterruptedException {
        new Meter(logger).m(title).start();
        // Wait and force garbage colletor to finalize meter
        System.gc();
        Thread.sleep(1000);
        System.gc();
        Thread.sleep(1000);
        System.gc();

        Assert.assertEquals(3, logger.getEventCount());
        final TestLoggerEvent startEvent1 = logger.getEvent(0);
        final TestLoggerEvent startDataEvent1 = logger.getEvent(1);
        final TestLoggerEvent messageEvent = logger.getEvent(2);
        Assert.assertEquals(Markers.MSG_START, startEvent1.getMarker());
        Assert.assertEquals(Markers.DATA_START, startDataEvent1.getMarker());
        Assert.assertEquals(Markers.INCONSISTENT_FINALIZED, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof IllegalMeterUsage);
    }

    @Test
    public void testMeterInternalException() {
        /* Override some method to force some exception. */
        final Meter m = new Meter(LoggerFactory.getLogger("teste")) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void collectManagedBeanStatus() {
                throw new RuntimeException();
            }

            @Override
            protected void collectRuntimeStatus() {
                throw new RuntimeException();
            }
        };
        m.start();
        m.ok();
    }

    @Test
    public void testIllegalCallSub() {
        final Meter m = new Meter(logger).m(title).start();
        final Meter m2 = m.sub(null).start();
        m2.ok();
        m.ok();
    }

    @Test
    public void testIllegalCallM() {
        final Meter m = new Meter(logger).m(title).m(null).start().ok();
    }

    @Test
    public void testIllegalCallM2() {
        final Meter m = new Meter(logger).m(title).m(null, "abc").start().ok();
    }

    @Test
    public void testIllegalCallM3() {
        final Meter m = new Meter(logger).m(title).m(null, "%d", 0.0f).start().ok();
    }

    @Test
    public void testIllegalCallLimitMilliseconds() {
        final Meter m = new Meter(logger).m(title).limitMilliseconds(-10).start().ok();
    }

    @Test
    public void testIllegalCallIterations() {
        final Meter m = new Meter(logger).m(title).iterations(-10).start().ok();
    }

    @Test
    public void testIllegalCallCtx0() {
        final Meter m = new Meter(logger).m(title).ctx(null).start().ok();
    }

    @Test
    public void testIllegalCallCtx1() {
        final Meter m = new Meter(logger).m(title).ctx(null, 0).start().ok();
    }

    @Test
    public void testIllegalCallCtx2() {
        final Meter m = new Meter(logger).m(title).ctx(null, 0L).start().ok();
    }

    @Test
    public void testIllegalCallCtx3() {
        final Meter m = new Meter(logger).m(title).ctx(null, "s").start().ok();
    }

    @Test
    public void testIllegalCallCtx4() {
        final Meter m = new Meter(logger).m(title).ctx(null, true).start().ok();
    }

    @Test
    public void testIllegalCallCtx5() {
        final Meter m = new Meter(logger).m(title).ctx(null, 0.0f).start().ok();
    }

    @Test
    public void testIllegalCallCtx6() {
        final Meter m = new Meter(logger).m(title).ctx(null, 0.0).start().ok();
    }

    @Test
    public void testIllegalCallCtx7() {
        final Meter m = new Meter(logger).m(title).ctx(null, Integer.valueOf(0)).start().ok();
    }

    @Test
    public void testIllegalCallCtx8() {
        final Meter m = new Meter(logger).m(title).ctx(null, Long.valueOf(0)).start().ok();
    }

    @Test
    public void testIllegalCallCtx9() {
        final Meter m = new Meter(logger).m(title).ctx(null, Boolean.FALSE).start().ok();
    }

    @Test
    public void testIllegalCallCtx10() {
        final Meter m = new Meter(logger).m(title).ctx(null, Float.valueOf(0)).start().ok();
    }

    @Test
    public void testIllegalCallCtx11() {
        final Meter m = new Meter(logger).m(title).ctx(null, Double.valueOf(0)).start().ok();
    }

    @Test
    public void testIllegalCallCtx12() {
        final Meter m = new Meter(logger).m(title).ctx(null, "a", "b").start().ok();
    }

    @Test
    public void testIllegalCallCtx13() {
        final Meter m = new Meter(logger).m(title).ctx("a", null, "b").start().ok();
    }

    @Test
    public void testIllegalCallCtx14() {
        final Meter m = new Meter(logger).m(title).ctx("a", "%d", 0.0f).start().ok();
    }

    @Test
    public void testIllegalCallIncBy() {
        final Meter m = new Meter(logger).m(title).incBy(-10).start().ok();
    }

    @Test
    public void testIllegalCallIncTo0() {
        final Meter m = new Meter(logger).m(title).incTo(-10).start().ok();
    }

    @Test
    public void testIllegalCallIncTo1() {
        final Meter m = new Meter(logger).m(title).incTo(10).incTo(5).start().ok();
    }
}
