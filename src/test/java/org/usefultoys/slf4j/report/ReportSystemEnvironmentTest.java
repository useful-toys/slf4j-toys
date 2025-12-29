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
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.utils.ConfigParser;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.usefultoys.slf4jtestmock.AssertLogger.assertEvent;

/**
 * Unit tests for {@link ReportSystemEnvironment}.
 * <p>
 * Tests verify that ReportSystemEnvironment correctly reports environment variables
 * and censors sensitive variables based on configurable regex patterns.
 */
@DisplayName("ReportSystemEnvironment")
@ValidateCharset
@ResetReporterConfig
@WithLocale("en")
@WithMockLogger
class ReportSystemEnvironmentTest {

    @Slf4jMock
    private Logger logger;


    // Helper method to create a ReportSystemEnvironment instance with a mocked environment
    private ReportSystemEnvironment createReportSystemEnvironment(Map<String, String> envMap) {
        return new ReportSystemEnvironment(logger) {
            @Override
            protected Map<String, String> getEnvironmentVariables() {
                return envMap;
            }
        };
    }

    @Test
    @DisplayName("should censor sensitive environment variables with default regex")
    void shouldCensorSensitiveEnvironmentVariablesWithDefaultRegex() {
        // Given: environment variables with sensitive and normal values
        Map<String, String> testEnv = new HashMap<>();
        testEnv.put("TEST_PASSWORD", "mysecretpassword");
        testEnv.put("TEST_SECRET", "anothersecret");
        testEnv.put("TEST_NORMAL", "normalvalue");

        // When: report is executed
        createReportSystemEnvironment(testEnv).run();

        // Then: sensitive variables should be censored with default regex
        assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "TEST_PASSWORD: ********",
                "TEST_SECRET: ********",
                "TEST_NORMAL: normalvalue");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    @DisplayName("should censor sensitive environment variables with custom regex")
    void shouldCensorSensitiveEnvironmentVariablesWithCustomRegex() {
        // Given: custom forbidden regex and environment variables
        System.setProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX, "(?i).*CUSTOM.*");
        ReporterConfig.init(); // Reinitialize to apply custom regex

        Map<String, String> testEnv = new HashMap<>();
        testEnv.put("TEST_CUSTOM_KEY", "customvalue");
        testEnv.put("TEST_NORMAL", "normalvalue");

        // When: report is executed
        createReportSystemEnvironment(testEnv).run();

        // Then: variables matching custom regex should be censored
        assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "TEST_CUSTOM_KEY: ********",
                "TEST_NORMAL: normalvalue");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    @DisplayName("should not censor when regex is empty")
    void shouldNotCensorWhenRegexIsEmpty() {
        // Given: empty forbidden regex and environment variables
        System.setProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX, "");
        ReporterConfig.init(); // Reinitialize to apply empty regex

        Map<String, String> testEnv = new HashMap<>();
        testEnv.put("TEST_PASSWORD", "mysecretpassword");
        testEnv.put("TEST_SECRET", "anothersecret");

        // When: report is executed
        createReportSystemEnvironment(testEnv).run();

        // Then: no variables should be censored
        assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "TEST_PASSWORD: mysecretpassword",
                "TEST_SECRET: anothersecret");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    @DisplayName("should not censor when regex does not match")
    void shouldNotCensorWhenRegexDoesNotMatch() {
        // Given: non-matching forbidden regex and environment variables
        System.setProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX, ".*NONEXISTENT.*");
        ReporterConfig.init(); // Reinitialize to apply non-matching regex

        Map<String, String> testEnv = new HashMap<>();
        testEnv.put("TEST_PASSWORD", "mysecretpassword");
        testEnv.put("TEST_SECRET", "anothersecret");

        // When: report is executed
        createReportSystemEnvironment(testEnv).run();

        // Then: no variables should be censored since regex doesn't match
        assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "TEST_PASSWORD: mysecretpassword",
                "TEST_SECRET: anothersecret");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }
}
