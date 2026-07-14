/*
 * Copyright 2026 Daniel Felix Ferber
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
 * This is a utility class and is not meant to be instantiated.
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
    /** System property key for enabling/disabling the JVM arguments report. */
    public final String PROP_JVM_ARGUMENTS = "slf4jtoys.report.jvmArguments";
    /** System property key for enabling/disabling the classpath report. */
    public final String PROP_CLASSPATH = "slf4jtoys.report.classpath";
    /** System property key for enabling/disabling the garbage collector report. */
    public final String PROP_GARBAGE_COLLECTOR = "slf4jtoys.report.garbageCollector";
    /** System property key for enabling/disabling the security providers report. */
    public final String PROP_SECURITY_PROVIDERS = "slf4jtoys.report.securityProviders";
    /** System property key for enabling/disabling the container info report. */
    public final String PROP_CONTAINER_INFO = "slf4jtoys.report.containerInfo";
    /** System property key for setting the default logger name for reports. */
    public final String PROP_NAME = "slf4jtoys.report.name";
    /** System property key for the regular expression defining forbidden property names. */
    public final String PROP_FORBIDDEN_PROPERTY_NAMES_REGEX = "slf4jtoys.report.forbiddenPropertyNamesRegex";


    /**
     * Whether the default report includes Java Virtual Machine (JVM) information.
     * <p>
     * The value is read from the system property {@code slf4jtoys.report.vm}, defaulting to {@code true}.
     * Can be changed at runtime.
     */
    public boolean reportVM;

    /**
     * Whether the default report includes information about available and used disk space for file system roots.
     * <p>
     * The value is read from the system property {@code slf4jtoys.report.fileSystem}, defaulting to {@code false}.
     * Can be changed at runtime.
     */
    public boolean reportFileSystem;

    /**
     * Whether the default report includes memory usage information (heap, non-heap, etc.).
     * <p>
     * The value is read from the system property {@code slf4jtoys.report.memory}, defaulting to {@code true}.
     * Can be changed at runtime.
     */
    public boolean reportMemory;

    /**
     * Whether the default report includes current user information (name, home directory).
     * <p>
     * The value is read from the system property {@code slf4jtoys.report.user}, defaulting to {@code true}.
     * Can be changed at runtime.
     */
    public boolean reportUser;

    /**
     * Whether the default report includes all Java system properties.
     * <p>
     * The value is read from the system property {@code slf4jtoys.report.properties}, defaulting to {@code true}.
     * Can be changed at runtime.
     */
    public boolean reportProperties;

    /**
     * Whether the default report includes all environment variables.
     * <p>
     * The value is read from the system property {@code slf4jtoys.report.environment}, defaulting to {@code false}.
     * Can be changed at runtime.
     */
    public boolean reportEnvironment;

    /**
     * Whether the default report includes physical machine information (e.g., number of processors).
     * <p>
     * The value is read from the system property {@code slf4jtoys.report.physicalSystem}, defaulting to {@code true}.
     * Can be changed at runtime.
     */
    public boolean reportPhysicalSystem;

    /**
     * Whether the default report includes operating system information (name, version, architecture).
     * <p>
     * The value is read from the system property {@code slf4jtoys.report.operatingSystem}, defaulting to {@code true}.
     * Can be changed at runtime.
     */
    public boolean reportOperatingSystem;

    /**
     * Whether the default report includes calendar, date, time, and timezone information.
     * <p>
     * The value is read from the system property {@code slf4jtoys.report.calendar}, defaulting to {@code true}.
     * Can be changed at runtime.
     */
    public boolean reportCalendar;

    /**
     * Whether the default report includes current and available locales.
     * <p>
     * The value is read from the system property {@code slf4jtoys.report.locale}, defaulting to {@code true}.
     * Can be changed at runtime.
     */
    public boolean reportLocale;

    /**
     * Whether the default report includes current and available character sets.
     * <p>
     * The value is read from the system property {@code slf4jtoys.report.charset}, defaulting to {@code true}.
     * Can be changed at runtime.
     */
    public boolean reportCharset;

    /**
     * Whether the default report includes network interface information.
     * <p>
     * This operation may block the thread for a significant amount of time.
     * The value is read from the system property {@code slf4jtoys.report.networkInterface}, defaulting to {@code false}.
     * Can be changed at runtime.
     */
    public boolean reportNetworkInterface;

    /**
     * Whether the default report includes SSL context information (e.g., protocols, cipher suites).
     * <p>
     * The value is read from the system property {@code slf4jtoys.report.SSLContext}, defaulting to {@code false}.
     * Can be changed at runtime.
     */
    public boolean reportSSLContext;

    /**
     * Whether the default report includes information about the default trusted keystore.
     * <p>
     * The value is read from the system property {@code slf4jtoys.report.defaultTrustKeyStore}, defaulting to {@code false}.
     * Can be changed at runtime.
     */
    public boolean reportDefaultTrustKeyStore;

    /**
     * Whether the default report includes JVM arguments.
     * <p>
     * The value is read from the system property {@code slf4jtoys.report.jvmArguments}, defaulting to {@code false}.
     * Can be changed at runtime.
     */
    public boolean reportJvmArguments;

    /**
     * Whether the default report includes classpath information.
     * <p>
     * The value is read from the system property {@code slf4jtoys.report.classpath}, defaulting to {@code false}.
     * Can be changed at runtime.
     */
    public boolean reportClasspath;

    /**
     * Whether the default report includes garbage collector information.
     * <p>
     * The value is read from the system property {@code slf4jtoys.report.garbageCollector}, defaulting to {@code false}.
     * Can be changed at runtime.
     */
    public boolean reportGarbageCollector;

    /**
     * Whether the default report includes security providers information.
     * <p>
     * The value is read from the system property {@code slf4jtoys.report.securityProviders}, defaulting to {@code false}.
     * Can be changed at runtime.
     */
    public boolean reportSecurityProviders;

    /**
     * Whether the default report includes container information.
     * <p>
     * The value is read from the system property {@code slf4jtoys.report.containerInfo}, defaulting to {@code false}.
     * Can be changed at runtime.
     */
    public boolean reportContainerInfo;

    /**
     * Defines the default name used for the logger that prints reports.
     * <p>
     * The value is read from the system property {@code slf4jtoys.report.name}, defaulting to {@code "report"}.
     * Can be changed at runtime.
     */
    public String name;

    /**
     * A regular expression used to identify sensitive property names (system properties or environment variables)
     * whose values should be censored in reports.
     * <p>
     * The value is read from the system property {@code slf4jtoys.report.forbiddenPropertyNamesRegex}, defaulting
     * to {@code "(?i).*password.*|.*secret.*|.*key.*|.*token.*"}.
     * The {@code (?i)} flag makes the regex case-insensitive.
     * Can be changed at runtime.
     */
    public String forbiddenPropertyNamesRegex;


    /**
     * Initializes the configuration properties by reading values from system properties.
     * <p>
     * This method is automatically called in a static initializer when the class is first loaded.
     * It can also be called manually to reload configuration from system properties after they have been modified.
     * <p>
     * For consistent behavior, ensure system properties are set before this class is first accessed.
     */
    public void init() {
        reportVM = ConfigParser.getBooleanProperty(PROP_VM, true);
        reportFileSystem = ConfigParser.getBooleanProperty(PROP_FILE_SYSTEM, false);
        reportMemory = ConfigParser.getBooleanProperty(PROP_MEMORY, true);
        reportUser = ConfigParser.getBooleanProperty(PROP_USER, false);
        reportProperties = ConfigParser.getBooleanProperty(PROP_PROPERTIES, false);
        reportEnvironment = ConfigParser.getBooleanProperty(PROP_ENVIRONMENT, false);
        reportPhysicalSystem = ConfigParser.getBooleanProperty(PROP_PHYSICAL_SYSTEM, true);
        reportOperatingSystem = ConfigParser.getBooleanProperty(PROP_OPERATING_SYSTEM, true);
        reportCalendar = ConfigParser.getBooleanProperty(PROP_CALENDAR, false);
        reportLocale = ConfigParser.getBooleanProperty(PROP_LOCALE, false);
        reportCharset = ConfigParser.getBooleanProperty(PROP_CHARSET, false);
        reportNetworkInterface = ConfigParser.getBooleanProperty(PROP_NETWORK_INTERFACE, false);
        reportSSLContext = ConfigParser.getBooleanProperty(PROP_SSL_CONTEXT, false);
        reportDefaultTrustKeyStore = ConfigParser.getBooleanProperty(PROP_DEFAULT_TRUST_KEYSTORE, false);
        reportJvmArguments = ConfigParser.getBooleanProperty(PROP_JVM_ARGUMENTS, false);
        reportClasspath = ConfigParser.getBooleanProperty(PROP_CLASSPATH, false);
        reportGarbageCollector = ConfigParser.getBooleanProperty(PROP_GARBAGE_COLLECTOR, false);
        reportSecurityProviders = ConfigParser.getBooleanProperty(PROP_SECURITY_PROVIDERS, false);
        reportContainerInfo = ConfigParser.getBooleanProperty(PROP_CONTAINER_INFO, false);
        name = ConfigParser.getStringProperty(PROP_NAME, "report");
        forbiddenPropertyNamesRegex = ConfigParser.getStringProperty(PROP_FORBIDDEN_PROPERTY_NAMES_REGEX, "(?i).*password.*|.*secret.*|.*key.*|.*token.*");
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
     * Resets the configuration properties to their default values.
     * This method is useful for testing or re-initializing the configuration.
     */
    public void reset() {
        System.clearProperty(PROP_VM);
        System.clearProperty(PROP_FILE_SYSTEM);
        System.clearProperty(PROP_MEMORY);
        System.clearProperty(PROP_USER);
        System.clearProperty(PROP_PROPERTIES);
        System.clearProperty(PROP_ENVIRONMENT);
        System.clearProperty(PROP_PHYSICAL_SYSTEM);
        System.clearProperty(PROP_OPERATING_SYSTEM);
        System.clearProperty(PROP_CALENDAR);
        System.clearProperty(PROP_LOCALE);
        System.clearProperty(PROP_CHARSET);
        System.clearProperty(PROP_NETWORK_INTERFACE);
        System.clearProperty(PROP_SSL_CONTEXT);
        System.clearProperty(PROP_DEFAULT_TRUST_KEYSTORE);
        System.clearProperty(PROP_JVM_ARGUMENTS);
        System.clearProperty(PROP_CLASSPATH);
        System.clearProperty(PROP_GARBAGE_COLLECTOR);
        System.clearProperty(PROP_SECURITY_PROVIDERS);
        System.clearProperty(PROP_CONTAINER_INFO);
        System.clearProperty(PROP_NAME);
        System.clearProperty(PROP_FORBIDDEN_PROPERTY_NAMES_REGEX);
        init();
    }
}
