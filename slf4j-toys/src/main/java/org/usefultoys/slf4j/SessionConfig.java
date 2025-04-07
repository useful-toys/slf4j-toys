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

import org.usefultoys.slf4j.internal.Config;
import org.usefultoys.slf4j.meter.Meter;
import org.usefultoys.slf4j.meter.MeterData;
import org.usefultoys.slf4j.watcher.Watcher;
import org.usefultoys.slf4j.watcher.WatcherData;

/**
 * Collection of properties that control the common behavior of {@link Watcher}, {@link WatcherData}, {@link Meter}, and {@link MeterData}.
 * <p>
 * Initial values are read from system properties at application startup, if available. These properties should ideally be configured before calling any method
 * from this library. Some properties can be reassigned at runtime, depending on their purpose.
 * <p>
 * This class is not meant to be instantiated.
 */
@SuppressWarnings("CanBeFinal")
public final class SessionConfig {

    private SessionConfig() {
        // prevent instances
    }

    public static final int UUID_LENGHT = 32;

    /**
     * Number of digits of the UUID printed in {@link Watcher} and {@link Meter} messages. The full UUID has 32 digits. If the goal is simply to distinguish
     * application instances (e.g., in multi-node servers, after a deployment, or in standalone executions), a shorter prefix may be sufficient.
     * <p>
     * If set to 0, the UUID will not be printed.
     * <p>
     * This value is initialized from the system property {@code slf4jtoys.session.print.uuid.size} at application startup, and defaults to 5. It may be
     * reassigned at runtime.
     */
    public static int uuidSize = Config.getProperty("slf4jtoys.session.print.uuid.size", 5);
}
