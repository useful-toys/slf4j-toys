/*
 * Copyright 2015 Daniel Felix Ferber.
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
import static org.usefultoys.slf4j.meter.MeterMessageTest.*;

/**
 *
 * @author Daniel Felix Ferber
 */
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

        Assert.assertTrue(m.isSuccess());
        Assert.assertFalse(m.isRejection());
        Assert.assertFalse(m.isFailure());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Slf4JMarkers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_OK, stopEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_OK, stopDataEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_OK, stopEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_OK, stopDataEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.INCONSISTENT_OK, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof Meter.IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped2() {
        final Meter m = new Meter(logger).m(title).start();
        m.ok();
        m.ok("OK");

        Assert.assertTrue(m.isSuccess());
        Assert.assertFalse(m.isRejection());
        Assert.assertFalse(m.isFailure());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Slf4JMarkers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_OK, stopEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_OK, stopDataEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_OK, stopEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_OK, stopDataEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.INCONSISTENT_OK, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof Meter.IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped3() {
        final Meter m = new Meter(logger).m(title).start();
        m.ok();
        m.reject("REJ");

        Assert.assertFalse(m.isSuccess());
        Assert.assertTrue(m.isRejection());
        Assert.assertFalse(m.isFailure());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Slf4JMarkers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_OK, stopEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_OK, stopDataEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_REJECT, stopEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_REJECT, stopDataEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.INCONSISTENT_REJECT, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof Meter.IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped4() {
        final Meter m = new Meter(logger).m(title).start();
        m.ok();
        m.fail(new IllegalStateException());

        Assert.assertFalse(m.isSuccess());
        Assert.assertFalse(m.isRejection());
        Assert.assertTrue(m.isFailure());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Slf4JMarkers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_OK, stopEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_OK, stopDataEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_FAIL, stopEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_FAIL, stopDataEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.INCONSISTENT_FAIL, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof Meter.IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped5() {
        final Meter m = new Meter(logger).m(title).start();
        m.ok("OK");
        m.ok();

        Assert.assertTrue(m.isSuccess());
        Assert.assertFalse(m.isRejection());
        Assert.assertFalse(m.isFailure());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Slf4JMarkers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_OK, stopEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_OK, stopDataEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_OK, stopEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_OK, stopDataEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.INCONSISTENT_OK, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof Meter.IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped6() {
        final Meter m = new Meter(logger).m(title).start();
        m.ok("OK");
        m.ok("OK");

        Assert.assertTrue(m.isSuccess());
        Assert.assertFalse(m.isRejection());
        Assert.assertFalse(m.isFailure());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Slf4JMarkers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_OK, stopEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_OK, stopDataEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_OK, stopEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_OK, stopDataEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.INCONSISTENT_OK, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof Meter.IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped7() {
        final Meter m = new Meter(logger).m(title).start();
        m.ok("OK");
        m.reject("REJ");

        Assert.assertFalse(m.isSuccess());
        Assert.assertTrue(m.isRejection());
        Assert.assertFalse(m.isFailure());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Slf4JMarkers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_OK, stopEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_OK, stopDataEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_REJECT, stopEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_REJECT, stopDataEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.INCONSISTENT_REJECT, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof Meter.IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped8() {
        final Meter m = new Meter(logger).m(title).start();
        m.ok("OK");
        m.fail(new IllegalStateException());

        Assert.assertFalse(m.isSuccess());
        Assert.assertFalse(m.isRejection());
        Assert.assertTrue(m.isFailure());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Slf4JMarkers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_OK, stopEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_OK, stopDataEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_FAIL, stopEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_FAIL, stopDataEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.INCONSISTENT_FAIL, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof Meter.IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped9() {
        final Meter m = new Meter(logger).m(title).start();
        m.reject("REJ");
        m.ok();

        Assert.assertTrue(m.isSuccess());
        Assert.assertFalse(m.isRejection());
        Assert.assertFalse(m.isFailure());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Slf4JMarkers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_REJECT, stopEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_REJECT, stopDataEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_OK, stopEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_OK, stopDataEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.INCONSISTENT_OK, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof Meter.IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped10() {
        final Meter m = new Meter(logger).m(title).start();
        m.reject("REJ");
        m.ok("OK");

        Assert.assertTrue(m.isSuccess());
        Assert.assertFalse(m.isRejection());
        Assert.assertFalse(m.isFailure());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Slf4JMarkers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_REJECT, stopEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_REJECT, stopDataEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_OK, stopEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_OK, stopDataEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.INCONSISTENT_OK, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof Meter.IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped11() {
        final Meter m = new Meter(logger).m(title).start();
        m.reject("REJ");
        m.reject("REJ");

        Assert.assertFalse(m.isSuccess());
        Assert.assertTrue(m.isRejection());
        Assert.assertFalse(m.isFailure());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Slf4JMarkers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_REJECT, stopEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_REJECT, stopDataEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_REJECT, stopEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_REJECT, stopDataEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.INCONSISTENT_REJECT, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof Meter.IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped12() {
        final Meter m = new Meter(logger).m(title).start();
        m.reject("REJ");
        m.fail(new IllegalStateException());

        Assert.assertFalse(m.isSuccess());
        Assert.assertFalse(m.isRejection());
        Assert.assertTrue(m.isFailure());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Slf4JMarkers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_REJECT, stopEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_REJECT, stopDataEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_FAIL, stopEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_FAIL, stopDataEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.INCONSISTENT_FAIL, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof Meter.IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped13() {
        final Meter m = new Meter(logger).m(title).start();
        m.fail(new IllegalStateException());
        m.ok();

        Assert.assertTrue(m.isSuccess());
        Assert.assertFalse(m.isRejection());
        Assert.assertFalse(m.isFailure());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Slf4JMarkers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_FAIL, stopEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_FAIL, stopDataEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_OK, stopEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_OK, stopDataEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.INCONSISTENT_OK, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof Meter.IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped14() {
        final Meter m = new Meter(logger).m(title).start();
        m.fail(new IllegalStateException());
        m.ok("OK");

        Assert.assertTrue(m.isSuccess());
        Assert.assertFalse(m.isRejection());
        Assert.assertFalse(m.isFailure());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Slf4JMarkers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_FAIL, stopEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_FAIL, stopDataEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_OK, stopEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_OK, stopDataEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.INCONSISTENT_OK, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof Meter.IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped15() {
        final Meter m = new Meter(logger).m(title).start();
        m.fail(new IllegalStateException());
        m.reject("REJ");

        Assert.assertFalse(m.isSuccess());
        Assert.assertTrue(m.isRejection());
        Assert.assertFalse(m.isFailure());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Slf4JMarkers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_FAIL, stopEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_FAIL, stopDataEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_REJECT, stopEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_REJECT, stopDataEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.INCONSISTENT_REJECT, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof Meter.IllegalMeterUsage);
    }

    @Test
    public void testMeterAlreadyStopped16() {
        final Meter m = new Meter(logger).m(title).start();
        m.fail(new IllegalStateException());
        m.fail(new IllegalStateException());
        
        Assert.assertFalse(m.isSuccess());
        Assert.assertFalse(m.isRejection());
        Assert.assertTrue(m.isFailure());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(7, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent1 = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent messageEvent = logger.getEvent(4);
        final TestLoggerEvent stopEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(6);
        Assert.assertEquals(Slf4JMarkers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_FAIL, stopEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_FAIL, stopDataEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_FAIL, stopEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_FAIL, stopDataEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.INCONSISTENT_FAIL, messageEvent.getMarker());
        Assert.assertTrue(messageEvent.getThrowable() instanceof Meter.IllegalMeterUsage);
    }

//    @Test
//    public void testMeterConfirmedButNotStarted() {
//        final Meter m = MeterFactory.getMeter("teste");
//        m.ok();
//    }
//
//    @Test
//    public void testMeterRefusedButNotStarted() {
//        final Meter m = MeterFactory.getMeter("teste");
//        m.fail(new IllegalStateException());
//    }
//
//    @Test
//    public void testMeterAlreadyStarted() {
//        final Meter m = new Meter(logger).m(title).start().start();
//        m.ok();
//    }
//
//    @Test
//    public void testMeterNotRefusedNorConfirmed() throws InterruptedException {
//        subMeterX();
//        // Wait and force garbage colletor to finalize meter
//        System.gc();
//        Thread.sleep(1000);
//        System.gc();
//        Thread.sleep(1000);
//        System.gc();
//    }
//
//    private void subMeterX() {
//        final Meter m = new Meter(logger).m(title).start();
//    }
//
//    @Test
//    public void testMeterInternalException() {
//        final Meter m = new Meter(LoggerFactory.getLogger("teste")) {
//            /**
//             *
//             */
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            protected void collectSystemStatus() {
//                throw new RuntimeException();
//            }
//        };
//        m.start();
//        m.ok();
//    }
//
//    @Test
//    public void testIllegalCallSub() {
//        final Meter m = new Meter(logger).m(title).start();
//        final Meter m2 = m.sub(null).start();
//        m2.ok();
//        m.ok();
//    }
//
//    @Test
//    public void testIllegalCallM() {
//        final Meter m = MeterFactory.getMeter("teste").m(null).start().ok();
//    }
//
//    @Test
//    public void testIllegalCallM2() {
//        final Meter m = MeterFactory.getMeter("teste").m(null, "abc").start().ok();
//    }
//
//    @Test
//    public void testIllegalCallM3() {
//        final Meter m = MeterFactory.getMeter("teste").m(null, "%d", 0.0f).start().ok();
//    }
//
//    @Test
//    public void testIllegalCallLimitMilliseconds() {
//        final Meter m = MeterFactory.getMeter("teste").limitMilliseconds(-10).start().ok();
//    }
//
//    @Test
//    public void testIllegalCallIterations() {
//        final Meter m = MeterFactory.getMeter("teste").iterations(-10).start().ok();
//    }
//
//    @Test
//    public void testIllegalCallCtx0() {
//        final Meter m = MeterFactory.getMeter("teste").ctx(null).start().ok();
//    }
//
//    @Test
//    public void testIllegalCallCtx1() {
//        final Meter m = MeterFactory.getMeter("teste").ctx(null, 0).start().ok();
//    }
//
//    @Test
//    public void testIllegalCallCtx2() {
//        final Meter m = MeterFactory.getMeter("teste").ctx(null, 0L).start().ok();
//    }
//
//    @Test
//    public void testIllegalCallCtx3() {
//        final Meter m = MeterFactory.getMeter("teste").ctx(null, "s").start().ok();
//    }
//
//    @Test
//    public void testIllegalCallCtx4() {
//        final Meter m = MeterFactory.getMeter("teste").ctx(null, true).start().ok();
//    }
//
//    @Test
//    public void testIllegalCallCtx5() {
//        final Meter m = MeterFactory.getMeter("teste").ctx(null, 0.0f).start().ok();
//    }
//
//    @Test
//    public void testIllegalCallCtx6() {
//        final Meter m = MeterFactory.getMeter("teste").ctx(null, 0.0).start().ok();
//    }
//
//    @Test
//    public void testIllegalCallCtx7() {
//        final Meter m = MeterFactory.getMeter("teste").ctx(null, Integer.valueOf(0)).start().ok();
//    }
//
//    @Test
//    public void testIllegalCallCtx8() {
//        final Meter m = MeterFactory.getMeter("teste").ctx(null, Long.valueOf(0)).start().ok();
//    }
//
//    @Test
//    public void testIllegalCallCtx9() {
//        final Meter m = MeterFactory.getMeter("teste").ctx(null, Boolean.FALSE).start().ok();
//    }
//
//    @Test
//    public void testIllegalCallCtx10() {
//        final Meter m = MeterFactory.getMeter("teste").ctx(null, Float.valueOf(0)).start().ok();
//    }
//
//    @Test
//    public void testIllegalCallCtx11() {
//        final Meter m = MeterFactory.getMeter("teste").ctx(null, Double.valueOf(0)).start().ok();
//    }
//
//    @Test
//    public void testIllegalCallCtx12() {
//        final Meter m = MeterFactory.getMeter("teste").ctx(null, "a", "b").start().ok();
//    }
//
//    @Test
//    public void testIllegalCallCtx13() {
//        final Meter m = MeterFactory.getMeter("teste").ctx("a", null, "b").start().ok();
//    }
//
//    @Test
//    public void testIllegalCallCtx14() {
//        final Meter m = MeterFactory.getMeter("teste").ctx("a", "%d", 0.0f).start().ok();
//    }
//
//    @Test
//    public void testIllegalCallIncBy() {
//        final Meter m = MeterFactory.getMeter("teste").incBy(-10).start().ok();
//    }
//
//    @Test
//    public void testIllegalCallIncTo0() {
//        final Meter m = MeterFactory.getMeter("teste").incTo(-10).start().ok();
//    }
//
//    @Test
//    public void testIllegalCallIncTo1() {
//        final Meter m = MeterFactory.getMeter("teste").incTo(10).incTo(5).start().ok();
//    }
}
