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

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Collection of utility methods to read system properties, with support for default values and typed conversion.
 * <p>
 * These methods provide safe access to system properties as {@code String}, {@code boolean}, {@code int}, and
 * {@code long}, and include additional logic for parsing time-based values with unit suffixes (e.g., "10s", "5min").
 * <p>
 * This class is not meant to be instantiated.
 *
 * @author Daniel Felix Ferber
 */
@UtilityClass
public class ConfigParser {

    /**
     * A list of errors that occurred during property parsing. Applications can inspect this list
     * after initialization to check for configuration issues.
     */
    public final List<String> initializationErrors = Collections.synchronizedList(new ArrayList<>());

    /**
     * Checks if any errors occurred during property parsing.
     *
     * @return {@code true} if no errors were recorded, {@code false} otherwise.
     */
    public boolean isInitializationOK() {
        return initializationErrors.isEmpty();
    }

    /**
     * Clears all recorded initialization errors. This is useful for testing or re-initialization.
     */
    public void clearInitializationErrors() {
        initializationErrors.clear();
    }

    /**
     * Retrieves the value of a system property as a string. If the property is not set, the default value is returned.
     *
     * @param name         the name of the system property
     * @param defaultValue the default value to return if the property is not set
     * @return the property value as a string, or the default value if the property is not set
     */
    public String getProperty(final String name, final String defaultValue) {
        final String value = System.getProperty(name);
        return value == null ? defaultValue : value.trim();
    }

    /**
     * Retrieves the value of a system property as a boolean. If the property is not set, the default value is
     * returned. If the value is not "true" or "false" (case-insensitive), an error is recorded and the default
     * value is returned.
     *
     * @param name         the name of the system property
     * @param defaultValue the default value to return if the property is not set or invalid
     * @return the property value as a boolean, or the default value if the property is not set or invalid
     */
    public boolean getProperty(final String name, final boolean defaultValue) {
        final String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        final String trimmedValue = value.trim();
        if (trimmedValue.equalsIgnoreCase("true")) {
            return true;
        }
        if (trimmedValue.equalsIgnoreCase("false")) {
            return false;
        }
        initializationErrors.add("Invalid boolean value for property '" + name + "': '" + value + "'. Using default value '" + defaultValue + "'.");
        return defaultValue;
    }

    /**
     * Retrieves the value of a system property as an integer. If the property is not set or cannot be parsed as an
     * integer, the default value is returned and an error is recorded.
     *
     * @param name         the name of the system property
     * @param defaultValue the default value to return if the property is not set or invalid
     * @return the property value as an integer, or the default value if the property is not set or invalid
     */
    public int getProperty(final String name, final int defaultValue) {
        final String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (final NumberFormatException e) {
            initializationErrors.add("Invalid integer value for property '" + name + "': '" + value + "'. Using default value '" + defaultValue + "'.");
            return defaultValue;
        }
    }

    /**
     * Retrieves the value of a system property as an integer within a given range. If the property is not set,
     * cannot be parsed, or is outside the range, the default value is returned and an error is recorded.
     *
     * @param name         the name of the system property
     * @param defaultValue the default value to return if the property is not set or invalid
     * @param minValue     the minimum value that is allowed
     * @param maxValue     the maximum value that is allowed
     * @return the property value as an integer, or the default value if the property is not set or invalid
     */
    public int getRangeProperty(final String name, final int defaultValue,
                                final int minValue, final int maxValue) {
        final String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            final int intValue = Integer.parseInt(value.trim());
            if (intValue < minValue || intValue > maxValue) {
                initializationErrors.add("Value for property '" + name + "' is out of range [" + minValue + "," + maxValue + "]: '" + value + "'. Using default value '" + defaultValue + "'.");
                return defaultValue;
            }
            return intValue;
        } catch (final NumberFormatException e) {
            initializationErrors.add("Invalid integer value for property '" + name + "': '" + value + "'. Using default value '" + defaultValue + "'.");
            return defaultValue;
        }
    }

    /**
     * Retrieves the value of a system property as a long integer. If the property is not set or cannot be parsed as a
     * long, the default value is returned and an error is recorded.
     *
     * @param name         the name of the system property
     * @param defaultValue the default value to return if the property is not set or invalid
     * @return the property value as a long, or the default value if the property is not set or invalid
     */
    public long getProperty(final String name, final long defaultValue) {
        final String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (final NumberFormatException e) {
            initializationErrors.add("Invalid long value for property '" + name + "': '" + value + "'. Using default value '" + defaultValue + "'.");
            return defaultValue;
        }
    }

    /**
     * Retrieves the value of a system property as a duration in milliseconds. If the property is not set or cannot be
     * parsed, the default value is returned and an error is recorded.
     *
     * @param name         the name of the system property
     * @param defaultValue the default value (in milliseconds) to return if the property is not set or invalid
     * @return the parsed duration in milliseconds, or the default value if the property is not set or invalid
     */
    public long getMillisecondsProperty(final String name, final long defaultValue) {
        final String rawValue = System.getProperty(name);
        if (rawValue == null) {
            return defaultValue;
        }
        final String value = rawValue.trim().toLowerCase();
        if (value.isEmpty()) {
            return defaultValue;
        }

        try {
            int multiplicador = 1;
            int suffixLength = 0;
            if (value.endsWith("ms")) {
                suffixLength = 2;
            } else if (value.endsWith("s")) {
                suffixLength = 1;
                multiplicador = 1000;
            } else if (value.endsWith("min")) {
                suffixLength = 3;
                multiplicador = 60 * 1000;
            } else if (value.endsWith("m")) {
                suffixLength = 1;
                multiplicador = 60 * 1000;
            } else if (value.endsWith("h")) {
                suffixLength = 1;
                multiplicador = 60 * 60 * 1000;
            }

            String numberPart = value.substring(0, value.length() - suffixLength).trim();
            if (numberPart.isEmpty()) {
                initializationErrors.add("Invalid time value for property '" + name + "': '" + rawValue + "'. Using default value '" + defaultValue + "'.");
                return defaultValue;
            }

            return Long.parseLong(numberPart) * multiplicador;

        } catch (final NumberFormatException e) {
            initializationErrors.add("Invalid time value for property '" + name + "': '" + rawValue + "'. Using default value '" + defaultValue + "'.");
            return defaultValue;
        }
    }
}
