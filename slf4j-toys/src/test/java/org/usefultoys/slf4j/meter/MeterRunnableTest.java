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
public class MeterRunnableTest {

    static final String meterCategory = "category";
    static final TestLogger logger = (TestLogger) LoggerFactory.getLogger(meterCategory);

    public MeterRunnableTest() {
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
        final Meter m = new Meter(logger).start();
        m.run(new Runnable() {
            @Override
            public void run() {
                Assert.assertEquals(m, Meter.getCurrentInstance());
                m.ok();
            }
        });

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
        final Meter m = new Meter(logger);
        m.run(new Runnable() {
            @Override
            public void run() {
                Assert.assertEquals(m, Meter.getCurrentInstance());
                m.ok();
            }
        });

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
    public void testNoStartNoOk() {
        final Meter m = new Meter(logger);
        m.run(new Runnable() {
            @Override
            public void run() {
                Assert.assertEquals(m, Meter.getCurrentInstance());
            }
        });

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
    public void testWithStartNoOk() {
        final Meter m = new Meter(logger).start();
        m.run(new Runnable() {
            @Override
            public void run() {
                Assert.assertEquals(m, Meter.getCurrentInstance());
            }
        });

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
    public void testNoStartWithReject() {
        final Meter m = new Meter(logger);
        m.run(new Runnable() {
            @Override
            public void run() {
                Assert.assertEquals(m, Meter.getCurrentInstance());
                m.reject("a");
            }
        });

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
    public void testNoStartWithFail() {
        final Meter m = new Meter(logger);
        m.run(new Runnable() {
            @Override
            public void run() {
                Assert.assertEquals(m, Meter.getCurrentInstance());
                m.fail("a");
            }
        });

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
    public void testNoStartWithException() {
        final Meter m = new Meter(logger);
        try {
            m.run(new Runnable() {
                @Override
                public void run() {
                    Assert.assertEquals(m, Meter.getCurrentInstance());
                    throw new IllegalArgumentException("someException");
                }
            });
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("someException", e.getMessage());
        }
        Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertTrue(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(4, logger.getEventCount());
        Assert.assertEquals("java.lang.IllegalArgumentException", m.failPath);
        Assert.assertEquals("someException", m.failMessage);
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        Assert.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assert.assertEquals(Markers.MSG_FAIL, stopEvent.getMarker());
        Assert.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assert.assertEquals(Markers.DATA_FAIL, stopDataEvent.getMarker());
        Assert.assertTrue(stopEvent.getFormattedMessage().contains("someException"));
    }
}
