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
package org.usefultoys.slf4j.profiler.meter;

import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.impl.TestLogger;
import org.slf4j.impl.TestLoggerEvent;
import org.usefultoys.slf4j.logger.LoggerFactory;
import org.usefultoys.slf4j.profiler.meter.Meter;
import org.usefultoys.slf4j.profiler.meter.Slf4JMarkers;

/**
 *
 * @author Daniel Felix Ferber
 */
public class MeterUseCaseTest {

    final String meterName = "name";
    final TestLogger logger = (TestLogger) LoggerFactory.getLogger(meterName);

    static final String MESSAGE_START_PREFIX = "Started:";
    static final String MESSAGE_PROGRESS_PREFIX = "Progress ";
    static final String MESSAGE_SLOW_PREFIX = "Slow:";
    static final String MESSAGE_OK_PREFIX = "OK:";
    static final String MESSAGE_FAIL_PREFIX = "Failed:";

    public MeterUseCaseTest() {
    }

    @BeforeClass
    public static void configureMeterSettings() {
        System.setProperty("meter.progress.period", "0ms");
    }

    @Before
    public void clearEvents() {
        logger.clearEvents();
    }

    @Test
    public void testOk() {
        System.out.println("testOk:");
        final String title = "Example of execution that succeeds.";
        final Meter m = new Meter(logger).m(title).start();
        try {
            /* Run stuff that may fail. */
            if (1 + 2 == 4) {
                throw new Exception("message");
            }
            m.ok();
        } catch (final Exception e) {
            m.fail(e);
            // may rethrow
        }

        Assert.assertTrue(m.isSuccess());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        Assert.assertEquals(Slf4JMarkers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_OK, stopEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_OK, stopDataEvent.getMarker());
        Assert.assertEquals(TestLoggerEvent.Level.DEBUG, startEvent.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.INFO, stopEvent.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.TRACE, startDataEvent.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.TRACE, stopDataEvent.getLevel());
        Assert.assertTrue(startEvent.getFormattedMessage().contains(MESSAGE_START_PREFIX));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains(MESSAGE_OK_PREFIX));
        Assert.assertTrue(startEvent.getFormattedMessage().contains(title));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains(title));
    }

    @Test
    public void testFail() {
        System.out.println("testFail:");
        final String title = "Example of execution that fails.";
        final Meter m = new Meter(logger).m(title).start();
        try {
            /* Run stuff that may fail. */
            if (1 + 2 == 3) {
                throw new Exception("message");
            }
            m.ok();
        } catch (final Exception e) {
            m.fail(e);
            // may rethrow
        }
        Assert.assertFalse(m.isSuccess());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        Assert.assertEquals(Slf4JMarkers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_FAIL, stopEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_FAIL, stopDataEvent.getMarker());
        Assert.assertEquals(TestLoggerEvent.Level.DEBUG, startEvent.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.WARN, stopEvent.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.TRACE, startDataEvent.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.TRACE, stopDataEvent.getLevel());
        Assert.assertTrue(startEvent.getFormattedMessage().contains(MESSAGE_START_PREFIX));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains(MESSAGE_FAIL_PREFIX));
        Assert.assertTrue(startEvent.getFormattedMessage().contains(title));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains(title));
    }

    @Test
    public void testSlownessNo() {
        System.out.println("testSlownessNo:");
        final String title = "Example of execution that succeeds within time limit.";
        final Meter m = new Meter(logger).m(title).limitMilliseconds(200).start();
        try {
            /* Run stuff that may delay. */
            Thread.sleep(1);
            m.ok();
        } catch (final Exception e) {
            m.fail(e);
            // may rethrow
        }
        Assert.assertTrue(m.isSuccess());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        Assert.assertEquals(Slf4JMarkers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_OK, stopEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_OK, stopDataEvent.getMarker());
        Assert.assertEquals(TestLoggerEvent.Level.DEBUG, startEvent.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.INFO, stopEvent.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.TRACE, startDataEvent.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.TRACE, stopDataEvent.getLevel());
        Assert.assertTrue(startEvent.getFormattedMessage().contains(MESSAGE_START_PREFIX));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains(MESSAGE_OK_PREFIX));
        Assert.assertTrue(startEvent.getFormattedMessage().contains(title));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains(title));
    }

    @Test
    public void testSlownessYes() {
        System.out.println("testSlownessYes:");
        final String title = "Example of execution that succeeds but exceeds time limit.";
        final Meter m = new Meter(logger).m(title).limitMilliseconds(200).start();
        try {
            /* Run stuff that may delay. */
            Thread.sleep(220);
            m.ok();
        } catch (final Exception e) {
            m.fail(e);
            // may rethrow
        }
        Assert.assertTrue(m.isSuccess());
        Assert.assertTrue(m.isSlow());
        Assert.assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        Assert.assertEquals(Slf4JMarkers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_SLOW_OK, stopEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_SLOW_OK, stopDataEvent.getMarker());
        Assert.assertEquals(TestLoggerEvent.Level.DEBUG, startEvent.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.WARN, stopEvent.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.TRACE, startDataEvent.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.TRACE, stopDataEvent.getLevel());
        Assert.assertTrue(startEvent.getFormattedMessage().contains(MESSAGE_START_PREFIX));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains(MESSAGE_SLOW_PREFIX));
        Assert.assertTrue(startEvent.getFormattedMessage().contains(title));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains(title));
    }

    @Test
    public void testIteration() {
        System.out.println("testIteration:");
        final String title = "Example of execution that succeeds and reports progress of completed iterations.";
        final Meter m = new Meter(logger).m(title).iterations(4).start();
        try {
            /* Run stuff that repeats. */
            for (int i = 0; i < 4; i++) {
                /* Do stuff. */
                Thread.sleep(100);
                m.inc().progress();
            }
            m.ok();
        } catch (final Exception e) {
            m.fail(e);
            // may rethrow
        }
        Assert.assertTrue(m.isSuccess());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(12, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent progressEvent1 = logger.getEvent(2);
        final TestLoggerEvent progressDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent progressEvent2 = logger.getEvent(4);
        final TestLoggerEvent progressDataEvent2 = logger.getEvent(5);
        final TestLoggerEvent progressEvent3 = logger.getEvent(6);
        final TestLoggerEvent progressDataEvent3 = logger.getEvent(7);
        final TestLoggerEvent progressEvent4 = logger.getEvent(8);
        final TestLoggerEvent progressDataEvent4 = logger.getEvent(9);
        final TestLoggerEvent stopEvent = logger.getEvent(10);
        final TestLoggerEvent stopDataEvent = logger.getEvent(11);
        Assert.assertEquals(Slf4JMarkers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_OK, stopEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_PROGRESS, progressEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_PROGRESS, progressEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_PROGRESS, progressEvent3.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_PROGRESS, progressEvent4.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_OK, stopDataEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_PROGRESS, progressDataEvent1.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_PROGRESS, progressDataEvent2.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_PROGRESS, progressDataEvent3.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_PROGRESS, progressDataEvent4.getMarker());
        Assert.assertEquals(TestLoggerEvent.Level.DEBUG, startEvent.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.INFO, progressEvent1.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.INFO, progressEvent2.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.INFO, progressEvent3.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.INFO, progressEvent4.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.INFO, stopEvent.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.TRACE, startDataEvent.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.TRACE, progressDataEvent1.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.TRACE, progressDataEvent2.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.TRACE, progressDataEvent3.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.TRACE, progressDataEvent4.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.TRACE, stopDataEvent.getLevel());
        Assert.assertTrue(startEvent.getFormattedMessage().contains(MESSAGE_START_PREFIX));
        Assert.assertTrue(progressEvent1.getFormattedMessage().contains(MESSAGE_PROGRESS_PREFIX));
        Assert.assertTrue(progressEvent2.getFormattedMessage().contains(MESSAGE_PROGRESS_PREFIX));
        Assert.assertTrue(progressEvent3.getFormattedMessage().contains(MESSAGE_PROGRESS_PREFIX));
        Assert.assertTrue(progressEvent4.getFormattedMessage().contains(MESSAGE_PROGRESS_PREFIX));
        Assert.assertTrue(progressEvent1.getFormattedMessage().contains("1/4"));
        Assert.assertTrue(progressEvent2.getFormattedMessage().contains("2/4"));
        Assert.assertTrue(progressEvent3.getFormattedMessage().contains("3/4"));
        Assert.assertTrue(progressEvent4.getFormattedMessage().contains("4/4"));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains(MESSAGE_OK_PREFIX));
        Assert.assertTrue(startEvent.getFormattedMessage().contains(title));
        Assert.assertTrue(progressEvent1.getFormattedMessage().contains(title));
        Assert.assertTrue(progressEvent2.getFormattedMessage().contains(title));
        Assert.assertTrue(progressEvent3.getFormattedMessage().contains(title));
        Assert.assertTrue(progressEvent4.getFormattedMessage().contains(title));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains(title));
    }

    @Test
    public void testIteration2() {
        System.out.println("testIteration2:");
        final Meter m = new Meter(logger).m("Example of execution that succeeds and reports progress of completed iterations.").iterations(30).start();
        final Random r = new Random();
        try {
            /* Run stuff that repeats, step size may vary. */
            for (int i = 0; i < 30;) {
                /* Do stuff. */
                final int increment = r.nextInt(3) + 1;
                i += increment;
                m.incBy(increment).progress();
            }
            m.ok();
        } catch (final Exception e) {
            m.fail(e);
            // may rethrow
        }
        Assert.assertTrue(m.isSuccess());
        Assert.assertFalse(m.isSlow());
        /* Messages were already tested by testIteration() */
    }

    @Test
    public void testIteration3() {
        System.out.println("testIteration3:");
        final Meter m = new Meter(logger).m("Example of execution that succeeds and reports progress of completed iterations.").iterations(30).start();
        final Random r = new Random();
        try {
            /* Run stuff that repeats, step size may vary. */
            for (int i = 0; i < 30;) {
                /* Do stuff. */
                final int increment = r.nextInt(3) + 1;
                i += increment;
                m.incBy(i).progress();
            }
            m.ok();
        } catch (final Exception e) {
            m.fail(e);
            // may rethrow
        }
        Assert.assertTrue(m.isSuccess());
        Assert.assertFalse(m.isSlow());
        /* Messages were already tested by testIteration() */
    }

    /**
     * As logger is enable for debug, it will print the start message. Hence, the stop message will not include context
     * from the input message anymore.
     */
    @Test
    public void testContext0() {
        System.out.println("testContext1:");
        final String title = "Example of execution that succeeds and reports context.";

        final TestLogger logger2 = (TestLogger) LoggerFactory.getLogger("context");
        logger2.clearEvents();
        logger2.setTraceEnabled(true);
        logger2.setDebugEnabled(true);
        logger2.setInfoEnabled(true);
        logger2.setWarnEnabled(true);
        logger2.setErrorEnabled(true);
        final Meter m = new Meter(logger2).m(title).limitMilliseconds(200);
        final String inputValue = "for example, an value received as input";
        final String outputValue = "for example, an value produced as output";
        final String causeValue = "for example, an identifier for the failure cause";
        final String detailValue = "for example, an value calculated during execution";
        m.ctx("input", inputValue).start();
        try {
            /* Run stuff. */
            m.ctx("detail", detailValue);
            /* Run stuff. */
            m.ctx("output", outputValue).ok();
        } catch (final Exception e) {
            m.ctx("cause", causeValue).fail(e);
            // may rethrow
        }
        Assert.assertTrue(m.isSuccess());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(4, logger2.getEventCount());
        final TestLoggerEvent startEvent = logger2.getEvent(0);
        final TestLoggerEvent startDataEvent = logger2.getEvent(1);
        final TestLoggerEvent stopEvent = logger2.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger2.getEvent(3);
        Assert.assertEquals(Slf4JMarkers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_OK, stopEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_OK, stopDataEvent.getMarker());
        Assert.assertEquals(TestLoggerEvent.Level.DEBUG, startEvent.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.INFO, stopEvent.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.TRACE, startDataEvent.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.TRACE, stopDataEvent.getLevel());
        Assert.assertTrue(startEvent.getFormattedMessage().contains(MESSAGE_START_PREFIX));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains(MESSAGE_OK_PREFIX));
        Assert.assertTrue(startEvent.getFormattedMessage().contains(title));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains(title));
        Assert.assertTrue(startEvent.getFormattedMessage().contains(inputValue));
        Assert.assertFalse(stopEvent.getFormattedMessage().contains(inputValue));
        Assert.assertTrue(startEvent.getFormattedMessage().contains("input="));
        Assert.assertFalse(stopEvent.getFormattedMessage().contains("input="));
        Assert.assertFalse(startEvent.getFormattedMessage().contains(outputValue));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains(outputValue));
        Assert.assertFalse(startEvent.getFormattedMessage().contains("output="));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains("output="));
        Assert.assertFalse(startEvent.getFormattedMessage().contains(detailValue));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains(detailValue));
        Assert.assertFalse(startEvent.getFormattedMessage().contains("detail="));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains("detail="));
    }

    /**
     * As logger is not enable for debug, it will print the start message. Hence, the stop message will not include
     * context from the input message anymore.
     */
    @Test
    public void testContext1() {
        System.out.println("testContext1:");
        final String title = "Example of execution that succeeds and reports context.";

        final TestLogger logger2 = (TestLogger) LoggerFactory.getLogger("context");
        logger2.clearEvents();
        logger2.setTraceEnabled(false);
        logger2.setDebugEnabled(false);
        logger2.setInfoEnabled(true);
        logger2.setWarnEnabled(true);
        logger2.setErrorEnabled(true);
        final Meter m = new Meter(logger2).m(title).limitMilliseconds(200);
        final String inputValue = "for example, an value received as input";
        final String outputValue = "for example, an value produced as output";
        final String causeValue = "for example, an identifier for the failure cause";
        final String detailValue = "for example, an value calculated during execution";
        m.ctx("input", inputValue).start();
        try {
            /* Run stuff. */
            m.ctx("detail", detailValue);
            /* Run stuff. */
            m.ctx("output", outputValue).ok();
        } catch (final Exception e) {
            m.ctx("cause", causeValue).fail(e);
            // may rethrow
        }
        Assert.assertTrue(m.isSuccess());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(1, logger2.getEventCount());
        final TestLoggerEvent stopEvent = logger2.getEvent(0);
        Assert.assertEquals(Slf4JMarkers.MSG_OK, stopEvent.getMarker());
        Assert.assertEquals(TestLoggerEvent.Level.INFO, stopEvent.getLevel());
        Assert.assertTrue(stopEvent.getFormattedMessage().contains(MESSAGE_OK_PREFIX));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains(title));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains(inputValue));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains("input="));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains(outputValue));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains("output="));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains(detailValue));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains("detail="));
    }

    /**
     * Tests context overriding
     */
    @Test
    public void testContext2() {
        System.out.println("testContext1:");
        final String title = "Example of execution that succeeds and reports context.";

        final TestLogger logger2 = (TestLogger) LoggerFactory.getLogger("context");
        logger2.clearEvents();
        logger2.setTraceEnabled(true);
        logger2.setDebugEnabled(true);
        logger2.setInfoEnabled(true);
        logger2.setWarnEnabled(true);
        logger2.setErrorEnabled(true);
        final Meter m = new Meter(logger2).m(title).limitMilliseconds(200);
        final String inputValue = "for example, an value received as input";
        final String outputValue = "for example, an value produced as output";
        final String causeValue = "for example, an identifier for the failure cause";
        final String detailValue1 = "for example, an value calculated before execution";
        final String detailValue2 = "for example, an value calculated during execution";
        final String extraValue1 = "for example, another value calculated during execution";
        final String extraValue2 = "for example, an value overriden during execution";
        final String otherValue = "for example, an value calculated and removed during execution";
        m.ctx("input", inputValue).ctx("detail", detailValue1).start();
        try {
            /* Run stuff. */
            m.ctx("detail", detailValue2);
            m.ctx("extra", extraValue1);
            /* Run stuff. */
            m.ctx("extra", extraValue2);
            m.ctx("other", otherValue);
            /* Run stuff. */
            m.unctx("other");
            /* Run stuff. */
            Thread.sleep(1);
            m.ctx("output", outputValue).ok();
        } catch (final Exception e) {
            m.ctx("cause", causeValue).fail(e);
            // may rethrow
        }

        Assert.assertTrue(m.isSuccess());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(4, logger2.getEventCount());
        final TestLoggerEvent startEvent = logger2.getEvent(0);
        final TestLoggerEvent startDataEvent = logger2.getEvent(1);
        final TestLoggerEvent stopEvent = logger2.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger2.getEvent(3);
        Assert.assertEquals(Slf4JMarkers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.MSG_OK, stopEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Slf4JMarkers.DATA_OK, stopDataEvent.getMarker());
        Assert.assertEquals(TestLoggerEvent.Level.DEBUG, startEvent.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.INFO, stopEvent.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.TRACE, startDataEvent.getLevel());
        Assert.assertEquals(TestLoggerEvent.Level.TRACE, stopDataEvent.getLevel());
        Assert.assertTrue(startEvent.getFormattedMessage().contains(MESSAGE_START_PREFIX));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains(MESSAGE_OK_PREFIX));
        Assert.assertTrue(startEvent.getFormattedMessage().contains(title));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains(title));
        Assert.assertTrue(startEvent.getFormattedMessage().contains(inputValue));
        Assert.assertFalse(stopEvent.getFormattedMessage().contains(inputValue));
        Assert.assertTrue(startEvent.getFormattedMessage().contains("input="));
        Assert.assertFalse(stopEvent.getFormattedMessage().contains("input="));
        Assert.assertFalse(startEvent.getFormattedMessage().contains(outputValue));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains(outputValue));
        Assert.assertFalse(startEvent.getFormattedMessage().contains("output="));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains("output="));
        Assert.assertTrue(startEvent.getFormattedMessage().contains(detailValue1));
        Assert.assertFalse(stopEvent.getFormattedMessage().contains(detailValue1));
        Assert.assertFalse(startEvent.getFormattedMessage().contains(detailValue2));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains(detailValue2));
        Assert.assertTrue(startEvent.getFormattedMessage().contains("detail="));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains("detail="));
        Assert.assertFalse(startEvent.getFormattedMessage().contains(extraValue1));
        Assert.assertFalse(stopEvent.getFormattedMessage().contains(extraValue1));
        Assert.assertFalse(startEvent.getFormattedMessage().contains(extraValue2));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains(extraValue2));
        Assert.assertFalse(startEvent.getFormattedMessage().contains("extra="));
        Assert.assertTrue(stopEvent.getFormattedMessage().contains("extra="));
        Assert.assertFalse(startEvent.getFormattedMessage().contains(otherValue));
        Assert.assertFalse(stopEvent.getFormattedMessage().contains(otherValue));
        Assert.assertFalse(startEvent.getFormattedMessage().contains("other="));
        Assert.assertFalse(stopEvent.getFormattedMessage().contains("other="));
    }
}
