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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;
import org.usefultoys.slf4j.utils.ConfigParser;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportSystemEnvironmentTest {

    @BeforeAll
    static void validateConsistentCharset() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    private static final String TEST_LOGGER_NAME = "test.logger";
    private MockLogger mockLogger;

    @BeforeEach
    void setUp() {
        // Clear any previous errors from ConfigParser
        ConfigParser.clearInitializationErrors();

        // Initialize MockLogger
        Logger testLogger = LoggerFactory.getLogger(TEST_LOGGER_NAME);
        mockLogger = (MockLogger) testLogger;
        mockLogger.clearEvents();

        // Reset all relevant configs to ensure a clean state for ConfigParser.initializationErrors
        // ReporterConfig.reset() calls init() internally
        ReporterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }

    @AfterEach
    void tearDown() {
        // Clear test properties
        System.clearProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX);

        // Reset all relevant configs again for good measure
        ReporterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }

    // Helper method to get all formatted log messages from the MockLogger
    private String getLogOutput() {
        return mockLogger.getLoggerEvents().stream()
                .map(MockLoggerEvent::getFormattedMessage)
                .collect(Collectors.joining("\n"));
    }

    // Helper method to create a ReportSystemEnvironment instance with a mocked environment
    private ReportSystemEnvironment createReportSystemEnvironment(Map<String, String> envMap) {
        return new ReportSystemEnvironment(mockLogger) {
            @Override
            protected Map<String, String> getEnvironmentVariables() {
                return envMap;
            }
        };
    }

    @Test
    void testSensitiveEnvironmentVariablesAreCensoredWithDefaultRegex() {
        Map<String, String> testEnv = new HashMap<>();
        testEnv.put("TEST_PASSWORD", "mysecretpassword");
        testEnv.put("TEST_SECRET", "anothersecret");
        testEnv.put("TEST_NORMAL", "normalvalue");

        createReportSystemEnvironment(testEnv).run();

        String logOutput = getLogOutput();

        assertTrue(logOutput.contains("TEST_PASSWORD: ********"), "Log output should censor TEST_PASSWORD");
        assertTrue(logOutput.contains("TEST_SECRET: ********"), "Log output should censor TEST_SECRET");
        assertTrue(logOutput.contains("TEST_NORMAL: normalvalue"), "Log output should contain TEST_NORMAL value");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    void testSensitiveEnvironmentVariablesAreCensoredWithCustomRegex() {
        System.setProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX, "(?i).*CUSTOM.*");
        ReporterConfig.init(); // Reinitialize to apply custom regex

        Map<String, String> testEnv = new HashMap<>();
        testEnv.put("TEST_CUSTOM_KEY", "customvalue");
        testEnv.put("TEST_NORMAL", "normalvalue");

        createReportSystemEnvironment(testEnv).run();

        String logOutput = getLogOutput();

        assertTrue(logOutput.contains("TEST_CUSTOM_KEY: ********"), "Log output should censor TEST_CUSTOM_KEY with custom regex");
        assertTrue(logOutput.contains("TEST_NORMAL: normalvalue"), "Log output should contain TEST_NORMAL value");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    void testNoCensoringWhenRegexIsEmpty() {
        System.setProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX, "");
        ReporterConfig.init(); // Reinitialize to apply empty regex

        Map<String, String> testEnv = new HashMap<>();
        testEnv.put("TEST_PASSWORD", "mysecretpassword");
        testEnv.put("TEST_SECRET", "anothersecret");

        createReportSystemEnvironment(testEnv).run();

        String logOutput = getLogOutput();

        assertTrue(logOutput.contains("TEST_PASSWORD: mysecretpassword"), "Log output should NOT censor TEST_PASSWORD when regex is empty");
        assertTrue(logOutput.contains("TEST_SECRET: anothersecret"), "Log output should NOT censor TEST_SECRET when regex is empty");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    void testNoCensoringWhenRegexDoesNotMatch() {
        System.setProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX, ".*NONEXISTENT.*");
        ReporterConfig.init(); // Reinitialize to apply non-matching regex

        Map<String, String> testEnv = new HashMap<>();
        testEnv.put("TEST_PASSWORD", "mysecretpassword");
        testEnv.put("TEST_SECRET", "anothersecret");

        createReportSystemEnvironment(testEnv).run();

        String logOutput = getLogOutput();

        assertTrue(logOutput.contains("TEST_PASSWORD: mysecretpassword"), "Log output should NOT censor TEST_PASSWORD when regex does not match");
        assertTrue(logOutput.contains("TEST_SECRET: anothersecret"), "Log output should NOT censor TEST_SECRET when regex does not match");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }
}
