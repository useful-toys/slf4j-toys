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
package org.usefultoys.slf4j.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link SystemTimeSource}.
 * <p>
 * Tests verify that SystemTimeSource correctly implements the TimeSource interface,
 * delegates to System.nanoTime(), and follows the singleton pattern.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Singleton Pattern:</b> Verifies that INSTANCE is always the same object</li>
 *   <li><b>TimeSource Implementation:</b> Tests that nanoTime() delegates to System.nanoTime()</li>
 *   <li><b>Monotonicity:</b> Ensures time values are monotonically increasing</li>
 *   <li><b>Thread Safety:</b> Verifies thread-safe behavior in concurrent scenarios</li>
 * </ul>
 *
 * @author Daniel Felix Ferber
 * @author Co-authored-by: GitHub Copilot using o1
 * @see SystemTimeSource
 * @see TimeSource
 */
@DisplayName("SystemTimeSource")
@ValidateCharset
@WithLocale("en")
class SystemTimeSourceTest {

    @Test
    @DisplayName("should delegate to System.nanoTime()")
    void testDelegatesToSystemNanoTime() {
        // Given: SystemTimeSource instance
        final SystemTimeSource timeSource = SystemTimeSource.INSTANCE;
        final long systemBefore = System.nanoTime();

        // When: calling nanoTime()
        final long sourceTime = timeSource.nanoTime();
        final long systemAfter = System.nanoTime();

        // Then: returned time should be within system time window
        assertTrue(sourceTime >= systemBefore, "nanoTime should be >= system time before call");
        assertTrue(sourceTime <= systemAfter, "nanoTime should be <= system time after call");
    }

    @Test
    @DisplayName("should return monotonically increasing time values")
    void testMonotonicity() {
        // Given: SystemTimeSource instance
        final SystemTimeSource timeSource = SystemTimeSource.INSTANCE;

        // When: calling nanoTime() multiple times
        final long time1 = timeSource.nanoTime();
        final long time2 = timeSource.nanoTime();
        final long time3 = timeSource.nanoTime();

        // Then: subsequent calls should return equal or greater values
        assertTrue(time2 >= time1, "Second call should return time >= first call");
        assertTrue(time3 >= time2, "Third call should return time >= second call");
    }

    @Test
    @DisplayName("should measure elapsed time correctly")
    void testElapsedTimeMeasurement() throws InterruptedException {
        // Given: SystemTimeSource instance
        final SystemTimeSource timeSource = SystemTimeSource.INSTANCE;
        final long startTime = timeSource.nanoTime();

        // When: waiting for a short duration
        Thread.sleep(10); // Sleep for at least 10 milliseconds
        final long endTime = timeSource.nanoTime();

        // Then: elapsed time should be positive and reasonable
        final long elapsedNanos = endTime - startTime;
        assertTrue(elapsedNanos > 0, "Elapsed time should be positive");
        assertTrue(elapsedNanos >= 10_000_000, "Elapsed time should be at least 10ms (10,000,000ns)");
        assertTrue(elapsedNanos < 1_000_000_000, "Elapsed time should be less than 1 second (reasonable upper bound)");
    }

    @Test
    @DisplayName("should be thread-safe when accessed concurrently")
    @SuppressWarnings("ObjectAllocationInLoop")
    void testThreadSafety() throws InterruptedException {
        // Given: SystemTimeSource instance and multiple threads
        final SystemTimeSource timeSource = SystemTimeSource.INSTANCE;
        final int threadCount = 10;
        final Thread[] threads = new Thread[threadCount];
        final long[] results = new long[threadCount];

        // When: multiple threads call nanoTime() concurrently
        for (int i = 0; i < threadCount; i++) {
            threads[i] = createTimeSourceThread(timeSource, results, i);
            threads[i].start();
        }

        // Wait for all threads to complete
        for (final Thread thread : threads) {
            thread.join();
        }

        // Then: all results should be valid (non-negative)
        for (final long result : results) {
            assertTrue(result >= 0, "Result from thread should be non-negative");
        }
    }

    private static Thread createTimeSourceThread(final SystemTimeSource timeSource, final long[] results, final int index) {
        return new Thread(() -> results[index] = timeSource.nanoTime());
    }

    @Test
    @DisplayName("should consistently return same singleton across calls")
    void testSingletonConsistency() {
        // Given: Multiple references to SystemTimeSource.INSTANCE
        final SystemTimeSource ref1 = SystemTimeSource.INSTANCE;
        final SystemTimeSource ref2 = SystemTimeSource.INSTANCE;
        final SystemTimeSource ref3 = SystemTimeSource.INSTANCE;

        // When: checking object identity
        // Then: all references should point to the same object
        assertSame(ref1, ref2, "First and second references should be same object");
        assertSame(ref2, ref3, "Second and third references should be same object");
        assertSame(ref1, ref3, "First and third references should be same object");
    }
}

