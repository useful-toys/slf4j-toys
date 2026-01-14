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
 * Abstraction for time measurement, allowing deterministic testing of time-dependent behavior.
 * <p>
 * This interface enables the {@link org.usefultoys.slf4j.meter.Meter} and {@link EventData} classes
 * to use pluggable time sources, making it possible to test time-sensitive functionality
 * (such as slowness detection, progress throttling, and duration calculations) without
 * depending on actual system time or thread delays.
 * <p>
 * The default implementation uses {@link System#nanoTime()}, but tests can provide
 * custom implementations with controllable time progression.
 * <p>
 * This abstraction follows the Clock Abstraction Pattern as documented in
 * TDR-0032: Clock Abstraction Pattern for Deterministic Time-Based Testing.
 *
 * @author Daniel Felix Ferber
 * @author Co-authored-by: GitHub Copilot using gpt-4o
 * @see SystemTimeSource
 * @see EventData#setTimeSource(TimeSource)
 */
public interface TimeSource {

    /**
     * Returns the current time in nanoseconds.
     * <p>
     * The returned value should be monotonically increasing and suitable for
     * measuring elapsed time intervals. The absolute value has no specific meaning;
     * only differences between successive calls are significant.
     * <p>
     * For production use, this typically delegates to {@link System#nanoTime()}.
     * For testing, this can return controllable values.
     *
     * @return the current time in nanoseconds
     */
    long nanoTime();
}

