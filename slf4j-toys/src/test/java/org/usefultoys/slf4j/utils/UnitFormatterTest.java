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
package org.usefultoys.slf4j.utils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.usefultoys.slf4j.SessionConfig;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.of;

/**
 *
 * @author Daniel
 */
class UnitFormatterTest {

    private static final int[] FACTORS = {1000, 1000, 1000};
    private static final String[] UNITS = {"A", "B", "C"};


    private static Locale originalLocale;

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeAll
    public static void setUpLocale() {
        // Set the default locale to English for consistent formatting
        originalLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
    }

    @AfterAll
    public static void tearDownLocale() {
        // Reset the default locale to the system default
        Locale.setDefault(originalLocale);
    }

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
    public void testLongUnit(final long value, final String expected) {
        assertEquals(expected, UnitFormatter.longUnit(value, UNITS, FACTORS));
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
            of(1249, "1.2B"),
            of(1250, "1.3B"),
            of(1251, "1.3B"),
            of(1299, "1.3B"),
            of(1300, "1.3B"),
            of(1301, "1.3B"),
            of(1349, "1.3B"),
            of(4900, "4.9B"),
            of(4949, "4.9B"),
            of(4990, "5.0B"),
            of(5000, "5.0B"),
            of(5010, "5.0B"),
            of(5050, "5.1B"),
            of(999900, "999.9B"),
            of(1000000, "1000.0B"),
            of(1100000, "1.1C")
        );
    }

    @ParameterizedTest
    @MethodSource("provideLongUnitWithLongParametersTestCases")
    public void testLongUnitWithLongParameters(final double value, final String expected) {
        assertEquals(expected, UnitFormatter.doubleUnit(value, UNITS, FACTORS));
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> provideBytesTestCases() {
        return Stream.of(
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
    void testBytes(final long value, final String expected) {
        assertEquals(expected, UnitFormatter.bytes(value));
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
    void testNanosecondsLong(final long value, final String expected) {
        assertEquals(expected, UnitFormatter.nanoseconds(value));
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
    void testNanosecondsDouble(final double value, final String expected) {
        assertEquals(expected, UnitFormatter.nanoseconds(value));
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
    void testIterationsPerSecond(final double value, final String expected) {
        assertEquals(expected, UnitFormatter.iterationsPerSecond(value));
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
    void testIterationsPerSecond(final long value, final String expected) {
        assertEquals(expected, UnitFormatter.iterations(value));
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
    public void testTimeUnit(final long value, final String expected) {
        assertEquals(expected, UnitFormatter.nanoseconds(value));
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
    public void testDoubleTimeUnit(final float value, final String expected) {
        assertEquals(expected, UnitFormatter.nanoseconds(value));
    }
}
