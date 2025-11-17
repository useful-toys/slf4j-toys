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
package org.usefultoys.slf4j.meter;

import java.util.IllegalFormatException;

/**
 * An interface defining methods for managing contextual data within a {@link Meter} operation.
 * This allows attaching key-value pairs to an operation, which can be useful for debugging,
 * analysis, or providing additional information in logs.
 * <p>
 * Implementations of this interface are expected to provide concrete logic for
 * {@link #putContext(String, Object)} and {@link #removeContext(String)}.
 * Default methods are provided for various overloads of {@code ctx} and {@code unctx}
 * to simplify usage.
 * </p>
 *
 * @author Daniel Felix Ferber
 */
public interface MeterContext<T extends Meter> {

    /**
     * Adds a key-value entry to the context map.
     *
     * @param name  The key of the entry to add.
     * @param value The string value of the entry.
     */
    void putContext(final String name, final Object value);
    void putContext(final String name);

    /**
     * Removes an entry from the context map.
     *
     * @param name The key of the entry to remove.
     */
    void removeContext(final String name);

    void clearContext();

    /**
     * Returns the full ID of the Meter, used for logging error messages.
     * This method is expected to be implemented by the class implementing this interface.
     * @return The full ID of the Meter.
     */
    String getFullID();

    // --- Default Methods for ctx (add context) ---

    /**
     * Adds a key-only entry to the context map. This is interpreted as a marker or flag.
     *
     * @param name The key of the entry to add. Must not be {@code null}.
     * @return Reference to this `MeterContext` instance, for method chaining.
     */
    default T ctx(final String name) {
        putContext(name);
        return (T) this;
    }

    /**
     * Conditionally adds a key-only entry to the context map if the condition is {@code true}.
     *
     * @param condition The condition to evaluate.
     * @param trueName  The key of the entry to add if {@code condition} is {@code true}. Must not be {@code null}.
     * @return Reference to this `MeterContext` instance, for method chaining.
     */
    default T ctx(final boolean condition, final String trueName) {
        if (!condition) {
            return (T) this;
        }
        putContext(trueName, null);
        return (T) this;
    }

    /**
     * Conditionally adds a key-only entry to the context map based on a boolean condition.
     *
     * @param condition The condition to evaluate.
     * @param trueName  The key of the entry to add if {@code condition} is {@code true}. Must not be {@code null}.
     * @param falseName The key of the entry to add if {@code condition} is {@code false}. Must not be {@code null}.
     * @return Reference to this `MeterContext` instance, for method chaining.
     */
    default T ctx(final boolean condition, final String trueName, final String falseName) {
        if (condition) {
            putContext(trueName, null);
        } else {
            putContext(falseName, null);
        }
        return (T) this;
    }

    /**
     * Adds a key-value entry to the context map with an integer value.
     *
     * @param name  The key of the entry to add. Must not be {@code null}.
     * @param value The integer value.
     * @return Reference to this `MeterContext` instance, for method chaining.
     */
    default T ctx(final String name, final int value) {
        putContext(name, Integer.toString(value));
        return (T) this;
    }

    /**
     * Adds a key-value entry to the context map with a long value.
     *
     * @param name  The key of the entry to add. Must not be {@code null}.
     * @param value The long value.
     * @return Reference to this `MeterContext` instance, for method chaining.
     */
    default T ctx(final String name, final long value) {
        putContext(name, Long.toString(value));
        return (T) this;
    }

    /**
     * Adds a key-value entry to the context map with a boolean value.
     *
     * @param name  The key of the entry to add. Must not be {@code null}.
     * @param value The boolean value.
     * @return Reference to this `MeterContext` instance, for method chaining.
     */
    default T ctx(final String name, final boolean value) {
        putContext(name, Boolean.toString(value));
        return (T) this;
    }

    /**
     * Adds a key-value entry to the context map with a float value.
     *
     * @param name  The key of the entry to add. Must not be {@code null}.
     * @param value The float value.
     * @return Reference to this `MeterContext` instance, for method chaining.
     */
    default T ctx(final String name, final float value) {
        putContext(name, Float.toString(value));
        return (T) this;
    }

    /**
     * Adds a key-value entry to the context map with a double value.
     *
     * @param name  The key of the entry to add. Must not be {@code null}.
     * @param value The double value.
     * @return Reference to this `MeterContext` instance, for method chaining.
     */
    default T ctx(final String name, final double value) {
        putContext(name, Double.toString(value));
        return (T) this;
    }

    /**
     * Adds a key-value entry to the context map with an {@link Integer} object value.
     *
     * @param name  The key of the entry to add. Must not be {@code null}.
     * @param value The {@link Integer} object value. {@code null} values are represented by {@code "<null>"}.
     * @return Reference to this `MeterContext` instance, for method chaining.
     */
    default T ctx(final String name, final Integer value) {
        putContext(name, value == null ? null : value.toString());
        return (T) this;
    }

    /**
     * Adds a key-value entry to the context map with a {@link Long} object value.
     *
     * @param name  The key of the entry to add. Must not be {@code null}.
     * @param value The {@link Long} object value. {@code null} values are represented by {@code "<null>"}.
     * @return Reference to this `MeterContext` instance, for method chaining.
     */
    default T ctx(final String name, final Long value) {
        putContext(name, value == null ? null : value.toString());
        return (T) this;
    }

    /**
     * Adds a key-value entry to the context map with a {@link Boolean} object value.
     *
     * @param name  The key of the entry to add. Must not be {@code null}.
     * @param value The {@link Boolean} object value. {@code null} values are represented by {@code "<null>"}.
     * @return Reference to this `MeterContext` instance, for method chaining.
     */
    default T ctx(final String name, final Boolean value) {
        putContext(name, value == null ? null : value.toString());
        return (T) this;
    }

    /**
     * Adds a key-value entry to the context map with a {@link Float} object value.
     *
     * @param name  The key of the entry to add. Must not be {@code null}.
     * @param value The {@link Float} object value. {@code null} values are represented by {@code "<null>"}.
     * @return Reference to this `MeterContext` instance, for method chaining.
     */
    default T ctx(final String name, final Float value) {
        putContext(name, value == null ? null : value.toString());
        return (T) this;
    }

    /**
     * Adds a key-value entry to the context map with a {@link Double} object value.
     *
     * @param name  The key of the entry to add. Must not be {@code null}.
     * @param value The {@link Double} object value. {@code null} values are represented by {@code "<null>"}.
     * @return Reference to this `MeterContext` instance, for method chaining.
     */
    default T ctx(final String name, final Double value) {
        putContext(name, value == null ? null : value.toString());
        return (T) this;
    }

    /**
     * Adds a key-value entry to the context map with a string value.
     *
     * @param name  The key of the entry to add. Must not be {@code null}.
     * @param value The string value. {@code null} values are represented by {@code "<null>"}.
     * @return Reference to this `MeterContext` instance, for method chaining.
     */
    default T ctx(final String name, final String value) {
        putContext(name, value);
        return (T) this;
    }

    
    /**
     * Adds a key-value entry to the context map, where the value is a formatted message.
     *
     * @param name   The key of the entry to add. Must not be {@code null}.
     * @param format The message format string (e.g., `String.format(java.lang.String, java.lang.Object...)`). Must not
     *               be {@code null}.
     * @param args   The arguments for the format string.
     * @return Reference to this `MeterContext` instance, for method chaining.
     */
    default T ctx(final String name, final String format, final Object... args) {
        try {
            ctx(name, String.format(format, args));
        } catch (final IllegalFormatException e) {
            ctx(name, e.getLocalizedMessage());
        }
        return (T) this;
    }

    default T ctx(final String name, final Object value) {
        putContext(name, value);
        return (T) this;
    }
    /**
     * Removes an entry from the context map.
     *
     * @param name The key of the entry to remove. Must not be {@code null}.
     * @return Reference to this `MeterContext` instance, for method chaining.
     */
    default T unctx(final String name) {
        removeContext(name);
        return (T) this;
    }
}
