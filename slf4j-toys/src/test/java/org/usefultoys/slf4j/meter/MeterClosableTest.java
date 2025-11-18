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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.impl.MockLogger;
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
    @DisplayName("Meter with correct try-with-resources usage, started and successful operation")
    void meterStartedAndSuccessfulOperation() {
        final Meter m;
        try (final Meter m2 = m = new Meter(logger, "meterStartedAndSuccessfulOperation").start()) {
            assertEquals(m, Meter.getCurrentInstance(), "Current instance should be the started meter");
            m2.ok();
        }

        assertTrue(m.isOK(), "Meter should be in successful state");
        assertFalse(m.isReject(), "Meter should not be in rejected state");
        assertFalse(m.isFail(), "Meter should not be in failed state");
        assertFalse(m.isSlow(), "Meter should not be in slow state");
        assertNull(m.getOkPath(), "Success path should be null when no path specified");
        assertNull(m.getRejectPath(), "Reject path should be null");
        assertNull(m.getFailPath(), "Fail path should be null");
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, INFO, MSG_OK);
        logger.assertEvent(3, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("Meter with incorrect try-with-resources usage, not started but successful operation")
    void meterNotStartedButSuccessfulOperation() {
        final Meter m;
        try (final Meter m2 = m = new Meter(logger, "meterNotStartedButSuccessfulOperation")) {
            assertNotEquals(m, Meter.getCurrentInstance(), "Current instance should not be the non-started meter");
            m2.ok();
        }

        assertTrue(m.isOK(), "Meter should be in successful state despite not being started");
        assertFalse(m.isReject(), "Meter should not be in rejected state");
        assertFalse(m.isFail(), "Meter should not be in failed state");
        assertFalse(m.isSlow(), "Meter should not be in slow state");
        assertNull(m.getOkPath(), "Success path should be null when no path specified");
        assertNull(m.getRejectPath(), "Reject path should be null");
        assertNull(m.getFailPath(), "Fail path should be null");
        logger.assertEvent(0, ERROR, INCONSISTENT_OK);
        logger.assertEvent(1, INFO, MSG_OK);
        logger.assertEvent(2, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("Meter with correct try-with-resources usage, started and successful operation with path")
    void meterStartedAndSuccessfulOperationWithPath() {
        final Meter m;
        try (final Meter m2 = m = new Meter(logger, "meterStartedAndSuccessfulOperationWithPath").start()) {
            assertEquals(m, Meter.getCurrentInstance(), "Current instance should be the started meter");
            m2.ok("a");
        }

        assertTrue(m.isOK(), "Meter should be in successful state");
        assertFalse(m.isReject(), "Meter should not be in rejected state");
        assertFalse(m.isFail(), "Meter should not be in failed state");
        assertFalse(m.isSlow(), "Meter should not be in slow state");
        assertEquals("a", m.getOkPath(), "Success path should be 'a'");
        assertNull(m.getRejectPath(), "Reject path should be null");
        assertNull(m.getFailPath(), "Fail path should be null");
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, INFO, MSG_OK);
        logger.assertEvent(3, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("Meter with incorrect try-with-resources usage, not started and no completion notification")
    void meterNotStartedAndNoCompletion() {
        final Meter m;
        try (final Meter m2 = m = new Meter(logger, "meterNotStartedAndNoCompletion")) {
            assertNotEquals(m, Meter.getCurrentInstance(), "Current instance should not be the non-started meter");
        }

        assertFalse(m.isOK(), "Meter should not be in successful state");
        assertFalse(m.isReject(), "Meter should not be in rejected state");
        assertTrue(m.isFail(), "Meter should be in failed state");
        assertFalse(m.isSlow(), "Meter should not be in slow state");
        assertNull(m.getOkPath(), "Success path should be null");
        assertNull(m.getRejectPath(), "Reject path should be null");
        assertEquals("try-with-resources", m.getFailPath(), "Fail path should be 'try-with-resources'");
        logger.assertEvent(0, ERROR, INCONSISTENT_CLOSE);
        logger.assertEvent(1, ERROR, MSG_FAIL);
        logger.assertEvent(2, TRACE, DATA_FAIL);
    }

    @Test
    @DisplayName("Meter with correct try-with-resources usage, started but no completion notification")
    void meterStartedButNoCompletion() {
        final Meter m;
        try (final Meter m2 = m = new Meter(logger, "meterStartedButNoCompletion").start()) {
            assertEquals(m, Meter.getCurrentInstance(), "Current instance should be the started meter");
        }

        assertFalse(m.isOK(), "Meter should not be in successful state");
        assertFalse(m.isReject(), "Meter should not be in rejected state");
        assertTrue(m.isFail(), "Meter should be in failed state");
        assertFalse(m.isSlow(), "Meter should not be in slow state");
        assertNull(m.getOkPath(), "Success path should be null");
        assertNull(m.getRejectPath(), "Reject path should be null");
        assertEquals("try-with-resources", m.getFailPath(), "Fail path should be 'try-with-resources'");
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, ERROR, MSG_FAIL, "try-with-resources");
        logger.assertEvent(3, TRACE, DATA_FAIL);
    }

    @Test
    @DisplayName("Meter with correct try-with-resources usage, started and rejected operation")
    void meterStartedAndRejectedOperation() {
        final Meter m;
        try (final Meter m2 = m = new Meter(logger, "meterStartedAndRejectedOperation").start()) {
            assertEquals(m, Meter.getCurrentInstance(), "Current instance should be the started meter");
            m2.reject("a");
        }

        assertFalse(m.isOK(), "Meter should not be in successful state");
        assertTrue(m.isReject(), "Meter should be in rejected state");
        assertFalse(m.isFail(), "Meter should not be in failed state");
        assertFalse(m.isSlow(), "Meter should not be in slow state");
        assertNull(m.getOkPath(), "Success path should be null");
        assertEquals("a", m.getRejectPath(), "Reject path should be 'a'");
        assertNull(m.getFailPath(), "Fail path should be null");
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, INFO, MSG_REJECT);
        logger.assertEvent(3, TRACE, DATA_REJECT);
    }

    @Test
    @DisplayName("Meter with correct try-with-resources usage, started and failed operation")
    void meterStartedAndFailedOperation() {
        final Meter m;
        try (final Meter m2 = m = new Meter(logger, "meterStartedAndFailedOperation").start()) {
            assertEquals(m, Meter.getCurrentInstance(), "Current instance should be the started meter");
            m2.fail("a");
        }

        assertFalse(m.isOK(), "Meter should not be in successful state");
        assertFalse(m.isReject(), "Meter should not be in rejected state");
        assertTrue(m.isFail(), "Meter should be in failed state");
        assertFalse(m.isSlow(), "Meter should not be in slow state");
        assertNull(m.getOkPath(), "Success path should be null");
        assertNull(m.getRejectPath(), "Reject path should be null");
        assertEquals("a", m.getFailPath(), "Fail path should be 'a'");
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, ERROR, MSG_FAIL);
        logger.assertEvent(3, TRACE, DATA_FAIL);
    }

    @Test
    @DisplayName("Meter with correct try-with-resources usage, started but operation throws exception")
    void meterStartedWithThrownException() {
        Meter m = null;
        try (final Meter m2 = m = new Meter(logger, "meterStartedWithThrownException").start()) {
            assertEquals(m, Meter.getCurrentInstance(), "Current instance should be the started meter");
            throw new RuntimeException("someException");
        } catch (final RuntimeException e) {
            // ignore
        }

        assertFalse(m.isOK(), "Meter should not be in successful state");
        assertFalse(m.isReject(), "Meter should not be in rejected state");
        assertTrue(m.isFail(), "Meter should be in failed state");
        assertFalse(m.isSlow(), "Meter should not be in slow state");
        assertNull(m.getOkPath(), "Success path should be null");
        assertNull(m.getRejectPath(), "Reject path should be null");
        assertEquals("try-with-resources", m.getFailPath(), "Fail path should be 'try-with-resources'");
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, ERROR, MSG_FAIL, "try-with-resources");
        logger.assertEvent(3, TRACE, DATA_FAIL);
    }
}
