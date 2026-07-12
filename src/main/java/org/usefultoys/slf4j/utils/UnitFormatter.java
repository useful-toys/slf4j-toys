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
package org.usefultoys.slf4j.utils;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Utility class that provides methods to format numbers by rounding them to a unit,
 * thereby reducing their string representation.
 *
 * <p>This class supports formatting for time durations, memory sizes, and iteration-related values.
 *
 * <p>For example, it can convert large numbers into human-readable formats such as "1.2kB" or "3.4ms".
 *
 * <p>Negative values, {@code NaN} and infinite values are not supported and are rendered as "?"
 * followed by the base unit (e.g., "?B", "?ns", "?/s").
 *
 * <p><b>Formatting is locale-independent by design.</b> Decimal values are rendered with scaled-integer
 * arithmetic and a fixed {@code '.'} decimal separator (US style), never consulting the session locale.
 * This favors speed and allocation on the instrumentation hot path over internationalization, which
 * carries no analytical value for diagnostic output. See
 * {@code doc/TDR-0040-locale-independent-readable-messages.md} and
 * {@code doc/TDR-0039-accept-scaled-integer-rounding-in-json5-serialization.md}.
 *
 * <p>Callers on the hot path should prefer the {@code append*(StringBuilder, ...)} variants, which write
 * directly into the caller's buffer and avoid the intermediate {@link String} that the {@link String}-returning
 * methods allocate.
 *
 * @author Daniel Felix Ferber
 * @author Co-authored-by: GitHub Copilot using OpenCode Go / Kimi K2.7 Code
 */
@UtilityClass
public final class UnitFormatter {

    final int[] TIME_FACTORS = {1000, 1000, 1000, 60, 60};
    final String[] TIME_UNITS = {"ns", "us", "ms", "s", "m", "h"};
    final String[] MEMORY_UNITS = {"B", "kB", "MB", "GB"};
    final int[] MEMORY_FACTORS = {1000, 1000, 1000};
    final String[] ITERATIONS_PER_TIME_UNITS = {"/s", "k/s", "M/s", "G/s"};
    final int[] ITERATIONS_PER_TIME_FACTORS = {1000, 1000, 1000};
    final String[] ITERATIONS_UNITS = {"", "k", "M", "G"};
    final int[] ITERATIONS_FACTORS = {1000, 1000, 1000};

    /**
     * Formats a long integer value into a human-readable string with appropriate units.
     * This method is used internally by the public formatting methods and by unit tests.
     *
     * <p>Negative values are not supported and are rendered as "?" followed by the base unit.
     *
     * @param value The long integer value to format.
     * @param units An array of unit strings (e.g., "B", "kB", "MB").
     * @param factors An array of factors for unit conversion (e.g., 1000, 1000, 1000).
     * @return A formatted string representing the value with units, or "?" followed by the base unit
     *         when the value is negative.
     */
    String longUnit(final long value, @NonNull final String[] units, @NonNull final int[] factors) {
        final StringBuilder sb = new StringBuilder(12);
        longUnit(sb, value, units, factors);
        return sb.toString();
    }

    /**
     * Appends a human-readable representation of a long integer value with appropriate units directly
     * into the provided {@link StringBuilder}, avoiding the intermediate {@link String} allocation of
     * {@link #longUnit(long, String[], int[])}.
     *
     * <p>Negative values are not supported and are rendered as "?" followed by the base unit.
     *
     * @param sb The StringBuilder that receives the formatted representation.
     * @param value The long integer value to format.
     * @param units An array of unit strings (e.g., "B", "kB", "MB").
     * @param factors An array of factors for unit conversion (e.g., 1000, 1000, 1000).
     */
    @SuppressWarnings("AssignmentToMethodParameter")
    void longUnit(final StringBuilder sb, long value, @NonNull final String[] units, @NonNull final int[] factors) {
        if (value < 0) {
            sb.append('?').append(units[0]);
            return;
        }

        int index = 0;
        final int limit = factors[index] + factors[index] / 10;
        if (value < limit) {
            sb.append(value).append(units[index]);
            return;
        }

        final int length = factors.length;
        double doubleValue = value;

        while (index < length && value >= (factors[index] + factors[index] / 10)) {
            doubleValue = value / (double) factors[index];
            value /= factors[index];
            index++;
        }
        appendScaledDecimal(sb, doubleValue, units[index]);
    }

    /**
     * A small epsilon value used for floating-point comparisons to account for precision issues.
     */
    final double EPSILON = 0.001;

    /**
     * Formats a double-precision floating-point value into a human-readable string with appropriate units.
     * This method is used internally by the public formatting methods and by unit tests.
     *
     * <p>Negative values, {@code NaN} and infinite values are not supported and are rendered as "?"
     * followed by the base unit.
     *
     * @param value The double value to format.
     * @param units An array of unit strings (e.g., "/s", "k/s", "M/s").
     * @param factors An array of factors for unit conversion (e.g., 1000, 1000, 1000).
     * @return A formatted string representing the value with units, or "?" followed by the base unit
     *         when the value is negative, {@code NaN} or infinite.
     */
    String doubleUnit(final double value, @NonNull final String[] units, @NonNull final int[] factors) {
        final StringBuilder sb = new StringBuilder(12);
        doubleUnit(sb, value, units, factors);
        return sb.toString();
    }

    /**
     * Appends a human-readable representation of a double-precision value with appropriate units directly
     * into the provided {@link StringBuilder}, avoiding the intermediate {@link String} allocation of
     * {@link #doubleUnit(double, String[], int[])}.
     *
     * <p>Negative values, {@code NaN} and infinite values are not supported and are rendered as "?"
     * followed by the base unit.
     *
     * @param sb The StringBuilder that receives the formatted representation.
     * @param value The double value to format.
     * @param units An array of unit strings (e.g., "/s", "k/s", "M/s").
     * @param factors An array of factors for unit conversion (e.g., 1000, 1000, 1000).
     */
    @SuppressWarnings("AssignmentToMethodParameter")
    void doubleUnit(final StringBuilder sb, double value, @NonNull final String[] units, @NonNull final int[] factors) {
        if (value == 0.0) {
            sb.append('0').append(units[0]);
            return;
        }
        if (value < 0.0 || Double.isNaN(value) || Double.isInfinite(value)) {
            sb.append('?').append(units[0]);
            return;
        }

        int index = 0;
        final int length = factors.length;

        while (index < length && (value + EPSILON) >= (factors[index] + factors[index] / 10.0)) {
            value /= factors[index];
            index++;
        }
        appendScaledDecimal(sb, value, units[index]);
    }

    /**
     * Appends a non-negative value rounded to one decimal place, followed by its unit, using scaled-integer
     * arithmetic and a fixed {@code '.'} decimal separator (locale-independent).
     *
     * <p>This replaces {@code String.format(locale, "%.1f%s", ...)} to remove the {@code Formatter} overhead
     * (format-string parsing, allocation, autoboxing) from the hot path. As accepted in TDR-0039 and extended
     * to human-readable messages by TDR-0040, the scaled-integer rounding may differ by one unit in the last
     * decimal from {@code %.1f} in rare tie cases; this is accepted as diagnostic measurement noise.
     *
     * <p>No overflow guard is needed: {@link Math#round(double)} saturates at {@link Long#MAX_VALUE} for the
     * extreme inputs reachable here (e.g. {@link Long#MAX_VALUE} bytes, {@link Double#MAX_VALUE} iterations/s),
     * so this never throws.
     *
     * @param sb The StringBuilder that receives the formatted representation.
     * @param value The non-negative value to render with one decimal place.
     * @param unit The unit suffix to append.
     */
    private void appendScaledDecimal(final StringBuilder sb, final double value, final String unit) {
        final long scaled = Math.round(value * 10);
        sb.append(scaled / 10).append('.').append(scaled % 10).append(unit);
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
     * Appends a human-readable byte size (B, kB, MB, GB) directly into the provided {@link StringBuilder},
     * avoiding the intermediate {@link String} of {@link #bytes(long)}.
     *
     * @param sb The StringBuilder that receives the formatted value.
     * @param value The number of bytes.
     */
    public void appendBytes(final StringBuilder sb, final long value) {
        longUnit(sb, value, MEMORY_UNITS, MEMORY_FACTORS);
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
     * Appends a human-readable duration (ns, us, ms, s, m, h) directly into the provided {@link StringBuilder},
     * avoiding the intermediate {@link String} of {@link #nanoseconds(long)}.
     *
     * @param sb The StringBuilder that receives the formatted value.
     * @param value The duration in nanoseconds.
     */
    public void appendNanoseconds(final StringBuilder sb, final long value) {
        longUnit(sb, value, TIME_UNITS, TIME_FACTORS);
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
     * Appends a human-readable duration (ns, us, ms, s, m, h) directly into the provided {@link StringBuilder},
     * avoiding the intermediate {@link String} of {@link #nanoseconds(double)}.
     *
     * @param sb The StringBuilder that receives the formatted value.
     * @param value The duration in nanoseconds.
     */
    public void appendNanoseconds(final StringBuilder sb, final double value) {
        doubleUnit(sb, value, TIME_UNITS, TIME_FACTORS);
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
     * Appends a human-readable iteration count (plain, k, M, G) directly into the provided {@link StringBuilder},
     * avoiding the intermediate {@link String} of {@link #iterations(long)}.
     *
     * @param sb The StringBuilder that receives the formatted value.
     * @param value The number of iterations.
     */
    public void appendIterations(final StringBuilder sb, final long value) {
        longUnit(sb, value, ITERATIONS_UNITS, ITERATIONS_FACTORS);
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

    /**
     * Appends a human-readable throughput (/s, k/s, M/s, G/s) directly into the provided {@link StringBuilder},
     * avoiding the intermediate {@link String} of {@link #iterationsPerSecond(double)}.
     *
     * @param sb The StringBuilder that receives the formatted value.
     * @param value The number of iterations per second.
     */
    public void appendIterationsPerSecond(final StringBuilder sb, final double value) {
        doubleUnit(sb, value, ITERATIONS_PER_TIME_UNITS, ITERATIONS_PER_TIME_FACTORS);
    }
}
