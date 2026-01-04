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
package org.usefultoys.slf4j.meter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetMeterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for {@link MeterFactory}.
 * <p>
 * Tests validate that MeterFactory correctly creates Meter instances from various sources
 * and manages the current meter context in multi-threaded environments.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Meter Creation from Logger:</b> Verifies that Meter can be created from Logger instances</li>
 *   <li><b>Meter Creation from Category:</b> Verifies that Meter can be created from category strings</li>
 *   <li><b>Meter Creation from Class:</b> Verifies that Meter can be created from Class references</li>
 *   <li><b>Meter Creation with Operation:</b> Verifies that operation names are properly set when provided</li>
 *   <li><b>Current Meter Tracking:</b> Verifies that started Meters are tracked and accessible via getCurrentMeter()</li>
 *   <li><b>Fallback Meter Behavior:</b> Verifies that fallback Meter is returned when no active Meter exists</li>
 *   <li><b>Sub-Meter Creation:</b> Verifies that sub-Meters are created with concatenated operation names</li>
 *   <li><b>Multiple Concurrent Sub-Meters:</b> Verifies that multiple sub-Meters can coexist with correct operation hierarchy</li>
 * </ul>
 */
@DisplayName("MeterFactory")
@ValidateCharset
@ResetMeterConfig
@WithLocale("en")
@WithMockLogger
class MeterFactoryTest {

    private static final String TEST_CATEGORY = "test.category";
    private static final String TEST_OPERATION = "testOperation";
    private static final String TEST_SUBOPERATION = "testSubOperation";

    @Slf4jMock(TEST_CATEGORY)
    protected Logger testLogger;

    @Nested
    @DisplayName("Meter Creation Tests")
    class MeterCreationTests {

        @Test
        @DisplayName("should create meter from logger")
        void shouldCreateMeterFromLogger() {
            // Given: a mock logger representing the measurement category
            // When: a meter is created from that logger
            final Meter meter = MeterFactory.getMeter(testLogger);

            // Then: the meter category must match the logger name and no operation is set
            assertNotNull(meter, "The Meter should not be null");
            assertEquals(TEST_CATEGORY, meter.getCategory(), "The Meter's category should match the logger name");
            assertNull(meter.getOperation(), "The operation name should be null when not specified");
        }

        @Test
        @DisplayName("should create meter from category")
        void shouldCreateMeterFromCategory() {
            // Given: a category string
            // When: a meter is created from the category
            final Meter meter = MeterFactory.getMeter(TEST_CATEGORY);

            // Then: the meter category should match the supplied category and operation remains null
            assertNotNull(meter, "The Meter should not be null");
            assertEquals(TEST_CATEGORY, meter.getCategory(), "The Meter's category should match the specified category");
            assertNull(meter.getOperation(), "The operation name should be null when not specified");
        }

        @Test
        @DisplayName("should create meter from class")
        void shouldCreateMeterFromClass() {
            // Given: a class reference
            // When: a meter is created from the class
            final Meter meter = MeterFactory.getMeter(MeterFactoryTest.class);

            // Then: the meter category should be the class name
            assertNotNull(meter, "The Meter should not be null");
            assertEquals(MeterFactoryTest.class.getName(), meter.getCategory(), "The Meter's category should match the class name");
            assertNull(meter.getOperation(), "The operation name should be null when not specified");
        }

        @Test
        @DisplayName("should create meter from class and operation")
        void shouldCreateMeterFromClassAndOperation() {
            // Given: class and operation name
            // When: meter is created with the specified operation
            final Meter meter = MeterFactory.getMeter(MeterFactoryTest.class, TEST_OPERATION);

            // Then: the meter carries the class name and operation
            assertNotNull(meter, "The Meter should not be null");
            assertEquals(MeterFactoryTest.class.getName(), meter.getCategory(), "The Meter's category should match the class name");
            assertEquals(TEST_OPERATION, meter.getOperation(), "The operation name should match the specified name");
        }

        @Test
        @DisplayName("should create meter from logger and operation")
        void shouldCreateMeterFromLoggerAndOperation() {
            // Given: logger and operation name
            // When: meter is created from them
            final Meter meter = MeterFactory.getMeter(testLogger, TEST_OPERATION);

            // Then: the meter category is the logger name and operation matches
            assertNotNull(meter, "The Meter should not be null");
            assertEquals(TEST_CATEGORY, meter.getCategory(), "The Meter's category should match the logger name");
            assertEquals(TEST_OPERATION, meter.getOperation(), "The operation name should match the specified name");
        }
    }

    @Nested
    @DisplayName("Current Meter Tracking Tests")
    class CurrentMeterTrackingTests {

        @Test
        @DisplayName("should report current meter after start")
        void shouldReportCurrentMeterAfterStart() {
            // Given: a meter that was started
            final Meter originalMeter = MeterFactory.getMeter(testLogger, TEST_OPERATION);
            originalMeter.start();

            // When: current meter is queried
            final Meter currentMeter = MeterFactory.getCurrentMeter();

            // Then: it should be the same meter that was started
            assertNotNull(currentMeter, "The current Meter should not be null");
            assertSame(originalMeter, currentMeter, "The current Meter should be the same as the last started one");
            originalMeter.ok();
        }
    }

    @Nested
    @DisplayName("Fallback Meter Tests")
    class FallbackMeterTests {

        @Test
        @DisplayName("should return fallback meter when none started")
        void shouldReturnFallbackMeterWhenNoneStarted() {
            // Given: (no active meter)
            // When: current meter is requested without prior start
            final Meter currentMeter = MeterFactory.getCurrentMeter();

            // Then: fallback meter is provided
            assertNotNull(currentMeter, "The Meter should not be null");
            assertEquals("???", currentMeter.getCategory(), "The Meter's category should fall back to the logger placeholder");
            assertNull(currentMeter.getOperation(), "The operation name should be null when not specified");
        }

        @Test
        @DisplayName("should create fallback sub meter when none started")
        void shouldCreateFallbackSubMeterWhenNoneStarted() {
            // Given: (no active meter)
            // When: sub meter is created without active meter
            final Meter subMeter = MeterFactory.getCurrentSubMeter(TEST_SUBOPERATION);

            // Then: fallback category and sub operation are returned
            assertNotNull(subMeter, "The sub-Meter should not be null");
            assertEquals("???", subMeter.getCategory(), "The sub-Meter's category should match the fallback category");
            assertEquals(TEST_SUBOPERATION, subMeter.getOperation(), "The sub-Meter's operation name should match the requested sub-operation");
        }
    }

    @Nested
    @DisplayName("Sub-Meter Tests")
    class SubMeterTests {

        @Test
        @DisplayName("should create sub meter from current meter")
        void shouldCreateSubMeterFromCurrentMeter() {
            // Given: started meter
            final Meter originalMeter = MeterFactory.getMeter(testLogger, TEST_OPERATION);
            originalMeter.start();

            // When: a sub meter is created
            final Meter subMeter = MeterFactory.getCurrentSubMeter(TEST_SUBOPERATION);

            // Then: sub-meter category matches original and operation concatenates
            assertNotNull(subMeter, "The sub-Meter should not be null");
            assertEquals(TEST_CATEGORY, subMeter.getCategory(), "The sub-Meter's category should match the original Meter's category");
            assertEquals(TEST_OPERATION + "/" + TEST_SUBOPERATION, subMeter.getOperation(), "The sub-Meter's operation name should combine the original operation and the sub-name");
            originalMeter.ok();
        }

        @Test
        @DisplayName("should allow multiple concurrent sub meters")
        void shouldAllowMultipleConcurrentSubMeters() {
            // Given: one parent meter
            final Meter originalMeter = MeterFactory.getMeter(testLogger, TEST_OPERATION);
            originalMeter.start();

            // When: multiple sub-meters are created
            final Meter subMeter1 = MeterFactory.getCurrentSubMeter("sub1");
            final Meter subMeter2 = MeterFactory.getCurrentSubMeter("sub2");

            // Then: each sub-meter reports distinct operations and the most recent start becomes current
            assertNotNull(subMeter1, "The first sub-Meter should not be null");
            assertNotNull(subMeter2, "The second sub-Meter should not be null");
            assertEquals(TEST_OPERATION + "/sub1", subMeter1.getOperation(), "The first sub-Meter's operation name should be correct");
            assertEquals(TEST_OPERATION + "/sub2", subMeter2.getOperation(), "The second sub-Meter's operation name should be correct");

            subMeter1.start();
            assertSame(subMeter1, MeterFactory.getCurrentMeter(), "The current Meter should be the most recently started sub-Meter");
            subMeter1.ok();
            originalMeter.ok();
        }
    }
}
