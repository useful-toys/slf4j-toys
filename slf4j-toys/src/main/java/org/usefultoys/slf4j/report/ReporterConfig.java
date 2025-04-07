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
package org.usefultoys.slf4j.report;

import org.usefultoys.slf4j.internal.Config;

/**
 * Collection of configuration flags that control the behavior of the {@link Reporter}.
 * <p>
 * The initial values are optionally loaded from system properties during application startup. These values may also be explicitly set before invoking any
 * {@link Reporter} methods. Some properties allow reassignment at runtime, depending on their purpose.
 * <p>
 * This class is not meant to be instantiated.
 *
 * @author Daniel Felix Ferber
 */
@SuppressWarnings("CanBeFinal")
public final class ReporterConfig {

    private ReporterConfig() {
        // prevent instances
    }

    /**
     * Whether the default report includes JVM information.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.vm}. Defaults to {@code true}. This value can be changed at runtime.
     */
    public static boolean reportVM = Config.getProperty("slf4jtoys.report.vm", true);
    /**
     * Whether the default report includes information about available and used disk space.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.fileSystem}. Defaults to {@code false}. This value can be changed at runtime.
     */
    public static boolean reportFileSystem = Config.getProperty("slf4jtoys.report.fileSystem", false);
    /**
     * Whether the default report includes memory usage information.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.memory}. Defaults to {@code true}. Can be changed at runtime.
     */
    public static boolean reportMemory = Config.getProperty("slf4jtoys.report.memory", true);

    /**
     * Whether the default report includes current user information.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.user}. Defaults to {@code true}. Can be changed at runtime.
     */
    public static boolean reportUser = Config.getProperty("slf4jtoys.report.user", true);
    /**
     * Whether the default report includes system properties.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.properties}. Defaults to {@code true}. Can be changed at runtime.
     */
    public static boolean reportProperties = Config.getProperty("slf4jtoys.report.properties", true);
    /**
     * Whether the default report includes environment variables.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.environment}. Defaults to {@code true}. Can be changed at runtime.
     */
    public static boolean reportEnvironment = Config.getProperty("slf4jtoys.report.environment", true);

    /**
     * Whether the default report includes physical machine information.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.physicalSystem}. Defaults to {@code true}. Can be changed at runtime.
     */
    public static boolean reportPhysicalSystem = Config.getProperty("slf4jtoys.report.physicalSystem", true);
    /**
     * Whether the default report includes operating system information.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.operatingSystem}. Defaults to {@code true}. Can be changed at runtime.
     */
    public static boolean reportOperatingSystem = Config.getProperty("slf4jtoys.report.operatingSystem", true);
    /**
     * Whether the default report includes calendar, date, time, and timezone information.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.calendar}. Defaults to {@code true}. Can be changed at runtime.
     */
    public static boolean reportCalendar = Config.getProperty("slf4jtoys.report.calendar", true);
    /**
     * Whether the default report includes current and available locales.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.locale}. Defaults to {@code true}. Can be changed at runtime.
     */
    public static boolean reportLocale = Config.getProperty("slf4jtoys.report.locale", true);
    /**
     * Whether the default report includes current and available character sets.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.charset}. Defaults to {@code true}. Can be changed at runtime.
     */
    public static boolean reportCharset = Config.getProperty("slf4jtoys.report.charset", true);
    /**
     * Whether the default report includes network interface information.
     * <p>
     * This operation may block the thread for a significant amount of time. Controlled by the system property {@code slf4jtoys.report.networkInterface}.
     * Defaults to {@code false}. Can be changed at runtime.
     */
    public static boolean reportNetworkInterface = Config.getProperty("slf4jtoys.report.networkInterface", false);
    /**
     * Whether the default report includes SSL context information.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.SSLContext}. Defaults to {@code false}. Can be changed at runtime.
     */
    public static boolean reportSSLContext = Config.getProperty("slf4jtoys.report.SSLContext", false);
    /**
     * Whether the default report includes information about the default trusted keystore.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.defaultTrustKeyStore}. Defaults to {@code false}. Can be changed at runtime.
     */
    public static boolean reportDefaultTrustKeyStore = Config.getProperty("slf4jtoys.report.defaultTrustKeyStore", false);

    /**
     * Default report name.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.name}. Defaults to {@code report}. Can be changed at runtime.
     */
    public static String name = Config.getProperty("slf4jtoys.report.name", "report");
}
