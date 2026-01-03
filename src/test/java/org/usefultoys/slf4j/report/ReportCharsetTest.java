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
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import java.nio.charset.Charset;
import java.util.*;

/**
 * Unit tests for {@link ReportCharset}.
 * <p>
 * Tests verify that ReportCharset correctly formats and logs charset information
 * including default charset details, available charsets, and edge cases like empty charsets.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Default Charset Information:</b> Verifies logging of default charset details and available charsets</li>
 *   <li><b>Custom Charset Information:</b> Tests reporting with custom charset provider</li>
 *   <li><b>Empty Available Charsets:</b> Verifies handling when no charsets are available</li>
 *   <li><b>Random Charset Sampling:</b> Tests reporting of random subsets of available charsets</li>
 * </ul>
 */
@DisplayName("ReportCharset")
@ValidateCharset
@ResetReporterConfig
@WithLocale("en")
@WithMockLogger
class ReportCharsetTest {

    @Slf4jMock
    private Logger logger;
    private static final Random random = new Random();

    @Test
    @DisplayName("should log default charset information")
    void shouldLogDefaultCharsetInformation() {
        // Given: a report with default charset
        final ReportCharset report = new ReportCharset(logger);
        final Charset defaultCharset = Charset.defaultCharset();

        // When: report is executed
        report.run();

        // Then: should log charset information with default charset details
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Charset",
            " - default charset: " + defaultCharset.displayName(),
            "; name=" + defaultCharset.name(),
            "; canEncode=" + defaultCharset.canEncode(),
            " - available charsets: ",
            Charset.forName("UTF-8").displayName() + "; ",
            Charset.forName("ISO-8859-1").displayName() + "; ");
    }

    @Test
    @DisplayName("should log custom charset information")
    void shouldLogCustomCharsetInformation() {
        // Given: a charset info provider with custom default and available charsets
        final Charset customDefaultCharset = Charset.forName("UTF-16");
        final Map<String, Charset> customAvailableCharsets = new LinkedHashMap<>();
        customAvailableCharsets.put("UTF-16", Charset.forName("UTF-16"));
        customAvailableCharsets.put("US-ASCII", Charset.forName("US-ASCII"));

        final ReportCharset.CharsetInfoProvider provider = new ReportCharset.CharsetInfoProvider() {
            @Override
            public Charset defaultCharset() {
                return customDefaultCharset;
            }

            @Override
            public Map<String, Charset> availableCharsets() {
                return customAvailableCharsets;
            }
        };

        final ReportCharset report = new ReportCharset(logger) {
            @Override
            protected ReportCharset.CharsetInfoProvider getCharsetInfoProvider() {
                return provider;
            }
        };

        // When: report is executed
        report.run();

        // Then: should log custom charset information and verify absence of non-included charsets
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Charset",
            " - default charset: " + customDefaultCharset.displayName(),
            "; name=" + customDefaultCharset.name(),
            "; canEncode=" + customDefaultCharset.canEncode(),
            " - available charsets: ",
            Charset.forName("UTF-16").displayName() + "; ",
            Charset.forName("US-ASCII").displayName() + "; ");
        AssertLogger.assertEventNot(logger, 0, MockLoggerEvent.Level.INFO, Charset.forName("ISO-8859-1").displayName());
    }

    @Test
    @DisplayName("should log empty available charsets")
    void shouldLogEmptyAvailableCharsets() {
        // Given: a charset info provider with empty available charsets map
        final Charset customDefaultCharset = Charset.forName("UTF-8");
        final Map<String, Charset> customAvailableCharsets = Collections.emptyMap();

        final ReportCharset.CharsetInfoProvider provider = new ReportCharset.CharsetInfoProvider() {
            @Override
            public Charset defaultCharset() {
                return customDefaultCharset;
            }

            @Override
            public Map<String, Charset> availableCharsets() {
                return customAvailableCharsets;
            }
        };

        final ReportCharset report = new ReportCharset(logger) {
            @Override
            protected ReportCharset.CharsetInfoProvider getCharsetInfoProvider() {
                return provider;
            }
        };

        // When: report is executed
        report.run();

        // Then: should log default charset info and empty available charsets section
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Charset",
            " - default charset: " + customDefaultCharset.displayName(),
            "; name=" + customDefaultCharset.name(),
            "; canEncode=" + customDefaultCharset.canEncode(),
            " - available charsets: ");
        AssertLogger.assertEventNot(logger, 0, MockLoggerEvent.Level.INFO, Charset.forName("UTF-16").displayName());
    }

    @Test
    @DisplayName("should log ten random available charsets")
    void shouldLogTenRandomAvailableCharsets() {
        // Given: a charset info provider with 10 random charsets
        testWithRandomCharsets(10);
    }

    @Test
    @DisplayName("should log twenty random available charsets")
    void shouldLogTwentyRandomAvailableCharsets() {
        // Given: a charset info provider with 20 random charsets
        testWithRandomCharsets(20);
    }

    /**
     * Helper method to test report with a variable number of random charsets.
     * <p>
     * Generates random charset selection, executes the report, and verifies that
     * all selected charsets are included in the logged output.
     *
     * @param count the number of random charsets to generate and test
     */
    private void testWithRandomCharsets(int count) {
        // Given: a charset info provider with 'count' random charsets
        final Charset customDefaultCharset = Charset.forName("UTF-8");
        final Map<String, Charset> randomCharsets = generateRandomCharsets(count);

        final ReportCharset.CharsetInfoProvider provider = new ReportCharset.CharsetInfoProvider() {
            @Override
            public Charset defaultCharset() {
                return customDefaultCharset;
            }

            @Override
            public Map<String, Charset> availableCharsets() {
                return randomCharsets;
            }
        };

        final ReportCharset report = new ReportCharset(logger) {
            @Override
            protected ReportCharset.CharsetInfoProvider getCharsetInfoProvider() {
                return provider;
            }
        };

        // When: report is executed
        report.run();

        // Then: should log default charset and all random charsets
        final List<String> expectedFragments = new ArrayList<>(Arrays.asList(
            "Charset",
            " - default charset: " + customDefaultCharset.displayName(),
            "; name=" + customDefaultCharset.name(),
            "; canEncode=" + customDefaultCharset.canEncode(),
            " - available charsets: "));
        randomCharsets.values().forEach(charset -> expectedFragments.add(charset.displayName() + "; "));
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO, expectedFragments.toArray(new String[0]));
    }

    /**
     * Generates a map of 'count' unique charsets randomly selected from all available charsets.
     * <p>
     * If 'count' is greater than the total number of available charsets, all available charsets are returned.
     *
     * @param count the number of random charsets to select
     * @return a LinkedHashMap with randomly selected charsets
     */
    private Map<String, Charset> generateRandomCharsets(int count) {
        final List<Charset> allAvailable = new ArrayList<>(Charset.availableCharsets().values());
        Collections.shuffle(allAvailable, random);

        final Map<String, Charset> selectedCharsets = new LinkedHashMap<>();
        final int limit = Math.min(count, allAvailable.size());
        for (int i = 0; i < limit; i++) {
            final Charset charset = allAvailable.get(i);
            selectedCharsets.put(charset.name(), charset);
        }
        return selectedCharsets;
    }
}




