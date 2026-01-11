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
 *
 * @author Co-authored-by: GitHub Copilot using Claude Haiku 4.5
 */
package org.usefultoys.slf4j.meter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetMeterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.ValidateCleanMeter;
import org.usefultoys.test.WithLocale;

import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.slf4j.impl.MockLoggerEvent.Level;

/**
 * Unit tests for {@link MeterValidator#logBug(Meter, String, Throwable)}.
 * <p>
 * Tests validate that when exceptions occur in Meter methods (start, progress, ok, reject, fail, close),
 * they are caught by try-catch blocks and logBug() is called to record the error with the BUG marker.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *     <li><b>start() method:</b> Verifies that exceptions during start() trigger logBug()</li>
 *     <li><b>progress() method:</b> Verifies that exceptions during progress() trigger logBug()</li>
 *     <li><b>ok() method:</b> Verifies that exceptions during ok() trigger logBug()</li>
 *     <li><b>reject() method:</b> Verifies that exceptions during reject() trigger logBug()</li>
 *     <li><b>fail() method:</b> Verifies that exceptions during fail() trigger logBug()</li>
 *     <li><b>close() method:</b> Verifies that exceptions during close() trigger logBug()</li>
 * </ul>
 */
@ValidateCharset
@ResetMeterConfig
@WithLocale("en")
@WithMockLogger
@ValidateCleanMeter()
@DisplayName("MeterValidator.logBug() - Meter method exceptions")
class MeterLogBugTest {

    @Slf4jMock
    private Logger logger;

    @Nested
    @DisplayName("start() method exception handling")
    class StartMethodTests {

        @Test
        @DisplayName("should call logBug when exception occurs in start()")
        void shouldCallLogBugWhenExceptionInStart() {
            // Given: MeterValidator.validateStartPrecondition() mocked to throw exception
            final Meter meter = new Meter(logger);
            final Meter result;
            
            try (MockedStatic<MeterValidator> mockedValidator = Mockito.mockStatic(MeterValidator.class)) {
                mockedValidator.when(() -> MeterValidator.validateStartPrecondition(any()))
                        .thenThrow(new RuntimeException("Validation failed"));
                // Allow logBug() to execute normally
                mockedValidator.when(() -> MeterValidator.logBug(any(), anyString(), any()))
                        .thenCallRealMethod();

                // When: start() is called (exception will be thrown and caught)
                result = meter.start();
            } // MockedStatic closed here, before assertions and @AfterEach

            // Then:
            // - meter returns self (chaining still works)
            assertNotNull(result, "should return self");
            assertSame(result, meter, "should return the same meter instance");
            // - logBug was called and exception was logged
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.BUG,
                    "Meter.start() method threw exception");
            AssertLogger.assertEventWithThrowable(logger, 0, RuntimeException.class, "Validation failed");
        }
    }

    @Nested
    @DisplayName("progress() method exception handling")
    class ProgressMethodTests {

        @Test
        @ValidateCleanMeter(expectDirtyStack = true)
        @DisplayName("should call logBug when exception occurs in progress()")
        void shouldCallLogBugWhenExceptionInProgress() {
            // Given: MeterValidator.validateProgressPrecondition() mocked to throw exception
            final Meter meter;
            final Meter result;
            
            try (MockedStatic<MeterValidator> mockedValidator = Mockito.mockStatic(MeterValidator.class)) {
                mockedValidator.when(() -> MeterValidator.validateStartPrecondition(any()))
                        .thenReturn(true);  // Allow start() to succeed
                mockedValidator.when(() -> MeterValidator.validateProgressPrecondition(any()))
                        .thenThrow(new RuntimeException("Progress validation failed"));
                mockedValidator.when(() -> MeterValidator.logBug(any(), anyString(), any()))
                        .thenCallRealMethod();

                // When: start() then progress() are called
                meter = new Meter(logger).start().iterations(100).inc();
                result = meter.progress();
            } // MockedStatic closed here

            // Then:
            // - meter returns self
            assertNotNull(result, "should return self");
            // - logBug was called with correct method name
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.BUG,
                    "Meter.progress() method threw exception");
            AssertLogger.assertEventWithThrowable(logger, 2, RuntimeException.class, "Progress validation failed");
        }
    }

    @Nested
    @DisplayName("ok() method exception handling")
    class OkMethodTests {

        @Test
        @ValidateCleanMeter(expectDirtyStack = true)
        @DisplayName("should call logBug when exception occurs in ok()")
        void shouldCallLogBugWhenExceptionInOk() {
            // Given: MeterValidator.validateStopPrecondition() mocked to throw exception
            final Meter result;
            
            try (MockedStatic<MeterValidator> mockedValidator = Mockito.mockStatic(MeterValidator.class)) {
                mockedValidator.when(() -> MeterValidator.validateStartPrecondition(any()))
                        .thenReturn(true);
                mockedValidator.when(() -> MeterValidator.validateStopPrecondition(any(), eq(Markers.INCONSISTENT_OK)))
                        .thenThrow(new RuntimeException("Stop validation failed"));
                mockedValidator.when(() -> MeterValidator.logBug(any(), anyString(), any()))
                        .thenCallRealMethod();

                // When: start() then ok() are called
                final Meter meter = new Meter(logger).start();
                result = meter.ok();
            } // MockedStatic closed here

            // Then:
            // - meter returns self
            assertNotNull(result, "should return self");
            // - logBug was called with correct method name
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.BUG,
                    "Meter.ok(...) method threw exception");
            AssertLogger.assertEventWithThrowable(logger, 2, RuntimeException.class, "Stop validation failed");
        }

        @Test
        @ValidateCleanMeter(expectDirtyStack = true)
        @DisplayName("should call logBug when exception occurs in ok(pathId)")
        void shouldCallLogBugWhenExceptionInOkWithPath() {
            // Given: MeterValidator mocked to throw exception in validatePathArgument
            final Meter result;
            
            try (MockedStatic<MeterValidator> mockedValidator = Mockito.mockStatic(MeterValidator.class)) {
                mockedValidator.when(() -> MeterValidator.validateStartPrecondition(any()))
                        .thenReturn(true);
                mockedValidator.when(() -> MeterValidator.validatePathArgument(any(), anyString(), any()))
                        .thenReturn(true);  // Allow first call (validatePathArgument before commonOk)
                mockedValidator.when(() -> MeterValidator.validateStopPrecondition(any(), eq(Markers.INCONSISTENT_OK)))
                        .thenThrow(new RuntimeException("Stop validation failed"));
                mockedValidator.when(() -> MeterValidator.logBug(any(), anyString(), any()))
                        .thenCallRealMethod();

                // When: start() then ok(pathId) are called
                final Meter meter = new Meter(logger).start();
                result = meter.ok("SUCCESS");
            } // MockedStatic closed here

            // Then:
            // - meter returns self
            assertNotNull(result, "should return self");
            // - logBug was called
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.BUG,
                    "Meter.ok(...) method threw exception");
        }
    }

    @Nested
    @DisplayName("reject() method exception handling")
    class RejectMethodTests {

        @Test
        @ValidateCleanMeter(expectDirtyStack = true)
        @DisplayName("should call logBug when exception occurs in reject()")
        void shouldCallLogBugWhenExceptionInReject() {
            // Given: MeterValidator mocked to throw exception
            final Meter result;
            
            try (MockedStatic<MeterValidator> mockedValidator = Mockito.mockStatic(MeterValidator.class)) {
                mockedValidator.when(() -> MeterValidator.validateStartPrecondition(any()))
                        .thenReturn(true);
                mockedValidator.when(() -> MeterValidator.validatePathArgument(any(), anyString(), any()))
                        .thenReturn(true);
                mockedValidator.when(() -> MeterValidator.validateStopPrecondition(any(), eq(Markers.INCONSISTENT_REJECT)))
                        .thenThrow(new RuntimeException("Reject validation failed"));
                mockedValidator.when(() -> MeterValidator.logBug(any(), anyString(), any()))
                        .thenCallRealMethod();

                // When: start() then reject(cause) are called
                final Meter meter = new Meter(logger).start();
                result = meter.reject("CAUSE");
            } // MockedStatic closed here

            // Then:
            // - meter returns self
            assertNotNull(result, "should return self");
            // - logBug was called with correct method name
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.BUG,
                    "Meter.reject(cause) method threw exception");
            AssertLogger.assertEventWithThrowable(logger, 2, RuntimeException.class, "Reject validation failed");
        }
    }

    @Nested
    @DisplayName("fail() method exception handling")
    class FailMethodTests {

        @Test
        @ValidateCleanMeter(expectDirtyStack = true)
        @DisplayName("should call logBug when exception occurs in fail(String)")
        void shouldCallLogBugWhenExceptionInFailString() {
            // Given: MeterValidator mocked to throw exception
            final Meter result;
            
            try (MockedStatic<MeterValidator> mockedValidator = Mockito.mockStatic(MeterValidator.class)) {
                mockedValidator.when(() -> MeterValidator.validateStartPrecondition(any()))
                        .thenReturn(true);
                mockedValidator.when(() -> MeterValidator.validatePathArgument(any(), anyString(), any()))
                        .thenReturn(true);
                mockedValidator.when(() -> MeterValidator.validateStopPrecondition(any(), eq(Markers.INCONSISTENT_FAIL)))
                        .thenThrow(new RuntimeException("Fail validation failed"));
                mockedValidator.when(() -> MeterValidator.logBug(any(), anyString(), any()))
                        .thenCallRealMethod();

                // When: start() then fail(cause) are called
                final Meter meter = new Meter(logger).start();
                result = meter.fail("FAILURE");
            } // MockedStatic closed here

            // Then:
            // - meter returns self
            assertNotNull(result, "should return self");
            // - logBug was called with correct method name
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.BUG,
                    "Meter.fail(cause) method threw exception");
            AssertLogger.assertEventWithThrowable(logger, 2, RuntimeException.class, "Fail validation failed");
        }

        @Test
        @ValidateCleanMeter(expectDirtyStack = true)
        @DisplayName("should call logBug when exception occurs in fail(Throwable)")
        void shouldCallLogBugWhenExceptionInFailThrowable() {
            // Given: MeterValidator mocked to throw exception
            final Meter result;
            
            try (MockedStatic<MeterValidator> mockedValidator = Mockito.mockStatic(MeterValidator.class)) {
                mockedValidator.when(() -> MeterValidator.validateStartPrecondition(any()))
                        .thenReturn(true);
                mockedValidator.when(() -> MeterValidator.validatePathArgument(any(), anyString(), any()))
                        .thenReturn(true);
                mockedValidator.when(() -> MeterValidator.validateStopPrecondition(any(), eq(Markers.INCONSISTENT_FAIL)))
                        .thenThrow(new RuntimeException("Fail validation failed"));
                mockedValidator.when(() -> MeterValidator.logBug(any(), anyString(), any()))
                        .thenCallRealMethod();

                // When: start() then fail(Throwable) are called
                final Meter meter = new Meter(logger).start();
                final Exception failCause = new IllegalStateException("Original failure");
                result = meter.fail(failCause);
            } // MockedStatic closed here

            // Then:
            // - meter returns self
            assertNotNull(result, "should return self");
            // - logBug was called
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.BUG,
                    "Meter.fail(cause) method threw exception");
        }
    }

    @Nested
    @DisplayName("close() method exception handling")
    class CloseMethodTests {

        @Test
        @ValidateCleanMeter(expectDirtyStack = true)
        @DisplayName("should call logBug when exception occurs in close()")
        void shouldCallLogBugWhenExceptionInClose() {
            // Given: MeterValidator mocked to throw exception
            final Meter meter;
            
            try (MockedStatic<MeterValidator> mockedValidator = Mockito.mockStatic(MeterValidator.class)) {
                mockedValidator.when(() -> MeterValidator.validateStartPrecondition(any()))
                        .thenReturn(true);
                mockedValidator.when(() -> MeterValidator.validateStopPrecondition(any(), eq(Markers.INCONSISTENT_CLOSE)))
                        .thenThrow(new RuntimeException("Close validation failed"));
                mockedValidator.when(() -> MeterValidator.logBug(any(), anyString(), any()))
                        .thenCallRealMethod();

                // When: start() then close() are called
                meter = new Meter(logger).start();
                assertDoesNotThrow(meter::close, "should not propagate exception");
            } // MockedStatic closed here

            // Then:
            // - logBug was called with correct method name
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.BUG,
                    "Meter.close() method threw exception");
            AssertLogger.assertEventWithThrowable(logger, 2, RuntimeException.class, "Close validation failed");
        }
    }

}

