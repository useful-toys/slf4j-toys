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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.SessionConfig;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Daniel Felix Ferber
 */
@SuppressWarnings("UnusedAssignment")
public class MeterCallableTest {

    static final String meterCategory = "category";
    static final MockLogger logger = (MockLogger) LoggerFactory.getLogger(meterCategory);

    public MeterCallableTest() {
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
    public void testWithStartWithOk() throws Exception {
        final Meter m = new Meter(logger, "testWithStartWithOk").start();
        final Object result = m.call(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Assertions.assertEquals(m, Meter.getCurrentInstance());
                m.ok();
                return null;
            }
        });

        Assertions.assertNull(result);
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
    public void testWithStartWithOkAndIgnoredReturn() throws Exception {
        final Meter m = new Meter(logger, "testWithStartWithOk").start();
        final Object result = m.call(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Assertions.assertEquals(m, Meter.getCurrentInstance());
                m.ok();
                return 1000;
            }
        });

        Assertions.assertEquals(Integer.valueOf(1000), result);
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
        Assertions.assertFalse(stopEvent.getFormattedMessage().contains("result=1000"));
        Assertions.assertFalse(stopDataEvent.getFormattedMessage().contains("result:1000"));
    }

    @Test
    public void testNoStartNoOkAndReturn() throws Exception {
        final Meter m = new Meter(logger, "testWithStartWithOk");
        final Object result = m.call(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Assertions.assertEquals(m, Meter.getCurrentInstance());
                return 1000;
            }
        });

        Assertions.assertEquals(Integer.valueOf(1000), result);
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
        Assertions.assertTrue(stopEvent.getFormattedMessage().contains("result=1000"));
        Assertions.assertTrue(stopDataEvent.getFormattedMessage().contains("result:1000"));
    }

    @Test
    public void testNoStartWithOk() throws Exception {
        final Meter m = new Meter(logger, "testNoStartWithOk");
        final Object result = m.call(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Assertions.assertEquals(m, Meter.getCurrentInstance());
                m.ok();
                return null;
            }
        });

        Assertions.assertNull(result);
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
    public void testNoStartNoOk() throws Exception {
        final Meter m = new Meter(logger, "testNoStartNoOk");
        final Object result = m.call(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Assertions.assertEquals(m, Meter.getCurrentInstance());
                return null;
            }
        });

        Assertions.assertNull(result);
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
    public void testWithStartNoOk() throws Exception {
        final Meter m = new Meter(logger, "testWithStartNoOk").start();
        final Object result = m.call(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Assertions.assertEquals(m, Meter.getCurrentInstance());
                return null;
            }
        });

        Assertions.assertNull(result);
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
    public void testNoStartWithReject() throws Exception {
        final Meter m = new Meter(logger, "testNoStartWithReject");
        final Object result = m.call(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Assertions.assertEquals(m, Meter.getCurrentInstance());
                m.reject("a");
                return null;
            }
        });

        Assertions.assertNull(result);
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
    public void testNoStartWithFail() throws Exception {
        final Meter m = new Meter(logger, "testNoStartWithFail");
        final Object result = m.call(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Assertions.assertEquals(m, Meter.getCurrentInstance());
                m.fail("a");
                return null;
            }
        });

        Assertions.assertNull(result);
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
    public void testNoStartWithException() {
        final Meter m = new Meter(logger, "testNoStartWithException");
        try {
            m.call(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    Assertions.assertEquals(m, Meter.getCurrentInstance());
                    throw new IllegalArgumentException("someException");
                }
            });
        } catch (final Exception e) {
            Assertions.assertEquals("someException", e.getMessage());
            Assertions.assertEquals(IllegalArgumentException.class, e.getClass());
        }
        Assertions.assertFalse(m.isOK());
        Assertions.assertFalse(m.isReject());
        Assertions.assertTrue(m.isFail());
        Assertions.assertFalse(m.isSlow());
        Assertions.assertEquals(4, logger.getEventCount());
        Assertions.assertEquals("java.lang.IllegalArgumentException", m.failPath);
        Assertions.assertEquals("someException", m.failMessage);
        final MockLoggerEvent startEvent = logger.getEvent(0);
        final MockLoggerEvent startDataEvent = logger.getEvent(1);
        final MockLoggerEvent stopEvent = logger.getEvent(2);
        final MockLoggerEvent stopDataEvent = logger.getEvent(3);
        Assertions.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assertions.assertEquals(Markers.MSG_FAIL, stopEvent.getMarker());
        Assertions.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assertions.assertEquals(Markers.DATA_FAIL, stopDataEvent.getMarker());
        Assertions.assertTrue(stopEvent.getFormattedMessage().contains("someException"));
    }

    @Test
    public void testNoStartWithException2() {
        final Meter m = new Meter(logger, "testNoStartWithException");
        try {
            m.call(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    Assertions.assertEquals(m, Meter.getCurrentInstance());
                    throw new IOException("someException");
                }
            });
        } catch (final Exception e) {
            Assertions.assertEquals("someException", e.getMessage());
            Assertions.assertEquals(IOException.class, e.getClass());
        }
        Assertions.assertFalse(m.isOK());
        Assertions.assertFalse(m.isReject());
        Assertions.assertTrue(m.isFail());
        Assertions.assertFalse(m.isSlow());
        Assertions.assertEquals(4, logger.getEventCount());
        Assertions.assertEquals("java.io.IOException", m.failPath);
        Assertions.assertEquals("someException", m.failMessage);
        final MockLoggerEvent startEvent = logger.getEvent(0);
        final MockLoggerEvent startDataEvent = logger.getEvent(1);
        final MockLoggerEvent stopEvent = logger.getEvent(2);
        final MockLoggerEvent stopDataEvent = logger.getEvent(3);
        Assertions.assertEquals(Markers.MSG_START, startEvent.getMarker());
        Assertions.assertEquals(Markers.MSG_FAIL, stopEvent.getMarker());
        Assertions.assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        Assertions.assertEquals(Markers.DATA_FAIL, stopDataEvent.getMarker());
        Assertions.assertTrue(stopEvent.getFormattedMessage().contains("someException"));
    }
}
