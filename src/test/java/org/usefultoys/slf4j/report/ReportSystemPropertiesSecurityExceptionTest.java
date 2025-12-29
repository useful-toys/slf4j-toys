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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.usefultoys.slf4jtestmock.AssertLogger.assertHasEvent;

/**
 * Unit tests for {@link ReportSystemProperties}.
 * <p>
 * Tests verify that ReportSystemProperties correctly handles SecurityException
 * when system properties cannot be accessed due to security restrictions.
 * Uses Mockito spy to avoid interfering with System class loading.
 */
@DisplayName("ReportSystemPropertiesSecurityException")
@ValidateCharset
@ResetReporterConfig
@WithLocale("en")
@WithMockLogger
class ReportSystemPropertiesSecurityExceptionTest {

    @Slf4jMock
    private Logger logger;

    @Test
    @DisplayName("should handle security exception when accessing system properties")
    void shouldHandleSecurityExceptionWhenAccessingSystemProperties() {
        // Given: ReportSystemProperties that throws SecurityException on getSystemProperties()
        final ReportSystemProperties reporter = spy(new ReportSystemProperties(logger));
        doThrow(new SecurityException("Access denied"))
                .when(reporter).getSystemProperties();

        // When: report is executed
        reporter.run();

        // Then: should log error message
        assertHasEvent(logger, "System Properties: access denied");
    }
}