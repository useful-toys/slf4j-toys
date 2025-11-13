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

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.PrintStream;
import java.security.Provider;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A report module that provides detailed information about SSL contexts supported by the JVM.
 * It includes details on cipher suites, protocols, and supported SSL parameters for various contexts.
 * This report is crucial for diagnosing SSL/TLS configuration issues.
 *
 * @author Daniel Felix Ferber
 * @see Reporter
 * @see ReporterConfig#reportSSLContext
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportSSLContext implements Runnable {

    private final @NonNull Logger logger;

    /**
     * An array of common SSL context names to be reported.
     */
    final String[] contextNames = {
            "Default", "SSL", "SSLv2", "SSLv3", "TLS", "TLSv1", "TLSv1.1", "TLSv1.2"
    };

    /**
     * Helper method to print a list of strings to the PrintStream, formatting it with newlines and indentation.
     *
     * @param ps The PrintStream to write to.
     * @param list The array of strings to print.
     * @param newLineSpace The indentation string to use after each newline.
     */
    private static void printList(final PrintStream ps, final String[] list, final String newLineSpace) {
        int i = 1;
        for (final String s : list) {
            if (i++ % 10 == 0) {
                ps.println();
                ps.print(newLineSpace);
            }
            ps.print(s);
            ps.print("; ");
        }
        ps.println();
    }

    /**
     * Executes the report, writing SSL context information to the configured logger.
     * The output is formatted as human-readable INFO messages.
     */
    @Override
    public void run() {
        for (final String contextName : contextNames) {
            @Cleanup
            final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
            ps.printf("SSL Context %s%n", contextName);

            try {
                final SSLContext sslContext = SSLContext.getInstance(contextName);
                ps.printf("   Protocol: %s%n", sslContext.getProtocol());
                ps.printf("   Class: %s%n", sslContext.getClass());
                ps.println("   Provider:");
                final Provider provider = sslContext.getProvider();
                final SortedMap<Object, Object> sortedProperties = new TreeMap<>(provider);
                for (final Map.Entry<Object, Object> entry : sortedProperties.entrySet()) {
                    ps.printf("    - %s: %s%n", entry.getKey(), entry.getValue());
                }
                ps.println("   SocketFactory: ");
                ps.print("      Default Cipher Suites:");
                printList(ps, sslContext.getSocketFactory().getDefaultCipherSuites(), "          ");
                ps.print("      Supported Cipher Suites: ");
                printList(ps, sslContext.getSocketFactory().getSupportedCipherSuites(), "          ");
                ps.println("   ServerSocketFactory: ");
                ps.print("      Default Cipher Suites:");
                printList(ps, sslContext.getServerSocketFactory().getDefaultCipherSuites(), "          ");
                ps.print("      Supported Cipher Suites:");
                printList(ps, sslContext.getServerSocketFactory().getSupportedCipherSuites(), "          ");
                SSLParameters p = sslContext.getDefaultSSLParameters();
                ps.println("   Default SSL Parameters:");
                ps.printf("      EndpointIdentificationAlgorithm: %s%n", p.getEndpointIdentificationAlgorithm());
                ps.printf("      Need Client Auth: %s%n", p.getNeedClientAuth());
                ps.printf("      Want Client Auth: %s%n", p.getWantClientAuth());
                ps.print("      Protocols: ");
                printList(ps, p.getProtocols(), "          ");
                ps.print("      Cipher Suites: ");
                printList(ps, p.getCipherSuites(), "          ");
                p = sslContext.getSupportedSSLParameters();
                ps.println("   Supported SSL Parameters:");
                ps.printf("      EndpointIdentificationAlgorithm: %s%n", p.getEndpointIdentificationAlgorithm());
                ps.printf("      Need Client Auth: %s%n", p.getNeedClientAuth());
                ps.printf("      Want Client Auth: %s%n", p.getWantClientAuth());
                ps.print("      Protocols: ");
                printList(ps, p.getProtocols(), "          ");
                ps.print("      Cipher Suites: ");
                printList(ps, p.getCipherSuites(), "          ");
            } catch (final Exception e) {
                ps.printf("Failed to detail SSLContext: %s%n", e.getMessage()); // Changed error message to English
            }
            ps.println(); // Ensure a newline at the end of each context report
        }
    }
}
