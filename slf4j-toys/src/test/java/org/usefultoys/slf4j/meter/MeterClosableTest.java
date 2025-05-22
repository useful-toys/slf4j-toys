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


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.SessionConfig;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;
import static org.slf4j.impl.MockLoggerEvent.Level.ERROR;
import static org.slf4j.impl.MockLoggerEvent.Level.DEBUG;
import static org.slf4j.impl.MockLoggerEvent.Level.INFO;
import static org.slf4j.impl.MockLoggerEvent.Level.TRACE;
import static org.usefultoys.slf4j.meter.Markers.*;
import static org.usefultoys.slf4j.meter.Markers.DATA_FAIL;
import static org.usefultoys.slf4j.meter.Markers.DATA_OK;
import static org.usefultoys.slf4j.meter.Markers.DATA_REJECT;
import static org.usefultoys.slf4j.meter.Markers.INCONSISTENT_OK;
import static org.usefultoys.slf4j.meter.Markers.MSG_FAIL;
import static org.usefultoys.slf4j.meter.Markers.MSG_REJECT;

/**
 * @author Daniel Felix Ferber
 */
@SuppressWarnings("UnusedAssignment")
class MeterClosableTest {

    static final String meterCategory = "category";
    static final MockLogger logger = (MockLogger) LoggerFactory.getLogger(meterCategory);

    @BeforeAll
    static void validate() {
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
    void testWithStartWithOk() {
        final Meter m;
        try (final Meter m2 = m = new Meter(logger, "testWithStartWithOk").start()) {
            assertEquals(m, Meter.getCurrentInstance());
            m2.ok();
        }

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
    void testNoStartWithOk() {
        final Meter m;
        try (final Meter m2 = m = new Meter(logger, "testNoStartWithOk")) {
            assertNotEquals(m, Meter.getCurrentInstance());
            m2.ok();
        }

        assertTrue(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertFalse(m.isSlow());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailPath());
        logger.assertEvent(0, ERROR, INCONSISTENT_OK);
        logger.assertEvent(1, INFO, MSG_OK);
        logger.assertEvent(2, TRACE, DATA_OK);
    }

    @Test
    void testStartWithOkAndPath() {
        final Meter m;
        try (final Meter m2 = m = new Meter(logger, "testStartWithOkAndPath").start()) {
            assertEquals(m, Meter.getCurrentInstance());
            m2.ok("a");
        }

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
    void testNoStartNoOk() {
        final Meter m;
        try (final Meter m2 = m = new Meter(logger, "testNoStartNoOk")) {
            assertNotEquals(m, Meter.getCurrentInstance());
        }

        assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertTrue(m.isFail());
        assertFalse(m.isSlow());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertEquals("try-with-resources", m.getFailPath());
        logger.assertEvent(0, ERROR, INCONSISTENT_CLOSE);
        logger.assertEvent(1, ERROR, MSG_FAIL);
        logger.assertEvent(2, TRACE, DATA_FAIL);
    }

    @Test
    void testWithStartNoOk() {
        final Meter m;
        try (final Meter m2 = m = new Meter(logger, "testWithStartNoOk").start()) {
            assertEquals(m, Meter.getCurrentInstance());
        }

        assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertTrue(m.isFail());
        assertFalse(m.isSlow());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertEquals("try-with-resources", m.getFailPath());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, ERROR, MSG_FAIL, "try-with-resources");
        logger.assertEvent(3, TRACE, DATA_FAIL);
    }

    @Test
    void testWithStartWithReject() {
        final Meter m;
        try (final Meter m2 = m = new Meter(logger, "testWithStartWithReject").start()) {
            assertEquals(m, Meter.getCurrentInstance());
            m2.reject("a");
        }

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
    void testWithStartWithFail() {
        final Meter m;
        try (final Meter m2 = m = new Meter(logger, "testWithStartWithFail").start()) {
            assertEquals(m, Meter.getCurrentInstance());
            m2.fail("a");
        }

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
    void testWithStartWithException() {
        Meter m = null;
        try (final Meter m2 = m = new Meter(logger, "testWithStartWithException").start()) {
            assertEquals(m, Meter.getCurrentInstance());
            throw new RuntimeException("someException");
        } catch (final RuntimeException e) {
            // ignore
        }

        assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertTrue(m.isFail());
        assertFalse(m.isSlow());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertEquals("try-with-resources", m.getFailPath());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, ERROR, MSG_FAIL, "try-with-resources");
        logger.assertEvent(3, TRACE, DATA_FAIL);
    }
}
