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

import lombok.NonNull;
import lombok.experimental.UtilityClass;
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
@UtilityClass
public class ReporterConfig {

    // System property keys
    public final String PROP_VM = "slf4jtoys.report.vm";
    public final String PROP_FILE_SYSTEM = "slf4jtoys.report.fileSystem";
    public final String PROP_MEMORY = "slf4jtoys.report.memory";
    public final String PROP_USER = "slf4jtoys.report.user";
    public final String PROP_PROPERTIES = "slf4jtoys.report.properties";
    public final String PROP_ENVIRONMENT = "slf4jtoys.report.environment";
    public final String PROP_PHYSICAL_SYSTEM = "slf4jtoys.report.physicalSystem";
    public final String PROP_OPERATING_SYSTEM = "slf4jtoys.report.operatingSystem";
    public final String PROP_CALENDAR = "slf4jtoys.report.calendar";
    public final String PROP_LOCALE = "slf4jtoys.report.locale";
    public final String PROP_CHARSET = "slf4jtoys.report.charset";
    public final String PROP_NETWORK_INTERFACE = "slf4jtoys.report.networkInterface";
    public final String PROP_SSL_CONTEXT = "slf4jtoys.report.SSLContext";
    public final String PROP_DEFAULT_TRUST_KEYSTORE = "slf4jtoys.report.defaultTrustKeyStore";
    public final String PROP_NAME = "slf4jtoys.report.name";

    /**
     * Whether the default report includes JVM information.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.vm}. Defaults to {@code true}. This value can be changed at runtime.
     */
    public boolean reportVM = Config.getProperty(PROP_VM, true);

    /**
     * Whether the default report includes information about available and used disk space.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.fileSystem}. Defaults to {@code false}. This value can be changed at runtime.
     */
    public boolean reportFileSystem = Config.getProperty(PROP_FILE_SYSTEM, false);

    /**
     * Whether the default report includes memory usage information.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.memory}. Defaults to {@code true}. Can be changed at runtime.
     */
    public boolean reportMemory = Config.getProperty(PROP_MEMORY, true);

    /**
     * Whether the default report includes current user information.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.user}. Defaults to {@code true}. Can be changed at runtime.
     */
    public boolean reportUser = Config.getProperty(PROP_USER, true);

    /**
     * Whether the default report includes system properties.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.properties}. Defaults to {@code true}. Can be changed at runtime.
     */
    public boolean reportProperties = Config.getProperty(PROP_PROPERTIES, true);

    /**
     * Whether the default report includes environment variables.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.environment}. Defaults to {@code false}. Can be changed at runtime.
     */
    public boolean reportEnvironment = Config.getProperty(PROP_ENVIRONMENT, false);

    /**
     * Whether the default report includes physical machine information.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.physicalSystem}. Defaults to {@code true}. Can be changed at runtime.
     */
    public boolean reportPhysicalSystem = Config.getProperty(PROP_PHYSICAL_SYSTEM, true);

    /**
     * Whether the default report includes operating system information.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.operatingSystem}. Defaults to {@code true}. Can be changed at runtime.
     */
    public boolean reportOperatingSystem = Config.getProperty(PROP_OPERATING_SYSTEM, true);

    /**
     * Whether the default report includes calendar, date, time, and timezone information.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.calendar}. Defaults to {@code true}. Can be changed at runtime.
     */
    public boolean reportCalendar = Config.getProperty(PROP_CALENDAR, true);

    /**
     * Whether the default report includes current and available locales.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.locale}. Defaults to {@code true}. Can be changed at runtime.
     */
    public boolean reportLocale = Config.getProperty(PROP_LOCALE, true);

    /**
     * Whether the default report includes current and available character sets.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.charset}. Defaults to {@code true}. Can be changed at runtime.
     */
    public boolean reportCharset = Config.getProperty(PROP_CHARSET, true);

    /**
     * Whether the default report includes network interface information.
     * <p>
     * This operation may block the thread for a significant amount of time. Controlled by the system property {@code slf4jtoys.report.networkInterface}.
     * Defaults to {@code false}. Can be changed at runtime.
     */
    public boolean reportNetworkInterface = Config.getProperty(PROP_NETWORK_INTERFACE, false);

    /**
     * Whether the default report includes SSL context information.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.SSLContext}. Defaults to {@code false}. Can be changed at runtime.
     */
    public boolean reportSSLContext = Config.getProperty(PROP_SSL_CONTEXT, false);

    /**
     * Whether the default report includes information about the default trusted keystore.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.defaultTrustKeyStore}. Defaults to {@code false}. Can be changed at runtime.
     */
    public boolean reportDefaultTrustKeyStore = Config.getProperty(PROP_DEFAULT_TRUST_KEYSTORE, false);

    /**
     * Default report name.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.name}. Defaults to {@code report}. Can be changed at runtime.
     */
    public String name = Config.getProperty(PROP_NAME, "report");

    static String getPropertySafely(final @NonNull String key) {
        try {
            return System.getProperty(key);
        } catch (final SecurityException ignored) {
            return "(Access denied)";
        }
    }
}
