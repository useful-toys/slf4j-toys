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

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.usefultoys.slf4j.meter.Meter;
import org.usefultoys.slf4j.watcher.Watcher;

import java.util.Locale;
import java.util.UUID;


/**
 * Holds session-level attributes for the current JVM instance, used by components like {@link Meter} and {@link Watcher}.
 * <p>
 * This is a utility class and is not meant to be instantiated.
 *
 * @author Daniel Felix Ferber
 */
@UtilityClass
public class Session {

    /**
     * A unique identifier for the `slf4j-toys` instance.
     * <p>
     * This UUID is included in all **machine-parsable data messages** and can be used to correlate logs from the same JVM instance,
     * especially when log files are aggregated.
     * <p>
     * The value is assigned once at application startup and remains constant.
     */
    public final String uuid = UUID.randomUUID().toString().replace("-", "");

    /**
     * Returns the rightmost {@link SessionConfig#uuidSize} hexadecimal digits of the session UUID.
     * <p>
     * If {@link SessionConfig#uuidSize} is outside the valid range {@code [2, SessionConfig#UUID_LENGTH]},
     * it is corrected to the nearest boundary before the shortened UUID is returned.
     *
     * @return A shortened UUID string containing the trailing hexadecimal characters of {@link #uuid}.
     * @see SessionConfig#uuidSize
     * @see SessionConfig#UUID_LENGTH
     */
    public @NonNull String shortSessionUuid() {
        SessionConfig.uuidSize = Math.max(2, Math.min(SessionConfig.uuidSize, SessionConfig.UUID_LENGTH));
        return Session.uuid.substring(SessionConfig.UUID_LENGTH - SessionConfig.uuidSize);
    }

    /**
     * Returns the {@link Locale} used to format human-readable numbers in log messages and reports.
     * <p>
     * This does not affect **machine-parsable data messages**, whose numeric fields always use
     * {@link Locale#US}.
     *
     * @return the {@link Locale} resolved from {@link SessionConfig#locale}.
     * @see SessionConfig#locale
     */
    public @NonNull Locale getLocale() {
        return Locale.forLanguageTag(SessionConfig.locale);
    }
}
