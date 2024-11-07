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

import org.usefultoys.slf4j.internal.Config;
import org.usefultoys.slf4j.internal.SystemData;

/**
 * Collection of properties that drive {@link SystemData} behavior.
 *
 * @author Daniel Felix Ferber
 */
@SuppressWarnings("CanBeFinal")
public final class SystemConfig {
    /**
     * If Sun native OperatingSystemMXBean is available.
     */
    public static final boolean hasSunOperatingSystemMXBean;

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

    /**
     * If memory usage status is retrieved from MemoryMXBean.
     * Not all JVM may support or allow MemoryMXBean usage.
     * Value is read from system property {@code slf4jtoys.useMemoryManagedBean} at application startup, defaults to {@code false}.
     * You may assign a new value at runtime.
     */
    public static boolean useMemoryManagedBean = Config.getProperty("slf4jtoys.useMemoryManagedBean", false);
    /**
     * If class loading status is retrieved from ClassLoadingMXBean.
     * Not all JVM may support or allow ClassLoadingMXBean usage.
     * Value is read from system property {@code slf4jtoys.useClassLoadingManagedBean} at application startup, defaults to {@code false}.
     * You may assign a new value at runtime.
     */
    public static boolean useClassLoadingManagedBean = Config.getProperty("slf4jtoys.useClassLoadingManagedBean", false);
    /**
     * If JIT compiler status is retrieved from CompilationMXBean.
     * Not all JVM may support or allow CompilationMXBean usage.
     * Value is read from system property {@code slf4jtoys.useCompilationManagedBean} at application startup, defaults to {@code false}.
     * You may assign a new value at runtime.
     */
    public static boolean useCompilationManagedBean = Config.getProperty("slf4jtoys.useCompilationManagedBean", false);
    /**
     * If garbage collector status is retrieved from GarbageCollectorMXBean.
     * Not all JVM may support or allow GarbageCollectorMXBean usage.
     * Value is read from system property {@code slf4jtoys.useGarbageCollectionManagedBean} at application startup, defaults to {@code false}.
     * You may assign a new value at runtime.
     */
    public static boolean useGarbageCollectionManagedBean = Config.getProperty("slf4jtoys.useGarbageCollectionManagedBean", false);
    /**
     * If operating system status is retrieved from OperatingSystemMXBean.
     * Not all JVM may support or allow OperatingSystemMXBean usage.
     * Value is read from system property {@code slf4jtoys.usePlatformManagedBean} at application startup, defaults to {@code false}.
     * You may assign a new value at runtime.
     */
    public static boolean usePlatformManagedBean = Config.getProperty("slf4jtoys.usePlatformManagedBean", false);

    private SystemConfig() {
    }
}
