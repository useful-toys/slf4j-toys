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
import org.usefultoys.slf4j.utils.ConfigParser;
import org.usefultoys.slf4jtestmock.MockLoggerExtension;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.test.CharsetConsistencyExtension;
import org.usefultoys.test.ResetReporterConfigExtension;
import org.usefultoys.test.WithLocale;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.usefultoys.slf4jtestmock.AssertLogger.assertHasEvent;

@ExtendWith({CharsetConsistencyExtension.class, ResetReporterConfigExtension.class, MockLoggerExtension.class})
@WithLocale("en")
class ReportGarbageCollectorTest {

    @Slf4jMock("test.report.garbagecollector")
    private Logger logger;

    private MockedStatic<ManagementFactory> mockedManagementFactory;

    @BeforeEach
    void setUp() {
        // Mock ManagementFactory
        mockedManagementFactory = Mockito.mockStatic(ManagementFactory.class);
    }

    @AfterEach
    void tearDown() {
        mockedManagementFactory.close(); // Close the mock static
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

        new ReportGarbageCollector(logger).run();

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
    void testNoGarbageCollectorsFound() {
        when(ManagementFactory.getGarbageCollectorMXBeans()).thenReturn(Collections.emptyList());

        new ReportGarbageCollector(logger).run();

        assertHasEvent(logger, " - No garbage collectors found.");
        assertTrue(ConfigParser.isInitializationOK());
    }
}
