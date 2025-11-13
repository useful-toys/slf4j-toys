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

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.usefultoys.slf4j.utils.ConfigParser;

/**
 * Centralized configuration for controlling the behavior of the {@link Reporter}.
 * <p>
 * This class exposes a set of flags and properties that determine which aspects of the runtime environment
 * are included in a system report. These reports are typically used for diagnostics, debugging, or logging
 * system context information at startup or during runtime.
 * <p>
 * These properties should ideally be defined *before* invoking any method from this library to ensure
 * consistent behavior. Some properties can be modified dynamically at runtime, although care should be taken
 * in concurrent environments.
 * <p>
 * **Security Note:** Some values retrieved from the environment (e.g., system properties, user information)
 * may contain sensitive data. Consider sanitizing reports if logs are shared externally.
 * <p>
 * This is a utility class and should not be instantiated.
 *
 * @author Daniel Felix Ferber
 * @see Reporter
 */
@UtilityClass
public class ReporterConfig {
    static {
        init();
    }

    // System property keys
    /** System property key for enabling/disabling the JVM report. */
    public final String PROP_VM = "slf4jtoys.report.vm";
    /** System property key for enabling/disabling the file system report. */
    public final String PROP_FILE_SYSTEM = "slf4jtoys.report.fileSystem";
    /** System property key for enabling/disabling the memory report. */
    public final String PROP_MEMORY = "slf4jtoys.report.memory";
    /** System property key for enabling/disabling the user report. */
    public final String PROP_USER = "slf4jtoys.report.user";
    /** System property key for enabling/disabling the system properties report. */
    public final String PROP_PROPERTIES = "slf4jtoys.report.properties";
    /** System property key for enabling/disabling the environment variables report. */
    public final String PROP_ENVIRONMENT = "slf4jtoys.report.environment";
    /** System property key for enabling/disabling the physical system report. */
    public final String PROP_PHYSICAL_SYSTEM = "slf4jtoys.report.physicalSystem";
    /** System property key for enabling/disabling the operating system report. */
    public final String PROP_OPERATING_SYSTEM = "slf4jtoys.report.operatingSystem";
    /** System property key for enabling/disabling the calendar report. */
    public final String PROP_CALENDAR = "slf4jtoys.report.calendar";
    /** System property key for enabling/disabling the locale report. */
    public final String PROP_LOCALE = "slf4jtoys.report.locale";
    /** System property key for enabling/disabling the charset report. */
    public final String PROP_CHARSET = "slf4jtoys.report.charset";
    /** System property key for enabling/disabling the network interface report. */
    public final String PROP_NETWORK_INTERFACE = "slf4jtoys.report.networkInterface";
    /** System property key for enabling/disabling the SSL context report. */
    public final String PROP_SSL_CONTEXT = "slf4jtoys.report.SSLContext";
    /** System property key for enabling/disabling the default trust keystore report. */
    public final String PROP_DEFAULT_TRUST_KEYSTORE = "slf4jtoys.report.defaultTrustKeyStore";
    /** System property key for setting the default logger name for reports. */
    public final String PROP_NAME = "slf4jtoys.report.name";

    /**
     * Whether the default report includes Java Virtual Machine (JVM) information.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.vm}. Defaults to {@code true}.
     * Can be changed at runtime.
     */
    public boolean reportVM;

    /**
     * Whether the default report includes information about available and used disk space for file system roots.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.fileSystem}. Defaults to {@code false}.
     * Can be changed at runtime.
     */
    public boolean reportFileSystem;

    /**
     * Whether the default report includes memory usage information (heap, non-heap, etc.).
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.memory}. Defaults to {@code true}.
     * Can be changed at runtime.
     */
    public boolean reportMemory;

    /**
     * Whether the default report includes current user information (name, home directory).
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.user}. Defaults to {@code true}.
     * Can be changed at runtime.
     */
    public boolean reportUser;

    /**
     * Whether the default report includes all Java system properties.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.properties}. Defaults to {@code true}.
     * Can be changed at runtime.
     */
    public boolean reportProperties;

    /**
     * Whether the default report includes all environment variables.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.environment}. Defaults to {@code false}.
     * Can be changed at runtime.
     */
    public boolean reportEnvironment;

    /**
     * Whether the default report includes physical machine information (e.g., number of processors).
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.physicalSystem}. Defaults to {@code true}.
     * Can be changed at runtime.
     */
    public boolean reportPhysicalSystem;

    /**
     * Whether the default report includes operating system information (name, version, architecture).
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.operatingSystem}. Defaults to {@code true}.
     * Can be changed at runtime.
     */
    public boolean reportOperatingSystem;

    /**
     * Whether the default report includes calendar, date, time, and timezone information.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.calendar}. Defaults to {@code true}.
     * Can be changed at runtime.
     */
    public boolean reportCalendar;

    /**
     * Whether the default report includes current and available locales.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.locale}. Defaults to {@code true}.
     * Can be changed at runtime.
     */
    public boolean reportLocale;

    /**
     * Whether the default report includes current and available character sets.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.charset}. Defaults to {@code true}.
     * Can be changed at runtime.
     */
    public boolean reportCharset;

    /**
     * Whether the default report includes network interface information.
     * <p>
     * This operation may block the thread for a significant amount of time.
     * Controlled by the system property {@code slf4jtoys.report.networkInterface}. Defaults to {@code false}.
     * Can be changed at runtime.
     */
    public boolean reportNetworkInterface;

    /**
     * Whether the default report includes SSL context information (e.g., protocols, cipher suites).
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.SSLContext}. Defaults to {@code false}.
     * Can be changed at runtime.
     */
    public boolean reportSSLContext;

    /**
     * Whether the default report includes information about the default trusted keystore.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.defaultTrustKeyStore}. Defaults to {@code false}.
     * Can be changed at runtime.
     */
    public boolean reportDefaultTrustKeyStore;

    /**
     * Defines the default name used for the logger that prints reports.
     * <p>
     * Controlled by the system property {@code slf4jtoys.report.name}. Defaults to {@code "report"}.
     * Can be changed at runtime.
     */
    public String name;

    /**
     * Initializes the configuration attributes by reading the corresponding system properties.
     * This method should be called at application startup to ensure they are properly initialized.
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
     * Attempts to read the value of a system property, gracefully handling {@link SecurityException} if thrown.
     *
     * @param key The name of the system property to retrieve.
     * @return The system property value, or {@code "(Access denied)"} if a security manager prevents access.
     */
    static String getPropertySafely(final @NonNull String key) {
        try {
            return System.getProperty(key);
        } catch (final SecurityException ignored) {
            return "(Access denied)";
        }
    }

    /**
     * Resets all configuration properties to their default values.
     * This method is useful for testing purposes or when reinitializing the configuration.
     */
    void reset() {
        System.clearProperty(ReporterConfig.PROP_VM);
        System.clearProperty(ReporterConfig.PROP_FILE_SYSTEM);
        System.clearProperty(ReporterConfig.PROP_MEMORY);
        System.clearProperty(ReporterConfig.PROP_USER);
        System.clearProperty(ReporterConfig.PROP_PROPERTIES);
        System.clearProperty(ReporterConfig.PROP_ENVIRONMENT);
        System.clearProperty(ReporterConfig.PROP_PHYSICAL_SYSTEM);
        System.clearProperty(ReporterConfig.PROP_OPERATING_SYSTEM);
        System.clearProperty(ReporterConfig.PROP_CALENDAR);
        System.clearProperty(ReporterConfig.PROP_LOCALE);
        System.clearProperty(ReporterConfig.PROP_CHARSET);
        System.clearProperty(ReporterConfig.PROP_NETWORK_INTERFACE);
        System.clearProperty(ReporterConfig.PROP_SSL_CONTEXT);
        System.clearProperty(ReporterConfig.PROP_DEFAULT_TRUST_KEYSTORE);
        System.clearProperty(ReporterConfig.PROP_NAME);
        init();
    }
}
