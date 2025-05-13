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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the MeterFactory class.
 */
class MeterFactoryTest {

    private MockLogger testLogger;
    private static final String TEST_CATEGORY = "test.category";
    private static final String TEST_OPERATION = "testOperation";
    private static final String TEST_SUBOPERATION = "testSubOperation";

    @BeforeEach
    void setUp() {
        // Get a MockLogger instance for testing
        testLogger = (MockLogger) LoggerFactory.getLogger(TEST_CATEGORY);
        testLogger.clearEvents();
        testLogger.setEnabled(true);
    }

    @AfterEach
    void tearDown() {
        // Ensure no state is maintained between tests
        testLogger.clearEvents();
        testLogger.setEnabled(true);
    }

    @Test
    void testGetMeterFromLogger() {
        // Test creating a Meter from a Logger
        final Meter meter = MeterFactory.getMeter(testLogger);
        
        assertNotNull(meter, "The Meter should not be null");
        assertEquals(TEST_CATEGORY, meter.getCategory(), 
                "The Meter's category should match the logger name");
        assertNull(meter.getOperation(), 
                "The operation name should be null when not specified");
    }

    @Test
    void testGetMeterFromCategory() {
        // Test creating a Meter from a category string
        final Meter meter = MeterFactory.getMeter(TEST_CATEGORY);
        
        assertNotNull(meter, "The Meter should not be null");
        assertEquals(TEST_CATEGORY, meter.getCategory(), 
                "The Meter's category should match the specified category");
        assertNull(meter.getOperation(), 
                "The operation name should be null when not specified");
    }

    @Test
    void testGetMeterFromClass() {
        // Test creating a Meter from a Class
        final Meter meter = MeterFactory.getMeter(MeterFactoryTest.class);
        
        assertNotNull(meter, "The Meter should not be null");
        assertEquals(MeterFactoryTest.class.getName(), meter.getCategory(), 
                "The Meter's category should match the class name");
        assertNull(meter.getOperation(), 
                "The operation name should be null when not specified");
    }

    @Test
    void testGetMeterFromClassAndOperation() {
        // Test creating a Meter from a Class and operation name
        final Meter meter = MeterFactory.getMeter(MeterFactoryTest.class, TEST_OPERATION);
        
        assertNotNull(meter, "The Meter should not be null");
        assertEquals(MeterFactoryTest.class.getName(), meter.getCategory(), 
                "The Meter's category should match the class name");
        assertEquals(TEST_OPERATION, meter.getOperation(), 
                "The operation name should match the specified name");
    }

    @Test
    void testGetMeterFromLoggerAndOperation() {
        // Test creating a Meter from a Logger and operation name
        final Meter meter = MeterFactory.getMeter(testLogger, TEST_OPERATION);
        
        assertNotNull(meter, "The Meter should not be null");
        assertEquals(TEST_CATEGORY, meter.getCategory(), 
                "The Meter's category should match the logger name");
        assertEquals(TEST_OPERATION, meter.getOperation(), 
                "The operation name should match the specified name");
    }

    @Test
    void testGetCurrentMeter() {
        // Create and start a Meter
        final Meter originalMeter = MeterFactory.getMeter(testLogger, TEST_OPERATION);
        originalMeter.start();

        // Get the current Meter
        final Meter currentMeter = MeterFactory.getCurrentMeter();

        assertNotNull(currentMeter, "The current Meter should not be null");
        assertSame(originalMeter, currentMeter,
                "The current Meter should be the same as the last started one");
        originalMeter.ok();
    }

    @Test
    void testGetCurrentMeterWhenNoMeterStarted() {
        // Try to get the current Meter
        final Meter currentMeter = MeterFactory.getCurrentMeter();

        assertNotNull(currentMeter, "The Meter should not be null");
        assertEquals("???", currentMeter.getCategory(),
                "The Meter's category should match the logger name");
        assertNull(currentMeter.getOperation(), "The operation name should match the specified name");
    }

    @Test
    void testGetCurrentSubMeter() {
        // Create and start a Meter
        final Meter originalMeter = MeterFactory.getMeter(testLogger, TEST_OPERATION);
        originalMeter.start();

        // Create a sub-Meter
        final Meter subMeter = MeterFactory.getCurrentSubMeter(TEST_SUBOPERATION);

        assertNotNull(subMeter, "The sub-Meter should not be null");
        assertEquals(TEST_CATEGORY, subMeter.getCategory(),
                "The sub-Meter's category should match the original Meter's category");
        assertEquals(TEST_OPERATION + "/" + TEST_SUBOPERATION, subMeter.getOperation(),
                "The sub-Meter's operation name should combine the original operation name and the sub-name");
        originalMeter.ok();
    }

    @Test
    void testGetCurrentSubMeterWhenNoMeterStarted() {
        // Create a sub-Meter
        final Meter subMeter = MeterFactory.getCurrentSubMeter(TEST_SUBOPERATION);

        assertNotNull(subMeter, "The sub-Meter should not be null");
        assertEquals("???", subMeter.getCategory(),
                "The sub-Meter's category should match the original Meter's category");
        assertEquals(TEST_SUBOPERATION, subMeter.getOperation(),
                "The sub-Meter's operation name should combine the original operation name and the sub-name");
    }

    @Test
    void testMultipleSubMeters() {
        // Create and start a Meter
        final Meter originalMeter = MeterFactory.getMeter(testLogger, TEST_OPERATION);
        originalMeter.start();

        // Create multiple sub-Meters
        final Meter subMeter1 = MeterFactory.getCurrentSubMeter("sub1");
        final Meter subMeter2 = MeterFactory.getCurrentSubMeter("sub2");

        assertNotNull(subMeter1, "The first sub-Meter should not be null");
        assertNotNull(subMeter2, "The second sub-Meter should not be null");

        assertEquals(TEST_OPERATION + "/sub1", subMeter1.getOperation(),
                "The first sub-Meter's operation name should be correct");
        assertEquals(TEST_OPERATION + "/sub2", subMeter2.getOperation(),
                "The second sub-Meter's operation name should be correct");

        // Start the first sub-Meter and verify it becomes the current Meter
        subMeter1.start();
        assertSame(subMeter1, MeterFactory.getCurrentMeter(),
                "The current Meter should be the most recently started sub-Meter");
        subMeter1.ok();
        originalMeter.ok();
    }
}
