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
package org.usefultoys.slf4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MarkerFactory;
import org.usefultoys.test.ValidateCharset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Unit tests for {@link NullLogger}.
 * <p>
 * Tests validate that NullLogger silently ignores all logging operations
 * without throwing exceptions, providing a no-op Logger implementation.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Logger Name:</b> Verifies getName() returns without exceptions</li>
 *   <li><b>Level Enabled Checks:</b> Confirms all logging levels (trace, debug, info, warn, error) report as not enabled</li>
 *   <li><b>Logging Methods:</b> Tests all logging methods (with and without markers, various argument counts) execute without exceptions</li>
 * </ul>
 */
@ValidateCharset
class NullLoggerTest {

    private final NullLogger nullLogger = NullLogger.INSTANCE;

    @Test
    @DisplayName("should return name without throwing exceptions")
    void shouldReturnNameWithoutThrowingExceptions() {
        // Given: NullLogger instance
        // When: getName() is called
        // Then: should not throw exception
        assertDoesNotThrow(() -> nullLogger.getName());
    }

    @Test
    @DisplayName("should report trace is not enabled")
    void shouldReportTraceIsNotEnabled() {
        // Given: NullLogger instance
        // When: isTraceEnabled is checked
        // Then: should return false
        assertFalse(nullLogger.isTraceEnabled(), "should report trace not enabled");
        assertFalse(nullLogger.isTraceEnabled(MarkerFactory.getMarker("TEST")), "should report trace not enabled with marker");
    }

    @Test
    @DisplayName("should execute all trace methods without throwing exceptions")
    void shouldExecuteAllTraceMethodsWithoutThrowingExceptions() {
        // Given: NullLogger instance
        // When: various trace methods are called
        assertDoesNotThrow(() -> nullLogger.trace("message"));
        assertDoesNotThrow(() -> nullLogger.trace("format {}", "arg"));
        assertDoesNotThrow(() -> nullLogger.trace("format {} {}", "arg1", "arg2"));
        assertDoesNotThrow(() -> nullLogger.trace("format {}", new Object[]{"arg"}));
        assertDoesNotThrow(() -> nullLogger.trace("message", new RuntimeException()));
        assertDoesNotThrow(() -> nullLogger.trace(MarkerFactory.getMarker("TEST"), "message"));
        assertDoesNotThrow(() -> nullLogger.trace(MarkerFactory.getMarker("TEST"), "format {}", "arg"));
        assertDoesNotThrow(() -> nullLogger.trace(MarkerFactory.getMarker("TEST"), "format {} {}", "arg1", "arg2"));
        assertDoesNotThrow(() -> nullLogger.trace(MarkerFactory.getMarker("TEST"), "format {}", new Object[]{"arg"}));
        // Then: should not throw exceptions
        assertDoesNotThrow(() -> nullLogger.trace(MarkerFactory.getMarker("TEST"), "message", new RuntimeException()));
    }

    @Test
    @DisplayName("should report debug is not enabled")
    void shouldReportDebugIsNotEnabled() {
        // Given: NullLogger instance
        // When: isDebugEnabled is checked
        // Then: should return false
        assertFalse(nullLogger.isDebugEnabled(), "should report debug not enabled");
        assertFalse(nullLogger.isDebugEnabled(MarkerFactory.getMarker("TEST")), "should report debug not enabled with marker");
    }

    @Test
    @DisplayName("should execute all debug methods without throwing exceptions")
    void shouldExecuteAllDebugMethodsWithoutThrowingExceptions() {
        // Given: NullLogger instance
        // When: various debug methods are called
        assertDoesNotThrow(() -> nullLogger.debug("message"));
        assertDoesNotThrow(() -> nullLogger.debug("format {}", "arg"));
        assertDoesNotThrow(() -> nullLogger.debug("format {} {}", "arg1", "arg2"));
        assertDoesNotThrow(() -> nullLogger.debug("format {}", new Object[]{"arg"}));
        assertDoesNotThrow(() -> nullLogger.debug("message", new RuntimeException()));
        assertDoesNotThrow(() -> nullLogger.debug(MarkerFactory.getMarker("TEST"), "message"));
        assertDoesNotThrow(() -> nullLogger.debug(MarkerFactory.getMarker("TEST"), "format {}", "arg"));
        assertDoesNotThrow(() -> nullLogger.debug(MarkerFactory.getMarker("TEST"), "format {} {}", "arg1", "arg2"));
        assertDoesNotThrow(() -> nullLogger.debug(MarkerFactory.getMarker("TEST"), "format {}", new Object[]{"arg"}));
        // Then: should not throw exceptions
        assertDoesNotThrow(() -> nullLogger.debug(MarkerFactory.getMarker("TEST"), "message", new RuntimeException()));
    }

    @Test
    @DisplayName("should report info is not enabled")
    void shouldReportInfoIsNotEnabled() {
        // Given: NullLogger instance
        // When: isInfoEnabled is checked
        // Then: should return false
        assertFalse(nullLogger.isInfoEnabled(), "should report info not enabled");
        assertFalse(nullLogger.isInfoEnabled(MarkerFactory.getMarker("TEST")), "should report info not enabled with marker");
    }

    @Test
    @DisplayName("should execute all info methods without throwing exceptions")
    void shouldExecuteAllInfoMethodsWithoutThrowingExceptions() {
        // Given: NullLogger instance
        // When: various info methods are called
        assertDoesNotThrow(() -> nullLogger.info("message"));
        assertDoesNotThrow(() -> nullLogger.info("format {}", "arg"));
        assertDoesNotThrow(() -> nullLogger.info("format {} {}", "arg1", "arg2"));
        assertDoesNotThrow(() -> nullLogger.info("format {}", new Object[]{"arg"}));
        assertDoesNotThrow(() -> nullLogger.info("message", new RuntimeException()));
        assertDoesNotThrow(() -> nullLogger.info(MarkerFactory.getMarker("TEST"), "message"));
        assertDoesNotThrow(() -> nullLogger.info(MarkerFactory.getMarker("TEST"), "format {}", "arg"));
        assertDoesNotThrow(() -> nullLogger.info(MarkerFactory.getMarker("TEST"), "format {} {}", "arg1", "arg2"));
        assertDoesNotThrow(() -> nullLogger.info(MarkerFactory.getMarker("TEST"), "format {}", new Object[]{"arg"}));
        // Then: should not throw exceptions
        assertDoesNotThrow(() -> nullLogger.info(MarkerFactory.getMarker("TEST"), "message", new RuntimeException()));
    }

    @Test
    @DisplayName("should report warn is not enabled")
    void shouldReportWarnIsNotEnabled() {
        // Given: NullLogger instance
        // When: isWarnEnabled is checked
        // Then: should return false
        assertFalse(nullLogger.isWarnEnabled(), "should report warn not enabled");
        assertFalse(nullLogger.isWarnEnabled(MarkerFactory.getMarker("TEST")), "should report warn not enabled with marker");
    }

    @Test
    @DisplayName("should execute all warn methods without throwing exceptions")
    void shouldExecuteAllWarnMethodsWithoutThrowingExceptions() {
        // Given: NullLogger instance
        // When: various warn methods are called
        assertDoesNotThrow(() -> nullLogger.warn("message"));
        assertDoesNotThrow(() -> nullLogger.warn("format {}", "arg"));
        assertDoesNotThrow(() -> nullLogger.warn("format {} {}", "arg1", "arg2"));
        assertDoesNotThrow(() -> nullLogger.warn("format {}", new Object[]{"arg"}));
        assertDoesNotThrow(() -> nullLogger.warn("message", new RuntimeException()));
        assertDoesNotThrow(() -> nullLogger.warn(MarkerFactory.getMarker("TEST"), "message"));
        assertDoesNotThrow(() -> nullLogger.warn(MarkerFactory.getMarker("TEST"), "format {}", "arg"));
        assertDoesNotThrow(() -> nullLogger.warn(MarkerFactory.getMarker("TEST"), "format {} {}", "arg1", "arg2"));
        assertDoesNotThrow(() -> nullLogger.warn(MarkerFactory.getMarker("TEST"), "format {}", new Object[]{"arg"}));
        // Then: should not throw exceptions
        assertDoesNotThrow(() -> nullLogger.warn(MarkerFactory.getMarker("TEST"), "message", new RuntimeException()));
    }

    @Test
    @DisplayName("should report error is not enabled")
    void shouldReportErrorIsNotEnabled() {
        // Given: NullLogger instance
        // When: isErrorEnabled is checked
        // Then: should return false
        assertFalse(nullLogger.isErrorEnabled(), "should report error not enabled");
        assertFalse(nullLogger.isErrorEnabled(MarkerFactory.getMarker("TEST")), "should report error not enabled with marker");
    }

    @Test
    @DisplayName("should execute all error methods without throwing exceptions")
    void shouldExecuteAllErrorMethodsWithoutThrowingExceptions() {
        // Given: NullLogger instance
        // When: various error methods are called
        assertDoesNotThrow(() -> nullLogger.error("message"));
        assertDoesNotThrow(() -> nullLogger.error("format {}", "arg"));
        assertDoesNotThrow(() -> nullLogger.error("format {} {}", "arg1", "arg2"));
        assertDoesNotThrow(() -> nullLogger.error("format {}", new Object[]{"arg"}));
        assertDoesNotThrow(() -> nullLogger.error("message", new RuntimeException()));
        assertDoesNotThrow(() -> nullLogger.error(MarkerFactory.getMarker("TEST"), "message"));
        assertDoesNotThrow(() -> nullLogger.error(MarkerFactory.getMarker("TEST"), "format {}", "arg"));
        assertDoesNotThrow(() -> nullLogger.error(MarkerFactory.getMarker("TEST"), "format {} {}", "arg1", "arg2"));
        assertDoesNotThrow(() -> nullLogger.error(MarkerFactory.getMarker("TEST"), "format {}", new Object[]{"arg"}));
        // Then: should not throw exceptions
        assertDoesNotThrow(() -> nullLogger.error(MarkerFactory.getMarker("TEST"), "message", new RuntimeException()));
    }
}
