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

package org.usefultoys.test;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test annotation to automatically clear {@link org.usefultoys.slf4j.utils.ConfigParser} errors before and after each test.
 * <p>
 * This annotation ensures that tests using {@link org.usefultoys.slf4j.utils.ConfigParser}
 * start with a clean state, preventing error accumulation from previous test executions.
 * This is particularly important for tests that intentionally trigger parsing errors to
 * validate error handling behavior.
 * <p>
 * <b>Usage on test class:</b>
 * <pre>{@code
 * @ClearParserErrors
 * class ConfigParserTest {
 *     @Test
 *     void testInvalidConfig() {
 *         // ConfigParser starts with no accumulated errors
 *     }
 * }
 * }</pre>
 * <p>
 * <b>Usage on test method:</b>
 * <pre>{@code
 * class MyTest {
 *     @Test
 *     @ClearParserErrors
 *     void testThatTriggersParsingErrors() {
 *         // Parser errors are cleared before and after this test only
 *     }
 * }
 * }</pre>
 * <p>
 * <b>When to use:</b>
 * <ul>
 *   <li>Tests that validate ConfigParser error handling</li>
 *   <li>Tests that parse invalid configuration strings</li>
 *   <li>Tests that need to ensure no residual errors from previous tests</li>
 * </ul>
 *
 * @see ClearConfigParser
 * @see org.usefultoys.slf4j.utils.ConfigParser
 * @author Daniel Felix Ferber
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@ExtendWith(ClearConfigParserExtension.class)
public @interface ClearConfigParser {
}

