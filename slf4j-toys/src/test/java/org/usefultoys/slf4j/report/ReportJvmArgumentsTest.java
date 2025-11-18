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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportJvmArgumentsTest {

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
        System.clearProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX);
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
    void testJvmArgumentsAreReported() {
        List<String> jvmArgs = Arrays.asList("-Xmx512m", "-Djava.awt.headless=true");
        when(mockRuntimeMXBean.getInputArguments()).thenReturn(jvmArgs);

        new ReportJvmArguments(mockLogger).run();

        String logOutput = getLogOutput();
        assertTrue(logOutput.contains("JVM Arguments:"));
        assertTrue(logOutput.contains(" - -Xmx512m"));
        assertTrue(logOutput.contains(" - -Djava.awt.headless=true"));
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testSensitiveJvmArgumentsAreCensoredWithDefaultRegex() {
        List<String> jvmArgs = Arrays.asList(
                "-Xmx512m",
                "-Dmy.password=secret123",
                "-Ddb.key=dbkeyvalue",
                "-Dnormal.prop=normalvalue"
        );
        when(mockRuntimeMXBean.getInputArguments()).thenReturn(jvmArgs);

        new ReportJvmArguments(mockLogger).run();

        String logOutput = getLogOutput();
        assertTrue(logOutput.contains(" - -Dmy.password=********"));
        assertTrue(logOutput.contains(" - -Ddb.key=********"));
        assertTrue(logOutput.contains(" - -Dnormal.prop=normalvalue"));
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testSensitiveJvmArgumentsAreCensoredWithCustomRegex() {
        System.setProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX, "(?i).*custom.*");
        ReporterConfig.init(); // Reinitialize to apply custom regex

        List<String> jvmArgs = Arrays.asList(
                "-Dapp.custom.token=tokenvalue",
                "-Dapp.normal=normalvalue"
        );
        when(mockRuntimeMXBean.getInputArguments()).thenReturn(jvmArgs);

        new ReportJvmArguments(mockLogger).run();

        String logOutput = getLogOutput();
        assertTrue(logOutput.contains(" - -Dapp.custom.token=********"));
        assertTrue(logOutput.contains(" - -Dapp.normal=normalvalue"));
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testNoJvmArgumentsFound() {
        when(mockRuntimeMXBean.getInputArguments()).thenReturn(Arrays.asList());

        new ReportJvmArguments(mockLogger).run();

        String logOutput = getLogOutput();
        assertTrue(logOutput.contains(" - No JVM arguments found."));
        assertTrue(ConfigParser.isInitializationOK());
    }
}
