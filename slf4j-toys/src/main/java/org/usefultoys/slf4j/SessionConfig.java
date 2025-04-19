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
package org.usefultoys.slf4j;

import lombok.experimental.UtilityClass;
import org.usefultoys.slf4j.internal.Config;
import org.usefultoys.slf4j.meter.Meter;
import org.usefultoys.slf4j.meter.MeterData;
import org.usefultoys.slf4j.watcher.Watcher;
import org.usefultoys.slf4j.watcher.WatcherData;

import java.nio.charset.StandardCharsets;

/**
 * Centralized configuration holder for controlling the behavior of {@link Watcher}, {@link WatcherData}, {@link Meter},
 * and {@link MeterData}.
 * <p>
 * This class exposes configurable properties that influence how the session-related logging behaves at runtime. It
 * supports reading initial values from system properties during application startup, allowing applications to
 * externalize configuration.
 * <p>
 * These properties should ideally be defined <em>before</em> invoking any method from this library, to ensure
 * consistent behavior. Some properties can be modified dynamically at runtime, although care should be taken in
 * concurrent environments.
 * <p>
 * <strong>Security note:</strong> These configuration parameters may influence logging output. Avoid using untrusted
 * input when modifying runtime values,
 * as it could cause unintended exposure or log format manipulation.
 * <p>
 * This class is a utility holder and should not be instantiated.
 */
@UtilityClass
public class SessionConfig {
    static { 
        init(); 
    }

    // System property keys
    public final String PROP_PRINT_UUID_SIZE = "slf4jtoys.session.print.uuid.size";
    public final String PROP_PRINT_CHARSET = "slf4jtoys.session.print.charset";

    /**
     * The number of hexadecimal characters in a full UUID, without separators.
     */
    public final int UUID_LENGTH = 32;

    /**
     * Number of UUID digits to print in messages from {@link Watcher} and {@link Meter}.
     * <p>
     * The full UUID (32 hex digits) uniquely identifies the application instance. In most cases, a shorter prefix
     * (e.g., 5 digits) is sufficient to distinguish between instances or nodes.
     * <ul>
     *   <li>If set to <code>0</code>, the UUID will not be printed.</li>
     *   <li>If set to a value greater than {@link #UUID_LENGTH}, it will be truncated.</li>
     * </ul>
     * <p>
     * Default value: <code>5</code><br>
     * Can be initialized via system property {@code slf4jtoys.session.print.uuid.size}.
     */
    public int uuidSize = 5;

    /**
     * Character encoding used when printing logs or performing string operations related to session behavior.
     * <p>
     * Defaults to {@link StandardCharsets#UTF_8}. May be changed at runtime.
     */
    public String charset = StandardCharsets.UTF_8.name();

    /**
     * Initializes the configurable properties of the SessionConfig class.
     * This method should be called during application startup to ensure consistent behavior.
     */
    public void init() {
        uuidSize = Config.getProperty(PROP_PRINT_UUID_SIZE, 5);
        charset = Config.getProperty(PROP_PRINT_CHARSET, StandardCharsets.UTF_8.name());
    }
}
