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
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import java.util.Locale;

import static org.usefultoys.slf4jtestmock.AssertLogger.assertEvent;

/**
 * Unit tests for {@link ReportLocale}.
 * <p>
 * Tests verify that ReportLocale correctly reports locale information
 * including default locale, language, country, variant, and available locales.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Locale Information Reporting:</b> Verifies logging of default locale, language, country, variant, and available locales</li>
 * </ul>
 */
@SuppressWarnings("NonConstantLogger")
@DisplayName("ReportLocale")
@ValidateCharset
@ResetReporterConfig
@WithLocale("en")
@WithMockLogger
class ReportLocaleTest {

    @Slf4jMock
    private Logger logger;

    @Test
    @DisplayName("should log locale information")
    void shouldLogLocaleInformation() {
        // Given: ReportLocale instance with default locale
        final ReportLocale report = new ReportLocale(logger);
        final Locale defaultLocale = Locale.getDefault();

        // When: report is executed
        report.run();

        // Then: should log locale information including default locale and available locales
        assertEvent(logger, 0,
                "Locale",
                "default locale: " + defaultLocale.getDisplayName(),
                "language=" + defaultLocale.getDisplayLanguage(),
                "country=" + defaultLocale.getDisplayCountry(),
                "variant=" + defaultLocale.getDisplayVariant(),
                "available locales:");
    }
}
