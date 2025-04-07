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
package org.usefultoys.slf4j.internal;

/**
 * Collection of static utility methods to read system properties, with support for default values and typed conversion.
 * <p>
 * These methods provide safe access to system properties as {@code String}, {@code boolean}, {@code int}, and {@code long}, and include additional logic for
 * parsing time-based values with unit suffixes (e.g., "10s", "5min").
 * <p>
 * This class is not meant to be instantiated.
 *
 * @author Daniel Felix Ferber
 */
public final class Config {
    private Config() {
        // prevent instances
    }

    /**
     * Retrieves the value of a system property as a string. Returns the default value if the property is not set.
     *
     * @param name         the system property name
     * @param defaultValue the default value to return if the property is not set
     * @return the property value, or the default value if unset
     */
    public static String getProperty(final String name, final String defaultValue) {
        final String value = System.getProperty(name);
        return value == null ? defaultValue : value;
    }

    /**
     * Retrieves the value of a system property as a boolean. Returns the default value if the property is not set.
     * <p>
     * The property value is parsed using {@link Boolean#parseBoolean(String)}.
     *
     * @param name         the system property name
     * @param defaultValue the default value to return if the property is not set
     * @return the boolean value, or the default value if unset
     */
    public static boolean getProperty(final String name, final boolean defaultValue) {
        final String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * Retrieves the value of a system property as an integer. Returns the default value if the property is not set or cannot be parsed as an integer.
     *
     * @param name         the system property name
     * @param defaultValue the default value to return if the property is not set or invalid
     * @return the integer value, or the default value if unset or invalid
     */
    public static int getProperty(final String name, final int defaultValue) {
        final String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException ignored) {
            return defaultValue;
        }
    }

    /**
     * Retrieves the value of a system property as a long integer. Returns the default value if the property is not set or cannot be parsed as a long.
     *
     * @param name         the system property name
     * @param defaultValue the default value to return if the property is not set or invalid
     * @return the long value, or the default value if unset or invalid
     */
    public static long getProperty(final String name, final long defaultValue) {
        final String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (final NumberFormatException ignored) {
            return defaultValue;
        }
    }

    /**
     * Retrieves the value of a system property as a duration in milliseconds.
     * <p>
     * Supports suffixes for time units:
     * <ul>
     *     <li>{@code ms} for milliseconds</li>
     *     <li>{@code s} for seconds</li>
     *     <li>{@code m} or {@code min} for minutes</li>
     *     <li>{@code h} for hours</li>
     * </ul>
     * If the property is not set or cannot be parsed, the default value is returned.
     *
     * @param name         the system property name
     * @param defaultValue the default value (in milliseconds) to return if the property is not set or invalid
     * @return the parsed duration in milliseconds, or the default value if unset or invalid
     */
    public static long getMillisecondsProperty(final String name, final long defaultValue) {
        final String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            int multiplicador = 1;
            int suffixLength = 1;
            if (value.endsWith("ms")) {
                suffixLength = 2;
            } else if (value.endsWith("s")) {
                multiplicador = 1000;
            } else if (value.endsWith("m")) {
                multiplicador = 60 * 1000;
            } else if (value.endsWith("min")) {
                multiplicador = 60 * 1000;
            } else if (value.endsWith("h")) {
                multiplicador = 60 * 60 * 1000;
            } else {
                return defaultValue;
            }
            return Long.parseLong(value.substring(0, value.length() - suffixLength)) * multiplicador;

        } catch (final NumberFormatException ignored) {
            return defaultValue;
        }
    }
}
