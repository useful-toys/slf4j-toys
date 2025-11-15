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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;
import org.usefultoys.slf4j.utils.ConfigParser;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportSystemEnvironmentTest {

    private static final String TEST_LOGGER_NAME = "test.logger";
    private MockLogger mockLogger;
    private Map<String, String> originalEnv; // To store original environment variables

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

        // Backup original environment variables
        originalEnv = new HashMap<>(System.getenv());
    }

    @AfterEach
    void tearDown() {
        // Restore original environment variables
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            java.lang.reflect.Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.clear();
            env.putAll(originalEnv);

            java.lang.reflect.Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.clear();
            cienv.putAll(originalEnv);
        } catch (Exception e) {
            System.err.println("Warning: Could not restore environment variables via reflection. Test might not be fully isolated. " + e.getMessage());
        }

        // Clear test properties
        System.clearProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX);

        // Reset all relevant configs again for good measure
        ReporterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }

    // Helper method to simulate environment variables for testing purposes
    // This is a workaround as System.getenv() is immutable after startup
    private void setEnv(String key, String value) {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            java.lang.reflect.Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.put(key, value);

            java.lang.reflect.Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.put(key, value);
        } catch (Exception e) {
            System.err.println("Warning: Could not set environment variable via reflection. Test might not be fully isolated. " + e.getMessage());
        }
    }

    // Helper method to get all formatted log messages from the MockLogger
    private String getLogOutput() {
        return mockLogger.getLoggerEvents().stream()
                .map(MockLoggerEvent::getFormattedMessage)
                .collect(Collectors.joining("\n"));
    }

    @Test
    void testSensitiveEnvironmentVariablesAreCensoredWithDefaultRegex() {
        setEnv("TEST_PASSWORD", "mysecretpassword");
        setEnv("TEST_SECRET", "anothersecret");
        setEnv("TEST_NORMAL", "normalvalue");

        new ReportSystemEnvironment(mockLogger).run();

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

        setEnv("TEST_CUSTOM_KEY", "customvalue");
        setEnv("TEST_NORMAL", "normalvalue");

        new ReportSystemEnvironment(mockLogger).run();

        String logOutput = getLogOutput();

        assertTrue(logOutput.contains("TEST_CUSTOM_KEY: ********"), "Log output should censor TEST_CUSTOM_KEY with custom regex");
        assertTrue(logOutput.contains("TEST_NORMAL: normalvalue"), "Log output should contain TEST_NORMAL value");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    void testNoCensoringWhenRegexIsEmpty() {
        System.setProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX, "");
        ReporterConfig.init(); // Reinitialize to apply empty regex

        setEnv("TEST_PASSWORD", "mysecretpassword");
        setEnv("TEST_SECRET", "anothersecret");

        new ReportSystemEnvironment(mockLogger).run();

        String logOutput = getLogOutput();

        assertTrue(logOutput.contains("TEST_PASSWORD: mysecretpassword"), "Log output should NOT censor TEST_PASSWORD when regex is empty");
        assertTrue(logOutput.contains("TEST_SECRET: anothersecret"), "Log output should NOT censor TEST_SECRET when regex is empty");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    void testNoCensoringWhenRegexDoesNotMatch() {
        System.setProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX, ".*NONEXISTENT.*");
        ReporterConfig.init(); // Reinitialize to apply non-matching regex

        setEnv("TEST_PASSWORD", "mysecretpassword");
        setEnv("TEST_SECRET", "anothersecret");

        new ReportSystemEnvironment(mockLogger).run();

        String logOutput = getLogOutput();

        assertTrue(logOutput.contains("TEST_PASSWORD: mysecretpassword"), "Log output should NOT censor TEST_PASSWORD when regex does not match");
        assertTrue(logOutput.contains("TEST_SECRET: anothersecret"), "Log output should NOT censor TEST_SECRET when regex does not match");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }
}
