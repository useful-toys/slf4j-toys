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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.utils.ConfigParser;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ReportClasspath}.
 * <p>
 * Tests verify that ReportClasspath correctly reads and logs classpath entries
 * including various separators (Unix, Windows) and edge cases (empty, null).
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Classpath Entries Reporting:</b> Verifies logging of classpath entries with different separators</li>
 *   <li><b>Empty Classpath Handling:</b> Tests proper handling when classpath is empty</li>
 * </ul>
 */
@DisplayName("ReportClasspath")
@ValidateCharset
@ResetReporterConfig
@WithLocale("en")
@WithMockLogger
class ReportClasspathTest {

    @Slf4jMock
    private Logger logger;
    private MockedStatic<ManagementFactory> mockedManagementFactory;
    private RuntimeMXBean mockRuntimeMXBean;

    /**
     * Setup mocked ManagementFactory and RuntimeMXBean for testing.
     */
    private void setupRuntimeMXBean() {
        mockedManagementFactory = Mockito.mockStatic(ManagementFactory.class);
        mockRuntimeMXBean = mock(RuntimeMXBean.class);
        mockedManagementFactory.when(ManagementFactory::getRuntimeMXBean).thenReturn(mockRuntimeMXBean);
    }

    @AfterEach
    void tearDown() {
        if (mockedManagementFactory != null) {
            mockedManagementFactory.close();
        }
    }

    @Test
    @DisplayName("should report classpath entries")
    void testClasspathIsReported() {
        // Given: a classpath with multiple entries separated by colon
        final String classpath = "/path/to/my.jar:/path/to/classes";
        final String pathSeparator = ":";
        final Map<String, String> systemProperties = new HashMap<>();
        systemProperties.put("path.separator", pathSeparator);

        setupRuntimeMXBean();
        when(mockRuntimeMXBean.getClassPath()).thenReturn(classpath);
        when(mockRuntimeMXBean.getSystemProperties()).thenReturn(systemProperties);

        // When: report is executed
        new ReportClasspath(logger).run();

        // Then: should log all classpath entries
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Classpath:",
            " - /path/to/my.jar",
            " - /path/to/classes");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    @DisplayName("should report empty classpath")
    void testEmptyClasspathIsReported() {
        // Given: an empty classpath
        final String classpath = "";
        final String pathSeparator = ":";
        final Map<String, String> systemProperties = new HashMap<>();
        systemProperties.put("path.separator", pathSeparator);

        setupRuntimeMXBean();
        when(mockRuntimeMXBean.getClassPath()).thenReturn(classpath);
        when(mockRuntimeMXBean.getSystemProperties()).thenReturn(systemProperties);

        // When: report is executed
        new ReportClasspath(logger).run();

        // Then: should log that classpath is empty
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Classpath:",
            " - Classpath is empty.");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    @DisplayName("should report null classpath as empty")
    void testNullClasspathIsReportedAsEmpty() {
        // Given: a null classpath
        final String classpath = null;
        final String pathSeparator = ":";
        final Map<String, String> systemProperties = new HashMap<>();
        systemProperties.put("path.separator", pathSeparator);

        setupRuntimeMXBean();
        when(mockRuntimeMXBean.getClassPath()).thenReturn(classpath);
        when(mockRuntimeMXBean.getSystemProperties()).thenReturn(systemProperties);

        // When: report is executed
        new ReportClasspath(logger).run();

        // Then: should log that classpath is empty
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Classpath:",
            " - Classpath is empty.");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    @DisplayName("should report classpath with Windows separator")
    void testClasspathWithWindowsSeparator() {
        // Given: a classpath with Windows separator and paths
        final String classpath = "C:\\path\\to\\my.jar;C:\\path\\to\\classes";
        final String pathSeparator = ";";
        final Map<String, String> systemProperties = new HashMap<>();
        systemProperties.put("path.separator", pathSeparator);

        setupRuntimeMXBean();
        when(mockRuntimeMXBean.getClassPath()).thenReturn(classpath);
        when(mockRuntimeMXBean.getSystemProperties()).thenReturn(systemProperties);

        // When: report is executed
        new ReportClasspath(logger).run();

        // Then: should log all classpath entries correctly separated
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Classpath:",
            " - C:\\path\\to\\my.jar",
            " - C:\\path\\to\\classes");
        assertTrue(ConfigParser.isInitializationOK());
    }
}
