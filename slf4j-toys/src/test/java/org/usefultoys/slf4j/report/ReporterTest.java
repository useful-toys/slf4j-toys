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
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;
import org.usefultoys.slf4j.utils.ConfigParser;

import java.nio.charset.Charset;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReporterTest {

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    private static final String TEST_LOGGER_NAME = "test.reporter";
    private MockLogger mockLogger;
    private Reporter reporter;

    @BeforeEach
    void setUp() {
        ConfigParser.clearInitializationErrors();
        ReporterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();

        Logger testLogger = LoggerFactory.getLogger(TEST_LOGGER_NAME);
        mockLogger = (MockLogger) testLogger;
        mockLogger.clearEvents();
        mockLogger.setInfoEnabled(true); // Ensure INFO level is enabled for reports

        reporter = new Reporter(mockLogger);
    }

    @AfterEach
    void tearDown() {
        ReporterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
        ConfigParser.clearInitializationErrors();
    }

    @Test
    void testRunDefaultReportExecutesAllEnabledReports() {
        // Enable all reports
        System.setProperty(ReporterConfig.PROP_VM, "true");
        System.setProperty(ReporterConfig.PROP_FILE_SYSTEM, "true");
        System.setProperty(ReporterConfig.PROP_MEMORY, "true");
        System.setProperty(ReporterConfig.PROP_USER, "true");
        System.setProperty(ReporterConfig.PROP_PROPERTIES, "true");
        System.setProperty(ReporterConfig.PROP_ENVIRONMENT, "true");
        System.setProperty(ReporterConfig.PROP_PHYSICAL_SYSTEM, "true");
        System.setProperty(ReporterConfig.PROP_OPERATING_SYSTEM, "true");
        System.setProperty(ReporterConfig.PROP_CALENDAR, "true");
        System.setProperty(ReporterConfig.PROP_LOCALE, "true");
        System.setProperty(ReporterConfig.PROP_CHARSET, "true");
        System.setProperty(ReporterConfig.PROP_NETWORK_INTERFACE, "false"); // Disable network interface as it can be slow/problematic in tests
        System.setProperty(ReporterConfig.PROP_SSL_CONTEXT, "true");
        System.setProperty(ReporterConfig.PROP_DEFAULT_TRUST_KEYSTORE, "true");
        System.setProperty(ReporterConfig.PROP_JVM_ARGUMENTS, "true");
        System.setProperty(ReporterConfig.PROP_CLASSPATH, "true");
        System.setProperty(ReporterConfig.PROP_GARBAGE_COLLECTOR, "true");
        ReporterConfig.init();

        // Use a custom executor to count executions
        AtomicInteger executionCount = new AtomicInteger(0);
        Executor countingExecutor = command -> {
            command.run();
            executionCount.incrementAndGet();
        };

        reporter.logDefaultReports(countingExecutor);

        // Expect 15 reports (all enabled except network interface)
        assertEquals(16, executionCount.get(), "All enabled reports should be executed");
        assertTrue(mockLogger.getEventCount() > 0, "Logger should have received events");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    void testRunDefaultReportExecutesOnlySelectedReports() {
        // Enable only a few reports
        System.setProperty(ReporterConfig.PROP_VM, "true");
        System.setProperty(ReporterConfig.PROP_PROPERTIES, "true");
        System.setProperty(ReporterConfig.PROP_JVM_ARGUMENTS, "true");
        ReporterConfig.init();

        AtomicInteger executionCount = new AtomicInteger(0);
        Executor countingExecutor = command -> {
            command.run();
            executionCount.incrementAndGet();
        };

        reporter.logDefaultReports(countingExecutor);

        assertEquals(6, mockLogger.getEventCount(), "Logger should have received 6 events");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    void testRunDefaultReportWithNoReportsEnabled() {
        // Disable all reports (default is mostly true, so explicitly disable)
        System.setProperty(ReporterConfig.PROP_VM, "false");
        System.setProperty(ReporterConfig.PROP_FILE_SYSTEM, "false");
        System.setProperty(ReporterConfig.PROP_MEMORY, "false");
        System.setProperty(ReporterConfig.PROP_USER, "false");
        System.setProperty(ReporterConfig.PROP_PROPERTIES, "false");
        System.setProperty(ReporterConfig.PROP_ENVIRONMENT, "false");
        System.setProperty(ReporterConfig.PROP_PHYSICAL_SYSTEM, "false");
        System.setProperty(ReporterConfig.PROP_OPERATING_SYSTEM, "false");
        System.setProperty(ReporterConfig.PROP_CALENDAR, "false");
        System.setProperty(ReporterConfig.PROP_LOCALE, "false");
        System.setProperty(ReporterConfig.PROP_CHARSET, "false");
        System.setProperty(ReporterConfig.PROP_NETWORK_INTERFACE, "false");
        System.setProperty(ReporterConfig.PROP_SSL_CONTEXT, "false");
        System.setProperty(ReporterConfig.PROP_DEFAULT_TRUST_KEYSTORE, "false");
        System.setProperty(ReporterConfig.PROP_JVM_ARGUMENTS, "false");
        System.setProperty(ReporterConfig.PROP_CLASSPATH, "false");
        System.setProperty(ReporterConfig.PROP_GARBAGE_COLLECTOR, "false");
        ReporterConfig.init();

        AtomicInteger executionCount = new AtomicInteger(0);
        Executor countingExecutor = command -> {
            command.run();
            executionCount.incrementAndGet();
        };

        reporter.logDefaultReports(countingExecutor);

        assertEquals(0, executionCount.get(), "No reports should be executed");
        assertEquals(0, mockLogger.getEventCount(), "Logger should not have received events");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    void testRunDefaultReportWithSameThreadExecutor() {
        // Enable a few reports
        System.setProperty(ReporterConfig.PROP_VM, "true");
        System.setProperty(ReporterConfig.PROP_PROPERTIES, "true");
        System.setProperty(ReporterConfig.PROP_NAME, TEST_LOGGER_NAME);
        ReporterConfig.init();

        // Test the static runDefaultReport method
        Reporter.runDefaultReport();

        // Verify that reports were logged
        assertEquals(5, mockLogger.getEventCount(), "Logger should have received 5 events");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }
}
