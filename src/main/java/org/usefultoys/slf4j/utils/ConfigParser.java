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
import java.util.LinkedList;
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
 * Error messages recorded in {@link #getInitializationErrors()} include the raw property value to aid debugging.
 * This is accepted by design and is not considered a CWE-209 vulnerability. This class is used solely to
 * obtain configuration defined in {@link org.usefultoys.slf4j.SessionConfig},
 * {@link org.usefultoys.slf4j.report.ReporterConfig}, {@link org.usefultoys.slf4j.watcher.WatcherConfig},
 * and {@link org.usefultoys.slf4j.SystemConfig}, none of which involve sensitive values. This parser was
 * not designed for reuse in other purposes.
 *
 * @author Daniel Felix Ferber
 * @author Co-authored-by: GitHub Copilot using OpenCode Go / Kimi K2.7 Code
 */
@SuppressWarnings("StringConcatenation")
@UtilityClass
public class ConfigParser {

    /**
     * Maximum number of error messages to retain. Once reached, the oldest error is discarded
     * when a new error is added to prevent unbounded memory growth.
     */
    private static final int MAX_ERRORS = 100;

    /**
     * A list of errors that occurred during property parsing. This list is private and only
     * modifiable through the methods of this class. Applications can inspect the errors through
     * {@link #getInitializationErrors()}, which returns an unmodifiable view.
     * <p>
     * The list is bounded at {@value #MAX_ERRORS} entries; once full, the oldest entry is
     * evicted to prevent unbounded memory growth. A LinkedList is used for efficient removal
     * of the oldest (first) element when the limit is reached.
     */
    private final List<String> initializationErrors = Collections.synchronizedList(new LinkedList<>());

    /**
     * Checks if any errors occurred during property parsing.
     *
     * @return {@code true} if no errors were recorded, {@code false} otherwise.
     */
    public boolean isInitializationOK() {
        return initializationErrors.isEmpty();
    }

    /**
     * Returns an unmodifiable view of the errors that occurred during property parsing.
     * Applications can inspect this list after initialization to check for configuration issues.
     *
     * @return an unmodifiable list of error messages; never {@code null}
     */
    public List<String> getInitializationErrors() {
        return Collections.unmodifiableList(initializationErrors);
    }

    /**
     * Clears all recorded initialization errors. This is useful for testing or re-initialization.
     */
    public void clearInitializationErrors() {
        initializationErrors.clear();
    }

    /**
     * Records an initialization error message, evicting the oldest entry if the list has reached
     * {@value #MAX_ERRORS} entries.
     *
     * @param message the error message to record
     */
    private void addInitializationError(final String message) {
        initializationErrors.add(message);
        if (initializationErrors.size() > MAX_ERRORS) {
            initializationErrors.remove(0);
        }
    }

    /**
     * Retrieves the value of a system property as a string. If the property is not set or is blank, the default
     * value is returned.
     *
     * @param name         the name of the system property
     * @param defaultValue the default value to return if the property is not set or blank
     * @return the property value as a string, or the default value if the property is not set or blank
     */
    public String getProperty(final String name, final String defaultValue) {
        final String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        final String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? defaultValue : trimmedValue;
    }

    /**
     * Retrieves the value of a system property as a boolean. If the property is not set or is blank, the default
     * value is returned. If the value is set but is not "true" or "false" (case-insensitive), the default value
     * is returned and an error is recorded.
     *
     * @param name         the name of the system property
     * @param defaultValue the default value to return if the property is not set, blank, or invalid
     * @return the property value as a boolean, or the default value if the property is not set, blank, or invalid
     */
    public boolean getProperty(final String name, final boolean defaultValue) {
        final String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        final String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            return defaultValue;
        }
        if (trimmedValue.equalsIgnoreCase("true")) {
            return true;
        }
        if (trimmedValue.equalsIgnoreCase("false")) {
            return false;
        }
        addInitializationError("Invalid boolean value for property '" + name + "': '" + value + "' is not 'true' or 'false'. Using default value '" + defaultValue + "'.");
        return defaultValue;
    }

    /**
     * Retrieves the value of a system property as an integer. If the property is not set or is blank, the default
     * value is returned. If the value is set but cannot be parsed as an integer, the default value is returned and
     * an error is recorded.
     *
     * @param name         the name of the system property
     * @param defaultValue the default value to return if the property is not set, blank, or invalid
     * @return the property value as an integer, or the default value if the property is not set, blank, or invalid
     */
    public int getProperty(final String name, final int defaultValue) {
        final String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        final String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(trimmedValue);
        } catch (final NumberFormatException e) {
            addInitializationError("Invalid integer value for property '" + name + "': '" + value + "' is not a number. Using default value '" + defaultValue + "'.");
            return defaultValue;
        }
    }

    /**
     * Retrieves the value of a system property as an integer within a given range.
     * <p>
     * If the property is not set or is blank, the default value is returned. If the value is set but cannot be
     * parsed as an integer, the default value is returned and an error is recorded. If the property is set to a
     * valid integer that is below the allowed minimum, the minimum value is returned and an error is recorded. If
     * it is above the allowed maximum, the maximum value is returned and an error is recorded. If the minimum
     * value is greater than the maximum value, the default value is returned and an error is recorded.
     *
     * @param name         the name of the system property
     * @param defaultValue the default value to return if the property is not set, blank, or invalid
     * @param minValue     the minimum value that is allowed
     * @param maxValue     the maximum value that is allowed
     * @return the property value as an integer; the default value if the property is not set, blank, invalid, or
     *         the range itself is invalid; the minimum or maximum value if the parsed value is out of range
     */
    public int getRangeProperty(final String name, final int defaultValue,
                                final int minValue, final int maxValue) {
        /* Reject invalid ranges to avoid silently confusing clamping behavior */
        if (minValue > maxValue) {
            addInitializationError("Invalid range for property '" + name + "': minValue '" + minValue + "' is greater than maxValue '" + maxValue + "'. Using default value '" + defaultValue + "'.");
            return defaultValue;
        }
        final String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        final String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            return defaultValue;
        }
        try {
            final int intValue = Integer.parseInt(trimmedValue);
            if (intValue < minValue) {
                addInitializationError("Invalid integer value for property '" + name + "': '" + value + "' is below minimum '" + minValue + "'. Using minimum value '" + minValue + "'.");
                return minValue;
            }
            if (intValue > maxValue) {
                addInitializationError("Invalid integer value for property '" + name + "': '" + value + "' is above maximum '" + maxValue + "'. Using maximum value '" + maxValue + "'.");
                return maxValue;
            }
            return intValue;
        } catch (final NumberFormatException e) {
            addInitializationError("Invalid integer value for property '" + name + "': '" + value + "' is not a number. Using default value '" + defaultValue + "'.");
            return defaultValue;
        }
    }

    /**
     * Retrieves the value of a system property as a long integer. If the property is not set or is blank, the
     * default value is returned. If the value is set but cannot be parsed as a long, the default value is
     * returned and an error is recorded.
     *
     * @param name         the name of the system property
     * @param defaultValue the default value to return if the property is not set, blank, or invalid
     * @return the property value as a long, or the default value if the property is not set, blank, or invalid
     */
    public long getProperty(final String name, final long defaultValue) {
        final String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        final String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(trimmedValue);
        } catch (final NumberFormatException e) {
            addInitializationError("Invalid long value for property '" + name + "': '" + value + "' is not a number. Using default value '" + defaultValue + "'.");
            return defaultValue;
        }
    }

    /**
     * Retrieves the value of a system property as a {@link Locale}, parsed from a BCP 47 language tag
     * (e.g., {@code "en-US"}, {@code "de-DE"}) via {@link Locale#forLanguageTag(String)}.
     * <p>
     * If the property is not set or is blank, the default value is returned. If the value is present but
     * cannot be resolved to a locale with a language (as happens for malformed input such as {@code "12345"}
     * or the common underscore mistake {@code "de_DE"}, which {@link Locale#forLanguageTag(String)} silently
     * reduces to {@link Locale#ROOT}), the default value is returned and an error is recorded.
     * <p>
     * Note that {@link Locale#forLanguageTag(String)} accepts any syntactically valid BCP 47 language subtag
     * (2-8 alphabetic characters) even if it does not name a real language, so values such as {@code "quatsch"}
     * are parsed successfully rather than treated as invalid.
     *
     * @param name         the name of the system property
     * @param defaultValue the default value to return if the property is not set, blank, or invalid
     * @return the property value as a {@link Locale}, or the default value if the property is not set, blank, or
     *         invalid
     */
    public Locale getProperty(final String name, final Locale defaultValue) {
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
            addInitializationError("Invalid locale value for property '" + name + "': '" + value + "' cannot be resolved to a locale with a language. Using default value '" + defaultValue.toLanguageTag() + "'.");
            return defaultValue;
        }
        return parsed;
    }

    /**
     * Retrieves the value of a system property as a duration in milliseconds. If the property is not set or is
     * blank, the default value is returned. If the value is set but cannot be parsed, the default value is
     * returned and an error is recorded.
     * <p>
     * The value is expected to be a number optionally followed by a time unit suffix (case-insensitive):
     * <ul>
     *   <li>{@code ms} - milliseconds (default if no suffix)</li>
     *   <li>{@code s} - seconds</li>
     *   <li>{@code m} or {@code min} - minutes</li>
     *   <li>{@code h} - hours</li>
     * </ul>
     * Examples: {@code "100ms"}, {@code "5s"}, {@code "2min"}, {@code "1h"}
     *
     * @param name         the name of the system property
     * @param defaultValue the default value (in milliseconds) to return if the property is not set, blank, or
     *                     invalid
     * @return the property value as a duration in milliseconds, or the default value if the property is not set,
     *         blank, or invalid
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
                addInitializationError("Invalid time value for property '" + name + "': '" + rawValue + "' is not a valid duration. Using default value '" + defaultValue + "'.");
                return defaultValue;
            }

            final long parsed = Long.parseLong(numberPart);
            return Math.multiplyExact(parsed, (long) multiplier);

        } catch (final NumberFormatException e) {
            addInitializationError("Invalid time value for property '" + name + "': '" + rawValue + "' is not a valid duration. Using default value '" + defaultValue + "'.");
            return defaultValue;
        } catch (final ArithmeticException e) {
            addInitializationError("Invalid time value for property '" + name + "': '" + rawValue + "' overflows when converted to milliseconds. Using default value '" + defaultValue + "'.");
            return defaultValue;
        }
    }
}
