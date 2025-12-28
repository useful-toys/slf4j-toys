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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.utils.ConfigParser;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.MockLoggerExtension;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.test.CharsetConsistencyExtension;
import org.usefultoys.test.ResetReporterConfigExtension;
import org.usefultoys.test.WithLocale;

import java.security.Provider;
import java.security.Security;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({CharsetConsistencyExtension.class, ResetReporterConfigExtension.class, MockLoggerExtension.class})
@WithLocale("en")
class ReportSecurityProvidersTest {

    @Slf4jMock("test.logger")
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

        new ReportSecurityProviders(logger).run();

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
    void testNoSecurityProvidersFound() {
        when(Security.getProviders()).thenReturn(new Provider[]{});

        new ReportSecurityProviders(logger).run();

        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO, "Security Providers:", " - No security providers found.");
        assertTrue(ConfigParser.isInitializationOK());
    }
}
