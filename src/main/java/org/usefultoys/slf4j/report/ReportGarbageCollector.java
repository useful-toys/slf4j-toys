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

import java.io.PrintStream;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * A report module that provides information about the Java Virtual Machine's (JVM)
 * garbage collectors. It lists the name, collection count, and collection time
 * for each active garbage collector.
 * This report is useful for diagnosing performance issues related to memory management.
 *
 * @author Daniel Felix Ferber
 * @see Reporter
 * @see ReporterConfig#reportGarbageCollector
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportGarbageCollector implements Runnable {

    private final @NonNull Logger logger;

    /**
     * Executes the report, writing garbage collector information to the configured logger.
     * The output is formatted as human-readable INFO messages.
     */
    @Override
    public void run() {
        @Cleanup
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        ps.println("Garbage Collectors:");

        final List<GarbageCollectorMXBean> gcMxBeans = ManagementFactory.getGarbageCollectorMXBeans();

        if (gcMxBeans.isEmpty()) {
            ps.println(" - No garbage collectors found.");
        } else {
            for (final GarbageCollectorMXBean gcBean : gcMxBeans) {
                ps.printf(" - Name: %s%n", gcBean.getName());
                ps.printf("   Collection Count: %d%n", gcBean.getCollectionCount());
                ps.printf("   Collection Time: %d ms%n", gcBean.getCollectionTime());
                ps.printf("   Memory Pool Names: %s%n", String.join(", ", gcBean.getMemoryPoolNames()));
            }
        }
        ps.println();
    }
}
