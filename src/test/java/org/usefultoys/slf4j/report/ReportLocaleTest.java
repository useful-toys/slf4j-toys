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
import org.usefultoys.slf4jtestmock.MockLoggerExtension;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.test.CharsetConsistencyExtension;
import org.usefultoys.test.ResetReporterConfigExtension;
import org.usefultoys.test.WithLocale;

import java.util.Locale;

import static org.usefultoys.slf4jtestmock.AssertLogger.assertEvent;

@ExtendWith({ResetReporterConfigExtension.class, CharsetConsistencyExtension.class, MockLoggerExtension.class})
@WithLocale("en")
class ReportLocaleTest {

    @Slf4jMock("test.report.locale")
    private Logger logger;

    @Test
    void shouldLogLocaleInformation() {
        // Arrange
        final ReportLocale report = new ReportLocale(logger);
        final Locale defaultLocale = Locale.getDefault();

        // Act
        report.run();

        // Assert
        assertEvent(logger, 0,
                "Locale",
                "default locale: " + defaultLocale.getDisplayName(),
                "language=" + defaultLocale.getDisplayLanguage(),
                "country=" + defaultLocale.getDisplayCountry(),
                "variant=" + defaultLocale.getDisplayVariant(),
                "available locales:");
    }
}
