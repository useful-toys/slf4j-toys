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
import org.usefultoys.slf4jtestmock.MockLoggerExtension;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.test.CharsetConsistency;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.WithLocale;

import javax.servlet.ServletContextEvent;

import static org.mockito.Mockito.mock;
import static org.usefultoys.slf4jtestmock.AssertLogger.assertHasEvent;
import static org.usefultoys.slf4jtestmock.AssertLogger.assertNoEvent;

@ExtendWith({CharsetConsistency.class, ResetReporterConfig.class, MockLoggerExtension.class})
@WithLocale("en")
class ReportContextListenerTest {
    @Slf4jMock("test.report.contextlistener")
    private Logger logger;
    private ReportContextListener listener;

    @BeforeEach
    void setUpListener() {
        listener = new ReportContextListener();
    }

    @Test
    void shouldLogReportsOnContextInitialization() {
        // Configure reporter to use our test logger
        System.setProperty(ReporterConfig.PROP_NAME, "test.report.contextlistener");

        // Enable only one report to simplify the test
        System.setProperty(ReporterConfig.PROP_VM, "true");
        System.setProperty(ReporterConfig.PROP_MEMORY, "false");
        System.setProperty(ReporterConfig.PROP_USER, "false");
        System.setProperty(ReporterConfig.PROP_OPERATING_SYSTEM, "false");
        System.setProperty(ReporterConfig.PROP_PHYSICAL_SYSTEM, "false");
        System.setProperty(ReporterConfig.PROP_ENVIRONMENT, "false");
        System.setProperty(ReporterConfig.PROP_PROPERTIES, "false");
        System.setProperty(ReporterConfig.PROP_FILE_SYSTEM, "false");
        System.setProperty(ReporterConfig.PROP_CALENDAR, "false");
        System.setProperty(ReporterConfig.PROP_LOCALE, "false");
        System.setProperty(ReporterConfig.PROP_CHARSET, "false");
        System.setProperty(ReporterConfig.PROP_NETWORK_INTERFACE, "false");
        System.setProperty(ReporterConfig.PROP_SSL_CONTEXT, "false");
        System.setProperty(ReporterConfig.PROP_DEFAULT_TRUST_KEYSTORE, "false");
        ReporterConfig.init();

        final ServletContextEvent event = mock(ServletContextEvent.class);

        // Act
        listener.contextInitialized(event);

        // Assert
        assertHasEvent(logger, "Java Virtual Machine");
    }

    @Test
    void shouldDoNothingOnContextDestroyed() {
        final ServletContextEvent event = mock(ServletContextEvent.class);

        // Act
        listener.contextDestroyed(event);

        // Assert
        // No side effect expected — especially no logging - verify no events were logged at all
        assertNoEvent(logger, "Java Virtual Machine");
        assertNoEvent(logger, "Physical system");
    }
}