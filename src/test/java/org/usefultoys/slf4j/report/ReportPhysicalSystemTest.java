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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4jtestmock.MockLoggerExtension;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.test.CharsetConsistencyExtension;
import org.usefultoys.test.ResetReporterConfigExtension;
import org.usefultoys.test.WithLocale;

import static org.usefultoys.slf4jtestmock.AssertLogger.assertEvent;

@ExtendWith({CharsetConsistencyExtension.class, ResetReporterConfigExtension.class, MockLoggerExtension.class})
@WithLocale("en")
class ReportPhysicalSystemTest {

    @Slf4jMock("test.report.physical")
    private Logger logger;

    @Test
    void shouldLogPhysicalSystemInformation() {
        // Arrange
        final ReportPhysicalSystem report = new ReportPhysicalSystem(logger);
        final int expectedProcessors = Runtime.getRuntime().availableProcessors();

        // Act
        report.run();

        // Assert
        assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "Physical system",
                "processors: " + expectedProcessors);
    }
}
