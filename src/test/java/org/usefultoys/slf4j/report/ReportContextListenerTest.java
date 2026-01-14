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

import jakarta.servlet.ServletContextEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link ReportContextListener}.
 * <p>
 * Tests verify that ReportContextListener correctly initializes and executes reports
 * when the servlet context is initialized, and performs no operations when context is destroyed.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Context Initialization:</b> Verifies that reports are executed and logged when the servlet context is initialized, with proper configuration handling</li>
 *   <li><b>Context Destruction:</b> Ensures no operations or logging occur when the servlet context is destroyed</li>
 *   <li><b>Report Configuration:</b> Tests selective enabling of specific reports (VM report) while disabling others to validate configuration-driven behavior</li>
 * </ul>
 */
@SuppressWarnings("NonConstantLogger")
@DisplayName("ReportContextListener")
@ValidateCharset
@ResetReporterConfig
@WithLocale("en")
@WithMockLogger
class ReportContextListenerTest {
    @Slf4jMock
    private Logger logger;
    private ReportContextListener listener;

    @BeforeEach
    void setUpListener() {
        listener = new ReportContextListener();
    }

    @Test
    @DisplayName("should log reports on context initialization")
    void shouldLogReportsOnContextInitialization() {
        // Given: reporter configured to use test logger with only VM report enabled
        System.setProperty(ReporterConfig.PROP_NAME, getClass().getCanonicalName());

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

        // When: context initialization is triggered
        listener.contextInitialized(event);

        // Then: should log Java Virtual Machine report
        AssertLogger.assertHasEvent(logger, "Java Virtual Machine");
    }

    @Test
    @DisplayName("should do nothing on context destroyed")
    void shouldDoNothingOnContextDestroyed() {
        // Given: servlet context event
        final ServletContextEvent event = mock(ServletContextEvent.class);

        // When: context destroyed is triggered
        listener.contextDestroyed(event);

        // Then: no side effects expected — especially no logging
        AssertLogger.assertEventCount(logger, 0);
    }
}