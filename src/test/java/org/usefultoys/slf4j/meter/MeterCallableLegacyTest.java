/*
 * Copyright 2026 Daniel Felix Ferber
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
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.meter.Meter;
import org.usefultoys.slf4j.meter.MeterConfig;
import org.usefultoys.slf4jtestmock.*;
import org.usefultoys.test.*;

import java.io.IOException;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.slf4j.impl.MockLoggerEvent.Level.*;
import static org.usefultoys.slf4j.meter.Markers.*;

/**
 * Unit tests for {@link Meter#call(Callable)} method.
 * <p>
 * Tests validate that Meter correctly executes Callable operations with proper lifecycle management,
 * context propagation, and outcome handling.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Callable with start() and ok():</b> Validates explicit start and ok transitions</li>
 *   <li><b>Callable with return value:</b> Ensures return values are properly propagated</li>
 *   <li><b>Callable without start():</b> Tests auto-start behavior</li>
 *   <li><b>Callable without ok():</b> Tests auto-ok behavior on normal completion</li>
 *   <li><b>Callable with reject():</b> Validates reject outcome handling</li>
 *   <li><b>Callable with fail():</b> Validates fail outcome handling</li>
 *   <li><b>Callable with exceptions:</b> Tests exception propagation and fail state</li>
 * </ul>
 *
 * @author Daniel Felix Ferber
 */
@SuppressWarnings("UnusedAssignment")
@ValidateCharset
@ResetMeterConfig
@WithLocale("en")
@ValidateCleanMeter
@WithMockLogger
@WithMockLoggerDebug
class MeterCallableLegacyTest {

    @Slf4jMock
    private Logger logger;

    @BeforeAll
    static void setupMeterSettings() {
        MeterConfig.progressPeriodMilliseconds = 0;
        MeterConfig.printCategory = false;
        MeterConfig.printStatus = true;
    }

    @Test
    @DisplayName("should complete successfully when callable explicitly starts and calls ok")
    void shouldCompleteSuccessfullyWhenCallableExplicitlyStartsAndCallsOk() throws Exception {
        // Given: a meter with explicit start
        final Meter m = new Meter(logger, "testWithStartWithOk").start();
        
        // When: callable executes and calls ok
        final Object result = m.call(() -> {
            assertEquals(m, Meter.getCurrentInstance(), "should have meter as current instance");
            m.ok();
            return null;
        });

        // Then: meter should be in OK state and log appropriate events
        assertNull(result, "should return null");
        assertTrue(m.isOK(), "should be OK");
        assertFalse(m.isReject(), "should not be reject");
        assertFalse(m.isFail(), "should not be fail");
        assertFalse(m.isSlow(), "should not be slow");
        assertNull(m.getOkPath(), "should have no ok path");
        assertNull(m.getRejectPath(), "should have no reject path");
        assertNull(m.getFailPath(), "should have no fail path");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_OK);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("should propagate return value when callable completes with ok")
    void shouldPropagateReturnValueWhenCallableCompletesWithOk() throws Exception {
        // Given: a meter with explicit start
        final Meter m = new Meter(logger, "testWithStartWithOk").start();
        
        // When: callable executes with ok and returns a value
        final Integer result = m.call(() -> {
            assertEquals(m, Meter.getCurrentInstance(), "should have meter as current instance");
            m.ok();
            return 1000;
        });

        // Then: should return the value and be in OK state
        assertEquals(Integer.valueOf(1000), result, "should return 1000");
        assertTrue(m.isOK(), "should be OK");
        assertFalse(m.isReject(), "should not be reject");
        assertFalse(m.isFail(), "should not be fail");
        assertFalse(m.isSlow(), "should not be slow");
        assertNull(m.getOkPath(), "should have no ok path");
        assertNull(m.getRejectPath(), "should have no reject path");
        assertNull(m.getFailPath(), "should have no fail path");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_OK);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("should auto-start and auto-ok when callable returns value without explicit calls")
    void shouldAutoStartAndAutoOkWhenCallableReturnsValueWithoutExplicitCalls() throws Exception {
        // Given: a meter without explicit start
        final Meter m = new Meter(logger, "testWithStartWithOk");
        
        // When: callable executes and returns a value without calling ok
        final Integer result = m.call(() -> {
            assertEquals(m, Meter.getCurrentInstance(), "should have meter as current instance");
            return 1000;
        });

        // Then: should return the value and be in OK state with logged result
        assertEquals(Integer.valueOf(1000), result, "should return 1000");
        assertTrue(m.isOK(), "should be OK");
        assertFalse(m.isReject(), "should not be reject");
        assertFalse(m.isFail(), "should not be fail");
        assertFalse(m.isSlow(), "should not be slow");
        assertNull(m.getOkPath(), "should have no ok path");
        assertNull(m.getRejectPath(), "should have no reject path");
        assertNull(m.getFailPath(), "should have no fail path");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_OK, "result=1000");
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_OK, "result:1000");
    }

    @Test
    @DisplayName("should auto-start when callable calls ok without explicit start")
    void shouldAutoStartWhenCallableCallsOkWithoutExplicitStart() throws Exception {
        // Given: a meter without explicit start
        final Meter m = new Meter(logger, "testNoStartWithOk");
        
        // When: callable executes and calls ok
        final Object result = m.call(() -> {
            assertEquals(m, Meter.getCurrentInstance(), "should have meter as current instance");
            m.ok();
            return null;
        });

        // Then: should be in OK state
        assertNull(result, "should return null");
        assertTrue(m.isOK(), "should be OK");
        assertFalse(m.isReject(), "should not be reject");
        assertFalse(m.isFail(), "should not be fail");
        assertFalse(m.isSlow(), "should not be slow");
        assertNull(m.getOkPath(), "should have no ok path");
        assertNull(m.getRejectPath(), "should have no reject path");
        assertNull(m.getFailPath(), "should have no fail path");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_OK);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("should record ok path when callable calls ok with path parameter")
    void shouldRecordOkPathWhenCallableCallsOkWithPathParameter() throws Exception {
        // Given: a meter without explicit start
        final Meter m = new Meter(logger, "testNoStartWithOk");
        
        // When: callable executes and calls ok with path
        final Object result = m.call(() -> {
            assertEquals(m, Meter.getCurrentInstance(), "should have meter as current instance");
            m.ok("a");
            return null;
        });

        // Then: should be in OK state with recorded path
        assertNull(result, "should return null");
        assertTrue(m.isOK(), "should be OK");
        assertFalse(m.isReject(), "should not be reject");
        assertFalse(m.isFail(), "should not be fail");
        assertFalse(m.isSlow(), "should not be slow");
        assertEquals("a", m.getOkPath(), "should have ok path 'a'");
        assertNull(m.getRejectPath(), "should have no reject path");
        assertNull(m.getFailPath(), "should have no fail path");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_OK);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("should auto-ok when callable completes normally without explicit ok call")
    void shouldAutoOkWhenCallableCompletesNormallyWithoutExplicitOkCall() throws Exception {
        // Given: a meter without explicit start
        final Meter m = new Meter(logger, "testNoStartNoOk");
        
        // When: callable executes without calling ok
        final Object result = m.call(() -> {
            assertEquals(m, Meter.getCurrentInstance(), "should have meter as current instance");
            return null;
        });

        // Then: should be in OK state automatically
        assertNull(result, "should return null");
        assertTrue(m.isOK(), "should be OK");
        assertFalse(m.isReject(), "should not be reject");
        assertFalse(m.isFail(), "should not be fail");
        assertFalse(m.isSlow(), "should not be slow");
        assertNull(m.getOkPath(), "should have no ok path");
        assertNull(m.getRejectPath(), "should have no reject path");
        assertNull(m.getFailPath(), "should have no fail path");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_OK);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("should auto-ok when callable completes normally with explicit start but no ok")
    void shouldAutoOkWhenCallableCompletesNormallyWithExplicitStartButNoOk() throws Exception {
        // Given: a meter with explicit start
        final Meter m = new Meter(logger, "testWithStartNoOk").start();
        
        // When: callable executes without calling ok
        final Object result = m.call(() -> {
            assertEquals(m, Meter.getCurrentInstance(), "should have meter as current instance");
            return null;
        });

        // Then: should be in OK state automatically
        assertNull(result, "should return null");
        assertTrue(m.isOK(), "should be OK");
        assertFalse(m.isReject(), "should not be reject");
        assertFalse(m.isFail(), "should not be fail");
        assertFalse(m.isSlow(), "should not be slow");
        assertNull(m.getOkPath(), "should have no ok path");
        assertNull(m.getRejectPath(), "should have no reject path");
        assertNull(m.getFailPath(), "should have no fail path");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_OK);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("should transition to reject state when callable calls reject")
    void shouldTransitionToRejectStateWhenCallableCallsReject() throws Exception {
        // Given: a meter without explicit start
        final Meter m = new Meter(logger, "testNoStartWithReject");
        
        // When: callable executes and calls reject
        final Object result = m.call(() -> {
            assertEquals(m, Meter.getCurrentInstance(), "should have meter as current instance");
            m.reject("a");
            return null;
        });

        // Then: should be in REJECT state
        assertNull(result, "should return null");
        assertFalse(m.isOK(), "should not be OK");
        assertTrue(m.isReject(), "should be reject");
        assertFalse(m.isFail(), "should not be fail");
        assertFalse(m.isSlow(), "should not be slow");
        assertNull(m.getOkPath(), "should have no ok path");
        assertEquals("a", m.getRejectPath(), "should have reject path 'a'");
        assertNull(m.getFailPath(), "should have no fail path");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_REJECT);
    }

    @Test
    @DisplayName("should transition to fail state when callable calls fail")
    void shouldTransitionToFailStateWhenCallableCallsFail() throws Exception {
        // Given: a meter without explicit start
        final Meter m = new Meter(logger, "testNoStartWithFail");
        
        // When: callable executes and calls fail
        final Object result = m.call(() -> {
            assertEquals(m, Meter.getCurrentInstance(), "should have meter as current instance");
            m.fail("a");
            return null;
        });

        // Then: should be in FAIL state
        assertNull(result, "should return null");
        assertFalse(m.isOK(), "should not be OK");
        assertFalse(m.isReject(), "should not be reject");
        assertTrue(m.isFail(), "should be fail");
        assertFalse(m.isSlow(), "should not be slow");
        assertNull(m.getOkPath(), "should have no ok path");
        assertNull(m.getRejectPath(), "should have no reject path");
        assertEquals("a", m.getFailPath(), "should have fail path 'a'");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, ERROR, MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_FAIL);
    }

    @Test
    @DisplayName("should transition to fail state and propagate exception when callable throws unchecked exception")
    void shouldTransitionToFailStateAndPropagateExceptionWhenCallableThrowsUncheckedException() {
        // Given: a meter without explicit start
        final Meter m = new Meter(logger, "testNoStartWithException");
        
        // When: callable throws an unchecked exception
        try {
            m.call(() -> {
                assertEquals(m, Meter.getCurrentInstance(), "should have meter as current instance");
                throw new IllegalArgumentException("someException");
            });
        } catch (final Exception e) {
            // Then: exception should be propagated
            assertEquals("someException", e.getMessage(), "should have exception message");
            assertSame(IllegalArgumentException.class, e.getClass(), "should be IllegalArgumentException");
        }
        
        // Then: meter should be in FAIL state with exception info
        assertFalse(m.isOK(), "should not be OK");
        assertFalse(m.isReject(), "should not be reject");
        assertTrue(m.isFail(), "should be fail");
        assertFalse(m.isSlow(), "should not be slow");
        assertNull(m.getOkPath(), "should have no ok path");
        assertNull(m.getRejectPath(), "should have no reject path");
        assertEquals("java.lang.IllegalArgumentException", m.getFailPath(), "should have exception class as fail path");
        assertEquals("someException", m.getFailMessage(), "should have exception message");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, ERROR, MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_FAIL);
    }

    @Test
    @DisplayName("should transition to fail state and propagate exception when callable throws checked exception")
    void shouldTransitionToFailStateAndPropagateExceptionWhenCallableThrowsCheckedException() {
        // Given: a meter without explicit start
        final Meter m = new Meter(logger, "testNoStartWithException");
        
        // When: callable throws a checked exception
        try {
            m.call((Callable<Void>) () -> {
                assertEquals(m, Meter.getCurrentInstance(), "should have meter as current instance");
                throw new IOException("someException");
            });
        } catch (final Exception e) {
            // Then: exception should be propagated
            assertEquals("someException", e.getMessage(), "should have exception message");
            assertSame(IOException.class, e.getClass(), "should be IOException");
        }
        
        // Then: meter should be in FAIL state with exception info
        assertFalse(m.isOK(), "should not be OK");
        assertFalse(m.isReject(), "should not be reject");
        assertTrue(m.isFail(), "should be fail");
        assertFalse(m.isSlow(), "should not be slow");
        assertEquals("java.io.IOException", m.getFailPath(), "should have exception class as fail path");
        assertEquals("someException", m.getFailMessage(), "should have exception message");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, ERROR, MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_FAIL);
    }
}
