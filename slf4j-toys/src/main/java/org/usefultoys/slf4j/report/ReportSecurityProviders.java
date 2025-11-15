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
import java.security.Provider;
import java.security.Security;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A report module that lists all Java Security Providers installed in the JVM.
 * For each provider, it details its name, version, and available services.
 * This report is useful for validating the security environment of the application,
 * especially when specific cryptographic algorithms or security features are required.
 *
 * @author Daniel Felix Ferber
 * @see Reporter
 * @see ReporterConfig#reportSecurityProviders
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportSecurityProviders implements Runnable {

    private final @NonNull Logger logger;

    /**
     * Executes the report, writing security providers information to the configured logger.
     * The output is formatted as human-readable INFO messages.
     */
    @Override
    public void run() {
        @Cleanup
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        ps.println("Security Providers:");

        Provider[] providers = Security.getProviders();

        if (providers.length == 0) {
            ps.println(" - No security providers found.");
        } else {
            for (int i = 0; i < providers.length; i++) {
                Provider provider = providers[i];
                ps.printf(" - Provider %d: %s (Version: %f)%n", i + 1, provider.getName(), provider.getVersion());
                ps.printf("   Info: %s%n", provider.getInfo());

                // List services offered by the provider
                SortedMap<Object, Object> sortedServices = new TreeMap<>(provider);
                if (!sortedServices.isEmpty()) {
                    ps.println("   Services:");
                    for (Map.Entry<Object, Object> entry : sortedServices.entrySet()) {
                        ps.printf("    - %s: %s%n", entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        ps.println();
    }
}
