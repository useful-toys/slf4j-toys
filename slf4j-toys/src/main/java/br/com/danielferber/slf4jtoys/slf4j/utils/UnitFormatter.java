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

    private static final double[] TIME_FACTORS = new double[]{1000.0, 1000.0, 1000.0, 60.0, 60.0};
    private static final String[] TIME_UNITS = new String[]{"ns", "us", "ms", "s", "m", "h"};
    private static final String[] MEMORY_UNITS = new String[]{"B", "kB", "MB", "GB"};
    private static final double[] MEMORY_FACTORS = new double[]{1000.0, 1000.0, 1000.0};
    private static final String[] ITERATIONS_PER_TIME_UNITS = new String[]{"/s", "k/s", "M/s"};
    private static final double[] ITERATIONS_PER_TIME_FACTORS = new double[]{1000.0, 1000.0, 1000.0};
    private static final String[] ITERATIONS_UNITS = new String[]{"", "k", "M"};
    private static final double[] ITERATIONS_FACTORS = new double[]{1000.0, 1000.0, 1000.0};

    private static String longUnit(long lvalue, String[] timeUnits, double[] timeFactors) {
        if (lvalue == 0.0) {
            return "0" + timeUnits[0];
        }

        int index = 0;
        double limit = timeFactors[index] * 1.1;
        double modifiedValue = lvalue;
        if (modifiedValue <= limit) {
            return String.format("%d%s", lvalue, timeUnits[index]);
        }

        int last = timeUnits.length - 1;
        do {
            modifiedValue /= timeFactors[index];
            limit = timeFactors[index] * 1.1;
            index++;
        } while (index != last && modifiedValue > limit);
        return String.format("%.1f%s", modifiedValue, timeUnits[index]);
    }

    private static String doubleUnit(double value, String[] timeUnits, double[] timeFactors) {
        if (value == 0.0) {
            return "0" + timeUnits[0];
        }

        int last = timeUnits.length - 1;
        int index = 0;
        double limit = timeFactors[index] * 1.1;
        double modifiedValue = value;
        while (index != last && modifiedValue > limit) {
            modifiedValue /= timeFactors[index];
            limit = timeFactors[index] * 1.1;
            index++;
        }
        return String.format("%.1f%s", modifiedValue, timeUnits[index]);
    }

    public static String bytes(long value) {
        return longUnit(value, MEMORY_UNITS, MEMORY_FACTORS);
    }

    public static String nanoseconds(long value) {
        return longUnit(value, MEMORY_UNITS, MEMORY_FACTORS);
    }

    public static String nanoseconds(double value) {
        return doubleUnit(value, MEMORY_UNITS, MEMORY_FACTORS);
    }

    public static String iterations(double value) {
        return doubleUnit(value, ITERATIONS_UNITS, ITERATIONS_FACTORS);
    }

    public static String iterationsPerSecond(double value) {
        return doubleUnit(value, ITERATIONS_PER_TIME_UNITS, ITERATIONS_PER_TIME_FACTORS);
    }
}
