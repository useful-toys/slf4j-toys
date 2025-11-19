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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.usefultoys.slf4j.SessionConfig;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.nio.charset.Charset;
import java.security.Provider;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ReportSSLContextTest {

    @BeforeAll
    static void validateConsistentCharset() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }
    
    private MockLogger mockLogger;

    @BeforeEach
    void setUp() {
        final Logger logger = LoggerFactory.getLogger("test.report.sslcontext");
        mockLogger = (MockLogger) logger;
        mockLogger.clearEvents();
    }

    @Test
    void testRun() throws Exception {
        try (MockedStatic<SSLContext> mockedStatic = mockStatic(SSLContext.class)) {
            final SSLContext mockSslContext = mock(SSLContext.class);
            final Provider mockProvider = mock(Provider.class);
            final SSLSocketFactory mockSocketFactory = mock(SSLSocketFactory.class);
            final SSLServerSocketFactory mockServerSocketFactory = mock(SSLServerSocketFactory.class);
            final SSLParameters mockDefaultParams = mock(SSLParameters.class);
            final SSLParameters mockSupportedParams = mock(SSLParameters.class);

            mockedStatic.when(() -> SSLContext.getInstance(anyString())).thenReturn(mockSslContext);

            when(mockSslContext.getProtocol()).thenReturn("TLSv1.2");
            when(mockSslContext.getProvider()).thenReturn(mockProvider);
            Properties props = new Properties();
            props.put("Provider.id name", "SunJSSE");
            when(mockProvider.entrySet()).thenReturn(props.entrySet());
            when(mockSslContext.getSocketFactory()).thenReturn(mockSocketFactory);
            when(mockSocketFactory.getDefaultCipherSuites()).thenReturn(new String[]{"TLS_DHE_DSS_WITH_AES_128_CBC_SHA"});
            when(mockSocketFactory.getSupportedCipherSuites()).thenReturn(new String[]{"TLS_DHE_DSS_WITH_AES_128_CBC_SHA", "TLS_DHE_DSS_WITH_AES_256_CBC_SHA"});
            when(mockSslContext.getServerSocketFactory()).thenReturn(mockServerSocketFactory);
            when(mockServerSocketFactory.getDefaultCipherSuites()).thenReturn(new String[]{"TLS_DHE_DSS_WITH_AES_128_CBC_SHA"});
            when(mockServerSocketFactory.getSupportedCipherSuites()).thenReturn(new String[]{"TLS_DHE_DSS_WITH_AES_128_CBC_SHA", "TLS_DHE_DSS_WITH_AES_256_CBC_SHA"});
            when(mockSslContext.getDefaultSSLParameters()).thenReturn(mockDefaultParams);
            when(mockDefaultParams.getEndpointIdentificationAlgorithm()).thenReturn("HTTPS");
            when(mockDefaultParams.getNeedClientAuth()).thenReturn(false);
            when(mockDefaultParams.getWantClientAuth()).thenReturn(true);
            when(mockDefaultParams.getProtocols()).thenReturn(new String[]{"TLSv1.2"});
            when(mockDefaultParams.getCipherSuites()).thenReturn(new String[]{"TLS_DHE_DSS_WITH_AES_128_CBC_SHA"});
            when(mockSslContext.getSupportedSSLParameters()).thenReturn(mockSupportedParams);
            when(mockSupportedParams.getEndpointIdentificationAlgorithm()).thenReturn("HTTPS");
            when(mockSupportedParams.getNeedClientAuth()).thenReturn(false);
            when(mockSupportedParams.getWantClientAuth()).thenReturn(true);
            when(mockSupportedParams.getProtocols()).thenReturn(new String[]{"TLSv1.2", "TLSv1.3", "Proto3", "Proto4", "Proto5", "Proto6", "Proto7", "Proto8", "Proto9", "Proto10", "Proto11", "Proto12"});
            when(mockSupportedParams.getCipherSuites()).thenReturn(new String[]{"TLS_DHE_DSS_WITH_AES_128_CBC_SHA", "TLS_AES_128_GCM_SHA256"});

            final ReportSSLContext report = new ReportSSLContext(mockLogger);
            report.run();

            assertEquals(report.contextNames.length, mockLogger.getEventCount());
            final String logs = mockLogger.getEvent(0).getFormattedMessage();

            assertTrue(logs.contains("SSL Context Default"));
            assertTrue(logs.contains("   Protocol: TLSv1.2"));
            assertTrue(logs.contains("   Provider:"));
            assertTrue(logs.contains("    - Provider.id name: SunJSSE"));
            assertTrue(logs.contains("   SocketFactory: "));
            assertTrue(logs.contains("      Default Cipher Suites:TLS_DHE_DSS_WITH_AES_128_CBC_SHA;"));
            assertTrue(logs.contains("      Supported Cipher Suites: TLS_DHE_DSS_WITH_AES_128_CBC_SHA; TLS_DHE_DSS_WITH_AES_256_CBC_SHA;"));
            assertTrue(logs.contains("   ServerSocketFactory: "));
            assertTrue(logs.contains("      Default Cipher Suites:TLS_DHE_DSS_WITH_AES_128_CBC_SHA;"));
            assertTrue(logs.contains("      Supported Cipher Suites:TLS_DHE_DSS_WITH_AES_128_CBC_SHA; TLS_DHE_DSS_WITH_AES_256_CBC_SHA;"));
            assertTrue(logs.contains("   Default SSL Parameters:"));
            assertTrue(logs.contains("      EndpointIdentificationAlgorithm: HTTPS"));
            assertTrue(logs.contains("      Need Client Auth: false"));
            assertTrue(logs.contains("      Want Client Auth: true"));
            assertTrue(logs.contains("      Protocols: TLSv1.2;"));
            assertTrue(logs.contains("      Cipher Suites: TLS_DHE_DSS_WITH_AES_128_CBC_SHA;"));
            assertTrue(logs.contains("   Supported SSL Parameters:"));
            assertTrue(logs.contains("      EndpointIdentificationAlgorithm: HTTPS"));
            assertTrue(logs.contains("      Need Client Auth: false"));
            assertTrue(logs.contains("      Want Client Auth: true"));
            assertTrue(logs.contains("      Protocols: TLSv1.2; TLSv1.3; Proto3; Proto4; Proto5; Proto6; Proto7; Proto8; Proto9;"));
            assertTrue(logs.contains("          Proto10; Proto11; Proto12;"));
            assertTrue(logs.contains("      Cipher Suites: TLS_DHE_DSS_WITH_AES_128_CBC_SHA; TLS_AES_128_GCM_SHA256;"));
        }
    }

    @Test
    void testRunGetInstanceThrowsException() throws Exception {
        try (MockedStatic<SSLContext> mockedStatic = mockStatic(SSLContext.class)) {
            mockedStatic.when(() -> SSLContext.getInstance("SSLv2")).thenThrow(new java.security.NoSuchAlgorithmException("SSLv2 not available"));

            final ReportSSLContext report = new ReportSSLContext(mockLogger);
            report.run();

            boolean found = false;
            for (int i = 0; i < mockLogger.getEventCount(); i++) {
                if (mockLogger.getEvent(i).getFormattedMessage().contains("SSL Context SSLv2") &&
                    mockLogger.getEvent(i).getFormattedMessage().contains("Failed to detail SSLContext: SSLv2 not available")) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Log message for SSLv2 failure not found.");
        }
    }
}
