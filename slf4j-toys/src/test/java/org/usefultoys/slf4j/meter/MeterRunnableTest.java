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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.*;
import static org.slf4j.impl.MockLoggerEvent.Level.*;
import static org.usefultoys.slf4j.meter.Markers.*;

import org.slf4j.impl.MockLogger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.SessionConfig;

import java.nio.charset.Charset;

/**
 * @author Daniel Felix Ferber
 */
@SuppressWarnings("UnusedAssignment")
public class MeterRunnableTest {

    static final String meterCategory = "category";
    static final MockLogger logger = (MockLogger) LoggerFactory.getLogger(meterCategory);

    public MeterRunnableTest() {
    }

    @BeforeAll
    static void validateConsistentCharset() {
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
        final Meter m = new Meter(logger).start();
        m.run(new Runnable() {
            @Override
            public void run() {
                assertEquals(m, Meter.getCurrentInstance());
                m.ok();
            }
        });

        assertTrue(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertFalse(m.isSlow());
        assertEquals(4, logger.getEventCount());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, INFO, MSG_OK);
        logger.assertEvent(3, TRACE, DATA_OK);
    }

    @Test
    public void testNoStartWithOk() {
        final Meter m = new Meter(logger);
        m.run(new Runnable() {
            @Override
            public void run() {
                assertEquals(m, Meter.getCurrentInstance());
                m.ok();
            }
        });

        assertTrue(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertFalse(m.isSlow());
        assertEquals(4, logger.getEventCount());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, INFO, MSG_OK);
        logger.assertEvent(3, TRACE, DATA_OK);
    }

    @Test
    public void testNoStartNoOk() {
        final Meter m = new Meter(logger);
        m.run(new Runnable() {
            @Override
            public void run() {
                assertEquals(m, Meter.getCurrentInstance());
            }
        });

        assertTrue(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertFalse(m.isSlow());
        assertEquals(4, logger.getEventCount());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, INFO, MSG_OK);
        logger.assertEvent(3, TRACE, DATA_OK);
    }

    @Test
    public void testWithStartNoOk() {
        final Meter m = new Meter(logger).start();
        m.run(new Runnable() {
            @Override
            public void run() {
                assertEquals(m, Meter.getCurrentInstance());
            }
        });

        assertTrue(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertFalse(m.isSlow());
        assertEquals(4, logger.getEventCount());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, INFO, MSG_OK);
        logger.assertEvent(3, TRACE, DATA_OK);
    }

    @Test
    public void testNoStartWithReject() {
        final Meter m = new Meter(logger);
        m.run(new Runnable() {
            @Override
            public void run() {
                assertEquals(m, Meter.getCurrentInstance());
                m.reject("a");
            }
        });

        assertFalse(m.isOK());
        assertTrue(m.isReject());
        assertFalse(m.isFail());
        assertFalse(m.isSlow());
        assertEquals(4, logger.getEventCount());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, INFO, MSG_REJECT);
        logger.assertEvent(3, TRACE, DATA_REJECT);
    }

    @Test
    public void testNoStartWithFail() {
        final Meter m = new Meter(logger);
        m.run(new Runnable() {
            @Override
            public void run() {
                assertEquals(m, Meter.getCurrentInstance());
                m.fail("a");
            }
        });

        assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertTrue(m.isFail());
        assertFalse(m.isSlow());
        assertEquals(4, logger.getEventCount());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, ERROR, MSG_FAIL);
        logger.assertEvent(3, TRACE, DATA_FAIL);
    }

    @Test
    public void testNoStartWithException() {
        final Meter m = new Meter(logger);
        try {
            m.run(new Runnable() {
                @Override
                public void run() {
                    assertEquals(m, Meter.getCurrentInstance());
                    throw new IllegalArgumentException("someException");
                }
            });
        } catch (final IllegalArgumentException e) {
            assertEquals("someException", e.getMessage());
        }
        assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertTrue(m.isFail());
        assertFalse(m.isSlow());
        assertEquals(4, logger.getEventCount());
        assertEquals("java.lang.IllegalArgumentException", m.failPath);
        assertEquals("someException", m.failMessage);
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, ERROR, MSG_FAIL);
        logger.assertEvent(3, TRACE, DATA_FAIL);
    }
}
