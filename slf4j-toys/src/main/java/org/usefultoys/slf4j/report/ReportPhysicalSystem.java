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

/**
 * A report module that provides basic information about the physical system, such as the number of available processors.
 * This report is useful for understanding the hardware capabilities of the environment.
 *
 * @author Daniel Felix Ferber
 * @see Reporter
 * @see ReporterConfig#reportPhysicalSystem
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportPhysicalSystem implements Runnable {

    private final @NonNull Logger logger;

    /**
     * Executes the report, writing physical system information to the configured logger.
     * The output is formatted as human-readable INFO messages.
     */
    @Override
    public void run() {
        final Runtime runtime = Runtime.getRuntime();
        @Cleanup
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        ps.println("Physical system");
        ps.printf(" - processors: %d%n", runtime.availableProcessors());
        ps.println(); // Ensure a newline at the end of the report
    }
}
