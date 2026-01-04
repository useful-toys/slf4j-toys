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
import org.usefultoys.test.WithLocale;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link MeterFactory}.
 * <p>
 * Tests validate that {@link MeterFactory} correctly creates new {@link Meter} instances via its
 * `getMeter(...)` shortcut methods.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Meter Creation from Logger:</b> Verifies that meters can be created from Logger instances</li>
 *   <li><b>Meter Creation from Category:</b> Verifies that meters can be created from category strings</li>
 *   <li><b>Meter Creation from Class:</b> Verifies that meters can be created from Class references</li>
 *   <li><b>Meter Creation with Operation:</b> Verifies that operation names are properly set when provided (including null)</li>
 *   <li><b>Logger Name Decoration:</b> Verifies prefixes/suffixes are applied to message/data logger names</li>
 *   <li><b>Current Meter Access:</b> Verifies current meter and current sub-meter shortcuts</li>
 * </ul>
 *
 * @author Co-authored-by: GitHub Copilot using GPT-5.2
 */
@DisplayName("MeterFactory")
@ValidateCharset
@ResetMeterConfig
@WithLocale("en")
@WithMockLogger
class MeterFactoryTest {

    private static final String TEST_CATEGORY = "test.category";
    private static final String TEST_OPERATION = "testOperation";

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
    @DisplayName("getMeter(Logger)")
    class GetMeterFromLoggerTests {

        @Test
        @DisplayName("should create meter")
        void shouldCreateMeterFromLogger() {
            // Given: a mock logger representing the measurement category
            seedPositionCounter(TEST_CATEGORY, null, 0L);

            // When: a meter is created from that logger
            final long beforeNanos = System.nanoTime();
            final Meter meter = MeterFactory.getMeter(testLogger);
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
            final Meter meter = MeterFactory.getMeter(testLogger);
            final long afterNanos = System.nanoTime();

            // Then: message and data loggers should reflect configured logger name decoration
            assertNewMeterDefaults(meter, TEST_CATEGORY, null, null, 1L, beforeNanos, afterNanos);
        }

        @Test
        @DisplayName("should generate consecutive positions for same logger category")
        void shouldGenerateConsecutivePositionsForSameLoggerCategory() {
            // Given: a logger

            // When: creating meters twice with the same parameterization
            final Meter first = MeterFactory.getMeter(testLogger);
            final Meter second = MeterFactory.getMeter(testLogger);

            // Then: positions should be consecutive (independent of the initial value)
            assertEquals(first.getPosition() + 1L, second.getPosition(), "should increment position on consecutive calls");
        }

        @Test
        @DisplayName("should wrap back to 1 after reaching Long.MAX_VALUE")
        void shouldWrapBackToOneAfterReachingLongMaxValue() {
            // Given: the per-key counter is seeded close to the maximum value
            seedPositionCounter(TEST_CATEGORY, null, Long.MAX_VALUE - 1);

            // When: creating meters repeatedly for the same key (logger category, no operation)
            final Meter last = MeterFactory.getMeter(testLogger);
            final Meter wrapped = MeterFactory.getMeter(testLogger);
            final Meter afterWrapped = MeterFactory.getMeter(testLogger);

            // Then: the sequence should reach MAX_VALUE and then wrap to 1 (not 0)
            assertEquals(Long.MAX_VALUE, last.getPosition(), "should reach Long.MAX_VALUE before wrapping");
            assertEquals(1L, wrapped.getPosition(), "should wrap back to 1 after reaching Long.MAX_VALUE");
            assertEquals(2L, afterWrapped.getPosition(), "should keep incrementing after wrap");
        }
    }

    @Nested
    @DisplayName("getMeter(String)")
    class GetMeterFromCategoryTests {

        @Test
        @DisplayName("should create meter")
        void shouldCreateMeterFromCategory() {
            // Given: a category string
            seedPositionCounter(TEST_CATEGORY, null, 0L);

            // When: a meter is created from the category
            final long beforeNanos = System.nanoTime();
            final Meter meter = MeterFactory.getMeter(TEST_CATEGORY);
            final long afterNanos = System.nanoTime();

            // Then: the meter category should match the supplied category and initialization is correct
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
            final Meter meter = MeterFactory.getMeter(TEST_CATEGORY);
            final long afterNanos = System.nanoTime();

            // Then: message and data loggers should reflect configured logger name decoration
            assertNewMeterDefaults(meter, TEST_CATEGORY, null, null, 1L, beforeNanos, afterNanos);
        }

        @Test
        @DisplayName("should generate consecutive positions for same category")
        void shouldGenerateConsecutivePositionsForSameCategory() {
            // Given: a dedicated category key
            final String category = TEST_CATEGORY + ".sequence";

            // When: creating meters twice with the same parameterization
            final Meter first = MeterFactory.getMeter(category);
            final Meter second = MeterFactory.getMeter(category);

            // Then: positions should be consecutive
            assertEquals(first.getPosition() + 1L, second.getPosition(), "should increment position on consecutive calls");
        }

        @Test
        @DisplayName("should wrap back to 1 after reaching Long.MAX_VALUE")
        void shouldWrapBackToOneAfterReachingLongMaxValue() {
            // Given: the per-key counter is seeded close to the maximum value
            final String category = TEST_CATEGORY + ".overflow";
            seedPositionCounter(category, null, Long.MAX_VALUE - 1);

            // When: creating meters repeatedly for the same key (category, no operation)
            final Meter last = MeterFactory.getMeter(category);
            final Meter wrapped = MeterFactory.getMeter(category);
            final Meter afterWrapped = MeterFactory.getMeter(category);

            // Then: the sequence should reach MAX_VALUE and then wrap to 1 (not 0)
            assertEquals(Long.MAX_VALUE, last.getPosition(), "should reach Long.MAX_VALUE before wrapping");
            assertEquals(1L, wrapped.getPosition(), "should wrap back to 1 after reaching Long.MAX_VALUE");
            assertEquals(2L, afterWrapped.getPosition(), "should keep incrementing after wrap");
        }
    }

    @Nested
    @DisplayName("getMeter(Class)")
    class GetMeterFromClassTests {

        @Test
        @DisplayName("should create meter")
        void shouldCreateMeterFromClass() {
            // Given: a class reference
            seedPositionCounter(MeterFactoryTest.class.getName(), null, 0L);

            // When: a meter is created from the class
            final long beforeNanos = System.nanoTime();
            final Meter meter = MeterFactory.getMeter(MeterFactoryTest.class);
            final long afterNanos = System.nanoTime();

            // Then: the meter category should be the class name and initialization is correct
            assertNewMeterDefaults(meter, MeterFactoryTest.class.getName(), null, null, 1L, beforeNanos, afterNanos);
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

            seedPositionCounter(MeterFactoryTest.class.getName(), null, 0L);

            // When: a meter is created
            final long beforeNanos = System.nanoTime();
            final Meter meter = MeterFactory.getMeter(MeterFactoryTest.class);
            final long afterNanos = System.nanoTime();

            // Then: message and data loggers should reflect configured logger name decoration
            assertNewMeterDefaults(meter, MeterFactoryTest.class.getName(), null, null, 1L, beforeNanos, afterNanos);
        }

        @Test
        @DisplayName("should generate consecutive positions for same class category")
        void shouldGenerateConsecutivePositionsForSameClassCategory() {
            // Given: a class reference

            // When: creating meters twice with the same parameterization
            final Meter first = MeterFactory.getMeter(MeterFactoryTest.class);
            final Meter second = MeterFactory.getMeter(MeterFactoryTest.class);

            // Then: positions should be consecutive (independent of the initial value)
            assertEquals(first.getPosition() + 1L, second.getPosition(), "should increment position on consecutive calls");
        }

        @Test
        @DisplayName("should wrap back to 1 after reaching Long.MAX_VALUE")
        void shouldWrapBackToOneAfterReachingLongMaxValue() {
            // Given: the per-key counter is seeded close to the maximum value
            seedPositionCounter(MeterFactoryTest.class.getName(), null, Long.MAX_VALUE - 1);

            // When: creating meters repeatedly for the same key (class category, no operation)
            final Meter last = MeterFactory.getMeter(MeterFactoryTest.class);
            final Meter wrapped = MeterFactory.getMeter(MeterFactoryTest.class);
            final Meter afterWrapped = MeterFactory.getMeter(MeterFactoryTest.class);

            // Then: the sequence should reach MAX_VALUE and then wrap to 1 (not 0)
            assertEquals(Long.MAX_VALUE, last.getPosition(), "should reach Long.MAX_VALUE before wrapping");
            assertEquals(1L, wrapped.getPosition(), "should wrap back to 1 after reaching Long.MAX_VALUE");
            assertEquals(2L, afterWrapped.getPosition(), "should keep incrementing after wrap");
        }
    }

    @Nested
    @DisplayName("getMeter(Class, String)")
    class GetMeterFromClassAndOperationTests {

        @Test
        @DisplayName("should create meter with operation")
        void shouldCreateMeterFromClassAndOperation() {
            // Given: class and operation name
            seedPositionCounter(MeterFactoryTest.class.getName(), TEST_OPERATION, 0L);

            // When: meter is created with the specified operation
            final long beforeNanos = System.nanoTime();
            final Meter meter = MeterFactory.getMeter(MeterFactoryTest.class, TEST_OPERATION);
            final long afterNanos = System.nanoTime();

            // Then: the meter carries the class name, operation and initialization is correct
            assertNewMeterDefaults(meter, MeterFactoryTest.class.getName(), TEST_OPERATION, null, 1L, beforeNanos, afterNanos);
        }

        @Test
        @DisplayName("should create meter with null operation")
        void shouldCreateMeterFromClassWithNullOperation() {
            // Given: class reference and null operation
            seedPositionCounter(MeterFactoryTest.class.getName(), null, 0L);

            // When: meter is created with a null operation
            final long beforeNanos = System.nanoTime();
            final Meter meter = MeterFactory.getMeter(MeterFactoryTest.class, null);
            final long afterNanos = System.nanoTime();

            // Then: the meter should behave like a category-only meter
            assertNewMeterDefaults(meter, MeterFactoryTest.class.getName(), null, null, 1L, beforeNanos, afterNanos);
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

            seedPositionCounter(MeterFactoryTest.class.getName(), TEST_OPERATION, 0L);

            // When: a meter is created
            final long beforeNanos = System.nanoTime();
            final Meter meter = MeterFactory.getMeter(MeterFactoryTest.class, TEST_OPERATION);
            final long afterNanos = System.nanoTime();

            // Then: message and data loggers should reflect configured logger name decoration
            assertNewMeterDefaults(meter, MeterFactoryTest.class.getName(), TEST_OPERATION, null, 1L, beforeNanos, afterNanos);
        }

        @Test
        @DisplayName("should generate consecutive positions for same class category and operation")
        void shouldGenerateConsecutivePositionsForSameClassCategoryAndOperation() {
            // Given: a class reference and operation

            // When: creating meters twice with the same parameterization
            final Meter first = MeterFactory.getMeter(MeterFactoryTest.class, TEST_OPERATION);
            final Meter second = MeterFactory.getMeter(MeterFactoryTest.class, TEST_OPERATION);

            // Then: positions should be consecutive (independent of the initial value)
            assertEquals(first.getPosition() + 1L, second.getPosition(), "should increment position on consecutive calls");
        }

        @Test
        @DisplayName("should wrap back to 1 after reaching Long.MAX_VALUE")
        void shouldWrapBackToOneAfterReachingLongMaxValue() {
            // Given: the per-key counter is seeded close to the maximum value
            seedPositionCounter(MeterFactoryTest.class.getName(), TEST_OPERATION, Long.MAX_VALUE - 1);

            // When: creating meters repeatedly for the same key (class category and operation)
            final Meter last = MeterFactory.getMeter(MeterFactoryTest.class, TEST_OPERATION);
            final Meter wrapped = MeterFactory.getMeter(MeterFactoryTest.class, TEST_OPERATION);
            final Meter afterWrapped = MeterFactory.getMeter(MeterFactoryTest.class, TEST_OPERATION);

            // Then: the sequence should reach MAX_VALUE and then wrap to 1 (not 0)
            assertEquals(Long.MAX_VALUE, last.getPosition(), "should reach Long.MAX_VALUE before wrapping");
            assertEquals(1L, wrapped.getPosition(), "should wrap back to 1 after reaching Long.MAX_VALUE");
            assertEquals(2L, afterWrapped.getPosition(), "should keep incrementing after wrap");
        }
    }

    @Nested
    @DisplayName("getMeter(Logger, String)")
    class GetMeterFromLoggerAndOperationTests {

        @Test
        @DisplayName("should create meter with operation")
        void shouldCreateMeterFromLoggerAndOperation() {
            // Given: logger and operation name
            seedPositionCounter(TEST_CATEGORY, TEST_OPERATION, 0L);

            // When: meter is created from them
            final long beforeNanos = System.nanoTime();
            final Meter meter = MeterFactory.getMeter(testLogger, TEST_OPERATION);
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
            final Meter meter = MeterFactory.getMeter(testLogger, null);
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
            final Meter first = MeterFactory.getMeter(testLogger, operation);
            final Meter second = MeterFactory.getMeter(testLogger, operation);

            // Then: positions should be consecutive
            assertEquals(first.getPosition() + 1L, second.getPosition(), "should increment position on consecutive calls");
        }

        @Test
        @DisplayName("should wrap back to 1 after reaching Long.MAX_VALUE")
        void shouldWrapBackToOneAfterReachingLongMaxValue() {
            // Given: the per-key counter is seeded close to the maximum value
            seedPositionCounter(TEST_CATEGORY, TEST_OPERATION, Long.MAX_VALUE - 1);

            // When: creating meters repeatedly for the same key (logger category and operation)
            final Meter last = MeterFactory.getMeter(testLogger, TEST_OPERATION);
            final Meter wrapped = MeterFactory.getMeter(testLogger, TEST_OPERATION);
            final Meter afterWrapped = MeterFactory.getMeter(testLogger, TEST_OPERATION);

            // Then: the sequence should reach MAX_VALUE and then wrap to 1 (not 0)
            assertEquals(Long.MAX_VALUE, last.getPosition(), "should reach Long.MAX_VALUE before wrapping");
            assertEquals(1L, wrapped.getPosition(), "should wrap back to 1 after reaching Long.MAX_VALUE");
            assertEquals(2L, afterWrapped.getPosition(), "should keep incrementing after wrap");
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
            final Meter meter = MeterFactory.getMeter(testLogger, TEST_OPERATION);
            final long afterNanos = System.nanoTime();

            // Then: message and data loggers should reflect configured logger name decoration
            assertNewMeterDefaults(meter, TEST_CATEGORY, TEST_OPERATION, null, 1L, beforeNanos, afterNanos);
        }
    }

    @Nested
    @DisplayName("getCurrentMeter()")
    class GetCurrentMeterTests {

        @Test
        @DisplayName("should return fallback meter when none started")
        void shouldReturnFallbackMeterWhenNoneStarted() {
            // Given: no active meter on the current thread
            // When: current meter is requested
            final Meter currentMeter = MeterFactory.getCurrentMeter();

            // Then: a fallback meter is returned
            assertNotNull(currentMeter, "should return non-null fallback meter");
            assertEquals(Meter.UNKNOWN_LOGGER_NAME, currentMeter.getCategory(), "should use placeholder category for fallback meter");
            assertNull(currentMeter.getOperation(), "should keep operation null for fallback meter");
            assertNotNull(currentMeter.getMessageLogger(), "should initialize fallback message logger");
            assertNotNull(currentMeter.getDataLogger(), "should initialize fallback data logger");
        }

        @Test
        @DisplayName("should return same meter after start")
        void shouldReturnSameMeterAfterStart() {
            // Given: a meter that was started
            final Meter meter = new Meter(testLogger, TEST_OPERATION);
            meter.start();

            // When: current meter is requested
            final Meter current = MeterFactory.getCurrentMeter();

            // Then: it should be the same started meter
            assertSame(meter, current, "should return the most recently started meter");
            meter.ok();
        }

        @Test
        @DisplayName("should restore previous meter after nested meter ends")
        void shouldRestorePreviousMeterAfterNestedMeterEnds() {
            // Given: two nested started meters
            final Meter outer = new Meter(testLogger, "outer");
            outer.start();

            final Meter inner = new Meter(testLogger, "inner");
            inner.start();

            assertSame(inner, MeterFactory.getCurrentMeter(), "should have inner as current meter while it is active");

            // When: the inner meter ends
            inner.ok();

            // Then: the previous meter becomes current again
            assertSame(outer, MeterFactory.getCurrentMeter(), "should restore previous current meter after inner termination");
            outer.ok();
        }
    }

    @Nested
    @DisplayName("getCurrentSubMeter(String)")
    class GetCurrentSubMeterTests {

        @Test
        @DisplayName("should create sub meter with concatenated operation when parent has operation")
        void shouldCreateSubMeterWithConcatenatedOperationWhenParentHasOperation() {
            // Given: a started parent meter with operation
            final Meter parent = new Meter(testLogger, "parent");
            parent.start();

            // When: a sub meter is created from current meter
            final Meter sub = MeterFactory.getCurrentSubMeter("child");

            // Then: category is inherited, operation is concatenated, and parent ID is set
            assertNotNull(sub, "should create non-null sub meter");
            assertEquals(TEST_CATEGORY, sub.getCategory(), "should inherit category from parent");
            assertEquals("parent/child", sub.getOperation(), "should concatenate operation with sub name");
            assertEquals(parent.getFullID(), sub.getParent(), "should set parent to parent's full ID");

            parent.ok();
        }

        @Test
        @DisplayName("should create sub meter with operation when parent has null operation")
        void shouldCreateSubMeterWithOperationWhenParentHasNullOperation() {
            // Given: a started parent meter without operation
            final Meter parent = new Meter(testLogger, null);
            parent.start();

            // When: a sub meter is created from current meter
            final Meter sub = MeterFactory.getCurrentSubMeter("child");

            // Then: category is inherited, operation is just the sub name, and parent ID is set
            assertNotNull(sub, "should create non-null sub meter");
            assertEquals(TEST_CATEGORY, sub.getCategory(), "should inherit category from parent");
            assertEquals("child", sub.getOperation(), "should use sub name as operation when parent operation is null");
            assertEquals(parent.getFullID(), sub.getParent(), "should set parent to parent's full ID");

            parent.ok();
        }

        @Test
        @DisplayName("should create nested sub meters with hierarchical operations")
        void shouldCreateNestedSubMetersWithHierarchicalOperations() {
            // Given: a started parent meter
            final Meter parent = new Meter(testLogger, "parent");
            parent.start();

            // When: creating nested sub meters
            final Meter firstLevel = MeterFactory.getCurrentSubMeter("level1");
            firstLevel.start();

            final Meter secondLevel = MeterFactory.getCurrentSubMeter("level2");
            secondLevel.start();

            // Then: each level should have concatenated operations and correct parent IDs
            assertEquals(TEST_CATEGORY, firstLevel.getCategory(), "should inherit category at first level");
            assertEquals("parent/level1", firstLevel.getOperation(), "should concatenate operation at first level");
            assertEquals(parent.getFullID(), firstLevel.getParent(), "should set parent ID at first level");

            assertEquals(TEST_CATEGORY, secondLevel.getCategory(), "should inherit category at second level");
            assertEquals("parent/level1/level2", secondLevel.getOperation(), "should concatenate operation at second level");
            assertEquals(firstLevel.getFullID(), secondLevel.getParent(), "should set parent ID at second level");

            secondLevel.ok();
            firstLevel.ok();
            parent.ok();
        }

        @Test
        @DisplayName("should inherit category from parent regardless of parent's logger")
        void shouldInheritCategoryFromParentRegardlessOfParentLogger() {
            // Given: a started parent meter with a different category
            final String customCategory = "custom.category";
            final Logger customLogger = org.slf4j.LoggerFactory.getLogger(customCategory);
            final Meter parent = new Meter(customLogger, "operation");
            parent.start();

            // When: a sub meter is created
            final Meter sub = MeterFactory.getCurrentSubMeter("child");

            // Then: the sub meter should inherit the parent's category
            assertEquals(customCategory, sub.getCategory(), "should inherit custom category from parent");
            assertEquals("operation/child", sub.getOperation(), "should concatenate operation correctly");
            assertEquals(parent.getFullID(), sub.getParent(), "should set parent to parent's full ID");

            parent.ok();
        }

        @Test
        @DisplayName("should create sub meter from fallback when no current meter is started")
        void shouldCreateSubMeterFromFallbackWhenNoCurrentMeterIsStarted() {
            // Given: no active meter on the current thread

            // When: a sub meter is requested
            final Meter sub = MeterFactory.getCurrentSubMeter("child");

            // Then: a sub meter should be created from the fallback meter
            assertNotNull(sub, "should create non-null sub meter even without current meter");
            assertEquals(Meter.UNKNOWN_LOGGER_NAME, sub.getCategory(), "should inherit fallback category");
            assertEquals("child", sub.getOperation(), "should use sub name as operation when parent has no operation");
        }
    }
}
