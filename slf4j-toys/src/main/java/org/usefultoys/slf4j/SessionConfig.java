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
 * Collection of properties that drive all common behavior ({@link Watcher}, {@link WatcherData}, {@link Meter}, {@link MeterData}).
 * Initial values are read from system properties at application startup, if available.
 * They may be assigned at application startup, before calling any methods from this library.
 * Some properties allow reassigning their values later at runtime.
 */
@SuppressWarnings("CanBeFinal")
public final class SessionConfig {

    public static final int UUID_LENGHT = 32;
    /**
     * Size of the UUID to print on {@link Watcher} and {@link Meter} messages.
     * The UUID has 32 digits. If you want just to distinguish one application instace from another (server running more than one node,
     * new instance after deploy, new execution of standalone application), less digits may be enough.
     * If set to 0 (zero), no UUID is print.
     * <p>Value is read from system property {@code slf4jtoys.session.print.uuid.size} at application startup, defaults to 5.
     * You may assign a new value at runtime.
     */
    public static int uuidSize = Config.getProperty("slf4jtoys.session.print.uuid.size", 5);

    private SessionConfig() {
    }
}
