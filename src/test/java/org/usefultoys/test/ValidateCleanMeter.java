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
 *   <li><b>If the test passed:</b>
 *     <ul>
 *       <li><b>If {@code expectDirtyStack = false} (default):</b> Validates that the stack is clean.
 *           If a non-unknown Meter is found, the test fails with a descriptive error message.</li>
 *       <li><b>If {@code expectDirtyStack = true}:</b> Validates that the stack is dirty (contains
 *           a non-unknown Meter). If the stack is clean, the test fails with a descriptive error.
 *           After successful validation, automatically cleans the Meter stack.</li>
 *     </ul>
 *   </li>
 * </ul>
 * <p>
 * The annotation can be applied at both class and method level. Method-level annotations take precedence
 * over class-level settings.
 * <p>
 * <b>Usage on test class (default behavior):</b>
 * <pre>{@code
 * @ValidateCleanMeter
 * class MeterOperationTest {
 *     @Test
 *     void testMeterOperation() {
 *         // Must leave stack clean
 *     }
 * }
 * }</pre>
 * <p>
 * <b>Usage with expectDirtyStack on specific test:</b>
 * <pre>{@code
 * @ValidateCleanMeter
 * class MeterOperationTest {
 *     @Test
 *     void testNormalOperation() {
 *         // Must leave stack clean
 *     }
 *
 *     @Test
 *     @ValidateCleanMeter(expectDirtyStack = true)
 *     void testThatLeavesMeterOnStack() {
 *         // Must leave stack dirty - test will fail if stack is clean
 *     }
 * }
 * }</pre>
 * <p>
 * <b>Common use cases:</b>
 * <ul>
 *   <li>Tests that create and use Meter instances</li>
 *   <li>Tests that verify Meter thread-local stack management</li>
 *   <li>Tests that intentionally leave Meters on stack (use {@code expectDirtyStack = true})</li>
 *   <li>Integration tests that need to ensure Meter cleanup</li>
 * </ul>
 *
 * @see ValidateCleanMeterExtension
 * @see org.usefultoys.slf4j.meter.Meter#getCurrentInstance()
 * @see org.usefultoys.slf4j.meter.Meter#UNKNOWN_LOGGER_NAME
 * @author Daniel Felix Ferber
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtendWith(ValidateCleanMeterExtension.class)
public @interface ValidateCleanMeter {
    /**
     * Indicates whether the test is expected to leave the Meter stack dirty.
     * <p>
     * <ul>
     *   <li><b>false (default):</b> The test must leave the stack clean. If a non-unknown Meter
     *       is found after the test passes, the test fails with a descriptive error.</li>
     *   <li><b>true:</b> The test must leave the stack dirty. After the test passes, the extension
     *       validates that a non-unknown Meter is on the stack. If the stack is clean, the test
     *       fails with a descriptive error. After successful validation, the stack is automatically cleaned.</li>
     * </ul>
     * <p>
     * Note: If the test fails, the stack is always cleaned regardless of this setting.
     *
     * @return {@code true} if the test is expected to leave the stack dirty, {@code false} otherwise
     */
    boolean expectDirtyStack() default false;
}
