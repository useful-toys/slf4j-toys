/*
 * Copyright 2013 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.danielferber.slf4jtoys.slf4j.utils;

/**
 *
 * @author Daniel Felix Ferber
 */
public final class UnitFormatter {

    private UnitFormatter() {
    }

    private static final int[] TIME_FACTORS = new int[]{1000, 1000, 1000, 60, 60};
    private static final String[] TIME_UNITS = new String[]{"ns", "us", "ms", "s", "m", "h"};
    private static final String[] MEMORY_UNITS = new String[]{"B", "kB", "MB", "GB"};
    private static final int[] MEMORY_FACTORS = new int[]{1000, 1000, 1000};
    private static final String[] ITERATIONS_PER_TIME_UNITS = new String[]{"/s", "k/s", "M/s"};
    private static final int[] ITERATIONS_PER_TIME_FACTORS = new int[]{1000, 1000, 1000};
    private static final String[] ITERATIONS_UNITS = new String[]{"", "k", "M"};
    private static final int[] ITERATIONS_FACTORS = new int[]{1000, 1000, 1000};

    static String longUnit(long value, String[] units, int[] factors) {
        int index = 0;
        int limit = factors[index] + factors[index] / 10;
        if (value < limit) {
            return String.format("%d%s", value, units[index]);
        }

        int last = units.length - 1;
        double doubleValue;
        do {
            doubleValue = (double) value / (double) factors[index];
            value /= factors[index];
            limit = factors[index];
            index++;
        } while (index != last && value >= limit);

        return String.format("%.1f%s", doubleValue, units[index]);
    }

    static String doubleUnit(double value, String[] units, int[] factors) {
        if (value == 0.0) {
            return "0" + units[0];
        }

        int last = units.length - 1;
        int index = 0;
        double limit = factors[index] * 1.1;
        double modifiedValue = value;
        while (index != last && modifiedValue > limit) {
            modifiedValue /= factors[index];
            limit = factors[index] * 1.1;
            index++;
        }
        return String.format("%.1f%s", modifiedValue, units[index]);
    }

    public static String bytes(long value) {
        return longUnit(value, MEMORY_UNITS, MEMORY_FACTORS);
    }

    public static String nanoseconds(long value) {
        return longUnit(value, TIME_UNITS, TIME_FACTORS);
    }

    public static String nanoseconds(double value) {
        return doubleUnit(value, TIME_UNITS, TIME_FACTORS);
    }

    public static String iterations(long value) {
        return longUnit(value, ITERATIONS_UNITS, ITERATIONS_FACTORS);
    }

    public static String iterations(double value) {
        return doubleUnit(value, ITERATIONS_UNITS, ITERATIONS_FACTORS);
    }

    public static String iterationsPerSecond(long value) {
        return longUnit(value, ITERATIONS_PER_TIME_UNITS, ITERATIONS_PER_TIME_FACTORS);
    }

    public static String iterationsPerSecond(double value) {
        return doubleUnit(value, ITERATIONS_PER_TIME_UNITS, ITERATIONS_PER_TIME_FACTORS);
    }
}
