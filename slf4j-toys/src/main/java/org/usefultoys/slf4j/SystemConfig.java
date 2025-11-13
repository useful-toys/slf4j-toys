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
package org.usefultoys.slf4j;

import lombok.experimental.UtilityClass;
import org.usefultoys.slf4j.internal.SystemData;
import org.usefultoys.slf4j.utils.ConfigParser;

/**
 * Centralized configuration for controlling how {@link SystemData} gathers runtime metrics from the Java platform.
 * <p>
 * This class enables or disables the querying of various {@code java.lang.management.*MXBean} interfaces for data collection.
 * These interfaces may not be available in all JVM implementations or may be restricted by a security manager.
 * <p>
 * For consistent behavior, these properties should be set before any methods from this library are called.
 * While some properties can be modified at runtime, caution is advised in concurrent environments.
 * <p>
 * This is a utility class and is not meant to be instantiated.
 *
 * @author Daniel Felix Ferber
 * @see SystemData
 */
@UtilityClass
public class SystemConfig {
    static {
        init();
    }

    // System property keys
    /** System property key for enabling/disabling memory MXBean usage. */
    public final String PROP_USE_MEMORY_MANAGED_BEAN = "slf4jtoys.useMemoryManagedBean";
    /** System property key for enabling/disabling class loading MXBean usage. */
    public final String PROP_USE_CLASS_LOADING_MANAGED_BEAN = "slf4jtoys.useClassLoadingManagedBean";
    /** System property key for enabling/disabling compilation MXBean usage. */
    public final String PROP_USE_COMPILATION_MANAGED_BEAN = "slf4jtoys.useCompilationManagedBean";
    /** System property key for enabling/disabling garbage collection MXBean usage. */
    public final String PROP_USE_GARBAGE_COLLECTION_MANAGED_BEAN = "slf4jtoys.useGarbageCollectionManagedBean";
    /** System property key for enabling/disabling platform MXBean usage. */
    public final String PROP_USE_PLATFORM_MANAGED_BEAN = "slf4jtoys.usePlatformManagedBean";

    /**
     * Determines whether memory usage metrics are retrieved from the {@link java.lang.management.MemoryMXBean}.
     * <p>
     * The value is read from the system property {@code slf4jtoys.useMemoryManagedBean}, defaulting to {@code false}.
     * It can be changed at runtime.
     */
    public boolean useMemoryManagedBean;
    /**
     * Determines whether class loading metrics are retrieved from the {@link java.lang.management.ClassLoadingMXBean}.
     * <p>
     * The value is read from the system property {@code slf4jtoys.useClassLoadingManagedBean}, defaulting to {@code false}.
     * It can be changed at runtime.
     */
    public boolean useClassLoadingManagedBean;
    /**
     * Determines whether JIT compiler metrics are retrieved from the {@link java.lang.management.CompilationMXBean}.
     * <p>
     * The value is read from the system property {@code slf4jtoys.useCompilationManagedBean}, defaulting to {@code false}.
     * It can be changed at runtime.
     */
    public boolean useCompilationManagedBean;
    /**
     * Determines whether garbage collection metrics are retrieved from the {@link java.lang.management.GarbageCollectorMXBean}s.
     * <p>
     * The value is read from the system property {@code slf4jtoys.useGarbageCollectionManagedBean}, defaulting to {@code false}.
     * It can be changed at runtime.
     */
    public boolean useGarbageCollectionManagedBean;
    /**
     * Determines whether operating system metrics are retrieved from the {@link java.lang.management.OperatingSystemMXBean}.
     * <p>
     * The value is read from the system property {@code slf4jtoys.usePlatformManagedBean}, defaulting to {@code false}.
     * It can be changed at runtime.
     */
    public boolean usePlatformManagedBean;

    /**
     * Initializes the configuration properties. This method should be called at application startup to ensure
     * consistent behavior.
     */
    public void init() {
        useMemoryManagedBean = ConfigParser.getProperty(PROP_USE_MEMORY_MANAGED_BEAN, false);
        useClassLoadingManagedBean = ConfigParser.getProperty(PROP_USE_CLASS_LOADING_MANAGED_BEAN, false);
        useCompilationManagedBean = ConfigParser.getProperty(PROP_USE_COMPILATION_MANAGED_BEAN, false);
        useGarbageCollectionManagedBean = ConfigParser.getProperty(PROP_USE_GARBAGE_COLLECTION_MANAGED_BEAN, false);
        usePlatformManagedBean = ConfigParser.getProperty(PROP_USE_PLATFORM_MANAGED_BEAN, false);
    }

    /**
     * Resets the configuration properties to their default values.
     * This method is useful for testing or re-initializing the configuration.
     */
    public void reset() {
        System.clearProperty(PROP_USE_MEMORY_MANAGED_BEAN);
        System.clearProperty(PROP_USE_CLASS_LOADING_MANAGED_BEAN);
        System.clearProperty(PROP_USE_COMPILATION_MANAGED_BEAN);
        System.clearProperty(PROP_USE_GARBAGE_COLLECTION_MANAGED_BEAN);
        System.clearProperty(PROP_USE_PLATFORM_MANAGED_BEAN);
        init();
    }
}
