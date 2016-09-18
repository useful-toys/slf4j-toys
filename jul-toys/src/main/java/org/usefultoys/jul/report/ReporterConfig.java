/*
 * Copyright 2016 Daniel Felix Ferber.
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
package org.usefultoys.jul.report;

import org.usefultoys.jul.internal.Config;

/**
 * Collection of properties that drive {@link Reporter} behavior.
 *
 * @author Daniel Felix Ferber
 */
public class ReporterConfig {

    /**
     * If default report includes JVM information.
     * Value is read from system property {@code jultoys.report} at application startup, defaults to {@code true}.
     * You may assign a new value at runtime.
     */
    public static boolean reportVM = Config.getProperty("jultoys.report.vm", true);
    /**
     * If default report includes available and used disk storage information.
     * Value is read from system property {@code jultoys.report.fileSystem} at application startup, defaults to {@code false}.
     * You may assign a new value at runtime.
     */
    public static boolean reportFileSystem = Config.getProperty("jultoys.report.fileSystem", false);
    /**
     * If default report includes available and used memory information.
     * Value is read from system property {@code jultoys.report.networkInterface} at application startup, defaults to {@code false}.
     * You may assign a new value at runtime.
     */
    public static boolean reportMemory = Config.getProperty("jultoys.report.memory", true);
    /**
     * If default report includes current user information.
     * Value is read from system property {@code jultoys.report.memory} at application startup, defaults to {@code true}.
     * You may assign a new value at runtime.
     */
    public static boolean reportUser = Config.getProperty("jultoys.report.user", true);
    /**
     * If default report includes physical machine information.
     * Value is read from system property {@code jultoys.report.user} at application startup, defaults to {@code true}.
     * You may assign a new value at runtime.
     */
    public static boolean reportPhysicalSystem = Config.getProperty("jultoys.report.physicalSystem", true);
    /**
     * If default report includes operating system information.
     * Value is read from system property {@code jultoys.report.physicalSystem} at application startup, defaults to {@code true}.
     * You may assign a new value at runtime.
     */
    public static boolean reportOperatingSystem = Config.getProperty("jultoys.report.operatingSystem", true);
    /**
     * If default report includes date, time, calendar and timezone information.
     * Value is read from system property {@code jultoys.report.operatingSystem} at application startup, defaults to {@code true}.
     * You may assign a new value at runtime.
     */
    public static boolean reportCalendar = Config.getProperty("jultoys.report.calendar", true);
    /**
     * If default report includes current and available locale information.
     * Value is read from system property {@code jultoys.report.calendar} at application startup, defaults to {@code true}.
     * You may assign a new value at runtime.
     */
    public static boolean reportLocale = Config.getProperty("jultoys.report.locale", true);
    /**
     * If default report includes current and available charset information.
     * Value is read from system property {@code jultoys.report.locale} at application startup, defaults to {@code true}.
     * You may assign a new value at runtime.
     */
    public static boolean reportCharset = Config.getProperty("jultoys.report.charset", true);
    /**
     * If default report includes available network interface information.
     * This report may block the thread for considerable amount of time.
     * Value is read from system property {@code jultoys.report.charset} at application startup, defaults to {@code true}.
     * You may assign a new value at runtime.
     */
    public static boolean reportNetworkInterface = Config.getProperty("jultoys.report.networkInterface", false);
    /**
     * Default report name.
     * Value is read from system property {@code jultoys.report.name} at application startup, defaults to {@code report}.
     * You may assign a new value at runtime.
     */
    public static String name = Config.getProperty("jultoys.report.name", "report");
}
