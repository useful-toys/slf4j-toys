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

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Collection of utility methods to read system properties, with support for default values and typed conversion.
 * <p>
 * These methods provide safe access to system properties as {@code String}, {@code boolean}, {@code int}, and
 * {@code long}, and include additional logic for parsing time-based values with unit suffixes (e.g., "10s", "5min").
 * <p>
 * This class is not meant to be instantiated.
 * <p>
 * <strong>Security note regarding CWE-209 (Information Exposure Through Error Message):</strong>
 * Error messages recorded in {@link #initializationErrors} include the raw property value to aid debugging.
 * This is accepted by design and is not considered a CWE-209 vulnerability. This class is used solely to
 * obtain configuration defined in {@link org.usefultoys.slf4j.SessionConfig},
 * {@link org.usefultoys.slf4j.report.ReporterConfig}, {@link org.usefultoys.slf4j.watcher.WatcherConfig},
 * and {@link org.usefultoys.slf4j.SystemConfig}, none of which involve sensitive values. This parser was
 * not designed for reuse in other purposes.
 *
 * @author Daniel Felix Ferber
 * @author Co-authored-by: GitHub Copilot using OpenCode Go / Kimi K2.7 Code
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
     * Retrieves the value of a system property as an integer within a given range.
     * <p>
     * If the property is not set or cannot be parsed as an integer, the default value is returned and an error
     * is recorded. If the property is set to a valid integer that is below the allowed minimum, the minimum value
     * is returned and an error is recorded. If it is above the allowed maximum, the maximum value is returned and
     * an error is recorded. If the minimum value is greater than the maximum value, the default value is returned
     * and an error is recorded.
     *
     * @param name         the name of the system property
     * @param defaultValue the default value to return if the property is not set or cannot be parsed
     * @param minValue     the minimum value that is allowed
     * @param maxValue     the maximum value that is allowed
     * @return the property value as an integer; the default value if the property is not set, invalid, or if the
     *         range is invalid; the minimum or maximum value if the parsed value is out of range
     */
    public int getRangeProperty(final String name, final int defaultValue,
                                final int minValue, final int maxValue) {
        /* Reject invalid ranges to avoid silently confusing clamping behavior */
        if (minValue > maxValue) {
            initializationErrors.add("Invalid range for property '" + name + "': minValue (" + minValue + ") is greater than maxValue (" + maxValue + "). Using default value '" + defaultValue + "'.");
            return defaultValue;
        }
        final String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            final int intValue = Integer.parseInt(value.trim());
            if (intValue < minValue) {
                initializationErrors.add("Value for property '" + name + "' is below minimum " + minValue + ": '" + value + "'. Using minimum value.");
                return minValue;
            }
            if (intValue > maxValue) {
                initializationErrors.add("Value for property '" + name + "' is above maximum " + maxValue + ": '" + value + "'. Using maximum value.");
                return maxValue;
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
    /**
     * Retrieves the value of a system property as a {@link Locale}, parsed from a BCP 47 language tag
     * (e.g., {@code "en-US"}, {@code "de-DE"}) via {@link Locale#forLanguageTag(String)}.
     * <p>
     * If the property is not set or is blank, the default value is returned. If the value is present but
     * cannot be resolved to a locale with a language (as happens for malformed input such as {@code "quatsch"}
     * or the common underscore mistake {@code "de_DE"}, which {@link Locale#forLanguageTag(String)} silently
     * reduces to {@link Locale#ROOT}), an error is recorded and the default value is returned.
     *
     * @param name         the name of the system property
     * @param defaultValue the default value to return if the property is not set or invalid
     * @return the property value as a {@link Locale}, or the default value if the property is not set or invalid
     */
    public Locale getLocaleProperty(final String name, final Locale defaultValue) {
        final String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        final String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            return defaultValue;
        }
        final Locale parsed = Locale.forLanguageTag(trimmedValue);
        if (parsed.getLanguage().isEmpty()) {
            initializationErrors.add("Invalid locale value for property '" + name + "': '" + value + "'. Using default value '" + defaultValue.toLanguageTag() + "'.");
            return defaultValue;
        }
        return parsed;
    }

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
            int multiplier = 1;
            int suffixLength = 0;
            if (value.endsWith("ms")) {
                suffixLength = 2;
            } else if (value.endsWith("s")) {
                suffixLength = 1;
                multiplier = 1000;
            } else if (value.endsWith("min")) {
                suffixLength = 3;
                multiplier = 60 * 1000;
            } else if (value.endsWith("m")) {
                suffixLength = 1;
                multiplier = 60 * 1000;
            } else if (value.endsWith("h")) {
                suffixLength = 1;
                multiplier = 60 * 60 * 1000;
            }

            final String numberPart = value.substring(0, value.length() - suffixLength).trim();
            if (numberPart.isEmpty()) {
                initializationErrors.add("Invalid time value for property '" + name + "': '" + rawValue + "'. Using default value '" + defaultValue + "'.");
                return defaultValue;
            }

            final long parsed = Long.parseLong(numberPart);
            return Math.multiplyExact(parsed, (long) multiplier);

        } catch (final NumberFormatException e) {
            initializationErrors.add("Invalid time value for property '" + name + "': '" + rawValue + "'. Using default value '" + defaultValue + "'.");
            return defaultValue;
        } catch (final ArithmeticException e) {
            initializationErrors.add("Time value overflow for property '" + name + "': '" + rawValue + "'. Using default value '" + defaultValue + "'.");
            return defaultValue;
        }
    }
}
