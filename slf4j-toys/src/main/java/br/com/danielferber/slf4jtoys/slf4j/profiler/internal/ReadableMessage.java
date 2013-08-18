/*
 * Copyright 2012 Daniel Felix Ferber
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
package br.com.danielferber.slf4jtoys.slf4j.profiler.internal;

/**
 *
 * @author Daniel Felix Ferber
 */
public final class ReadableMessage {

    private ReadableMessage() {
    }

    public static final double[] TIME_FACTORS = new double[]{1000.0, 1000.0, 1000.0, 60.0, 60.0};
    public static final String[] TIME_UNITS = new String[]{"ns", "us", "ms", "s", "m", "h"};
    public static final String[] MEMORY_UNITS = new String[]{"B", "kB", "MB", "GB"};
    public static final double[] MEMORY_FACTORS = new double[]{1000.0, 1000.0, 1000.0};

    public static String bestUnit(double value, String[] timeUnits, double[] timeFactors) {
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
}
