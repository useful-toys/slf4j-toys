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
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Arrays;
import java.util.List;

/**
 * A report module that lists all entries in the Java application's classpath.
 * This report is useful for diagnosing classpath-related issues, such as missing
 * dependencies or version conflicts.
 *
 * @author Daniel Felix Ferber
 * @see Reporter
 * @see ReporterConfig#reportClasspath
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportClasspath implements Runnable {

    private final @NonNull Logger logger;

    /**
     * Executes the report, writing classpath information to the configured logger.
     * The output is formatted as human-readable INFO messages.
     */
    @Override
    public void run() {
        @Cleanup
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        ps.println("Classpath:");

        final RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        final String classpath = runtimeMxBean.getClassPath();
        final String pathSeparator = runtimeMxBean.getSystemProperties().get("path.separator");

        if (classpath == null || classpath.isEmpty()) {
            ps.println(" - Classpath is empty.");
        } else {
            final List<String> classpathEntries = Arrays.asList(classpath.split(pathSeparator));
            for (final String entry : classpathEntries) {
                ps.printf(" - %s%n", entry);
            }
        }
        ps.println();
    }
}
