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
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.MockLoggerExtension;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4j.utils.ConfigParser;
import org.usefultoys.test.CharsetConsistencyExtension;
import org.usefultoys.test.ResetReporterConfigExtension;
import org.usefultoys.test.WithLocale;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({CharsetConsistencyExtension.class, ResetReporterConfigExtension.class, MockLoggerExtension.class})
@WithLocale("en")
class ReportClasspathTest {

    private static final String TEST_LOGGER_NAME = "test.report.classpath";
    @Slf4jMock(TEST_LOGGER_NAME)
    private Logger logger;
    private MockedStatic<ManagementFactory> mockedManagementFactory;
    private RuntimeMXBean mockRuntimeMXBean;

    private void setupRuntimeMXBean() {
        mockedManagementFactory = Mockito.mockStatic(ManagementFactory.class);
        mockRuntimeMXBean = mock(RuntimeMXBean.class);
        mockedManagementFactory.when(ManagementFactory::getRuntimeMXBean).thenReturn(mockRuntimeMXBean);
    }

    @AfterEach
    void tearDown() {
        if (mockedManagementFactory != null) {
            mockedManagementFactory.close(); // Close the mock static
        }
    }

    @Test
    void testClasspathIsReported() {
        String classpath = "/path/to/my.jar:/path/to/classes";
        String pathSeparator = ":";
        Map<String, String> systemProperties = new HashMap<>();
        systemProperties.put("path.separator", pathSeparator);

        setupRuntimeMXBean();
        when(mockRuntimeMXBean.getClassPath()).thenReturn(classpath);
        when(mockRuntimeMXBean.getSystemProperties()).thenReturn(systemProperties);

        new ReportClasspath(logger).run();
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Classpath:",
            " - /path/to/my.jar",
            " - /path/to/classes");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testEmptyClasspathIsReported() {
        String classpath = "";
        String pathSeparator = ":";
        Map<String, String> systemProperties = new HashMap<>();
        systemProperties.put("path.separator", pathSeparator);

        setupRuntimeMXBean();
        when(mockRuntimeMXBean.getClassPath()).thenReturn(classpath);
        when(mockRuntimeMXBean.getSystemProperties()).thenReturn(systemProperties);

        new ReportClasspath(logger).run();
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Classpath:",
            " - Classpath is empty.");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testNullClasspathIsReportedAsEmpty() {
        String classpath = null;
        String pathSeparator = ":";
        Map<String, String> systemProperties = new HashMap<>();
        systemProperties.put("path.separator", pathSeparator);

        setupRuntimeMXBean();
        when(mockRuntimeMXBean.getClassPath()).thenReturn(classpath);
        when(mockRuntimeMXBean.getSystemProperties()).thenReturn(systemProperties);

        new ReportClasspath(logger).run();
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Classpath:",
            " - Classpath is empty.");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testClasspathWithWindowsSeparator() {
        String classpath = "C:\\path\\to\\my.jar;C:\\path\\to\\classes";
        String pathSeparator = ";";
        Map<String, String> systemProperties = new HashMap<>();
        systemProperties.put("path.separator", pathSeparator);

        setupRuntimeMXBean();
        when(mockRuntimeMXBean.getClassPath()).thenReturn(classpath);
        when(mockRuntimeMXBean.getSystemProperties()).thenReturn(systemProperties);

        new ReportClasspath(logger).run();
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Classpath:",
            " - C:\\path\\to\\my.jar",
            " - C:\\path\\to\\classes");
        assertTrue(ConfigParser.isInitializationOK());
    }
}
