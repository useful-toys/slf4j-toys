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
 * Reports memory usage of the JVM, including maximum available memory, currently allocated memory, and currently used
 * memory.
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportMemory implements Runnable {

    private final @NonNull Logger logger;

    @Override
    public void run() {
        @Cleanup
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        final Runtime runtime = Runtime.getRuntime();
        ps.println("Memory:");
        final long maxMemory = runtime.maxMemory();
        final long totalMemory = runtime.totalMemory();
        final long freeMemory = runtime.freeMemory();
        ps.printf(" - maximum allowed: %s%n", maxMemory == Long.MAX_VALUE ? "no limit" : UnitFormatter.bytes(maxMemory));
        ps.printf(" - currently allocated: %s (%s more available)%n", UnitFormatter.bytes(totalMemory), UnitFormatter.bytes(maxMemory - totalMemory));
        ps.printf(" - currently used: %s (%s free)%n", UnitFormatter.bytes(totalMemory - freeMemory), UnitFormatter.bytes(freeMemory));
    }
}
