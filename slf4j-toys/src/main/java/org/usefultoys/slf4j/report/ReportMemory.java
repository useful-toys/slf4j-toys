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

import lombok.Cleanup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.utils.UnitFormatter;

import java.io.PrintStream;

/**
 * A report module that provides information about the Java Virtual Machine's (JVM) memory usage.
 * It reports the maximum available memory, currently allocated memory, and currently used memory.
 * This report is crucial for identifying memory leaks or tuning memory allocation.
 *
 * @author Daniel Felix Ferber
 * @see Reporter
 * @see ReporterConfig#reportMemory
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportMemory implements Runnable {

    private final @NonNull Logger logger;

    /**
     * Interface for providing memory information to the report.
     * <p>
     * This interface can be overridden in tests to simulate different memory scenarios.
     * The default implementation uses the JVM's {@link Runtime} class.
     */
    protected interface MemoryInfoProvider {
        /**
         * @return the maximum amount of memory that the JVM will attempt to use (in bytes)
         */
        long maxMemory();
        /**
         * @return the total amount of memory currently allocated to the JVM (in bytes)
         */
        long totalMemory();
        /**
         * @return the amount of free memory in the JVM (in bytes)
         */
        long freeMemory();
    }

    /**
     * Returns the provider for memory information used by this report.
     * <p>
     * The default implementation returns a provider based on the JVM's {@link Runtime}.
     * This method can be overridden in subclasses for testing or custom memory sources.
     *
     * @return a MemoryInfoProvider instance
     */
    protected MemoryInfoProvider getMemoryInfoProvider() {
        return new RuntimeMemoryInfoProvider();
    }

    /**
     * Executes the report, writing memory usage information to the configured logger.
     * The output is formatted as human-readable INFO messages.
     */
    @Override
    public void run() {
        @Cleanup
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        final MemoryInfoProvider memoryInfoProvider = getMemoryInfoProvider();
        final long maxMemory = memoryInfoProvider.maxMemory();
        final long totalMemory = memoryInfoProvider.totalMemory();
        final long freeMemory = memoryInfoProvider.freeMemory();
        ps.println("Memory:");
        ps.printf(" - maximum allowed: %s%n",
                maxMemory == Long.MAX_VALUE ? "no limit" : UnitFormatter.bytes(maxMemory));
        ps.printf(" - currently allocated: %s (%s more available)%n",
                UnitFormatter.bytes(totalMemory),
                maxMemory == Long.MAX_VALUE ? "n/a" : UnitFormatter.bytes(maxMemory - totalMemory));
        ps.printf(" - currently used: %s (%s free)%n",
                UnitFormatter.bytes(totalMemory - freeMemory),
                UnitFormatter.bytes(freeMemory));
        ps.println(); // Ensure a newline at the end of the report
    }

    /**
     * Default implementation of MemoryInfoProvider using the JVM's {@link Runtime}.
     */
    private static class RuntimeMemoryInfoProvider implements MemoryInfoProvider {
        private final Runtime runtime = Runtime.getRuntime();

        /**
         * Returns the maximum amount of memory that the JVM will attempt to use (in bytes).
         * @return the maximum memory in bytes
         */
        @Override
        public long maxMemory() { return runtime.maxMemory(); }
        /**
         * Returns the total amount of memory currently allocated to the JVM (in bytes).
         * @return the total allocated memory in bytes
         */
        @Override
        public long totalMemory() { return runtime.totalMemory(); }
        /**
         * Returns the amount of free memory in the JVM (in bytes).
         * @return the free memory in bytes
         */
        @Override
        public long freeMemory() { return runtime.freeMemory(); }
    }
}
