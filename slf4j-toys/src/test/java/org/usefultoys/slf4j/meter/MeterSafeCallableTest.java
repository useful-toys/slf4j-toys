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

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * @author Daniel Felix Ferber
 */
@SuppressWarnings("UnusedAssignment")
public class MeterSafeCallableTest {

    static final String meterCategory = "category";
    static final TestLogger logger = (TestLogger) LoggerFactory.getLogger(meterCategory);

    public MeterSafeCallableTest() {
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
        final Meter m = new Meter(logger, "testWithStartWithOk").start();
        Object result = m.safeCall(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Assert.assertEquals(m, Meter.getCurrentInstance());
                m.ok();
                return null;
            }
        });

        Assert.assertNull(result);
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
    public void testWithStartWithOkAndIgnoredReturn() {
        final Meter m = new Meter(logger, "testWithStartWithOk").start();
        Object result = m.safeCall(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Assert.assertEquals(m, Meter.getCurrentInstance());
                m.ok();
                return 1000;
            }
        });

        Assert.assertEquals(Integer.valueOf(1000), result);
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
        Assert.assertFalse(stopEvent.getFormattedMessage().contains("result=1000"));
        Assert.assertFalse(stopDataEvent.getFormattedMessage().contains("result:1000"));
    }

    @Test
    public void testNoStartNoOkAndReturn() {
        final Meter m = new Meter(logger, "testWithStartWithOk");
        Object result = m.safeCall(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Assert.assertEquals(m, Meter.getCurrentInstance());
                return 1000;
            }
        });

        Assert.assertEquals(Integer.valueOf(1000), result);
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
        Assert.assertTrue(stopEvent.getFormattedMessage().contains("result=1000"));
        Assert.assertTrue(stopDataEvent.getFormattedMessage().contains("result:1000"));
    }

    @Test
    public void testNoStartWithOk() {
        final Meter m = new Meter(logger, "testNoStartWithOk");
        Object result = m.safeCall(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Assert.assertEquals(m, Meter.getCurrentInstance());
                m.ok();
                return null;
            }
        });

        Assert.assertNull(result);
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
        final Meter m = new Meter(logger, "testNoStartNoOk");
        Object result = m.safeCall(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Assert.assertEquals(m, Meter.getCurrentInstance());
                return null;
            }
        });

        Assert.assertNull(result);
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
        final Meter m = new Meter(logger, "testWithStartNoOk").start();
        Object result = m.safeCall(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Assert.assertEquals(m, Meter.getCurrentInstance());
                return null;
            }
        });

        Assert.assertNull(result);
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
        final Meter m = new Meter(logger, "testNoStartWithReject");
        Object result = m.safeCall(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Assert.assertEquals(m, Meter.getCurrentInstance());
                m.reject("a");
                return null;
            }
        });

        Assert.assertNull(result);
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
        final Meter m = new Meter(logger, "testNoStartWithFail");
        Object result = m.safeCall(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Assert.assertEquals(m, Meter.getCurrentInstance());
                m.fail("a");
                return null;
            }
        });

        Assert.assertNull(result);
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
        final Meter m = new Meter(logger, "testNoStartWithException");
        try {
            m.safeCall(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    Assert.assertEquals(m, Meter.getCurrentInstance());
                    throw new IllegalArgumentException("someException");
                }
            });
        } catch (RuntimeException e) {
            Assert.assertEquals(IllegalArgumentException.class, e.getClass());
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

    @Test
    public void testNoStartWithException2() {
        final Meter m = new Meter(logger, "testNoStartWithException");
        try {
            m.safeCall(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    Assert.assertEquals(m, Meter.getCurrentInstance());
                    throw new IOException("someException");
                }
            });
        } catch (RuntimeException e) {
            Assert.assertEquals("Meter.safeCall wrapped exception.", e.getMessage());
            Assert.assertEquals(RuntimeException.class, e.getClass());
            Assert.assertEquals(IOException.class, e.getCause().getClass());
            Assert.assertEquals("someException", e.getCause().getMessage());
        }
        Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertTrue(m.isFail());
        Assert.assertFalse(m.isSlow());
        Assert.assertEquals(4, logger.getEventCount());
        Assert.assertEquals("java.io.IOException", m.failPath);
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
