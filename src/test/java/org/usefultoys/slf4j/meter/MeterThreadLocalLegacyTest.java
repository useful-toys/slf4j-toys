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
import org.usefultoys.test.ValidateCleanMeter;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Validates use cases for meter and threadlocal.
 * @author Daniel Felix Ferber
 */
@ValidateCleanMeter
public class MeterThreadLocalLegacyTest {

    @BeforeAll
    public static void validateConsistentCharset() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    final String meterName = "name";
    final MockLogger loggerName = (MockLogger) LoggerFactory.getLogger(meterName);
    final String meterOther = "other";
    final MockLogger loggerOther = (MockLogger) LoggerFactory.getLogger(meterOther);

    @BeforeEach
    public void clearEvents() {
        loggerName.clearEvents();
        loggerOther.clearEvents();
    }

    /**
     * Tests the stacking and unstacking of Meter instances in ThreadLocal
     * when operations complete successfully (`ok()`).
     * <p>
     * Flow:
     * 1. Initially, no Meter is active, so `getCurrentInstance()` returns a dummy Meter ("???").
     * 2. `m1` is created and started. It becomes the active Meter for the current thread.
     * 3. `m2` is created and started. `m1` is pushed onto the stack, and `m2` becomes the active Meter.
     * 4. `m2` completes with `ok()`. It is unstacked, and `m1` is restored as the active Meter.
     * 5. `m1` completes with `ok()`. It is unstacked, and no Meter is active for the thread.
     * <p>
     * Objective: Ensure `Meter.getCurrentInstance()` correctly reflects the most recently started
     * and still active Meter on the current thread.
     */
    @Test
    @DisplayName("Should stack and unstack Meters correctly on successful completion")
    public void shouldStackAndUnstackMetersOnSuccess() {
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially");

        final Meter m1 = MeterFactory.getMeter(loggerName);
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter before m1.start()");
        m1.start();
        assertEquals(meterName, Meter.getCurrentInstance().getCategory(), "m1 should be the active Meter after m1.start()");

        final Meter m2 = MeterFactory.getMeter(loggerOther);
        assertEquals(meterName, Meter.getCurrentInstance().getCategory(), "m1 should still be the active Meter before m2.start()");
        m2.start();
        assertEquals(meterOther, Meter.getCurrentInstance().getCategory(), "m2 should be the active Meter after m2.start()");

        m2.ok();
        assertEquals(meterName, Meter.getCurrentInstance().getCategory(), "m1 should be restored as active Meter after m2.ok()");

        m1.ok();
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter after m1.ok()");
    }

    /**
     * Tests the stacking and unstacking of Meter instances in ThreadLocal
     * when operations fail (`fail()`).
     * <p>
     * Flow:
     * 1. Initially, no Meter is active.
     * 2. `m1` is created and started. It becomes the active Meter.
     * 3. `m2` is created and started. `m1` is pushed, `m2` becomes active.
     * 4. `m2` fails with `fail()`. It is unstacked, and `m1` is restored as the active Meter.
     * 5. `m1` fails with `fail()`. It is unstacked, and no Meter is active.
     * <p>
     * Objective: Confirm that the ThreadLocal mechanism works consistently for both
     * successful and failed operation completions.
     */
    @Test
    @DisplayName("Should stack and unstack Meters correctly on failure")
    public void shouldStackAndUnstackMetersOnFailure() {
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially");

        final Meter m1 = MeterFactory.getMeter(loggerName);
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter before m1.start()");
        m1.start();
        assertEquals(meterName, Meter.getCurrentInstance().getCategory(), "m1 should be the active Meter after m1.start()");

        final Meter m2 = MeterFactory.getMeter(loggerOther);
        assertEquals(meterName, Meter.getCurrentInstance().getCategory(), "m1 should still be the active Meter before m2.start()");
        m2.start();
        assertEquals(meterOther, Meter.getCurrentInstance().getCategory(), "m2 should be the active Meter after m2.start()");

        m2.fail(new IllegalStateException());
        assertEquals(meterName, Meter.getCurrentInstance().getCategory(), "m1 should be restored as active Meter after m2.fail()");

        m1.fail(new IllegalStateException());
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter after m1.fail()");
    }

    /**
     * Tests the isolation of Meter contexts between different threads.
     * <p>
     * Flow:
     * 1. In the main thread, `m1` is created and started, becoming its active Meter.
     * 2. A new thread (`t`) is created and started.
     * 3. Inside the new thread:
     *    a. Initially, no Meter is active for this new thread.
     *    b. `m2` is created and started, becoming the active Meter *for the new thread*.
     *    c. `m2` completes with `ok()`, unstacking itself *from the new thread*.
     * 4. The main thread waits for the new thread to finish.
     * 5. After the new thread completes, the main thread verifies that `m1` is *still*
     *    its active Meter, demonstrating isolation.
     * 6. `m1` fails in the main thread.
     * <p>
     * Objective: Ensure that Meter operations in one thread do not interfere with
     * the active Meter context in other threads.
     */
    @Test
    @DisplayName("Should isolate Meter context between different threads")
    public void shouldIsolateMeterContextBetweenThreads() throws InterruptedException {
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially in main thread");

        final Meter m1 = MeterFactory.getMeter(loggerName);
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter before m1.start() in main thread");
        m1.start();
        assertEquals(meterName, Meter.getCurrentInstance().getCategory(), "m1 should be the active Meter after m1.start() in main thread");

        final Thread t = new Thread() {
            @Override
            public void run() {
                final Meter m2 = MeterFactory.getMeter(loggerOther);
                assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially in new thread"); // No Meter active in this new thread initially
                m2.start();
                assertEquals(meterOther, Meter.getCurrentInstance().getCategory(), "m2 should be the active Meter after m2.start() in new thread"); // m2 is active in this new thread
                m2.ok();
                assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter in new thread after m2.ok()"); // No Meter active in this new thread after m2 completes
            }
        };
        t.start();
        t.join(); // Wait for the new thread to complete
        assertEquals(meterName, Meter.getCurrentInstance().getCategory(), "m1 should still be active in main thread after new thread completes"); // m1 is still active in the main thread

        m1.fail(new IllegalStateException());
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter in main thread after m1.fail()");
    }

    /**
     * Tests the stacking and unstacking of Meter instances in ThreadLocal
     * when operations are rejected (`reject()`).
     * <p>
     * Flow:
     * 1. Initially, no Meter is active.
     * 2. `m1` is created and started. It becomes the active Meter.
     * 3. `m2` is created and started. `m1` is pushed, `m2` becomes active.
     * 4. `m2` is rejected with `reject()`. It is unstacked, and `m1` is restored as the active Meter.
     * 5. `m1` is rejected with `reject()`. It is unstacked, and no Meter is active.
     * <p>
     * Objective: Confirm that the ThreadLocal mechanism works consistently for rejected operation completions.
     */
    @Test
    @DisplayName("Should stack and unstack Meters correctly on rejection")
    public void shouldStackAndUnstackMetersOnReject() {
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially");

        final Meter m1 = MeterFactory.getMeter(loggerName);
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter before m1.start()");
        m1.start();
        assertEquals(meterName, Meter.getCurrentInstance().getCategory(), "m1 should be the active Meter after m1.start()");

        final Meter m2 = MeterFactory.getMeter(loggerOther);
        assertEquals(meterName, Meter.getCurrentInstance().getCategory(), "m1 should still be the active Meter before m2.start()");
        m2.start();
        assertEquals(meterOther, Meter.getCurrentInstance().getCategory(), "m2 should be the active Meter after m2.start()");

        m2.reject("business_rule");
        assertEquals(meterName, Meter.getCurrentInstance().getCategory(), "m1 should be restored as active Meter after m2.reject()");

        m1.reject("another_reason");
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter after m1.reject()");
    }

    /**
     * Tests the behavior of Meter.getCurrentInstance() when a Meter is used within a try-with-resources block.
     * <p>
     * Flow:
     * 1. Initially, no Meter is active.
     * 2. `m1` is created and started. It becomes the active Meter.
     * 3. A try-with-resources block is entered, creating and starting `m2`. `m1` is pushed, `m2` becomes active.
     * 4. The try-with-resources block exits, automatically calling `m2.close()`. `m2` is unstacked, and `m1` is restored.
     * 5. `m1` completes.
     * <p>
     * Objective: Ensure that `close()` invoked by try-with-resources correctly manages the ThreadLocal stack.
     */
    @Test
    @DisplayName("Should handle Meter in try-with-resources block correctly")
    public void shouldHandleMeterInTryWithResources() {
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially");

        final Meter m1 = MeterFactory.getMeter(loggerName);
        m1.start();
        assertEquals(meterName, Meter.getCurrentInstance().getCategory(), "m1 should be the active Meter after m1.start()");

        try (final Meter m2 = MeterFactory.getMeter(loggerOther).start()) {
            assertEquals(meterOther, Meter.getCurrentInstance().getCategory(), "m2 should be the active Meter inside try-with-resources");
        } // m2.close() is called automatically here

        assertEquals(meterName, Meter.getCurrentInstance().getCategory(), "m1 should be restored as active Meter after try-with-resources block exits");

        m1.ok();
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter after m1.ok()");
    }

    /**
     * Tests that a Meter instance is not considered active by getCurrentInstance()
     * if its start() method has not been called.
     * <p>
     * Flow:
     * 1. Initially, no Meter is active.
     * 2. `m1` is created but `start()` is not called.
     * 3. `getCurrentInstance()` should still return the dummy Meter.
     * <p>
     * Objective: Confirm that only calling `start()` registers a Meter in the ThreadLocal.
     */
    @Test
    @DisplayName("Should not return Meter from getCurrentInstance if start() was not called")
    public void shouldNotReturnMeterIfNotStarted() {
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially");

        final Meter m1 = MeterFactory.getMeter(loggerName);
        // m1.start() is intentionally not called

        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should still be no active Meter if m1.start() was not called");

        // Clean up to avoid finalize warnings if m1 is not explicitly stopped
        // In a real scenario, an unstarted Meter wouldn't cause issues, but for testing, we ensure clean state.
        // If m1 was started and not stopped, it would log a warning on finalize.
        // Since it was not started, it won't be in the ThreadLocal stack, so no explicit stop is needed for ThreadLocal.
        // However, if it were to be started later, it would need to be stopped.
        // For this specific test, we just ensure it's not active.
    }

    /**
     * Tests a misuse case where a Meter is created but not started,
     * and another Meter is started and completed.
     * <p>
     * Flow:
     * 1. Initially, no Meter is active.
     * 2. `m1` is created but `start()` is *not* called. It should not become the active Meter.
     * 3. `m2` is created and started. It becomes the active Meter.
     * 4. `m2` completes with `ok()`. It is unstacked, and no Meter is active.
     * 5. `m1` is completed with `ok()`, which is an error as it was never started.
     * <p>
     * Objective: Ensure that `getCurrentInstance()` behaves as expected when `start()` is omitted,
     * and that completing an unstarted Meter does not cause unexpected state changes.
     */
    @Test
    @DisplayName("Should handle misuse: Meter created but not started, then another started and completed")
    public void shouldHandleMisuseMeterCreatedButNotStarted() {
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially");

        final Meter m1 = MeterFactory.getMeter(loggerName);
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter after m1 creation (not started)");
        // Forgets to call m1.start(); current meter is not set to m1
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Still no active Meter as m1 was not started");

        final Meter m2 = MeterFactory.getMeter(loggerOther);
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Still no active Meter before m2.start()");
        m2.start();
        assertEquals(meterOther, Meter.getCurrentInstance().getCategory(), "m2 should be the active Meter after m2.start()");

        m2.ok();
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter after m2.ok()");

        m1.ok(); // This reports an error internally but should not change the current Meter state
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Still no active Meter after m1.ok() (which was not started)");
    }

    /**
     * Tests a misuse case where an inner Meter is started but not completed,
     * and an outer Meter is completed, which should report an error.
     * <p>
     * Flow:
     * 1. Initially, no Meter is active.
     * 2. `m1` is created and started. It becomes the active Meter.
     * 3. `m2` is created and started. `m1` is pushed, `m2` becomes active.
     * 4. `m2` is *not* completed (e.g., `ok()`, `fail()`, `reject()`).
     * 5. `m1` is completed with `ok()`. This is a misuse as `m2` is still active.
     * <p>
     * Objective: Ensure that `getCurrentInstance()` reflects the correct active Meter,
     * and that completing an outer Meter while an inner Meter is still active
     * results in the expected error handling (e.g., `m1.ok()` reports an error and clears the stack).
     */
    @Test
    @DisplayName("Should handle misuse: Inner Meter not completed, outer Meter completed")
    public void shouldHandleMisuseInnerMeterNotCompleted() {
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially");

        final Meter m1 = MeterFactory.getMeter(loggerName);
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter before m1.start()");
        m1.start();
        assertEquals(meterName, Meter.getCurrentInstance().getCategory(), "m1 should be the active Meter after m1.start()");

        final Meter m2 = MeterFactory.getMeter(loggerOther);
        assertEquals(meterName, Meter.getCurrentInstance().getCategory(), "m1 should still be the active Meter before m2.start()");
        m2.start();
        assertEquals(meterOther, Meter.getCurrentInstance().getCategory(), "m2 should be the active Meter after m2.start()");

        // Forgets to call m2.ok() or m2.fail() or m2.reject();
        assertEquals(meterOther, Meter.getCurrentInstance().getCategory(), "m2 should still be the active Meter as it was not completed");

        m1.ok(); // This reports an error internally because m2 is still on the stack, and clears the stack.
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter after m1.ok() (misuse, stack cleared)");
    }

    // --- New tests for repeated and out-of-order calls ---

    @Test
    @DisplayName("Should handle repeated start() calls gracefully")
    public void shouldHandleRepeatedStartCalls() {
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially");

        final Meter m1 = MeterFactory.getMeter(loggerName);
        m1.start();
        assertEquals(meterName, Meter.getCurrentInstance().getCategory(), "m1 should be active after first start()");

        // Repeated start call
        m1.start();
        assertEquals(meterName, Meter.getCurrentInstance().getCategory(), "m1 should still be active after repeated start()");

        m1.ok();
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter after m1.ok()");
    }

    @Test
    @DisplayName("Should handle repeated ok() calls gracefully")
    public void shouldHandleRepeatedOkCalls() {
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially");

        final Meter m1 = MeterFactory.getMeter(loggerName);
        m1.start();
        assertEquals(meterName, Meter.getCurrentInstance().getCategory(), "m1 should be active after start()");

        m1.ok();
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter after first ok()");

        // Repeated ok call
        m1.ok();
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should still be no active Meter after repeated ok()");
    }

    @Test
    @DisplayName("Should handle repeated reject() calls gracefully")
    public void shouldHandleRepeatedRejectCalls() {
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially");

        final Meter m1 = MeterFactory.getMeter(loggerName);
        m1.start();
        assertEquals(meterName, Meter.getCurrentInstance().getCategory(), "m1 should be active after start()");

        m1.reject("reason1");
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter after first reject()");

        // Repeated reject call
        m1.reject("reason2");
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should still be no active Meter after repeated reject()");
    }

    @Test
    @DisplayName("Should handle repeated fail() calls gracefully")
    public void shouldHandleRepeatedFailCalls() {
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially");

        final Meter m1 = MeterFactory.getMeter(loggerName);
        m1.start();
        assertEquals(meterName, Meter.getCurrentInstance().getCategory(), "m1 should be active after start()");

        m1.fail(new IllegalStateException("error1"));
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter after first fail()");

        // Repeated fail call
        m1.fail(new IllegalStateException("error2"));
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should still be no active Meter after repeated fail()");
    }

    @Test
    @DisplayName("Should handle ok() call before start() gracefully")
    public void shouldHandleOkBeforeStart() {
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially");

        final Meter m1 = MeterFactory.getMeter(loggerName);
        // m1.start() is intentionally not called

        m1.ok(); // Call ok() before start()
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter after ok() before start()");
    }

    @Test
    @DisplayName("Should handle reject() call before start() gracefully")
    public void shouldHandleRejectBeforeStart() {
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially");

        final Meter m1 = MeterFactory.getMeter(loggerName);
        // m1.start() is intentionally not called

        m1.reject("reason"); // Call reject() before start()
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter after reject() before start()");
    }

    @Test
    @DisplayName("Should handle fail() call before start() gracefully")
    public void shouldHandleFailBeforeStart() {
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially");

        final Meter m1 = MeterFactory.getMeter(loggerName);
        // m1.start() is intentionally not called

        m1.fail(new IllegalStateException("error")); // Call fail() before start()
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter after fail() before start()");
    }

    @Test
    @DisplayName("Should handle start() call after ok() gracefully (does not restart meter)")
    public void shouldHandleStartAfterOk() {
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially");

        final Meter m1 = MeterFactory.getMeter(loggerName);
        m1.start();
        m1.ok();
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter after ok()");

        // Call start() again on a completed meter
        m1.start();
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter after start() on a completed meter");
    }

    @Test
    @DisplayName("Should handle start() call after reject() gracefully (does not restart meter)")
    public void shouldHandleStartAfterReject() {
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially");

        final Meter m1 = MeterFactory.getMeter(loggerName);
        m1.start();
        m1.reject("reason");
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter after reject()");

        // Call start() again on a completed meter
        m1.start();
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter after start() on a completed meter");
    }

    @Test
    @DisplayName("Should handle start() call after fail() gracefully (does not restart meter)")
    public void shouldHandleStartAfterFail() {
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially");

        final Meter m1 = MeterFactory.getMeter(loggerName);
        m1.start();
        m1.fail(new IllegalStateException("error"));
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter after fail()");

        // Call start() again on a completed meter
        m1.start();
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter after start() on a completed meter");
    }

    @Test
    @DisplayName("Should handle ok() call after fail() gracefully (no effect)")
    public void shouldHandleOkAfterFail() {
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially");

        final Meter m1 = MeterFactory.getMeter(loggerName);
        m1.start();
        m1.fail(new IllegalStateException("error"));
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter after fail()");

        // Call ok() after fail()
        m1.ok();
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should still be no active Meter after ok() after fail()");
    }

    @Test
    @DisplayName("Should handle fail() call after ok() gracefully (no effect)")
    public void shouldHandleFailAfterOk() {
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially");

        final Meter m1 = MeterFactory.getMeter(loggerName);
        m1.start();
        m1.ok();
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter after ok()");

        // Call fail() after ok()
        m1.fail(new IllegalStateException("error"));
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should still be no active Meter after fail() after ok()");
    }

    @Test
    @DisplayName("Should handle reject() call after ok() gracefully (no effect)")
    public void shouldHandleRejectAfterOk() {
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially");

        final Meter m1 = MeterFactory.getMeter(loggerName);
        m1.start();
        m1.ok();
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter after ok()");

        // Call reject() after ok()
        m1.reject("reason");
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should still be no active Meter after reject() after ok()");
    }

    @Test
    @DisplayName("Should handle ok() call after reject() gracefully (no effect)")
    public void shouldHandleOkAfterReject() {
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially");

        final Meter m1 = MeterFactory.getMeter(loggerName);
        m1.start();
        m1.reject("reason");
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter after reject()");

        // Call ok() after reject()
        m1.ok();
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should still be no active Meter after ok() after reject()");
    }

    // --- New tests for invalid lifecycle scenarios ---

    @Test
    @DisplayName("Should clear ThreadLocal stack if a non-active Meter is completed")
    public void shouldHandleCompletionOfNonActiveMeter() {
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially");

        final Meter m1 = MeterFactory.getMeter(loggerName);
        m1.start();
        assertEquals(meterName, Meter.getCurrentInstance().getCategory(), "m1 should be active");

        final Meter m2 = MeterFactory.getMeter(loggerOther);
        m2.start();
        assertEquals(meterOther, Meter.getCurrentInstance().getCategory(), "m2 should be active");

        // m1.ok() is called while m2 is active. This is an inconsistent state.
        // The framework should detect this and clear the ThreadLocal stack.
        m1.ok();
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "ThreadLocal should be cleared after inconsistent m1.ok()");
    }

    @Test
    @DisplayName("Should not affect ThreadLocal if close() is called on an unstarted Meter")
    public void shouldHandleCloseOnUnstartedMeter() {
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially");

        final Meter m1 = MeterFactory.getMeter(loggerName);
        // m1.start() is intentionally not called

        assertEquals("???", Meter.getCurrentInstance().getCategory(), "No active Meter before close() on unstarted m1");
        m1.close(); // Calling close() on an unstarted meter
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "No active Meter after close() on unstarted m1");
    }

    @Test
    @DisplayName("Should not affect ThreadLocal if close() is called on an already stopped Meter")
    public void shouldHandleCloseOnAlreadyStoppedMeter() {
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially");

        final Meter m1 = MeterFactory.getMeter(loggerName);
        m1.start();
        assertEquals(meterName, Meter.getCurrentInstance().getCategory(), "m1 should be active");
        m1.ok();
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "m1 should be stopped");

        // Call close() on an already stopped meter
        m1.close();
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "No active Meter after close() on already stopped m1");
    }

    @Test
    @DisplayName("Should ignore subsequent completion calls after an ignored start()")
    public void shouldHandleCompletionAfterIgnoredStart() {
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "Should be no active Meter initially");

        final Meter m1 = MeterFactory.getMeter(loggerName);
        m1.start();
        m1.ok();
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "m1 should be stopped");

        // This start() call will be ignored as m1 is already stopped
        m1.start();
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "m1.start() should be ignored, no active Meter");

        // Subsequent completion calls should also be ignored and not change ThreadLocal state
        m1.fail(new IllegalStateException("error"));
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "m1.fail() should be ignored after ignored start()");

        m1.reject("reason");
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "m1.reject() should be ignored after ignored start()");

        m1.ok();
        assertEquals("???", Meter.getCurrentInstance().getCategory(), "m1.ok() should be ignored after ignored start()");
    }
}
