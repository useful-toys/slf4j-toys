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
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import static org.usefultoys.slf4jtestmock.AssertLogger.assertEvent;

/**
 * Unit tests for {@link ReportVM}.
 * <p>
 * Tests verify that ReportVM correctly reports Java Virtual Machine information
 * including vendor, version, and installation directory.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>JVM Information Reporting:</b> Verifies logging of JVM vendor, version, and installation directory</li>
 * </ul>
 */
@SuppressWarnings("NonConstantLogger")
@DisplayName("ReportVM")
@ValidateCharset
@ResetReporterConfig
@WithLocale("en")
@WithMockLogger
class ReportVMTest {

    @Slf4jMock
    private Logger logger;

    @Test
    @DisplayName("should log VM information")
    void shouldLogVMInformation() {
        // Given: ReportVM instance
        final ReportVM report = new ReportVM(logger);

        // When: report is executed
        report.run();

        // Then: should log JVM details
        assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "Java Virtual Machine",
                "vendor: " + System.getProperty("java.vendor"),
                "version: " + System.getProperty("java.version"),
                "installation directory: " + System.getProperty("java.home"));
    }
}
