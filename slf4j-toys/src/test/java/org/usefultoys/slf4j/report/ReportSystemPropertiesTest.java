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
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.utils.ConfigParser;
import org.usefultoys.test.CharsetConsistency;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.WithLocale;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({CharsetConsistency.class, ResetReporterConfig.class})
@WithLocale("en")
class ReportSystemPropertiesTest {

    private static final String TEST_LOGGER_NAME = "test.logger";
    private MockLogger mockLogger;

    @BeforeEach
    void setUp() {
        // Initialize MockLogger
        // LoggerFactory.getLogger should return a MockLogger in the test environment due to slf4j-simple binding
        Logger testLogger = LoggerFactory.getLogger(TEST_LOGGER_NAME);
        mockLogger = (MockLogger) testLogger;
        mockLogger.clearEvents();
    }

    @AfterEach
    void tearDown() {
        // Clear test properties
        System.clearProperty("test.password");
        System.clearProperty("test.secret");
        System.clearProperty("test.normal");
        System.clearProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX);
    }

    // Helper method to get all formatted log messages from the MockLogger
    private String getLogOutput() {
        return mockLogger.getLoggerEvents().stream()
                .map(MockLoggerEvent::getFormattedMessage)
                .collect(Collectors.joining("\n"));
    }

    @Test
    void testSensitivePropertiesAreCensoredWithDefaultRegex() {
        System.setProperty("test.password", "mysecretpassword");
        System.setProperty("test.secret", "anothersecret");
        System.setProperty("test.normal", "normalvalue");

        new ReportSystemProperties(mockLogger).run();

        String logOutput = getLogOutput();

        assertTrue(logOutput.contains("test.password: ********"), "Log output should censor test.password");
        assertTrue(logOutput.contains("test.secret: ********"), "Log output should censor test.secret");
        assertTrue(logOutput.contains("test.normal: normalvalue"), "Log output should contain test.normal value");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    void testSensitivePropertiesAreCensoredWithCustomRegex() {
        System.setProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX, "(?i).*custom.*");
        ReporterConfig.init(); // Reinitialize to apply custom regex

        System.setProperty("test.custom.key", "customvalue");
        System.setProperty("test.normal", "normalvalue");

        new ReportSystemProperties(mockLogger).run();

        String logOutput = getLogOutput();

        assertTrue(logOutput.contains("test.custom.key: ********"), "Log output should censor test.custom.key with custom regex");
        assertTrue(logOutput.contains("test.normal: normalvalue"), "Log output should contain test.normal value");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    void testNoCensoringWhenRegexIsEmpty() {
        System.setProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX, "");
        ReporterConfig.init(); // Reinitialize to apply empty regex

        System.setProperty("test.password", "mysecretpassword");
        System.setProperty("test.secret", "anothersecret");

        new ReportSystemProperties(mockLogger).run();

        String logOutput = getLogOutput();

        assertTrue(logOutput.contains("test.password: mysecretpassword"), "Log output should NOT censor test.password when regex is empty");
        assertTrue(logOutput.contains("test.secret: anothersecret"), "Log output should NOT censor test.secret when regex is empty");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    void testNoCensoringWhenRegexDoesNotMatch() {
        System.setProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX, ".*nonexistent.*");
        ReporterConfig.init(); // Reinitialize to apply non-matching regex

        System.setProperty("test.password", "mysecretpassword");
        System.setProperty("test.secret", "anothersecret");

        new ReportSystemProperties(mockLogger).run();

        String logOutput = getLogOutput();

        assertTrue(logOutput.contains("test.password: mysecretpassword"), "Log output should NOT censor test.password when regex does not match");
        assertTrue(logOutput.contains("test.secret: anothersecret"), "Log output should NOT censor test.secret when regex does not match");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }
}
