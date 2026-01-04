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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.impl.MockLogger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4jtestmock.AssertLogger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.slf4j.impl.MockLoggerEvent.Level.*;
import static org.usefultoys.slf4j.meter.Markers.*;

/**
 * Unit tests demonstrating the MeterExecutor interface functionality.
 * 
 * @author Daniel Felix Ferber
 * @author Co-authored-by: GitHub Copilot using GPT-5.2
 */
@DisplayName("MeterExecutor interface functionality tests")
class MeterExecutorTest {

    static final MockLogger logger = (MockLogger) LoggerFactory.getLogger("test.executor");

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
    @DisplayName("run() with start with ok")
    void testWithStartWithOk() {
        final Meter meter = new Meter(logger).start();
        final boolean[] executed = {false};
        
        meter.run(() -> {
            assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
            executed[0] = true;
            meter.ok();
        });
        
        assertTrue(executed[0], "Runnable should have been executed");
        assertTrue(meter.isOK());
        assertFalse(meter.isReject());
        assertFalse(meter.isFail());
        assertFalse(meter.isSlow());
        assertEquals(4, logger.getEventCount());
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_OK);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("run() with start with excessive start call")
    void testWithStartWithExcessiveStart() {
        final Meter meter = new Meter(logger).start();
        final boolean[] executed = {false};
        
        meter.run(() -> {
            meter.start(); // Excessive call to start() - should generate error log
            assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
            executed[0] = true;
            meter.ok();
        });
        
        assertTrue(executed[0], "Runnable should have been executed");
        assertTrue(meter.isOK(), "Meter should be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertFalse(meter.isFail(), "Meter should not be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals(7, logger.getEventCount(), "Should have 7 log events (4 normal + 3 from excessive start)");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, ERROR, INCONSISTENT_START); // Error from excessive start() call
        AssertLogger.assertEvent(logger, 3, DEBUG, MSG_START); // Duplicate start message
        AssertLogger.assertEvent(logger, 4, TRACE, DATA_START); // Duplicate start data
        AssertLogger.assertEvent(logger, 5, INFO, MSG_OK);
        AssertLogger.assertEvent(logger, 6, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("run() no start with ok")
    void testNoStartWithOk() {
        final Meter meter = new Meter(logger);
        final boolean[] executed = {false};
        
        meter.run(() -> {
            assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
            executed[0] = true;
            meter.ok();
        });
        
        assertTrue(executed[0], "Runnable should have been executed");
        assertTrue(meter.isOK());
        assertFalse(meter.isReject());
        assertFalse(meter.isFail());
        assertFalse(meter.isSlow());
        assertEquals(4, logger.getEventCount());
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_OK);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("run() no start no ok")
    void testNoStartNoOk() {
        final Meter meter = new Meter(logger);
        final boolean[] executed = {false};
        
        meter.run(() -> {
            assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
            executed[0] = true;
        });
        
        assertTrue(executed[0], "Runnable should have been executed");
        assertTrue(meter.isOK(), "Meter should be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertFalse(meter.isFail(), "Meter should not be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals(4, logger.getEventCount(), "Should have exactly 4 log events");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_OK);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("run() no start with reject")
    void testNoStartWithReject() {
        final Meter meter = new Meter(logger);
        final boolean[] executed = {false};
        
        meter.run(() -> {
            assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
            executed[0] = true;
            meter.reject("test rejection");
        });
        
        assertTrue(executed[0], "Runnable should have been executed");
        assertFalse(meter.isOK(), "Meter should not be in OK state");
        assertTrue(meter.isReject(), "Meter should be in reject state");
        assertFalse(meter.isFail(), "Meter should not be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals(4, logger.getEventCount(), "Should have exactly 4 log events");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_REJECT);
    }

    @Test
    @DisplayName("run() no start with fail")
    void testNoStartWithFail() {
        final Meter meter = new Meter(logger);
        final boolean[] executed = {false};
        
        meter.run(() -> {
            assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
            executed[0] = true;
            meter.fail("test failure");
        });
        
        assertTrue(executed[0], "Runnable should have been executed");
        assertFalse(meter.isOK(), "Meter should not be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertTrue(meter.isFail(), "Meter should be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals(4, logger.getEventCount(), "Should have exactly 4 log events");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, ERROR, MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_FAIL);
    }

    @Test
    @DisplayName("run() no start with exception")
    void testNoStartWithException() {
        final Meter meter = new Meter(logger);
        final boolean[] executed = {false};
        
        try {
            meter.run(() -> {
                assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
                executed[0] = true;
                throw new IllegalArgumentException("test exception");
            });
        } catch (final IllegalArgumentException e) {
            assertEquals("test exception", e.getMessage(), "Exception message should match");
        }
        
        assertTrue(executed[0], "Runnable should have been executed");
        assertFalse(meter.isOK(), "Meter should not be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertTrue(meter.isFail(), "Meter should be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals("java.lang.IllegalArgumentException", meter.failPath, "Fail path should match exception class");
        assertEquals("test exception", meter.failMessage, "Fail message should match exception message");
        assertEquals(4, logger.getEventCount(), "Should have exactly 4 log events");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, ERROR, MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_FAIL);
    }

    @Test
    @DisplayName("runOrReject() no start with ok")
    @SuppressWarnings("unchecked")
    void testRunOrRejectNoStartWithOk() {
        final Meter meter = new Meter(logger, "testRunOrReject");
        final boolean[] executed = {false};
        
        try {
            meter.runOrReject(() -> {
                assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
                executed[0] = true;
                meter.ok();
            }, IOException.class);
        } catch (final Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
        
        assertTrue(executed[0], "Runnable should have been executed");
        assertTrue(meter.isOK(), "Meter should be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertFalse(meter.isFail(), "Meter should not be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals(4, logger.getEventCount(), "Should have exactly 4 log events");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_OK);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("runOrReject() no start no ok")
    @SuppressWarnings("unchecked")
    void testRunOrRejectNoStartNoOk() {
        final Meter meter = new Meter(logger, "testRunOrRejectAutoOk");
        final boolean[] executed = {false};
        
        try {
            meter.runOrReject(() -> {
                assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
                executed[0] = true;
            }, IOException.class);
        } catch (final Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
        
        assertTrue(executed[0], "Runnable should have been executed");
        assertTrue(meter.isOK(), "Meter should be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertFalse(meter.isFail(), "Meter should not be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals(4, logger.getEventCount(), "Should have exactly 4 log events");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_OK);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("runOrReject() no start with IOException to reject")
    @SuppressWarnings("unchecked")
    void testRunOrRejectNoStartWithIOExceptionToReject() {
        final Meter meter = new Meter(logger, "testRunOrRejectReject");
        final boolean[] executed = {false};
        
        try {
            meter.runOrReject(() -> {
                assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
                executed[0] = true;
                // Simulating IOException in a Runnable by wrapping in RuntimeException
                throw new RuntimeException(new IOException("test reject exception"));
            }, RuntimeException.class);
            fail("Should have thrown RuntimeException");
        } catch (final Exception e) {
            assertEquals(RuntimeException.class, e.getClass(), "Exception class should be RuntimeException");
            assertTrue(e.getCause() instanceof IOException, "Cause should be IOException");
            assertEquals("test reject exception", e.getCause().getMessage(), "Exception message should match");
        }
        
        assertTrue(executed[0], "Runnable should have been executed");
        assertFalse(meter.isOK(), "Meter should not be in OK state");
        assertTrue(meter.isReject(), "Meter should be in reject state");
        assertFalse(meter.isFail(), "Meter should not be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals("RuntimeException", meter.getRejectPath(), "Reject path should match exception class");
        assertEquals(4, logger.getEventCount(), "Should have exactly 4 log events");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_REJECT);
    }

    @Test
    @DisplayName("runOrReject() no start with exception to fail")
    @SuppressWarnings("unchecked")
    void testRunOrRejectNoStartWithExceptionToFail() {
        final Meter meter = new Meter(logger, "testRunOrRejectFail");
        final boolean[] executed = {false};
        
        try {
            meter.runOrReject(() -> {
                assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
                executed[0] = true;
                throw new IllegalArgumentException("test fail exception");
            }, IOException.class);
            fail("Should have thrown IllegalArgumentException");
        } catch (final Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass(), "Exception class should be IllegalArgumentException");
            assertEquals("test fail exception", e.getMessage(), "Exception message should match");
        }
        
        assertTrue(executed[0], "Runnable should have been executed");
        assertFalse(meter.isOK(), "Meter should not be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertTrue(meter.isFail(), "Meter should be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals("java.lang.IllegalArgumentException", meter.getFailPath(), "Fail path should match exception class");
        assertEquals("test fail exception", meter.getFailMessage(), "Fail message should match exception message");
        assertEquals(4, logger.getEventCount(), "Should have exactly 4 log events");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, ERROR, MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_FAIL);
    }

    @Test
    @DisplayName("runOrReject() with start with excessive start call")
    @SuppressWarnings("unchecked")
    void testRunOrRejectWithStartWithExcessiveStart() {
        final Meter meter = new Meter(logger, "testRunOrRejectExcessiveStart").start();
        final boolean[] executed = {false};
        
        try {
            meter.runOrReject(() -> {
                meter.start(); // Excessive call to start() - should generate error log
                assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
                executed[0] = true;
                meter.ok();
            }, IOException.class);
        } catch (final Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
        
        assertTrue(executed[0], "Runnable should have been executed");
        assertTrue(meter.isOK(), "Meter should be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertFalse(meter.isFail(), "Meter should not be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals(7, logger.getEventCount(), "Should have 7 log events (4 normal + 3 from excessive start)");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, ERROR, INCONSISTENT_START); // Error from excessive start() call
        AssertLogger.assertEvent(logger, 3, DEBUG, MSG_START); // Duplicate start message
        AssertLogger.assertEvent(logger, 4, TRACE, DATA_START); // Duplicate start data
        AssertLogger.assertEvent(logger, 5, INFO, MSG_OK);
        AssertLogger.assertEvent(logger, 6, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("runOrReject() with start with multiple exception classes to reject")
    @SuppressWarnings("unchecked")
    void testRunOrRejectWithStartWithMultipleExceptionClassesToReject() {
        final Meter meter = new Meter(logger, "testRunOrRejectMultiple").start();
        final boolean[] executed = {false};
        
        try {
            meter.runOrReject(() -> {
                assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
                executed[0] = true;
                throw new IllegalStateException("test multiple reject");
            }, IOException.class, IllegalStateException.class, RuntimeException.class);
            fail("Should have thrown IllegalStateException");
        } catch (final Exception e) {
            assertEquals(IllegalStateException.class, e.getClass(), "Exception class should be IllegalStateException");
            assertEquals("test multiple reject", e.getMessage(), "Exception message should match");
        }
        
        assertTrue(executed[0], "Runnable should have been executed");
        assertFalse(meter.isOK(), "Meter should not be in OK state");
        assertTrue(meter.isReject(), "Meter should be in reject state");
        assertFalse(meter.isFail(), "Meter should not be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals("IllegalStateException", meter.getRejectPath(), "Reject path should match exception class");
        assertEquals(4, logger.getEventCount(), "Should have exactly 4 log events");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_REJECT);
    }

    @Test
    @DisplayName("safeCall() with start with ok")
    void testSafeCallWithStartWithOk() {
        final Meter meter = new Meter(logger, "testSafeCallWithStartAndOk").start();
        final boolean[] executed = {false};
        
        final Object result = meter.safeCall(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
                executed[0] = true;
                meter.ok();
                return null;
            }
        });
        
        assertTrue(executed[0], "Callable should have been executed");
        assertNull(result, "Result should be null");
        assertTrue(meter.isOK(), "Meter should be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertFalse(meter.isFail(), "Meter should not be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_OK);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("safeCall() with start with ok and return")
    void testSafeCallWithStartWithOkAndReturn() {
        final Meter meter = new Meter(logger, "testSafeCallWithReturn").start();
        final boolean[] executed = {false};
        
        final Object result = meter.safeCall(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
                executed[0] = true;
                meter.ok();
                return 1000;
            }
        });
        
        assertTrue(executed[0], "Callable should have been executed");
        assertEquals(Integer.valueOf(1000), result);
        assertTrue(meter.isOK());
        assertFalse(meter.isReject());
        assertFalse(meter.isFail());
        assertFalse(meter.isSlow());
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_OK);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("safeCall() with start with excessive start call and return")
    void testSafeCallWithStartWithExcessiveStartAndReturn() {
        final Meter meter = new Meter(logger, "testSafeCallWithExcessiveStart").start();
        final boolean[] executed = {false};
        
        final Object result = meter.safeCall(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                meter.start(); // Excessive call to start() - should generate error log
                assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
                executed[0] = true;
                meter.ok();
                return 1000;
            }
        });
        
        assertTrue(executed[0], "Callable should have been executed");
        assertEquals(Integer.valueOf(1000), result, "Result should match expected value");
        assertTrue(meter.isOK(), "Meter should be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertFalse(meter.isFail(), "Meter should not be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals(7, logger.getEventCount(), "Should have 7 log events (4 normal + 3 from excessive start)");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, ERROR, INCONSISTENT_START); // Error from excessive start() call
        AssertLogger.assertEvent(logger, 3, DEBUG, MSG_START); // Duplicate start message
        AssertLogger.assertEvent(logger, 4, TRACE, DATA_START); // Duplicate start data
        AssertLogger.assertEvent(logger, 5, INFO, MSG_OK);
        AssertLogger.assertEvent(logger, 6, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("safeCall() no start no ok and return")
    void testSafeCallNoStartNoOkAndReturn() {
        final Meter meter = new Meter(logger, "testSafeCallAutoOk");
        final boolean[] executed = {false};
        
        final Object result = meter.safeCall(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
                executed[0] = true;
                return 1000;
            }
        });
        
        assertTrue(executed[0], "Callable should have been executed");
        assertEquals(Integer.valueOf(1000), result, "Result should match expected value");
        assertTrue(meter.isOK(), "Meter should be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertFalse(meter.isFail(), "Meter should not be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_OK, "result=1000");
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_OK, "result:1000");
    }

    @Test
    @DisplayName("safeCall() no start with reject")
    void testSafeCallNoStartWithReject() {
        final Meter meter = new Meter(logger, "testSafeCallReject");
        final boolean[] executed = {false};
        
        final Object result = meter.safeCall(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
                executed[0] = true;
                meter.reject("test rejection");
                return null;
            }
        });
        
        assertTrue(executed[0], "Callable should have been executed");
        assertNull(result, "Result should be null");
        assertFalse(meter.isOK(), "Meter should not be in OK state");
        assertTrue(meter.isReject(), "Meter should be in reject state");
        assertFalse(meter.isFail(), "Meter should not be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_REJECT);
    }

    @Test
    @DisplayName("safeCall() no start with fail")
    void testSafeCallNoStartWithFail() {
        final Meter meter = new Meter(logger, "testSafeCallFail");
        final boolean[] executed = {false};
        
        final Object result = meter.safeCall(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
                executed[0] = true;
                meter.fail("test failure");
                return null;
            }
        });
        
        assertTrue(executed[0], "Callable should have been executed");
        assertNull(result, "Result should be null");
        assertFalse(meter.isOK(), "Meter should not be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertTrue(meter.isFail(), "Meter should be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, ERROR, MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_FAIL);
    }

    @Test
    @DisplayName("safeCall() no start with exception")
    void testSafeCallNoStartWithException() {
        final Meter meter = new Meter(logger, "testSafeCallRuntimeException");
        final boolean[] executed = {false};
        
        try {
            meter.safeCall(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
                    executed[0] = true;
                    throw new IllegalArgumentException("test runtime exception");
                }
            });
        } catch (final RuntimeException e) {
            assertSame(IllegalArgumentException.class, e.getClass(), "Exception class should be IllegalArgumentException");
            assertEquals("test runtime exception", e.getMessage(), "Exception message should match");
        }
        
        assertTrue(executed[0], "Callable should have been executed");
        assertFalse(meter.isOK(), "Meter should not be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertTrue(meter.isFail(), "Meter should be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, ERROR, MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_FAIL);
    }

    @Test
    @DisplayName("safeCall() no start with checked exception")
    void testSafeCallNoStartWithCheckedException() {
        final Meter meter = new Meter(logger, "testSafeCallCheckedException");
        final boolean[] executed = {false};
        
        try {
            meter.safeCall(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
                    executed[0] = true;
                    throw new IOException("test checked exception");
                }
            });
        } catch (final RuntimeException e) {
            assertEquals("MeterExecutor.safeCall wrapped exception.", e.getMessage(), "Wrapper exception message should match");
            assertSame(RuntimeException.class, e.getClass(), "Wrapper exception class should be RuntimeException");
            assertSame(IOException.class, e.getCause().getClass(), "Cause exception class should be IOException");
            assertEquals("test checked exception", e.getCause().getMessage(), "Cause exception message should match");
        }
        
        assertTrue(executed[0], "Callable should have been executed");
        assertFalse(meter.isOK(), "Meter should not be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertTrue(meter.isFail(), "Meter should be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, ERROR, MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_FAIL);
    }

    @Test
    @DisplayName("call() no start no ok and return")
    void testCallNoStartNoOkAndReturn() throws Exception {
        final Meter meter = new Meter(logger, "testCall");
        final boolean[] executed = {false};
        
        // Test successful execution with result
        final String expectedResult = "Hello, World!";
        final String result = meter.call(() -> {
            assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
            executed[0] = true;
            return expectedResult;
        });
        
        assertTrue(executed[0], "Callable should have been executed");
        assertEquals(expectedResult, result, "Callable result should match expected value");
        assertTrue(meter.isOK(), "Meter should be in successful state");
        assertFalse(meter.isReject(), "Meter should not be in rejected state");
        assertFalse(meter.isFail(), "Meter should not be in failed state");
    }

    @Test
    @DisplayName("call() with start with excessive start call")
    void testCallWithStartWithExcessiveStart() throws Exception {
        final Meter meter = new Meter(logger, "testCallExcessiveStart").start();
        final boolean[] executed = {false};
        
        final String expectedResult = "Result with excessive start";
        final String result = meter.call(() -> {
            meter.start(); // Excessive call to start() - should generate error log
            assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
            executed[0] = true;
            meter.ok();
            return expectedResult;
        });
        
        assertTrue(executed[0], "Callable should have been executed");
        assertEquals(expectedResult, result, "Callable result should match expected value");
        assertTrue(meter.isOK(), "Meter should be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertFalse(meter.isFail(), "Meter should not be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals(7, logger.getEventCount(), "Should have 7 log events (4 normal + 3 from excessive start)");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, ERROR, INCONSISTENT_START); // Error from excessive start() call
        AssertLogger.assertEvent(logger, 3, DEBUG, MSG_START); // Duplicate start message
        AssertLogger.assertEvent(logger, 4, TRACE, DATA_START); // Duplicate start data
        AssertLogger.assertEvent(logger, 5, INFO, MSG_OK);
        AssertLogger.assertEvent(logger, 6, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("call() no start with exception to fail")
    void testCallNoStartWithExceptionToFail() {
        final Meter meter = new Meter(logger, "testCallException");
        final boolean[] executed = {false};
        
        try {
            meter.call(() -> {
                assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
                executed[0] = true;
                throw new IOException("test exception");
            });
            fail("Should have thrown IOException");
        } catch (final Exception e) {
            assertEquals(IOException.class, e.getClass(), "Exception class should be IOException");
            assertEquals("test exception", e.getMessage(), "Exception message should match");
        }
        
        assertTrue(executed[0], "Callable should have been executed");
        assertFalse(meter.isOK(), "Meter should not be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertTrue(meter.isFail(), "Meter should be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals("java.io.IOException", meter.getFailPath(), "Fail path should match exception class");
        assertEquals("test exception", meter.getFailMessage(), "Fail message should match exception message");
        assertEquals(4, logger.getEventCount(), "Should have exactly 4 log events");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, ERROR, MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_FAIL);
    }

    @Test
    @DisplayName("callOrReject() no start with ok and return")
    @SuppressWarnings("unchecked")
    void testCallOrRejectNoStartWithOkAndReturn() throws Exception {
        final Meter meter = new Meter(logger, "testCallOrReject");
        final boolean[] executed = {false};
        
        final String expectedResult = "Success";
        final String result = meter.callOrReject(() -> {
            assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
            executed[0] = true;
            meter.ok();
            return expectedResult;
        });
        
        assertTrue(executed[0], "Callable should have been executed");
        assertEquals(expectedResult, result, "Result should match expected value");
        assertTrue(meter.isOK(), "Meter should be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertFalse(meter.isFail(), "Meter should not be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals(4, logger.getEventCount(), "Should have exactly 4 log events");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_OK);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("callOrReject() no start no ok and return")
    @SuppressWarnings("unchecked")
    void testCallOrRejectNoStartNoOkAndReturn() throws Exception {
        final Meter meter = new Meter(logger, "testCallOrRejectAutoOk");
        final boolean[] executed = {false};
        
        final String expectedResult = "Success";
        final String result = meter.callOrReject(() -> {
            assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
            executed[0] = true;
            return expectedResult;
        });
        
        assertTrue(executed[0], "Callable should have been executed");
        assertEquals(expectedResult, result, "Result should match expected value");
        assertTrue(meter.isOK(), "Meter should be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertFalse(meter.isFail(), "Meter should not be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals(4, logger.getEventCount(), "Should have exactly 4 log events");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_OK, "result=Success");
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_OK, "result:Success");
    }

    @Test
    @DisplayName("callOrReject() no start with reject via exception")
    @SuppressWarnings("unchecked")
    void testCallOrRejectNoStartWithRejectViaException() throws Exception {
        final Meter meter = new Meter(logger, "testCallOrRejectWithReject");
        final boolean[] executed = {false};
        
        try {
            meter.callOrReject(() -> {
                assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
                executed[0] = true;
                throw new IllegalArgumentException("test rejection");
            }, IllegalArgumentException.class);
        } catch (final IllegalArgumentException e) {
            assertEquals("test rejection", e.getMessage(), "Exception message should match");
        }
        
        assertTrue(executed[0], "Callable should have been executed");
        assertFalse(meter.isOK(), "Meter should not be in OK state");
        assertTrue(meter.isReject(), "Meter should be in reject state");
        assertFalse(meter.isFail(), "Meter should not be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals("IllegalArgumentException", meter.getRejectPath(), "Reject path should match exception class");
        assertEquals(4, logger.getEventCount(), "Should have exactly 4 log events");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_REJECT);
    }

    @Test
    @DisplayName("callOrReject() no start with exception to fail")
    @SuppressWarnings("unchecked")
    void testCallOrRejectNoStartWithExceptionToFail() throws Exception {
        final Meter meter = new Meter(logger, "testCallOrRejectException");
        final boolean[] executed = {false};
        
        try {
            meter.callOrReject(() -> {
                assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
                executed[0] = true;
                throw new IllegalArgumentException("test exception");
            });
        } catch (final IllegalArgumentException e) {
            assertEquals("test exception", e.getMessage(), "Exception message should match");
        }
        
        assertTrue(executed[0], "Callable should have been executed");
        assertFalse(meter.isOK(), "Meter should not be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertTrue(meter.isFail(), "Meter should be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals("java.lang.IllegalArgumentException", meter.getFailPath(), "Fail path should match exception class");
        assertEquals("test exception", meter.getFailMessage(), "Fail message should match exception message");
        assertEquals(4, logger.getEventCount(), "Should have exactly 4 log events");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, ERROR, MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_FAIL);
    }

    @Test
    @DisplayName("callOrReject() with start with excessive start call")
    @SuppressWarnings("unchecked")
    void testCallOrRejectWithStartWithExcessiveStart() throws Exception {
        final Meter meter = new Meter(logger, "testCallOrRejectExcessiveStart").start();
        final boolean[] executed = {false};
        
        final String expectedResult = "Excessive start result";
        final String result = meter.callOrReject(() -> {
            meter.start(); // Excessive call to start() - should generate error log
            assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
            executed[0] = true;
            meter.ok();
            return expectedResult;
        });
        
        assertTrue(executed[0], "Callable should have been executed");
        assertEquals(expectedResult, result, "Result should match expected value");
        assertTrue(meter.isOK(), "Meter should be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertFalse(meter.isFail(), "Meter should not be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals(7, logger.getEventCount(), "Should have 7 log events (4 normal + 3 from excessive start)");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, ERROR, INCONSISTENT_START); // Error from excessive start() call
        AssertLogger.assertEvent(logger, 3, DEBUG, MSG_START); // Duplicate start message
        AssertLogger.assertEvent(logger, 4, TRACE, DATA_START); // Duplicate start data
        AssertLogger.assertEvent(logger, 5, INFO, MSG_OK);
        AssertLogger.assertEvent(logger, 6, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("callOrReject() with start with ok and return")
    @SuppressWarnings("unchecked")
    void testCallOrRejectWithStartWithOkAndReturn() throws Exception {
        final Meter meter = new Meter(logger, "testCallOrRejectWithStart").start();
        final boolean[] executed = {false};
        
        final String expectedResult = "Success";
        final String result = meter.callOrReject(() -> {
            assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
            executed[0] = true;
            meter.ok();
            return expectedResult;
        });
        
        assertTrue(executed[0], "Callable should have been executed");
        assertEquals(expectedResult, result, "Result should match expected value");
        assertTrue(meter.isOK(), "Meter should be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertFalse(meter.isFail(), "Meter should not be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals(4, logger.getEventCount(), "Should have exactly 4 log events");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_OK);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("callOrRejectChecked() with start with excessive start call")
    void testCallOrRejectCheckedWithStartWithExcessiveStart() throws Exception {
        final Meter meter = new Meter(logger, "testCallOrRejectCheckedExcessiveStart").start();
        final boolean[] executed = {false};
        
        final String expectedResult = "Checked excessive start result";
        final String result = meter.callOrRejectChecked(() -> {
            meter.start(); // Excessive call to start() - should generate error log
            assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
            executed[0] = true;
            meter.ok();
            return expectedResult;
        });
        
        assertTrue(executed[0], "Callable should have been executed");
        assertEquals(expectedResult, result, "Result should match expected value");
        assertTrue(meter.isOK(), "Meter should be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertFalse(meter.isFail(), "Meter should not be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals(7, logger.getEventCount(), "Should have 7 log events (4 normal + 3 from excessive start)");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, ERROR, INCONSISTENT_START); // Error from excessive start() call
        AssertLogger.assertEvent(logger, 3, DEBUG, MSG_START); // Duplicate start message
        AssertLogger.assertEvent(logger, 4, TRACE, DATA_START); // Duplicate start data
        AssertLogger.assertEvent(logger, 5, INFO, MSG_OK);
        AssertLogger.assertEvent(logger, 6, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("callOrRejectChecked() no start with ok and return")
    void testCallOrRejectCheckedNoStartWithOkAndReturn() throws Exception {
        final Meter meter = new Meter(logger, "testCallOrRejectChecked");
        final boolean[] executed = {false};
        
        final String expectedResult = "Checked Success";
        final String result = meter.callOrRejectChecked(() -> {
            assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
            executed[0] = true;
            meter.ok();
            return expectedResult;
        });
        
        assertTrue(executed[0], "Callable should have been executed");
        assertEquals(expectedResult, result, "Result should match expected value");
        assertTrue(meter.isOK(), "Meter should be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertFalse(meter.isFail(), "Meter should not be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals(4, logger.getEventCount(), "Should have exactly 4 log events");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_OK);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("callOrRejectChecked() no start no ok and return")
    void testCallOrRejectCheckedNoStartNoOkAndReturn() throws Exception {
        final Meter meter = new Meter(logger, "testCallOrRejectCheckedAutoOk");
        final boolean[] executed = {false};
        
        final String expectedResult = "Auto OK";
        final String result = meter.callOrRejectChecked(() -> {
            assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
            executed[0] = true;
            return expectedResult;
        });
        
        assertTrue(executed[0], "Callable should have been executed");
        assertEquals(expectedResult, result, "Result should match expected value");
        assertTrue(meter.isOK(), "Meter should be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertFalse(meter.isFail(), "Meter should not be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals(4, logger.getEventCount(), "Should have exactly 4 log events");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_OK, "result=Auto OK");
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_OK, "result:Auto OK");
    }

    @Test
    @DisplayName("callOrRejectChecked() no start with reject via exception")
    void testCallOrRejectCheckedNoStartWithRejectViaException() {
        final Meter meter = new Meter(logger, "testCallOrRejectCheckedReject");
        final boolean[] executed = {false};
        
        try {
            meter.callOrRejectChecked(() -> {
                assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
                executed[0] = true;
                throw new IOException("test rejection");
            });
            fail("Should have thrown IOException");
        } catch (final Exception e) {
            assertEquals(IOException.class, e.getClass(), "Exception class should be IOException");
            assertEquals("test rejection", e.getMessage(), "Exception message should match");
        }
        
        assertTrue(executed[0], "Callable should have been executed");
        assertFalse(meter.isOK(), "Meter should not be in OK state");
        assertTrue(meter.isReject(), "Meter should be in reject state");
        assertFalse(meter.isFail(), "Meter should not be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals("IOException", meter.getRejectPath(), "Reject path should match exception class");
        assertEquals(4, logger.getEventCount(), "Should have exactly 4 log events");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_REJECT);
    }

    @Test
    @DisplayName("callOrRejectChecked() no start with exception to reject")
    void testCallOrRejectCheckedNoStartWithExceptionToReject() {
        final Meter meter = new Meter(logger, "testCallOrRejectCheckedException");
        final boolean[] executed = {false};
        
        try {
            meter.callOrRejectChecked(() -> {
                assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
                executed[0] = true;
                throw new IOException("test exception");
            });
            fail("Should have thrown IOException");
        } catch (final Exception e) {
            assertEquals(IOException.class, e.getClass(), "Exception class should be IOException");
            assertEquals("test exception", e.getMessage(), "Exception message should match");
        }
        
        assertTrue(executed[0], "Callable should have been executed");
        assertFalse(meter.isOK(), "Meter should not be in OK state");
        assertTrue(meter.isReject(), "Meter should be in reject state");
        assertFalse(meter.isFail(), "Meter should not be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals("IOException", meter.getRejectPath(), "Reject path should match exception class");
        assertEquals(4, logger.getEventCount(), "Should have exactly 4 log events");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_REJECT);
    }

    @Test
    @DisplayName("callOrRejectChecked() no start with RuntimeException to fail")
    void testCallOrRejectCheckedNoStartWithRuntimeExceptionToFail() {
        final Meter meter = new Meter(logger, "testCallOrRejectCheckedRuntimeException");
        final boolean[] executed = {false};
        
        try {
            meter.callOrRejectChecked(() -> {
                assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
                executed[0] = true;
                throw new IllegalArgumentException("test runtime exception");
            });
            fail("Should have thrown IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            assertEquals("test runtime exception", e.getMessage(), "Exception message should match");
        }
        
        assertTrue(executed[0], "Callable should have been executed");
        assertFalse(meter.isOK(), "Meter should not be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertTrue(meter.isFail(), "Meter should be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals("java.lang.IllegalArgumentException", meter.getFailPath(), "Fail path should match exception class");
        assertEquals("test runtime exception", meter.getFailMessage(), "Fail message should match exception message");
        assertEquals(4, logger.getEventCount(), "Should have exactly 4 log events");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, ERROR, MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_FAIL);
    }

    @Test
    @DisplayName("safeCall() with exception class with start with excessive start call")
    void testSafeCallWithExceptionClassWithStartWithExcessiveStart() {
        final Meter meter = new Meter(logger, "testSafeCallWithExceptionClassExcessiveStart").start();
        final boolean[] executed = {false};
        
        final String expectedResult = "Safe excessive start result";
        final String result = meter.safeCall(RuntimeException.class, () -> {
            meter.start(); // Excessive call to start() - should generate error log
            assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
            executed[0] = true;
            meter.ok();
            return expectedResult;
        });
        
        assertTrue(executed[0], "Callable should have been executed");
        assertEquals(expectedResult, result, "Result should match expected value");
        assertTrue(meter.isOK(), "Meter should be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertFalse(meter.isFail(), "Meter should not be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals(7, logger.getEventCount(), "Should have 7 log events (4 normal + 3 from excessive start)");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, ERROR, INCONSISTENT_START); // Error from excessive start() call
        AssertLogger.assertEvent(logger, 3, DEBUG, MSG_START); // Duplicate start message
        AssertLogger.assertEvent(logger, 4, TRACE, DATA_START); // Duplicate start data
        AssertLogger.assertEvent(logger, 5, INFO, MSG_OK);
        AssertLogger.assertEvent(logger, 6, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("safeCall() with exception class no start with ok and return")
    void testSafeCallWithExceptionClassNoStartWithOkAndReturn() {
        final Meter meter = new Meter(logger, "testSafeCallWithExceptionClass");
        final boolean[] executed = {false};
        
        final String expectedResult = "Safe Success";
        final String result = meter.safeCall(RuntimeException.class, () -> {
            assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
            executed[0] = true;
            meter.ok();
            return expectedResult;
        });
        
        assertTrue(executed[0], "Callable should have been executed");
        assertEquals(expectedResult, result, "Result should match expected value");
        assertTrue(meter.isOK(), "Meter should be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertFalse(meter.isFail(), "Meter should not be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals(4, logger.getEventCount(), "Should have exactly 4 log events");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_OK);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("safeCall() with exception class no start no ok and return")
    void testSafeCallWithExceptionClassNoStartNoOkAndReturn() {
        final Meter meter = new Meter(logger, "testSafeCallWithExceptionClassAutoOk");
        final boolean[] executed = {false};
        
        final String expectedResult = "Auto Safe Success";
        final String result = meter.safeCall(RuntimeException.class, () -> {
            assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
            executed[0] = true;
            return expectedResult;
        });
        
        assertTrue(executed[0], "Callable should have been executed");
        assertEquals(expectedResult, result, "Result should match expected value");
        assertTrue(meter.isOK(), "Meter should be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertFalse(meter.isFail(), "Meter should not be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals(4, logger.getEventCount(), "Should have exactly 4 log events");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, INFO, MSG_OK, "result=Auto Safe Success");
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_OK, "result:Auto Safe Success");
    }

    @Test
    @DisplayName("safeCall() with exception class no start with exception wrapped")
    void testSafeCallWithExceptionClassNoStartWithExceptionWrapped() {
        final Meter meter = new Meter(logger, "testSafeCallWithExceptionClassWrapped");
        final boolean[] executed = {false};
        
        try {
            meter.safeCall(RuntimeException.class, () -> {
                assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
                executed[0] = true;
                throw new IOException("test exception");
            });
            fail("Should have thrown RuntimeException");
        } catch (final RuntimeException e) {
            assertEquals(RuntimeException.class, e.getClass(), "Exception class should be RuntimeException");
            assertEquals(IOException.class, e.getCause().getClass(), "Cause should be IOException");
            assertEquals("test exception", e.getCause().getMessage(), "Cause message should match");
        }
        
        assertTrue(executed[0], "Callable should have been executed");
        assertFalse(meter.isOK(), "Meter should not be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertTrue(meter.isFail(), "Meter should be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals("java.io.IOException", meter.getFailPath(), "Fail path should match original exception class");
        assertEquals("test exception", meter.getFailMessage(), "Fail message should match original exception message");
        assertEquals(4, logger.getEventCount(), "Should have exactly 4 log events");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, ERROR, MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_FAIL);
    }

    @Test
    @DisplayName("safeCall() with exception class no start with exception to fail")
    void testSafeCallWithExceptionClassNoStartWithExceptionToFail() {
        final Meter meter = new Meter(logger, "testSafeCallWithExceptionClassFail");
        final boolean[] executed = {false};
        
        try {
            meter.safeCall(RuntimeException.class, () -> {
                assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
                executed[0] = true;
                throw new IllegalArgumentException("test exception");
            });
        } catch (final RuntimeException e) {
            assertSame(IllegalArgumentException.class, e.getClass(), "Exception class should be IllegalArgumentException");
            assertEquals("test exception", e.getMessage(), "Exception message should match");
        }
        
        assertTrue(executed[0], "Callable should have been executed");
        assertFalse(meter.isOK(), "Meter should not be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertTrue(meter.isFail(), "Meter should be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals("java.lang.IllegalArgumentException", meter.getFailPath(), "Fail path should match exception class");
        assertEquals("test exception", meter.getFailMessage(), "Fail message should match exception message");
        assertEquals(4, logger.getEventCount(), "Should have exactly 4 log events");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, ERROR, MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_FAIL);
    }

    @Test
    @DisplayName("safeCall() with invalid exception class falls back to RuntimeException")
    void testSafeCallWithInvalidExceptionClassFallbackToRuntimeException() {
        final Meter meter = new Meter(logger, "testSafeCallInvalidException");
        final boolean[] executed = {false};
        
        // Creating a custom RuntimeException that doesn't have the expected constructor
        class CustomRuntimeException extends RuntimeException {
            // Intentionally no constructor with (String, Throwable) signature
            // Only has default constructor
            public CustomRuntimeException() {
                super("Custom exception without proper constructor");
            }
            
            // Adding a different constructor to make sure it doesn't match
            public CustomRuntimeException(String message) {
                super(message);
            }
        }
        
        try {
            meter.safeCall(CustomRuntimeException.class, () -> {
                assertEquals(meter, Meter.getCurrentInstance(), "Current instance should be the same meter");
                executed[0] = true;
                throw new IOException("test checked exception");
            });
            fail("Should have thrown RuntimeException");
        } catch (final RuntimeException e) {
            // Should fall back to plain RuntimeException when CustomRuntimeException can't be instantiated
            assertEquals(RuntimeException.class, e.getClass(), "Exception class should be RuntimeException (fallback)");
            assertEquals(IOException.class, e.getCause().getClass(), "Cause should be IOException");
            assertEquals("test checked exception", e.getCause().getMessage(), "Cause message should match");
        }
        
        assertTrue(executed[0], "Callable should have been executed");
        assertFalse(meter.isOK(), "Meter should not be in OK state");
        assertFalse(meter.isReject(), "Meter should not be in reject state");
        assertTrue(meter.isFail(), "Meter should be in fail state");
        assertFalse(meter.isSlow(), "Meter should not be in slow state");
        assertEquals("java.io.IOException", meter.getFailPath(), "Fail path should match original exception class");
        assertEquals("test checked exception", meter.getFailMessage(), "Fail message should match original exception message");
        
        // Should have 5 log events: 4 normal + 1 error from convertException fallback
        assertEquals(5, logger.getEventCount(), "Should have exactly 5 log events (4 normal + 1 from convertException)");
        AssertLogger.assertEvent(logger, 0, DEBUG, MSG_START);
        AssertLogger.assertEvent(logger, 1, TRACE, DATA_START);
        AssertLogger.assertEvent(logger, 2, ERROR, MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, TRACE, DATA_FAIL);
        AssertLogger.assertEvent(logger, 4, ERROR, INCONSISTENT_EXCEPTION); // Error from convertException fallback
    }
}