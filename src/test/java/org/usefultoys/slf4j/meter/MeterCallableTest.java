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

import org.junit.jupiter.api.*;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.SessionConfig;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.slf4j.impl.MockLoggerEvent.Level.*;
import static org.usefultoys.slf4j.meter.Markers.*;

/**
 * @author Daniel Felix Ferber
 */
@SuppressWarnings("UnusedAssignment")
class MeterCallableTest {

    static final String meterCategory = "category";
    static final MockLogger logger = (MockLogger) LoggerFactory.getLogger(meterCategory);

    @BeforeAll
    static void validateConsistentCharset() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeAll
    static void setupMeterSettings() {
        MeterConfig.progressPeriodMilliseconds = 0;
        MeterConfig.printCategory = false;
        MeterConfig.printStatus = true;
    }

    @AfterAll
    static void tearDownMeterSettings() {
        MeterConfig.reset();
    }

    @BeforeEach
    void setupLogger() {
        logger.clearEvents();
        logger.setEnabled(true);
    }

    @AfterEach
    void clearLogger() {
        logger.clearEvents();
        logger.setEnabled(true);
    }

    @Test
    void testWithStartWithOk() throws Exception {
        final Meter m = new Meter(logger, "testWithStartWithOk").start();
        final Object result = m.call(() -> {
            assertEquals(m, Meter.getCurrentInstance());
            m.ok();
            return null;
        });

        assertNull(result);
        assertTrue(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertFalse(m.isSlow());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailPath());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, INFO, MSG_OK);
        logger.assertEvent(3, TRACE, DATA_OK);
    }

    @Test
    void testWithStartWithOkAndIgnoredReturn() throws Exception {
        final Meter m = new Meter(logger, "testWithStartWithOk").start();
        final Integer result = m.call(() -> {
            assertEquals(m, Meter.getCurrentInstance());
            m.ok();
            return 1000;
        });

        assertEquals(Integer.valueOf(1000), result);
        assertTrue(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertFalse(m.isSlow());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailPath());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, INFO, MSG_OK);
        logger.assertEvent(3, TRACE, DATA_OK);
    }

    @Test
    void testNoStartNoOkAndReturn() throws Exception {
        final Meter m = new Meter(logger, "testWithStartWithOk");
        final Integer result = m.call(() -> {
            assertEquals(m, Meter.getCurrentInstance());
            return 1000;
        });

        assertEquals(Integer.valueOf(1000), result);
        assertTrue(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertFalse(m.isSlow());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailPath());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, INFO, MSG_OK, "result=1000");
        logger.assertEvent(3, TRACE, DATA_OK, "result:1000");
    }

    @Test
    void testNoStartWithOk() throws Exception {
        final Meter m = new Meter(logger, "testNoStartWithOk");
        final Object result = m.call(() -> {
            assertEquals(m, Meter.getCurrentInstance());
            m.ok();
            return null;
        });

        assertNull(result);
        assertTrue(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertFalse(m.isSlow());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailPath());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, INFO, MSG_OK);
        logger.assertEvent(3, TRACE, DATA_OK);
    }

    @Test
    void testNoStartWithOkAndPath() throws Exception {
        final Meter m = new Meter(logger, "testNoStartWithOk");
        final Object result = m.call(() -> {
            assertEquals(m, Meter.getCurrentInstance());
            m.ok("a");
            return null;
        });

        assertNull(result);
        assertTrue(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertFalse(m.isSlow());
        assertEquals("a", m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailPath());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, INFO, MSG_OK);
        logger.assertEvent(3, TRACE, DATA_OK);
    }

    @Test
    void testNoStartNoOk() throws Exception {
        final Meter m = new Meter(logger, "testNoStartNoOk");
        final Object result = m.call(() -> {
            assertEquals(m, Meter.getCurrentInstance());
            return null;
        });

        assertNull(result);
        assertTrue(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertFalse(m.isSlow());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailPath());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, INFO, MSG_OK);
        logger.assertEvent(3, TRACE, DATA_OK);
    }

    @Test
    void testWithStartNoOk() throws Exception {
        final Meter m = new Meter(logger, "testWithStartNoOk").start();
        final Object result = m.call(() -> {
            assertEquals(m, Meter.getCurrentInstance());
            return null;
        });

        assertNull(result);
        assertTrue(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertFalse(m.isSlow());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailPath());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, INFO, MSG_OK);
        logger.assertEvent(3, TRACE, DATA_OK);
    }

    @Test
    void testNoStartWithReject() throws Exception {
        final Meter m = new Meter(logger, "testNoStartWithReject");
        final Object result = m.call(() -> {
            assertEquals(m, Meter.getCurrentInstance());
            m.reject("a");
            return null;
        });

        assertNull(result);
        assertFalse(m.isOK());
        assertTrue(m.isReject());
        assertFalse(m.isFail());
        assertFalse(m.isSlow());
        assertNull(m.getOkPath());
        assertEquals("a", m.getRejectPath());
        assertNull(m.getFailPath());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, INFO, MSG_REJECT);
        logger.assertEvent(3, TRACE, DATA_REJECT);
    }

    @Test
    void testNoStartWithFail() throws Exception {
        final Meter m = new Meter(logger, "testNoStartWithFail");
        final Object result = m.call(() -> {
            assertEquals(m, Meter.getCurrentInstance());
            m.fail("a");
            return null;
        });

        assertNull(result);
        assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertTrue(m.isFail());
        assertFalse(m.isSlow());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertEquals("a", m.getFailPath());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, ERROR, MSG_FAIL);
        logger.assertEvent(3, TRACE, DATA_FAIL);
    }

    @Test
    void testNoStartWithException() {
        final Meter m = new Meter(logger, "testNoStartWithException");
        try {
            m.call(() -> {
                assertEquals(m, Meter.getCurrentInstance());
                throw new IllegalArgumentException("someException");
            });
        } catch (final Exception e) {
            assertEquals("someException", e.getMessage());
            assertSame(IllegalArgumentException.class, e.getClass());
        }
        assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertTrue(m.isFail());
        assertFalse(m.isSlow());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertEquals("java.lang.IllegalArgumentException", m.getFailPath());
        assertEquals("someException", m.getFailMessage());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, ERROR, MSG_FAIL);
        logger.assertEvent(3, TRACE, DATA_FAIL);
    }

    @Test
    void testNoStartWithException2() {
        final Meter m = new Meter(logger, "testNoStartWithException");
        try {
            m.call((Callable<Void>) () -> {
                assertEquals(m, Meter.getCurrentInstance());
                throw new IOException("someException");
            });
        } catch (final Exception e) {
            assertEquals("someException", e.getMessage());
            assertSame(IOException.class, e.getClass());
        }
        assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertTrue(m.isFail());
        assertFalse(m.isSlow());
        assertEquals("java.io.IOException", m.getFailPath());
        assertEquals("someException", m.getFailMessage());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, ERROR, MSG_FAIL);
        logger.assertEvent(3, TRACE, DATA_FAIL);
    }
}
