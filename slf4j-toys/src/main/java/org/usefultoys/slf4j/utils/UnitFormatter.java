/*
 * Copyright 2024 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.usefultoys.slf4j.utils;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Utility class that provides methods to format numbers by rounding them to a unit, 
 * thereby reducing their string representation.
 *
 * <p>This class supports formatting for time durations, memory sizes, and iteration-related values.</p>
 *
 * <p>For example, it can convert large numbers into human-readable formats such as "1.2kB" or "3.4ms".</p>
 *
 * @author Daniel Felix Ferber
 */
@UtilityClass
public final class UnitFormatter {

    private final int[] TIME_FACTORS = {1000, 1000, 1000, 60, 60};
    private final String[] TIME_UNITS = {"ns", "us", "ms", "s", "m", "h"};
    private final String[] MEMORY_UNITS = {"B", "kB", "MB", "GB"};
    private final int[] MEMORY_FACTORS = {1000, 1000, 1000};
    private final String[] ITERATIONS_PER_TIME_UNITS = {"/s", "k/s", "M/s"};
    private final int[] ITERATIONS_PER_TIME_FACTORS = {1000, 1000, 1000};
    private final String[] ITERATIONS_UNITS = {"", "k", "M"};
    private final int[] ITERATIONS_FACTORS = {1000, 1000, 1000};

    @SuppressWarnings("AssignmentToMethodParameter")
    String longUnit(long value, @NonNull final String[] units, @NonNull final int[] factors) {
        int index = 0;
        final int limit = factors[index] + factors[index] / 10;
        if (value < limit) {
            return String.format("%d%s", value, units[index]);
        }

        final int length = factors.length;
        double doubleValue = value;

        while (index < length && value >= (factors[index] + factors[index] / 10)) {
            doubleValue = value / (double) factors[index];
            value /= factors[index];
            index++;
        }
        return String.format("%.1f%s", doubleValue, units[index]);
    }

    final double Epsylon = 0.001;

    @SuppressWarnings("AssignmentToMethodParameter")
    String doubleUnit(double value, @NonNull final String[] units, @NonNull final int[] factors) {
        if (value == 0.0) {
            return "0" + units[0];
        }

        int index = 0;
        final int length = factors.length;

        while (index < length && (value + Epsylon) >= (factors[index] + factors[index] / 10.0)) {
            value /= factors[index];
            index++;
        }
        return String.format("%.1f%s", value, units[index]);
    }

    /**
     * Formats a number of bytes into a human-readable string with appropriate units.
     *
     * @param value The number of bytes.
     * @return A formatted string representing the value in bytes, kilobytes, megabytes, or gigabytes.
     */
    public String bytes(final long value) {
        return longUnit(value, MEMORY_UNITS, MEMORY_FACTORS);
    }

    /**
     * Formats a duration in nanoseconds into a human-readable string with appropriate time units.
     *
     * @param value The duration in nanoseconds.
     * @return A formatted string representing the value in nanoseconds, microseconds, milliseconds, seconds, minutes, or hours.
     */
    public String nanoseconds(final long value) {
        return longUnit(value, TIME_UNITS, TIME_FACTORS);
    }

    /**
     * Formats a duration in nanoseconds (as a double) into a human-readable string with appropriate time units.
     *
     * @param value The duration in nanoseconds.
     * @return A formatted string representing the value in nanoseconds, microseconds, milliseconds, seconds, minutes, or hours.
     */
    public String nanoseconds(final double value) {
        return doubleUnit(value, TIME_UNITS, TIME_FACTORS);
    }

    /**
     * Formats a number of iterations into a human-readable string with appropriate units.
     *
     * @param value The number of iterations.
     * @return A formatted string representing the value in iterations, thousands, or millions.
     */
    public String iterations(final long value) {
        return longUnit(value, ITERATIONS_UNITS, ITERATIONS_FACTORS);
    }

    /**
     * Formats a number of iterations per second (as a double) into a human-readable string with appropriate units.
     *
     * @param value The number of iterations per second.
     * @return A formatted string representing the value in iterations per second, thousands per second, or millions per second.
     */
    public String iterationsPerSecond(final double value) {
        return doubleUnit(value, ITERATIONS_PER_TIME_UNITS, ITERATIONS_PER_TIME_FACTORS);
    }
}
