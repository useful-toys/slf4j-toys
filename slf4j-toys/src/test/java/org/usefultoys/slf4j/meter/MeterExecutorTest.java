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
 */
@DisplayName("MeterExecutor interface functionality tests")
class MeterExecutorTest {

    static final MockLogger logger = (MockLogger) LoggerFactory.getLogger("test.executor");

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
    @DisplayName("Run Runnable with explicit start and ok calls")
    void runRunnableWithStartAndOk() {
        final Meter meter = new Meter(logger).start();
        final boolean[] executed = {false};
        
        meter.run(() -> {
            assertEquals(meter, Meter.getCurrentInstance());
            executed[0] = true;
            meter.ok();
        });
        
        assertTrue(executed[0], "Runnable should have been executed");
        assertTrue(meter.isOK());
        assertFalse(meter.isReject());
        assertFalse(meter.isFail());
        assertFalse(meter.isSlow());
        assertEquals(4, logger.getEventCount());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, INFO, MSG_OK);
        logger.assertEvent(3, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("Run Runnable without explicit start but with ok call")
    void runRunnableNoStartWithOk() {
        final Meter meter = new Meter(logger);
        final boolean[] executed = {false};
        
        meter.run(() -> {
            assertEquals(meter, Meter.getCurrentInstance());
            executed[0] = true;
            meter.ok();
        });
        
        assertTrue(executed[0], "Runnable should have been executed");
        assertTrue(meter.isOK());
        assertFalse(meter.isReject());
        assertFalse(meter.isFail());
        assertFalse(meter.isSlow());
        assertEquals(4, logger.getEventCount());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, INFO, MSG_OK);
        logger.assertEvent(3, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("Run Runnable without start and without ok - automatic ok")
    void runRunnableNoStartNoOk() {
        final Meter meter = new Meter(logger);
        final boolean[] executed = {false};
        
        meter.run(() -> {
            assertEquals(meter, Meter.getCurrentInstance());
            executed[0] = true;
        });
        
        assertTrue(executed[0], "Runnable should have been executed");
        assertTrue(meter.isOK());
        assertFalse(meter.isReject());
        assertFalse(meter.isFail());
        assertFalse(meter.isSlow());
        assertEquals(4, logger.getEventCount());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, INFO, MSG_OK);
        logger.assertEvent(3, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("Run Runnable with explicit reject")
    void runRunnableWithReject() {
        final Meter meter = new Meter(logger);
        final boolean[] executed = {false};
        
        meter.run(() -> {
            assertEquals(meter, Meter.getCurrentInstance());
            executed[0] = true;
            meter.reject("test rejection");
        });
        
        assertTrue(executed[0], "Runnable should have been executed");
        assertFalse(meter.isOK());
        assertTrue(meter.isReject());
        assertFalse(meter.isFail());
        assertFalse(meter.isSlow());
        assertEquals(4, logger.getEventCount());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, INFO, MSG_REJECT);
        logger.assertEvent(3, TRACE, DATA_REJECT);
    }

    @Test
    @DisplayName("Run Runnable with explicit fail")
    void runRunnableWithFail() {
        final Meter meter = new Meter(logger);
        final boolean[] executed = {false};
        
        meter.run(() -> {
            assertEquals(meter, Meter.getCurrentInstance());
            executed[0] = true;
            meter.fail("test failure");
        });
        
        assertTrue(executed[0], "Runnable should have been executed");
        assertFalse(meter.isOK());
        assertFalse(meter.isReject());
        assertTrue(meter.isFail());
        assertFalse(meter.isSlow());
        assertEquals(4, logger.getEventCount());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, ERROR, MSG_FAIL);
        logger.assertEvent(3, TRACE, DATA_FAIL);
    }

    @Test
    @DisplayName("Run Runnable with uncaught exception")
    void runRunnableWithException() {
        final Meter meter = new Meter(logger);
        final boolean[] executed = {false};
        
        try {
            meter.run(() -> {
                assertEquals(meter, Meter.getCurrentInstance());
                executed[0] = true;
                throw new IllegalArgumentException("test exception");
            });
        } catch (final IllegalArgumentException e) {
            assertEquals("test exception", e.getMessage());
        }
        
        assertTrue(executed[0], "Runnable should have been executed");
        assertFalse(meter.isOK());
        assertFalse(meter.isReject());
        assertTrue(meter.isFail());
        assertFalse(meter.isSlow());
        assertEquals("java.lang.IllegalArgumentException", meter.failPath);
        assertEquals("test exception", meter.failMessage);
        assertEquals(4, logger.getEventCount());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, ERROR, MSG_FAIL);
        logger.assertEvent(3, TRACE, DATA_FAIL);
    }

    @Test
    @DisplayName("SafeCall Callable with explicit start and ok")
    void safeCallCallableWithStartAndOk() {
        final Meter meter = new Meter(logger, "testSafeCallWithStartAndOk").start();
        final boolean[] executed = {false};
        
        final Object result = meter.safeCall(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                assertEquals(meter, Meter.getCurrentInstance());
                executed[0] = true;
                meter.ok();
                return null;
            }
        });
        
        assertTrue(executed[0], "Callable should have been executed");
        assertNull(result);
        assertTrue(meter.isOK());
        assertFalse(meter.isReject());
        assertFalse(meter.isFail());
        assertFalse(meter.isSlow());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, INFO, MSG_OK);
        logger.assertEvent(3, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("SafeCall Callable with return value and explicit ok")
    void safeCallCallableWithReturnValueAndOk() {
        final Meter meter = new Meter(logger, "testSafeCallWithReturn").start();
        final boolean[] executed = {false};
        
        final Object result = meter.safeCall(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                assertEquals(meter, Meter.getCurrentInstance());
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
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, INFO, MSG_OK);
        logger.assertEvent(3, TRACE, DATA_OK);
    }

    @Test
    @DisplayName("SafeCall Callable with return value and automatic ok")
    void safeCallCallableWithReturnValueNoOk() {
        final Meter meter = new Meter(logger, "testSafeCallAutoOk");
        final boolean[] executed = {false};
        
        final Object result = meter.safeCall(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                assertEquals(meter, Meter.getCurrentInstance());
                executed[0] = true;
                return 1000;
            }
        });
        
        assertTrue(executed[0], "Callable should have been executed");
        assertEquals(Integer.valueOf(1000), result);
        assertTrue(meter.isOK());
        assertFalse(meter.isReject());
        assertFalse(meter.isFail());
        assertFalse(meter.isSlow());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, INFO, MSG_OK, "result=1000");
        logger.assertEvent(3, TRACE, DATA_OK, "result:1000");
    }

    @Test
    @DisplayName("SafeCall Callable with reject")
    void safeCallCallableWithReject() {
        final Meter meter = new Meter(logger, "testSafeCallReject");
        final boolean[] executed = {false};
        
        final Object result = meter.safeCall(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                assertEquals(meter, Meter.getCurrentInstance());
                executed[0] = true;
                meter.reject("test rejection");
                return null;
            }
        });
        
        assertTrue(executed[0], "Callable should have been executed");
        assertNull(result);
        assertFalse(meter.isOK());
        assertTrue(meter.isReject());
        assertFalse(meter.isFail());
        assertFalse(meter.isSlow());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, INFO, MSG_REJECT);
        logger.assertEvent(3, TRACE, DATA_REJECT);
    }

    @Test
    @DisplayName("SafeCall Callable with fail")
    void safeCallCallableWithFail() {
        final Meter meter = new Meter(logger, "testSafeCallFail");
        final boolean[] executed = {false};
        
        final Object result = meter.safeCall(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                assertEquals(meter, Meter.getCurrentInstance());
                executed[0] = true;
                meter.fail("test failure");
                return null;
            }
        });
        
        assertTrue(executed[0], "Callable should have been executed");
        assertNull(result);
        assertFalse(meter.isOK());
        assertFalse(meter.isReject());
        assertTrue(meter.isFail());
        assertFalse(meter.isSlow());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, ERROR, MSG_FAIL);
        logger.assertEvent(3, TRACE, DATA_FAIL);
    }

    @Test
    @DisplayName("SafeCall Callable with RuntimeException")
    void safeCallCallableWithRuntimeException() {
        final Meter meter = new Meter(logger, "testSafeCallRuntimeException");
        final boolean[] executed = {false};
        
        try {
            meter.safeCall(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    assertEquals(meter, Meter.getCurrentInstance());
                    executed[0] = true;
                    throw new IllegalArgumentException("test runtime exception");
                }
            });
        } catch (final RuntimeException e) {
            assertSame(IllegalArgumentException.class, e.getClass());
            assertEquals("test runtime exception", e.getMessage());
        }
        
        assertTrue(executed[0], "Callable should have been executed");
        assertFalse(meter.isOK());
        assertFalse(meter.isReject());
        assertTrue(meter.isFail());
        assertFalse(meter.isSlow());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, ERROR, MSG_FAIL);
        logger.assertEvent(3, TRACE, DATA_FAIL);
    }

    @Test
    @DisplayName("SafeCall Callable with checked exception wrapping")
    void safeCallCallableWithCheckedException() {
        final Meter meter = new Meter(logger, "testSafeCallCheckedException");
        final boolean[] executed = {false};
        
        try {
            meter.safeCall(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    assertEquals(meter, Meter.getCurrentInstance());
                    executed[0] = true;
                    throw new IOException("test checked exception");
                }
            });
        } catch (final RuntimeException e) {
            assertEquals("MeterExecutor.safeCall wrapped exception.", e.getMessage());
            assertSame(RuntimeException.class, e.getClass());
            assertSame(IOException.class, e.getCause().getClass());
            assertEquals("test checked exception", e.getCause().getMessage());
        }
        
        assertTrue(executed[0], "Callable should have been executed");
        assertFalse(meter.isOK());
        assertFalse(meter.isReject());
        assertTrue(meter.isFail());
        assertFalse(meter.isSlow());
        logger.assertEvent(0, DEBUG, MSG_START);
        logger.assertEvent(1, TRACE, DATA_START);
        logger.assertEvent(2, ERROR, MSG_FAIL);
        logger.assertEvent(3, TRACE, DATA_FAIL);
    }

    @Test
    @DisplayName("Call Callable with automatic lifecycle management")
    void callCallableWithAutomaticLifecycle() throws Exception {
        final Meter meter = new Meter(logger, "testCall");
        final boolean[] executed = {false};
        
        // Test successful execution with result
        final String expectedResult = "Hello, World!";
        final String result = meter.call(() -> {
            assertEquals(meter, Meter.getCurrentInstance());
            executed[0] = true;
            return expectedResult;
        });
        
        assertTrue(executed[0], "Callable should have been executed");
        assertEquals(expectedResult, result, "Callable result should match expected value");
        assertTrue(meter.isOK(), "Meter should be in successful state");
        assertFalse(meter.isReject(), "Meter should not be in rejected state");
        assertFalse(meter.isFail(), "Meter should not be in failed state");
    }
}