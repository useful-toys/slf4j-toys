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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.usefultoys.slf4j.utils.ConfigParser;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.usefultoys.slf4jtestmock.AssertLogger.assertHasEvent;

/**
 * Unit tests for {@link ReportGarbageCollector}.
 * <p>
 * Tests verify that ReportGarbageCollector correctly reports garbage collector information
 * including names, collection counts, times, and memory pool names.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Garbage Collector Details:</b> Verifies logging of garbage collector names, collection counts, collection times, and associated memory pool names</li>
 *   <li><b>Multiple Collectors:</b> Tests handling of multiple garbage collectors, ensuring each collector's information is properly reported</li>
 *   <li><b>No Collectors Scenario:</b> Validates behavior when no garbage collectors are available, logging appropriate "no collectors found" message</li>
 *   <li><b>Memory Pool Names:</b> Ensures memory pool names associated with each garbage collector are correctly listed and formatted</li>
 * </ul>
 */
@DisplayName("ReportGarbageCollector")
@ValidateCharset
@ResetReporterConfig
@WithLocale("en")
@WithMockLogger
class ReportGarbageCollectorTest {

    @Slf4jMock
    private Logger logger;

    private MockedStatic<ManagementFactory> mockedManagementFactory;

    @BeforeEach
    void setUp() {
        // Mock ManagementFactory
        mockedManagementFactory = Mockito.mockStatic(ManagementFactory.class);
    }

    @AfterEach
    void tearDown() {
        mockedManagementFactory.close();
    }


    @Test
    @DisplayName("should report garbage collectors information")
    void shouldReportGarbageCollectors() {
        // Given: two garbage collectors configured with collection statistics
        final GarbageCollectorMXBean gc1 = mock(GarbageCollectorMXBean.class);
        when(gc1.getName()).thenReturn("G1 Young Generation");
        when(gc1.getCollectionCount()).thenReturn(100L);
        when(gc1.getCollectionTime()).thenReturn(5000L);
        when(gc1.getMemoryPoolNames()).thenReturn(new String[]{"G1 Eden Space", "G1 Survivor Space"});

        final GarbageCollectorMXBean gc2 = mock(GarbageCollectorMXBean.class);
        when(gc2.getName()).thenReturn("G1 Old Generation");
        when(gc2.getCollectionCount()).thenReturn(10L);
        when(gc2.getCollectionTime()).thenReturn(15000L);
        when(gc2.getMemoryPoolNames()).thenReturn(new String[]{"G1 Old Gen"});

        when(ManagementFactory.getGarbageCollectorMXBeans()).thenReturn(Arrays.asList(gc1, gc2));

        // When: report is executed
        new ReportGarbageCollector(logger).run();

        // Then: should log garbage collector details
        assertHasEvent(logger, "Garbage Collectors:");
        assertHasEvent(logger, " - Name: G1 Young Generation");
        assertHasEvent(logger, "   Collection Count: 100");
        assertHasEvent(logger, "   Collection Time: 5000 ms");
        assertHasEvent(logger, "   Memory Pool Names: G1 Eden Space, G1 Survivor Space");
        assertHasEvent(logger, " - Name: G1 Old Generation");
        assertHasEvent(logger, "   Collection Count: 10");
        assertHasEvent(logger, "   Collection Time: 15000 ms");
        assertHasEvent(logger, "   Memory Pool Names: G1 Old Gen");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    @DisplayName("should report when no garbage collectors found")
    void shouldReportNoGarbageCollectors() {
        // Given: no garbage collectors available
        when(ManagementFactory.getGarbageCollectorMXBeans()).thenReturn(Collections.emptyList());

        // When: report is executed
        new ReportGarbageCollector(logger).run();

        // Then: should log that no garbage collectors were found
        assertHasEvent(logger, " - No garbage collectors found.");
        assertTrue(ConfigParser.isInitializationOK());
    }
}
