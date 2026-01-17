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

import org.slf4j.Logger;
import org.usefultoys.slf4jtestmock.Slf4jMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Helper utilities for {@link Meter} lifecycle tests.
 * <p>
 * This class provides shared helper methods, test enums, and test objects used across
 * multiple {@code MeterLifeCycle*Test} classes to avoid code duplication and ensure consistency.
 * <p>
 * <b>Provided Utilities:</b>
 * <ul>
 *   <li><b>assertMeterState():</b> Comprehensive assertion method to verify all Meter state attributes</li>
 *   <li><b>TestEnum:</b> Sample enum for testing path handling with enum values</li>
 *   <li><b>TestObject:</b> Sample object for testing path handling with custom objects</li>
 * </ul>
 * <p>
 * <b>Usage Pattern:</b> Test classes should use static import for {@code assertMeterState()}
 * and reference {@code TestEnum} and {@code TestObject} as needed.
 * <p>
 * This is a utility class and should not be instantiated. It is not a test class itself.
 *
 * @author Co-authored-by: GitHub Copilot using Gemini 3 Flash (Preview)
 * @author Co-authored-by: GitHub Copilot using GPT-5.2
 * @author Co-authored-by: GitHub Copilot using Claude 3.5 Sonnet
 */
class MeterLifeCycleTestHelper {

    @SuppressWarnings("NonConstantLogger")
    @Slf4jMock
    Logger logger;

    /**
     * Test enum for validating enum path handling in Meter.
     */
    enum TestEnum {
        VALUE1, VALUE2
    }

    /**
     * Test object for validating object path handling in Meter.
     */
    static class TestObject {
        @Override
        public String toString() {
            return "testObjectString";
        }
    }

    /**
     * Verifies the state of the given {@code Meter} object against the provided expected values and conditions.
     *
     * @param meter                 the {@code Meter} object to validate
     * @param started               {@code true} if the meter is expected to be started, otherwise {@code false}
     * @param stopped               {@code true} if the meter is expected to be stopped, otherwise {@code false}
     * @param okPath                the expected value of the "okPath" property, or {@code null} if it is expected to be null
     * @param rejectPath            the expected value of the "rejectPath" property, or {@code null} if it is expected to be null
     * @param failPath              the expected value of the "failPath" property, or {@code null} if it is expected to be null
     * @param failMessage           the expected value of the "*/
    static void assertMeterState(final Meter meter, final boolean started, final boolean stopped, final String okPath, final String rejectPath, final String failPath, final String failMessage, final long currentIteration, final long expectedIterations, final long timeLimitMilliseconds) {
        if (started) {
            assertTrue(meter.getStartTime() > 0, "startTime should be > 0");
        } else {
            assertEquals(0, meter.getStartTime(), "startTime should be 0");
        }

        if (stopped) {
            assertTrue(meter.getStopTime() > 0, "stopTime should be > 0");
            assertTrue(meter.getStopTime() >= meter.getStartTime(), "stopTime should be >= startTime");
        } else {
            assertEquals(0, meter.getStopTime(), "stopTime should be 0");
        }

        if (okPath == null) {
            assertNull(meter.getOkPath(), "okPath should be null");
        } else {
            assertEquals(okPath, meter.getOkPath(), "okPath should match expected value: " + okPath);
        }

        if (rejectPath == null) {
            assertNull(meter.getRejectPath(), "rejectPath should be null");
        } else {
            assertEquals(rejectPath, meter.getRejectPath(), "rejectPath should match expected value: " + rejectPath);
        }

        if (failPath == null) {
            assertNull(meter.getFailPath(), "failPath should be null");
        } else {
            assertEquals(failPath, meter.getFailPath(), "failPath should match expected value: " + failPath);
        }

        if (failMessage == null) {
            assertNull(meter.getFailMessage(), "failMessage should be null");
        } else {
            assertEquals(failMessage, meter.getFailMessage(), "failMessage should match expected value: " + failMessage);
        }

        assertEquals(currentIteration, meter.getCurrentIteration(), "currentIteration should match expected value: " + currentIteration);
        assertEquals(expectedIterations, meter.getExpectedIterations(), "expectedIterations should match expected value: " + expectedIterations);
        assertEquals(timeLimitMilliseconds * 1000 * 1000, meter.getTimeLimit(), "timeLimit should match expected value: " + timeLimitMilliseconds + "ms");

        assertTrue(meter.getCreateTime() > 0, "createTime should be > 0");
        if (stopped) {
            assertTrue(meter.getLastCurrentTime() >= meter.getStopTime(), "lastCurrentTime should be >= stopTime");
        } else if (started) {
            assertTrue(meter.getLastCurrentTime() >= meter.getStartTime(), "lastCurrentTime should be >= startTime");
        } else {
            assertEquals(meter.getCreateTime(), meter.getLastCurrentTime(), "lastCurrentTime should be equal to createTime");
        }
    }

}
