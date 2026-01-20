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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.usefultoys.slf4jtestmock.AssertLogger.assertHasEvent;

/**
 * Unit tests for {@link ReportJvmArguments}.
 * <p>
 * Tests verify that ReportJvmArguments correctly reports JVM arguments
 * and censors sensitive arguments based on configurable regex patterns.
 */
@SuppressWarnings("NonConstantLogger")
@DisplayName("ReportJvmArguments")
@ValidateCharset
@ResetReporterConfig
@WithLocale("en")
@WithMockLogger
class ReportJvmArgumentsTest {

    @Slf4jMock
    private Logger logger;
    private MockedStatic<ManagementFactory> mockedManagementFactory;
    private RuntimeMXBean mockRuntimeMXBean;

    @AfterEach
    void tearDown() {
        if (mockedManagementFactory != null) {
            mockedManagementFactory.close(); // Close the mock static
        }
    }

    private void setupManagedFactory() {
        mockedManagementFactory = Mockito.mockStatic(ManagementFactory.class);
        mockRuntimeMXBean = mock(RuntimeMXBean.class);
        mockedManagementFactory.when(ManagementFactory::getRuntimeMXBean).thenReturn(mockRuntimeMXBean);
    }

    @Test
    @DisplayName("should report JVM arguments")
    void shouldReportJvmArguments() {
        // Given: JVM arguments configured in RuntimeMXBean
        setupManagedFactory();
        final List<String> jvmArgs = Arrays.asList("-Xmx512m", "-Djava.awt.headless=true");
        when(mockRuntimeMXBean.getInputArguments()).thenReturn(jvmArgs);

        // When: report is executed
        new ReportJvmArguments(logger).run();

        // Then: should log all JVM arguments
        assertHasEvent(logger, "JVM Arguments:");
        assertHasEvent(logger, " - -Xmx512m");
        assertHasEvent(logger, " - -Djava.awt.headless=true");
    }

    @Test
    @DisplayName("should censor sensitive JVM arguments with default regex")
    void shouldCensorSensitiveJvmArgumentsWithDefaultRegex() {
        // Given: JVM arguments with sensitive properties
        setupManagedFactory();
        final List<String> jvmArgs = Arrays.asList(
                "-Xmx512m",
                "-Dmy.password=secret123",
                "-Ddb.key=dbkeyvalue",
                "-Dnormal.prop=normalvalue"
        );
        when(mockRuntimeMXBean.getInputArguments()).thenReturn(jvmArgs);

        // When: report is executed
        new ReportJvmArguments(logger).run();

        // Then: sensitive arguments should be censored with default regex
        assertHasEvent(logger, " - -Dmy.password=********");
        assertHasEvent(logger, " - -Ddb.key=********");
        assertHasEvent(logger, " - -Dnormal.prop=normalvalue");
    }

    @Test
    @DisplayName("should censor sensitive JVM arguments with custom regex")
    void shouldCensorSensitiveJvmArgumentsWithCustomRegex() {
        // Given: custom forbidden regex and JVM arguments
        setupManagedFactory();
        System.setProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX, "(?i).*custom.*");
        ReporterConfig.init(); // Reinitialize to apply custom regex

        final List<String> jvmArgs = Arrays.asList(
                "-Dapp.custom.token=tokenvalue",
                "-Dapp.normal=normalvalue"
        );
        when(mockRuntimeMXBean.getInputArguments()).thenReturn(jvmArgs);

        // When: report is executed
        new ReportJvmArguments(logger).run();

        // Then: arguments matching custom regex should be censored
        assertHasEvent(logger, " - -Dapp.custom.token=********");
        assertHasEvent(logger, " - -Dapp.normal=normalvalue");
    }

    @Test
    @DisplayName("should handle no JVM arguments")
    void shouldHandleNoJvmArguments() {
        // Given: no JVM arguments available
        setupManagedFactory();
        when(mockRuntimeMXBean.getInputArguments()).thenReturn(Collections.emptyList());

        // When: report is executed
        new ReportJvmArguments(logger).run();

        // Then: should log that no arguments were found
        assertHasEvent(logger, " - No JVM arguments found.");
    }

    @Test
    @DisplayName("should not censor JVM argument with equals sign at position 2 (-D=value)")
    void shouldNotCensorJvmArgumentWithEqualsAtPosition2() {
        // Given: JVM argument with equals sign at position 2 (not a valid key=value pair)
        setupManagedFactory();
        final List<String> jvmArgs = Arrays.asList("-D=value");
        when(mockRuntimeMXBean.getInputArguments()).thenReturn(jvmArgs);

        // When: report is executed
        new ReportJvmArguments(logger).run();

        // Then: argument should be logged as-is (not censored because equalsIndex is not > 2)
        assertHasEvent(logger, " - -D=value");
    }

    @Test
    @DisplayName("should not censor JVM argument without equals sign (-Dvalue)")
    void shouldNotCensorJvmArgumentWithoutEqualsSign() {
        // Given: JVM argument without equals sign (not a valid key=value pair)
        setupManagedFactory();
        final List<String> jvmArgs = Arrays.asList("-Dvalue");
        when(mockRuntimeMXBean.getInputArguments()).thenReturn(jvmArgs);

        // When: report is executed
        new ReportJvmArguments(logger).run();

        // Then: argument should be logged as-is (not censored because equals not found)
        assertHasEvent(logger, " - -Dvalue");
    }

    @Test
    @DisplayName("should not censor JVM argument with only -D flag")
    void shouldNotCensorJvmArgumentWithOnlyDFlag() {
        // Given: JVM argument with only -D flag (not a valid key=value pair)
        setupManagedFactory();
        final List<String> jvmArgs = Arrays.asList("-D");
        when(mockRuntimeMXBean.getInputArguments()).thenReturn(jvmArgs);

        // When: report is executed
        new ReportJvmArguments(logger).run();

        // Then: argument should be logged as-is (not censored because equals not found)
        assertHasEvent(logger, " - -D");
    }

    @Test
    @DisplayName("should censor JVM argument with single-character key (-Dk=value)")
    void shouldCensorJvmArgumentWithSingleCharacterKey() {
        // Given: JVM argument with single-character key matching forbidden pattern
        setupManagedFactory();
        System.setProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX, "k"); // Single character pattern
        ReporterConfig.init();

        final List<String> jvmArgs = Arrays.asList("-Dk=secret");
        when(mockRuntimeMXBean.getInputArguments()).thenReturn(jvmArgs);

        // When: report is executed
        new ReportJvmArguments(logger).run();

        // Then: argument should be censored (equalsIndex=3 > 2, key matches forbidden pattern)
        assertHasEvent(logger, " - -Dk=********");
    }
}
