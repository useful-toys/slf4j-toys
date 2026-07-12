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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.slf4j.impl.MockLogger;
import org.usefultoys.slf4j.CallerStackTraceThrowable;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.test.ResetMeterConfig;
import org.usefultoys.test.ValidateCleanMeter;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.slf4j.impl.MockLoggerEvent.Level.ERROR;
import static org.usefultoys.slf4j.meter.Markers.INVALID_STATE;
import static org.usefultoys.slf4j.meter.Markers.INVALID_TRANSITION;

/**
 * Verifies the misuse-report throttle on the shared unknown-meter null-object (see TDR-0042): a
 * defensive call pattern like {@code Meter.getCurrentInstance().progress()} in a hot loop must not flood
 * the error channel or pay a stack-trace capture on every single call.
 *
 * @author Daniel Felix Ferber
 */
@ResetMeterConfig
@ValidateCleanMeter
@DisplayName("Shared unknown-meter misuse-report throttle")
class MeterUnknownInstanceThrottleTest {

    private MockLogger unknownLogger;

    @BeforeEach
    void resolveUnknownLogger() {
        unknownLogger = (MockLogger) Meter.getCurrentInstance().getMessageLogger();
        unknownLogger.clearEvents();
    }

    @Test
    @DisplayName("first misuse after reset reports immediately")
    void firstMisuseReportsImmediately() {
        Meter.getCurrentInstance().start();

        AssertLogger.assertEvent(unknownLogger, 0, ERROR, INVALID_TRANSITION);
        AssertLogger.assertEventCount(unknownLogger, 1);
    }

    @Test
    @DisplayName("subsequent misuse within the interval is suppressed")
    void subsequentMisuseWithinIntervalIsSuppressed() {
        MeterConfig.noopReportIntervalMilliseconds = 60_000L;
        final Meter unknown = Meter.getCurrentInstance();

        unknown.start(); // reports immediately
        unknown.start(); // suppressed
        unknown.start(); // suppressed

        AssertLogger.assertEventCount(unknownLogger, 1);
    }

    @Test
    @DisplayName("a report after the interval elapses carries the suppressed count")
    void reportAfterIntervalCarriesSuppressedCount() throws InterruptedException {
        MeterConfig.noopReportIntervalMilliseconds = 20L;
        final Meter unknown = Meter.getCurrentInstance();

        unknown.start(); // reports immediately
        unknown.start(); // suppressed (1)
        unknown.start(); // suppressed (2)
        Thread.sleep(60); // cross the interval
        unknown.start(); // reports again, with the suppressed count embedded

        AssertLogger.assertEventCount(unknownLogger, 2);
        AssertLogger.assertEvent(unknownLogger, 1, ERROR, INVALID_TRANSITION, "2 similar reports suppressed");
    }

    @Test
    @DisplayName("interval 0 reports every occurrence")
    void intervalZeroReportsEveryOccurrence() {
        MeterConfig.noopReportIntervalMilliseconds = 0L;
        final Meter unknown = Meter.getCurrentInstance();

        unknown.start();
        unknown.start();
        unknown.start();

        AssertLogger.assertEventCount(unknownLogger, 3);
    }

    @Test
    @DisplayName("negative interval reports nothing")
    void negativeIntervalReportsNothing() {
        MeterConfig.noopReportIntervalMilliseconds = -1L;
        final Meter unknown = Meter.getCurrentInstance();

        unknown.start();
        unknown.start();

        AssertLogger.assertEventCount(unknownLogger, 0);
    }

    @Test
    @DisplayName("misuse routed through logInvalidState (inc/incBy/incTo/progress/path) is also throttled "
            + "and uses the corrected diagnosis, not the misleading \"not yet started\" message")
    void misuseViaLogInvalidStateIsThrottledAndUsesCorrectDiagnosis() {
        MeterConfig.noopReportIntervalMilliseconds = 60_000L;
        final Meter unknown = Meter.getCurrentInstance();

        unknown.inc(); // not overridden by UnknownMeter; rejected by validateIncPrecondition -> logInvalidState
        unknown.inc(); // suppressed
        unknown.inc(); // suppressed

        AssertLogger.assertEventCount(unknownLogger, 1);
        AssertLogger.assertEvent(unknownLogger, 0, ERROR, INVALID_STATE, "no operation is active on the current thread");
    }

    @Test
    @DisplayName("concurrent misuse: exactly one thread claims the report window, the rest retry the CAS loop")
    void concurrentMisuseOnlyOneThreadClaimsTheWindow() throws InterruptedException {
        MeterConfig.noopReportIntervalMilliseconds = 60_000L;
        final int threadCount = 64;
        final AtomicInteger readyCount = new AtomicInteger(0);
        final AtomicBoolean start = new AtomicBoolean(false);
        final AtomicInteger claimedCount = new AtomicInteger(0);
        final Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                final Meter unknown = Meter.getCurrentInstance();
                readyCount.incrementAndGet();
                /* Busy-spin on a plain flag (no park/unpark) to release all threads within as tight a
                   window as possible, maximizing the chance that multiple threads observe the same stale
                   "next allowed" timestamp and must retry the CAS loop against each other. */
                while (!start.get()) {
                    Thread.yield();
                }
                if (unknown.shouldReportInvalidUsage()) {
                    claimedCount.incrementAndGet();
                }
            });
            threads[i].start();
        }

        while (readyCount.get() < threadCount) {
            Thread.yield();
        }
        start.set(true);
        for (final Thread thread : threads) {
            thread.join();
        }

        assertEquals(1, claimedCount.get(),
                "exactly one of " + threadCount + " concurrent misuse occurrences should claim the report window");
    }

    @Test
    @DisplayName("real (non-singleton) meters are never throttled")
    void realMetersAreNeverThrottled() {
        /* Long enough to throttle the singleton, to prove it has no effect on a real Meter */
        MeterConfig.noopReportIntervalMilliseconds = 60_000L;
        final MockLogger logger = (MockLogger) LoggerFactory.getLogger("realMeterThrottleTest");
        logger.clearEvents();
        final Meter meter = new Meter(logger).start();
        meter.ok();
        logger.clearEvents(); // discard the legitimate start()/ok() events; only misuse matters below

        /* Every call below hits the same "already stopped" branch of validateMPrecondition */
        meter.m("first misuse");
        meter.m("second misuse");
        meter.m("third misuse");

        AssertLogger.assertEventCount(logger, 3);
        AssertLogger.assertEvent(logger, 0, ERROR, INVALID_STATE);
        AssertLogger.assertEvent(logger, 1, ERROR, INVALID_STATE);
        AssertLogger.assertEvent(logger, 2, ERROR, INVALID_STATE);
    }

    @Test
    @DisplayName("no CallerStackTraceThrowable is allocated when ERROR is disabled, via logInvalidTransition or logInvalidState")
    void noThrowableAllocatedWhenErrorDisabled() {
        unknownLogger.setErrorEnabled(false);
        try (MockedConstruction<CallerStackTraceThrowable> mocked = Mockito.mockConstruction(CallerStackTraceThrowable.class)) {
            Meter.getCurrentInstance().start(); // routes through logInvalidTransition
            Meter.getCurrentInstance().inc();   // routes through logInvalidState

            assertTrue(mocked.constructed().isEmpty(),
                    "no CallerStackTraceThrowable should be allocated when ERROR is disabled");
        } finally {
            unknownLogger.setErrorEnabled(true);
        }
    }
}
