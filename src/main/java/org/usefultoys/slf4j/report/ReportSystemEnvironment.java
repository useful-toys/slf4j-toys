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
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * A report module that lists all environment variables available to the current Java process.
 * <p>
 * This report can be useful for debugging application behavior that depends on environment settings.
 * However, it may contain sensitive information, so caution is advised when logging this report.
 *
 * @author Daniel Felix Ferber
 * @see Reporter
 * @see ReporterConfig#reportEnvironment
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportSystemEnvironment implements Runnable {

    private final @NonNull Logger logger;

    /**
     * Retrieves the environment variables. This method can be overridden in tests to provide
     * a custom map of environment variables without modifying the actual system environment.
     *
     * @return A map of environment variable names to their values.
     */
    protected Map<String, String> getEnvironmentVariables() {
        return System.getenv();
    }

    /**
     * Executes the report, writing environment variables to the configured logger.
     * The output is formatted as human-readable INFO messages.
     */
    @Override
    public void run() {
        @Cleanup
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        final SortedMap<String, String> sortedProperties;
        try {
            sortedProperties = new TreeMap<>(getEnvironmentVariables()); // Use the protected method
        } catch (final SecurityException ignored) {
            ps.println("System Environment: access denied");
            return;
        }
        ps.println("System Environment:");

        final Pattern forbiddenPattern = Pattern.compile(ReporterConfig.forbiddenPropertyNamesRegex);

        for (final Map.Entry<String, String> entry : sortedProperties.entrySet()) {
            final String key = entry.getKey();
            String value = entry.getValue();

            if (forbiddenPattern.matcher(key).matches()) {
                value = "********"; // Censor sensitive values
            }
            ps.printf(" - %s: %s%n", key, value);
        }
        ps.println(); // Ensure a newline at the end of the report
    }
}
