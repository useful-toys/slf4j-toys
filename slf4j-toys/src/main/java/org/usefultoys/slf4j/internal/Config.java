/*
 * Copyright 2016 Daniel Felix Ferber.
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
 * Collection of static methods to read system properties.
 * 
 * @author Daniel Felix Ferber
 *
 */
public class Config {
    /**
     * Retrieve the value of a system property as a string value.
     * If the system property is not set, the default value is returned.

     * @param name the system property name
     * @param defaultValue the default value, returned if system property is not set
     * @return the value as string
     */
    public static String getProperty(final String name, final String defaultValue) {
        final String value = System.getProperty(name);
        return value == null ? defaultValue : value;
    }

    /**
     * Retrieve the value of a system property as a boolean value.
     * If the system property is not set, or its value is a valid number, the default value is returned.
     * See {@link Boolean#parseBoolean(java.lang.String)}.
     *
     * @param name the system property name
     * @param defaultValue the default value, returned if system property is not set
     * @return the value as boolean
     */
    public static boolean getProperty(final String name, final boolean defaultValue) {
        final String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * Retrieve the value of a system property as an integer value.
     * If the system property is not set, or its value is a valid number, the default value is returned.
     *
     * @param name the system property name
     * @param defaultValue the default value, returned if system property is not set
     * @return the value as integer
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
     * Retrieve the value of a system property as a long integer value.
     * If the system property is not set, or its value is a valid number, the default value is returned.
     *
     * @param name the system property name
     * @param defaultValue the default value, returned if system property is not set
     * @return the value as long integer
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
     * Retrieve the value of a system property as an integer representing milliseconds.
     * If the system property is not set, or its value is a valid number, the default value is returned.
     * The value may be suffixed with 'ms', 's', 'm' or 'h', that will be interpreted as
     * a value in milliseconds, seconds, minutes or hours, respectively, and converted
     * to an integer in milliseconds.
     *
     * @param name the system property name
     * @param defaultValue the default value, in milliseconds, returned if system property is not set
     * @return the value in milliseconds
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
