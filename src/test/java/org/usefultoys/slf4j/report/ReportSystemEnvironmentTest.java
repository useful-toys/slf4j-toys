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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.utils.ConfigParser;
import org.usefultoys.slf4jtestmock.MockLoggerExtension;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.test.CharsetConsistencyExtension;
import org.usefultoys.test.ResetReporterConfigExtension;
import org.usefultoys.test.WithLocale;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.usefultoys.slf4jtestmock.AssertLogger.assertEvent;

@ExtendWith({CharsetConsistencyExtension.class, ResetReporterConfigExtension.class, MockLoggerExtension.class})
@WithLocale("en")
class ReportSystemEnvironmentTest {

    @Slf4jMock("test.logger.env")
    private Logger logger;

    @AfterEach
    void tearDown() {
        // Clear test properties
        System.clearProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX);
    }

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
    void testSensitiveEnvironmentVariablesAreCensoredWithDefaultRegex() {
        Map<String, String> testEnv = new HashMap<>();
        testEnv.put("TEST_PASSWORD", "mysecretpassword");
        testEnv.put("TEST_SECRET", "anothersecret");
        testEnv.put("TEST_NORMAL", "normalvalue");

        createReportSystemEnvironment(testEnv).run();

        assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "TEST_PASSWORD: ********",
                "TEST_SECRET: ********",
                "TEST_NORMAL: normalvalue");
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

        assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "TEST_CUSTOM_KEY: ********",
                "TEST_NORMAL: normalvalue");
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

        assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "TEST_PASSWORD: mysecretpassword",
                "TEST_SECRET: anothersecret");
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

        assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "TEST_PASSWORD: mysecretpassword",
                "TEST_SECRET: anothersecret");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }
}
