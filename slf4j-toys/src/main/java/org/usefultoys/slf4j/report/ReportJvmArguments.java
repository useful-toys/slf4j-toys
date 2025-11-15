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
import java.util.List;
import java.util.regex.Pattern;

/**
 * A report module that lists all Java Virtual Machine (JVM) input arguments.
 * This includes arguments like heap size settings (-Xmx, -Xms), garbage collector configurations,
 * and system properties passed via -D.
 * This report is crucial for validating the JVM's startup configuration.
 *
 * @author Daniel Felix Ferber
 * @see Reporter
 * @see ReporterConfig#reportJvmArguments
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportJvmArguments implements Runnable {

    private final @NonNull Logger logger;

    /**
     * Executes the report, writing JVM input arguments to the configured logger.
     * The output is formatted as human-readable INFO messages.
     */
    @Override
    public void run() {
        @Cleanup
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        ps.println("JVM Arguments:");

        final List<String> jvmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
        final Pattern forbiddenPattern = Pattern.compile(ReporterConfig.forbiddenPropertyNamesRegex);

        if (jvmArguments.isEmpty()) {
            ps.println(" - No JVM arguments found.");
        } else {
            for (final String arg : jvmArguments) {
                String displayArg = arg;
                // Check if it's a system property argument like -Dkey=value
                if (arg.startsWith("-D")) {
                    int equalsIndex = arg.indexOf('=');
                    if (equalsIndex > 2) { // Ensure it's -Dkey=value and not just -D or -D=
                        String key = arg.substring(2, equalsIndex);
                        if (forbiddenPattern.matcher(key).matches()) {
                            displayArg = arg.substring(0, equalsIndex + 1) + "********"; // Censor sensitive values
                        }
                    }
                }
                ps.printf(" - %s%n", displayArg);
            }
        }
        ps.println();
    }
}
