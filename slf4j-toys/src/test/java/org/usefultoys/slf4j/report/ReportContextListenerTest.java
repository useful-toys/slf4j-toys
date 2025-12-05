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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.MockLoggerExtension;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.test.CharsetConsistency;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.WithLocale;

import javax.servlet.ServletContextEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith({CharsetConsistency.class, ResetReporterConfig.class, MockLoggerExtension.class})
@WithLocale("en")
class ReportContextListenerTest {
    @Slf4jMock("report")
    private Logger logger;
    private ReportContextListener listener;

    private MockLogger getMockLogger() {
        return (MockLogger) logger;
    }

    @BeforeEach
    void setUpListener() {
        getMockLogger().clearEvents();
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
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO, "Java Virtual Machine");
    }

    @Test
    void shouldDoNothingOnContextDestroyed() {
        final ServletContextEvent event = mock(ServletContextEvent.class);

        // Act
        listener.contextDestroyed(event);

        // Assert
        // No side effect expected — especially no logging
        assertEquals(0, getMockLogger().getEventCount(), "Expected no log output on contextDestroyed");
    }
}