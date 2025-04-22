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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.impl.TestLogger;
import org.slf4j.impl.TestLoggerEvent;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.SessionConfig;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Daniel Felix Ferber
 */
@SuppressWarnings("UnusedAssignment")
public class MeterSafeCallableTest {

    static final String meterCategory = "category";
    static final TestLogger logger = (TestLogger) LoggerFactory.getLogger(meterCategory);

    public MeterSafeCallableTest() {
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
        final Meter m = new Meter(logger, "testWithStartWithOk").start();
        Object result = m.safeCall(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                assertEquals(m, Meter.getCurrentInstance());
                m.ok();
                return null;
            }
        });

        assertNull(result);
        assertTrue(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertFalse(m.isSlow());
        assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_OK, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_OK, stopDataEvent.getMarker());
    }

    @Test
    public void testWithStartWithOkAndIgnoredReturn() {
        final Meter m = new Meter(logger, "testWithStartWithOk").start();
        Object result = m.safeCall(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                assertEquals(m, Meter.getCurrentInstance());
                m.ok();
                return 1000;
            }
        });

        assertEquals(Integer.valueOf(1000), result);
        assertTrue(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertFalse(m.isSlow());
        assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_OK, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_OK, stopDataEvent.getMarker());
        assertFalse(stopEvent.getFormattedMessage().contains("result=1000"));
        assertFalse(stopDataEvent.getFormattedMessage().contains("result:1000"));
    }

    @Test
    public void testNoStartNoOkAndReturn() {
        final Meter m = new Meter(logger, "testWithStartWithOk");
        Object result = m.safeCall(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                assertEquals(m, Meter.getCurrentInstance());
                return 1000;
            }
        });

        assertEquals(Integer.valueOf(1000), result);
        assertTrue(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertFalse(m.isSlow());
        assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_OK, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_OK, stopDataEvent.getMarker());
        assertTrue(stopEvent.getFormattedMessage().contains("result=1000"));
        assertTrue(stopDataEvent.getFormattedMessage().contains("result:1000"));
    }

    @Test
    public void testNoStartWithOk() {
        final Meter m = new Meter(logger, "testNoStartWithOk");
        Object result = m.safeCall(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                assertEquals(m, Meter.getCurrentInstance());
                m.ok();
                return null;
            }
        });

        assertNull(result);
        assertTrue(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertFalse(m.isSlow());
        assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_OK, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_OK, stopDataEvent.getMarker());
    }

    @Test
    public void testNoStartNoOk() {
        final Meter m = new Meter(logger, "testNoStartNoOk");
        Object result = m.safeCall(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                assertEquals(m, Meter.getCurrentInstance());
                return null;
            }
        });

        assertNull(result);
        assertTrue(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertFalse(m.isSlow());
        assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_OK, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_OK, stopDataEvent.getMarker());
    }

    @Test
    public void testWithStartNoOk() {
        final Meter m = new Meter(logger, "testWithStartNoOk").start();
        Object result = m.safeCall(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                assertEquals(m, Meter.getCurrentInstance());
                return null;
            }
        });

        assertNull(result);
        assertTrue(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertFalse(m.isSlow());
        assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_OK, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_OK, stopDataEvent.getMarker());
    }

    @Test
    public void testNoStartWithReject() {
        final Meter m = new Meter(logger, "testNoStartWithReject");
        Object result = m.safeCall(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                assertEquals(m, Meter.getCurrentInstance());
                m.reject("a");
                return null;
            }
        });

        assertNull(result);
        assertFalse(m.isOK());
        assertTrue(m.isReject());
        assertFalse(m.isFail());
        assertFalse(m.isSlow());
        assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_REJECT, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_REJECT, stopDataEvent.getMarker());
    }

    @Test
    public void testNoStartWithFail() {
        final Meter m = new Meter(logger, "testNoStartWithFail");
        Object result = m.safeCall(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                assertEquals(m, Meter.getCurrentInstance());
                m.fail("a");
                return null;
            }
        });

        assertNull(result);
        assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertTrue(m.isFail());
        assertFalse(m.isSlow());
        assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_FAIL, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_FAIL, stopDataEvent.getMarker());
    }

    @Test
    public void testNoStartWithException() {
        final Meter m = new Meter(logger, "testNoStartWithException");
        try {
            m.safeCall(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    assertEquals(m, Meter.getCurrentInstance());
                    throw new IllegalArgumentException("someException");
                }
            });
        } catch (RuntimeException e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
            assertEquals("someException", e.getMessage());
        }
        assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertTrue(m.isFail());
        assertFalse(m.isSlow());
        assertEquals(4, logger.getEventCount());
        assertEquals("java.lang.IllegalArgumentException", m.failPath);
        assertEquals("someException", m.failMessage);
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_FAIL, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_FAIL, stopDataEvent.getMarker());
        assertTrue(stopEvent.getFormattedMessage().contains("someException"));
    }

    @Test
    public void testNoStartWithException2() {
        final Meter m = new Meter(logger, "testNoStartWithException");
        try {
            m.safeCall(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    assertEquals(m, Meter.getCurrentInstance());
                    throw new IOException("someException");
                }
            });
        } catch (RuntimeException e) {
            assertEquals("Meter.safeCall wrapped exception.", e.getMessage());
            assertEquals(RuntimeException.class, e.getClass());
            assertEquals(IOException.class, e.getCause().getClass());
            assertEquals("someException", e.getCause().getMessage());
        }
        assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertTrue(m.isFail());
        assertFalse(m.isSlow());
        assertEquals(4, logger.getEventCount());
        assertEquals("java.io.IOException", m.failPath);
        assertEquals("someException", m.failMessage);
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_FAIL, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_FAIL, stopDataEvent.getMarker());
        assertTrue(stopEvent.getFormattedMessage().contains("someException"));
    }
}
