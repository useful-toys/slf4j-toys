/*
 * Copyright 2025 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.usefultoys.slf4j.report;

import org.usefultoys.slf4j.utils.ConfigParser;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Centralized configuration holder for controlling the behavior of the {@link Reporter}.
 * <p>
 * This class exposes a set of flags that determine which aspects of the runtime environment are included in a system
 * report. These reports are typically used for diagnostics, debugging, or logging system context information at startup
 * or during runtime.
 * <p>
 * These properties should ideally be defined <em>before</em> invoking any method from this library, to ensure
 * consistent behavior. Some properties can be modified dynamically at runtime, although care should be taken in
 * concurrent environments.
 * <p>
 * <strong>Security note:</strong> Some values retrieved from the environment (e.g., system properties, user
 * information) may contain sensitive data. Consider sanitizing reports if logs are shared externally.
 * <p>
 * This class is a utility holder and should not be instantiated.
 * <p>
 * * @author Daniel Felix Ferber
 */
@UtilityClass
public class ReporterConfig {
    static {
        init();
    }
    
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
     * Controlled by the system property {@code slf4jtoys.report.vm}. Defaults to {@code true}. May be changed at
     * runtime.
     */
    public boolean reportVM;

    /**
     * Whether the default report includes information about available and used disk space.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.fileSystem}. Defaults to {@code false}. May be changed
     * at runtime.
     */
    public boolean reportFileSystem;

    /**
     * Whether the default report includes memory usage information.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.memory}. Defaults to {@code true}. May be changed at
     * runtime.
     */
    public boolean reportMemory;

    /**
     * Whether the default report includes current user information.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.user}. Defaults to {@code true}. May be changed at
     * runtime.
     */
    public boolean reportUser;

    /**
     * Whether the default report includes system properties.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.properties}. Defaults to {@code true}. May be changed
     * at runtime.
     */
    public boolean reportProperties;

    /**
     * Whether the default report includes environment variables.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.environment}. Defaults to {@code false}. May be changed
     * at runtime.
     */
    public boolean reportEnvironment;

    /**
     * Whether the default report includes physical machine information.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.physicalSystem}. Defaults to {@code true}. May be
     * changed at runtime.
     */
    public boolean reportPhysicalSystem;

    /**
     * Whether the default report includes operating system information.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.operatingSystem}. Defaults to {@code true}. May be
     * changed at runtime.
     */
    public boolean reportOperatingSystem;

    /**
     * Whether the default report includes calendar, date, time, and timezone information.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.calendar}. Defaults to {@code true}. May be changed at
     * runtime.
     */
    public boolean reportCalendar;

    /**
     * Whether the default report includes current and available locales.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.locale}. Defaults to {@code true}. May be changed at
     * runtime.
     */
    public boolean reportLocale;

    /**
     * Whether the default report includes current and available character sets.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.charset}. Defaults to {@code true}. May be changed at
     * runtime.
     */
    public boolean reportCharset;

    /**
     * Whether the default report includes network interface information.
     * <p>
     * This operation may block the thread for a significant amount of time. Controlled by the system property
     * {@code slf4jtoys.report.networkInterface}. Defaults to {@code false}. May be changed at runtime.
     */
    public boolean reportNetworkInterface;

    /**
     * Whether the default report includes SSL context information.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.SSLContext}. Defaults to {@code false}. May be changed
     * at runtime.
     */
    public boolean reportSSLContext;

    /**
     * Whether the default report includes information about the default trusted keystore.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.defaultTrustKeyStore}. Defaults to {@code false}. May
     * be changed at runtime.
     */
    public boolean reportDefaultTrustKeyStore;

    /**
     * Defines the default name used for generated reports.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.name}. Defaults to {@code "report"}. May be changed at
     * runtime.
     */
    public String name;

    /**
     * Initializes the configuration attributes by reading the corresponding system properties.
     * This method should be called before accessing any configuration attributes to ensure
     * they are properly initialized.
     */
    public void init() {
        reportVM = ConfigParser.getProperty(PROP_VM, true);
        reportFileSystem = ConfigParser.getProperty(PROP_FILE_SYSTEM, false);
        reportMemory = ConfigParser.getProperty(PROP_MEMORY, true);
        reportUser = ConfigParser.getProperty(PROP_USER, true);
        reportProperties = ConfigParser.getProperty(PROP_PROPERTIES, true);
        reportEnvironment = ConfigParser.getProperty(PROP_ENVIRONMENT, false);
        reportPhysicalSystem = ConfigParser.getProperty(PROP_PHYSICAL_SYSTEM, true);
        reportOperatingSystem = ConfigParser.getProperty(PROP_OPERATING_SYSTEM, true);
        reportCalendar = ConfigParser.getProperty(PROP_CALENDAR, true);
        reportLocale = ConfigParser.getProperty(PROP_LOCALE, true);
        reportCharset = ConfigParser.getProperty(PROP_CHARSET, true);
        reportNetworkInterface = ConfigParser.getProperty(PROP_NETWORK_INTERFACE, false);
        reportSSLContext = ConfigParser.getProperty(PROP_SSL_CONTEXT, false);
        reportDefaultTrustKeyStore = ConfigParser.getProperty(PROP_DEFAULT_TRUST_KEYSTORE, false);
        name = ConfigParser.getProperty(PROP_NAME, "report");
    }

    /**
     * Attempts to read the given system property while gracefully handling {@link SecurityException}, if thrown.
     *
     * @param key the name of the system property to retrieve
     * @return the system property value, or {@code "(Access denied)"} if not permitted
     */
    static String getPropertySafely(final @NonNull String key) {
        try {
            return System.getProperty(key);
        } catch (final SecurityException ignored) {
            return "(Access denied)";
        }
    }
}
