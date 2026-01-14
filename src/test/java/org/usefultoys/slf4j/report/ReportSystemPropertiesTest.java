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
import org.usefultoys.slf4j.utils.ConfigParser;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.ResetSystemProperty;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.usefultoys.slf4jtestmock.AssertLogger.assertEvent;

/**
 * Unit tests for {@link ReportSystemProperties}.
 * <p>
 * Tests verify that ReportSystemProperties correctly reports system properties
 * and censors sensitive properties based on configurable regex patterns.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>System Property Reporting:</b> Tests reporting of system properties with censoring of sensitive data using default regex</li>
 *   <li><b>Custom Regex Censoring:</b> Verifies censoring based on custom regex patterns</li>
 *   <li><b>Empty Regex Handling:</b> Tests behavior when no censoring regex is configured</li>
 *   <li><b>Non-Matching Regex:</b> Ensures properties are not censored when regex doesn't match</li>
 * </ul>
 */
@SuppressWarnings("NonConstantLogger")
@DisplayName("ReportSystemProperties")
@ValidateCharset
@ResetReporterConfig
@WithLocale("en")
@WithMockLogger
class ReportSystemPropertiesTest {

    @Slf4jMock
    private Logger logger;


    @Test
    @DisplayName("should censor sensitive properties with default regex")
    @ResetSystemProperty("test.password")
    @ResetSystemProperty("test.secret")
    @ResetSystemProperty("test.normal")
    void shouldCensorSensitivePropertiesWithDefaultRegex() {
        // Given: system properties with sensitive and normal values
        System.setProperty("test.password", "mysecretpassword");
        System.setProperty("test.secret", "anothersecret");
        System.setProperty("test.normal", "normalvalue");

        // When: report is executed
        new ReportSystemProperties(logger).run();

        // Then: sensitive properties should be censored with default regex
        assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "test.password: ********",
                "test.secret: ********",
                "test.normal: normalvalue");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    @DisplayName("should censor sensitive properties with custom regex")
    @ResetSystemProperty("test.custom.key")
    @ResetSystemProperty("test.normal")
    void shouldCensorSensitivePropertiesWithCustomRegex() {
        // Given: custom forbidden regex and system properties
        System.setProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX, "(?i).*custom.*");
        ReporterConfig.init(); // Reinitialize to apply custom regex

        System.setProperty("test.custom.key", "customvalue");
        System.setProperty("test.normal", "normalvalue");

        // When: report is executed
        new ReportSystemProperties(logger).run();

        // Then: properties matching custom regex should be censored
        assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "test.custom.key: ********",
                "test.normal: normalvalue");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    @DisplayName("should not censor when regex is empty")
    @ResetSystemProperty("test.password")
    @ResetSystemProperty("test.secret")
    void shouldNotCensorWhenRegexIsEmpty() {
        // Given: empty forbidden regex and system properties
        System.setProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX, "");
        ReporterConfig.init(); // Reinitialize to apply empty regex

        System.setProperty("test.password", "mysecretpassword");
        System.setProperty("test.secret", "anothersecret");

        // When: report is executed
        new ReportSystemProperties(logger).run();

        // Then: no properties should be censored
        assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "test.password: mysecretpassword",
                "test.secret: anothersecret");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    @DisplayName("should not censor when regex does not match")
    @ResetSystemProperty("test.password")
    @ResetSystemProperty("test.secret")
    void shouldNotCensorWhenRegexDoesNotMatch() {
        // Given: non-matching forbidden regex and system properties
        System.setProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX, ".*nonexistent.*");
        ReporterConfig.init(); // Reinitialize to apply non-matching regex

        System.setProperty("test.password", "mysecretpassword");
        System.setProperty("test.secret", "anothersecret");

        // When: report is executed
        new ReportSystemProperties(logger).run();

        // Then: no properties should be censored since regex doesn't match
        assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "test.password: mysecretpassword",
                "test.secret: anothersecret");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }
}
