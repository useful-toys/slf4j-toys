/*
 * Copyright 2026 Daniel Felix Ferber
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Date;

import static org.mockito.Mockito.*;
import static org.usefultoys.slf4jtestmock.AssertLogger.assertHasEvent;
import static org.usefultoys.slf4jtestmock.AssertLogger.assertNoEvent;

/**
 * Unit tests for {@link ReportDefaultTrustKeyStore}.
 * <p>
 * Tests verify that ReportDefaultTrustKeyStore correctly reads and logs default trust keystore information,
 * including trust managers, certificates, and handles various error scenarios.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Certificate Information Logging:</b> Verifies logging of trust keystore details including trust manager types, certificate subjects, issuers, serial numbers, and validity periods</li>
 *   <li><b>Empty Trust Managers:</b> Tests behavior when no trust managers are available, ensuring proper header logging without manager details</li>
 *   <li><b>KeyStore Exception Handling:</b> Validates error reporting when KeyStore initialization fails with KeyStoreException</li>
 *   <li><b>Algorithm Exception Handling:</b> Ensures proper error logging when TrustManagerFactory algorithm is not available (NoSuchAlgorithmException)</li>
 * </ul>
 */
@DisplayName("ReportDefaultTrustKeyStore")
@ValidateCharset
@ResetReporterConfig
@WithLocale("en")
@WithMockLogger
class ReportDefaultTrustKeyStoreTest {

    @Slf4jMock
    private Logger logger;

    @Test
    @DisplayName("should report trust keystore with certificate information")
    void shouldReportTrustKeystoreWithCertificateInformation() throws Exception {
        // Given: TrustManagerFactory with mock trust manager and certificate
        try (MockedStatic<TrustManagerFactory> mockedStatic = mockStatic(TrustManagerFactory.class)) {
            final TrustManagerFactory mockTmf = mock(TrustManagerFactory.class);
            mockedStatic.when(() -> TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())).thenReturn(mockTmf);

            final X509TrustManager mockTrustManager = mock(X509TrustManager.class);
            when(mockTmf.getTrustManagers()).thenReturn(new TrustManager[]{mockTrustManager});

            final X509Certificate mockCert = mock(X509Certificate.class);
            when(mockTrustManager.getAcceptedIssuers()).thenReturn(new X509Certificate[]{mockCert});

            when(mockCert.getSubjectX500Principal()).thenReturn(new X500Principal("CN=Test Subject"));
            when(mockCert.getIssuerX500Principal()).thenReturn(new X500Principal("CN=Test Issuer"));
            when(mockCert.getSerialNumber()).thenReturn(BigInteger.valueOf(12345));
            final Date notBefore = new Date(1000000000000L);
            final Date notAfter = new Date(2000000000000L);
            when(mockCert.getNotBefore()).thenReturn(notBefore);
            when(mockCert.getNotAfter()).thenReturn(notAfter);

            // When: report is executed
            final ReportDefaultTrustKeyStore report = new ReportDefaultTrustKeyStore(logger);
            report.run();

            // Then: should log trust keystore and certificate details
            assertHasEvent(logger, "Trust Keystore");
            assertHasEvent(logger, " - TrustManager: 0 (class");
            assertHasEvent(logger, "   - Certificate #0");
            assertHasEvent(logger, "       Subject: CN=Test Subject");
            assertHasEvent(logger, "       Issuer: CN=Test Issuer");
            assertHasEvent(logger, "       #: 12345 From: " + notBefore + " Until: " + notAfter);
        }
    }

    @Test
    @DisplayName("should report trust keystore when no trust managers")
    void shouldReportTrustKeystoreWhenNoTrustManagers() throws Exception {
        // Given: TrustManagerFactory with no trust managers
        try (MockedStatic<TrustManagerFactory> mockedStatic = mockStatic(TrustManagerFactory.class)) {
            final TrustManagerFactory mockTmf = mock(TrustManagerFactory.class);
            mockedStatic.when(() -> TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())).thenReturn(mockTmf);
            when(mockTmf.getTrustManagers()).thenReturn(new TrustManager[0]);

            // When: report is executed
            final ReportDefaultTrustKeyStore report = new ReportDefaultTrustKeyStore(logger);
            report.run();

            // Then: should log trust keystore header but no trust managers
            assertHasEvent(logger, "Trust Keystore");
            assertNoEvent(logger, " - TrustManager:");
        }
    }

    @Test
    @DisplayName("should report error when KeyStore exception occurs")
    void shouldReportErrorWhenKeyStoreException() throws Exception {
        // Given: TrustManagerFactory that throws KeyStoreException on init
        try (MockedStatic<TrustManagerFactory> mockedStatic = mockStatic(TrustManagerFactory.class)) {
            final TrustManagerFactory mockTmf = mock(TrustManagerFactory.class);
            mockedStatic.when(() -> TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())).thenReturn(mockTmf);
            doThrow(new KeyStoreException("Test KeyStore Exception")).when(mockTmf).init((KeyStore) null);

            // When: report is executed
            final ReportDefaultTrustKeyStore report = new ReportDefaultTrustKeyStore(logger);
            report.run();

            // Then: should log error message
            assertHasEvent(logger, "Cannot read TrustManager: Test KeyStore Exception");
        }
    }

    @Test
    @DisplayName("should report error when NoSuchAlgorithm exception occurs")
    void shouldReportErrorWhenNoSuchAlgorithmException() throws Exception {
        // Given: TrustManagerFactory that throws NoSuchAlgorithmException
        try (MockedStatic<TrustManagerFactory> mockedStatic = mockStatic(TrustManagerFactory.class)) {
            mockedStatic.when(() -> TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())).thenThrow(new NoSuchAlgorithmException("Test Algorithm Exception"));

            // When: report is executed
            final ReportDefaultTrustKeyStore report = new ReportDefaultTrustKeyStore(logger);
            report.run();

            // Then: should log error message
            assertHasEvent(logger, "Cannot read TrustManager: Test Algorithm Exception");
        }
    }
}
