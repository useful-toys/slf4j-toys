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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.MarkerFactory;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.usefultoys.slf4j.SessionConfig;


class NullLoggerTest {

    @BeforeAll
    static void validateConsistentCharset() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    private final NullLogger nullLogger = NullLogger.INSTANCE;

    @Test
    void testGetName() {
        assertDoesNotThrow(() -> nullLogger.getName());
    }

    @Test
    void testIsTraceEnabled() {
        assertFalse(nullLogger.isTraceEnabled());
        assertFalse(nullLogger.isTraceEnabled(MarkerFactory.getMarker("TEST")));
    }

    @Test
    void testTraceMethodsDoNotThrowExceptions() {
        assertDoesNotThrow(() -> nullLogger.trace("message"));
        assertDoesNotThrow(() -> nullLogger.trace("format {}", "arg"));
        assertDoesNotThrow(() -> nullLogger.trace("format {} {}", "arg1", "arg2"));
        assertDoesNotThrow(() -> nullLogger.trace("format {}", new Object[]{"arg"}));
        assertDoesNotThrow(() -> nullLogger.trace("message", new RuntimeException()));
        assertDoesNotThrow(() -> nullLogger.trace(MarkerFactory.getMarker("TEST"), "message"));
        assertDoesNotThrow(() -> nullLogger.trace(MarkerFactory.getMarker("TEST"), "format {}", "arg"));
        assertDoesNotThrow(() -> nullLogger.trace(MarkerFactory.getMarker("TEST"), "format {} {}", "arg1", "arg2"));
        assertDoesNotThrow(() -> nullLogger.trace(MarkerFactory.getMarker("TEST"), "format {}", new Object[]{"arg"}));
        assertDoesNotThrow(() -> nullLogger.trace(MarkerFactory.getMarker("TEST"), "message", new RuntimeException()));
    }

    @Test
    void testIsDebugEnabled() {
        assertFalse(nullLogger.isDebugEnabled());
        assertFalse(nullLogger.isDebugEnabled(MarkerFactory.getMarker("TEST")));
    }

    @Test
    void testDebugMethodsDoNotThrowExceptions() {
        assertDoesNotThrow(() -> nullLogger.debug("message"));
        assertDoesNotThrow(() -> nullLogger.debug("format {}", "arg"));
        assertDoesNotThrow(() -> nullLogger.debug("format {} {}", "arg1", "arg2"));
        assertDoesNotThrow(() -> nullLogger.debug("format {}", new Object[]{"arg"}));
        assertDoesNotThrow(() -> nullLogger.debug("message", new RuntimeException()));
        assertDoesNotThrow(() -> nullLogger.debug(MarkerFactory.getMarker("TEST"), "message"));
        assertDoesNotThrow(() -> nullLogger.debug(MarkerFactory.getMarker("TEST"), "format {}", "arg"));
        assertDoesNotThrow(() -> nullLogger.debug(MarkerFactory.getMarker("TEST"), "format {} {}", "arg1", "arg2"));
        assertDoesNotThrow(() -> nullLogger.debug(MarkerFactory.getMarker("TEST"), "format {}", new Object[]{"arg"}));
        assertDoesNotThrow(() -> nullLogger.debug(MarkerFactory.getMarker("TEST"), "message", new RuntimeException()));
    }

    @Test
    void testIsInfoEnabled() {
        assertFalse(nullLogger.isInfoEnabled());
        assertFalse(nullLogger.isInfoEnabled(MarkerFactory.getMarker("TEST")));
    }

    @Test
    void testInfoMethodsDoNotThrowExceptions() {
        assertDoesNotThrow(() -> nullLogger.info("message"));
        assertDoesNotThrow(() -> nullLogger.info("format {}", "arg"));
        assertDoesNotThrow(() -> nullLogger.info("format {} {}", "arg1", "arg2"));
        assertDoesNotThrow(() -> nullLogger.info("format {}", new Object[]{"arg"}));
        assertDoesNotThrow(() -> nullLogger.info("message", new RuntimeException()));
        assertDoesNotThrow(() -> nullLogger.info(MarkerFactory.getMarker("TEST"), "message"));
        assertDoesNotThrow(() -> nullLogger.info(MarkerFactory.getMarker("TEST"), "format {}", "arg"));
        assertDoesNotThrow(() -> nullLogger.info(MarkerFactory.getMarker("TEST"), "format {} {}", "arg1", "arg2"));
        assertDoesNotThrow(() -> nullLogger.info(MarkerFactory.getMarker("TEST"), "format {}", new Object[]{"arg"}));
        assertDoesNotThrow(() -> nullLogger.info(MarkerFactory.getMarker("TEST"), "message", new RuntimeException()));
    }

    @Test
    void testIsWarnEnabled() {
        assertFalse(nullLogger.isWarnEnabled());
        assertFalse(nullLogger.isWarnEnabled(MarkerFactory.getMarker("TEST")));
    }

    @Test
    void testWarnMethodsDoNotThrowExceptions() {
        assertDoesNotThrow(() -> nullLogger.warn("message"));
        assertDoesNotThrow(() -> nullLogger.warn("format {}", "arg"));
        assertDoesNotThrow(() -> nullLogger.warn("format {} {}", "arg1", "arg2"));
        assertDoesNotThrow(() -> nullLogger.warn("format {}", new Object[]{"arg"}));
        assertDoesNotThrow(() -> nullLogger.warn("message", new RuntimeException()));
        assertDoesNotThrow(() -> nullLogger.warn(MarkerFactory.getMarker("TEST"), "message"));
        assertDoesNotThrow(() -> nullLogger.warn(MarkerFactory.getMarker("TEST"), "format {}", "arg"));
        assertDoesNotThrow(() -> nullLogger.warn(MarkerFactory.getMarker("TEST"), "format {} {}", "arg1", "arg2"));
        assertDoesNotThrow(() -> nullLogger.warn(MarkerFactory.getMarker("TEST"), "format {}", new Object[]{"arg"}));
        assertDoesNotThrow(() -> nullLogger.warn(MarkerFactory.getMarker("TEST"), "message", new RuntimeException()));
    }

    @Test
    void testIsErrorEnabled() {
        assertFalse(nullLogger.isErrorEnabled());
        assertFalse(nullLogger.isErrorEnabled(MarkerFactory.getMarker("TEST")));
    }

    @Test
    void testErrorMethodsDoNotThrowExceptions() {
        assertDoesNotThrow(() -> nullLogger.error("message"));
        assertDoesNotThrow(() -> nullLogger.error("format {}", "arg"));
        assertDoesNotThrow(() -> nullLogger.error("format {} {}", "arg1", "arg2"));
        assertDoesNotThrow(() -> nullLogger.error("format {}", new Object[]{"arg"}));
        assertDoesNotThrow(() -> nullLogger.error("message", new RuntimeException()));
        assertDoesNotThrow(() -> nullLogger.error(MarkerFactory.getMarker("TEST"), "message"));
        assertDoesNotThrow(() -> nullLogger.error(MarkerFactory.getMarker("TEST"), "format {}", "arg"));
        assertDoesNotThrow(() -> nullLogger.error(MarkerFactory.getMarker("TEST"), "format {} {}", "arg1", "arg2"));
        assertDoesNotThrow(() -> nullLogger.error(MarkerFactory.getMarker("TEST"), "format {}", new Object[]{"arg"}));
        assertDoesNotThrow(() -> nullLogger.error(MarkerFactory.getMarker("TEST"), "message", new RuntimeException()));
    }
}
