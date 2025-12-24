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
package org.usefultoys.slf4j.internal;

import lombok.experimental.UtilityClass;

import java.lang.management.ManagementFactory;

/**
 * Provides a singleton instance of the {@link SystemMetricsCollector}.
 * This class uses the initialization-on-demand holder idiom to ensure that
 * the expensive creation of the collector (which involves querying {@link ManagementFactory})
 * happens only once and in a thread-safe manner.
 *
 * @author Daniel Felix Ferber
 */
@UtilityClass
public class SystemMetrics {

    /**
     * Returns the singleton instance of the {@link SystemMetricsCollector}.
     *
     * @return The shared instance.
     */
    public SystemMetricsCollector getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * The holder class that contains the singleton instance.
     * The instance is created only when the {@link Holder} class is first accessed.
     */
    @UtilityClass
    private class Holder {
        final SystemMetricsCollector INSTANCE = createInstance();

        private SystemMetricsCollector createInstance() {
            return new SystemMetricsCollector(
                    ManagementFactory.getOperatingSystemMXBean(),
                    ManagementFactory.getMemoryMXBean(),
                    ManagementFactory.getClassLoadingMXBean(),
                    ManagementFactory.getCompilationMXBean(),
                    ManagementFactory.getGarbageCollectorMXBeans()
            );
        }
    }
}
