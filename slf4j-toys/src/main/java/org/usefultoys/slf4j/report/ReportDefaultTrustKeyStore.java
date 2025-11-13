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

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.PrintStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * A report module that lists the trusted certificate authorities (CAs) from the default JVM trust store.
 * This report is useful for diagnosing SSL/TLS connection issues and verifying the trust chain.
 *
 * @author Daniel Felix Ferber
 * @see Reporter
 * @see ReporterConfig#reportDefaultTrustKeyStore
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportDefaultTrustKeyStore implements Runnable {

    private final @NonNull Logger logger;

    /**
     * Executes the report, writing trusted keystore information to the configured logger.
     * The output is formatted as human-readable INFO messages.
     */
    @Override
    public void run() {
        @Cleanup final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        ps.println("Trust Keystore");

        try {
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);
            final TrustManager[] trustManagers = tmf.getTrustManagers();

            for (int i = 0; i < trustManagers.length; ++i) {
                final TrustManager tm = trustManagers[i];
                ps.printf(" - TrustManager: %d (%s)%n", i, tm.getClass());
                if (tm instanceof X509TrustManager) {
                    final X509TrustManager x509tm = (X509TrustManager) tm;
                    final X509Certificate[] certificates = x509tm.getAcceptedIssuers();
                    for (int j = 0; j < certificates.length; j++) {
                        final X509Certificate cert = certificates[j];
                        ps.printf("   - Certificate #%d%n", j);
                        ps.printf("       Subject: %s%n", cert.getSubjectX500Principal());
                        ps.printf("       Issuer: %s%n", cert.getIssuerX500Principal());
                        ps.printf("       #: %s From: %s Until: %s%n", cert.getSerialNumber(), cert.getNotBefore(), cert.getNotAfter());
                    }
                }
            }
        } catch (final KeyStoreException | NoSuchAlgorithmException e) {
            ps.printf("Cannot read TrustManager: %s%n", e.getMessage());
        }
        ps.println(); // Ensure a newline at the end of the report
    }
}
