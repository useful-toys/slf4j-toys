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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;

import java.nio.charset.Charset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportCharsetTest {

    private MockLogger mockLogger;
    private static final Random random = new Random();

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    void resetWatcherConfigBeforeEach() {
        // Reinitialize each configuration to ensure a clean configuration before each test
        ReporterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }

    @AfterAll
    static void resetWatcherConfigAfterAll() {
        // Reinitialize each configuration to ensure a clean configuration before each test
        ReporterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }

    @BeforeEach
    void setUp() {
        final Logger logger = LoggerFactory.getLogger("test.report.charset");
        mockLogger = (MockLogger) logger;
        mockLogger.clearEvents();
    }

    @Test
    void shouldLogDefaultCharsetInformation() {
        // Arrange
        final ReportCharset report = new ReportCharset(mockLogger);
        final Charset defaultCharset = Charset.defaultCharset();

        // Act
        report.run();

        // Assert
        String logs = mockLogger.toText();
        assertTrue(logs.contains("Charset"), "Should contain 'Charset'");
        assertTrue(logs.contains("default charset: " + defaultCharset.displayName()), "Should contain default charset display name");
        assertTrue(logs.contains("name=" + defaultCharset.name()), "Should contain default charset name");
        assertTrue(logs.contains("canEncode=" + defaultCharset.canEncode()), "Should contain default charset canEncode status");
        assertTrue(logs.contains("available charsets:"), "Should contain 'available charsets:'");
        // We can't assert all available charsets as they vary by JVM, but we can check for a few common ones
        assertTrue(logs.contains(Charset.forName("UTF-8").displayName()), "Should contain UTF-8 display name");
        assertTrue(logs.contains(Charset.forName("ISO-8859-1").displayName()), "Should contain ISO-8859-1 display name");
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

        ReportCharset report = new ReportCharset(mockLogger) {
            @Override
            protected ReportCharset.CharsetInfoProvider getCharsetInfoProvider() {
                return provider;
            }
        };

        // Act
        report.run();

        // Assert
        String logs = mockLogger.toText();
        assertTrue(logs.contains("Charset"), "Should contain 'Charset'");
        assertTrue(logs.contains("default charset: " + customDefaultCharset.displayName()), "Should contain custom default charset display name");
        assertTrue(logs.contains("name=" + customDefaultCharset.name()), "Should contain custom default charset name");
        assertTrue(logs.contains("canEncode=" + customDefaultCharset.canEncode()), "Should contain custom default charset canEncode status");
        assertTrue(logs.contains("available charsets:"), "Should contain 'available charsets:'");
        assertTrue(logs.contains(Charset.forName("UTF-16").displayName()), "Should contain custom available UTF-16 display name");
        assertTrue(logs.contains(Charset.forName("US-ASCII").displayName()), "Should contain custom available US-ASCII display name");
        // Ensure other charsets are not present if not in custom list
        assertTrue(!logs.contains(Charset.forName("ISO-8859-1").displayName()), "Should not contain ISO-8859-1 display name");
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

        ReportCharset report = new ReportCharset(mockLogger) {
            @Override
            protected ReportCharset.CharsetInfoProvider getCharsetInfoProvider() {
                return provider;
            }
        };

        // Act
        report.run();

        // Assert
        String logs = mockLogger.toText();
        assertTrue(logs.contains("Charset"), "Should contain 'Charset'");
        assertTrue(logs.contains("default charset: " + customDefaultCharset.displayName()), "Should contain custom default charset display name");
        assertTrue(logs.contains("name=" + customDefaultCharset.name()), "Should contain custom default charset name");
        assertTrue(logs.contains("canEncode=" + customDefaultCharset.canEncode()), "Should contain custom default charset canEncode status");
        assertTrue(logs.contains("available charsets: "), "Should contain 'available charsets:'");
        // Ensure no other charset names are present
        assertTrue(!logs.contains(Charset.forName("UTF-16").displayName()), "Should not contain UTF-16 display name");
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

        ReportCharset report = new ReportCharset(mockLogger) {
            @Override
            protected ReportCharset.CharsetInfoProvider getCharsetInfoProvider() {
                return provider;
            }
        };

        // Act
        report.run();

        // Assert
        String logs = mockLogger.toText();
        assertTrue(logs.contains("Charset"), "Should contain 'Charset'");
        assertTrue(logs.contains("default charset: " + customDefaultCharset.displayName()), "Should contain custom default charset display name");
        assertTrue(logs.contains("name=" + customDefaultCharset.name()), "Should contain custom default charset name");
        assertTrue(logs.contains("canEncode=" + customDefaultCharset.canEncode()), "Should contain custom default charset canEncode status");
        assertTrue(logs.contains("available charsets:"), "Should contain 'available charsets:'");

        for (Charset charset : randomCharsets.values()) {
            assertTrue(logs.contains(charset.displayName()), "Should contain random charset: " + charset.displayName());
        }
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
