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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.of;

/**
 * Unit tests for {@link UnitFormatter}.
 * <p>
 * Tests validate that UnitFormatter correctly formats various numeric values
 * with appropriate units for bytes, time, iterations, and custom units.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Custom Units:</b> Tests formatting of long and double values with custom unit arrays</li>
 *   <li><b>Byte Units:</b> Verifies formatting of byte values with appropriate units (B, KB, MB, etc.)</li>
 *   <li><b>Time Units:</b> Covers formatting of time values in nanoseconds, microseconds, milliseconds, seconds</li>
 *   <li><b>Iteration Units:</b> Tests formatting of iteration counts with appropriate units, including G suffix for large values and M-to-G boundary transition</li>
 *   <li><b>Negative Values:</b> Verifies formatting of negative long and double values, which stay in the first unit</li>
 *   <li><b>Edge Cases:</b> Ensures correct handling of zero, negative, large values, extreme values (Long.MAX_VALUE, Double.MAX_VALUE, Long.MIN_VALUE), and unit boundary transitions</li>
 *   <li><b>Append Variants:</b> {@code bytes}/{@code appendBytes} are dual-covered (both a {@code String}-returning
 *       and an {@code append*(StringBuilder, ...)} test per scenario); durations, iterations and throughput are
 *       {@code append}-only, since {@code UnitFormatter} no longer exposes {@code String}-returning overloads for
 *       those units, plus a dedicated check that append preserves pre-existing buffer content</li>
 * </ul>
 */
@ValidateCharset
@WithLocale("en")
class UnitFormatterTest {

    private static final int[] FACTORS = {1000, 1000, 1000};
    private static final String[] UNITS = {"A", "B", "C"};

    static Stream<org.junit.jupiter.params.provider.Arguments> provideLongUnitTestCases() {
        return Stream.of(
            of(0L, "0A"),
            of(1L, "1A"),
            of(9L, "9A"),
            of(10L, "10A"),
            of(11L, "11A"),
            of(99L, "99A"),
            of(100L, "100A"),
            of(101L, "101A"),
            of(999L, "999A"),
            of(1000L, "1000A"),
            of(1001L, "1001A"),
            of(1099L, "1099A"),
            of(1100L, "1.1B"),
            of(1101L, "1.1B"),
            of(1149L, "1.1B"),
            of(1150L, "1.2B"),
            of(1151L, "1.2B"),
            of(1199L, "1.2B"),
            of(1200L, "1.2B"),
            of(1201L, "1.2B"),
            of(1249L, "1.2B"),
            of(1250L, "1.3B"),
            of(1251L, "1.3B"),
            of(1299L, "1.3B"),
            of(1300L, "1.3B"),
            of(1301L, "1.3B"),
            of(1349L, "1.3B"),
            of(4900L, "4.9B"),
            of(4949L, "4.9B"),
            of(4990L, "5.0B"),
            of(5000L, "5.0B"),
            of(5010L, "5.0B"),
            of(5050L, "5.1B"),
            of(999900L, "999.9B"),
            of(1000000L, "1000.0B"),
            of(1100000L, "1.1C")
        );
    }

    @ParameterizedTest
    @MethodSource("provideLongUnitTestCases")
    @DisplayName("should format long values with custom units correctly")
    void shouldFormatLongValueWithCustomUnitsCorrectly(final long value, final String expected) {
        // Given: a long value and custom units array
        // When: longUnit is called
        final String result = UnitFormatter.longUnit(value, UNITS, FACTORS);
        // Then: should return correctly formatted value with unit
        assertEquals(expected, result, "should format value " + value + " as " + expected);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> provideLongUnitInvalidTestCases() {
        return Stream.of(
            of(-1L, "?A"),
            of(-100L, "?A"),
            of(-999L, "?A"),
            of(-1000L, "?A"),
            of(-1099L, "?A"),
            of(Long.MIN_VALUE, "?A")
        );
    }

    @ParameterizedTest
    @MethodSource("provideLongUnitInvalidTestCases")
    @DisplayName("should format negative long values as invalid marker")
    void shouldFormatNegativeLongValuesAsInvalidMarker(final long value, final String expected) {
        // Given: a negative long value
        // When: longUnit is called
        final String result = UnitFormatter.longUnit(value, UNITS, FACTORS);
        // Then: should return "?" followed by the base unit, as negative values are not supported
        assertEquals(expected, result, "should format value " + value + " as " + expected);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> provideLongUnitWithLongParametersTestCases() {
        return Stream.of(
            of(0, "0A"),
            of(1, "1.0A"),
            of(1.1, "1.1A"),
            of(1.11, "1.1A"),
            of(9, "9.0A"),
            of(10, "10.0A"),
            of(11, "11.0A"),
            of(99, "99.0A"),
            of(100, "100.0A"),
            of(101, "101.0A"),
            of(999, "999.0A"),
            of(1000, "1000.0A"),
            of(1001, "1001.0A"),
            of(1099, "1099.0A"),
            of(1100, "1.1B"),
            of(1101, "1.1B"),
            of(1149, "1.1B"),
            of(1150, "1.2B"),
            of(1151, "1.2B"),
            of(1199, "1.2B"),
            of(1200, "1.2B"),
            of(1201, "1.2B"),
            of(1249L, "1.2B"),
            of(1250L, "1.3B"),
            of(1251L, "1.3B"),
            of(1299L, "1.3B"),
            of(1300L, "1.3B"),
            of(1301L, "1.3B"),
            of(1349L, "1.3B"),
            of(4900L, "4.9B"),
            of(4949L, "4.9B"),
            of(4990L, "5.0B"),
            of(5000L, "5.0B"),
            of(5010L, "5.0B"),
            of(5050L, "5.1B"),
            of(999900L, "999.9B"),
            of(1000000L, "1000.0B"),
            of(1100000L, "1.1C")
        );
    }

    @ParameterizedTest
    @MethodSource("provideLongUnitWithLongParametersTestCases")
    @DisplayName("should format double values with custom units correctly")
    void shouldFormatDoubleValueWithCustomUnitsCorrectly(final double value, final String expected) {
        // Given: a double value and custom units array
        // When: doubleUnit is called
        final StringBuilder sb = new StringBuilder();
        UnitFormatter.doubleUnit(sb, value, UNITS, FACTORS);
        // Then: should append correctly formatted value with unit
        assertEquals(expected, sb.toString(), "should format value " + value + " as " + expected);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> provideDoubleUnitInvalidTestCases() {
        return Stream.of(
            of(-0.0, "0A"),
            of(-1.0, "?A"),
            of(-999.0, "?A"),
            of(-1000.0, "?A"),
            of(-1099.0, "?A"),
            of(Double.NaN, "?A"),
            of(Double.POSITIVE_INFINITY, "?A"),
            of(Double.NEGATIVE_INFINITY, "?A")
        );
    }

    @ParameterizedTest
    @MethodSource("provideDoubleUnitInvalidTestCases")
    @DisplayName("should format invalid double values as invalid marker")
    void shouldFormatInvalidDoubleValuesAsInvalidMarker(final double value, final String expected) {
        // Given: an invalid double value (negative, NaN or infinite)
        // When: doubleUnit is called
        final StringBuilder sb = new StringBuilder();
        UnitFormatter.doubleUnit(sb, value, UNITS, FACTORS);
        // Then: should append "?" followed by the base unit, as invalid values are not supported
        assertEquals(expected, sb.toString(), "should format value " + value + " as " + expected);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> provideBytesTestCases() {
        return Stream.of(
            of(0, "0B"),
            of(500, "500B"),
            of(1000, "1000B"),
            of(1500, "1.5kB"),
            of(1_000_000, "1000.0kB"),
            of(1_500_000, "1.5MB"),
            of(1_000_000_000, "1000.0MB"),
            of(1_000_500_000, "1000.5MB")
        );
    }

    @ParameterizedTest
    @MethodSource("provideBytesTestCases")
    @DisplayName("should format byte sizes with correct unit suffixes")
    void shouldFormatByteSizesWithCorrectUnitSuffixes(final long value, final String expected) {
        // Given: a byte value
        // When: bytes formatter is called
        final String result = UnitFormatter.bytes(value);
        // Then: should return value formatted with B, kB, MB suffixes
        assertEquals(expected, result, "should format " + value + " bytes as " + expected);
    }

    @ParameterizedTest
    @MethodSource("provideBytesTestCases")
    @DisplayName("should append byte sizes with correct unit suffixes")
    void shouldAppendByteSizesWithCorrectUnitSuffixes(final long value, final String expected) {
        // Given: a byte value and a StringBuilder
        // When: appendBytes is called
        final StringBuilder sb = new StringBuilder();
        UnitFormatter.appendBytes(sb, value);
        // Then: should append value formatted with B, kB, MB suffixes
        assertEquals(expected, sb.toString(), "should append " + value + " bytes as " + expected);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> provideNanosecondsLongTestCases() {
        return Stream.of(
            of(500, "500ns"),
            of(1000, "1000ns"),
            of(1500, "1.5us"),
            of(1_000_000, "1000.0us"),
            of(1_040_000, "1040.0us"),
            of(1_050_000, "1050.0us"),
            of(1_305_000, "1.3ms"),
            of(1_500_000, "1.5ms"),
            of(1_550_000, "1.6ms"),
            of(1_000_000_000, "1000.0ms"),
            of(1_000_500_000, "1000.5ms"),
            of(1_000_040_000, "1000.0ms"),
            of(1_000_050_000, "1000.1ms"),
            of(1_000_005_000, "1000.0ms")
        );
    }

    @ParameterizedTest
    @MethodSource("provideNanosecondsLongTestCases")
    @DisplayName("should append nanoseconds (long) with correct time unit suffixes")
    void shouldAppendNanosecondsLongWithCorrectTimeUnitSuffixes(final long value, final String expected) {
        // Given: a nanosecond value as long and a StringBuilder
        // When: appendNanoseconds is called
        final StringBuilder sb = new StringBuilder();
        UnitFormatter.appendNanoseconds(sb, value);
        // Then: should append value formatted with ns, us, ms suffixes
        assertEquals(expected, sb.toString(), "should append " + value + "ns as " + expected);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> provideNanosecondsDoubleTestCases() {
        return Stream.of(
            of(500.0, "500.0ns"),
            of(1000.0, "1000.0ns"),
            of(1500.0, "1.5us"),
            of(1_000_000.0, "1000.0us"),
            of(1_000_000_000.0, "1000.0ms")
        );
    }

    @ParameterizedTest
    @MethodSource("provideNanosecondsDoubleTestCases")
    @DisplayName("should append nanoseconds (double) with correct time unit suffixes")
    void shouldAppendNanosecondsDoubleWithCorrectTimeUnitSuffixes(final double value, final String expected) {
        // Given: a nanosecond value as double and a StringBuilder
        // When: appendNanoseconds is called
        final StringBuilder sb = new StringBuilder();
        UnitFormatter.appendNanoseconds(sb, value);
        // Then: should append value formatted with ns, us, ms suffixes
        assertEquals(expected, sb.toString(), "should append " + value + "ns as " + expected);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> provideIterationsPerSecondTestCases() {
        return Stream.of(
            of(0.4, "0.4/s"),
            of(0.5, "0.5/s"),
            of(0.6, "0.6/s"),
            of(0.9, "0.9/s"),
            of(1.0, "1.0/s"),
            of(2.0, "2.0/s"),
            of(2.89, "2.9/s"),
            of(2.9, "2.9/s"),
            of(3.0, "3.0/s"),
            of(3.1, "3.1/s"),
            of(3.11, "3.1/s"),
            of(3.111, "3.1/s"),
            of(12.0, "12.0/s"),
            of(120.0, "120.0/s"),
            of(1_200.0, "1.2k/s"),
            of(12_000.0, "12.0k/s"),
            of(120_000.0, "120.0k/s"),
            of(1_200_000.0, "1.2M/s"),
            of(12_000_000.0, "12.0M/s"),
            of(120_000_000.0, "120.0M/s")
        );
    }

    @ParameterizedTest
    @MethodSource("provideIterationsPerSecondTestCases")
    @DisplayName("should append iterations per second with correct unit suffixes")
    void shouldAppendIterationsPerSecondWithCorrectUnitSuffixes(final double value, final String expected) {
        // Given: an iterations per second value and a StringBuilder
        // When: appendIterationsPerSecond is called
        final StringBuilder sb = new StringBuilder();
        UnitFormatter.appendIterationsPerSecond(sb, value);
        // Then: should append value formatted with /s, k/s, M/s suffixes
        assertEquals(expected, sb.toString(), "should append " + value + "/s as " + expected);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> provideIterationsTestCases() {
        return Stream.of(
            of(0, "0"),
            of(1, "1"),
            of(12, "12"),
            of(120, "120"),
            of(1_200, "1.2k"),
            of(12_000, "12.0k"),
            of(120_000, "120.0k"),
            of(1_200_000, "1.2M"),
            of(12_000_000, "12.0M"),
            of(120_000_000, "120.0M")
        );
    }

    @ParameterizedTest
    @MethodSource("provideIterationsTestCases")
    @DisplayName("should append iteration counts with correct unit suffixes")
    void shouldAppendIterationCountsWithCorrectUnitSuffixes(final long value, final String expected) {
        // Given: an iteration count value and a StringBuilder
        // When: appendIterations is called
        final StringBuilder sb = new StringBuilder();
        UnitFormatter.appendIterations(sb, value);
        // Then: should append value formatted with k, M suffixes
        assertEquals(expected, sb.toString(), "should append " + value + " iterations as " + expected);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> provideIterationsLargeTestCases() {
        return Stream.of(
            of(1_100_000_000L, "1.1G"),
            of(1_500_000_000L, "1.5G"),
            of(1_000_000_000_000L, "1000.0G")
        );
    }

    @ParameterizedTest
    @MethodSource("provideIterationsLargeTestCases")
    @DisplayName("should append large iteration counts with G unit suffix without overflow")
    void shouldAppendLargeIterationCountsWithGUnitSuffixWithoutOverflow(final long value, final String expected) {
        // Given: a large iteration count value that exceeds the M unit range, and a StringBuilder
        // When: appendIterations is called
        final StringBuilder sb = new StringBuilder();
        UnitFormatter.appendIterations(sb, value);
        // Then: should append value formatted with G suffix, not throw ArrayIndexOutOfBoundsException
        assertEquals(expected, sb.toString(), "should append " + value + " iterations as " + expected);
    }

    @Test
    @DisplayName("should not throw ArrayIndexOutOfBoundsException for Long.MAX_VALUE iterations via appendIterations")
    void shouldNotThrowForLongMaxValueIterationsViaAppend() {
        // Given: the maximum possible long iteration count and a StringBuilder
        // When: appendIterations is called
        final StringBuilder sb = new StringBuilder();
        UnitFormatter.appendIterations(sb, Long.MAX_VALUE);
        // Then: should append a string ending with G suffix, not throw ArrayIndexOutOfBoundsException
        assertTrue(sb.toString().endsWith("G"), "should end with G suffix, got: " + sb);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> provideIterationsPerSecondLargeTestCases() {
        return Stream.of(
            of(1.1E9, "1.1G/s"),
            of(1.5E9, "1.5G/s"),
            of(1.0E12, "1000.0G/s")
        );
    }

    @ParameterizedTest
    @MethodSource("provideIterationsPerSecondLargeTestCases")
    @DisplayName("should append large iterations per second with G/s unit suffix without overflow")
    void shouldAppendLargeIterationsPerSecondWithGPerSecondUnitSuffixWithoutOverflow(final double value, final String expected) {
        // Given: a large iterations per second value that exceeds the M/s unit range, and a StringBuilder
        // When: appendIterationsPerSecond is called
        final StringBuilder sb = new StringBuilder();
        UnitFormatter.appendIterationsPerSecond(sb, value);
        // Then: should append value formatted with G/s suffix, not throw ArrayIndexOutOfBoundsException
        assertEquals(expected, sb.toString(), "should append " + value + "/s as " + expected);
    }

    @Test
    @DisplayName("should not throw ArrayIndexOutOfBoundsException for Double.MAX_VALUE iterations per second via appendIterationsPerSecond")
    void shouldNotThrowForDoubleMaxValueIterationsPerSecondViaAppend() {
        // Given: the maximum possible double iterations per second value and a StringBuilder
        // When: appendIterationsPerSecond is called
        final StringBuilder sb = new StringBuilder();
        UnitFormatter.appendIterationsPerSecond(sb, Double.MAX_VALUE);
        // Then: should append a string ending with G/s suffix, not throw ArrayIndexOutOfBoundsException
        assertTrue(sb.toString().endsWith("G/s"), "should end with G/s suffix, got: " + sb);
    }

    @Test
    @DisplayName("should append zero iterations per second as 0/s")
    void shouldAppendZeroIterationsPerSecondAsZero() {
        // Given: zero iterations per second and a StringBuilder
        // When: appendIterationsPerSecond is called
        final StringBuilder sb = new StringBuilder();
        UnitFormatter.appendIterationsPerSecond(sb, 0.0);
        // Then: should append "0/s" via the early return for zero
        assertEquals("0/s", sb.toString(), "should append 0.0/s as 0/s");
    }

    @Test
    @DisplayName("should not throw ArrayIndexOutOfBoundsException for Long.MAX_VALUE bytes")
    void shouldNotThrowForLongMaxValueBytes() {
        // Given: the maximum possible long byte count
        // When: bytes formatter is called
        final String result = UnitFormatter.bytes(Long.MAX_VALUE);
        // Then: should return a string ending with GB suffix, not throw ArrayIndexOutOfBoundsException
        assertTrue(result.endsWith("GB"), "should end with GB suffix, got: " + result);
    }

    @Test
    @DisplayName("should not throw ArrayIndexOutOfBoundsException for Long.MAX_VALUE bytes via appendBytes")
    void shouldNotThrowForLongMaxValueBytesViaAppend() {
        // Given: the maximum possible long byte count and a StringBuilder
        // When: appendBytes is called
        final StringBuilder sb = new StringBuilder();
        UnitFormatter.appendBytes(sb, Long.MAX_VALUE);
        // Then: should append a string ending with GB suffix, not throw ArrayIndexOutOfBoundsException
        assertTrue(sb.toString().endsWith("GB"), "should end with GB suffix, got: " + sb);
    }

    @Test
    @DisplayName("should not throw ArrayIndexOutOfBoundsException for Long.MAX_VALUE nanoseconds via appendNanoseconds")
    void shouldNotThrowForLongMaxValueNanosecondsViaAppend() {
        // Given: the maximum possible long nanosecond duration and a StringBuilder
        // When: appendNanoseconds is called
        final StringBuilder sb = new StringBuilder();
        UnitFormatter.appendNanoseconds(sb, Long.MAX_VALUE);
        // Then: should append a string ending with h suffix, not throw ArrayIndexOutOfBoundsException
        assertTrue(sb.toString().endsWith("h"), "should end with h suffix, got: " + sb);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> provideIterationsMToGBoundaryTestCases() {
        return Stream.of(
            of(1_099_000_000L, "1099.0M"),
            of(1_100_000_000L, "1.1G")
        );
    }

    @ParameterizedTest
    @MethodSource("provideIterationsMToGBoundaryTestCases")
    @DisplayName("should correctly transition from M to G unit at the boundary via appendIterations")
    void shouldCorrectlyTransitionFromMToGUnitAtBoundaryViaAppend(final long value, final String expected) {
        // Given: iteration counts near the M-to-G boundary (limit = 1100M) and a StringBuilder
        // When: appendIterations is called
        final StringBuilder sb = new StringBuilder();
        UnitFormatter.appendIterations(sb, value);
        // Then: should append the correct unit based on the boundary threshold
        assertEquals(expected, sb.toString(), "should append " + value + " iterations as " + expected);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> provideIterationsPerSecondMToGBoundaryTestCases() {
        return Stream.of(
            of(1_099_000_000.0, "1099.0M/s"),
            of(1_100_000_000.0, "1.1G/s")
        );
    }

    @ParameterizedTest
    @MethodSource("provideIterationsPerSecondMToGBoundaryTestCases")
    @DisplayName("should correctly transition from M/s to G/s unit at the boundary via appendIterationsPerSecond")
    void shouldCorrectlyTransitionFromMPerSecondToGPerSecondUnitAtBoundaryViaAppend(final double value, final String expected) {
        // Given: iterations per second near the M/s-to-G/s boundary (limit = 1100M/s) and a StringBuilder
        // When: appendIterationsPerSecond is called
        final StringBuilder sb = new StringBuilder();
        UnitFormatter.appendIterationsPerSecond(sb, value);
        // Then: should append the correct unit based on the boundary threshold
        assertEquals(expected, sb.toString(), "should append " + value + "/s as " + expected);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> provideTimeUnitTestCases() {
        return Stream.of(
            of(0L, "0ns"),
            of(1L, "1ns"),
            of(999L, "999ns"),
            of(1000L, "1000ns"),
            of(1001L, "1001ns"),
            of(1100L, "1.1us"),
            of(1000000000L, "1000.0ms"),
            of(1100000000L, "1.1s"),
            of(60000000000L, "60.0s"),
            of(61000000000L, "61.0s"),
            of(66000000000L, "1.1m"),
            of(600000000000L, "10.0m"),
            of(3600000000000L, "60.0m"),
            of(4400000000000L, "1.2h")
        );
    }

    @ParameterizedTest
    @MethodSource("provideTimeUnitTestCases")
    @DisplayName("should append time duration with comprehensive time unit conversion")
    void shouldAppendTimeDurationWithComprehensiveTimeUnitConversion(final long value, final String expected) {
        // Given: a time duration in nanoseconds and a StringBuilder
        // When: appendNanoseconds is called for comprehensive time conversion
        final StringBuilder sb = new StringBuilder();
        UnitFormatter.appendNanoseconds(sb, value);
        // Then: should append value formatted with ns, us, ms, s, m, h suffixes
        assertEquals(expected, sb.toString(), "should append " + value + "ns as " + expected);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> provideDoubleTimeUnitTestCases() {
        return Stream.of(
            of(0.0f, "0ns"),
            of(1.0f, "1.0ns"),
            of(999.0f, "999.0ns"),
            of(1000.0f, "1000.0ns"),
            of(1001.0f, "1001.0ns"),
            of(1100.0f, "1.1us"),
            of(1000000000.0f, "1000.0ms"),
            of(1100000000.0f, "1.1s"),
            of(60000000000.0f, "60.0s"),
            of(61000000000.0f, "61.0s"),
            of(66000000000.0f, "1.1m"),
            of(600000000000.0f, "10.0m"),
            of(3600000000000.0f, "60.0m"),
            of(4400000000000.0f, "1.2h")
        );
    }

    @ParameterizedTest
    @MethodSource("provideDoubleTimeUnitTestCases")
    @DisplayName("should append time duration (double) with comprehensive time unit conversion")
    void shouldAppendTimeDurationDoubleWithComprehensiveTimeUnitConversion(final float value, final String expected) {
        // Given: a time duration in nanoseconds as double and a StringBuilder
        // When: appendNanoseconds is called for comprehensive time conversion
        final StringBuilder sb = new StringBuilder();
        UnitFormatter.appendNanoseconds(sb, value);
        // Then: should append value formatted with ns, us, ms, s, m, h suffixes
        assertEquals(expected, sb.toString(), "should append " + value + "ns as " + expected);
    }

    @Test
    @DisplayName("should append to existing StringBuilder content instead of replacing it")
    void shouldAppendToExistingStringBuilderContent() {
        // Given: a StringBuilder pre-filled with unrelated content
        final StringBuilder sb = new StringBuilder("prefix-");
        // When: each append* method is called in sequence
        UnitFormatter.appendBytes(sb, 1500);
        sb.append(' ');
        UnitFormatter.appendNanoseconds(sb, 1500L);
        sb.append(' ');
        UnitFormatter.appendNanoseconds(sb, 1500.0);
        sb.append(' ');
        UnitFormatter.appendIterations(sb, 1_200L);
        sb.append(' ');
        UnitFormatter.appendIterationsPerSecond(sb, 1_200.0);
        // Then: the pre-existing content is preserved and each result is appended, not replacing the buffer
        assertEquals("prefix-1.5kB 1.5us 1.5us 1.2k 1.2k/s", sb.toString());
    }

    @Test
    @DisplayName("should maintain unit array length invariant")
    void shouldMaintainUnitArrayLengthInvariant() {
        // Given: predefined unit and factor arrays
        // When/Then: each units array has exactly one more element than its corresponding factors array
        assertEquals(UnitFormatter.TIME_FACTORS.length + 1, UnitFormatter.TIME_UNITS.length,
            "TIME_UNITS length should be TIME_FACTORS length + 1");
        assertEquals(UnitFormatter.MEMORY_FACTORS.length + 1, UnitFormatter.MEMORY_UNITS.length,
            "MEMORY_UNITS length should be MEMORY_FACTORS length + 1");
        assertEquals(UnitFormatter.ITERATIONS_FACTORS.length + 1, UnitFormatter.ITERATIONS_UNITS.length,
            "ITERATIONS_UNITS length should be ITERATIONS_FACTORS length + 1");
        assertEquals(UnitFormatter.ITERATIONS_PER_TIME_FACTORS.length + 1, UnitFormatter.ITERATIONS_PER_TIME_UNITS.length,
            "ITERATIONS_PER_TIME_UNITS length should be ITERATIONS_PER_TIME_FACTORS length + 1");
    }
}

