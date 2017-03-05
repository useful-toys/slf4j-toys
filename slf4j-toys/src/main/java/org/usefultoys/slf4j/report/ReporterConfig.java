/**
 * Copyright 2017 Daniel Felix Ferber
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
package org.usefultoys.slf4j.report;

import org.usefultoys.slf4j.internal.Config;
import org.usefultoys.slf4j.meter.Meter;
import org.usefultoys.slf4j.watcher.Watcher;

/**
 * Collection of properties that drive {@link Reporter} behavior.
 * Initial values are read from system properties at application startup, if available.
 * They may be assigned at application startup, before calling any {@link Reporter} methods.
 * Some properties allow reassigning their values later at runtime.
 *
 * @author Daniel Felix Ferber
 */
@SuppressWarnings("CanBeFinal")
public final class ReporterConfig {
    public ReporterConfig() {
        // prevent instances
    }

    /**
     * If default report includes JVM information.
     * Value is read from system property {@code slf4jtoys.report.vm} at application startup, defaults to {@code true}.
     * You may assign a new value at runtime.
     */
    public static boolean reportVM = Config.getProperty("slf4jtoys.report.vm", true);
    /**
     * If default report includes available and used disk storage information.
     * Value is read from system property {@code slf4jtoys.report.fileSystem} at application startup, defaults to {@code false}.
     * You may assign a new value at runtime.
     */
    public static boolean reportFileSystem = Config.getProperty("slf4jtoys.report.fileSystem", false);
    /**
     * If default report includes available and used memory information.
     * Value is read from system property {@code slf4jtoys.report.memory} at application startup, defaults to {@code true}.
     * You may assign a new value at runtime.
     */
    public static boolean reportMemory = Config.getProperty("slf4jtoys.report.memory", true);
    /**
     * If default report includes current user information.
     * Value is read from system property {@code slf4jtoys.report.user} at application startup, defaults to {@code true}.
     * You may assign a new value at runtime.
     */
    public static boolean reportUser = Config.getProperty("slf4jtoys.report.user", true);
    /**
     * If default report includes physical machine information.
     * Value is read from system property {@code slf4jtoys.report.physicalSystem} at application startup, defaults to {@code true}.
     * You may assign a new value at runtime.
     */
    public static boolean reportPhysicalSystem = Config.getProperty("slf4jtoys.report.physicalSystem", true);
    /**
     * If default report includes operating system information.
     * Value is read from system property {@code slf4jtoys.report.operatingSystem} at application startup, defaults to {@code true}.
     * You may assign a new value at runtime.
     */
    public static boolean reportOperatingSystem = Config.getProperty("slf4jtoys.report.operatingSystem", true);
    /**
     * If default report includes date, time, calendar and timezone information.
     * Value is read from system property {@code slf4jtoys.report.calendar} at application startup, defaults to {@code true}.
     * You may assign a new value at runtime.
     */
    public static boolean reportCalendar = Config.getProperty("slf4jtoys.report.calendar", true);
    /**
     * If default report includes current and available locale information.
     * Value is read from system property {@code slf4jtoys.report.locale} at application startup, defaults to {@code true}.
     * You may assign a new value at runtime.
     */
    public static boolean reportLocale = Config.getProperty("slf4jtoys.report.locale", true);
    /**
     * If default report includes current and available charset information.
     * Value is read from system property {@code slf4jtoys.report.charset} at application startup, defaults to {@code true}.
     * You may assign a new value at runtime.
     */
    public static boolean reportCharset = Config.getProperty("slf4jtoys.report.charset", true);
    /**
     * If default report includes available network interface information.
     * This report may block the thread for considerable amount of time.
     * Value is read from system property {@code slf4jtoys.report.networkInterface} at application startup, defaults to {@code false}.
     * You may assign a new value at runtime.
     */
    public static boolean reportNetworkInterface = Config.getProperty("slf4jtoys.report.networkInterface", false);
    /**
     * Default report name.
     * Value is read from system property {@code slf4jtoys.report.name} at application startup, defaults to {@code report}.
     * You may assign a new value at runtime.
     */
    public static String name = Config.getProperty("slf4jtoys.report.name", "report");
}
