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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import org.usefultoys.slf4jtestmock.MockLoggerExtension;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.test.CharsetConsistencyExtension;
import org.usefultoys.test.ResetReporterConfigExtension;
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

@ExtendWith({CharsetConsistencyExtension.class, ResetReporterConfigExtension.class, MockLoggerExtension.class})
@WithLocale("en")
class ReportDefaultTrustKeyStoreTest {

    @Slf4jMock("test.report.truststore")
    private Logger logger;

    @Test
    void testRun() throws Exception {
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
            Date notBefore = new Date(1000000000000L);
            Date notAfter = new Date(2000000000000L);
            when(mockCert.getNotBefore()).thenReturn(notBefore);
            when(mockCert.getNotAfter()).thenReturn(notAfter);

            final ReportDefaultTrustKeyStore report = new ReportDefaultTrustKeyStore(logger);
            report.run();

            assertHasEvent(logger, "Trust Keystore");
            assertHasEvent(logger, " - TrustManager: 0 (class org.mockito.codegen.X509TrustManager$MockitoMock$");
            assertHasEvent(logger, "   - Certificate #0");
            assertHasEvent(logger, "       Subject: CN=Test Subject");
            assertHasEvent(logger, "       Issuer: CN=Test Issuer");
            assertHasEvent(logger, "       #: 12345 From: " + notBefore + " Until: " + notAfter);
        }
    }

    @Test
    void testRunNoTrustManagers() throws Exception {
        try (MockedStatic<TrustManagerFactory> mockedStatic = mockStatic(TrustManagerFactory.class)) {
            final TrustManagerFactory mockTmf = mock(TrustManagerFactory.class);
            mockedStatic.when(() -> TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())).thenReturn(mockTmf);
            when(mockTmf.getTrustManagers()).thenReturn(new TrustManager[0]);

            final ReportDefaultTrustKeyStore report = new ReportDefaultTrustKeyStore(logger);
            report.run();

            assertHasEvent(logger, "Trust Keystore");
            assertNoEvent(logger, " - TrustManager:");
        }
    }

    @Test
    void testRunKeyStoreException() throws Exception {
        try (MockedStatic<TrustManagerFactory> mockedStatic = mockStatic(TrustManagerFactory.class)) {
            final TrustManagerFactory mockTmf = mock(TrustManagerFactory.class);
            mockedStatic.when(() -> TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())).thenReturn(mockTmf);
            doThrow(new KeyStoreException("Test KeyStore Exception")).when(mockTmf).init((KeyStore) null);

            final ReportDefaultTrustKeyStore report = new ReportDefaultTrustKeyStore(logger);
            report.run();

            assertHasEvent(logger, "Cannot read TrustManager: Test KeyStore Exception");
        }
    }

    @Test
    void testRunNoSuchAlgorithmException() throws Exception {
        try (MockedStatic<TrustManagerFactory> mockedStatic = mockStatic(TrustManagerFactory.class)) {
            mockedStatic.when(() -> TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())).thenThrow(new NoSuchAlgorithmException("Test Algorithm Exception"));

            final ReportDefaultTrustKeyStore report = new ReportDefaultTrustKeyStore(logger);
            report.run();

            assertHasEvent(logger, "Cannot read TrustManager: Test Algorithm Exception");
        }
    }
}
