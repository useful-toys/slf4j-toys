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
package org.usefultoys.slf4j;

import lombok.experimental.UtilityClass;
import org.usefultoys.slf4j.meter.Meter;
import org.usefultoys.slf4j.utils.ConfigParser;
import org.usefultoys.slf4j.watcher.Watcher;

import java.nio.charset.Charset;

/**
 * Centralized configuration for session-related components like {@link Watcher}, {@link Meter},
 * and their data counterparts.
 * <p>
 * This class holds properties that control logging behavior. It reads initial values from
 * system properties at startup, allowing for externalized configuration.
 * <p>
 * For consistent behavior, these properties should be set before any methods from this library are called.
 * While some properties can be modified at runtime, caution is advised in concurrent environments.
 * <p>
 * **Security Note:** These parameters can influence log output. Avoid using untrusted input
 * when modifying runtime values to prevent unintended information disclosure or log format manipulation.
 * <p>
 * This is a utility class and is not meant to be instantiated.
 *
 * @author Daniel Felix Ferber
 * @see Session
 */
@UtilityClass
public class SessionConfig {
    static {
        init();
    }

    // System property keys
    /** System property key for the number of UUID characters to print. */
    public final String PROP_PRINT_UUID_SIZE = "slf4jtoys.session.print.uuid.size";
    /** System property key for the character encoding used for logging. */
    public final String PROP_PRINT_CHARSET = "slf4jtoys.session.print.charset";

    /**
     * The number of hexadecimal characters in a full UUID, without separators.
     */
    public final int UUID_LENGTH = 32;

    /**
     * The number of UUID characters to include in **machine-parsable data messages** from {@link Watcher} and {@link Meter}.
     * <p>
     * The full UUID (32 hex characters) uniquely identifies the application instance. In most cases, a shorter
     * prefix (e.g., 5 characters) is sufficient to distinguish between instances.
     * <ul>
     *   <li>If set to {@code 0}, the UUID will not be included.</li>
     *   <li>If set to a value greater than {@link #UUID_LENGTH}, it will be truncated.</li>
     * </ul>
     * <p>
     * The value is read from the system property {@code slf4jtoys.session.print.uuid.size}, defaulting to {@code 5}.
     */
    public int uuidSize = 5;

    /**
     * The character encoding used for logging and string operations.
     * <p>
     * The value is read from the system property {@code slf4jtoys.session.print.charset}, defaulting to the
     * JVM's default charset.
     */
    public String charset = Charset.defaultCharset().name();

    /**
     * Initializes the configuration properties. This method should be called at application startup to ensure
     * consistent behavior.
     */
    public void init() {
        uuidSize = ConfigParser.getProperty(PROP_PRINT_UUID_SIZE, 5);
        charset = ConfigParser.getProperty(PROP_PRINT_CHARSET, Charset.defaultCharset().name());
    }

    /**
     * Resets the configuration properties to their default values.
     * This method is useful for testing or re-initializing the configuration.
     */
    public void reset() {
        System.clearProperty(PROP_PRINT_UUID_SIZE);
        System.clearProperty(PROP_PRINT_CHARSET);
        init();
    }
}
