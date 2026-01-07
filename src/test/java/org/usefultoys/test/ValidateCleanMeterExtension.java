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

package org.usefultoys.test;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.usefultoys.slf4j.meter.Meter;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JUnit 5 extension that validates the Meter thread-local stack is clean before and after each test.
 * <p>
 * This extension ensures that {@link Meter#getCurrentInstance()} returns the "unknown" Meter
 * (with category equal to {@code "???"}) both before and after each test method. This validates
 * that the thread-local Meter stack is properly cleaned up and no Meter instances are left
 * hanging in the thread after test execution.
 * <p>
 * The validation runs before each test and after each test. If a Meter instance with a
 * non-unknown category is found on the thread-local stack, the test fails with a descriptive
 * error message.
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * @ExtendWith(ValidateCleanMeterExtension.class)
 * class MyMeterTest {
 *     @Test
 *     void testMeterOperation() {
 *         // Meter stack validation runs before and after test
 *     }
 * }
 * }</pre>
 *
 * @see ValidateCleanMeter
 * @see Meter#getCurrentInstance()
 * @see Meter#UNKNOWN_LOGGER_NAME
 * @author Daniel Felix Ferber
 */
public class ValidateCleanMeterExtension implements BeforeEachCallback, AfterEachCallback {

    /**
     * Validates that the Meter stack is clean before the test starts.
     * <p>
     * Checks that {@link Meter#getCurrentInstance()} returns a Meter with the unknown
     * category ({@code "???"}), indicating no Meter instance is active on the current thread.
     *
     * @param context the current extension context
     * @throws AssertionError if a non-unknown Meter is found on the thread-local stack
     */
    @Override
    public void beforeEach(final ExtensionContext context) {
        validateMeterStackIsClean("before test");
    }

    /**
     * Validates that the Meter stack is clean after the test completes.
     * <p>
     * Checks that {@link Meter#getCurrentInstance()} returns a Meter with the unknown
     * category ({@code "???"}), indicating the test properly cleaned up all Meter instances.
     *
     * @param context the current extension context
     * @throws AssertionError if a non-unknown Meter is found on the thread-local stack
     */
    @Override
    public void afterEach(final ExtensionContext context) {
        validateMeterStackIsClean("after test");
    }

    /**
     * Validates that the current Meter instance has the unknown category.
     * <p>
     * This ensures that the thread-local Meter stack is in a clean state, with no
     * Meter instances left active.
     *
     * @param timing a descriptive string indicating when the validation occurred
     *               (e.g., "before test" or "after test")
     * @throws AssertionError if the current Meter's category is not the unknown logger name
     */
    private void validateMeterStackIsClean(final String timing) {
        final Meter currentMeter = Meter.getCurrentInstance();
        assertEquals(Meter.UNKNOWN_LOGGER_NAME, currentMeter.getCategory(),
                "Meter stack must be clean " + timing + ": found active Meter with category '" +
                currentMeter.getCategory() + "', but expected unknown logger '" + Meter.UNKNOWN_LOGGER_NAME + "'. " +
                "This indicates the thread-local Meter stack is not consistent.");
    }
}
