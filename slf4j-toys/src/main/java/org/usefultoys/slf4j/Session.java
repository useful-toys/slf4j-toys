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
import org.usefultoys.slf4j.meter.Meter;
import org.usefultoys.slf4j.watcher.Watcher;

import java.util.UUID;


/**
 * Holds session-level attributes related to the current JVM instance, such as those used by {@link Meter} and
 * {@link Watcher}.
 * <p>
 * This class is not meant to be instantiated.
 *
 * @author Daniel Felix Ferber
 */
@UtilityClass
public class Session {

    /**
     * Unique identifier for this SLF4J-Toys session.
     * <p>
     * This UUID is added to all trace messages and can be used to distinguish log entries originating from different
     * JVM instances, especially when log files are aggregated.
     * <p>
     * The value is assigned once at application startup and remains constant during runtime.
     */
    public final String uuid = UUID.randomUUID().toString().replace("-", "");
}
