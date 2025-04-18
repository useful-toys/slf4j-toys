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

import lombok.experimental.UtilityClass;
import org.usefultoys.slf4j.internal.Config;
import org.usefultoys.slf4j.internal.SystemData;

/**
 * Centralized configuration holder for controlling how {@link SystemData} gathers runtime metrics from the Java
 * platform.
 * <p>
 * This class determines whether various {@code java.lang.management.*MXBean} interfaces are queried to collect data.
 * These interfaces may not be available in all JVM implementations or may be restricted by the security manager. * <p>
 * These properties should ideally be defined <em>before</em> invoking any method from this library, to ensure
 * consistent behavior. Some properties can be modified dynamically at runtime, although care should be taken in
 * concurrent environments.
 * <p>
 * This class is a utility holder and should not be instantiated.
 *
 * @author Daniel Felix Ferber
 */
@UtilityClass
public class SystemConfig {
    /**
     * Whether the Sun-specific {@code com.sun.management.OperatingSystemMXBean} is available in the current JVM.
     * <p>
     * This is detected automatically at class load time and cannot be changed at runtime.
     */
    public final boolean hasSunOperatingSystemMXBean;

    static {
        boolean tmpHasSunOperatingSystemMXBean = false;
        try {
            Class.forName("com.sun.management.OperatingSystemMXBean");
            tmpHasSunOperatingSystemMXBean = true;
        } catch (final ClassNotFoundException ignored) {
            // ignora
        }
        hasSunOperatingSystemMXBean = tmpHasSunOperatingSystemMXBean;
    }

    public final String SLF_4_JTOYS_USE_MEMORY_MANAGED_BEAN = "slf4jtoys.useMemoryManagedBean";
    public final String PROP_USE_CLASS_LOADING_MANAGED_BEAN = "slf4jtoys.useClassLoadingManagedBean";
    public final String PROP_USE_COMPILATION_MANAGED_BEAN = "slf4jtoys.useCompilationManagedBean";
    public final String PROP_USE_GARBAGE_COLLECTION_MANAGED_BEAN = "slf4jtoys.useGarbageCollectionManagedBean";
    public final String PROP_USE_PLATFORM_MANAGED_BEAN = "slf4jtoys.usePlatformManagedBean";

    /**
     * Whether memory usage metrics are retrieved from the {@link java.lang.management.MemoryMXBean}.
     * <p>
     * Controlled by the system property {@code slf4jtoys.useMemoryManagedBean}. Defaults to {@code false}. May be
     * changed at runtime.
     */
    public boolean useMemoryManagedBean = Config.getProperty(SLF_4_JTOYS_USE_MEMORY_MANAGED_BEAN, false);
    /**
     * Whether class loading metrics are retrieved from the {@link java.lang.management.ClassLoadingMXBean}.
     * <p>
     * Controlled by the system property {@code slf4jtoys.useClassLoadingManagedBean}. Defaults to {@code false}. May be
     * changed at runtime.
     */
    public boolean useClassLoadingManagedBean = Config.getProperty(PROP_USE_CLASS_LOADING_MANAGED_BEAN, false);
    /**
     * Whether JIT compiler metrics are retrieved from the {@link java.lang.management.CompilationMXBean}.
     * <p>
     * Controlled by the system property {@code slf4jtoys.useCompilationManagedBean}. Defaults to {@code false}. May be
     * changed at runtime.
     */
    public boolean useCompilationManagedBean = Config.getProperty(PROP_USE_COMPILATION_MANAGED_BEAN, false);
    /**
     * Whether garbage collection metrics are retrieved from the {@link java.lang.management.GarbageCollectorMXBean}s.
     * <p>
     * Controlled by the system property {@code slf4jtoys.useGarbageCollectionManagedBean}. Defaults to {@code false}.
     * May be changed at runtime.
     */
    public boolean useGarbageCollectionManagedBean = Config.getProperty(PROP_USE_GARBAGE_COLLECTION_MANAGED_BEAN, false);
    /**
     * Whether operating system metrics are retrieved from the {@link java.lang.management.OperatingSystemMXBean}.
     * <p>
     * Controlled by the system property {@code slf4jtoys.usePlatformManagedBean}. Defaults to {@code false}. May be
     * changed at runtime.
     */
    public boolean usePlatformManagedBean = Config.getProperty(PROP_USE_PLATFORM_MANAGED_BEAN, false);
}
