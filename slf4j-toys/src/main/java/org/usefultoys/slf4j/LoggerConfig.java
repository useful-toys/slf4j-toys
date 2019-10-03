/*
 * Copyright 2019 Daniel Felix Ferber
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
import org.usefultoys.slf4j.report.Reporter;
import org.usefultoys.slf4j.watcher.Watcher;

/**
 * Collection of properties that drive {@link Meter}, {@link Watcher} and {@link Reporter} common logging behavior.
 * Initial values are read from system properties at application startup, if available.
 * They may be assigned at application startup, before calling any {@link Meter} or {@link Watcher} methods.
 * Some properties allow reassigning their values later at runtime.
 *
 * @author Daniel Felix Ferber
 */
public final class LoggerConfig {

    private LoggerConfig() {
        // prevent instances
    }

    /**
     * Use JUL instead of SLF4J. Uses JUL instead of SLF4J for start, progress, ok, reject and fail messages. This hack
     * might be reasonable when the SLF4J integration does not work well over the underlying logger framework,
     * disturbing the message readability.
     * Value is read from system property {@code slf4jtoys.hack.jul.enable} at application startup, defaults to {@code false}.
     * You may assign a new value at runtime.
     */
    public static boolean hackJulEnable = Config.getProperty("slf4jtoys.hack.jul.enable", false);
    /**
     * When using JUL instead of SLF4J, inform category and operation name as class and method name instead of logger name. This hack
     * might be reasonable when the SLF4J integration does not support logger names, what would prevent informing the
     * logger name, disturbing the message readability. This happens on Google App, that imposes the class name as what
     * SLF4J considers the logger name.
     * Value is read from system property {@code slf4jtoys.hack.jul.replaceSource} at application startup, defaults to {@code false}.
     * You may assign a new value at runtime.
     */
    public static boolean hackJulReplaceSource = Config.getProperty("slf4jtoys.hack.jul.replaceSource", false);
}
