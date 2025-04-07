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

import org.usefultoys.slf4j.meter.Meter;
import org.usefultoys.slf4j.report.Reporter;
import org.usefultoys.slf4j.watcher.Watcher;

import java.util.UUID;
import java.util.concurrent.Executor;

/**
 * Keeps {@link Meter} and {@link Watcher} session attributes related to the current JVM instance.
 *
 * @author Daniel Felix Ferber
 */
public final class Session {

    private Session() {
        // prevent instances
    }

    /**
     * UUID of the current SLF4J-Toys instance. This UUID is added to all trace messages. It allows to distinguish messages from different JVM
     * instances when log files are shared. Value is assigned at application startup and cannot be changed at runtime.
     */
    public static final String uuid = UUID.randomUUID().toString().replace("-", "");
}
