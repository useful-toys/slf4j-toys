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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;

import javax.servlet.ServletContextEvent;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ReportContextListenerTest {
    private MockLogger mockLogger;
    private ReportContextListener listener;

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    void resetWatcherConfigBeforeEach() {
        // Reinitialize each configuration to ensure a clean configuration before each test
        ReporterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }

    @AfterAll
    static void resetWatcherConfigAfterAll() {
        // Reinitialize each configuration to ensure a clean configuration before each test
        ReporterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }

    @BeforeEach
    void setUp() {
        mockLogger = (MockLogger) LoggerFactory.getLogger(ReporterConfig.name);
        mockLogger.clearEvents();
        listener = new ReportContextListener();
    }

    @Test
    void shouldLogReportsOnContextInitialization() {
        // Enable only one report to simplify the test
        ReporterConfig.reportVM = true;
        ReporterConfig.reportMemory = false;
        ReporterConfig.reportUser = false;
        ReporterConfig.reportOperatingSystem = false;
        ReporterConfig.reportPhysicalSystem = false;
        ReporterConfig.reportEnvironment = false;
        ReporterConfig.reportProperties = false;
        ReporterConfig.reportFileSystem = false;
        ReporterConfig.reportCalendar = false;
        ReporterConfig.reportLocale = false;
        ReporterConfig.reportCharset = false;
        ReporterConfig.reportNetworkInterface = false;
        ReporterConfig.reportSSLContext = false;
        ReporterConfig.reportDefaultTrustKeyStore = false;
        final ServletContextEvent event = mock(ServletContextEvent.class);

        // Act
        listener.contextInitialized(event);

        // Assert
        assertTrue(mockLogger.getEventCount() > 0, "Expected at least one log event");

        final boolean vmReported = mockLogger.getEvent(0).getFormattedMessage().contains("Java Virtual Machine");
        assertTrue(vmReported, "Expected VM report to be logged");
    }

    @Test
    void shouldDoNothingOnContextDestroyed() {
        final ServletContextEvent event = mock(ServletContextEvent.class);

        // Act
        listener.contextDestroyed(event);

        // Assert
        // No side effect expected — especially no logging
        assertEquals(0, mockLogger.getEventCount(), "Expected no log output on contextDestroyed");
    }
}