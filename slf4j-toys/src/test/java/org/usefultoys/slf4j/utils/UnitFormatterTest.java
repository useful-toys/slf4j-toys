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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.of;

/**
 *
 * @author Daniel
 */
public class UnitFormatterTest {

    private static Locale originalLocal;

    public UnitFormatterTest() {
    }
    private static final int[] FACTORS = {1000, 1000, 1000};
    private static final String[] UNITS = {"A", "B", "C"};

    @BeforeAll
    public static void setUpClass() {
        // Set the default locale to English for consistent formatting
        originalLocal = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
    }

    @AfterAll
    public static void tearDownClass() {
        // Reset the default locale to the system default
        Locale.setDefault(originalLocal);
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
    public void testLongUnit(long value, String expected) {
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
    public void testLongUnitWithLongParameters(double value, String expected) {
        assertEquals(expected, UnitFormatter.doubleUnit(value, UNITS, FACTORS));
    }

    @Test
    void testBytes() {
        assertEquals("500B", UnitFormatter.bytes(500));
        assertEquals("1000B", UnitFormatter.bytes(1000));
        assertEquals("1.5kB", UnitFormatter.bytes(1500));
        assertEquals("1000.0kB", UnitFormatter.bytes(1_000_000));
        assertEquals("1000.0MB", UnitFormatter.bytes(1_000_000_000));
    }

    @Test
    void testNanosecondsLong() {
        assertEquals("500ns", UnitFormatter.nanoseconds(500));
        assertEquals("1000ns", UnitFormatter.nanoseconds(1000));
        assertEquals("1.5us", UnitFormatter.nanoseconds(1500));
        assertEquals("1000.0us", UnitFormatter.nanoseconds(1_000_000));
        assertEquals("1000.0ms", UnitFormatter.nanoseconds(1_000_000_000));
    }

    @Test
    void testNanosecondsDouble() {
        assertEquals("500.0ns", UnitFormatter.nanoseconds(500.0));
        assertEquals("1000.0ns", UnitFormatter.nanoseconds(1000.0));
        assertEquals("1.5us", UnitFormatter.nanoseconds(1500.0));
        assertEquals("1000.0us", UnitFormatter.nanoseconds(1_000_000.0));
        assertEquals("1000.0ms", UnitFormatter.nanoseconds(1_000_000_000.0));
    }
}
