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
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.MockLoggerExtension;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.test.CharsetConsistency;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.WithLocale;

import java.nio.charset.Charset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith({CharsetConsistency.class, ResetReporterConfig.class, MockLoggerExtension.class})
@WithLocale("en")
class ReportCharsetTest {

    @Slf4jMock("test.report.charset")
    private Logger logger;
    private static final Random random = new Random();

    private org.slf4j.impl.MockLogger getMockLogger() {
        return (org.slf4j.impl.MockLogger) logger;
    }

    @Test
    void shouldLogDefaultCharsetInformation() {
        // Arrange
        final ReportCharset report = new ReportCharset(logger);
        final Charset defaultCharset = Charset.defaultCharset();

        // Act
        report.run();

        // Assert
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
    void shouldLogCustomCharsetInformation() {
        // Arrange: create a CharsetInfoProvider with controlled values
        final Charset customDefaultCharset = Charset.forName("UTF-16");
        final Map<String, Charset> customAvailableCharsets = new LinkedHashMap<>();
        customAvailableCharsets.put("UTF-16", Charset.forName("UTF-16"));
        customAvailableCharsets.put("US-ASCII", Charset.forName("US-ASCII"));

        ReportCharset.CharsetInfoProvider provider = new ReportCharset.CharsetInfoProvider() {
            @Override
            public Charset defaultCharset() {
                return customDefaultCharset;
            }

            @Override
            public Map<String, Charset> availableCharsets() {
                return customAvailableCharsets;
            }
        };

        ReportCharset report = new ReportCharset(logger) {
            @Override
            protected ReportCharset.CharsetInfoProvider getCharsetInfoProvider() {
                return provider;
            }
        };

        // Act
        report.run();

        // Assert
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Charset",
            " - default charset: " + customDefaultCharset.displayName(),
            "; name=" + customDefaultCharset.name(),
            "; canEncode=" + customDefaultCharset.canEncode(),
            " - available charsets: ",
            Charset.forName("UTF-16").displayName() + "; ",
            Charset.forName("US-ASCII").displayName() + "; ");
        assertFalse(getMockLogger().toText().contains(Charset.forName("ISO-8859-1").displayName()), "Should not contain ISO-8859-1 display name");
    }

    @Test
    void shouldLogEmptyAvailableCharsets() {
        // Arrange: create a CharsetInfoProvider with no available charsets
        final Charset customDefaultCharset = Charset.forName("UTF-8");
        final Map<String, Charset> customAvailableCharsets = Collections.emptyMap();

        ReportCharset.CharsetInfoProvider provider = new ReportCharset.CharsetInfoProvider() {
            @Override
            public Charset defaultCharset() {
                return customDefaultCharset;
            }

            @Override
            public Map<String, Charset> availableCharsets() {
                return customAvailableCharsets;
            }
        };

        ReportCharset report = new ReportCharset(logger) {
            @Override
            protected ReportCharset.CharsetInfoProvider getCharsetInfoProvider() {
                return provider;
            }
        };

        // Act
        report.run();

        // Assert
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Charset",
            " - default charset: " + customDefaultCharset.displayName(),
            "; name=" + customDefaultCharset.name(),
            "; canEncode=" + customDefaultCharset.canEncode(),
            " - available charsets: ");
        assertFalse(getMockLogger().toText().contains(Charset.forName("UTF-16").displayName()), "Should not contain UTF-16 display name");
    }

    @Test
    void shouldLogTenRandomAvailableCharsets() {
        testWithRandomCharsets(10);
    }

    @Test
    void shouldLogTwentyRandomAvailableCharsets() {
        testWithRandomCharsets(20);
    }

    private void testWithRandomCharsets(int count) {
        // Arrange
        final Charset customDefaultCharset = Charset.forName("UTF-8"); // Keep default for simplicity
        final Map<String, Charset> randomCharsets = generateRandomCharsets(count);

        ReportCharset.CharsetInfoProvider provider = new ReportCharset.CharsetInfoProvider() {
            @Override
            public Charset defaultCharset() {
                return customDefaultCharset;
            }

            @Override
            public Map<String, Charset> availableCharsets() {
                return randomCharsets;
            }
        };

        ReportCharset report = new ReportCharset(logger) {
            @Override
            protected ReportCharset.CharsetInfoProvider getCharsetInfoProvider() {
                return provider;
            }
        };

        // Act
        report.run();

        // Assert
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
     * If 'count' is greater than the total number of available charsets, all available charsets are returned.
     */
    private Map<String, Charset> generateRandomCharsets(int count) {
        List<Charset> allAvailable = new ArrayList<>(Charset.availableCharsets().values());
        Collections.shuffle(allAvailable, random); // Shuffle to get random order

        Map<String, Charset> selectedCharsets = new LinkedHashMap<>();
        int limit = Math.min(count, allAvailable.size());
        for (int i = 0; i < limit; i++) {
            Charset charset = allAvailable.get(i);
            selectedCharsets.put(charset.name(), charset);
        }
        return selectedCharsets;
    }
}
