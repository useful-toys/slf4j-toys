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
package org.usefultoys.slf4j.meter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.impl.MockLogger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.SessionConfig;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.slf4j.impl.MockLoggerEvent.Level.ERROR;
import static org.usefultoys.slf4j.meter.Markers.ILLEGAL;

/**
 *
 * @author Daniel Felix Ferber
 */
@SuppressWarnings("UnusedAssignment")
class MeterIterationAttributesTest {

    MockLogger logger = (MockLogger) LoggerFactory.getLogger("MeterIterationAttributesTest");

    @BeforeAll
    static void validateConsistentCharset() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    void setupLogger() {
        logger.clearEvents();
        logger.setEnabled(true);
    }

    @AfterEach
    void clearLogger() {
        logger.clearEvents();
        logger.setEnabled(true);
    }

    @Test
    void testIterationAttributes1() {
        final int iterationCount = 4;
        final Meter m1 = new Meter(logger).iterations(iterationCount).start();
        assertEquals(iterationCount, m1.getExpectedIterations());
        assertEquals(0, m1.getCurrentIteration());
        m1.inc();
        assertEquals(iterationCount, m1.getExpectedIterations());
        assertEquals(1, m1.getCurrentIteration());
        m1.incBy(2);
        assertEquals(iterationCount, m1.getExpectedIterations());
        assertEquals(3, m1.getCurrentIteration());
        m1.incTo(4);
        assertEquals(iterationCount, m1.getExpectedIterations());
        assertEquals(4, m1.getCurrentIteration());
        m1.ok();
    }


    @Test
    void testIterationAttributes2() {
        final int iterationCount = 10;
        
        
        final Meter m1 = new Meter(logger);
        
        assertEquals(0, m1.getExpectedIterations());
        assertEquals(0, m1.getCurrentIteration());
        assertEquals(0.0d, m1.getIterationsPerSecond(), Double.MIN_VALUE);

        m1.iterations(iterationCount);
        
        assertEquals(iterationCount, m1.getExpectedIterations());
        assertEquals(0, m1.getCurrentIteration());
        assertEquals(0.0d, m1.getIterationsPerSecond(), Double.MIN_VALUE);

        final long now1a = System.nanoTime();
        m1.start();
        final long now1b = System.nanoTime();
        
        assertEquals(iterationCount, m1.getExpectedIterations());
        assertEquals(0, m1.getCurrentIteration());
        assertEquals(0.0d, m1.getIterationsPerSecond(), Double.MIN_VALUE);
        
        m1.inc();
        
        assertEquals(iterationCount, m1.getExpectedIterations());
        assertEquals(1, m1.getCurrentIteration());
        assertTrue(m1.getIterationsPerSecond() > 0.0);

        m1.incBy(2);
        assertEquals(iterationCount, m1.getExpectedIterations());
        assertTrue(m1.getIterationsPerSecond() > 0.0);
        assertEquals(3, m1.getCurrentIteration());
        
        m1.incTo(4);
        assertEquals(iterationCount, m1.getExpectedIterations());
        assertEquals(4, m1.getCurrentIteration());
        assertTrue(m1.getIterationsPerSecond() > 0.0);
        
        final long now2a = System.nanoTime();
        m1.ok();
        final long now2b = System.nanoTime();
        
        assertEquals(iterationCount, m1.getExpectedIterations());
        assertEquals(4, m1.getCurrentIteration());
        assertTrue(m1.getIterationsPerSecond() > 0.0);
    }

    @Test
    void testInvalidIteration() {
        final Meter meter = new Meter(logger);

        // Test m(message) with null
        meter.iterations(-1);
        logger.assertEvent(0, ERROR, ILLEGAL, "Illegal call to Meter.iterations(expectedIterations): Non-positive argument. id=MeterIterationAttributesTest#");
        meter.start();
        meter.incBy(-1);
        logger.assertEvent(3, ERROR, ILLEGAL, "Illegal call to Meter.incBy(increment): Non-positive argument. id=MeterIterationAttributesTest#");
        meter.incTo(-1);
        logger.assertEvent(4, ERROR, ILLEGAL, "Illegal call to Meter.incTo(currentIteration): Non-positive argument. id=MeterIterationAttributesTest#");

        meter.incTo(10);
        meter.incTo(5);
        logger.assertEvent(5, ERROR, ILLEGAL, "Illegal call to Meter.incTo(currentIteration): Non-forward iteration. id=MeterIterationAttributesTest#");
        meter.ok();
    }
 }
