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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.utils.ConfigParser;
import org.usefultoys.test.CharsetConsistency;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.WithLocale;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({CharsetConsistency.class, ResetReporterConfig.class})
@WithLocale("en")
class ReportGarbageCollectorTest {

    private static final String TEST_LOGGER_NAME = "test.logger";
    private MockLogger mockLogger;
    private MockedStatic<ManagementFactory> mockedManagementFactory;

    @BeforeEach
    void setUp() {
        Logger testLogger = LoggerFactory.getLogger(TEST_LOGGER_NAME);
        mockLogger = (MockLogger) testLogger;
        mockLogger.clearEvents();
        mockLogger.setInfoEnabled(true); // Ensure INFO level is enabled

        // Mock ManagementFactory
        mockedManagementFactory = Mockito.mockStatic(ManagementFactory.class);
    }

    @AfterEach
    void tearDown() {
        mockedManagementFactory.close(); // Close the mock static
    }

    private String getLogOutput() {
        return mockLogger.getLoggerEvents().stream()
                .map(MockLoggerEvent::getFormattedMessage)
                .collect(Collectors.joining("\n"));
    }

    @Test
    void testGarbageCollectorsAreReported() {
        GarbageCollectorMXBean gc1 = mock(GarbageCollectorMXBean.class);
        when(gc1.getName()).thenReturn("G1 Young Generation");
        when(gc1.getCollectionCount()).thenReturn(100L);
        when(gc1.getCollectionTime()).thenReturn(5000L);
        when(gc1.getMemoryPoolNames()).thenReturn(new String[]{"G1 Eden Space", "G1 Survivor Space"});

        GarbageCollectorMXBean gc2 = mock(GarbageCollectorMXBean.class);
        when(gc2.getName()).thenReturn("G1 Old Generation");
        when(gc2.getCollectionCount()).thenReturn(10L);
        when(gc2.getCollectionTime()).thenReturn(15000L);
        when(gc2.getMemoryPoolNames()).thenReturn(new String[]{"G1 Old Gen"});

        when(ManagementFactory.getGarbageCollectorMXBeans()).thenReturn(Arrays.asList(gc1, gc2));

        new ReportGarbageCollector(mockLogger).run();

        String logOutput = getLogOutput();
        assertTrue(logOutput.contains("Garbage Collectors:"));
        assertTrue(logOutput.contains(" - Name: G1 Young Generation"));
        assertTrue(logOutput.contains("   Collection Count: 100"));
        assertTrue(logOutput.contains("   Collection Time: 5000 ms"));
        assertTrue(logOutput.contains("   Memory Pool Names: G1 Eden Space, G1 Survivor Space"));
        assertTrue(logOutput.contains(" - Name: G1 Old Generation"));
        assertTrue(logOutput.contains("   Collection Count: 10"));
        assertTrue(logOutput.contains("   Collection Time: 15000 ms"));
        assertTrue(logOutput.contains("   Memory Pool Names: G1 Old Gen"));
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testNoGarbageCollectorsFound() {
        when(ManagementFactory.getGarbageCollectorMXBeans()).thenReturn(Collections.emptyList());

        new ReportGarbageCollector(mockLogger).run();

        String logOutput = getLogOutput();
        assertTrue(logOutput.contains(" - No garbage collectors found."));
        assertTrue(ConfigParser.isInitializationOK());
    }
}
