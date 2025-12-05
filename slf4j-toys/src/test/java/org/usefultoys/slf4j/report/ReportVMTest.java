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
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.usefultoys.test.CharsetConsistency;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.WithLocale;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({ResetReporterConfig.class, CharsetConsistency.class})
@WithLocale("en")
class ReportVMTest {

    private MockLogger mockLogger;

    @BeforeEach
    void setUp() {
        mockLogger = (MockLogger) LoggerFactory.getLogger("test.report.vm");
        mockLogger.clearEvents();
    }

    @Test
    void shouldLogJvmInformation() {
        // Arrange
        final ReportVM report = new ReportVM(mockLogger);

        // Act
        report.run();

        // Assert
        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("Java Virtual Machine"));
        assertTrue(logs.contains("vendor: " + System.getProperty("java.vendor")));
        assertTrue(logs.contains("version: " + System.getProperty("java.version")));
        assertTrue(logs.contains("installation directory: " + System.getProperty("java.home")));
    }
}
