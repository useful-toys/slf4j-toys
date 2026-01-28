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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.usefultoys.slf4j.internal.TimeSource;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetMeterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.ValidateCleanMeter;
import org.usefultoys.test.WithLocale;

import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link Meter} basic features.
 * <p>
 * Tests validate basic Meter functionality that does not depend on preconditions,
 * startTime, or stopTime. This includes constructors, utility methods, and simple
 * operations.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Constructors:</b> Validates all three constructors with different parameter combinations</li>
 *   <li><b>Time Source Injection:</b> Tests withTimeSource() method for custom time sources</li>
 *   <li><b>Position Extraction:</b> Tests extractNextPosition() for unique sequential IDs</li>
 *   <li><b>Path Conversion:</b> Tests toPath() for String, Enum, Throwable, and Object conversions</li>
 *   <li><b>Sub-Meter Creation:</b> Tests sub() method for creating child meters</li>
 *   <li><b>Current Instance Tracking:</b> Tests getCurrentInstance() thread-local behavior</li>
 *   <li><b>Current Instance Check:</b> Tests checkCurrentInstance() for validating thread-local state</li>
 *   <li><b>Logger Access:</b> Validates getMessageLogger() and getDataLogger() methods</li>
 * </ul>
 *
 * @author Co-authored-by: GitHub Copilot using Claude Sonnet 4.5
 */
@DisplayName("Meter Basic Features")
@ValidateCharset
@ResetMeterConfig
@WithLocale("en")
@WithMockLogger
@ValidateCleanMeter
class MeterBasicFeaturesTest {

    @Slf4jMock
    private Logger logger;

    /* Enum for testing path conversion */
    private enum TestEnum {
        VALUE1, VALUE2, VALUE3
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should create meter with logger only")
        void shouldCreateMeterWithLoggerOnly() {
            // Given: a logger
            // When: creating a meter with logger only
            final Meter meter = new Meter(logger);

            // Then: meter is initialized with correct values
            assertNotNull(meter, "should create non-null meter");
            assertEquals(logger.getName(), meter.getCategory(), "should use logger name as category");
            assertNull(meter.getOperation(), "should have null operation");
            assertNull(meter.getParent(), "should have null parent");
            assertNotNull(meter.getMessageLogger(), "should have message logger");
            assertNotNull(meter.getDataLogger(), "should have data logger");
            assertTrue(meter.getPosition() > 0, "should have positive position");
        }

        @Test
        @DisplayName("should create meter with logger and operation")
        void shouldCreateMeterWithLoggerAndOperation() {
            // Given: a logger and operation name
            final String operationName = "testOperation";

            // When: creating a meter with logger and operation
            final Meter meter = new Meter(logger, operationName);

            // Then: meter is initialized with correct values
            assertNotNull(meter, "should create non-null meter");
            assertEquals(logger.getName(), meter.getCategory(), "should use logger name as category");
            assertEquals(operationName, meter.getOperation(), "should use provided operation name");
            assertNull(meter.getParent(), "should have null parent");
            assertNotNull(meter.getMessageLogger(), "should have message logger");
            assertNotNull(meter.getDataLogger(), "should have data logger");
            assertTrue(meter.getPosition() > 0, "should have positive position");
        }

        @Test
        @DisplayName("should create meter with logger, operation, and parent")
        void shouldCreateMeterWithLoggerOperationAndParent() {
            // Given: a logger, operation name, and parent ID
            final String operationName = "childOperation";
            final String parentId = "parent-uuid/category/operation#123";

            // When: creating a meter with logger, operation, and parent
            final Meter meter = new Meter(logger, operationName, parentId);

            // Then: meter is initialized with correct values
            assertNotNull(meter, "should create non-null meter");
            assertEquals(logger.getName(), meter.getCategory(), "should use logger name as category");
            assertEquals(operationName, meter.getOperation(), "should use provided operation name");
            assertEquals(parentId, meter.getParent(), "should use provided parent ID");
            assertNotNull(meter.getMessageLogger(), "should have message logger");
            assertNotNull(meter.getDataLogger(), "should have data logger");
            assertTrue(meter.getPosition() > 0, "should have positive position");
        }

        @Test
        @DisplayName("should throw NullPointerException when logger is null")
        void shouldThrowWhenLoggerIsNull() {
            // Given: a null logger
            // When: creating a meter with null logger
            // Then: should throw NullPointerException
            assertThrows(NullPointerException.class, () -> new Meter(null),
                    "should throw NullPointerException for null logger");
        }
    }

    @Nested
    @DisplayName("Time Source Injection Tests")
    class TimeSourceTests {

        @Test
        @DisplayName("should inject custom time source")
        void shouldInjectCustomTimeSource() {
            // Given: a meter and a custom time source
            final Meter meter = new Meter(logger);
            final TimeSource customTimeSource = new TimeSource() {
                @Override
                public long nanoTime() {
                    return 123456789L;
                }
            };

            // When: injecting custom time source
            final Meter result = meter.withTimeSource(customTimeSource);

            // Then: should return the same meter instance for chaining
            assertSame(meter, result, "should return same meter instance for method chaining");
        }

        @Test
        @DisplayName("should allow method chaining with withTimeSource")
        void shouldAllowMethodChainingWithTimeSource() {
            // Given: a meter and a custom time source
            final TimeSource customTimeSource = new TimeSource() {
                @Override
                public long nanoTime() {
                    return 999999999L;
                }
            };

            // When: chaining withTimeSource with other methods
            final Meter meter = new Meter(logger)
                    .withTimeSource(customTimeSource)
                    .m("test description");

            // Then: meter should have description set
            assertEquals("test description", meter.getDescription(), "should preserve method chaining");
        }
    }

    @Nested
    @DisplayName("Position Extraction Tests")
    class PositionExtractionTests {

        @Test
        @DisplayName("should generate sequential positions for same category")
        void shouldGenerateSequentialPositionsForSameCategory() {
            // Given: a category without operation name
            final String category = "testCategory";

            // When: extracting positions multiple times for same category
            final long pos1 = Meter.extractNextPosition(category, null);
            final long pos2 = Meter.extractNextPosition(category, null);
            final long pos3 = Meter.extractNextPosition(category, null);

            // Then: positions should be sequential
            assertTrue(pos1 > 0, "should generate positive position");
            assertEquals(pos1 + 1, pos2, "should increment position by 1");
            assertEquals(pos2 + 1, pos3, "should continue incrementing");
        }

        @Test
        @DisplayName("should generate sequential positions for category with operation")
        void shouldGenerateSequentialPositionsForCategoryWithOperation() {
            // Given: a category with operation name
            final String category = "category2";
            final String operation = "operation2";

            // When: extracting positions multiple times for same category/operation
            final long pos1 = Meter.extractNextPosition(category, operation);
            final long pos2 = Meter.extractNextPosition(category, operation);
            final long pos3 = Meter.extractNextPosition(category, operation);

            // Then: positions should be sequential
            assertTrue(pos1 > 0, "should generate positive position");
            assertEquals(pos1 + 1, pos2, "should increment position by 1");
            assertEquals(pos2 + 1, pos3, "should continue incrementing");
        }

        @Test
        @DisplayName("should generate independent positions for different categories")
        void shouldGenerateIndependentPositionsForDifferentCategories() {
            // Given: different categories
            final String category1 = "category3";
            final String category2 = "category4";

            // When: extracting positions for different categories
            final long pos1 = Meter.extractNextPosition(category1, null);
            final long pos2 = Meter.extractNextPosition(category2, null);

            // Then: positions should be independent (not necessarily sequential)
            assertTrue(pos1 > 0, "should generate positive position for category1");
            assertTrue(pos2 > 0, "should generate positive position for category2");
        }

        @Test
        @DisplayName("should generate independent positions for different operations")
        void shouldGenerateIndependentPositionsForDifferentOperations() {
            // Given: same category with different operations
            final String category = "category5";
            final String operation1 = "operation1";
            final String operation2 = "operation2";

            // When: extracting positions for different operations
            final long pos1 = Meter.extractNextPosition(category, operation1);
            final long pos2 = Meter.extractNextPosition(category, operation2);

            // Then: positions should be independent
            assertTrue(pos1 > 0, "should generate positive position for operation1");
            assertTrue(pos2 > 0, "should generate positive position for operation2");
        }

        @Test
        @DisplayName("should wrap position at Long.MAX_VALUE")
        void shouldWrapPositionAtMaxValue() {
            // Given: a category with counter at MAX_VALUE
            final String category = "wrapTestCategory";
            final AtomicLong counter = new AtomicLong(Long.MAX_VALUE);
            Meter.EVENT_COUNTER.put(category, counter);

            // When: extracting position
            final long pos = Meter.extractNextPosition(category, null);

            // Then: should wrap to 1 (0 is never used, counter increments first)
            assertEquals(1L, pos, "should wrap to 1 when counter was at MAX_VALUE");
        }
    }

    @Nested
    @DisplayName("Path Conversion Tests")
    class PathConversionTests {

        @Test
        @DisplayName("should return null for null object")
        void shouldReturnNullForNullObject() {
            // Given: a null object
            // When: converting to path
            final String result = Meter.toPath(null, true);

            // Then: should return null
            assertNull(result, "should return null for null object");
        }

        @Test
        @DisplayName("should return String as-is")
        void shouldReturnStringAsIs() {
            // Given: a String object
            final String input = "testPath";

            // When: converting to path
            final String result = Meter.toPath(input, true);

            // Then: should return the same string
            assertEquals(input, result, "should return String as-is");
        }

        @Test
        @DisplayName("should return Enum name")
        void shouldReturnEnumName() {
            // Given: an Enum value
            final TestEnum input = TestEnum.VALUE2;

            // When: converting to path
            final String result = Meter.toPath(input, true);

            // Then: should return enum name
            assertEquals("VALUE2", result, "should return Enum.name()");
        }

        @Test
        @DisplayName("should return Throwable simple class name when useSimpleClassNameForThrowable is true")
        void shouldReturnThrowableSimpleClassName() {
            // Given: a Throwable object
            final Throwable input = new IllegalArgumentException("test message");

            // When: converting to path with useSimpleClassNameForThrowable=true
            final String result = Meter.toPath(input, true);

            // Then: should return simple class name
            assertEquals("IllegalArgumentException", result, "should return simple class name for Throwable");
        }

        @Test
        @DisplayName("should return Throwable full class name when useSimpleClassNameForThrowable is false")
        void shouldReturnThrowableFullClassName() {
            // Given: a Throwable object
            final Throwable input = new IllegalArgumentException("test message");

            // When: converting to path with useSimpleClassNameForThrowable=false
            final String result = Meter.toPath(input, false);

            // Then: should return full class name
            assertEquals("java.lang.IllegalArgumentException", result, "should return full class name for Throwable");
        }

        @Test
        @DisplayName("should return toString for other objects")
        void shouldReturnToStringForOtherObjects() {
            // Given: an arbitrary object with toString
            final Object input = new Object() {
                @Override
                public String toString() {
                    return "customToString";
                }
            };

            // When: converting to path
            final String result = Meter.toPath(input, true);

            // Then: should return toString value
            assertEquals("customToString", result, "should return toString() for arbitrary objects");
        }

        @ParameterizedTest
        @MethodSource("org.usefultoys.slf4j.meter.MeterBasicFeaturesTest#providePathConversionCases")
        @DisplayName("should convert objects to paths correctly")
        void shouldConvertObjectsToPathsCorrectly(final Object input, final boolean useSimpleName,
                                                   final String expected, final String description) {
            // Given: various objects (provided by parameters)
            // When: converting to path
            final String result = Meter.toPath(input, useSimpleName);

            // Then: should match expected result
            assertEquals(expected, result, description);
        }
    }

    /* Provider method for path conversion test cases */
    static Stream<Arguments> providePathConversionCases() {
        return Stream.of(
                Arguments.of(null, true, null, "null object"),
                Arguments.of("simpleString", true, "simpleString", "String object"),
                Arguments.of(TestEnum.VALUE1, true, "VALUE1", "Enum object"),
                Arguments.of(new RuntimeException(), true, "RuntimeException", "Throwable with simple name"),
                Arguments.of(new RuntimeException(), false, "java.lang.RuntimeException", "Throwable with full name"),
                Arguments.of(123, true, "123", "Integer object")
        );
    }

    @Nested
    @DisplayName("Sub-Meter Creation Tests")
    class SubMeterTests {

        @Test
        @DisplayName("should create sub-meter with operation name appended")
        void shouldCreateSubMeterWithOperationAppended() {
            // Given: a meter with operation
            final Meter parent = new Meter(logger, "parentOp");

            // When: creating a sub-meter
            final Meter sub = parent.sub("childOp");

            // Then: sub-meter should have combined operation name
            assertNotNull(sub, "should create non-null sub-meter");
            assertEquals(logger.getName(), sub.getCategory(), "should inherit category from parent");
            assertEquals("parentOp/childOp", sub.getOperation(), "should append operation name");
            assertEquals(parent.getFullID(), sub.getParent(), "should have parent ID set");
        }

        @Test
        @DisplayName("should create sub-meter when parent has no operation")
        void shouldCreateSubMeterWhenParentHasNoOperation() {
            // Given: a meter without operation
            final Meter parent = new Meter(logger);

            // When: creating a sub-meter
            final Meter sub = parent.sub("childOp");

            // Then: sub-meter should use child operation name directly
            assertNotNull(sub, "should create non-null sub-meter");
            assertEquals(logger.getName(), sub.getCategory(), "should inherit category from parent");
            assertEquals("childOp", sub.getOperation(), "should use child operation name");
            assertEquals(parent.getFullID(), sub.getParent(), "should have parent ID set");
        }

        @Test
        @DisplayName("should create sub-meter when child operation is null")
        void shouldCreateSubMeterWhenChildOperationIsNull() {
            // Given: a meter with operation
            final Meter parent = new Meter(logger, "parentOp");

            // When: creating a sub-meter with null operation
            final Meter sub = parent.sub(null);

            // Then: sub-meter should preserve parent operation name
            assertNotNull(sub, "should create non-null sub-meter");
            assertEquals(logger.getName(), sub.getCategory(), "should inherit category from parent");
            assertEquals("parentOp", sub.getOperation(), "should preserve parent operation name");
            assertEquals(parent.getFullID(), sub.getParent(), "should have parent ID set");
        }

        @Test
        @DisplayName("should create sub-meter when both operations are null")
        void shouldCreateSubMeterWhenBothOperationsAreNull() {
            // Given: a meter without operation
            final Meter parent = new Meter(logger);

            // When: creating a sub-meter with null operation
            final Meter sub = parent.sub(null);

            // Then: sub-meter should have null operation
            assertNotNull(sub, "should create non-null sub-meter");
            assertEquals(logger.getName(), sub.getCategory(), "should inherit category from parent");
            assertNull(sub.getOperation(), "should have null operation");
            assertEquals(parent.getFullID(), sub.getParent(), "should have parent ID set");
        }

        @Test
        @DisplayName("should inherit context from parent meter")
        void shouldInheritContextFromParentMeter() {
            // Given: a meter with context
            final Meter parent = new Meter(logger, "parentOp");
            parent.ctx("key1", "value1");
            parent.ctx("key2", "value2");

            // When: creating a sub-meter
            final Meter sub = parent.sub("childOp");

            // Then: sub-meter should have copy of parent context
            assertNotNull(sub.getContext(), "should have context map");
            assertEquals(2, sub.getContext().size(), "should have 2 context entries");
            assertEquals("value1", sub.getContext().get("key1"), "should inherit key1");
            assertEquals("value2", sub.getContext().get("key2"), "should inherit key2");
        }

        @Test
        @DisplayName("should not inherit context when parent has no context")
        void shouldNotInheritContextWhenParentHasNoContext() {
            // Given: a meter without context
            final Meter parent = new Meter(logger, "parentOp");

            // When: creating a sub-meter
            final Meter sub = parent.sub("childOp");

            // Then: sub-meter should have empty or null context
            assertTrue(sub.getContext() == null || sub.getContext().isEmpty(),
                    "should not have context entries when parent has no context");
        }
    }

    @Nested
    @DisplayName("Current Instance Tracking Tests")
    class CurrentInstanceTests {

        @Test
        @DisplayName("should return unknown meter when no meter is current")
        void shouldReturnUnknownMeterWhenNoMeterIsCurrent() {
            // Given: no meter has been started on current thread
            // When: getting current instance
            final Meter current = Meter.getCurrentInstance();

            // Then: should return unknown meter
            assertNotNull(current, "should return non-null meter");
            assertEquals(Meter.UNKNOWN_LOGGER_NAME, current.getCategory(),
                    "should return meter with unknown logger name");
        }

        @Test
        @DisplayName("should return current meter after start")
        void shouldReturnCurrentMeterAfterStart() {
            // Given: a meter that has been started
            final Meter meter = new Meter(logger, "testOp");
            meter.start();

            // When: getting current instance
            final Meter current = Meter.getCurrentInstance();

            // Then: should return the started meter
            assertSame(meter, current, "should return the same meter instance");

            /* Cleanup */
            meter.ok();
        }

        @Test
        @DisplayName("should return unknown meter after meter is closed")
        void shouldReturnUnknownMeterAfterMeterIsClosed() {
            // Given: a meter that has been started and closed
            final Meter meter = new Meter(logger, "testOp");
            meter.start();
            meter.ok();

            // When: getting current instance
            final Meter current = Meter.getCurrentInstance();

            // Then: should return unknown meter
            assertEquals(Meter.UNKNOWN_LOGGER_NAME, current.getCategory(),
                    "should return unknown meter after meter is closed");
        }
    }

    @Nested
    @DisplayName("Logger Access Tests")
    class LoggerAccessTests {

        @Test
        @DisplayName("should provide access to message logger")
        void shouldProvideAccessToMessageLogger() {
            // Given: a meter
            final Meter meter = new Meter(logger);

            // When: accessing message logger
            final Logger messageLogger = meter.getMessageLogger();

            // Then: should return non-null logger with correct name
            assertNotNull(messageLogger, "should return non-null message logger");
            assertTrue(messageLogger.getName().contains(logger.getName()),
                    "message logger name should contain category");
        }

        @Test
        @DisplayName("should provide access to data logger")
        void shouldProvideAccessToDataLogger() {
            // Given: a meter
            final Meter meter = new Meter(logger);

            // When: accessing data logger
            final Logger dataLogger = meter.getDataLogger();

            // Then: should return non-null logger with correct name
            assertNotNull(dataLogger, "should return non-null data logger");
            assertTrue(dataLogger.getName().contains(logger.getName()),
                    "data logger name should contain category");
        }
    }

    @Nested
    @DisplayName("Current Instance Check Tests")
    class CurrentInstanceCheckTests {

        @Test
        @DisplayName("should return true when no meter is current on thread")
        void shouldReturnTrueWhenNoMeterIsCurrent() {
            // Given: a meter that has not been started
            final Meter meter = new Meter(logger);

            // When: checking if this is the current instance
            final boolean result = meter.checkCurrentInstance();

            // Then: should return true (this meter is NOT the current instance)
            assertTrue(result, "should return true when no meter is current on thread");
        }

        @Test
        @DisplayName("should return false when this meter is the current instance")
        void shouldReturnFalseWhenThisMeterIsCurrent() {
            // Given: a meter that has been started
            final Meter meter = new Meter(logger);
            meter.start();

            // When: checking if this is the current instance
            final boolean result = meter.checkCurrentInstance();

            // Then: should return false (this meter IS the current instance)
            assertFalse(result, "should return false when this meter is the current instance");

            /* Cleanup */
            meter.ok();
        }

        @Test
        @DisplayName("should return true when another meter is the current instance")
        void shouldReturnTrueWhenAnotherMeterIsCurrent() {
            // Given: two meters, where the second one is started
            final Meter meter1 = new Meter(logger, "operation1");
            final Meter meter2 = new Meter(logger, "operation2");
            meter2.start();

            // When: checking if meter1 is the current instance
            final boolean result = meter1.checkCurrentInstance();

            // Then: should return true (meter1 is NOT the current instance, meter2 is)
            assertTrue(result, "should return true when another meter is the current instance");

            /* Cleanup */
            meter2.ok();
        }

        @Test
        @DisplayName("should return true after meter is stopped")
        void shouldReturnTrueAfterMeterIsStopped() {
            // Given: a meter that was started and then stopped
            final Meter meter = new Meter(logger);
            meter.start();
            meter.ok();

            // When: checking if this is the current instance after stopping
            final boolean result = meter.checkCurrentInstance();

            // Then: should return true (this meter is no longer the current instance)
            assertTrue(result, "should return true after meter is stopped");
        }

        @Test
        @DisplayName("should return false for current meter even when other meters exist")
        void shouldReturnFalseForCurrentMeterEvenWhenOtherMetersExist() {
            // Given: multiple meters where only the last one is current
            final Meter meter1 = new Meter(logger, "operation1");
            final Meter meter2 = new Meter(logger, "operation2");
            final Meter meter3 = new Meter(logger, "operation3");
            meter3.start();

            // When: checking each meter
            final boolean result1 = meter1.checkCurrentInstance();
            final boolean result2 = meter2.checkCurrentInstance();
            final boolean result3 = meter3.checkCurrentInstance();

            // Then: only meter3 should return false (it is current)
            assertTrue(result1, "meter1 should not be current");
            assertTrue(result2, "meter2 should not be current");
            assertFalse(result3, "meter3 should be current");

            /* Cleanup */
            meter3.ok();
        }
    }
}
