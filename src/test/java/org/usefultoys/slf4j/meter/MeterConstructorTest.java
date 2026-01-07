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
import org.usefultoys.slf4j.Session;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetMeterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.ValidateCleanMeter;
import org.usefultoys.test.WithLocale;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link Meter} constructors.
 * <p>
 * Tests validate that {@link Meter} constructors correctly initialize new {@link Meter} instances.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Meter Creation from Logger:</b> Verifies that meters can be created from Logger instances</li>
 *   <li><b>Meter Creation from Logger with Operation:</b> Verifies that operation names are properly set when provided (including null)</li>
 *   <li><b>Meter Creation from Logger with Operation and Parent:</b> Verifies that parent IDs are properly set when provided (including null)</li>
 *   <li><b>Logger Name Decoration:</b> Verifies prefixes/suffixes are applied to message/data logger names</li>
 *   <li><b>Position Counter Management:</b> Verifies position counter increments correctly and wraps from Long.MAX_VALUE to 1</li>
 * </ul>
 *
 * @author Co-authored-by: GitHub Copilot using Claude Sonnet 4.5
 */
@DisplayName("Meter Constructors")
@ValidateCharset
@ResetMeterConfig
@WithLocale("en")
@WithMockLogger
@ValidateCleanMeter
class MeterConstructorTest {

    private static final String TEST_CATEGORY = "test.category";
    private static final String TEST_OPERATION = "testOperation";
    private static final String TEST_PARENT = "parent-id";

    @Slf4jMock(TEST_CATEGORY)
    protected Logger testLogger;

    private static String positionKey(final String category, final String operation) {
        return operation == null ? category : category + "/" + operation;
    }

    private static void seedPositionCounter(final String category, final String operation, final long initialValue) {
        Meter.EVENT_COUNTER.put(positionKey(category, operation), new AtomicLong(initialValue));
    }

    private static void assertNewMeterDefaults(final Meter meter, final String expectedCategory, final String expectedOperation,
                                               final String expectedParent, final long expectedPosition,
                                               final long beforeNanos, final long afterNanos) {
        assertNotNull(meter, "should not create null Meter");

        assertNotNull(meter.getMessageLogger(), "should initialize message logger");
        assertNotNull(meter.getDataLogger(), "should initialize data logger");

        assertEquals(MeterConfig.messagePrefix + expectedCategory + MeterConfig.messageSuffix,
            meter.getMessageLogger().getName(),
            "should initialize message logger name from MeterConfig prefix/suffix and category");
        assertEquals(MeterConfig.dataPrefix + expectedCategory + MeterConfig.dataSuffix,
            meter.getDataLogger().getName(),
            "should initialize data logger name from MeterConfig prefix/suffix and category");

        assertEquals(expectedCategory, meter.getCategory(), "should initialize category");
        assertEquals(expectedOperation, meter.getOperation(), "should initialize operation");
        assertEquals(expectedParent, meter.getParent(), "should initialize parent");

        assertEquals(Session.shortSessionUuid(), meter.getSessionUuid(), "should initialize session UUID from Session.shortSessionUuid()");
        assertEquals(expectedPosition, meter.getPosition(), "should assign expected position");

        assertTrue(meter.getCreateTime() >= beforeNanos, "should set createTime at or after 'before' timestamp");
        assertTrue(meter.getCreateTime() <= afterNanos, "should set createTime at or before 'after' timestamp");
        assertEquals(meter.getCreateTime(), meter.getLastCurrentTime(), "should align lastCurrentTime with createTime on construction");

        assertNull(meter.getDescription(), "should leave description null");
        assertEquals(0L, meter.getStartTime(), "should leave startTime at zero before start()");
        assertEquals(0L, meter.getStopTime(), "should leave stopTime at zero before terminal call");
        assertEquals(0L, meter.getTimeLimit(), "should leave timeLimit at zero by default");
        assertEquals(0L, meter.getCurrentIteration(), "should leave currentIteration at zero by default");
        assertEquals(0L, meter.getExpectedIterations(), "should leave expectedIterations at zero by default");
        assertNull(meter.getOkPath(), "should leave okPath null by default");
        assertNull(meter.getRejectPath(), "should leave rejectPath null by default");
        assertNull(meter.getFailPath(), "should leave failPath null by default");
        assertNull(meter.getFailMessage(), "should leave failMessage null by default");
        assertTrue(meter.getContext().isEmpty(), "should expose empty context by default");

        assertEquals(0L, meter.getHeap_commited(), "should not collect heap_commited on construction");
        assertEquals(0L, meter.getHeap_max(), "should not collect heap_max on construction");
        assertEquals(0L, meter.getHeap_used(), "should not collect heap_used on construction");
        assertEquals(0L, meter.getNonHeap_commited(), "should not collect nonHeap_commited on construction");
        assertEquals(0L, meter.getNonHeap_max(), "should not collect nonHeap_max on construction");
        assertEquals(0L, meter.getNonHeap_used(), "should not collect nonHeap_used on construction");
        assertEquals(0L, meter.getObjectPendingFinalizationCount(), "should not collect objectPendingFinalizationCount on construction");
        assertEquals(0L, meter.getClassLoading_loaded(), "should not collect classLoading_loaded on construction");
        assertEquals(0L, meter.getClassLoading_total(), "should not collect classLoading_total on construction");
        assertEquals(0L, meter.getClassLoading_unloaded(), "should not collect classLoading_unloaded on construction");
        assertEquals(0L, meter.getCompilationTime(), "should not collect compilationTime on construction");
        assertEquals(0L, meter.getGarbageCollector_count(), "should not collect garbageCollector_count on construction");
        assertEquals(0L, meter.getGarbageCollector_time(), "should not collect garbageCollector_time on construction");
        assertEquals(0L, meter.getRuntime_usedMemory(), "should not collect runtime_usedMemory on construction");
        assertEquals(0L, meter.getRuntime_maxMemory(), "should not collect runtime_maxMemory on construction");
        assertEquals(0L, meter.getRuntime_totalMemory(), "should not collect runtime_totalMemory on construction");
        assertEquals(0.0, meter.getSystemLoad(), "should not collect systemLoad on construction");
    }

    @Nested
    @DisplayName("Meter(Logger)")
    class MeterFromLoggerTests {

        @Test
        @DisplayName("should create meter")
        void shouldCreateMeterFromLogger() {
            // Given: a mock logger representing the measurement category
            seedPositionCounter(TEST_CATEGORY, null, 0L);

            // When: a meter is created from that logger
            final long beforeNanos = System.nanoTime();
            final Meter meter = new Meter(testLogger);
            final long afterNanos = System.nanoTime();

            // Then: the meter category must match the logger name and initialization is correct
            assertNewMeterDefaults(meter, TEST_CATEGORY, null, null, 1L, beforeNanos, afterNanos);
        }

        @Test
        @DisplayName("should apply message/data logger prefixes and suffixes")
        void shouldApplyMessageAndDataLoggerPrefixesAndSuffixes() {
            // Given: configured prefixes and suffixes
            System.setProperty(MeterConfig.PROP_MESSAGE_PREFIX, "msg.");
            System.setProperty(MeterConfig.PROP_MESSAGE_SUFFIX, ".m");
            System.setProperty(MeterConfig.PROP_DATA_PREFIX, "data.");
            System.setProperty(MeterConfig.PROP_DATA_SUFFIX, ".d");
            MeterConfig.init();

            seedPositionCounter(TEST_CATEGORY, null, 0L);

            // When: a meter is created
            final long beforeNanos = System.nanoTime();
            final Meter meter = new Meter(testLogger);
            final long afterNanos = System.nanoTime();

            // Then: message and data loggers should reflect configured logger name decoration
            assertNewMeterDefaults(meter, TEST_CATEGORY, null, null, 1L, beforeNanos, afterNanos);
        }

        @Test
        @DisplayName("should generate consecutive positions for same logger category")
        void shouldGenerateConsecutivePositionsForSameLoggerCategory() {
            // Given: a logger

            // When: creating meters twice with the same parameterization
            final Meter first = new Meter(testLogger);
            final Meter second = new Meter(testLogger);

            // Then: positions should be consecutive (independent of the initial value)
            assertEquals(first.getPosition() + 1L, second.getPosition(), "should increment position on consecutive calls");
        }

        @Test
        @DisplayName("should wrap back to 1 after reaching Long.MAX_VALUE")
        void shouldWrapBackToOneAfterReachingLongMaxValue() {
            // Given: the per-key counter is seeded close to the maximum value
            seedPositionCounter(TEST_CATEGORY, null, Long.MAX_VALUE - 1);

            // When: creating meters repeatedly for the same key (logger category, no operation)
            final long beforeNanos1 = System.nanoTime();
            final Meter last = new Meter(testLogger);
            final long afterNanos1 = System.nanoTime();

            final long beforeNanos2 = System.nanoTime();
            final Meter wrapped = new Meter(testLogger);
            final long afterNanos2 = System.nanoTime();

            final long beforeNanos3 = System.nanoTime();
            final Meter afterWrapped = new Meter(testLogger);
            final long afterNanos3 = System.nanoTime();

            // Then: the sequence should reach MAX_VALUE and then wrap to 1 (not 0)
            assertNewMeterDefaults(last, TEST_CATEGORY, null, null, Long.MAX_VALUE, beforeNanos1, afterNanos1);
            assertNewMeterDefaults(wrapped, TEST_CATEGORY, null, null, 1L, beforeNanos2, afterNanos2);
            assertNewMeterDefaults(afterWrapped, TEST_CATEGORY, null, null, 2L, beforeNanos3, afterNanos3);
        }
    }

    @Nested
    @DisplayName("Meter(Logger, String)")
    class MeterFromLoggerAndOperationTests {

        @Test
        @DisplayName("should create meter with operation")
        void shouldCreateMeterFromLoggerAndOperation() {
            // Given: logger and operation name
            seedPositionCounter(TEST_CATEGORY, TEST_OPERATION, 0L);

            // When: meter is created from them
            final long beforeNanos = System.nanoTime();
            final Meter meter = new Meter(testLogger, TEST_OPERATION);
            final long afterNanos = System.nanoTime();

            // Then: the meter category is the logger name, operation matches and initialization is correct
            assertNewMeterDefaults(meter, TEST_CATEGORY, TEST_OPERATION, null, 1L, beforeNanos, afterNanos);
        }

        @Test
        @DisplayName("should create meter with null operation")
        void shouldCreateMeterFromLoggerWithNullOperation() {
            // Given: logger and null operation name
            seedPositionCounter(TEST_CATEGORY, null, 0L);

            // When: meter is created with a null operation
            final long beforeNanos = System.nanoTime();
            final Meter meter = new Meter(testLogger, null);
            final long afterNanos = System.nanoTime();

            // Then: operation should remain null and initialization is correct
            assertNewMeterDefaults(meter, TEST_CATEGORY, null, null, 1L, beforeNanos, afterNanos);
        }

        @Test
        @DisplayName("should generate consecutive positions for same category and operation")
        void shouldGenerateConsecutivePositionsForSameCategoryAndOperation() {
            // Given: a dedicated operation
            final String operation = TEST_OPERATION + ".sequence";

            // When: creating meters twice with the same category and operation
            final Meter first = new Meter(testLogger, operation);
            final Meter second = new Meter(testLogger, operation);

            // Then: positions should be consecutive
            assertEquals(first.getPosition() + 1L, second.getPosition(), "should increment position on consecutive calls");
        }

        @Test
        @DisplayName("should wrap back to 1 after reaching Long.MAX_VALUE")
        void shouldWrapBackToOneAfterReachingLongMaxValue() {
            // Given: the per-key counter is seeded close to the maximum value
            seedPositionCounter(TEST_CATEGORY, TEST_OPERATION, Long.MAX_VALUE - 1);

            // When: creating meters repeatedly for the same key (logger category and operation)
            final long beforeNanos1 = System.nanoTime();
            final Meter last = new Meter(testLogger, TEST_OPERATION);
            final long afterNanos1 = System.nanoTime();

            final long beforeNanos2 = System.nanoTime();
            final Meter wrapped = new Meter(testLogger, TEST_OPERATION);
            final long afterNanos2 = System.nanoTime();

            final long beforeNanos3 = System.nanoTime();
            final Meter afterWrapped = new Meter(testLogger, TEST_OPERATION);
            final long afterNanos3 = System.nanoTime();

            // Then: the sequence should reach MAX_VALUE and then wrap to 1 (not 0)
            assertNewMeterDefaults(last, TEST_CATEGORY, TEST_OPERATION, null, Long.MAX_VALUE, beforeNanos1, afterNanos1);
            assertNewMeterDefaults(wrapped, TEST_CATEGORY, TEST_OPERATION, null, 1L, beforeNanos2, afterNanos2);
            assertNewMeterDefaults(afterWrapped, TEST_CATEGORY, TEST_OPERATION, null, 2L, beforeNanos3, afterNanos3);
        }

        @Test
        @DisplayName("should apply message/data logger prefixes and suffixes")
        void shouldApplyMessageAndDataLoggerPrefixesAndSuffixes() {
            // Given: configured prefixes and suffixes
            System.setProperty(MeterConfig.PROP_MESSAGE_PREFIX, "msg.");
            System.setProperty(MeterConfig.PROP_MESSAGE_SUFFIX, ".m");
            System.setProperty(MeterConfig.PROP_DATA_PREFIX, "data.");
            System.setProperty(MeterConfig.PROP_DATA_SUFFIX, ".d");
            MeterConfig.init();

            seedPositionCounter(TEST_CATEGORY, TEST_OPERATION, 0L);

            // When: a meter is created
            final long beforeNanos = System.nanoTime();
            final Meter meter = new Meter(testLogger, TEST_OPERATION);
            final long afterNanos = System.nanoTime();

            // Then: message and data loggers should reflect configured logger name decoration
            assertNewMeterDefaults(meter, TEST_CATEGORY, TEST_OPERATION, null, 1L, beforeNanos, afterNanos);
        }
    }

    @Nested
    @DisplayName("Meter(Logger, String, String)")
    class MeterFromLoggerOperationAndParentTests {

        @Test
        @DisplayName("should create meter with operation and parent")
        void shouldCreateMeterFromLoggerOperationAndParent() {
            // Given: logger, operation name, and parent ID
            seedPositionCounter(TEST_CATEGORY, TEST_OPERATION, 0L);

            // When: meter is created with all parameters
            final long beforeNanos = System.nanoTime();
            final Meter meter = new Meter(testLogger, TEST_OPERATION, TEST_PARENT);
            final long afterNanos = System.nanoTime();

            // Then: the meter should initialize with all provided values
            assertNewMeterDefaults(meter, TEST_CATEGORY, TEST_OPERATION, TEST_PARENT, 1L, beforeNanos, afterNanos);
        }

        @Test
        @DisplayName("should create meter with null operation and parent")
        void shouldCreateMeterWithNullOperationAndParent() {
            // Given: logger with null operation but with parent ID
            seedPositionCounter(TEST_CATEGORY, null, 0L);

            // When: meter is created with null operation
            final long beforeNanos = System.nanoTime();
            final Meter meter = new Meter(testLogger, null, TEST_PARENT);
            final long afterNanos = System.nanoTime();

            // Then: operation should remain null while parent is set
            assertNewMeterDefaults(meter, TEST_CATEGORY, null, TEST_PARENT, 1L, beforeNanos, afterNanos);
        }

        @Test
        @DisplayName("should create meter with operation and null parent")
        void shouldCreateMeterWithOperationAndNullParent() {
            // Given: logger with operation but null parent
            seedPositionCounter(TEST_CATEGORY, TEST_OPERATION, 0L);

            // When: meter is created with null parent
            final long beforeNanos = System.nanoTime();
            final Meter meter = new Meter(testLogger, TEST_OPERATION, null);
            final long afterNanos = System.nanoTime();

            // Then: parent should remain null while operation is set
            assertNewMeterDefaults(meter, TEST_CATEGORY, TEST_OPERATION, null, 1L, beforeNanos, afterNanos);
        }

        @Test
        @DisplayName("should create meter with null operation and null parent")
        void shouldCreateMeterWithNullOperationAndNullParent() {
            // Given: logger with both null operation and null parent
            seedPositionCounter(TEST_CATEGORY, null, 0L);

            // When: meter is created with both parameters null
            final long beforeNanos = System.nanoTime();
            final Meter meter = new Meter(testLogger, null, null);
            final long afterNanos = System.nanoTime();

            // Then: both operation and parent should remain null
            assertNewMeterDefaults(meter, TEST_CATEGORY, null, null, 1L, beforeNanos, afterNanos);
        }

        @Test
        @DisplayName("should apply message/data logger prefixes and suffixes")
        void shouldApplyMessageAndDataLoggerPrefixesAndSuffixes() {
            // Given: configured prefixes and suffixes
            System.setProperty(MeterConfig.PROP_MESSAGE_PREFIX, "msg.");
            System.setProperty(MeterConfig.PROP_MESSAGE_SUFFIX, ".m");
            System.setProperty(MeterConfig.PROP_DATA_PREFIX, "data.");
            System.setProperty(MeterConfig.PROP_DATA_SUFFIX, ".d");
            MeterConfig.init();

            seedPositionCounter(TEST_CATEGORY, TEST_OPERATION, 0L);

            // When: a meter is created
            final long beforeNanos = System.nanoTime();
            final Meter meter = new Meter(testLogger, TEST_OPERATION, TEST_PARENT);
            final long afterNanos = System.nanoTime();

            // Then: message and data loggers should reflect configured logger name decoration
            assertNewMeterDefaults(meter, TEST_CATEGORY, TEST_OPERATION, TEST_PARENT, 1L, beforeNanos, afterNanos);
        }

        @Test
        @DisplayName("should generate consecutive positions for same category and operation")
        void shouldGenerateConsecutivePositionsForSameCategoryAndOperation() {
            // Given: logger, operation, and parent

            // When: creating meters twice with the same parameterization
            final Meter first = new Meter(testLogger, TEST_OPERATION, TEST_PARENT);
            final Meter second = new Meter(testLogger, TEST_OPERATION, TEST_PARENT);

            // Then: positions should be consecutive (independent of the initial value)
            assertEquals(first.getPosition() + 1L, second.getPosition(), "should increment position on consecutive calls");
        }

        @Test
        @DisplayName("should wrap back to 1 after reaching Long.MAX_VALUE")
        void shouldWrapBackToOneAfterReachingLongMaxValue() {
            // Given: the per-key counter is seeded close to the maximum value
            seedPositionCounter(TEST_CATEGORY, TEST_OPERATION, Long.MAX_VALUE - 1);

            // When: creating meters repeatedly for the same key (logger category and operation)
            final long beforeNanos1 = System.nanoTime();
            final Meter last = new Meter(testLogger, TEST_OPERATION, TEST_PARENT);
            final long afterNanos1 = System.nanoTime();

            final long beforeNanos2 = System.nanoTime();
            final Meter wrapped = new Meter(testLogger, TEST_OPERATION, TEST_PARENT);
            final long afterNanos2 = System.nanoTime();

            final long beforeNanos3 = System.nanoTime();
            final Meter afterWrapped = new Meter(testLogger, TEST_OPERATION, TEST_PARENT);
            final long afterNanos3 = System.nanoTime();

            // Then: the sequence should reach MAX_VALUE and then wrap to 1 (not 0)
            assertNewMeterDefaults(last, TEST_CATEGORY, TEST_OPERATION, TEST_PARENT, Long.MAX_VALUE, beforeNanos1, afterNanos1);
            assertNewMeterDefaults(wrapped, TEST_CATEGORY, TEST_OPERATION, TEST_PARENT, 1L, beforeNanos2, afterNanos2);
            assertNewMeterDefaults(afterWrapped, TEST_CATEGORY, TEST_OPERATION, TEST_PARENT, 2L, beforeNanos3, afterNanos3);
        }
    }
}
