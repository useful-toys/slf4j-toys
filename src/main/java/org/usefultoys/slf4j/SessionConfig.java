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
package org.usefultoys.slf4j;

import lombok.experimental.UtilityClass;
import org.usefultoys.slf4j.meter.Meter;
import org.usefultoys.slf4j.utils.ConfigParser;
import org.usefultoys.slf4j.watcher.Watcher;

import java.nio.charset.Charset;
import java.util.Locale;

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
    /** System property key for the locale used to format human-readable numbers. */
    public final String PROP_PRINT_LOCALE = "slf4jtoys.session.print.locale";

    /**
     * The number of hexadecimal characters in a full UUID, without separators.
     */
    public final int UUID_LENGTH = 32;

    /**
     * The number of UUID characters to include in **machine-parsable data messages** from {@link Watcher} and {@link Meter}.
     * <p>
     * The full UUID (32 hex characters) uniquely identifies the application instance. In most cases, a shorter
     * suffix of rightmost hexadecimal characters (e.g., 6 characters) is sufficient to distinguish between instances.
     * <p>
     * Valid values are in the range {@code [2, UUID_LENGTH]}. Values below {@code 2} are clamped to
     * {@code 2}, and values above {@link #UUID_LENGTH} are clamped to {@link #UUID_LENGTH}.
     * <p>
     * The value is read from the system property {@code slf4jtoys.session.print.uuid.size}, defaulting to {@code 6}.
     * <p>
     * <strong>Thread Safety:</strong> This field can be modified at runtime, but caution is advised in concurrent
     * environments as changes are not synchronized.
     */
    public int uuidSize = 6;

    /**
     * The character encoding used for logging and string operations.
     * <p>
     * The value is read from the system property {@code slf4jtoys.session.print.charset}, defaulting to the
     * JVM's default charset.
     * <p>
     * <strong>Thread Safety:</strong> This field can be modified at runtime, but caution is advised in concurrent
     * environments as changes are not synchronized.
     */
    public String charset = Charset.defaultCharset().name();

    /**
     * The locale used to format human-readable numbers (e.g., durations, memory sizes, throughput)
     * in log messages and reports produced by {@link Watcher}, {@link Meter}, and the {@code report} package.
     * <p>
     * This setting affects only **human-readable formatting**. It has no effect on
     * **machine-parsable data messages**, whose numeric fields always use {@link Locale#US}
     * (a fixed {@code .} decimal separator) so that downstream parsers are not broken by
     * locale-dependent output.
     * <p>
     * The value must be a BCP 47 language tag (e.g., {@code "en-US"}, {@code "de-DE"}), as accepted
     * by {@link Locale#forLanguageTag(String)}.
     * <p>
     * The value is read from the system property {@code slf4jtoys.session.print.locale}, defaulting to the
     * JVM's default locale.
     * <p>
     * <strong>Thread Safety:</strong> This field can be modified at runtime, but caution is advised in concurrent
     * environments as changes are not synchronized.
     */
    public String locale = Locale.getDefault().toLanguageTag();

    /**
     * Initializes the configuration properties by reading values from system properties.
     * <p>
     * This method is automatically called in a static initializer when the class is first loaded.
     * It can also be called manually to reload configuration from system properties after they have been modified.
     * <p>
     * For consistent behavior, ensure system properties are set before this class is first accessed.
     */
    public void init() {
        uuidSize = ConfigParser.getRangeProperty(PROP_PRINT_UUID_SIZE, 6, 2, UUID_LENGTH);
        charset = ConfigParser.getProperty(PROP_PRINT_CHARSET, Charset.defaultCharset().name());
        locale = ConfigParser.getProperty(PROP_PRINT_LOCALE, Locale.getDefault().toLanguageTag());
    }

    /**
     * Resets the configuration properties to their default values.
     * This method is useful for testing or re-initializing the configuration.
     */
    public void reset() {
        System.clearProperty(PROP_PRINT_UUID_SIZE);
        System.clearProperty(PROP_PRINT_CHARSET);
        System.clearProperty(PROP_PRINT_LOCALE);
        init();
    }
}
