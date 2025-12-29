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

import static org.usefultoys.slf4j.report.ReporterConfig.getPropertySafely;

/**
 * A report module that provides information about the current user running the application.
 * It includes the user's name, home directory, working directory, and temporary directory.
 * This report is useful for auditing or understanding the execution context.
 *
 * @author Daniel Felix Ferber
 * @see Reporter
 * @see ReporterConfig#reportUser
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportUser implements Runnable {

    private final @NonNull Logger logger;

    /**
     * Executes the report, writing user information to the configured logger.
     * The output is formatted as human-readable INFO messages.
     */
    @Override
    public void run() {
        @Cleanup
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        ps.println("User:");
        ps.printf(" - name: %s%n", getPropertySafely("user.name"));
        ps.printf(" - home directory: %s%n", getPropertySafely("user.home"));
        ps.printf(" - working directory: %s%n", getPropertySafely("user.dir"));
        ps.printf(" - temporary directory: %s%n", getPropertySafely("java.io.tmpdir"));
        ps.println(); // Ensure a newline at the end of the report
    }
}
