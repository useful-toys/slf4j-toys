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

/**
 * Default implementation of {@link TimeSource} that delegates to {@link System#nanoTime()}.
 * <p>
 * This is the production implementation used by default in all time-sensitive components
 * of the library. It provides actual system monotonic time suitable for measuring
 * elapsed durations and detecting slow operations.
 * <p>
 * This implementation is immutable and thread-safe.
 *
 * @author Daniel Felix Ferber
 * @author Co-authored-by: GitHub Copilot using gpt-4o
 * @see TimeSource
 */
public final class SystemTimeSource implements TimeSource {

    /**
     * Singleton instance of the system time source.
     */
    public static final SystemTimeSource INSTANCE = new SystemTimeSource();

    /**
     * Private constructor to enforce singleton pattern.
     */
    private SystemTimeSource() {
    }

    /**
     * Returns the current value of the running JVM's high-resolution time source, in nanoseconds.
     * <p>
     * This method delegates directly to {@link System#nanoTime()}.
     *
     * @return the current time in nanoseconds from {@link System#nanoTime()}
     */
    @Override
    public long nanoTime() {
        return System.nanoTime();
    }
}

