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
import org.usefultoys.slf4jtestmock.MockLoggerExtension;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.test.CharsetConsistencyExtension;
import org.usefultoys.test.ResetReporterConfigExtension;
import org.usefultoys.test.WithLocale;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.usefultoys.slf4jtestmock.AssertLogger.assertHasEvent;

@ExtendWith({CharsetConsistencyExtension.class, ResetReporterConfigExtension.class, MockLoggerExtension.class})
@WithLocale("en")
class ReportJvmArgumentsTest {

    @Slf4jMock("test.report.jvm.arguments")
    private Logger logger;
    private MockedStatic<ManagementFactory> mockedManagementFactory;
    private RuntimeMXBean mockRuntimeMXBean;

    @AfterEach
    void tearDown() {
        if (mockedManagementFactory != null) {
            mockedManagementFactory.close(); // Close the mock static
        }
        System.clearProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX);
    }

    private void setupManagedFactory() {
        mockedManagementFactory = Mockito.mockStatic(ManagementFactory.class);
        mockRuntimeMXBean = mock(RuntimeMXBean.class);
        mockedManagementFactory.when(ManagementFactory::getRuntimeMXBean).thenReturn(mockRuntimeMXBean);
    }

    @Test
    void testJvmArgumentsAreReported() {
        setupManagedFactory();
        List<String> jvmArgs = Arrays.asList("-Xmx512m", "-Djava.awt.headless=true");
        when(mockRuntimeMXBean.getInputArguments()).thenReturn(jvmArgs);

        new ReportJvmArguments(logger).run();

        assertHasEvent(logger, "JVM Arguments:");
        assertHasEvent(logger, " - -Xmx512m");
        assertHasEvent(logger, " - -Djava.awt.headless=true");
    }

    @Test
    void testSensitiveJvmArgumentsAreCensoredWithDefaultRegex() {
        setupManagedFactory();
        List<String> jvmArgs = Arrays.asList(
                "-Xmx512m",
                "-Dmy.password=secret123",
                "-Ddb.key=dbkeyvalue",
                "-Dnormal.prop=normalvalue"
        );
        when(mockRuntimeMXBean.getInputArguments()).thenReturn(jvmArgs);

        new ReportJvmArguments(logger).run();

        assertHasEvent(logger, " - -Dmy.password=********");
        assertHasEvent(logger, " - -Ddb.key=********");
        assertHasEvent(logger, " - -Dnormal.prop=normalvalue");
    }

    @Test
    void testSensitiveJvmArgumentsAreCensoredWithCustomRegex() {
        setupManagedFactory();
        System.setProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX, "(?i).*custom.*");
        ReporterConfig.init(); // Reinitialize to apply custom regex

        List<String> jvmArgs = Arrays.asList(
                "-Dapp.custom.token=tokenvalue",
                "-Dapp.normal=normalvalue"
        );
        when(mockRuntimeMXBean.getInputArguments()).thenReturn(jvmArgs);

        new ReportJvmArguments(logger).run();

        assertHasEvent(logger, " - -Dapp.custom.token=********");
        assertHasEvent(logger, " - -Dapp.normal=normalvalue");
    }

    @Test
    void testNoJvmArgumentsFound() {
        setupManagedFactory();
        when(mockRuntimeMXBean.getInputArguments()).thenReturn(Arrays.asList());

        new ReportJvmArguments(logger).run();

        assertHasEvent(logger, " - No JVM arguments found.");
    }
}
