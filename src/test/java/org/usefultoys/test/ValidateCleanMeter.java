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

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test annotation to validate that the Meter stack is clean before and after each test.
 * <p>
 * This annotation ensures that {@link org.usefultoys.slf4j.meter.Meter#getCurrentInstance()}
 * returns the "unknown" Meter (with category equal to {@code "???"}) before each test method.
 * <p>
 * <b>Before each test:</b> Automatically cleans any leftover Meter instances from previous tests,
 * providing a clean slate and preventing cascade failures.
 * <p>
 * <b>After each test:</b>
 * <ul>
 *   <li><b>If the test failed:</b> Automatically cleans the Meter stack to prevent cascade
 *       failures in subsequent tests. No validation is performed.</li>
 *   <li><b>If the test passed:</b> Validates that the stack is clean. If a non-unknown Meter
 *       is found, the test fails with a descriptive error message indicating the test did
 *       not properly clean up the Meter thread-local stack.</li>
 * </ul>
 * <p>
 * <b>Usage on test class:</b>
 * <pre>{@code
 * @ValidateCleanMeter
 * class MeterOperationTest {
 *     @Test
 *     void testMeterOperation() {
 *         // Test runs with Meter stack validation before and after
 *     }
 * }
 * }</pre>
 * <p>
 * <b>Common use cases:</b>
 * <ul>
 *   <li>Tests that create and use Meter instances</li>
 *   <li>Tests that verify Meter thread-local stack management</li>
 *   <li>Integration tests that need to ensure Meter cleanup</li>
 * </ul>
 *
 * @see ValidateCleanMeterExtension
 * @see org.usefultoys.slf4j.meter.Meter#getCurrentInstance()
 * @see org.usefultoys.slf4j.meter.Meter#UNKNOWN_LOGGER_NAME
 * @author Daniel Felix Ferber
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(ValidateCleanMeterExtension.class)
public @interface ValidateCleanMeter {
}
