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
import org.usefultoys.test.CharsetConsistency;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.WithLocale;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.usefultoys.slf4jtestmock.AssertLogger.assertEvent;

@ExtendWith({CharsetConsistency.class, ResetReporterConfig.class, MockLoggerExtension.class})
@WithLocale("en")
class ReportSystemPropertiesTest {

    @Slf4jMock("test.logger.props")
    private Logger logger;

    @AfterEach
    void tearDown() {
        // Clear test properties
        System.clearProperty("test.password");
        System.clearProperty("test.secret");
        System.clearProperty("test.normal");
    }

    @Test
    void testSensitivePropertiesAreCensoredWithDefaultRegex() {
        System.setProperty("test.password", "mysecretpassword");
        System.setProperty("test.secret", "anothersecret");
        System.setProperty("test.normal", "normalvalue");

        new ReportSystemProperties(logger).run();

        assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "test.password: ********",
                "test.secret: ********",
                "test.normal: normalvalue");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    void testSensitivePropertiesAreCensoredWithCustomRegex() {
        System.setProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX, "(?i).*custom.*");
        ReporterConfig.init(); // Reinitialize to apply custom regex

        System.setProperty("test.custom.key", "customvalue");
        System.setProperty("test.normal", "normalvalue");

        new ReportSystemProperties(logger).run();

        assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "test.custom.key: ********",
                "test.normal: normalvalue");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    void testNoCensoringWhenRegexIsEmpty() {
        System.setProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX, "");
        ReporterConfig.init(); // Reinitialize to apply empty regex

        System.setProperty("test.password", "mysecretpassword");
        System.setProperty("test.secret", "anothersecret");

        new ReportSystemProperties(logger).run();

        assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "test.password: mysecretpassword",
                "test.secret: anothersecret");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    void testNoCensoringWhenRegexDoesNotMatch() {
        System.setProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX, ".*nonexistent.*");
        ReporterConfig.init(); // Reinitialize to apply non-matching regex

        System.setProperty("test.password", "mysecretpassword");
        System.setProperty("test.secret", "anothersecret");

        new ReportSystemProperties(logger).run();

        assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "test.password: mysecretpassword",
                "test.secret: anothersecret");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }
}
