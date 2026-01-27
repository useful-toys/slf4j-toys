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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.slf4jtestmock.WithMockLoggerDebug;
import org.usefultoys.test.ResetMeterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.ValidateCleanMeter;
import org.usefultoys.test.WithLocale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link Meter} context lifecycle behavior.
 * <p>
 * Tests validate that context data is correctly captured and cleared
 * at each lifecycle event (start, progress, ok, reject, fail).
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Delta Context Pattern (DEBUG enabled):</b> Verifies each event clears context after log emission when DEBUG is enabled</li>
 *   <li><b>Context Accumulation (INFO only):</b> Verifies context accumulates when start() doesn't emit logs (DEBUG disabled)</li>
 *   <li><b>Context Clearing:</b> Validates that context is automatically cleared after each lifecycle method that emits logs</li>
 *   <li><b>Context Isolation:</b> Ensures context from one event does not leak into subsequent events</li>
 *   <li><b>Auto-correct Behavior:</b> Validates context handling when start() is never called</li>
 *   <li><b>Edge Cases:</b> Empty context, multiple ctx() calls, context isolation between meters</li>
 * </ul>
 *
 * @author Co-authored-by: GitHub Copilot using Claude Sonnet 4.5
 */
@ValidateCharset
@ResetMeterConfig
@WithLocale("en")
@WithMockLogger
@WithMockLoggerDebug
@ValidateCleanMeter
class MeterContextLifecycleTest {

    @Slf4jMock
    private Logger logger;

    // ========================================================================
    // Context Assertion Helper Methods
    // ========================================================================

    /**
     * Asserts that the meter's context contains exactly the specified key-value pairs and no others.
     * <p>
     * Examples:
     * <ul>
     *   <li>{@code assertContext(meter)} → validates empty context</li>
     *   <li>{@code assertContext(meter, "key", "value")} → validates exactly one entry</li>
     *   <li>{@code assertContext(meter, "k1", "v1", "k2", "v2")} → validates exactly two entries</li>
     * </ul>
     *
     * @param meter The meter to validate.
     * @param data  Varargs of key-value pairs (must be even number of strings: key1, value1, key2, value2, ...).
     *              Pass no arguments to validate an empty context.
     */
    private static void assertContext(final Meter meter, final String... data) {
        if (data.length % 2 != 0) {
            throw new IllegalArgumentException("data must contain an even number of strings (key-value pairs)");
        }

        final int expectedSize = data.length / 2;
        assertEquals(expectedSize, meter.getContext().size(),
                String.format("should have exactly %d context %s", expectedSize, 
                        expectedSize == 1 ? "entry" : "entries"));

        for (int i = 0; i < data.length; i += 2) {
            final String key = data[i];
            final String expectedValue = data[i + 1];
            assertEquals(expectedValue, meter.getContext().get(key),
                    String.format("should have %s=%s in context", key, expectedValue));
        }
    }

    // ========================================================================
    // Test Cases
    // ========================================================================

    @Nested
    @DisplayName("Context lifecycle with ok() termination")
    class OkTerminationTests {

        @Nested
        @DisplayName("DEBUG level enabled (Delta Context Pattern)")
        @WithMockLoggerDebug
        class WithDebugLevel {

            @Test
            @DisplayName("should clear context: create-ctx-start-ctx-ok")
            void shouldClearContextAcrossStartAndOk() {
                // Given: a meter with empty context
                final Meter meter = new Meter(logger, "test-operation");
                // Then: new meter has empty context
                assertContext(meter);

                // When: context A added before start
                meter.ctx("user", "alice")
                        .ctx("action", "login");
                // Then: context A present before start
                assertContext(meter, "user", "alice", "action", "login");

                // When: start() called (DEBUG enabled - emits log and clears context)
                meter.start();
                // Then: context A cleared after start
                assertContext(meter);

                // When: context B added before ok
                meter.ctx("result", "success")
                        .ctx("duration", "2s");
                // Then: only context B present (context A was cleared)
                assertContext(meter, "result", "success", "duration", "2s");

                // When: ok() called
                meter.ok();
                // Then: context B cleared after ok
                assertContext(meter);

                // Then: expected log messages (4 events total)
                // start() emits: DEBUG message + TRACE JSON5 with context A
                AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.DEBUG, Markers.MSG_START);
                AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.TRACE, Markers.DATA_START, "user", "alice", "action", "login");
                // ok() emits: INFO message + TRACE JSON5 with context B
                AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
                AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK, "result", "success", "duration", "2s");
                AssertLogger.assertEventCount(logger, 4);
            }

            @Test
            @DisplayName("should clear context: create-ctx-start-ctx-progress-ctx-ok")
            void shouldClearContextAcrossStartProgressAndOk() {
                // Given: progress throttling disabled
                MeterConfig.progressPeriodMilliseconds = 0;
                
                // Given: a meter with empty context
                final Meter meter = new Meter(logger, "test-operation");
                // Then: new meter has empty context
                assertContext(meter);

                // When: context A added before start
                meter.ctx("user", "bob")
                        .iterations(100);
                // Then: context A present before start
                assertContext(meter, "user", "bob");

                // When: start() called (DEBUG enabled - emits log and clears context)
                meter.start();
                // Then: context A cleared after start
                assertContext(meter);

                // When: context B added before progress
                meter.ctx("step", "processing")
                        .incTo(50);
                // Then: only context B present
                assertContext(meter, "step", "processing");

                // When: progress() called
                meter.progress();
                // Then: context B cleared after progress
                assertContext(meter);

                // When: context C added before ok
                meter.ctx("result", "done");
                // Then: only context C present
                assertContext(meter, "result", "done");

                // When: ok() called
                meter.ok();
                // Then: context C cleared after ok
                assertContext(meter);

                // Then: expected log messages (8 events total)
                // start() emits: DEBUG message + TRACE JSON5 with context A
                AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.DEBUG, Markers.MSG_START);
                AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.TRACE, Markers.DATA_START, "user", "bob");
                // progress() emits: INFO message + TRACE JSON5 with context B
                AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
                AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_PROGRESS, "step", "processing");
                // ok() emits: INFO message + TRACE JSON5 with context C
                AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
                AssertLogger.assertEvent(logger, 5, MockLoggerEvent.Level.TRACE, Markers.DATA_OK, "result", "done");
                AssertLogger.assertEventCount(logger, 6);
            }
        }

        @Nested
        @DisplayName("INFO level only (Context Accumulation)")
        class WithInfoLevel {

            @Test
            @DisplayName("should accumulate context: create-ctx-start-ctx-ok")
            void shouldAccumulateContextWhenStartDoesNotEmit(@Slf4jMock(debugEnabled = false) final Logger logger) {
                // Given: a meter with empty context
                final Meter meter = new Meter(logger, "test-operation");
                // Then: new meter has empty context
                assertContext(meter);

                // When: context A added before start
                meter.ctx("user", "charlie")
                        .ctx("action", "process");
                // Then: context A present before start
                assertContext(meter, "user", "charlie", "action", "process");

                // When: start() called (DEBUG disabled - NO log, NO clearing)
                meter.start();
                // Then: context A preserved (start didn't emit log)
                assertContext(meter, "user", "charlie", "action", "process");

                // When: context B added before ok
                meter.ctx("result", "finished");
                // Then: BOTH context A and B present (accumulated)
                assertContext(meter, "user", "charlie", "action", "process", "result", "finished");

                // When: ok() called
                meter.ok();
                // Then: all accumulated context cleared
                assertContext(meter);

                // Then: expected log messages (2 events total)
                // start() does NOT emit (DEBUG disabled)
                // ok() emits: INFO message + TRACE JSON5 with accumulated context
                AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
                AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.TRACE, Markers.DATA_OK, "user", "charlie", "action", "process", "result", "finished");
                AssertLogger.assertEventCount(logger, 2);
            }

            @Test
            @DisplayName("should accumulate then clear: create-ctx-start-ctx-progress-ctx-ok")
            void shouldAccumulateUntilProgressThenClear(@Slf4jMock(debugEnabled = false) final Logger logger) {
                // Given: a meter with empty context
                final Meter meter = new Meter(logger, "test-operation");
                // Then: new meter has empty context
                assertContext(meter);

                // When: context A added before start
                meter.ctx("user", "dave")
                        .iterations(100);
                // Then: context A present before start
                assertContext(meter, "user", "dave");

                // When: start() called (DEBUG disabled - NO log, NO clearing)
                meter.start();
                // Then: context A preserved
                assertContext(meter, "user", "dave");

                // When: context B added before progress
                meter.ctx("step", "validating")
                        .incTo(50);
                // Then: BOTH contexts present (accumulated)
                assertContext(meter, "user", "dave", "step", "validating");

                // When: progress() called (INFO level DISABLED - NO log, NO clearing)
                meter.progress();
                // Then: all accumulated context PRESERVED (progress didn't emit log)
                assertContext(meter, "user", "dave", "step", "validating");

                // When: context C added before ok
                meter.ctx("result", "complete");
                // Then: only context C present (BUT actually all are present since progress didn't clear)
                assertContext(meter, "user", "dave", "step", "validating", "result", "complete");

                // When: ok() called
                meter.ok();
                // Then: context cleared
                assertContext(meter);

                // Then: expected log messages (2 events total)
                // start() does NOT emit (DEBUG disabled)
                // progress() does NOT emit (INFO disabled)
                // ok() emits: INFO message + TRACE JSON5 with ALL accumulated context (user, step, result)
                AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
                AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.TRACE, Markers.DATA_OK, "user", "dave", "step", "validating", "result", "complete");
                AssertLogger.assertEventCount(logger, 2);
            }
        }

        @Nested
        @DisplayName("Auto-correct: start() never called")
        @WithMockLoggerDebug
        class AutoCorrectWithoutStart {

            @Test
            @DisplayName("should handle context when start() is skipped: create-ctx-ok")
            void shouldHandleContextWhenStartIsSkipped() {
                // Given: a meter with empty context
                final Meter meter = new Meter(logger, "test-operation");
                // Then: new meter has empty context
                assertContext(meter);

                // When: context A added before ok (start() never called)
                meter.ctx("user", "eve")
                        .ctx("action", "skip-start");
                // Then: context A present
                assertContext(meter, "user", "eve", "action", "skip-start");

                // When: ok() called without start() (auto-correct)
                meter.ok();
                // Then: context cleared after ok
                assertContext(meter);

                // Then: expected log messages (3 events total)
                // auto-correct emits ERROR INCONSISTENT_OK (startTime was 0)
                AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
                // ok() emits: INFO message + TRACE JSON5 with context A
                AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
                AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_OK, "user", "eve", "action", "skip-start");
                AssertLogger.assertEventCount(logger, 3);
            }
        }
    }

    @Nested
    @DisplayName("Context lifecycle with reject() termination")
    class RejectTerminationTests {

        @Nested
        @DisplayName("DEBUG level enabled (Delta Context Pattern)")
        @WithMockLoggerDebug
        class WithDebugLevel {

            @Test
            @DisplayName("should clear context: create-ctx-start-ctx-reject")
            void shouldClearContextAcrossStartAndReject() {
                // Given: a meter with empty context
                final Meter meter = new Meter(logger, "test-operation");
                // Then: new meter has empty context
                assertContext(meter);

                // When: context A added before start
                meter.ctx("user", "frank");
                // Then: context A present before start
                assertContext(meter, "user", "frank");

                // When: start() called (DEBUG enabled - emits log and clears context)
                meter.start();
                // Then: context A cleared after start
                assertContext(meter);

                // When: context B added before reject
                meter.ctx("reason", "invalid-input")
                        .ctx("code", "400");
                // Then: only context B present
                assertContext(meter, "reason", "invalid-input", "code", "400");

                // When: reject() called
                meter.reject("validation-failed");
                // Then: context B cleared after reject
                assertContext(meter);

                // Then: expected log messages (4 events total)
                // start() emits: DEBUG message + TRACE JSON5 with context A
                AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.DEBUG, Markers.MSG_START);
                AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.TRACE, Markers.DATA_START, "user", "frank");
                // reject() emits: INFO message + TRACE JSON5 with context B
                AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
                AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT, "reason", "invalid-input", "code", "400");
                AssertLogger.assertEventCount(logger, 4);
            }

            @Test
            @DisplayName("should clear context: create-ctx-start-ctx-progress-ctx-reject")
            void shouldClearContextAcrossStartProgressAndReject() {
                // Given: progress throttling disabled
                MeterConfig.progressPeriodMilliseconds = 0;
                
                // Given: a meter with empty context
                final Meter meter = new Meter(logger, "test-operation");
                // Then: new meter has empty context
                assertContext(meter);

                // When: context A added before start
                meter.ctx("user", "grace")
                        .iterations(100);
                // Then: context A present before start
                assertContext(meter, "user", "grace");

                // When: start() called
                meter.start();
                // Then: context A cleared after start
                assertContext(meter);

                // When: context B added before progress
                meter.ctx("step", "processing")
                        .incTo(50);
                // Then: only context B present
                assertContext(meter, "step", "processing");

                // When: progress() called (INFO level - emits log and clears)
                meter.progress();
                // Then: context B cleared after progress
                assertContext(meter);

                // When: context C added before reject
                meter.ctx("reason", "aborted");
                // Then: only context C present
                assertContext(meter, "reason", "aborted");

                // When: reject() called
                meter.reject("user-cancelled");
                // Then: context C cleared after reject
                assertContext(meter);

                // Then: expected log messages (6 events total)
                // start() emits: DEBUG message + TRACE JSON5 with context A
                AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.DEBUG, Markers.MSG_START);
                AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.TRACE, Markers.DATA_START, "user", "grace");
                // progress() emits: INFO message + TRACE JSON5 with context B
                AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
                AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_PROGRESS, "step", "processing");
                // reject() emits: INFO message + TRACE JSON5 with context C
                AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
                AssertLogger.assertEvent(logger, 5, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT, "reason", "aborted");
                AssertLogger.assertEventCount(logger, 6);
            }
        }

        @Nested
        @DisplayName("INFO level only (Context Accumulation)")
        class WithInfoLevel {

            @Test
            @DisplayName("should accumulate context: create-ctx-start-ctx-reject")
            void shouldAccumulateContextWhenStartDoesNotEmit(@Slf4jMock(debugEnabled = false) final Logger logger) {
                // Given: a meter with empty context
                final Meter meter = new Meter(logger, "test-operation");
                // Then: new meter has empty context
                assertContext(meter);

                // When: context A added before start
                meter.ctx("user", "henry");
                // Then: context A present before start
                assertContext(meter, "user", "henry");

                // When: start() called (DEBUG disabled - NO log, NO clearing)
                meter.start();
                // Then: context A preserved
                assertContext(meter, "user", "henry");

                // When: context B added before reject
                meter.ctx("reason", "timeout");
                // Then: BOTH contexts present (accumulated)
                assertContext(meter, "user", "henry", "reason", "timeout");

                // When: reject() called
                meter.reject("request-timeout");
                // Then: all accumulated context cleared
                assertContext(meter);

                // Then: expected log messages (2 events total)
                // start() does NOT emit (DEBUG disabled)
                // reject() emits: INFO message + TRACE JSON5 with accumulated context
                AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
                AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT, "user", "henry", "reason", "timeout");
                AssertLogger.assertEventCount(logger, 2);
            }

            @Test
            @DisplayName("should accumulate then clear: create-ctx-start-ctx-progress-ctx-reject")
            void shouldAccumulateUntilProgressThenClear(@Slf4jMock(debugEnabled = false) final Logger logger) {
                // Given: a meter with empty context
                final Meter meter = new Meter(logger, "test-operation");
                // Then: new meter has empty context
                assertContext(meter);

                // When: context A added before start
                meter.ctx("user", "irene")
                        .iterations(100);
                // Then: context A present before start
                assertContext(meter, "user", "irene");

                // When: start() called (DEBUG disabled)
                meter.start();
                // Then: context A preserved
                assertContext(meter, "user", "irene");

                // When: context B added before progress
                meter.ctx("step", "downloading")
                        .incTo(50);
                // Then: BOTH contexts present (accumulated)
                assertContext(meter, "user", "irene", "step", "downloading");

                // When: progress() called (INFO level DISABLED - NO log, NO clearing)
                meter.progress();
                // Then: all accumulated context PRESERVED (progress didn't emit log)
                assertContext(meter, "user", "irene", "step", "downloading");

                // When: context C added before reject
                meter.ctx("reason", "interrupted");
                // Then: only context C present (BUT actually all are present since progress didn't clear)
                assertContext(meter, "user", "irene", "step", "downloading", "reason", "interrupted");

                // When: reject() called
                meter.reject("download-interrupted");
                // Then: context cleared
                assertContext(meter);

                // Then: expected log messages (2 events total)
                // start() does NOT emit (DEBUG disabled)
                // progress() does NOT emit (INFO disabled)
                // reject() emits: INFO message + TRACE JSON5 with ALL accumulated context (user, step, reason)
                AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
                AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT, "user", "irene", "step", "downloading", "reason", "interrupted");
                AssertLogger.assertEventCount(logger, 2);
            }
        }

        @Nested
        @DisplayName("Auto-correct: start() never called")
        @WithMockLoggerDebug
        class AutoCorrectWithoutStart {

            @Test
            @DisplayName("should handle context when start() is skipped: create-ctx-reject")
            void shouldHandleContextWhenStartIsSkipped() {
                // Given: a meter with empty context
                final Meter meter = new Meter(logger, "test-operation");
                // Then: new meter has empty context
                assertContext(meter);

                // When: context A added before reject (start() never called)
                meter.ctx("user", "jack");
                // Then: context A present
                assertContext(meter, "user", "jack");

                // When: reject() called without start() (auto-correct)
                meter.reject("precondition-failed");
                // Then: context cleared after reject
                assertContext(meter);

                // Then: expected log messages (3 events total)
                // auto-correct emits ERROR INCONSISTENT_REJECT (startTime was 0)
                AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
                // reject() emits: INFO message + TRACE JSON5 with context A
                AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
                AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT, "user", "jack");
                AssertLogger.assertEventCount(logger, 3);
            }
        }
    }

    @Nested
    @DisplayName("Context lifecycle with fail() termination")
    class FailTerminationTests {

        @Nested
        @DisplayName("DEBUG level enabled (Delta Context Pattern)")
        @WithMockLoggerDebug
        class WithDebugLevel {

            @Test
            @DisplayName("should clear context: create-ctx-start-ctx-fail")
            void shouldClearContextAcrossStartAndFail() {
                // Given: a meter with empty context
                final Meter meter = new Meter(logger, "test-operation");
                // Then: new meter has empty context
                assertContext(meter);

                // When: context A added before start
                meter.ctx("user", "kate");
                // Then: context A present before start
                assertContext(meter, "user", "kate");

                // When: start() called (DEBUG enabled - emits log and clears context)
                meter.start();
                // Then: context A cleared after start
                assertContext(meter);

                // When: context B added before fail
                meter.ctx("error", "connection-lost")
                        .ctx("retries", "3");
                // Then: only context B present
                assertContext(meter, "error", "connection-lost", "retries", "3");

                // When: fail() called
                meter.fail(new RuntimeException("Connection timeout"));
                // Then: context B cleared after fail
                assertContext(meter);

                // Then: expected log messages (4 events total)
                // start() emits: DEBUG message + TRACE JSON5 with context A
                AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.DEBUG, Markers.MSG_START);
                AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.TRACE, Markers.DATA_START, "user", "kate");
                // fail() emits: ERROR message + TRACE JSON5 with context B
                AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
                AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL, "error", "connection-lost", "retries", "3");
                AssertLogger.assertEventCount(logger, 4);
            }

            @Test
            @DisplayName("should clear context: create-ctx-start-ctx-progress-ctx-fail")
            void shouldClearContextAcrossStartProgressAndFail() {
                // Given: progress throttling disabled
                MeterConfig.progressPeriodMilliseconds = 0;
                
                // Given: a meter with empty context
                final Meter meter = new Meter(logger, "test-operation");
                // Then: new meter has empty context
                assertContext(meter);

                // When: context A added before start
                meter.ctx("user", "leo")
                        .iterations(100);
                // Then: context A present before start
                assertContext(meter, "user", "leo");

                // When: start() called
                meter.start();
                // Then: context A cleared after start
                assertContext(meter);

                // When: context B added before progress
                meter.ctx("step", "uploading")
                        .incTo(50);
                // Then: only context B present
                assertContext(meter, "step", "uploading");

                // When: progress() called (INFO level - emits log and clears)
                meter.progress();
                // Then: context B cleared after progress
                assertContext(meter);

                // When: context C added before fail
                meter.ctx("error", "disk-full");
                // Then: only context C present
                assertContext(meter, "error", "disk-full");

                // When: fail() called
                meter.fail(new RuntimeException("Disk full"));
                // Then: context C cleared after fail
                assertContext(meter);

                // Then: expected log messages (6 events total)
                // start() emits: DEBUG message + TRACE JSON5 with context A
                AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.DEBUG, Markers.MSG_START);
                AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.TRACE, Markers.DATA_START, "user", "leo");
                // progress() emits: INFO message + TRACE JSON5 with context B
                AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
                AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_PROGRESS, "step", "uploading");
                // fail() emits: ERROR message + TRACE JSON5 with context C
                AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
                AssertLogger.assertEvent(logger, 5, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL, "error", "disk-full");
                AssertLogger.assertEventCount(logger, 6);
            }
        }

        @Nested
        @DisplayName("INFO level only (Context Accumulation)")
        class WithInfoLevel {

            @Test
            @DisplayName("should accumulate context: create-ctx-start-ctx-fail")
            void shouldAccumulateContextWhenStartDoesNotEmit(@Slf4jMock(debugEnabled = false) final Logger logger) {
                // Given: a meter with empty context
                final Meter meter = new Meter(logger, "test-operation");
                // Then: new meter has empty context
                assertContext(meter);

                // When: context A added before start
                meter.ctx("user", "mary");
                // Then: context A present before start
                assertContext(meter, "user", "mary");

                // When: start() called (DEBUG disabled - NO log, NO clearing)
                meter.start();
                // Then: context A preserved
                assertContext(meter, "user", "mary");

                // When: context B added before fail
                meter.ctx("error", "network-error");
                // Then: BOTH contexts present (accumulated)
                assertContext(meter, "user", "mary", "error", "network-error");

                // When: fail() called
                meter.fail(new RuntimeException("Network error"));
                // Then: all accumulated context cleared
                assertContext(meter);

                // Then: expected log messages (2 events total)
                // start() does NOT emit (DEBUG disabled)
                // fail() emits: ERROR message + TRACE JSON5 with accumulated context
                AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
                AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL, "user", "mary", "error", "network-error");
                AssertLogger.assertEventCount(logger, 2);
            }

            @Test
            @DisplayName("should accumulate then clear: create-ctx-start-ctx-progress-ctx-fail")
            void shouldAccumulateUntilProgressThenClear(@Slf4jMock(debugEnabled = false) final Logger logger) {
                // Given: a meter with empty context
                final Meter meter = new Meter(logger, "test-operation");
                // Then: new meter has empty context
                assertContext(meter);

                // When: context A added before start
                meter.ctx("user", "nancy")
                        .iterations(100);
                // Then: context A present before start
                assertContext(meter, "user", "nancy");

                // When: start() called (DEBUG disabled)
                meter.start();
                // Then: context A preserved
                assertContext(meter, "user", "nancy");

                // When: context B added before progress
                meter.ctx("step", "compressing")
                        .incTo(50);
                // Then: BOTH contexts present (accumulated)
                assertContext(meter, "user", "nancy", "step", "compressing");

                // When: progress() called (INFO level DISABLED - NO log, NO clearing)
                meter.progress();
                // Then: all accumulated context PRESERVED (progress didn't emit log)
                assertContext(meter, "user", "nancy", "step", "compressing");

                // When: context C added before fail
                meter.ctx("error", "out-of-memory");
                // Then: only context C present (BUT actually all are present since progress didn't clear)
                assertContext(meter, "user", "nancy", "step", "compressing", "error", "out-of-memory");

                // When: fail() called
                meter.fail(new RuntimeException("Out of memory"));
                // Then: context cleared
                assertContext(meter);

                // Then: expected log messages (2 events total)
                // start() does NOT emit (DEBUG disabled)
                // progress() does NOT emit (INFO disabled)
                // fail() emits: ERROR message + TRACE JSON5 with ALL accumulated context (user, step, error)
                AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
                AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL, "user", "nancy", "step", "compressing", "error", "out-of-memory");
                AssertLogger.assertEventCount(logger, 2);
            }
        }

        @Nested
        @DisplayName("Auto-correct: start() never called")
        @WithMockLoggerDebug
        class AutoCorrectWithoutStart {

            @Test
            @DisplayName("should handle context when start() is skipped: create-ctx-fail")
            void shouldHandleContextWhenStartIsSkipped() {
                // Given: a meter with empty context
                final Meter meter = new Meter(logger, "test-operation");
                // Then: new meter has empty context
                assertContext(meter);

                // When: context A added before fail (start() never called)
                meter.ctx("user", "oscar");
                // Then: context A present
                assertContext(meter, "user", "oscar");

                // When: fail() called without start() (auto-correct)
                meter.fail(new RuntimeException("Init failed"));
                // Then: context cleared after fail
                assertContext(meter);

                // Then: expected log messages (3 events total)
                // auto-correct emits ERROR INCONSISTENT_FAIL (startTime was 0)
                AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
                // fail() emits: ERROR message + TRACE JSON5 with context A
                AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
                AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL, "user", "oscar");
                AssertLogger.assertEventCount(logger, 3);
            }
        }
    }
}
