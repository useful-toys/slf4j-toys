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

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.impl.MockLogger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.internal.SystemMetrics;
import org.usefultoys.slf4j.internal.SystemMetricsCollector;
import org.usefultoys.test.ValidateCleanMeter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.slf4j.impl.MockLoggerEvent.Level.TRACE;
import static org.usefultoys.slf4j.meter.Markers.DATA_OK;

/**
 * Regression coverage for PERF-007: {@code commonOk()} must not collect system metrics nor format the
 * human-readable message when the result will never be logged (message logger at WARN, operation not slow).
 * <p>
 * These tests mock {@link SystemMetrics#getInstance()} so that the assertions can prove the expensive collection
 * work was skipped, rather than only observing the already-correct absence of a log event (which the buggy code
 * also produces, since only the internal work was wasted, not the final emission).
 */
@ValidateCleanMeter
class MeterCommonOkGateTest {

    private static final String CATEGORY = "commonOkGate";

    private MockLogger warnOnlyLogger() {
        final MockLogger logger = (MockLogger) LoggerFactory.getLogger(CATEGORY);
        logger.clearEvents();
        logger.setErrorEnabled(true);
        logger.setWarnEnabled(true);
        logger.setInfoEnabled(false);
        logger.setDebugEnabled(false);
        logger.setTraceEnabled(false);
        return logger;
    }

    @Test
    void warnEnabledInfoDisabledNotSlow_skipsCollectionAndMessage() {
        final MockLogger logger = warnOnlyLogger();

        try (MockedStatic<SystemMetrics> mockedSystemMetrics = Mockito.mockStatic(SystemMetrics.class)) {
            final SystemMetricsCollector mockCollector = Mockito.mock(SystemMetricsCollector.class);
            mockedSystemMetrics.when(SystemMetrics::getInstance).thenReturn(mockCollector);

            new Meter(logger).start().ok();

            verify(mockCollector, never()).collectRuntimeStatus(any());
            verify(mockCollector, never()).collectPlatformStatus(any());
        }
        assertEquals(0, logger.getEventCount());
    }

    @Test
    void warnEnabledInfoDisabledNotSlow_traceEnabled_stillCollectsForDataLogger() {
        final MockLogger logger = warnOnlyLogger();
        logger.setTraceEnabled(true);

        try (MockedStatic<SystemMetrics> mockedSystemMetrics = Mockito.mockStatic(SystemMetrics.class)) {
            final SystemMetricsCollector mockCollector = Mockito.mock(SystemMetricsCollector.class);
            mockedSystemMetrics.when(SystemMetrics::getInstance).thenReturn(mockCollector);

            new Meter(logger).start().ok();

            verify(mockCollector).collectRuntimeStatus(any());
            verify(mockCollector).collectPlatformStatus(any());
        }
        /* start() logs nothing (DEBUG disabled); only the data logger emits from commonOk() */
        assertEquals(1, logger.getEventCount());
        logger.assertEvent(0, TRACE, DATA_OK);
    }

    @Test
    void warnEnabledInfoDisabledNotSlow_stillClearsContext() {
        final MockLogger logger = warnOnlyLogger();

        try (MockedStatic<SystemMetrics> mockedSystemMetrics = Mockito.mockStatic(SystemMetrics.class)) {
            final SystemMetricsCollector mockCollector = Mockito.mock(SystemMetricsCollector.class);
            mockedSystemMetrics.when(SystemMetrics::getInstance).thenReturn(mockCollector);

            final Meter m = new Meter(logger).start().ctx("key", "value").ok();

            assertTrue(m.getContext().isEmpty(), "context must still be cleared even though nothing was logged");
        }
    }
}
