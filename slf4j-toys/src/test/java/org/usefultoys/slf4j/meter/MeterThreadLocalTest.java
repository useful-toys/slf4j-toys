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

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Validates use cases for meter and threadlocal.
 * @author Daniel Felix Ferber
 */
public class MeterThreadLocalTest {

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

        try (Meter m2 = MeterFactory.getMeter(loggerOther).start()) {
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
}
