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

import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;
import org.usefultoys.slf4j.utils.ConfigParser;

import java.nio.charset.Charset;
import java.security.Provider;
import java.security.Security;
import java.util.Collections;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportSecurityProvidersTest {

    @BeforeAll
    static void validateConsistentCharset() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeAll
    public static void setUpLocale() {
        Locale.setDefault(Locale.ENGLISH);
    }

    private static final String TEST_LOGGER_NAME = "test.logger";
    private MockLogger mockLogger;
    private MockedStatic<Security> mockedSecurity;

    @BeforeEach
    void setUp() {
        ConfigParser.clearInitializationErrors();
        ReporterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();

        Logger testLogger = LoggerFactory.getLogger(TEST_LOGGER_NAME);
        mockLogger = (MockLogger) testLogger;
        mockLogger.clearEvents();
        mockLogger.setInfoEnabled(true); // Ensure INFO level is enabled

        // Mock Security class
        mockedSecurity = Mockito.mockStatic(Security.class);
    }

    @AfterEach
    void tearDown() {
        mockedSecurity.close(); // Close the mock static
        ReporterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
        ConfigParser.clearInitializationErrors();
    }

    private String getLogOutput() {
        return mockLogger.getLoggerEvents().stream()
                .map(MockLoggerEvent::getFormattedMessage)
                .collect(Collectors.joining("\n"));
    }

    @Test
    void testSecurityProvidersAreReported() {
        Provider provider1 = mock(Provider.class);
        when(provider1.getName()).thenReturn("SUN");
        when(provider1.getVersion()).thenReturn(1.8);
        when(provider1.getInfo()).thenReturn("SUN security provider");
        when(provider1.entrySet()).thenReturn(Collections.singletonMap((Object)"Signature.SHA1withDSA", (Object)"SUN provider").entrySet());

        Provider provider2 = mock(Provider.class);
        when(provider2.getName()).thenReturn("BC");
        when(provider2.getVersion()).thenReturn(1.68);
        when(provider2.getInfo()).thenReturn("Bouncy Castle security provider");
        when(provider2.entrySet()).thenReturn(Collections.singletonMap((Object)"Cipher.AES", (Object)"BC provider").entrySet());

        when(Security.getProviders()).thenReturn(new Provider[]{provider1, provider2});

        new ReportSecurityProviders(mockLogger).run();

        String logOutput = getLogOutput();
        assertTrue(logOutput.contains("Security Providers:"));
        assertTrue(logOutput.contains(" - Provider 1: SUN (Version: 1.800000)"));
        assertTrue(logOutput.contains("   Info: SUN security provider"));
        assertTrue(logOutput.contains("   Services:"));
        assertTrue(logOutput.contains("    - Signature.SHA1withDSA: SUN provider"));
        assertTrue(logOutput.contains(" - Provider 2: BC (Version: 1.680000)"));
        assertTrue(logOutput.contains("   Info: Bouncy Castle security provider"));
        assertTrue(logOutput.contains("    - Cipher.AES: BC provider"));
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testNoSecurityProvidersFound() {
        when(Security.getProviders()).thenReturn(new Provider[]{});

        new ReportSecurityProviders(mockLogger).run();

        String logOutput = getLogOutput();
        assertTrue(logOutput.contains(" - No security providers found."));
        assertTrue(ConfigParser.isInitializationOK());
    }
}
