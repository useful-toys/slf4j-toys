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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.utils.ConfigParser;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import java.security.Provider;
import java.security.Security;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ReportSecurityProviders}.
 * <p>
 * Tests verify that ReportSecurityProviders correctly reports security provider information
 * including provider names, versions, info descriptions, and available services.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Security Providers Reporting:</b> Verifies logging of security providers with names, versions, info, and services</li>
 *   <li><b>No Providers Handling:</b> Tests proper handling when no security providers are found</li>
 * </ul>
 */
@DisplayName("ReportSecurityProviders")
@ValidateCharset
@ResetReporterConfig
@WithLocale("en")
@WithMockLogger
class ReportSecurityProvidersTest {

    @Slf4jMock
    private Logger logger;
    private MockedStatic<Security> mockedSecurity;

    @BeforeEach
    void setUp() {
        // Mock Security class
        mockedSecurity = Mockito.mockStatic(Security.class);
    }

    @AfterEach
    void tearDown() {
        mockedSecurity.close(); // Close the mock static
    }

    @Test
    @DisplayName("should report security providers with services")
    void shouldReportSecurityProvidersWithServices() {
        // Given: two security providers with services configured
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

        // When: report is executed
        new ReportSecurityProviders(logger).run();

        // Then: should log all providers with their services
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "Security Providers:",
                " - Provider 1: SUN (Version: 1.800000)",
                "   Info: SUN security provider",
                "   Services:",
                "    - Signature.SHA1withDSA: SUN provider",
                " - Provider 2: BC (Version: 1.680000)",
                "   Info: Bouncy Castle security provider",
                "   Services:",
                "    - Cipher.AES: BC provider"
        );
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    @DisplayName("should handle no security providers found")
    void shouldHandleNoSecurityProvidersFound() {
        // Given: no security providers available
        when(Security.getProviders()).thenReturn(new Provider[]{});

        // When: report is executed
        new ReportSecurityProviders(logger).run();

        // Then: should log that no providers were found
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO, "Security Providers:", " - No security providers found.");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    @DisplayName("should handle security provider with no services")
    void shouldHandleSecurityProviderWithNoServices() {
        // Given: security provider with no services (empty services map)
        Provider provider = mock(Provider.class);
        when(provider.getName()).thenReturn("EMPTY");
        when(provider.getVersion()).thenReturn(1.0);
        when(provider.getInfo()).thenReturn("Provider with no services");
        when(provider.entrySet()).thenReturn(Collections.emptySet());

        when(Security.getProviders()).thenReturn(new Provider[]{provider});

        // When: report is executed
        new ReportSecurityProviders(logger).run();

        // Then: should log provider info without services section (sortedServices.isEmpty() is true)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "Security Providers:",
                " - Provider 1: EMPTY (Version: 1.000000)",
                "   Info: Provider with no services"
        );
        assertTrue(ConfigParser.isInitializationOK());
    }
}
