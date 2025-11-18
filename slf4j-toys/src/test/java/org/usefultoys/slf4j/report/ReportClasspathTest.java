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
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;
import org.usefultoys.slf4j.utils.ConfigParser;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportClasspathTest {

    @BeforeAll
    static void validateConsistentCharset() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    private static final String TEST_LOGGER_NAME = "test.logger";
    private MockLogger mockLogger;
    private MockedStatic<ManagementFactory> mockedManagementFactory;
    private RuntimeMXBean mockRuntimeMXBean;

    @BeforeEach
    void setUp() {
        ConfigParser.clearInitializationErrors();
        ReporterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();

        Logger testLogger = LoggerFactory.getLogger(TEST_LOGGER_NAME);
        mockLogger = (MockLogger) testLogger;
        mockLogger.clearEvents();
        mockLogger.setInfoEnabled(true); // Ensure INFO level is enabled

        // Mock ManagementFactory and RuntimeMXBean
        mockedManagementFactory = Mockito.mockStatic(ManagementFactory.class);
        mockRuntimeMXBean = mock(RuntimeMXBean.class);
        mockedManagementFactory.when(ManagementFactory::getRuntimeMXBean).thenReturn(mockRuntimeMXBean);
    }

    @AfterEach
    void tearDown() {
        mockedManagementFactory.close(); // Close the mock static
        ReporterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
        ConfigParser.clearInitializationErrors();
    }

    private String getLogOutput() {
        return mockLogger.getLoggerEvents().stream()
                .map(MockLoggerEvent::getFormattedMessage)
                .collect(Collectors.joining("\n"));
    }

    @Test
    void testClasspathIsReported() {
        String classpath = "/path/to/my.jar:/path/to/classes";
        String pathSeparator = ":";
        Map<String, String> systemProperties = new HashMap<>();
        systemProperties.put("path.separator", pathSeparator);

        when(mockRuntimeMXBean.getClassPath()).thenReturn(classpath);
        when(mockRuntimeMXBean.getSystemProperties()).thenReturn(systemProperties);

        new ReportClasspath(mockLogger).run();

        String logOutput = getLogOutput();
        assertTrue(logOutput.contains("Classpath:"));
        assertTrue(logOutput.contains(" - /path/to/my.jar"));
        assertTrue(logOutput.contains(" - /path/to/classes"));
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testEmptyClasspathIsReported() {
        String classpath = "";
        String pathSeparator = ":";
        Map<String, String> systemProperties = new HashMap<>();
        systemProperties.put("path.separator", pathSeparator);

        when(mockRuntimeMXBean.getClassPath()).thenReturn(classpath);
        when(mockRuntimeMXBean.getSystemProperties()).thenReturn(systemProperties);

        new ReportClasspath(mockLogger).run();

        String logOutput = getLogOutput();
        assertTrue(logOutput.contains(" - Classpath is empty."));
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testNullClasspathIsReportedAsEmpty() {
        String classpath = null;
        String pathSeparator = ":";
        Map<String, String> systemProperties = new HashMap<>();
        systemProperties.put("path.separator", pathSeparator);

        when(mockRuntimeMXBean.getClassPath()).thenReturn(classpath);
        when(mockRuntimeMXBean.getSystemProperties()).thenReturn(systemProperties);

        new ReportClasspath(mockLogger).run();

        String logOutput = getLogOutput();
        assertTrue(logOutput.contains(" - Classpath is empty."));
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testClasspathWithWindowsSeparator() {
        String classpath = "C:\\path\\to\\my.jar;C:\\path\\to\\classes";
        String pathSeparator = ";";
        Map<String, String> systemProperties = new HashMap<>();
        systemProperties.put("path.separator", pathSeparator);

        when(mockRuntimeMXBean.getClassPath()).thenReturn(classpath);
        when(mockRuntimeMXBean.getSystemProperties()).thenReturn(systemProperties);

        new ReportClasspath(mockLogger).run();

        String logOutput = getLogOutput();
        assertTrue(logOutput.contains(" - C:\\path\\to\\my.jar"));
        assertTrue(logOutput.contains(" - C:\\path\\to\\classes"));
        assertTrue(ConfigParser.isInitializationOK());
    }
}
