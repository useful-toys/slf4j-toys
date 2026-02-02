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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.slf4j.impl.MockLoggerEvent.Level;

/**
 * Unit tests for {@link MeterValidator#logUnexpectedException(Meter, Throwable)}.
 * <p>
 * Tests validate that when exceptions occur in Meter methods (start, progress, ok, reject, fail, close),
 * they are caught by try-catch blocks and logUnexpectedException() is called to record the error with the UNEXPECTED_EXCEPTION marker.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *     <li><b>start() method:</b> Verifies that exceptions during start() trigger logUnexpectedException()</li>
 *     <li><b>progress() method:</b> Verifies that exceptions during progress() trigger logUnexpectedException()</li>
 *     <li><b>ok() method:</b> Verifies that exceptions during ok() trigger logUnexpectedException()</li>
 *     <li><b>reject() method:</b> Verifies that exceptions during reject() trigger logUnexpectedException()</li>
 *     <li><b>fail() method:</b> Verifies that exceptions during fail() trigger logUnexpectedException()</li>
 *     <li><b>close() method:</b> Verifies that exceptions during close() trigger logUnexpectedException()</li>
 * </ul>
 */
@SuppressWarnings("NonConstantLogger")
@ValidateCharset
@ResetMeterConfig
@WithLocale("en")
@WithMockLogger
@ValidateCleanMeter
@DisplayName("MeterValidator.logUnexpectedException() - Meter method exceptions")
class MeterLogBugTest {

    @Slf4jMock
    private Logger logger;

    @Nested
    @DisplayName("start() method exception handling")
    class StartMethodTests {

        @Test
        @DisplayName("should call logUnexpectedException when exception occurs in start()")
        void shouldCallLogUnexpectedExceptionWhenExceptionInStart() {
            // Given: MeterValidator.validateStartPrecondition() mocked to throw exception
            final Meter meter = new Meter(logger);
            final Meter result;
            
            try (final MockedStatic<MeterValidator> mockedValidator = Mockito.mockStatic(MeterValidator.class)) {
                mockedValidator.when(() -> MeterValidator.validateStartPrecondition(any()))
                        .thenThrow(new RuntimeException("Validation failed"));
                // Allow logUnexpectedException() to execute normally
                mockedValidator.when(() -> MeterValidator.logUnexpectedException(any(), any()))
                        .thenCallRealMethod();

                // When: start() is called (exception will be thrown and caught)
                result = meter.start();
            } // MockedStatic closed here, before assertions and @AfterEach

            // Then:
            // - meter returns self (chaining still works)
            assertNotNull(result, "should return self");
            assertSame(result, meter, "should return the same meter instance");
            // - logUnexpectedException was called and exception was logged
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.UNEXPECTED_EXCEPTION,
                    "Meter.start", "Unexpected exception", meter.getFullID());
            AssertLogger.assertEventWithThrowable(logger, 0, RuntimeException.class, "Validation failed");
        }
    }

    @Nested
    @DisplayName("progress() method exception handling")
    class ProgressMethodTests {

        @Test
        @ValidateCleanMeter(expectDirtyStack = true)
        @DisplayName("should call logUnexpectedException when exception occurs in progress()")
        void shouldCallLogUnexpectedExceptionWhenExceptionInProgress() {
            // Given: MeterValidator.validateProgressPrecondition() mocked to throw exception
            final Meter meter;
            final Meter result;
            
            try (final MockedStatic<MeterValidator> mockedValidator = Mockito.mockStatic(MeterValidator.class)) {
                mockedValidator.when(() -> MeterValidator.validateStartPrecondition(any()))
                        .thenReturn(true);  // Allow start() to succeed
                mockedValidator.when(() -> MeterValidator.validateProgressPrecondition(any()))
                        .thenThrow(new RuntimeException("Progress validation failed"));
                mockedValidator.when(() -> MeterValidator.logUnexpectedException(any(), any()))
                        .thenCallRealMethod();

                // When: start() then progress() are called
                meter = new Meter(logger).start().iterations(100).inc();
                result = meter.progress();
            } // MockedStatic closed here

            // Then:
            // - meter returns self
            assertNotNull(result, "should return self");
            // - logUnexpectedException was called
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.UNEXPECTED_EXCEPTION,
                    "Meter.progress", "Unexpected exception", meter.getFullID());
            AssertLogger.assertEventWithThrowable(logger, 2, RuntimeException.class, "Progress validation failed");
        }
    }

    @Nested
    @DisplayName("ok() method exception handling")
    class OkMethodTests {

        @Test
        @ValidateCleanMeter(expectDirtyStack = true)
        @DisplayName("should call logUnexpectedException when exception occurs in ok()")
        void shouldCallLogUnexpectedExceptionWhenExceptionInOk() {
            // Given: MeterValidator.validateStopPrecondition() mocked to throw exception
            final Meter result;
            
            try (final MockedStatic<MeterValidator> mockedValidator = Mockito.mockStatic(MeterValidator.class)) {
                mockedValidator.when(() -> MeterValidator.validateStartPrecondition(any()))
                        .thenReturn(true);
                mockedValidator.when(() -> MeterValidator.validateStopPrecondition(any(), eq(Markers.INCONSISTENT_OK)))
                        .thenThrow(new RuntimeException("Stop validation failed"));
                mockedValidator.when(() -> MeterValidator.logUnexpectedException(any(), any()))
                        .thenCallRealMethod();

                // When: start() then ok() are called
                final Meter meter = new Meter(logger).start();
                result = meter.ok();
            } // MockedStatic closed here

            // Then:
            // - meter returns self
            assertNotNull(result, "should return self");
            // - logUnexpectedException was called
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.UNEXPECTED_EXCEPTION,
                    "Meter.ok", "Unexpected exception", result.getFullID());
            AssertLogger.assertEventWithThrowable(logger, 2, RuntimeException.class, "Stop validation failed");
        }

        @Test
        @ValidateCleanMeter(expectDirtyStack = true)
        @DisplayName("should call logUnexpectedException when exception occurs in ok(pathId)")
        void shouldCallLogUnexpectedExceptionWhenExceptionInOkWithPath() {
            // Given: MeterValidator mocked to throw exception in validatePathCallArgument
            final Meter result;
            
            try (final MockedStatic<MeterValidator> mockedValidator = Mockito.mockStatic(MeterValidator.class)) {
                mockedValidator.when(() -> MeterValidator.validateStartPrecondition(any()))
                        .thenReturn(true);
                mockedValidator.when(() -> MeterValidator.validatePathCallArgument(any(), any()))
                        .thenReturn(true);  // Allow first call (validatePathCallArgument before commonOk)
                mockedValidator.when(() -> MeterValidator.validateStopPrecondition(any(), eq(Markers.INCONSISTENT_OK)))
                        .thenThrow(new RuntimeException("Stop validation failed"));
                mockedValidator.when(() -> MeterValidator.logUnexpectedException(any(), any()))
                        .thenCallRealMethod();

                // When: start() then ok(pathId) are called
                final Meter meter = new Meter(logger).start();
                result = meter.ok("SUCCESS");
            } // MockedStatic closed here

            // Then:
            // - meter returns self
            assertNotNull(result, "should return self");
            // - logUnexpectedException was called
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.UNEXPECTED_EXCEPTION,
                    "Meter.ok", "Unexpected exception", result.getFullID());
        }
    }

    @Nested
    @DisplayName("reject() method exception handling")
    class RejectMethodTests {

        @Test
        @ValidateCleanMeter(expectDirtyStack = true)
        @DisplayName("should call logUnexpectedException when exception occurs in reject()")
        void shouldCallLogUnexpectedExceptionWhenExceptionInReject() {
            // Given: MeterValidator mocked to throw exception
            final Meter result;
            
            try (final MockedStatic<MeterValidator> mockedValidator = Mockito.mockStatic(MeterValidator.class)) {
                mockedValidator.when(() -> MeterValidator.validateStartPrecondition(any()))
                        .thenReturn(true);
                mockedValidator.when(() -> MeterValidator.validatePathCallArgument(any(), any()))
                        .thenReturn(true);
                mockedValidator.when(() -> MeterValidator.validateStopPrecondition(any(), eq(Markers.INCONSISTENT_REJECT)))
                        .thenThrow(new RuntimeException("Reject validation failed"));
                mockedValidator.when(() -> MeterValidator.logUnexpectedException(any(), any()))
                        .thenCallRealMethod();

                // When: start() then reject(cause) are called
                final Meter meter = new Meter(logger).start();
                result = meter.reject("CAUSE");
            } // MockedStatic closed here

            // Then:
            // - meter returns self
            assertNotNull(result, "should return self");
            // - logUnexpectedException was called
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.UNEXPECTED_EXCEPTION,
                    "Meter.reject", "Unexpected exception", result.getFullID());
            AssertLogger.assertEventWithThrowable(logger, 2, RuntimeException.class, "Reject validation failed");
        }
    }

    @Nested
    @DisplayName("fail() method exception handling")
    class FailMethodTests {

        @Test
        @ValidateCleanMeter(expectDirtyStack = true)
        @DisplayName("should call logUnexpectedException when exception occurs in fail(String)")
        void shouldCallLogUnexpectedExceptionWhenExceptionInFailString() {
            // Given: MeterValidator mocked to throw exception
            final Meter result;
            
            try (final MockedStatic<MeterValidator> mockedValidator = Mockito.mockStatic(MeterValidator.class)) {
                mockedValidator.when(() -> MeterValidator.validateStartPrecondition(any()))
                        .thenReturn(true);
                mockedValidator.when(() -> MeterValidator.validatePathCallArgument(any(), any()))
                        .thenReturn(true);
                mockedValidator.when(() -> MeterValidator.validateStopPrecondition(any(), eq(Markers.INCONSISTENT_FAIL)))
                        .thenThrow(new RuntimeException("Fail validation failed"));
                mockedValidator.when(() -> MeterValidator.logUnexpectedException(any(), any()))
                        .thenCallRealMethod();

                // When: start() then fail(cause) are called
                final Meter meter = new Meter(logger).start();
                result = meter.fail("FAILURE");
            } // MockedStatic closed here

            // Then:
            // - meter returns self
            assertNotNull(result, "should return self");
            // - logUnexpectedException was called
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.UNEXPECTED_EXCEPTION,
                    "Meter.fail", "Unexpected exception", result.getFullID());
            AssertLogger.assertEventWithThrowable(logger, 2, RuntimeException.class, "Fail validation failed");
        }

        @Test
        @ValidateCleanMeter(expectDirtyStack = true)
        @DisplayName("should call logUnexpectedException when exception occurs in fail(Throwable)")
        void shouldCallLogUnexpectedExceptionWhenExceptionInFailThrowable() {
            // Given: MeterValidator mocked to throw exception
            final Meter result;
            
            try (final MockedStatic<MeterValidator> mockedValidator = Mockito.mockStatic(MeterValidator.class)) {
                mockedValidator.when(() -> MeterValidator.validateStartPrecondition(any()))
                        .thenReturn(true);
                mockedValidator.when(() -> MeterValidator.validatePathCallArgument(any(), any()))
                        .thenReturn(true);
                mockedValidator.when(() -> MeterValidator.validateStopPrecondition(any(), eq(Markers.INCONSISTENT_FAIL)))
                        .thenThrow(new RuntimeException("Fail validation failed"));
                mockedValidator.when(() -> MeterValidator.logUnexpectedException(any(), any()))
                        .thenCallRealMethod();

                // When: start() then fail(Throwable) are called
                final Meter meter = new Meter(logger).start();
                final Exception failCause = new IllegalStateException("Original failure");
                result = meter.fail(failCause);
            } // MockedStatic closed here

            // Then:
            // - meter returns self
            assertNotNull(result, "should return self");
            // - logUnexpectedException was called
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.UNEXPECTED_EXCEPTION,
                    "Meter.fail", "Unexpected exception", result.getFullID());
        }
    }

    @Nested
    @DisplayName("close() method exception handling")
    class CloseMethodTests {

        @Test
        @ValidateCleanMeter(expectDirtyStack = true)
        @DisplayName("should call logUnexpectedException when exception occurs in close()")
        void shouldCallLogUnexpectedExceptionWhenExceptionInClose() {
            // Given: MeterValidator mocked to throw exception
            final Meter meter;
            
            try (final MockedStatic<MeterValidator> mockedValidator = Mockito.mockStatic(MeterValidator.class)) {
                mockedValidator.when(() -> MeterValidator.validateStartPrecondition(any()))
                        .thenReturn(true);
                mockedValidator.when(() -> MeterValidator.validateStopPrecondition(any(), eq(Markers.INCONSISTENT_CLOSE)))
                        .thenThrow(new RuntimeException("Close validation failed"));
                mockedValidator.when(() -> MeterValidator.logUnexpectedException(any(), any()))
                        .thenCallRealMethod();

                // When: start() then close() are called
                meter = new Meter(logger).start();
                assertDoesNotThrow(meter::close, "should not propagate exception");
            } // MockedStatic closed here

            // Then:
            // - logUnexpectedException was called
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.UNEXPECTED_EXCEPTION,
                    "Meter.close", "Unexpected exception", meter.getFullID());
            AssertLogger.assertEventWithThrowable(logger, 2, RuntimeException.class, "Close validation failed");
        }
    }

}
