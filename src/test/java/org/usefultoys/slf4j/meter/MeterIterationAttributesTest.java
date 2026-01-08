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
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent.Level;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetMeterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.ValidateCleanMeter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.usefultoys.slf4j.meter.Markers.ILLEGAL;
import static org.usefultoys.slf4j.meter.Markers.INCONSISTENT_INCREMENT;

/**
 * Unit tests for {@link Meter} iteration attributes.
 * <p>
 * Tests validate that Meter correctly tracks and reports progress through iterations and steps.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Expected Iterations:</b> Verifies that the total number of expected iterations is correctly set and reported</li>
 *   <li><b>Current Iteration:</b> Verifies that the current iteration count is correctly incremented and reported</li>
 *   <li><b>Iteration Speed:</b> Verifies that iterations per second are calculated and reported</li>
 *   <li><b>Increment By:</b> Verifies incrementing by a specific value</li>
 *   <li><b>Increment To:</b> Verifies setting the current iteration to a specific value</li>
 *   <li><b>Invalid Iterations:</b> Verifies that invalid iteration settings and increments are handled and logged</li>
 *   <li><b>Precondition Order:</b> Verifies that preconditions are checked before arguments for increment methods</li>
 * </ul>
 *
 * @author Daniel Felix Ferber
 * @author Co-authored-by: GitHub Copilot using Gemini 3 Flash (Preview)
 */
@ValidateCharset
@ResetMeterConfig
@WithMockLogger
@ValidateCleanMeter
class MeterIterationAttributesTest {

    @Slf4jMock
    private Logger logger;

    @Test
    @DisplayName("should correctly track iteration counts using inc, incBy, and incTo")
    void shouldTrackIterationCounts() {
        // Given: a meter with expected iterations
        final int iterationCount = 4;
        final Meter m1 = new Meter(logger).iterations(iterationCount).start();
        
        // Then: initial state
        assertEquals(iterationCount, m1.getExpectedIterations(), "expected iterations should match");
        assertEquals(0, m1.getCurrentIteration(), "current iteration should be 0");
        
        // When: inc() is called
        m1.inc();
        // Then: iteration count increments by 1
        assertEquals(1, m1.getCurrentIteration(), "current iteration should be 1");
        
        // When: incBy(2) is called
        m1.incBy(2);
        // Then: iteration count increments by 2
        assertEquals(3, m1.getCurrentIteration(), "current iteration should be 3");
        
        // When: incTo(4) is called
        m1.incTo(4);
        // Then: iteration count becomes 4
        assertEquals(4, m1.getCurrentIteration(), "current iteration should be 4");
        
        m1.ok();
    }

    @Test
    @DisplayName("should calculate iterations per second correctly")
    void shouldCalculateIterationsPerSecond() {
        // Given: a started meter
        final int iterationCount = 10;
        final Meter m1 = new Meter(logger).iterations(iterationCount).start();
        
        assertEquals(0.0d, m1.getIterationsPerSecond(), Double.MIN_VALUE, "initial speed should be 0.0");
        
        // When: incrementing iterations
        m1.inc();
        // Then: speed should be positive
        assertTrue(m1.getIterationsPerSecond() > 0.0, "speed should be positive after increments");

        m1.incBy(2);
        m1.incTo(4);
        assertTrue(m1.getIterationsPerSecond() > 0.0, "speed should still be positive");
        
        m1.ok();
    }

    @Test
    @DisplayName("should handle and log invalid iteration arguments and states")
    void shouldHandleInvalidIterationCalls() {
        final Meter meter = new Meter(logger);

        // When: setting negative iterations
        meter.iterations(-1);
        // Then: should log illegal argument error
        AssertLogger.assertEvent(logger, 0, Level.ERROR, ILLEGAL, "Illegal call to Meter.iterations(expectedIterations): Non-positive argument", "MeterIterationAttributesTest#");
        
        meter.start();
        
        // When: incrementing by negative value
        meter.incBy(-1);
        // Then: should log illegal argument error
        AssertLogger.assertEvent(logger, 3, Level.ERROR, ILLEGAL, "Illegal call to Meter.incBy(increment): Non-positive increment", "MeterIterationAttributesTest#");
        
        // When: incrementing to negative value
        meter.incTo(-1);
        // Then: should log illegal argument error
        AssertLogger.assertEvent(logger, 4, Level.ERROR, ILLEGAL, "Illegal call to Meter.incTo(currentIteration): Non-positive argument", "MeterIterationAttributesTest#");

        // When: incrementing to a value that does not move forward
        meter.incTo(10);
        meter.incTo(5);
        // Then: should log non-forward increment error
        AssertLogger.assertEvent(logger, 5, Level.ERROR, ILLEGAL, "Illegal call to Meter.incTo(currentIteration): Non-forward increment", "MeterIterationAttributesTest#");
        
        meter.ok();
    }

    @Test
    @DisplayName("should validate precondition before arguments for incBy")
    void shouldValidatePreconditionBeforeArgumentsForIncBy() {
        // Given: a meter that is NOT started
        final Meter meter = new Meter(logger);
        
        // When: incBy(-1) is called (both precondition and argument are invalid)
        meter.incBy(-1);
        // Then: should log "Meter not started" (precondition), NOT "Non-positive increment" (argument)
        AssertLogger.assertEvent(logger, 0, Level.ERROR, INCONSISTENT_INCREMENT, "Meter not started", "MeterIterationAttributesTest#");
    }

    @Test
    @DisplayName("should validate precondition before arguments for incTo")
    void shouldValidatePreconditionBeforeArgumentsForIncTo() {
        // Given: a meter that is NOT started
        final Meter meter = new Meter(logger);

        // When: incTo(-1) is called (both precondition and argument are invalid)
        meter.incTo(-1);
        // Then: should log "Meter not started" (precondition), NOT "Non-positive argument" (argument)
        AssertLogger.assertEvent(logger, 0, Level.ERROR, INCONSISTENT_INCREMENT, "Meter not started", "MeterIterationAttributesTest#");
    }
}
